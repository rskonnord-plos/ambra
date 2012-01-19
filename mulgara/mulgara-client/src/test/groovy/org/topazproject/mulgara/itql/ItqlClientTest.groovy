/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
    String model = "<local:///test1#m1>";
    doCommands(itql, model) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '42' into ${model};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    itql.close();

    // Create a second client and test it sees the same db
    itql = icf.createClient(URI.create("local:///test1"));
    doCommands(itql, model, false) { checker ->
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    itql.close();

    // Create two clients on different db's and check they are distinct
    /* Doesn't work: XANodePoolFactory uses a singleton and hence will always mess us up
    itql = icf.createClient(URI.create("local:///test1"));
    ItqlClient itql2 = icf.createClient(URI.create("local:///test2"));
    String model2 = "<local:///test2#m1>";

    doCommands(itql2, model2) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '42' into ${model2};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model2} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('42') }
      }
    }
    doCommands(itql, model, false) { checker ->
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
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

    model = "<local:///test1#m1>";
    doCommands(itql, model) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45' into ${model};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one'); literal ('45') }
      }
    }

    // test literal datatypes
    model = "<local:///test1#m1>";
    doCommands(itql, model) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45'^^<xsd:int> into ${model};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one'); literal ('45', dt:'xsd:int') }
      }
    }

    // test literal language-tags
    /* not supported by mulgara (yet)
    model = "<local:///test1#m1>";
    doCommands(itql, model) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45'@fr into ${model};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('well:/blow/me/down#one'); uri('bar:one'); literal ('45', lang:'en') }
      }
    }
    */
    itql.close();

    // test transactions
    itql = icf.createClient(URI.create("local:///test1"));

    model = "<local:///test1#m1>";
    doCommands(itql, model) { checker ->
      itql.doUpdate("insert <foo:one> <bar:one> '45' into ${model};");
      List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
      checker.verify(ans, variables:['s', 'p', 'o']) {
        row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      }
    }

    List ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o;")
    def checker = new AnswerChecker(test:this)
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
    }

    itql.doUpdate("insert <foo:two> <bar:two> '46' into ${model};");

    itql.beginTxn("tx-rb")
    itql.doUpdate("insert <foo:thr> <bar:thr> '47' into ${model};");
    ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o order by \$o;")
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      row { uri ('foo:two'); uri('bar:two'); literal ('46') }
      row { uri ('foo:thr'); uri('bar:thr'); literal ('47') }
    }
    itql.rollbackTxn("tx-rb")

    ans = itql.doQuery("select \$s \$p \$o from ${model} where \$s \$p \$o order by \$o;")
    checker.verify(ans, variables:['s', 'p', 'o']) {
      row { uri ('foo:one'); uri('bar:one'); literal ('45') }
      row { uri ('foo:two'); uri('bar:two'); literal ('46') }
    }
  }

  private void doCommands(ItqlClient itql, String model, Closure c) {
    doCommands(itql, model, true, c)
  }

  private void doCommands(ItqlClient itql, String model, boolean clean, Closure c) {
    if (clean) {
      try {
        itql.doUpdate("drop ${model};");
      } catch (Exception e) {
      }
      itql.doUpdate("create ${model};");
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
