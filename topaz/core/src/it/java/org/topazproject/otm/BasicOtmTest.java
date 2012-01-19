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
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Annotea;
import org.topazproject.otm.samples.Annotea.Body;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.ClassWithEnum;
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

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test
  public void testCrud() throws Exception {
    log.info("Testing basic CRUD operations ...");
    final byte[] blob = "Hello world".getBytes("UTF-8");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a;
          session.saveOrUpdate(a = new PublicAnnotation(URI.create("http://localhost/annotation/1")));
          assertTrue(session.contains(a));
          session.evict(a);
          assertTrue(!session.contains(a));
          a.setType(Annotation.NS + "Comment");
          a.setBody(new Body(blob));
          session.saveOrUpdate(a);
          assertEquals(a, session.get("PublicMarker", "http://localhost/annotation/1"));
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = (Annotation) session.get(Annotea.class, "http://localhost/annotation/1");
          assertNotNull(a);

          a.setCreator("Pradeep");
          a.setState(42);

          if (a.getFoobar() == null)
            a.setFoobar(new SampleEmbeddable());

          a.getFoobar().setFoo("FOO");
          a.getFoobar().setBar("BAR");
          a.getFoobar().getSet().add(a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = (Annotation) session.get(Annotea.class, "http://localhost/annotation/1");

          assertNotNull(a);
          assertNotNull(a.getFoobar());

          assertTrue(a instanceof PublicAnnotation);

          assertEquals(42, a.getState());
          assertEquals("Pradeep", a.getCreator());
          assertEquals(Annotation.NS + "Comment", a.getType());
          assertNotNull(a.getBody());
          assertEquals(blob, a.getBody().getBlob());
          assertEquals("FOO", a.getFoobar().getFoo());
          assertEquals("BAR", a.getFoobar().getBar());


          PropertyBinder b = session.getSessionFactory().getClassMetadata(Annotation.class)
                                     .getMapperByName("foobar.set").getBinder(session);
          assertFalse(b.isLoaded(a));
          assertNotNull(b.getRawFieldData(a));
          assertEquals(1, a.getFoobar().getSet().size());
          assertEquals(a, a.getFoobar().getSet().iterator().next());
          assertTrue(b.isLoaded(a));
          assertNull(b.getRawFieldData(a));

          session.delete(a);

          a = session.get(Annotation.class, "http://localhost/annotation/1");

          assertNull(a);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = (Annotation) session.get("PublicMarker", "http://localhost/annotation/1");
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
    doInReadOnlySession(new Action() {
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
    doInReadOnlySession(new Action() {
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
  public void testEnum() throws OtmException {
    log.info("Testing enum ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          ClassWithEnum c = new ClassWithEnum("http://localhost/ClassWithEnum/1");
          c.setFoo(ClassWithEnum.Foo.bar1);
          session.saveOrUpdate(c);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          ClassWithEnum c = session.get(ClassWithEnum.class, "http://localhost/ClassWithEnum/1");
          assertNotNull(c);

          assertEquals(ClassWithEnum.Foo.bar1, c.getFoo());
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  @Test(dependsOnMethods =  {
    "testEnum"}
  )
  public void testAssociations() throws OtmException {
    log.info("Testing associations ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
          a.setAnnotates(URI.create("http://www.topazproject.org"));

          Annotation sa = new PublicAnnotation(URI.create("http://localhost/annotation/1/1"));
          sa.setAnnotates(URI.create("http://www.topazproject.org"));

          a.setSupersededBy(sa);
          sa.setSupersedes(a);

          session.saveOrUpdate(a);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1/1");

          assertNotNull(a);

          Annotation old = a.getSupersedes();
          assertNotNull(old);

          assertTrue(old.getSupersededBy() == a);
          assertEquals(URI.create("http://localhost/annotation/1"), old.getId());
          assertEquals(URI.create("http://www.topazproject.org"), old.getAnnotates());
          assertEquals(URI.create("http://www.topazproject.org"), a.getAnnotates());
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

          // set up for next test - perform implicit delete
          a.getReplies().clear();
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testInverse"}
  )
  public void testImplicitDelete() throws OtmException {
    log.info("Testing implicit delete of associations ...");

    // see testInverse for the data setup
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

          assertNotNull(a);

          List<ReplyThread> replies = a.getReplies();
          assertNotNull(replies);
          assertEquals(0, replies.size());

          ReplyThread r = session.get(ReplyThread.class, "http://localhost/reply/1");
          assertNull(r);
          r = session.get(ReplyThread.class, "http://localhost/reply/1/1");
          assertNull(r);
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testImplicitDelete"}
  )
  public void testMergeAddAssoc() throws OtmException {
    log.info("Testing merge() - addition of associations...");

    // see testInverse for the data setup
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation o = session.get(Annotation.class, "http://localhost/annotation/1");
          assertNotNull(o);

          Annotation  a  = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
          ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/1"));
          ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/1/1"));

          a.addReply(r);
          r.addReply(rr);

          // test to see if merge adds the data
          Annotation n = session.merge(a);

          assertTrue(n == o);  // instance should match

          assertEquals(URI.create("http://localhost/annotation/1"), n.getId());
          List<ReplyThread> replies = n.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          ReplyThread nr = replies.get(0);
          assertNotNull(nr);
          assertEquals(URI.create("http://localhost/reply/1"), nr.getId());

          replies = nr.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          nr = replies.get(0);
          assertNotNull(nr);
          assertEquals(URI.create("http://localhost/reply/1/1"), nr.getId());

          session.flush();
        }
      });

    final URI aid  = URI.create("http://localhost/article/11");
    final URI pid1 = URI.create("http://localhost/article/11/part/1");
    final URI pid2 = URI.create("http://localhost/article/11/part/2");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = new Article();
          a.setUri(aid);

          ObjectInfo p1 = new ObjectInfo();
          p1.setUri(pid1);
          p1.setIsPartOf(a);
          a.getParts().add(p1);

          session.saveOrUpdate(a);
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = session.get(Article.class, aid.toString());
          assertNotNull(a);

          Article a2 = new Article();
          a2.setUri(aid);

          ObjectInfo p1 = new ObjectInfo();
          ObjectInfo p2 = new ObjectInfo();
          p1.setUri(pid1);
          p2.setUri(pid2);
          p1.setIsPartOf(a2);
          p2.setIsPartOf(a2);
          a2.getParts().add(p1);
          a2.getParts().add(p2);

          Article n = session.merge(a2);
          assertTrue(n == a);  // instance should match
          assertEquals(2, n.getParts().size());
          for (ObjectInfo p : n.getParts())
            assertEquals(p.getIsPartOf(), n);

          session.flush();
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testMergeAddAssoc"}
  )
  public void testMergeDelAssoc() throws OtmException {
    log.info("Testing merge() - deletion of associations...");
    // delete reply
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

          Annotation  n  = session.merge(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
          assertTrue(n == a);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

          assertNotNull(a);

          List<ReplyThread> replies = a.getReplies();
          assertNotNull(replies);
          assertEquals(0, replies.size());

          ReplyThread r = session.get(ReplyThread.class, "http://localhost/reply/1");
          assertNull(r);
          r = session.get(ReplyThread.class, "http://localhost/reply/1/1");
          assertNull(r);
        }
      });

    // same as above, but where reply hasn't been loaded yet
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

          ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/1"));
          ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/1/1"));

          a.addReply(r);
          r.addReply(rr);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");
          assertNotNull(a);

          Annotation n =
              session.merge(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
          assertTrue(n == a);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");
          assertNotNull(a);

          ReplyThread r = session.get(ReplyThread.class, "http://localhost/reply/1");
          assertNull(r);
          r = session.get(ReplyThread.class, "http://localhost/reply/1/1");
          assertNull(r);
        }
      });

    final URI aid  = URI.create("http://localhost/article/11");
    final URI pid1 = URI.create("http://localhost/article/11/part/1");
    final URI pid2 = URI.create("http://localhost/article/11/part/2");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article o = session.get(Article.class, aid.toString());
          assertNotNull(o);

          Article a = new Article();
          a.setUri(aid);

          ObjectInfo p1 = new ObjectInfo();
          p1.setUri(pid1);
          p1.setIsPartOf(a);
          a.getParts().add(p1);

          Article n = session.merge(a);
          assertTrue(n == o);
          assertEquals(1, n.getParts().size());
          for (ObjectInfo p : n.getParts())
            assertEquals(p.getIsPartOf(), n);

          session.flush();
        }
      });

    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = session.get(Article.class, aid.toString());
          assertNotNull(a);
          assertEquals(1, a.getParts().size());
          for (ObjectInfo p : a.getParts())
            assertEquals(p.getIsPartOf(), a);
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testMergeDelAssoc"}
  )
  public void testMergeNew() throws OtmException {
    log.info("Testing merge() - creating new instance ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = new PublicAnnotation(URI.create("http://localhost/annotation/2"));
          ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/2"));
          ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/2/1"));

          a.addReply(r);
          r.addReply(rr);
          session.merge(a);

          session.flush();
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Annotation a = session.get(Annotation.class, "http://localhost/annotation/2");

          assertNotNull(a);

          List<ReplyThread> replies = a.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          ReplyThread r = replies.get(0);
          assertNotNull(r);
          assertEquals(URI.create("http://localhost/reply/2"), r.getId());

          replies = r.getReplies();
          assertNotNull(replies);
          assertEquals(1, replies.size());

          r = replies.get(0);
          assertNotNull(r);
          assertEquals(URI.create("http://localhost/reply/2/1"), r.getId());
        }
      });

    final URI aid  = URI.create("http://localhost/article/12");
    final URI pid1 = URI.create("http://localhost/article/12/part/1");
    final URI pid2 = URI.create("http://localhost/article/12/part/2");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = new Article();
          a.setUri(aid);

          ObjectInfo p1 = new ObjectInfo();
          p1.setUri(pid1);
          p1.setIsPartOf(a);
          a.getParts().add(p1);

          session.merge(a);
        }
      });

    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = session.get(Article.class, aid.toString());
          assertNotNull(a);

          assertEquals(1, a.getParts().size());
          for (ObjectInfo p : a.getParts())
            assertEquals(p.getIsPartOf(), a);
        }
      });
  }

  @Test(dependsOnMethods =  {
    "testMergeNew"}
  )
  public void testSpecialMappers() throws OtmException {
    log.info("Testing special mappers (rdf:list etc.) ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = new SpecialMappers("http://localhost/sm/1");

          for (int i = 1; i < 11; i++) {
            m.getList().add("l" + i);
            m.getBag().add("b" + i);
            m.getSeq().add("s" + i);
            m.getAlt().add("a" + i);
          }

          session.saveOrUpdate(m);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getList().add("l11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getBag().add("b11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getSeq().add("s11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getAlt().add("a11");
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);

          assertEquals(11, m.getList().size());
          assertEquals(11, m.getBag().size());
          assertEquals(11, m.getSeq().size());
          assertEquals(11, m.getAlt().size());

          for (int i = 1; i < 12; i++) {
            assertEquals("l" + i, m.getList().get(i - 1));
            assertTrue(m.getBag().contains("b" + i));
            assertEquals("s" + i, m.getSeq().get(i - 1));
            assertTrue(m.getAlt().contains("a" + i));
          }
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getList().remove("l11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getBag().remove("b11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getSeq().remove("s11");
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.getAlt().remove("a11");
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);

          assertEquals(10, m.getList().size());
          assertEquals(10, m.getBag().size());
          assertEquals(10, m.getSeq().size());
          assertEquals(10, m.getAlt().size());

          for (int i = 1; i < 11; i++) {
            assertEquals("l" + i, m.getList().get(i - 1));
            assertTrue(m.getBag().contains("b" + i));
            assertEquals("s" + i, m.getSeq().get(i - 1));
            assertTrue(m.getAlt().contains("a" + i));
          }
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          m.setSeq(null);
          m.getAlt().clear();
          m.setBag(m.getAlt());
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
          assertNotNull(m);
          assertEquals(10, m.getList().size());
          assertEquals(0, m.getBag().size());
          assertEquals(0, m.getSeq().size());
          assertEquals(0, m.getAlt().size());
          for (int i = 1; i < 11; i++) {
            assertEquals("l" + i, m.getList().get(i - 1));
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
          g.setResource("http://localhost/articles/1");
          g.getPermissions().put("perm:1", Collections.singletonList("user:1"));
          g.getPermissions().put("perm:2", Collections.singletonList("user:1"));

          session.saveOrUpdate(g);
        }
      });
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Grants g = session.get(Grants.class, "http://localhost/articles/1");
          assertNotNull(g);

          assertEquals(2, g.getPermissions().size());

          List<String> u = g.getPermissions().get("perm:1");
          assertNotNull(u);
          assertEquals(1, u.size());
          assertEquals("user:1", u.get(0));

          List<Grants> l =
                session.createCriteria(Grants.class)
                        .add(Restrictions.eq("resource", "http://localhost/articles/1")).list();
          assertEquals(1, l.size());

          Grants g1 = l.get(0);
          assertTrue(g == g1);

          assertEquals(2, g.getPermissions().size());

          u = g.getPermissions().get("perm:1");
          assertNotNull(u);
          assertEquals(1, u.size());
          assertEquals("user:1", u.get(0));

          u = g.getPermissions().get("perm:2");
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
    doInReadOnlySession(new Action() {
        public void run(Session session) throws OtmException {
          Article a = session.get(Article.class, aid.toString());

          assertNotNull(a);
          assertEquals(2, a.getParts().size());

          for (ObjectInfo o : a.getParts())
            assertTrue(o.getIsPartOf() == a);
        }
      });
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          // test to ensure delete is done children first
          Article a = session.get(Article.class, aid.toString());
          session.delete(a);
          // Now check if the back-pointing articles are all loaded
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

          List<Annotation> l =
                session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", foo))
                        .list();
          assertEquals(3, l.size());

          for (Annotation o : l) {
            assertEquals(foo, o.getAnnotates());

            if (o instanceof PublicAnnotation)
              assertTrue((a1 == o) || (a2 == o));
          }

          Results r =
                session.createQuery("select p from PublicMarker p where cast(p, Annotation).annotates = <foo:1>;")
                        .execute();
          l.clear();

          while (r.next())
            l.add((Annotation) r.get(0));

          r.close();

          assertEquals(2, l.size());

          for (Annotation o : l) {
            assertEquals(foo, o.getAnnotates());
            assertTrue((a1 == o) || (a2 == o));
          }

          r = session.createQuery("select p from PrivateMarker p where cast(p, Annotation).annotates = <foo:1>;")
                        .execute();
          l.clear();

          while (r.next())
            l.add((Annotation) r.get(0));

          r.close();

          assertEquals(1, l.size());

          for (Annotation o : l) {
            assertEquals(foo, o.getAnnotates());
            assertTrue(a3 == o);
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

  @Test
  public void testAliases() throws OtmException {
    log.info("Testing aliases ...");

    ClassMetadata cm = factory.getClassMetadata(Article.class);
    assertEquals(Collections.singleton(Rdf.topaz + "Article"), cm.getTypes());

    RdfMapper m = (RdfMapper)cm.getMapperByName("date");
    assertNotNull(m);
    assertEquals(Rdf.dc + "date", m.getUri());
    assertEquals(Rdf.xsd + "date", m.getDataType());

    assertEquals("http://www.baz.com",  factory.listAliases().get("bazAlias"));
    assertEquals("http://www.bar.org/", factory.listAliases().get("barAlias"));

    // TODO: test expansion on uriPrefix
  }
}
