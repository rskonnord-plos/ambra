/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.util.logging.Level;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * Basic tests on the cache-invalidator filter-handler.
 *
 * @author Ronald Tschalär
 */
class CacheInvalidatorTest extends GroovyTestCase {
  static final String MULGARA     = 'local:///topazproject'
  static final String TEST_MODEL  = "<${MULGARA}#filter:model=test>"
  static final String REAL_MODEL  = "<${MULGARA}#test>"
  static final String RSLV_TYPE   = "<${FilterResolver.MODEL_TYPE}>"
  static final SessionFactory sf  = SessionFactoryFinder.newSessionFactory(MULGARA.toURI())
  static final TestListener   cel = new TestListener()

  ItqlInterpreterBean itql

  static {
    sf.setDirectory(new File(new File(System.getProperty('basedir')), 'target/mulgara-db'))
    openDb()
  }

  static void openDb() {
    sf.newSession().close()     // force initialization of resolver and filter-handler
    Ehcache cache = CacheManager.getInstance().getEhcache('article-state')
    cache.getCacheEventNotificationService().registerListener(cel)
  }

  static void closeDb() {
    sf.close()
    CacheManager.getInstance().getEhcache('queryCache').removeAll()
  }

  private void openDbCon() {
    itql = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain())
    itql.setAliasMap([topaz:'http://rdf.topazproject.org/RDF/'.toURI()])
  }

  private void closeDbCon() {
    itql.close()
  }

  void setUp() {
    openDbCon()
  }

  void tearDown() {
    closeDbCon()
  }

  private void resetDb() {
    try {
      itql.executeUpdate("drop ${TEST_MODEL};")
    } catch (Exception e) {
      log.log(Level.FINE, "Error dropping ${TEST_MODEL} (probably because it doesn't exist)", e)
    }
    try {
      itql.executeUpdate("drop ${REAL_MODEL};")
    } catch (Exception e) {
      log.log(Level.FINE, "Error dropping ${REAL_MODEL} (probably because it doesn't exist)", e)
    }

    itql.executeUpdate("create ${TEST_MODEL} ${RSLV_TYPE};")
  }

  void testDirectKeys() {
    resetDb()
    cel.keys.clear()

    itql.executeUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)

    itql.executeUpdate("insert <foo:1> <topaz:pred> 'a' into ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['foo:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("insert <topaz:subj> <bar:has> 'a' into ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['a'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("insert <foo:1> <bar:has> <topaz:obj> into ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['bar:has'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <foo:1> <bar:is> 'a' from ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)

    itql.executeUpdate("delete <foo:1> <topaz:pred> 'a' from ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['foo:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <topaz:subj> <bar:has> 'a' from ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['a'] as Set, cel.keys)
    cel.keys.clear()

    itql.executeUpdate("delete <foo:1> <bar:has> <topaz:obj> from ${TEST_MODEL};")
    Thread.sleep(100)
    assertEquals(['bar:has'] as Set, cel.keys)
    cel.keys.clear()
  }

  void testQueryDerivedKeys() {
    /* <rule>
     *   <match>
     *     <p>topaz:articleState</p>
     *     <m>model:test</m>
     *   </match>
     *   <object>
     *     <cache>article-state</cache>
     *     <query>
     *       select $s $state from &lt;model:test&gt;
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
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_MODEL};")
    itql.commit('qdk1')
    Thread.sleep(500)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk2')
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '1' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:2> <topaz:propagate-permissions-to> <bar:2> into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:2> <topaz:propagate-permissions-to> <baz:2> into ${TEST_MODEL};")
    itql.commit('qdk2')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk3')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '1' from ${TEST_MODEL};")
    itql.commit('qdk3')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk4')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '0' into ${TEST_MODEL};")
    itql.commit('qdk4')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk5')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '1' from ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '0' into ${TEST_MODEL};")
    itql.commit('qdk5')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk6')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '0' from ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:2> <topaz:articleState> '0' into ${TEST_MODEL};")
    itql.commit('qdk6')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk7')
    itql.executeUpdate("insert <foo:2> <bar:2> '1' into ${TEST_MODEL};")
    itql.commit('qdk7')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk8')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '0' from ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:3> <topaz:foo> '1' into ${TEST_MODEL};")
    itql.commit('qdk8')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1', 'foo:3'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk9')
    itql.executeUpdate("delete <foo:3> <topaz:foo> '1' from ${TEST_MODEL};")
    itql.commit('qdk9')
    Thread.sleep(100)
    assertEquals(['foo:3'] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk10')
    itql.executeUpdate("delete <foo:2> <topaz:articleState> '0' from ${TEST_MODEL};")
    itql.executeUpdate("delete <foo:2> <topaz:propagate-permissions-to> <bar:2> from ${TEST_MODEL};")
    itql.executeUpdate("delete <foo:2> <topaz:propagate-permissions-to> <baz:2> from ${TEST_MODEL};")
    itql.commit('qdk10')
    Thread.sleep(100)
    assertEquals(['foo:2', 'bar:2', 'baz:2'] as Set, cel.keys)
    cel.keys.clear()

    // test db restarts
    resetDb()

    itql.beginTransaction('qdk11')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_MODEL};")
    itql.commit('qdk11')
    Thread.sleep(100)
    assertEquals(['foo:1', 'bar:1', 'baz:1'] as Set, cel.keys)
    cel.keys.clear()

    closeDbCon()
    closeDb()
    openDb()
    openDbCon()

    itql.beginTransaction('qdk12')
    itql.executeUpdate("insert <foo:1> <topaz:articleState> '1' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <bar:1> into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <topaz:propagate-permissions-to> <baz:1> into ${TEST_MODEL};")
    itql.commit('qdk12')
    Thread.sleep(100)
    assertEquals([] as Set, cel.keys)
    cel.keys.clear()

    itql.beginTransaction('qdk13')
    itql.executeUpdate("delete <foo:1> <topaz:articleState> '1' from ${TEST_MODEL};")
    itql.executeUpdate("delete <foo:1> <topaz:propagate-permissions-to> <bar:1> from ${TEST_MODEL};")
    itql.executeUpdate("delete <foo:1> <topaz:propagate-permissions-to> <baz:1> from ${TEST_MODEL};")
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
