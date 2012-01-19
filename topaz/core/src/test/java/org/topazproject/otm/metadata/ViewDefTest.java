/* $HeadURL::                                                                            $
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

package org.topazproject.otm.metadata;

import java.net.URI;

import junit.framework.TestCase;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.View;

public class ViewDefTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();

  public void testNoGettersView() throws OtmException {
    sf.preload(A.class);
    sf.preload(Assoc2.class);
    sf.preload(B.class);
    sf.validate();
    assertNotNull(sf.getClassMetadata("B"));
    assertNotNull(sf.getClassMetadata(B.class));
    assertNotNull(sf.getClassMetadata(B.class.getName()));
  }

  @Entity(graph="test", name="Base")
  @UriPrefix("a:")
  public static class Base {
    public String getId() {return null;}
    @Id
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

  @Entity(types={"t:a1"}, name="Assoc1")
  public static class Assoc1 extends Base {
  }

  @Entity(types={"t:a2"}, name="Assoc2")
  public static class Assoc2 extends Assoc1 {
  }

  @View(name="B", query="select a.p1, a.p2, a.p3 from A a where a.id = :id")
  public static class B {
    @Id
    public void setId(String s) {}
    @Projection("p1")
    public void setP1(String s) {}
    @Projection("p2")
    public void setP2(Assoc1 u) {}
    @Projection("p3")
    public void setP3(Assoc2 a) {}
  }
}
