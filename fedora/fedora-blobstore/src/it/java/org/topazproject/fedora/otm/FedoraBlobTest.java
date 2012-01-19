/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.otm;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.MemStore;

/**
 * FedoraBlob persistence tests.
 *
 * @author Pradeep Krishnan
 */
public class FedoraBlobTest {
  private static final Log log       = LogFactory.getLog(FedoraBlobTest.class);
  private SessionFactory   factory   = new SessionFactoryImpl();
  private FedoraBlobStore  blobStore =
    new FedoraBlobStore("http://localhost:9090/fedora/services/management", "fedoraAdmin",
                        "fedoraAdmin");

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() throws OtmException {
    try {
      initFactory();
      initModels();
    } catch (OtmException e) {
      log.error("OtmException in setup", e);
      throw e;
    } catch (RuntimeException e) {
      log.error("Exception in setup", e);
      throw e;
    } catch (Error e) {
      log.error("Error in setup", e);
      throw e;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  public void testCrud() throws Exception {
    log.info("Testing basic CRUD operations ...");

    final byte[] blob1 = "Hello world".getBytes("UTF-8");
    final byte[] blob2 = "Good bye world".getBytes("UTF-8");
    final Test1  t     = new Test1();
    t.blob             = blob1;
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(t);
          assertNotNull(t.id);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          FedoraConnection con = new FedoraConnection(blobStore, session);
          assertEquals(blob1, blobStore.get(factory.getClassMetadata(Test1.class), t.id, con));

          Test1 t2 = session.get(Test1.class, t.id);
          assertNotNull(t2);
          assertEquals(t.id, t2.id);
          assertEquals(blob1, t2.blob);
          t2.blob = blob2;
          session.saveOrUpdate(t2);
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Test1 t2 = session.get(Test1.class, t.id);
          assertNotNull(t2);
          assertEquals(t.id, t2.id);
          assertEquals(blob2, t2.blob);
          session.delete(t2);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Test1 t2 = session.get(Test1.class, t.id);

          if (t2 != null)
            assertNull(t2.blob);
        }
      });
  }

  private void initFactory() throws OtmException {
    log.info("initializing otm session factory ...");

    ModelConfig[] models =
      new ModelConfig[] { new ModelConfig("ri", URI.create("local:///topazproject#otmtest1"), null), };
    factory.setTripleStore(new MemStore());
    factory.setBlobStore(blobStore);

    blobStore.addBlobFactory(new DefaultFedoraBlobFactory());

    for (ModelConfig model : models)
      factory.addModel(model);

    Class[] classes = new Class[] { Test1.class, Test2.class };

    for (Class c : classes)
      factory.preload(c);
  }

  private void initModels() throws OtmException {
    log.info("initializing mulgara test models ...");

    String[] names = new String[] { "ri", "grants", "revokes", "criteria" };

    for (String name : names) {
      ModelConfig model = factory.getModel(name);

      try {
        factory.getTripleStore().dropModel(model);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to drop model '" + name + "'", t);
      }

      factory.getTripleStore().createModel(model);
    }
  }

  /**
   * Run the given action within a session.
   *
   * @param action the action to run
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected void doInSession(Action action) throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();
      action.run(session);
      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

/**
   * The interface actions must implement.
   */
  protected static interface Action {
    /**
     * This is run within the context of a session.
     *
     * @param session the current transaction
     *
     * @throws OtmException DOCUMENT ME!
     */
    void run(Session session) throws OtmException;
  }

  public static class Test1 {
    @Id
    @GeneratedValue(generatorClass = "org.topazproject.fedora.otm.FedoraIdGenerator", uriPrefix = "info:fedora/test1/")
    public String                                                                                                                       id;
    @Blob
    public byte[]                                                                                                                       blob;
  }

  @Entity(model = "ri")
  @UriPrefix("test2:test2/")
  public static class Test2 {
    @Id
    public String    id;
    public String    name;
    public Test1     test1;
  }
}
