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


public class IdGenTest {
  private static final Log log = LogFactory.getLog(IdGenTest.class);
  private SessionFactory factory = new SessionFactoryImpl();
  private Session session = null;
  private Transaction tx = null;

  @Entity(type = Rdf.topaz + "A", model = "idtest")
  public static class A {
    @Id @GeneratedValue(generatorClass = "org.topazproject.otm.id.GUIDGenerator",
                        uriPrefix = Rdf.topaz + "A#")
    public String id;
    @Predicate(uri = Rdf.topaz + "name")
    public String name;
  }

  @Entity(type = Rdf.topaz + "B")
  public static class B extends A {
    @Predicate(uri = Rdf.topaz + "justInCase")
    public String extra;
  }

  @Entity(type = Rdf.topaz + "C")
  public static class C extends B {
  }

  @Entity(type = Rdf.topaz + "D", model = "idtest")
  public static class D {
    @Id @GeneratedValue(generatorClass = "org.topazproject.otm.id.GUIDGenerator")
    public URI uri;
    @Predicate(uri = Rdf.topaz + "data")
    public String data;
  }

  @Entity(type = Rdf.topaz + "E", model = "idtest")
  public static class E {
    @Id @GeneratedValue
    public String id;
  }

  @Entity(type = Rdf.topaz + "Kontrol", model = "idtest")
  public static class Kontrol {
    @Id
    public String id;
    @Predicate(uri = Rdf.topaz + "data")
    public String data;
  }

  @BeforeClass
  public void setUpFactory() throws OtmException {
    factory.setTripleStore(
      new ItqlStore(URI.create("local:///topazproject")));

    ModelConfig idtest = new ModelConfig("idtest", URI.create("local:///topazproject#idtest"), null);
    ModelConfig ri = new ModelConfig("ri", URI.create("local:///topazproject#idgentest-ri"), null);
    factory.addModel(idtest);
    factory.addModel(ri);

    try {
      factory.getTripleStore().dropModel(idtest);
    } catch (Throwable t) {
      log.debug("Failed to drop model 'idtest'", t);
    }

    try {
      factory.getTripleStore().dropModel(ri);
    } catch (Throwable t) {
      log.debug("Failed to drop model 'ri'", t);
    }

    factory.getTripleStore().createModel(idtest);
    factory.getTripleStore().createModel(ri);
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
    session.saveOrUpdate(new PublicAnnotation(URI.create("http://localhost/annotation/1")));
  }

  @Test(groups = { "tx" })
  public void controlTest() throws OtmException {
    // Seeing a bug in other tests... verify control keeps working after fix
    factory.preload(Kontrol.class);
    Kontrol k = new Kontrol();
    String id = "http://www.topazproject.org/Kontrol";
    k.id = id;
    session.saveOrUpdate(k);
    assertEquals(id, k.id);
  }

  @Test
  public void aInstanceTest() throws OtmException {
    factory.preload(A.class);
  }

  @Test(groups = { "tx" })
  public void aCreateTest() throws OtmException {
    factory.preload(A.class);
    A a = new A();
    session.saveOrUpdate(a);
    assert a.id != null;

    // Ensure it is a uri
    URI uri = URI.create(a.id);

    assert a.id.startsWith(Rdf.topaz + "A#") : a.id;
  }

  @Test
  public void bTest() throws OtmException {
    factory.preload(B.class);
  }

  @Test(groups = { "tx" })
  public void bCreateTest() throws OtmException {
    factory.preload(C.class);
    C c = new C();
    session.saveOrUpdate(c);
    assert c.id != null;
  }

  @Test(groups = { "tx" })
  public void dCreateTest() throws OtmException {
    factory.preload(D.class);
    D d = new D();
    session.saveOrUpdate(d);
    assert d.uri != null;
    assert d.uri.toString().startsWith(Rdf.topaz + D.class.getName() + "/uri#") : d.uri;
  }

  @Test(groups = { "tx" })
  public void eCreateTest() throws OtmException {
    factory.preload(E.class);
    E e = new E();
    session.saveOrUpdate(e);
    assert e.id.startsWith(Rdf.topaz + E.class.getName() + "/id#") : e.id;
  }
}
