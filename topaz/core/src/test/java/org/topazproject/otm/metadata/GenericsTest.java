/* $HeadURL::                                                                            $
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

package org.topazproject.otm.metadata;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

public class GenericsTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();

  private void compare(RdfDefinition def, Object[] vals) {
    String message = "Testing '" + def.getName() + "': ";
    assertEquals(message, vals[0], def.getUri());
    assertEquals(message, vals[1], def.isEntityOwned());
    assertEquals(message, vals[2], def.getGraph());
    assertEquals(message, vals[3], def.hasInverseUri());
    assertEquals(message, vals[4], def.typeIsUri());
    assertEquals(message, vals[5], def.getDataType());
    assertEquals(message, vals[6], def.getAssociatedEntity());
    assertEquals(message, vals[7], def.getColType());
    assertEquals(message, vals[8], def.getFetchType());
    assertEquals(message, vals[9], def.getCascade());
    assertEquals(message, vals[10], def.isAssociation());
  }

  private void testAssoc(String A, String B) {
    Object[] vals = new Object[] {"a:assoc", true, "test", false, true, null, "Assoc",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};


    RdfDefinition assoc, extended;
    assoc = (RdfDefinition)sf.getDefinition(A + ":assoc");
    extended = (RdfDefinition)sf.getDefinition(B + ":assoc");

    extended.resolveReference(sf);
    compare(assoc, vals);
    vals[6] = "Extended";
    compare(extended, vals);

    assertTrue(extended.refersSameGraphNodes(assoc));
    assertTrue(extended.refersSameRange(assoc));
  }

  private void testCm(String A, String B) {

    ClassMetadata cm = sf.getClassMetadata(A);
    int expected = "A".equals(A) ? 3 : 2;
    assertEquals(expected, cm.getRdfMappers().size());
    assertNotNull(cm.getMapperByName("assoc"));
    assertNotNull(cm.getMapperByName("u"));

    cm = sf.getClassMetadata(B);
    expected = "B".equals(B) ? 4 : 2;
    assertEquals(expected, cm.getRdfMappers().size());
    assertNotNull(cm.getMapperByName("assoc"));
    assertNotNull(cm.getMapperByName("u"));
  }

  private void testU(String A, String B) {
    Object[] bvals = new Object[] {"a:u", true, "test", false, true, null, "Object",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    Object[] vals = new Object[] {"a:u", true, "test", false, false, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};



    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition(A + ":u");
    def = (RdfDefinition)sf.getDefinition(B + ":u");

    def.resolveReference(sf);
    compare(def, vals);
    compare(base, bvals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  private void testV(String A, String B) {
    Object[] bvals = new Object[] {"a:v", true, "test", false, true, null, "Object",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    Object[] vals = new Object[] {"a:v", true, "test", false, false, Rdf.xsd + "int", null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition(A + ":v");
    def = (RdfDefinition)sf.getDefinition(B + ":v");

    def.resolveReference(sf);
    compare(def, vals);
    compare(base, bvals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  private void testW(String A, String B) {
    Object[] bvals = new Object[] {"a:w", true, "test", false, true, null, "Object",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    Object[] vals = new Object[] {"a:w", true, "test", false, false, Rdf.xsd + "dateTime", null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition(A + ":w");
    def = (RdfDefinition)sf.getDefinition(B + ":w");

    def.resolveReference(sf);
    compare(def, vals);
    compare(base, bvals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  private void test(String A, String B) {
    testAssoc(A, B);
    testU(A, B);
    testCm(A, B);
  }

  public void test01() {
    sf.preload(B.class);
    sf.preload(Extended.class);
    sf.validate();

    test("A", "B");
    testV("A", "B");
    testW("W", "B");
  }

  public void test02() {
    sf.preload(LB.class);
    sf.preload(Extended.class);
    sf.validate();

    test("LA", "LB");
  }

  public void test03() {
    sf.preload(AB.class);
    sf.preload(Extended.class);
    sf.validate();

    test("AA", "AB");
  }

  @Entity(graph="test", name="Base")
  public static class Base {
    public String getId() {return null;}
    @Id
    public void setId(String s) {}
  }

  @UriPrefix("a:")
  @Entity(name="U")
  public static interface UProvider<U> {
    public U getU();
    @Predicate(graph="test")
    public void setU(U a);
  }

  @UriPrefix("a:")
  @Entity(name="W")
  public static interface WProvider<W> {
    public W getW();
    @Predicate(graph="test")
    public void setW(W a);
  }

  @UriPrefix("a:")
  @Entity(name="A")
  public static abstract class A<T extends Assoc, U, V> extends Base implements UProvider<U> {
    public T getAssoc() {return null;}
    @Predicate(graph="test")
    public void setAssoc(T a) {}
    public U getU() { return null; }
    @Predicate(graph="test")
    public void setU(U a) {}
    public V getV() { return null; }
    @Predicate(graph="test")
    public void setV(U a) {}
  }

  @UriPrefix("b:")
  @Entity(name="B")
  public static class B extends A<Extended, String, Integer> implements WProvider<Date> {
    @Predicate(dataType="xsd:int")
    public Integer getV() { return null; }
    public Date getW() { return null; }
    @Predicate(dataType="xsd:dateTime")
    public void setW(Date a) {}
  }

  @UriPrefix("a:")
  @Entity(name="LA")
  public static class LA<T extends Assoc, U> extends Base {
    public List<T> getAssoc() {return null;}
    @Predicate(graph="test")
    public void setAssoc(List<T> a) {}
    public List<U> getU() { return null; }
    @Predicate(graph="test")
    public void setU(List<U> a) {}
  }

  @UriPrefix("b:")
  @Entity(name="LB")
  public static class LB extends LA<Extended, String> {
  }

  @UriPrefix("a:")
  @Entity(name="AA")
  public static class AA<T extends Assoc, U> extends Base {
    public T[] getAssoc() {return null;}
    @Predicate(graph="test")
    public void setAssoc(T[] a) {}
    public U[] getU() { return null; }
    @Predicate(graph="test")
    public void setU(U[] a) {}
  }

  @UriPrefix("a:")
  @Entity(name="AAA")
  public static class AAA<R extends Assoc, S> extends AA<R, S> {
  }

  @UriPrefix("b:")
  @Entity(name="AB")
  public static class AB extends AAA<Extended, String> {
  }


  @Entity(types={"t:assoc"}, name="Assoc")
  public static class Assoc extends Base {
  }

  @Entity(types={"t:extended"}, name="Extended")
  public static class Extended extends Assoc {
  }
}
