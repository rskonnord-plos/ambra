/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.stores.ItqlStore;

import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.GeneratedValue;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import static org.testng.AssertJUnit.*;

import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.ReplyThread;


public class IdGenTest {
  private static final Log log = LogFactory.getLog(IdGenTest.class);
  private SessionFactory factory;
  private Session session = null;
  private Transaction tx = null;

  @Entity(types = {Rdf.topaz + "A"}, graph = "idtest")
  public static class A {
    private String id;
    private String name;

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
    @Id @GeneratedValue(generatorClass = "org.topazproject.otm.id.GUIDGenerator",
                        uriPrefix = Rdf.topaz + "A#")
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
    @Predicate(uri = Rdf.topaz + "name")
    public void setName(String name) {
      this.name = name;
    }
  }

  @Entity(types = {Rdf.topaz + "B"})
  public static class B extends A {
    private String extra;

    /**
     * Get extra.
     *
     * @return extra as String.
     */
    public String getExtra() {
      return extra;
    }

    /**
     * Set extra.
     *
     * @param extra the value to set.
     */
    @Predicate(uri = Rdf.topaz + "justInCase")
    public void setExtra(String extra) {
      this.extra = extra;
    }
  }

  @Entity(types = {Rdf.topaz + "C"})
  public static class C extends B {
  }

  @Entity(types = {Rdf.topaz + "D"}, graph = "idtest")
  public static class D {
    private URI uri;
    private String data;

    /**
     * Get uri.
     *
     * @return uri as URI.
     */
    public URI getUri() {
      return uri;
    }

    /**
     * Set uri.
     *
     * @param uri the value to set.
     */
    @Id @GeneratedValue(generatorClass = "org.topazproject.otm.id.GUIDGenerator")
    public void setUri(URI uri) {
      this.uri = uri;
    }

    /**
     * Get data.
     *
     * @return data as String.
     */
    public String getData() {
      return data;
    }

    /**
     * Set data.
     *
     * @param data the value to set.
     */
    @Predicate(uri = Rdf.topaz + "data")
    public void setData(String data) {
      this.data = data;
    }
  }

  @Entity(types = {Rdf.topaz + "E"}, graph = "idtest")
  public static class E {
    private String id;

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
    @Id @GeneratedValue
    public void setId(String id) {
      this.id = id;
    }
  }

  @Entity(types = {Rdf.topaz + "Kontrol"}, graph = "idtest")
  public static class Kontrol {
    private String id;
    private String data;

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
     * Get data.
     *
     * @return data as String.
     */
    public String getData() {
      return data;
    }

    /**
     * Set data.
     *
     * @param data the value to set.
     */
    @Predicate(uri = Rdf.topaz + "data")
    public void setData(String data) {
      this.data = data;
    }
  }

  @BeforeClass
  public void setUpFactory() throws OtmException {
    factory = new SessionFactoryImpl();
    factory.setTripleStore(new ItqlStore(URI.create("local:///topazproject")));

    GraphConfig idtest = new GraphConfig("idtest", URI.create("local:///topazproject#idtest"), null);
    GraphConfig ri = new GraphConfig("ri", URI.create("local:///topazproject#idgentest-ri"), null);
    factory.addGraph(idtest);
    factory.addGraph(ri);

    Session s = null;
    Transaction txn = null;
    try {
      s = factory.openSession();
      dropGraph(s, idtest.getId());
      dropGraph(s, ri.getId());

      txn = s.beginTransaction();
      s.createGraph(idtest.getId());
      s.createGraph(ri.getId());
      txn.commit();
    } catch (OtmException e) {
      if (txn != null) txn.rollback();
      throw e;
    } finally {
      if (s != null) s.close();
    }
  }

  @BeforeClass(groups = { "tx" }, dependsOnMethods = { "setUpFactory" })
  public void setUpTx() throws OtmException {
    session = factory.openSession();
    tx = session.beginTransaction();
  }

  @AfterClass(groups = { "tx" })
  public void tearDownTx() throws OtmException {
    if (tx == null) {
      session.close();
      return;
    }

    try {
      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }
    }

    session.close();
  }

  @Test(groups = { "tx" })
  public void annotationTest() throws OtmException {
    factory.preload(PublicAnnotation.class);
    factory.validate();
    session.saveOrUpdate(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
    PublicAnnotation a = new PublicAnnotation(URI.create("foo:bar"));
    a.addReply(new ReplyThread(URI.create("bar:foo")));
    try {
      session.saveOrUpdate(a);
      fail("Expecting a failure for undefined ReplyThread");
    } catch (OtmException e) {
      assertTrue("Expecing exception to contain Annotation:replies", 
          e.getMessage().contains("Annotation:replies"));
      session.clear();
    }

  }

  @Test(groups = { "tx" })
  public void controlTest() throws OtmException {
    // Seeing a bug in other tests... verify control keeps working after fix
    factory.preload(Kontrol.class);
    factory.validate();
    Kontrol k = new Kontrol();
    String id = "http://www.topazproject.org/Kontrol";
    k.setId(id);
    session.saveOrUpdate(k);
    assertEquals(id, k.getId());
  }

  @Test
  public void aInstanceTest() throws OtmException {
    factory.preload(A.class);
    factory.validate();
  }

  @Test(groups = { "tx" })
  public void aCreateTest() throws OtmException {
    factory.preload(A.class);
    factory.validate();
    A a = new A();
    session.saveOrUpdate(a);
    assert a.getId() != null;

    // Ensure it is a uri
    URI uri = URI.create(a.id);

    assert a.getId().startsWith(Rdf.topaz + "A#") : a.getId();
  }

  @Test
  public void bTest() throws OtmException {
    factory.preload(B.class);
    factory.validate();
  }

  @Test(groups = { "tx" })
  public void bCreateTest() throws OtmException {
    factory.preload(C.class);
    factory.validate();
    C c = new C();
    session.saveOrUpdate(c);
    assert c.getId() != null;
  }

  @Test(groups = { "tx" })
  public void dCreateTest() throws OtmException {
    factory.preload(D.class);
    factory.validate();
    D d = new D();
    session.saveOrUpdate(d);
    assert d.getUri() != null;
    assert d.getUri().toString().startsWith(Rdf.topaz + D.class.getName() + "/uri#") : d.getUri();
  }

  @Test(groups = { "tx" })
  public void eCreateTest() throws OtmException {
    factory.preload(E.class);
    factory.validate();
    E e = new E();
    session.saveOrUpdate(e);
    assert e.getId().startsWith(Rdf.topaz + E.class.getName() + "/id#") : e.getId();
  }

  private void dropGraph(Session session, String name) {
    assert session.getTransaction() == null;
    Transaction tx = session.beginTransaction();
    try {
      session.dropGraph(name);
      tx.commit();
    } catch (Throwable t) {
      log.debug("Failed to drop graph 'idtest'", t);
      tx.rollback();
    }
  }
}
