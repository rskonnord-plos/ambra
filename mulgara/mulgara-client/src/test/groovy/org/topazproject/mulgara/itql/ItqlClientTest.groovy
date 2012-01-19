/* $HeadURL::                                                                            $
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

package org.topazproject.mulgara.itql;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Basic itql-client tests. 
 * 
 * @author Ronald Tschalär
 */
public class ItqlClientTest extends GroovyTestCase {
  private static final Log log = LogFactory.getLog(ItqlClientTest.class);

  public void testEmbeddedClient() {
    ItqlClientFactory icf = new DefaultItqlClientFactory();

    // Create one client and run a couple commands
    ItqlClient itql = icf.createClient(URI.create("local:///test1"));
    String graph = "<local:///test1#m1>";
    doCommands(itql, graph) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '42' into ${graph};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    itql.close();

    // Create a second client and test it sees the same db
    itql = icf.createClient(URI.create("local:///test1"));
    doCommands(itql, graph, false) { checker ->
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    itql.close();

    // Create two clients on different db's and check they are distinct
    /* Doesn't work: XANodePoolFactory uses a singleton and hence will always mess us up
    itql = icf.createClient(URI.create("local:///test1"));
    ItqlClient itql2 = icf.createClient(URI.create("local:///test2"));
    String graph2 = "<local:///test2#m1>";

    doCommands(itql2, graph2) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '42' into ${graph2};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph2} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    doCommands(itql, graph, false) { checker ->
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }

    itql.close();
    itql2.close();
    */

    // test aliases work
    itql = icf.createClient(URI.create("local:///test1"));
    itql.setAliases([foo:'well:/blow/me/down#'])
    assertEquals([foo:'well:/blow/me/down#'], itql.getAliases())

    graph = "<local:///test1#m1>";
    doCommands(itql, graph) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45' into ${graph};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one'); literal ('45') }
      }
    }

    // test literal datatypes
    graph = "<local:///test1#m1>";
    doCommands(itql, graph) { checker ->
      itql.doUpdate(
          "insert <foo:one> <bar:one> '45'^^<http://www.w3.org/2001/XMLSchema#int> into ${graph};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one');
              literal ('45', dt:'http://www.w3.org/2001/XMLSchema#int') }
      }
    }

    // test literal language-tags
    graph = "<local:///test1#m1>";
    doCommands(itql, graph) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45'@fr into ${graph};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one'); literal ('45', lang:'fr') }
      }
    }
    itql.close();

    // test transactions
    itql = icf.createClient(URI.create("local:///test1"));

    graph = "<local:///test1#m1>";
    doCommands(itql, graph) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45' into ${graph};");
      List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      }
    }

    List ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o;")
    def checker = new AnswerChecker(test:this)
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
    }

    itql.doUpdate("insert <foo:two> <bar:two> '46' into ${graph};");

    itql.beginTxn("tx-rb")
    itql.doUpdate("insert <foo:thr> <bar:thr> '47' into ${graph};");
    ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o order by \$o;")
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      row { uri ('foo:two'); uri('bar:two'); literal ('46') }
      row { uri ('foo:thr'); uri('bar:thr'); literal ('47') }
    }
    itql.rollbackTxn("tx-rb")

    ans = itql.doQuery("select \$s \$p \$o from ${graph} where \$s \$p \$o order by \$o;")
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      row { uri ('foo:two'); uri('bar:two'); literal ('46') }
    }
  }

  private void doCommands(ItqlClient itql, String graph, Closure c) {
    doCommands(itql, graph, true, c)
  }

  private void doCommands(ItqlClient itql, String graph, boolean clean, Closure c) {
    if (clean) {
      try {
        itql.doUpdate("drop ${graph};");
      } catch (Exception e) {
      }
      itql.doUpdate("create ${graph};");
    }

    doInTx(itql) {
      def checker = new AnswerChecker(test:this)
      c(checker)
    }
  }

  private def doInTx(ItqlClient itql, Closure c) {
    itql.beginTxn("t1")
    try {
      def r = c(itql)
      itql.commitTxn("t1")
      return r
    } catch (Exception e) {
      try {
        itql.rollbackTxn("t1")
      } catch (Exception ee) {
        log.warn("rollback failed", ee)
      }
      log.error("error: ${e}", e)
      throw e
    }
  }
}

class AnswerChecker extends BuilderSupport {
  private Stack ansHist = new Stack()
  private Stack colHist = new Stack()

  private Answer  ans
  private int     col = 0

  GroovyTestCase test

  protected Object createNode(Object name) {
    return createNode(name, null, null)
  }

  protected Object createNode(Object name, Object value) {
    return createNode(name, null, value)
  }

  protected Object createNode(Object name, Map attributes) {
    return createNode(name, attributes, null)
  }

  protected Object createNode(Object name, Map attributes, Object value) {
    switch (name) {
      case 'verify':
        test.assertEquals(1, value.size)
        ans = value[0]

        if (attributes?.message)
          test.assertNotNull(ans.message)
        else {
          if (ans.message)
            test.log.error "Got message: " + ans.message
          test.assertNull(ans.message)
        }

        if (attributes?.variables)
          test.assertArrayEquals(attributes.variables as Object[], ans.variables)

        break

      case 'row':
        test.assertTrue(ans.next())
        col = 0
        break

      case 'string':
        test.assertEquals(value.toString(), ans.getString(col++))
        break

      case 'uri':
        test.assertTrue(ans.isURI(col))
        test.assertEquals((value instanceof URI) ? value : value.toURI(), ans.getURI(col++))
        break

      case 'literal':
        test.assertTrue(ans.isLiteral(col))
        test.assertEquals(value, ans.getString(col))

        if (attributes?.dt)
          test.assertEquals(
              (attributes.dt instanceof String) ? attributes.dt : attributes.dt.toString(),
              ans.getLiteralDataType(col))
        else
          test.assertNull(ans.getLiteralDataType(col))

        if (attributes?.lang)
          test.assertEquals(attributes.lang, ans.getLiteralLangTag(col))
        else
          test.assertNull(ans.getLiteralLangTag(col))

        col++
        break

      case 'subq':
        test.assertTrue(ans.isSubQueryResults(col))
        Answer a = ans.getSubQueryResults(col++)
        ansHist.push(ans)
        colHist.push(col)
        ans = a
        break

      default:
        throw new Exception("unsupported item '${name}'")
    }

    return name
  }

  protected void setParent(Object parent, Object child) {
  }

  protected void nodeCompleted(Object parent, Object node) {
    if (parent == null || node == 'subq') {
      test.assertFalse(ans.next())
      ans.close()
    }

    if (node == 'subq') {
      ans = ansHist.pop()
      col = colHist.pop()
    }
  }
}
