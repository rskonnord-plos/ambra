/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;

/**
 * Session tests.
 *
 * @author Pradeep Krishnan
 */
@Test(sequential = true)
public class SessionTest extends AbstractOtmTest {
  private static final Log  log     = LogFactory.getLog(SessionTest.class);
  private final URI         id1     = URI.create("http://localhost/annotation/1");
  private final URI         id2     = URI.create("http://localhost/annotation/2");
  private final URI         id3     = URI.create("http://localhost/annotation/3");
  private final URI         foo     = URI.create("foo:1");
  private Session           session;
  private Transaction       txn;
  private Article           a;
  private PublicAnnotation  a1;
  private PublicAnnotation  a2;
  private PrivateAnnotation a3;

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
      setUpData();
      createSession();
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

  private void setUpData() {
    log.info("Setting up data for tests ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a                 = new Article();
          a.setUri(foo);

          Annotation a1 = new PublicAnnotation(id1);
          Annotation a2 = new PublicAnnotation(id2);
          Annotation a3 = new PrivateAnnotation(id3);

          a1.setAnnotates(foo);
          a2.setAnnotates(foo);
          a3.setAnnotates(foo);

          session.saveOrUpdate(a);
          session.saveOrUpdate(a1);
          session.saveOrUpdate(a2);
          session.saveOrUpdate(a3);
        }
      });
  }

  private void createSession() throws OtmException {
    log.info("Setting up session for tests ...");
    session   = factory.openSession();
    txn       = session.beginTransaction();
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @AfterClass
  public void endSession() throws OtmException {
    log.info("Closing session after tests ...");

    try {
      if (txn != null)
        txn.commit();
    } catch (OtmException e) {
      try {
        if (txn != null)
          txn.rollback();
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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test
  public void testLoad() throws OtmException {
    log.info("Testing/Loading data ...");

    a = session.load(Article.class, foo.toString());

    List<PublicAnnotation> al = a.getPublicAnnotations();
    assertEquals(2, al.size());

    List<PrivateAnnotation> pl = a.getPrivateAnnotations();
    assertEquals(1, pl.size());

    a1   = al.get(0);
    a2   = al.get(1);
    a3   = pl.get(0);
    assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
    assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
    assertEquals(foo, a1.getAnnotates());
    assertEquals(foo, a2.getAnnotates());
    assertEquals(id3, a3.getId());
    assertEquals(foo, a3.getAnnotates());
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testLoad"}
  )
  public void testCriteriaSessionCache() throws OtmException {
    log.info("Testing usage of session cache by criteria ...");

    List l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo)).list();
    assertEquals(3, l.size());

    for (Object o : l) {
      assertEquals(foo, ((Annotation) o).getAnnotates());

      if (o instanceof PublicAnnotation)
        assertTrue((a1 == o) || (a2 == o));

      if (o instanceof PrivateAnnotation)
        assertTrue(a3 == o);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testLoad"}
  )
  public void testOQLSessionCache() throws OtmException {
    log.info("Testing usage of session cache by OQL ...");

    Results r =
      session.createQuery("select a from Annotation a where a.annotates = <foo:1>;").execute();
    List    l = new ArrayList();

    while (r.next())
      l.add(r.get(0));

    assertEquals(3, l.size());

    for (Object o : l) {
      assertEquals(foo, ((Annotation) o).getAnnotates());

      if (o instanceof PublicAnnotation)
        assertTrue((a1 == o) || (a2 == o));

      if (o instanceof PrivateAnnotation)
        assertTrue(a3 == o);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testCriteriaSessionCache", "testOQLSessionCache"}
  )
  public void testDelete() throws OtmException {
    log.info("Testing removal of objects from session cache by delete ...");

    session.delete(a);
    assertNull(session.get(Article.class, a.getUri().toString()));
    assertNull(session.get(Annotation.class, a1.getId().toString()));
    assertNull(session.get(Annotation.class, a2.getId().toString()));
    assertNull(session.get(Annotation.class, a3.getId().toString()));

    List l = session.createCriteria(Article.class).add(Restrictions.eq("uri", foo)).list();
    assertEquals(0, l.size());
    l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo)).list();
    assertEquals(0, l.size());

    Results r =
      session.createQuery("select a from Annotation a where a.annotates = <foo:1>;").execute();
    assertFalse(r.next());
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDelete"}
  )
  public void testFlush() throws OtmException {
    log.info("Testing removal of objects from session cache by delete and flush...");

    session.flush();
    assertNull(session.get(Article.class, a.getUri().toString()));
    assertNull(session.get(Annotation.class, a1.getId().toString()));
    assertNull(session.get(Annotation.class, a2.getId().toString()));
    assertNull(session.get(Annotation.class, a3.getId().toString()));

    List l = session.createCriteria(Article.class).add(Restrictions.eq("uri", foo)).list();
    assertEquals(0, l.size());
    l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo)).list();
    assertEquals(0, l.size());

    Results r =
      session.createQuery("select a from Annotation a where a.annotates = <foo:1>;").execute();
    assertFalse(r.next());
  }
}
