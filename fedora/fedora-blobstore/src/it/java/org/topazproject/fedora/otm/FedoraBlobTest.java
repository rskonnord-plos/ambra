/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.fedora.otm;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
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

  private SessionFactory   factory   =  new SessionFactoryImpl();
  private FedoraBlobStore  blobStore =
    new FedoraBlobStore("http://localhost:9090/fedora/services/management", "fedoraAdmin", "fedoraAdmin");

  @BeforeClass
  public void setUp() throws OtmException {
    try {
      initFactory();
      initGraphs();
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

  @Test
  public void testCrud() throws Exception {
    log.info("Testing basic CRUD operations ...");

    final byte[] blob1 = "Hello world".getBytes("UTF-8");
    final byte[] blob2 = "Good bye world".getBytes("UTF-8");
    final Test1  t     = new Test1();
    t.setBlob(blob1);
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(t);
          assertNotNull(t.getId());
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Test1 t2 = session.get(Test1.class, t.getId());
          assertNotNull(t2);
          assertEquals(t.getId(), t2.getId());
          assertEquals(blob1, t2.getBlob());
          t2.setBlob(blob2);
          session.saveOrUpdate(t2);
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Test1 t2 = session.get(Test1.class, t.getId());
          assertNotNull(t2);
          assertEquals(t.getId(), t2.getId());
          assertEquals(blob2, t2.getBlob());
          session.delete(t2);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Test1 t2 = session.get(Test1.class, t.getId());

          if (t2 != null)
            assertNull(t2.getBlob());
        }
      });
  }

  private void initFactory() throws OtmException {
    log.info("initializing otm session factory ...");

    GraphConfig[] graphs =
      new GraphConfig[] { new GraphConfig("ri", URI.create("local:///topazproject#otmtest1"), null), };
    factory.setTripleStore(new MemStore());
    factory.setBlobStore(blobStore);

    blobStore.addBlobFactory(new DefaultFedoraBlobFactory());

    for (GraphConfig graph : graphs)
      factory.addGraph(graph);

    Class[] classes = new Class[] { Test1.class, Test2.class };

    for (Class c : classes)
      factory.preload(c);

    factory.validate();
  }

  private void initGraphs() throws OtmException {
    log.info("initializing mulgara test graphs ...");

    String[] names = new String[] { "ri", "grants", "revokes", "criteria" };

    for (final String name : names) {
      try {
        doInSession(new Action() { public void run(Session s) throws OtmException { s.dropGraph(name); } });
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to drop graph '" + name + "'", t);
      }

      doInSession(new Action() { public void run(Session s) throws OtmException { s.createGraph(name); } });
    }

  }

  /**
   * Run the given action within a session.
   *
   * @param action the action to run
   *
   * @throws OtmException
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

      throw e;
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
     * @throws OtmException
     */
    void run(Session session) throws OtmException;
  }

  public static class Test1 {
    private String id;
    private byte[] blob;

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
      return id;
    }

    /**
     * Set id.
     *
     * @param id the value to set.
     */
    @Id
    @GeneratedValue(generatorClass = "org.topazproject.fedora.otm.FedoraIdGenerator",
                    uriPrefix = "info:fedora/test1/")
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Get blob.
     *
     * @return blob as byte[].
     */
    public byte[] getBlob() {
      return blob;
    }

    /**
     * Set blob.
     *
     * @param blob the value to set.
     */
    @Blob
    public void setBlob(byte[] blob) {
      this.blob = blob;
    }
  }

  @Entity(graph = "ri")
  @UriPrefix("test2:test2/")
  public static class Test2 {
    private String    id;
    private String    name;
    private Test1     test1;

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
      return id;
    }

    /**
     * Set id.
     *
     * @param id the value to set.
     */
    @Id
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Get name.
     *
     * @return name as String.
     */
    public String getName() {
      return name;
    }

    /**
     * Set name.
     *
     * @param name the value to set.
     */
    @Predicate
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Get test1.
     *
     * @return test1 as String.
     */
    public Test1 getTest1() {
      return test1;
    }

    /**
     * Set test1.
     *
     * @param test1 the value to set.
     */
    @Predicate
    public void setTest1(Test1 test1) {
      this.test1 = test1;
    }
  }
}
