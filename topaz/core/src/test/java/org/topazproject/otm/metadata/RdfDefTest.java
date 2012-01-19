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

import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.CascadeType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

public class RdfDefTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();
  private RdfDefinition ap1,ap2,ap3,bp1,bp2,bp3;

  public void setUp() throws OtmException {
    sf.preload(A.class);
    sf.preload(B.class);
    sf.preload(Assoc2.class);
    sf.validate();

    ap1 = (RdfDefinition)sf.getDefinition("A:p1");
    ap2 = (RdfDefinition)sf.getDefinition("A:p2");
    ap3 = (RdfDefinition)sf.getDefinition("A:p3");
    bp1 = (RdfDefinition)sf.getDefinition("B:p1");
    bp2 = (RdfDefinition)sf.getDefinition("B:p2");
    bp3 = (RdfDefinition)sf.getDefinition("B:p3");
  }

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
    Object[] vals = new Object[] {"a:p1", true, "test", false, false, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(ap1, vals);
    compare(bp1, vals);

    assertTrue(ap1.refersSameGraphNodes(bp1));
    assertTrue(ap1.refersSameRange(bp1));
  }

  public void test02() {
    Object[] vals = new Object[] {"a:p2", true, "test", false, true, null, null,
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), false};

    compare(ap2, vals);
    vals[6] = "Assoc1";
    vals[10] = true;
    compare(bp2, vals);

    assertTrue(ap2.refersSameGraphNodes(bp2));
    // FIXME assertFalse(ap2.refersSameRange(bp2));
  }

  public void test03() {
    Object[] vals = new Object[] {"a:p3", true, "test", false, true, null, "Assoc1",
                               CollectionType.PREDICATE, FetchType.lazy,
                               Collections.singleton(CascadeType.peer), true};

    compare(ap3, vals);
    vals[6] = "Assoc2";
    compare(bp3, vals);

    assertTrue(ap3.refersSameGraphNodes(bp3));
    // FIXME assertFalse(ap3.refersSameRange(bp3));
  }

  public void test04() {
    sf.preload(Journal.class);
    sf.validate();
    assertNotNull(sf.getClassMetadata(Journal.class).getMapperByName("eIssn"));
  }

  public void test05() {
    ClassMetadata cm = sf.getClassMetadata(RdfNode.class);
    assertEquals(0, cm.getSuperEntities().size());

    for (Class e : new Class[] {A.class, B.class, Assoc1.class, Assoc2.class}) {
      cm = sf.getClassMetadata(e);
      assertEquals(1, cm.getSuperEntities().size());
      assertEquals(sf.getClassMetadata(e.getSuperclass()).getName(), cm.getSuperEntities().iterator().next());
    }

    cm = sf.getClassMetadata(Base.class);
    assertEquals(1, cm.getSuperEntities().size());
    assertEquals("RdfNode", cm.getSuperEntities().iterator().next());
  }

  public void test06() throws Exception {
    for (Class e : new Class[] {A.class, B.class, Assoc1.class, Assoc2.class})
      assertEquals(sf.getClassMetadata(e), sf.getInstanceMetadata(null, EntityMode.POJO, e.newInstance()));
  }

  @Entity(graph="test", name="RdfNode")
  @UriPrefix("a:")
  public static interface RdfNode {
    public String getId();
    @Id
    public void setId(String s);
  }

  @Entity(graph="test", name="Base")
  @UriPrefix("a:")
  public static class Base implements RdfNode {
    public String getId() {return null;}
    public void setId(String s) {}
  }

  @Entity(name="A")
  public static class A extends Base {
    public String getP1() {return null;}
    @Predicate(graph="test", dataType=Predicate.UNTYPED)
    public void setP1(String s) {}
    public URI getP2() {return null;}
    @Predicate(graph="test")
    public void setP2(URI u) {}
    public Assoc1 getP3() {return null;}
    @Predicate(graph="test")
    public void setP3(Assoc1 a) {}
  }

  @UriPrefix("b:")
  @Entity(name="B")
  public static class B extends Base {
    public String getP1() {return null;}
    @Predicate(ref="A:p1")
    public void setP1(String s) {}
    public Assoc1 getP2() {return null;}
    @Predicate(ref="A:p2")
    public void setP2(Assoc1 u) {}
    public Assoc2 getP3() {return null;}
    @Predicate(ref="A:p3")
    public void setP3(Assoc2 a) {}
  }

  @Entity(types={"t:a1"}, name="Assoc1")
  public static class Assoc1 extends Base {
  }

  @Entity(types={"t:a2"}, name="Assoc2")
  public static class Assoc2 extends Assoc1 {
  }

  @Entity(name="Journal")
  public static class Journal extends Base {
    public String geteIssn() {return null;}
    @Predicate
    public void seteIssn(String s) {}
  }
}
