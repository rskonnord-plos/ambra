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
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.mapping.java.ClassBinder;

public class MultiBoundClassTest extends TestCase {
  private SessionFactory sf = new SessionFactoryImpl();

  public void testMultiBoundClass() throws OtmException {
    sf.preload(Permission.class);

    sf.addGraph(new GraphConfig("grants", URI.create("local:///topazproject#grants")));
    sf.addGraph(new GraphConfig("revokes", URI.create("local:///topazproject#revokes")));

    Set<String> types = Collections.emptySet();
    Set<String> sup = Collections.singleton("Permission");
    sf.addDefinition(new EntityDefinition("Grants", types, "grants", sup));
    sf.addDefinition(new EntityDefinition("Revokes", types, "revokes", sup));

    // Bind it to the same Permission.class. Note that 'suppressAliases' is true here.
    sf.getClassBinding("Grants").bind(EntityMode.POJO, new ClassBinder(Permission.class, true));
    sf.getClassBinding("Revokes").bind(EntityMode.POJO, new ClassBinder(Permission.class, true));

    // Validate and build metadata
    sf.validate();

    ClassMetadata cm = sf.getClassMetadata("Permission");
    ClassMetadata gm = sf.getClassMetadata("Grants");
    ClassMetadata rm = sf.getClassMetadata("Revokes");

    assertNotNull(cm);
    assertNotNull(gm);
    assertNotNull(rm);
    assertEquals(cm, sf.getClassMetadata(Permission.class));
    assertTrue(cm.isAssignableFrom(gm, EntityMode.POJO));
    assertTrue(cm.isAssignableFrom(rm, EntityMode.POJO));

    assertEquals("grants", gm.getGraph());
    assertEquals("revokes", rm.getGraph());
    assertNull(cm.getGraph());
  }

  @Entity(name="Permission", types={"foo:bar"})
  public static class Permission {
    @Id
    public String getResource() {return null;}
    public void setResource(String resource) {}
    @PredicateMap
    public Map<String, List<String>> getPermissions() {return null;}
    public void setPermissions(Map<String, List<String>> permissions) {}
  }
}
