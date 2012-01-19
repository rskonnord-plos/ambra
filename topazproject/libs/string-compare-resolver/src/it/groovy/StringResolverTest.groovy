/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.topazproject.mulgara.itql.ItqlHelper

class StringResolverTest extends GroovyTestCase {
  String MULGARA    = 'http://localhost:9091/mulgara-service/services/ItqlBeanService'
  String TEST_MODEL = '<local:///topazproject#EqualIgnoreCaseTests>'
  String RSLV_MODEL = '<local:///topazproject#str>'
  String RSLV_TYPE  = '<http://topazproject.org/models#StringCompare>'
  ItqlHelper itql

  void setUp() {
    itql = new ItqlHelper(new URI(MULGARA))
    itql.doUpdate("create ${RSLV_MODEL} ${RSLV_TYPE};", null)
    itql.doUpdate("create ${TEST_MODEL};", null)
    itql.doUpdate("insert <foo:1> <bar:is> 'a' into ${TEST_MODEL};", null)
    itql.doUpdate("insert <foo:2> <bar:is> 'b' into ${TEST_MODEL};", null)
    itql.doUpdate("insert <foo:X> <bar:is> 'c' into ${TEST_MODEL};", null)
  }

  void testEqualIgnoresCase() {
    def query = """select \$s from ${TEST_MODEL} 
                    where \$s <bar:is> \$o 
                      and \$o <topaz:equalsIgnoreCase> 'B' in ${RSLV_MODEL}"""
    def results = itql.doQuery(query, null)
    def ans = new XmlSlurper().parseText(results)
    assert ans.query[0].solution.s.'@resource' == 'foo:2'
  }

  void testEqualIgnoreCaseSubject() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <topaz:equalsIgnoreCase> <foo:x> in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution.s.'@resource' == 'foo:X'
  }

  void testLt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'b' in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution.s.'@resource' == 'foo:1'
  }

  void testLt2() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:lt> 'c' in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:1', 'foo:2' ]
  }

  void testLe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:le> 'b' in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:1', 'foo:2' ]
  }

  void testGt1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:gt> 'b' in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution*.s.'@resource' == 'foo:X'
  }

  void testGe1() {
    def query = """select \$s from ${TEST_MODEL}
                    where \$s <bar:is> \$o
                      and \$o <topaz:ge> 'b' in ${RSLV_MODEL}"""
    def ans = new XmlSlurper().parseText(itql.doQuery(query, null))
    assert ans.query[0].solution*.s.'@resource'.list() == [ 'foo:2', 'foo:X' ]
  }
}
