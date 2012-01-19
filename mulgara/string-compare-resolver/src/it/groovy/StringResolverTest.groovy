/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;

class StringResolverTest extends GroovyTestCase {
  static final String MULGARA    = 'local:///topazproject'
  static final String TEST_MODEL = '<local:///topazproject#EqualIgnoreCaseTests>'
  static final String RSLV_MODEL = '<local:///topazproject#str>'
  static final String RSLV_TYPE  = '<http://topazproject.org/models#StringCompare>'
  static final SessionFactory sf = SessionFactoryFinder.newSessionFactory(MULGARA.toURI());

  ItqlInterpreterBean itql

  static {
    sf.setDirectory(new File(new File(System.getProperty('basedir')), 'target/mulgara-db'))
  }

  // somewhere: sf.delete()

  void setUp() {
    itql = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain());
    itql.setAliasMap([topaz:'http://rdf.topazproject.org/RDF/'.toURI()])
    itql.executeUpdate("create ${RSLV_MODEL} ${RSLV_TYPE};")
    itql.executeUpdate("create ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:2> <bar:is> 'b' into ${TEST_MODEL};")
    itql.executeUpdate("insert <foo:X> <bar:is> 'c' into ${TEST_MODEL};")
  }

  void tearDown() {
    itql.close();
  }

  void testEqualIgnoresCase() {
    def query = """select \$s from ${TEST_MODEL} 
                    where \$s <bar:is> \$o 
                      and \$o <topaz:equalsIgnoreCase> 'B' in ${RSLV_MODEL};"""
    def results = itql.executeQueryToString(query)
    def ans = new XmlSlurper().parseText(results)
    assert ans.query[0].solution.s.'@resource' == 'foo:2'
  }

  void testEqualIgnoreCaseSubject() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <topaz:equalsIgnoreCase> <foo:x> in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution.s.'@resource' == 'foo:X'
  }

  void testLt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'b' in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution.s.'@resource' == 'foo:1'
  }

  void testLt2() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'c' in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:1', 'foo:2' ]
  }

  void testLe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:le> 'b' in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:1', 'foo:2' ]
  }

  void testGt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:gt> 'b' in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution*.s.'@resource' == 'foo:X'
  }

  void testGe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:ge> 'b' in ${RSLV_MODEL};"""
    def ans = new XmlSlurper().parseText(itql.executeQueryToString(query))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:2', 'foo:X' ]
  }
}
