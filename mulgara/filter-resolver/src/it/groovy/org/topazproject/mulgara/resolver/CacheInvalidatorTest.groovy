/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.mulgara.resolver;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * Basic tests on the cache-invalidator filter-handler.
 *
 * @author Ronald Tschalär
 */
class CacheInvalidatorTest extends AbstractTest {
  private final TestListener cel = new TestListener()

  void openDb() {
    Ehcache cache = CacheManager.getInstance().getEhcache('article-state')
    cache.getCacheEventNotificationService().registerListener(cel)

    System.properties[FilterResolverFactory.CONFIG_FACTORY_CONFIG_PROPERTY] =
                                          "/conf/topaz-factory-config.cache-invalidator-test.xml"

    super.openDb();
  }

  void closeDb() {
    super.closeDb();
    CacheManager.getInstance().getEhcache('queryCache').removeAll()
  }

  void testDirectKeys() {
    resetDb()
    cel.keys.clear()

    itql.executeUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)

    itql.executeUpdate("insert <foo:1> <topaz:pred> 'a' into ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['foo:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("insert <topaz:subj> <bar:has> 'a' into ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['a'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("insert <foo:1> <bar:has> <topaz:obj> into ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['bar:has'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <foo:1> <bar:is> 'a' from ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)

    itql.executeUpdate("delete <foo:1> <topaz:pred> 'a' from ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['foo:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <topaz:subj> <bar:has> 'a' from ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['a'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <foo:1> <bar:has> <topaz:obj> from ${TEST_GRAPH};")
    Thread.sleep(100)
    assertEquals(['bar:has'] as Set, cel.keys)
    cel.keys.clear()
  }

  void testQueryDerivedKeys() {
    /* <rule>
     *   <match>
     *     <p>topaz:articleState</p>
     *     <m>graph:test</m>
     *   </match>
     *   <object>
     *     <cache>article-state</cache>
     *     <query>
     *       select $s $state from &lt;graph:test&gt;
     *           where (&lt;${s}&gt; &lt;topaz:articleState&gt; $state)
     *           and ($s &lt;mulgara:is&gt; &lt;${s}&gt; or
     *                &lt;${s}&gt; &lt;topaz:propagate-permissions-to&gt; $s);
     *     </query>
     *   </object>
     * </rule>
     */
    resetDb()
    cel.keys.clear()

    itql.beginTransaction('qdk1')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_GRAPH};")
    itql.commit('qdk1')
    Thread.sleep(500)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk2')
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '1' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:2> <topaz:propagate-permissions-to> <bar:2> into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:2> <topaz:propagate-permissions-to> <baz:2> into ${TEST_GRAPH};")
    itql.commit('qdk2')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk3')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '1' from ${TEST_GRAPH};")
    itql.commit('qdk3')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk4')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '0' into ${TEST_GRAPH};")
    itql.commit('qdk4')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk5')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '1' from ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '0' into ${TEST_GRAPH};")
    itql.commit('qdk5')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk6')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '0' from ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '0' into ${TEST_GRAPH};")
    itql.commit('qdk6')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk7')
    itql.executeUpdate("insert <foo:2> <bar:2> '1' into ${TEST_GRAPH};")
    itql.commit('qdk7')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk8')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '0' from ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:3> <topaz:foo> '1' into ${TEST_GRAPH};")
    itql.commit('qdk8')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1', 'foo:3'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk9')
    itql.executeUpdate("delete <foo:3> <topaz:foo> '1' from ${TEST_GRAPH};")
    itql.commit('qdk9')
    Thread.sleep(100)
    assertEquals(['foo:3'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk10')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '0' from ${TEST_GRAPH};")
    itql.executeUpdate("delete <foo:2> <topaz:propagate-permissions-to> <bar:2> from ${TEST_GRAPH};")
    itql.executeUpdate("delete <foo:2> <topaz:propagate-permissions-to> <baz:2> from ${TEST_GRAPH};")
    itql.commit('qdk10')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    // test db restarts
    resetDb()

    itql.beginTransaction('qdk11')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_GRAPH};")
    itql.commit('qdk11')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    closeDb()
    openDb()

    itql.beginTransaction('qdk12')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_GRAPH};")
    itql.commit('qdk12')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk13')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '1' from ${TEST_GRAPH};")
    itql.executeUpdate("delete <foo:1> <topaz:propagate-permissions-to> <bar:1> from ${TEST_GRAPH};")
    itql.executeUpdate("delete <foo:1> <topaz:propagate-permissions-to> <baz:1> from ${TEST_GRAPH};")
    itql.commit('qdk13')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()
  }
}

class TestListener implements CacheEventListener {
  public final Set keys = []

  public void dispose() {
  }

  public void notifyElementEvicted(Ehcache cache, Element element) {
    throw new Error("Unexpected evict event")
  }

  public void notifyElementExpired(Ehcache cache, Element element) {
    throw new Error("Unexpected expired event")
  }

  public void notifyElementRemoved(Ehcache cache, Element element) {
    assert (element.getValue() == null)
    keys << element.getKey()
  }

  public void notifyElementPut(Ehcache cache, Element element) {
    throw new Error("Unexpected put event")
  }

  public void notifyElementUpdated(Ehcache cache, Element element) {
    throw new Error("Unexpected updated event")
  }

  public void notifyRemoveAll(Ehcache cache) {
    throw new Error("Unexpected remove-all event")
  }
}
