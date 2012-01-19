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
import org.testng.annotations.Test;

import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.PublicAnnotation;

/**
 * Tests for Criteria.
 *
 * @author Pradeep Krishnan
 */
@Test
public class CriteriaTest extends AbstractOtmTest {
  private static final Log log = LogFactory.getLog(CriteriaTest.class);
  private URI              id1 = URI.create("http://localhost/annotation/1");
  private URI              id2 = URI.create("http://localhost/annotation/2");
  private URI              id3 = URI.create("http://localhost/annotation/3");
  private URI              id4 = URI.create("http://localhost/annotation/4");

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
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testUnrestricted() {
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
  public void testEq() {
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
  public void testIdEq() {
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
  public void testDefaultConjunction() {
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
  public void testEmptyResultSet() {
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
  public void testConjunction() {
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
  public void testDisjunction() {
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
  public void testWalk1() {
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
  public void testWalk2() {
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
  public void testTrans1() {
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
  public void testTrans2() {
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
  public void testNe() {
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
  public void testMaxResults() {
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
  public void testFirstResult() {
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
  public void testAscOrder() {
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
  public void testDescOrder() {
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

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDescOrder"}
  )
  public void testGT() {
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
  public void testLT() {
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
  public void testLE() {
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
  public void testGE() {
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
  public void testExists() {
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
  public void testNotExists() {
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
  public void testChild() {
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
  public void testChildWithOrderByChild() {
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
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testChildWithOrderByChild"}
  )
  public void testChildWithOrderByChildMember() {
    log.info("Testing child criteria with order-by on child member ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Criteria criteria = session.createCriteria(Annotation.class);
          criteria.createCriteria("supersedes").addOrder(Order.desc("creator"))
                   .add(Restrictions.eq("annotates", "foo:1"));

          List l = criteria.addOrder(Order.desc("annotates")).list();
          assertEquals(2, l.size());

          Annotation a1 = (Annotation) l.get(0);
          Annotation a2 = (Annotation) l.get(1);
          assertEquals(id3, a1.getId());
          assertEquals(id2, a2.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testChildWithOrderByChildMember"}
  )
  public void testParamBinding() {
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

  private DetachedCriteria createDC() {
    DetachedCriteria dc = new DetachedCriteria("Annotation");
    dc.createCriteria("supersedes").addOrder(Order.desc("creator"))
       .add(Restrictions.eq("annotates", new Parameter("p1")));
    dc.addOrder(Order.desc("annotates"));

    return dc;
  }

  private void evaluateDC(DetachedCriteria dc, Session session)
                   throws OtmException {
    List l = dc.getExecutableCriteria(session).setParameter("p1", "foo:1").list();
    assertEquals(2, l.size());

    Annotation a1 = (Annotation) l.get(0);
    Annotation a2 = (Annotation) l.get(1);
    assertEquals(id3, a1.getId());
    assertEquals(id2, a2.getId());
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testParamBinding"}
  )
  public void testDC() {
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
  public void testDCSave() {
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
    assertEquals("" + dc.da.rdfType, factory.getClassMetadata(Annotation.class).getType());
    assertEquals("" + dc.getOrderList().iterator().next().da.predicateUri,
                 factory.getClassMetadata(Annotation.class).getMapperByName("annotates").getUri());
    dc = dc.getChildCriteriaList().iterator().next();
    assertEquals("" + dc.da.rdfType, factory.getClassMetadata(Annotation.class).getType());
    assertEquals("" + dc.da.predicateUri,
                 factory.getClassMetadata(Annotation.class).getMapperByName("supersedes").getUri());
    assertEquals("" + dc.getOrderList().iterator().next().da.predicateUri,
                 factory.getClassMetadata(Annotation.class).getMapperByName("creator").getUri());
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testDCSave"}
  )
  public void testDCLoad() {
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
}
