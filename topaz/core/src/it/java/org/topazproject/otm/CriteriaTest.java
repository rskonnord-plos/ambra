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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.SpecialMappers;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * Tests for Criteria.
 *
 * @author Pradeep Krishnan
 */
@Test
public class CriteriaTest extends AbstractOtmTest {
  private static final Log log   = LogFactory.getLog(CriteriaTest.class);
  private URI              id1   = URI.create("http://localhost/annotation/1");
  private URI              id2   = URI.create("http://localhost/annotation/2");
  private URI              id3   = URI.create("http://localhost/annotation/3");
  private URI              id4   = URI.create("http://localhost/annotation/4");
  private URI              idl1  = URI.create("http://localhost/annotation-link/1");
  private URI              idl2  = URI.create("http://localhost/annotation-link/2");
  private String           sm1Id = "http://localhost/sm/1";
  private String           sm2Id = "http://localhost/sm/2";

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

  private void setUpData() throws OtmException {
    log.info("Setting up the data for tests ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a1 = new PublicAnnotation(id1);
          Annotation a2 = new PublicAnnotation(id2);
          Annotation a3 = new PublicAnnotation(id3);
          Annotation a4 = new PublicAnnotation(id4);

          a1.setAnnotates(URI.create("foo:1"));
          a2.setAnnotates(URI.create("foo:1"));
          a3.setAnnotates(URI.create("bar:1"));

          a1.setCreator("aa");
          a2.setCreator("bb");
          a3.setCreator("cc");
          a4.setCreator("dd");

          a3.setCreated(new Date());

          a1.setSupersededBy(a2);
          a2.setSupersedes(a1);
          a2.setSupersededBy(a3);
          a3.setSupersedes(a2);

          a4.setTitle("foo");

          session.saveOrUpdate(a1);
          session.saveOrUpdate(a2);
          session.saveOrUpdate(a3);
          session.saveOrUpdate(a4);

          SpecialMappers sm1 = new SpecialMappers(sm1Id);
          SpecialMappers sm2 = new SpecialMappers(sm2Id);

          sm1.list.add("a1");
          sm1.seq.add("a1");
          sm2.list.add("b1");
          sm2.list.add("b2");
          sm2.seq.add("b1");
          sm2.seq.add("b2");

          session.saveOrUpdate(sm1);
          session.saveOrUpdate(sm2);

        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testUnrestricted() throws OtmException {
    log.info("Testing unrestricted ...");

    final List<Annotation> al = new ArrayList();
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          al.addAll(session.createCriteria(Annotation.class).list());
        }
      });
    assertNotNull(al);
    assertEquals(4, al.size());

