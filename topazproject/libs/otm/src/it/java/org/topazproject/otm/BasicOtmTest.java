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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Annotea;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.Grants;
import org.topazproject.otm.samples.NoPredicate;
import org.topazproject.otm.samples.NoRdfType;
import org.topazproject.otm.samples.ObjectInfo;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.ReplyThread;
import org.topazproject.otm.samples.SampleEmbeddable;
import org.topazproject.otm.samples.SpecialMappers;

/**
 * Basic OTM persistence tests.
 *
 * @author Pradeep Krishnan
 */
public class BasicOtmTest extends AbstractOtmTest {
  private static final Log log = LogFactory.getLog(BasicOtmTest.class);

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
   * @throws OtmException DOCUMENT ME!
   */
  @Test
  public void testCrud() throws OtmException {
    log.info("Testing basic CRUD operations ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = (Annotation) session.get(Annotea.class, "http://localhost/annotation/1");
          assertNotNull(a);

          a.setCreator("Pradeep");
          a.setState(42);
          a.setType(Annotation.NS + "Comment");

          if (a.foobar == null)
            a.foobar = new SampleEmbeddable();

          a.foobar.foo   = "FOO";
          a.foobar.bar = "BAR";

          session.saveOrUpdate(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = (Annotation) session.get(Annotea.class, "http://localhost/annotation/1");

          assertNotNull(a);
          assertNotNull(a.foobar);

          assertTrue(a instanceof PublicAnnotation);

          assertEquals(42, a.getState());
          assertEquals("Pradeep", a.getCreator());
          assertEquals(Annotation.NS + "Comment", a.getType());
          assertEquals("FOO", a.foobar.foo);
          assertEquals("BAR", a.foobar.bar);

          session.delete(a);

          a = session.get(Annotation.class, "http://localhost/annotation/1");

          assertNull(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");
          assertNull(a);
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testCrud"}
  )
  public void testNoRdfType() throws OtmException {
    log.info("Testing classes with no rdf:type ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(new NoRdfType("http://localhost/noRdfType/1"));
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          NoRdfType n = session.get(NoRdfType.class, "http://localhost/noRdfType/1");
          assertNotNull(n);

          n = session.get(NoRdfType.class, "http://localhost/noRdfType/2");
          assertNull(n);
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testNoRdfType"}
  )
  public void testNoPredicate() throws OtmException {
    log.info("Testing classes with no predicates ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          session.saveOrUpdate(new NoPredicate("http://localhost/noPredicate/1"));
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          NoPredicate np = session.get(NoPredicate.class, "http://localhost/noPredicate/1");
          assertNotNull(np);

          np = session.get(NoPredicate.class, "http://localhost/noPredicate/2");
          assertNull(np);
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testNoPredicate"}
  )
  public void testAssociations() throws OtmException {
    log.info("Testing associations ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
          a.setAnnotates(URI.create("http://www.plosone.org"));

          Annotation sa = new PublicAnnotation(URI.create("http://localhost/annotation/1/1"));
          sa.setAnnotates(URI.create("http://www.plosone.org"));

          a.setSupersededBy(sa);
          sa.setSupersedes(a);

          session.saveOrUpdate(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1/1");

          assertNotNull(a);

          Annotation old = a.getSupersedes();
          assertNotNull(old);

          assertTrue(old.getSupersededBy() == a);
          assertEquals(URI.create("http://localhost/annotation/1"), old.getId());
          assertEquals(URI.create("http://www.plosone.org"), old.getAnnotates());
          assertEquals(URI.create("http://www.plosone.org"), a.getAnnotates());
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testAssociations"}
  )
  public void testInverse() throws OtmException {
    log.info("Testing inverse associations ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation  a  = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
          ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/1"));
          ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/1/1"));

          a.addReply(r);
          r.addReply(rr);

          session.saveOrUpdate(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

          assertNotNull(a);

          List<ReplyThread> replies = a.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          ReplyThread r = replies.get(0);
          assertNotNull(r);
          assertEquals(URI.create("http://localhost/reply/1"), r.getId());

          replies = r.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          r = replies.get(0);
          assertNotNull(r);
          assertEquals(URI.create("http://localhost/reply/1/1"), r.getId());
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testInverse"}
  )
  public void testSpecialMappers() throws OtmException {
    log.info("Testing special mappers (rdf:list etc.) ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = new SpecialMappers("http://localhost/sm/1");

          for (int i = 1; i < 12; i++) {
            m.list.add("l" + i);
            m.bag.add("b" + i);
            m.seq.add("s" + i);
            m.alt.add("a" + i);
          }

          session.saveOrUpdate(m);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);

          assertEquals(11, m.list.size());
          assertEquals(11, m.bag.size());
          assertEquals(11, m.seq.size());
          assertEquals(11, m.alt.size());

          for (int i = 1; i < 12; i++) {
            assertTrue(m.list.get(i - 1).equals("l" + i));
            assertTrue(m.bag.contains("b" + i));
            assertTrue(m.seq.get(i - 1).equals("s" + i));
            assertTrue(m.alt.contains("a" + i));
          }
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testSpecialMappers"}
  )
  public void testPredicateMap() throws OtmException {
    log.info("Testing predicate-map ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Grants g = new Grants();
          g.resource = "http://localhost/articles/1";
          g.permissions.put("perm:1", Collections.singletonList("user:1"));
          g.permissions.put("perm:2", Collections.singletonList("user:1"));

          session.saveOrUpdate(g);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Grants g = session.get(Grants.class, "http://localhost/articles/1");
          assertNotNull(g);

          assertEquals(2, g.permissions.size());

          List<String> u = g.permissions.get("perm:1");
          assertNotNull(u);
          assertEquals(1, u.size());
          assertEquals("user:1", u.get(0));

          List<Grants> l =
                session.createCriteria(Grants.class)
                        .add(Restrictions.eq("resource", "http://localhost/articles/1")).list();
          assertEquals(1, l.size());

          Grants g1 = l.get(0);
          assertTrue(g == g1);

          assertEquals(2, g.permissions.size());

          u = g.permissions.get("perm:1");
          assertNotNull(u);
          assertEquals(1, u.size());
          assertEquals("user:1", u.get(0));

          u = g.permissions.get("perm:2");
          assertNotNull(u);
          assertEquals(1, u.size());
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testPredicateMap"}
  )
  public void testBackPointer() throws OtmException {
    log.info("Testing back-pointer ...");

    final URI aid  = URI.create("http://localhost/article/1");
    final URI pid1 = URI.create("http://localhost/article/1/part/1");
    final URI pid2 = URI.create("http://localhost/article/1/part/2");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = new Article();
          a.setUri(aid);

          ObjectInfo p1 = new ObjectInfo();
          ObjectInfo p2 = new ObjectInfo();
          p1.setUri(pid1);
          p2.setUri(pid2);
          p1.setIsPartOf(a);
          p2.setIsPartOf(a);
          a.getParts().add(p1);
          a.getParts().add(p2);

          session.saveOrUpdate(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = session.get(Article.class, aid.toString());

          assertNotNull(a);
          assertEquals(2, a.getParts().size());

          for (ObjectInfo o : a.getParts())
            assertTrue(o.getIsPartOf() == a);
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testBackPointer"}
  )
  public void testAssocWithSamePredUri() throws OtmException {
    log.info("Testing associations with same predicate-uri ...");

    final URI id1 = URI.create("http://localhost/annotation/1");
    final URI id2 = URI.create("http://localhost/annotation/2");
    final URI id3 = URI.create("http://localhost/annotation/3");
    final URI foo = URI.create("foo:1");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = new Article();
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
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article                a  = session.load(Article.class, foo.toString());

          List<PublicAnnotation> al = a.getPublicAnnotations();
          assertEquals(2, al.size());

          List<PrivateAnnotation> pl = a.getPrivateAnnotations();
          assertEquals(1, pl.size());

          PublicAnnotation  a1 = al.get(0);
          PublicAnnotation  a2 = al.get(1);
          PrivateAnnotation a3 = pl.get(0);
          assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
          assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
          assertEquals(foo, a1.getAnnotates());
          assertEquals(foo, a2.getAnnotates());
          assertTrue(id3.equals(a3.getId()));
          assertEquals(foo, a3.getAnnotates());

          List l =
                session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo))
                        .list();
          assertEquals(3, l.size());

          for (Object o : l) {
            assertEquals(foo, ((Annotation) o).getAnnotates());

            if (o instanceof PublicAnnotation)
              assertTrue((a1 == o) || (a2 == o));
          }

          Results r =
                session.createQuery("select a from Annotation a where a.annotates = <foo:1>;")
                        .execute();
          l.clear();

          while (r.next())
            l.add(r.get(0));

          assertEquals(3, l.size());

          for (Object o : l) {
            assertEquals(foo, ((Annotation) o).getAnnotates());

            if (o instanceof PublicAnnotation)
              assertTrue((a1 == o) || (a2 == o));
          }

          session.delete(a);
          assertNull(session.get(Article.class, a.getUri().toString()));
          session.flush();
          assertNull(session.get(Article.class, a.getUri().toString()));
          l = session.createCriteria(Article.class).add(Restrictions.eq("uri", foo)).list();
          assertEquals(0, l.size());
          l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo)).list();
          assertEquals(0, l.size());
        }
      });
  }
}
