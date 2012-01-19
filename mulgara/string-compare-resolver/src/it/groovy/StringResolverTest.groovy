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

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

class StringResolverTest extends GroovyTestCase {
  static final String MULGARA    = 'local:///topazproject'
  static final String TEST_GRAPH = '<local:///topazproject#EqualIgnoreCaseTests>'
  static final String RSLV_GRAPH = '<local:///topazproject#str>'
  static final String RSLV_TYPE  = '<http://topazproject.org/graphs#StringCompare>'
  static final SessionFactory sf = SessionFactoryFinder.newSessionFactory(MULGARA.toURI());

  ItqlInterpreterBean itql

  static {
    sf.setDirectory(new File(new File(System.getProperty('basedir')), 'target/mulgara-db'))
  }

  // somewhere: sf.delete()

  void setUp() {
    itql = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain());
    itql.setAliasMap([topaz:'http://rdf.topazproject.org/RDF/'.toURI()])
    itql.executeUpdate("create ${RSLV_GRAPH} ${RSLV_TYPE};")
    itql.executeUpdate("create ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:2> <bar:is> 'b' into ${TEST_GRAPH};")
    itql.executeUpdate("insert <foo:X> <bar:is> 'c' into ${TEST_GRAPH};")
  }

  void tearDown() {
    itql.close();
  }

  void testEqualIgnoresCase() {
    testFunction('equalsIgnoreCase', 'equalsIgnoreCase', 'B', [ 'foo:2' ], 'foo:x', [ 'foo:X' ])
  }

  void testLt() {
    testFunction('lt', 'gt', 'b', [ 'foo:1' ], 'foo:2', [ 'foo:1' ])
    testFunction('lt', 'gt', 'c', [ 'foo:1', 'foo:2' ], 'foo:X', [ 'foo:1', 'foo:2' ])
  }

  void testLe() {
    testFunction('le', 'ge', 'b', [ 'foo:1', 'foo:2' ], 'foo:2', [ 'foo:1', 'foo:2' ])
  }

  void testGt() {
    testFunction('gt', 'lt', 'b', [ 'foo:X' ], 'foo:2', [ 'foo:X' ])
    testFunction('gt', 'lt', 'a', [ 'foo:2', 'foo:X' ], 'foo:1', [ 'foo:2', 'foo:X' ])
  }

  void testGe() {
    testFunction('ge', 'le', 'b', [ 'foo:2', 'foo:X' ], 'foo:2', [ 'foo:2', 'foo:X' ])
  }

  void testFunction(String op, String revOp, String obj, def expObj, String subj, def expSubj) {
    runQuery('$o',        "<topaz:${op}>",    "'${obj}'",  expObj)
    runQuery("'${obj}'",  "<topaz:${revOp}>", '$o',        expObj)

    runQuery('$s',        "<topaz:${op}>",    "<${subj}>", expSubj)
    runQuery("<${subj}>", "<topaz:${revOp}>", '$s',        expSubj)
  }

  void runQuery(String subj, String pred, String obj, def expected) {
    def query = """select \$s from ${TEST_GRAPH}
                    where \$s <bar:is> \$o
                      and ${subj} ${pred} ${obj} in ${RSLV_GRAPH};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assertEquals(expected, ans.query[0].solution*.s.'@resource'.list()*.text())
  }
}