    for (Annotation a : al) {
      if (a.getSupersedes() != null)
        assertTrue(a == a.getSupersedes().getSupersededBy());

      if (a.getSupersededBy() != null)
        assertTrue(a == a.getSupersededBy().getSupersedes());
    }
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testUnrestricted"}
  )
  public void testEq() throws OtmException {
    log.info("Testing EQ ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                        .list();

          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);

          assertEquals(URI.create("foo:1"), a1.getAnnotates());
          assertEquals(URI.create("foo:1"), a2.getAnnotates());

          assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
          assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testEq"}
  )
  public void testIdEq() throws OtmException {
    log.info("Testing EQ on id field ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l = session.createCriteria(Annotation.class).add(Restrictions.eq("id", id3)).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);
          assertEquals(URI.create("bar:1"), a1.getAnnotates());
          assertEquals(id3, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testIdEq"}
  )
  public void testDefaultConjunction() throws OtmException {
    log.info("Testing default conjunction ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                        .add(Restrictions.eq("id", id1)).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);
          assertEquals(URI.create("foo:1"), a1.getAnnotates());
          assertEquals(id1, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDefaultConjunction"}
  )
  public void testEmptyResultSet() throws OtmException {
    log.info("Testing empty result-set ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                        .add(Restrictions.eq("id", id3)).list();

          assertEquals(0, l.size());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testEmptyResultSet"}
  )
  public void testConjunction() throws OtmException {
    log.info("Testing conjunction ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.conjunction().add(Restrictions.eq("annotates", "foo:1"))
                                          .add(Restrictions.eq("id", id1))).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);
          assertEquals(URI.create("foo:1"), a1.getAnnotates());
          assertEquals(id1, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testConjunction"}
  )
  public void testDisjunction() throws OtmException {
    log.info("Testing disjunction ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.disjunction().add(Restrictions.eq("annotates", "foo:1"))
                                          .add(Restrictions.eq("id", id1))).list();

          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);

          assertEquals(URI.create("foo:1"), a1.getAnnotates());
          assertEquals(URI.create("foo:1"), a2.getAnnotates());

          assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
          assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDisjunction"}
  )
  public void testWalk1() throws OtmException {
    log.info("Testing walk 1 ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.walk("supersededBy", id3.toString())).list();

          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);

          assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
          assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testWalk1"}
  )
  public void testWalk2() throws OtmException {
    log.info("Testing walk 2 ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.walk("supersedes", id2.toString())).list();

          assertEquals(1, l.size());

          Annotation a3 = (Annotation) l.get(0);

          assertEquals(id3, a3.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testWalk2"}
  )
  public void testTrans1() throws OtmException {
    log.info("Testing trans 1 ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.trans("supersededBy", id3.toString())).list();

          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);

          assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
          assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testTrans1"}
  )
  public void testTrans2() throws OtmException {
    log.info("Testing trans 2 ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .add(Restrictions.trans("supersedes", id2.toString())).list();

          assertEquals(1, l.size());

          Annotation a3 = (Annotation) l.get(0);

          assertEquals(id3, a3.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testTrans2"}
  )
  public void testNe() throws OtmException {
    log.info("Testing NE ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "foo:1"))
                        .list();

          assertEquals(2, l.size());

          Annotation a3 = (Annotation) l.get(0);

          Annotation a4 = (Annotation) l.get(1);

          assertTrue(id3.equals(a3.getId()) || id4.equals(a3.getId()));
          assertTrue(id3.equals(a4.getId()) || id4.equals(a4.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testNe"}
  )
  public void testMaxResults() throws OtmException {
    log.info("Testing max-results ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                        .setFirstResult(0).setMaxResults(1).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);

          assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testMaxResults"}
  )
  public void testFirstResult() throws OtmException {
    log.info("Testing first-result ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                        .setFirstResult(1).setMaxResults(1).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);

          assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testFirstResult"}
  )
  public void testAscOrder() throws OtmException {
    log.info("Testing asc order ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.asc("creator")).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);

          assertEquals(id2, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testAscOrder"}
  )
  public void testDescOrder() throws OtmException {
    log.info("Testing desc order ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.desc("creator")).list();

          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);

          assertEquals(id2, a1.getId());
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testDescOrder"}
  )
  public void testIdOrder() throws OtmException {
    log.info("Testing id order ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class)
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.asc("id")).list();

          assertEquals(1, l.size());
          Annotation a1 = (Annotation) l.get(0);
          assertEquals(id2, a1.getId());

          l = session.createCriteria(Annotation.class)
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.desc("id")).list();

          assertEquals(1, l.size());
          a1 = (Annotation) l.get(0);
          assertEquals(id3, a1.getId());

          l = session.createCriteria(Annotation.class)
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.asc("id"))
                        .addOrder(Order.desc("creator")).list();

          assertEquals(1, l.size());
          a1 = (Annotation) l.get(0);
          assertEquals(id2, a1.getId());

          l = session.createCriteria(Annotation.class)
                        .setFirstResult(1).setMaxResults(1).addOrder(Order.desc("id"))
                        .addOrder(Order.desc("creator")).list();

          assertEquals(1, l.size());
          a1 = (Annotation) l.get(0);
          assertEquals(id3, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testIdOrder"}
  )
  public void testGT() throws OtmException {
    log.info("Testing GT ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.gt("creator", "bb"))
                        .addOrder(Order.asc("creator")).list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id4, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testGT"}
  )
  public void testLT() throws OtmException {
    log.info("Testing LT ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.lt("creator", "bb")).list();
          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);
          assertEquals(id1, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testLT"}
  )
  public void testLE() throws OtmException {
    log.info("Testing LE ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.le("creator", "bb"))
                        .addOrder(Order.asc("creator")).list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id1, a1.getId());
          assertEquals(id2, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testLE"}
  )
  public void testGE() throws OtmException {
    log.info("Testing GE ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.ge("creator", "bb"))
                        .addOrder(Order.desc("creator")).list();
          assertEquals(3, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          Annotation a3 = (Annotation) l.get(2);
          assertEquals(id4, a1.getId());
          assertEquals(id3, a2.getId());
          assertEquals(id2, a3.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testGE"}
  )
  public void testExists() throws OtmException {
    log.info("Testing exists() ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.exists("created"))
                        .addOrder(Order.desc("creator")).list();
          assertEquals(1, l.size());

          Annotation a1 = (Annotation) l.get(0);
          assertEquals(id3, a1.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testExists"}
  )
  public void testNotExists() throws OtmException {
    log.info("Testing notExists() ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).add(Restrictions.notExists("created"))
                        .addOrder(Order.asc("creator")).list();
          assertEquals(3, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          Annotation a3 = (Annotation) l.get(2);
          assertEquals(id1, a1.getId());
          assertEquals(id2, a2.getId());
          assertEquals(id4, a3.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testNotExists"}
  )
  public void testChild() throws OtmException {
    log.info("Testing child criteria ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).addOrder(Order.desc("annotates"))
                        .createCriteria("supersedes").add(Restrictions.eq("annotates", "foo:1"))
                        .list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id2, a1.getId());
          assertEquals(id3, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testChild"}
  )
  public void testChildWithOrderByChild() throws OtmException {
    log.info("Testing child criteria with order-by on child ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l =
                session.createCriteria(Annotation.class).addOrder(Order.asc("supersedes"))
                        .createCriteria("supersedes").add(Restrictions.eq("annotates", "foo:1"))
                        .list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id2, a1.getId());
          assertEquals(id3, a2.getId());

          l = session.createCriteria(Annotation.class).addOrder(Order.desc("supersedes"))
                        .createCriteria("supersedes").add(Restrictions.eq("annotates", "foo:1"))
                        .list();
          assertEquals(2, l.size());

          a1 = (Annotation) l.get(0);
          a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id2, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testChildWithOrderByChild"}
  )
  public void testChildWithOrderByChildMember() throws OtmException {
    log.info("Testing child criteria with order-by on child member ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          // order on non-id
          Criteria criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.asc("creator"))
                   .add(Restrictions.eq("annotates", "foo:1"));

          List l = criteria.addOrder(Order.desc("annotates")).list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id2, a1.getId());
          assertEquals(id3, a2.getId());

          criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.desc("creator"))
                   .add(Restrictions.eq("annotates", "foo:1"));

          l = criteria.addOrder(Order.desc("annotates")).list();
          assertEquals(2, l.size());

          a1 = (Annotation) l.get(0);
          a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id2, a2.getId());

          // order on id
          criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.asc("id"))
                   .add(Restrictions.eq("annotates", "foo:1"));

          l = criteria.addOrder(Order.desc("annotates")).list();
          assertEquals(2, l.size());

          a1 = (Annotation) l.get(0);
          a2 = (Annotation) l.get(1);
          assertEquals(id2, a1.getId());
          assertEquals(id3, a2.getId());

          criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.desc("id"))
                   .add(Restrictions.eq("annotates", "foo:1"));

          l = criteria.addOrder(Order.desc("annotates")).list();
          assertEquals(2, l.size());

          a1 = (Annotation) l.get(0);
          a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id2, a2.getId());
        }
      });
  }

  @Entity(name = "AnnotationLink", model = "ri")
  @UriPrefix(Rdf.topaz)
  public static class AnnotationLink {
    @Id
    public URI        id;
    public Annotation ann1;
    public Annotation ann2;
    @Predicate(inverse = true)
    public Annotation annR;
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testChildWithOrderByChildMember"}
  )
  public void testReferrer() throws OtmException {
    log.info("Testing referrer criteria ...");

    factory.preload(AnnotationLink.class);

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          AnnotationLink l1 = new AnnotationLink();
          AnnotationLink l2 = new AnnotationLink();
          l1.id = idl1;
          l2.id = idl2;

          l1.ann1 = session.get(Annotation.class, id1.toString());
          l1.ann2 = session.get(Annotation.class, id2.toString());
          l2.ann1 = session.get(Annotation.class, id3.toString());
          l2.ann2 = session.get(Annotation.class, id4.toString());

          session.saveOrUpdate(l1);
          session.saveOrUpdate(l2);
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l = session.createCriteria(Annotation.class)
                          .createReferrerCriteria("AnnotationLink", "ann1")
                          .add(new SubjectCriterion(idl1.toString()))
                          .list();
          assertEquals(1, l.size());

          Annotation a = (Annotation) l.get(0);
          assertEquals(id1, a.getId());

          l = session.createCriteria(Annotation.class)
                     .createReferrerCriteria("AnnotationLink", "ann2")
                     .createCriteria("ann1")
                     .add(Restrictions.eq("annotates", "bar:1"))
                     .list();
          assertEquals(1, l.size());

          a = (Annotation) l.get(0);
          assertEquals(id4, a.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testReferrer"}
  )
  public void testReferrerWithInvPredicate() throws OtmException {
    log.info("Testing referrer criteria with inverse predicate ...");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          AnnotationLink l = session.get(AnnotationLink.class, idl1.toString());
          Annotation     a = session.get(Annotation.class, id3.toString());
          l.annR = a;
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l = session.createCriteria(Annotation.class)
                          .createReferrerCriteria("AnnotationLink", "annR")
                          .add(Restrictions.exists("ann1"))
                          .list();
          assertEquals(1, l.size());

          Annotation a = (Annotation) l.get(0);
          assertEquals(id3, a.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testReferrerWithInvPredicate"}
  )
  public void testReferrerWithOrderByChildMember() throws OtmException {
    log.info("Testing referrer criteria with order-by on child member ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          List l = session.createCriteria(Annotation.class)
                          .createReferrerCriteria("AnnotationLink", "ann1")
                          .addOrder(Order.asc("id")).add(Restrictions.exists("ann2"))
                          .list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id1, a1.getId());
          assertEquals(id3, a2.getId());

          l = session.createCriteria(Annotation.class)
                          .createReferrerCriteria("AnnotationLink", "ann1")
                          .addOrder(Order.desc("id")).add(Restrictions.exists("ann2"))
                          .list();
          assertEquals(2, l.size());

          a1 = (Annotation) l.get(0);
          a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id1, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testReferrerWithOrderByChildMember"}
  )
  public void testParamBinding() throws OtmException {
    log.info("Testing param binding ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Criteria criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.desc("creator"))
                   .add(Restrictions.eq("annotates", new Parameter("p1")));
          criteria.addOrder(Order.desc("annotates"));
          criteria.setParameter("p1", "foo:1");

          List l = criteria.list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id2, a2.getId());
        }
      });
  }

  private DetachedCriteria createDC() throws OtmException {
    DetachedCriteria dc = new DetachedCriteria("Annotation");
    dc.createCriteria("supersedes").addOrder(Order.desc("creator"))
       .add(Restrictions.eq("annotates", new Parameter("p1")))
       .createReferrerCriteria("AnnotationLink", "ann1")
       .add(Restrictions.exists("ann2"));
    dc.addOrder(Order.desc("annotates"));

    return dc;
  }

  private void evaluateDC(DetachedCriteria dc, Session session)
                   throws OtmException {
    List l = dc.getExecutableCriteria(session).setParameter("p1", "foo:1").list();
    assertEquals(1, l.size());

    Annotation a1 = (Annotation) l.get(0);
    assertEquals(id2, a1.getId());
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testParamBinding"}
  )
  public void testDC() throws OtmException {
    log.info("Testing detached criteria ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          evaluateDC(createDC(), session);
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDC"}
  )
  public void testDCSave() throws OtmException {
    log.info("Testing detached criteria saveOrUpdate ...");

    final DetachedCriteria dc = createDC();
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(dc);
        }
      });
    verifyDC(dc);
  }

  private void verifyDC(DetachedCriteria dc) {
    assertEquals(dc.da.rdfType, factory.getClassMetadata(Annotation.class).getTypes());
    assertEquals("" + dc.getOrderList().iterator().next().da.predicateUri,
                 getMapper(Annotation.class, "annotates").getUri());

    assertEquals(1, dc.getChildCriteriaList().size());
    dc = dc.getChildCriteriaList().iterator().next();
    assertEquals(dc.da.rdfType, factory.getClassMetadata(Annotation.class).getTypes());
    assertEquals("" + dc.da.predicateUri,
                 getMapper(Annotation.class, "supersedes").getUri());
    assertEquals("" + dc.getOrderList().iterator().next().da.predicateUri,
                 getMapper(Annotation.class, "creator").getUri());

    assertEquals(1, dc.getChildCriteriaList().size());
    dc = dc.getChildCriteriaList().iterator().next();
    assertEquals(dc.da.rdfType, factory.getClassMetadata(AnnotationLink.class).getTypes());
    assertEquals("" + dc.da.predicateUri,
                 getMapper(AnnotationLink.class, "ann1").getUri());
  }

  private RdfMapper getMapper(Class c, String name) {
    return (RdfMapper)factory.getClassMetadata(c).getMapperByName(name);
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDCSave"}
  )
  public void testDCLoad() throws OtmException {
    log.info("Testing detached criteria load ...");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          DetachedCriteria dc =
                (DetachedCriteria) session.createCriteria(DetachedCriteria.class)
                                           .add(Restrictions.eq("alias", "Annotation")).list()
                                           .iterator().next();

          evaluateDC(dc, session);
          verifyDC(dc);
        }
      });
  }

  @Test(dependsOnMethods =  {"testDCLoad"})
  public void testCollections() throws OtmException {
    for (String field : new String[] {"list", "seq"}) {
      log.info("Testing EQ on an rdf:" + field + " field ...");
      doCollectionsTest("eq", field, "a1", new String[] {sm1Id});
      doCollectionsTest("eq", field, "b1", new String[] {sm2Id});
      doCollectionsTest("eq", field, "b2", new String[] {sm2Id});
      doCollectionsTest("eq", field, "c1", new String[] {});

      log.info("Testing NE on an rdf:" + field + " field ...");
      doCollectionsTest("ne", field, "a1", new String[] {sm2Id});
      doCollectionsTest("ne", field, "b1", new String[] {sm1Id});
      doCollectionsTest("ne", field, "b2", new String[] {sm1Id});
      doCollectionsTest("ne", field, "c1", new String[] {sm1Id, sm2Id});

      log.info("Testing GT on an rdf:" + field + " field ...");
      doCollectionsTest("gt", field, "a1", new String[] {sm2Id});
      doCollectionsTest("gt", field, "b1", new String[] {sm2Id});
      doCollectionsTest("gt", field, "b2", new String[] {});
      doCollectionsTest("gt", field, "c1", new String[] {});

      log.info("Testing LT on an rdf:" + field + " field ...");
      doCollectionsTest("lt", field, "a1", new String[] {});
      doCollectionsTest("lt", field, "b1", new String[] {sm1Id});
      doCollectionsTest("lt", field, "b2", new String[] {sm1Id, sm2Id});
      doCollectionsTest("lt", field, "c1", new String[] {sm1Id, sm2Id});

      log.info("Testing GE on an rdf:" + field + " field ...");
      doCollectionsTest("ge", field, "a1", new String[] {sm1Id, sm2Id});
      doCollectionsTest("ge", field, "b1", new String[] {sm2Id});
      doCollectionsTest("ge", field, "b2", new String[] {sm2Id});
      doCollectionsTest("ge", field, "c1", new String[] {});

      log.info("Testing LE on an rdf:" + field + " field ...");
      doCollectionsTest("le", field, "a1", new String[] {sm1Id});
      doCollectionsTest("le", field, "b1", new String[] {sm1Id, sm2Id});
      doCollectionsTest("le", field, "b2", new String[] {sm1Id, sm2Id});
      doCollectionsTest("le", field, "c1", new String[] {sm1Id, sm2Id});
    }
  }

  private void doCollectionsTest(final String op, final String field, final String val, 
      final String[] results) throws OtmException {
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Criterion crit;
          try {
            crit = (Criterion)Restrictions.class
                             .getDeclaredMethod(op, String.class, Object.class)
                             .invoke(null, field, val);
          } catch (Exception e) {
            throw new OtmException("", e);
          }
          List<SpecialMappers> l = session.createCriteria(SpecialMappers.class)
                                          .add(crit).list();
          assertEquals(results.length, l.size());
          for (SpecialMappers sm : l) {
            boolean found = false;
            for (String id : results)
               if (id.equals(sm.id))
                  found = true;
            assertTrue(sm.id + " not found in " + results, found);
          }
        }
      });
  }
}
