package org.topazproject.otm;

import java.net.URI;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Annotea;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.Grants;
import org.topazproject.otm.samples.NoPredicate;
import org.topazproject.otm.samples.NoRdfType;
import org.topazproject.otm.samples.ObjectInfo;
import org.topazproject.otm.samples.Permissions;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.Reply;
import org.topazproject.otm.samples.ReplyThread;
import org.topazproject.otm.samples.Revokes;
import org.topazproject.otm.samples.SampleEmbeddable;
import org.topazproject.otm.samples.SpecialMappers;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.MemStore;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class OtmTest extends TestCase {
  private static final Log log = LogFactory.getLog(OtmTest.class);

  /**
   * DOCUMENT ME!
   */
  protected SessionFactory factory = new SessionFactory();


  public OtmTest() {
    factory.setTripleStore(new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")));

    factory.getTripleStore().createModel(new ModelConfig("str", URI.create("local:///topazproject#str"), URI.create("http://topazproject.org/models#StringCompare")));
    ModelConfig ri      = new ModelConfig("ri", URI.create("local:///topazproject#otmtest1"), null);
    ModelConfig grants  =
      new ModelConfig("grants", URI.create("local:///topazproject#otmtest2"), null);
    ModelConfig revokes =
      new ModelConfig("revokes", URI.create("local:///topazproject#otmtest2"), null);

    factory.addModel(ri);
    factory.addModel(grants);
    factory.addModel(revokes);

    factory.preload(ReplyThread.class);
    factory.preload(PublicAnnotation.class);
    factory.preload(PrivateAnnotation.class);
    factory.preload(Article.class);
    factory.preload(NoRdfType.class);
    factory.preload(NoPredicate.class);
    factory.preload(SpecialMappers.class);
    factory.preload(Grants.class);
    factory.preload(Revokes.class);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected void setUp() throws OtmException {
    ModelConfig ri      = factory.getModel("ri");
    ModelConfig grants  = factory.getModel("grants");
    ModelConfig revokes = factory.getModel("revokes");

    try {
      factory.getTripleStore().dropModel(ri);
    } catch (Throwable t) {
      if (log.isDebugEnabled())
        log.debug("Failed to drop model '" + ri.getId() + "'", t);
    }

    try {
      factory.getTripleStore().dropModel(grants);
    } catch (Throwable t) {
      if (log.isDebugEnabled())
        log.debug("Failed to drop model '" + grants.getId() + "'", t);
    }

    try {
      factory.getTripleStore().dropModel(revokes);
    } catch (Throwable t) {
      if (log.isDebugEnabled())
        log.debug("Failed to drop model '" + revokes.getId() + "'", t);
    }


    factory.getTripleStore().createModel(ri);
    factory.getTripleStore().createModel(grants);
    factory.getTripleStore().createModel(revokes);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test01() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      session.saveOrUpdate(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
      session.saveOrUpdate(new NoRdfType("http://localhost/noRdfType/1"));
      session.saveOrUpdate(new NoPredicate("http://localhost/noPredicate/1"));

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = (Annotation) session.get(Annotea.class, "http://localhost/annotation/1");
      assertNotNull(a);

      NoRdfType n = session.get(NoRdfType.class, "http://localhost/noRdfType/1");
      assertNotNull(n);

      n = session.get(NoRdfType.class, "http://localhost/noRdfType/2");
      assertNull(n);

      NoPredicate np = session.get(NoPredicate.class, "http://localhost/noPredicate/1");
      assertNotNull(np);

      np = session.get(NoPredicate.class, "http://localhost/noPredicate/2");
      assertNull(np);

      a.setCreator("Pradeep");
      a.setState(42);
      a.setType(Annotation.NS + "Comment");

      if (a.foobar == null)
        a.foobar = new SampleEmbeddable();

      a.foobar.foo   = "FOO";
      a.foobar.bar   = "BAR";

      session.saveOrUpdate(a);

      tx.commit();
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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

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

      tx.commit();
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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1");

      assertNull(a);

      tx.commit();
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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test02() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation a = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
      a.setAnnotates(URI.create("http://www.plosone.org"));

      Annotation sa = new PublicAnnotation(URI.create("http://localhost/annotation/1/1"));
      sa.setAnnotates(URI.create("http://www.plosone.org"));

      a.setSupersededBy(sa);
      sa.setSupersedes(a);

      session.saveOrUpdate(a);

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Annotation a = session.get(Annotation.class, "http://localhost/annotation/1/1");

      assertNotNull(a);

      Annotation old = a.getSupersedes();
      assertNotNull(old);

      assertEquals(URI.create("http://localhost/annotation/1"), old.getId());
      assertEquals(URI.create("http://www.plosone.org"), old.getAnnotates());
      assertEquals(URI.create("http://www.plosone.org"), a.getAnnotates());

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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test03() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Annotation  a  = new PublicAnnotation(URI.create("http://localhost/annotation/1"));
      ReplyThread r  = new ReplyThread(URI.create("http://localhost/reply/1"));
      ReplyThread rr = new ReplyThread(URI.create("http://localhost/reply/1/1"));

      a.addReply(r);
      r.addReply(rr);

      session.saveOrUpdate(a);

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test04() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    URI         id1     = URI.create("http://localhost/annotation/1");
    URI         id2     = URI.create("http://localhost/annotation/2");
    URI         id3     = URI.create("http://localhost/annotation/3");
    URI         id4     = URI.create("http://localhost/annotation/4");

    try {
      tx = session.beginTransaction();

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      List l =
        session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1")).list();

      assertEquals(2, l.size());

      Annotation a1 = (Annotation) l.get(0);
      Annotation a2 = (Annotation) l.get(1);

      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertEquals(URI.create("foo:1"), a2.getAnnotates());

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.id(id3.toString())).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("bar:1"), a1.getAnnotates());
      assertTrue(id3.equals(a1.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                  .add(Restrictions.id(id3.toString())).list();

      assertEquals(0, l.size());

      l = session.createCriteria(Annotation.class).add(Restrictions.eq("annotates", "foo:1"))
                  .add(Restrictions.id(id1.toString())).list();

      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertTrue(id1.equals(a1.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.conjunction().add(Restrictions.eq("annotates", "foo:1"))
                                    .add(Restrictions.id(id1.toString()))).list();

      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertTrue(id1.equals(a1.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.disjunction().add(Restrictions.eq("annotates", "foo:1"))
                                    .add(Restrictions.id(id1.toString()))).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertEquals(URI.create("foo:1"), a1.getAnnotates());
      assertEquals(URI.create("foo:1"), a2.getAnnotates());

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.walk("supersededBy", id3.toString())).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.walk("supersedes", id2.toString())).list();

      assertEquals(1, l.size());

      Annotation a3 = (Annotation) l.get(0);

      assertTrue(id3.equals(a3.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.trans("supersededBy", id3.toString())).list();

      assertEquals(2, l.size());

      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);

      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));

      l = session.createCriteria(Annotation.class)
                  .add(Restrictions.trans("supersedes", id2.toString())).list();

      assertEquals(1, l.size());

      a3 = (Annotation) l.get(0);

      assertTrue(id3.equals(a3.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "foo:1")).list();

      assertEquals(2, l.size());

      a3 = (Annotation) l.get(0);
      Annotation a4 = (Annotation) l.get(1);

      assertTrue(id3.equals(a3.getId()) || id4.equals(a3.getId()));
      assertTrue(id3.equals(a4.getId()) || id4.equals(a4.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                  .setFirstResult(0).setMaxResults(1).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);

      assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.ne("annotates", "bar:1"))
                  .setFirstResult(1).setMaxResults(1).addOrder(Order.asc("annotates")).list();

      assertEquals(1, l.size());

      a1 = (Annotation) l.get(0);

      assertTrue(id1.equals(a1.getId()) || id2.equals(a1.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.gt("creator", "bb")).list();
      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(id3, a1.getId());
      l = session.createCriteria(Annotation.class).add(Restrictions.lt("creator", "bb")).list();
      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(id1, a1.getId());
      l = session.createCriteria(Annotation.class).add(Restrictions.le("creator", "bb")).list();
      assertEquals(2, l.size());
      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);
      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
      l = session.createCriteria(Annotation.class).add(Restrictions.ge("creator", "bb")).list();
      assertEquals(2, l.size());
      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
      assertTrue(id3.equals(a1.getId()) || id3.equals(a2.getId()));

      l = session.createCriteria(Annotation.class).add(Restrictions.exists("created")).list();
      assertEquals(1, l.size());
      a1 = (Annotation) l.get(0);
      assertEquals(id3, a1.getId());

      l = session.createCriteria(Annotation.class).add(Restrictions.notExists("created")).list();
      assertEquals(3, l.size());
      a1   = (Annotation) l.get(0);
      a2   = (Annotation) l.get(1);
      a4   = (Annotation) l.get(2);
      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()) || id1.equals(a4.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()) || id2.equals(a4.getId()));
      assertTrue(id4.equals(a1.getId()) || id4.equals(a2.getId()) || id4.equals(a4.getId()));

      l = session.createCriteria(Annotation.class).addOrder(Order.desc("annotates"))
          .createCriteria("supersedes").add(Restrictions.eq("annotates", "foo:1")).list();
      assertEquals(2, l.size());
      a1 = (Annotation) l.get(0);
      a2 = (Annotation) l.get(1);
      assertEquals(id2, a1.getId());
      assertEquals(id3, a2.getId());
      l = session.createCriteria(Annotation.class).addOrder(Order.asc("supersedes"))
          .createCriteria("supersedes").add(Restrictions.eq("annotates", "foo:1")).list();
      assertEquals(2, l.size());
      a1 = (Annotation) l.get(0);
      a2 = (Annotation) l.get(1);
      assertEquals(id2, a1.getId());
      assertEquals(id3, a2.getId());

      Criteria criteria = session.createCriteria(Annotation.class);
      criteria.createCriteria("supersedes").addOrder(Order.desc("creator"))
                    .add(Restrictions.eq("annotates", "foo:1"));
      l = criteria.addOrder(Order.desc("annotates")).list();
      assertEquals(2, l.size());
      a1 = (Annotation) l.get(0);
      a2 = (Annotation) l.get(1);
      assertEquals(id3, a1.getId());
      assertEquals(id2, a2.getId());

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      log.warn("test failed", e);

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
    session   = factory.openSession();
    tx        = null;

    List<Annotation> al = null;

    try {
      tx = session.beginTransaction();

      al = session.createCriteria(Annotation.class).list();
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

    assertNotNull(al);
    assertEquals(4, al.size());
    for (Annotation a : al) {
      if (a.getSupersedes() != null)
        assertTrue(a == a.getSupersedes().getSupersededBy());
      if (a.getSupersededBy() != null)
        assertTrue(a == a.getSupersededBy().getSupersedes());
    }

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Results r = session.doQuery("select a from Annotation a;");
      al.clear();

      while (r.next())
        al.add((Annotation)r.get(0));

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
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test05() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      SpecialMappers m = new SpecialMappers("http://localhost/sm/1");
      m.list.add("l1");
      m.list.add("l2");

      m.bag.add("b1");
      m.bag.add("b2");

      m.seq.add("s1");
      m.seq.add("s2");
      m.seq.add("s3");
      m.seq.add("s4");
      m.seq.add("s5");
      m.seq.add("s6");
      m.seq.add("s7");
      m.seq.add("s8");
      m.seq.add("s9");
      m.seq.add("s10");
      m.seq.add("s11");

      m.alt.add("a1");
      m.alt.add("a2");

      session.saveOrUpdate(m);

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      SpecialMappers m = session.get(SpecialMappers.class, "http://localhost/sm/1");
      assertNotNull(m);

      assertEquals(2, m.list.size());
      assertTrue(m.list.contains("l1") && m.list.contains("l2"));

      assertEquals(2, m.bag.size());
      assertTrue(m.bag.contains("b1") && m.bag.contains("b2"));

      assertEquals(11, m.seq.size());
      assertTrue(m.seq.contains("s1") && m.seq.contains("s2"));

      assertEquals(2, m.alt.size());
      assertTrue(m.alt.contains("a1") && m.alt.contains("a2"));

      assertTrue(m.alt.get(0).equals("a1") && m.alt.get(1).equals("a2"));
      assertTrue(m.seq.get(0).equals("s1"));
      assertTrue(m.seq.get(1).equals("s2"));
      assertTrue(m.seq.get(2).equals("s3"));
      assertTrue(m.seq.get(3).equals("s4"));
      assertTrue(m.seq.get(4).equals("s5"));
      assertTrue(m.seq.get(5).equals("s6"));
      assertTrue(m.seq.get(6).equals("s7"));
      assertTrue(m.seq.get(7).equals("s8"));
      assertTrue(m.seq.get(8).equals("s9"));
      assertTrue(m.seq.get(9).equals("s10"));
      assertTrue(m.seq.get(10).equals("s11"));

      tx.commit();
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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test06() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      Grants g = new Grants();
      g.resource = "http://localhost/articles/1";
      g.permissions.put("perm:1", Collections.singletonList("user:1"));
      g.permissions.put("perm:2", Collections.singletonList("user:1"));

      session.saveOrUpdate(g);

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Grants g = session.get(Grants.class, "http://localhost/articles/1");
      assertNotNull(g);

      assertEquals(2, g.permissions.size());

      List<String> u = g.permissions.get("perm:1");
      assertNotNull(u);
      assertEquals(1, u.size());
      assertEquals("user:1", u.get(0));

      u = g.permissions.get("perm:2");
      assertNotNull(u);
      assertEquals(1, u.size());
      assertEquals("user:1", u.get(0));

      List<Grants> l =
        session.createCriteria(Grants.class).add(Restrictions.id("http://localhost/articles/1"))
                .list();
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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test07() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    URI         id1     = URI.create("http://localhost/annotation/1");
    URI         id2     = URI.create("http://localhost/annotation/2");
    URI         id3     = URI.create("http://localhost/annotation/3");
    URI         foo     = URI.create("foo:1");

    try {
      tx = session.beginTransaction();

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

    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      List l = session.createCriteria(Article.class).list();
      assertEquals(1, l.size());

      Article                a  = (Article) l.get(0);

      List<PublicAnnotation> al = a.getPublicAnnotations();
      assertEquals(2, al.size());
      List<PrivateAnnotation> pl = a.getPrivateAnnotations();
      assertEquals(1, pl.size());

      PublicAnnotation a1 = al.get(0);
      PublicAnnotation a2 = al.get(1);
      PrivateAnnotation a3 = pl.get(0);
      assertTrue(id1.equals(a1.getId()) || id1.equals(a2.getId()));
      assertTrue(id2.equals(a1.getId()) || id2.equals(a2.getId()));
      assertEquals(foo, a1.getAnnotates());
      assertEquals(foo, a2.getAnnotates());
      assertTrue(id3.equals(a3.getId()));
      assertEquals(foo, a3.getAnnotates());

      l = session.createCriteria(Annotation.class).list();
      assertEquals(3, l.size());

      for (Object o : l) {
        assertEquals(foo, ((Annotation) o).getAnnotates());

        if (o instanceof PublicAnnotation)
          assertTrue((a1 == o) || (a2 == o));
      }

      Results r = session.doQuery("select a from Annotation a where a.annotates = <foo:1>;");
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
      l = session.createCriteria(Article.class).list();
      assertEquals(0, l.size());
      l = session.createCriteria(Annotation.class).list();
      assertEquals(0, l.size());

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      log.warn("test failed", e);

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
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public void test08() throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    URI         aid     = URI.create("http://localhost/article/1");
    URI         pid1     = URI.create("http://localhost/article/1/part/1");
    URI         pid2     = URI.create("http://localhost/article/1/part/2");

    try {
      tx = session.beginTransaction();

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
    session   = factory.openSession();
    tx        = null;

    try {
      tx = session.beginTransaction();

      Article a = session.get(Article.class, aid.toString());

      assertNotNull(a);
      assertEquals(2, a.getParts().size());
      for (ObjectInfo o : a.getParts())
        assertTrue(o.getIsPartOf() == a);

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
}
