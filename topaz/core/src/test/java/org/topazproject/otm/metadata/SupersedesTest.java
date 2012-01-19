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

import java.util.Collection;
import java.util.Collections;
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

public class SupersedesTest extends TestCase {
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

  public void test01() {
    sf.preload(B.class);
    sf.preload(Extended.class);
    sf.validate();

    RdfDefinition assoc, extended;
    assoc = (RdfDefinition)sf.getDefinition("A:object");
    extended = (RdfDefinition)sf.getDefinition("B:object");

    extended.resolveReference(sf);
    Object[] vals = new Object[] {"a:object", true, "test", false, true, null, "Assoc",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    compare(assoc, vals);
    vals[6] = "Extended";
    compare(extended, vals);

    assertTrue(extended.refersSameGraphNodes(assoc));
    assertTrue(extended.refersSameRange(assoc));
  }

  public void test02() {
    sf.preload(B.class);
    sf.preload(Extended.class);
    sf.validate();
    ClassMetadata cm = sf.getClassMetadata("A");
    assertEquals(1, cm.getRdfMappers().size());
    assertEquals("object", cm.getRdfMappers().iterator().next().getName());
    assertEquals("Assoc", cm.getRdfMappers().iterator().next().getAssociatedEntity());

    cm = sf.getClassMetadata("B");
    assertEquals(1, cm.getRdfMappers().size());
    assertEquals("object", cm.getRdfMappers().iterator().next().getName());
    assertEquals("Extended", cm.getRdfMappers().iterator().next().getAssociatedEntity());
  }

  public void test03() {
    sf.preload(S.class);
    sf.validate();

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition("O:object");
    def = (RdfDefinition)sf.getDefinition("S:object");
    def.resolveReference(sf);

    Object[] vals = new Object[] {"a:object", true, "test", false, false, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(def, vals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  public void test04() {
    sf.preload(I.class);
    sf.validate();

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition("O:object");
    def = (RdfDefinition)sf.getDefinition("I:object");
    def.resolveReference(sf);

    Object[] vals = new Object[] {"a:object", true, "test", false, false, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(def, vals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  public void test05() {
    sf.preload(II.class);
    sf.validate();

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition("O:object");
    def = (RdfDefinition)sf.getDefinition("II:object");
    def.resolveReference(sf);

    Object[] vals = new Object[] {"a:object", true, "test", false, false, Rdf.xsd + "int", null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(def, vals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  public void test06() {
    sf.preload(AS.class);
    sf.validate();

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition("O:object");
    def = (RdfDefinition)sf.getDefinition("AS:object");
    def.resolveReference(sf);

    Object[] vals = new Object[] {"a:object", true, "test", false, false, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(def, vals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  public void test07() {
    sf.preload(SII.class);
    sf.validate();

    RdfDefinition base, def;
    base = (RdfDefinition)sf.getDefinition("O:object");
    def = (RdfDefinition)sf.getDefinition("SII:object");
    def.resolveReference(sf);

    Object[] vals = new Object[] {"a:object", true, "test", false, false, Rdf.xsd + "int", null,
                               CollectionType.RDFSEQ, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(def, vals);

    assertTrue(def.refersSameGraphNodes(base));
    assertFalse(def.refersSameRange(base));
  }

  @Entity(graph="test", name="Base")
  @UriPrefix("a:")
  public static abstract class Base {
    public String getId() {return null;}
    @Id
    public void setId(String s) {}
  }

  @Entity(name="O")
  public static abstract class O extends Base {
    @Predicate(graph="test")
    public Object getObject() {return null;}
    public void setObject(Object a) {}
    public void setObject(Collection a) {}
    public void setObject(Object[] a) {}
  }

  @Entity(name="A")
  public static class A extends O {
    @Predicate(graph="test")
    public Assoc getObject() {return null;}
  }

  @UriPrefix("b:")
  @Entity(name="B")
  public static class B extends A {
    @Override
    @Predicate
    public Extended getObject() {return null;}
  }

  @Entity(name="S")
  public static class S extends O {
    @Predicate(graph="test")
    public String getObject() {return null;}
  }

  @Entity(name="I")
  public static class I extends O {
    @Predicate(graph="test")
    public Integer getObject() {return null;}
  }

  @Entity(name="II")
  public static class II extends O {
    @Predicate(graph="test", dataType="xsd:int")
    public Integer getObject() {return null;}
  }

  @Entity(name="AS")
  public static class AS extends O {
    @Predicate(graph="test")
    public String[] getObject() {return null;}
  }

  @Entity(name="SII")
  public static class SII extends O {
    @Predicate(graph="test", dataType="xsd:int", collectionType=CollectionType.RDFSEQ)
    public List<Integer> getObject() {return null;}
  }

  @Entity(types={"t:assoc"}, name="Assoc")
  public static class Assoc extends Base {
  }

  @Entity(types={"t:extended"}, name="Extended")
  public static class Extended extends Assoc {
  }

}
