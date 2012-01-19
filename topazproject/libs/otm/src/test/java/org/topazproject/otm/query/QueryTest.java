
package org.topazproject.otm.query;

import java.io.StringReader;
import java.net.URI;

import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

import junit.framework.TestCase;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.samples.ReplyThread;

public class QueryTest extends TestCase {
  private static final String[] parseOkQueries = {
    // simple projections, projection aliases, variable number of projections
    "select a from Article a where a.uri = '42';",
    "select a a from Article a where a.uri = '42';",
    "select a a, b from Article a where a.uri = '42';",
    "select a a, b b from Article a where a.uri = '42';",
    "select a, b b from Article a where a.uri = '42';",
    "select a, b from Article a where a.uri = '42';",
    "select a, b, c from Article a where a.uri = '42';",

    // complex projection expressions
    "select a.uri, b from Article a where a.uri = '42';",
    "select a.foo.bar, b.* from Article a where a.uri = '42';",
    "select a.foo.bar, b.c.* from Article a where a.uri = '42';",
    "select count(a.foo.bar), b from Article a where a.uri = '42';",
    "select count(a.foo.bar) c, b from Article a where a.uri = '42';",
    "select x:count(a.foo.bar) c, b from Article a where a.uri = '42';",
    "select count(a.foo.bar) c, (select d from Dummy d where true()) from Article a where a.uri = '42';",
    "select count(a.foo.bar) c, (select d from Dummy d where true()) e from Article a where a.uri = '42';",
    "select x:count(y:size(a.foo.bar)) c, (select d from Dummy d where true()) e from Article a where a.uri = '42';",
    "select x:count(y:size(a.foo.bar)) c, (select d from Dummy d where true()) e from Article a where a.uri = '42';",

    // variable number of from clauses
    "select a, b from Article a, Reply b where a.uri = '42';",
    "select a, b from Article a, Reply b, Thread t where a.uri = '42';",

    // order, limit, offset clauses
    "select a from Article a where a.uri = '42' order by a;",
    "select a from Article a where a.uri = '42' limit 10;",
    "select a from Article a where a.uri = '42' offset 20;",
    "select a from Article a where a.uri = '42' order by a limit 10 offset 20;",

    // where clauses: var comparisons
    "select a from Article a where a.uri = '42';",
    "select a from Article a where a.uri != '42';",
    "select a from Article a where a.uri = a.foo;",
    "select a from Article a where a.uri != a.foo;",
    "select a from Article a where a.uri = a;",
    "select a from Article a where a.uri != a;",
    "select a from Article a where '42' = a;",
    "select a from Article a where '42' != a;",
    "select a from Article a where '42' = a.uri;",
    "select a from Article a where '42' != a.uri;",

    // where clauses: literals and uri-refs
    "select a from Article a where a.uri = '42'^^<xsd:int>;",
    "select a from Article a where a.uri != '42'@en;",
    "select a from Article a where foo('42'^^<xsd:int>, '12'@en, <hello>, 'bye');",

    // where clauses: aliases
    "select a from Article a where pp := a.foo.bar;",
    "select a from Article a where a.uri = '42' and pp := a.foo and pp.bar = <hello>;",

    // where clauses: function comparisons
    "select a from Article a where a.uri = foo('42');",
    "select a from Article a where a.uri != foo('42');",
    "select a from Article a where a.uri = foo(a.foo, a.bar);",
    "select a from Article a where a.uri != foo(a.foo, a.bar);",
    "select a from Article a where '42' = foo(a.uri);",
    "select a from Article a where '42' != foo(a.uri);",
    "select a from Article a where foo(a.uri) = '42';",
    "select a from Article a where foo(a.uri) != '42';",
    "select a from Article a where foo(a.uri, a.bar) = a.foo;",
    "select a from Article a where foo(a.uri, a.bar) != a.foo;",
    "select a from Article a where foo(a.uri, a.bar) = foo(a.foo);",
    "select a from Article a where foo(a.uri, a.bar) != foo(a.foo);",

    // where clauses: functions
    "select a from Article a where foo('42');",
    "select a from Article a where foo('42', a.foo);",
    "select a from Article a where foo('42', a.foo, a.bar);",
    "select a from Article a where x:foo('42');",
    "select a from Article a where x:foo(bar('42'));",
    "select a from Article a where x:foo(yb:bar('42', x, x.uri), '42');",
    "select a from Article a where foo(a.uri, a.bar) != foo(a.foo);",

    // where clauses: boolean combinations
    "select a from Article a where foo('42') and a.uri = '12' or a.bar = x:bar('12') and (true() or '12' = a);",
    "select a from Article a where a.uri = '12' or a.bar = x:bar('12') or (true() or '12' = a and (x:bar() or y:foo()));",

    // where clauses: predicate expressions
    "select a from Article a where a.{pred:} = '42';",
    "select a from Article a where a.{pred: pred = 'id'} = '42';",
    "select a from Article a where a.{p: x:foo(p)} = '42';",
    "select a from Article a where a.{p: x:foo(p) = p} = '42';",
    "select a from Article a where a.{p: x:foo(p) = p and p != '21' or (y:bar(blah(p)) and z:baz(p))} = '42';",

    // misc
    "select a.timestamp ts, a.uri, " +
    "  (select p.prefs.pref.value from Preferences p where " +
    "    p.uri = a.uri and p.prefs.pref.name = 'alertsEmailAddress') " +
    "from Alerts a where lessThan(a.timestamp, 'äö£')" +
    " order by ts limit 10 offset 20; "
  };

  private static final String[] parseErrQueries = {
    "select from Article a where a.uri = '42';",
    "select a from a where a.uri = '42';",
    "select a from Article a where a.uri;",
    "select a from Article a where a.uri =;",
    "select a from Article a where a.uri = 42;",
    "select a.*.b from Article a where a.uri = '42';",
    "select a b c from Article a where a.uri = '42';",
    "select a, b from Article a b where a.uri = '42';",
    "select a, b from Article a Foo b where a.uri = '42';",
    "select a, b from Article a, Foo b where a.uri = b.;",
    "select a, b from Article a, Foo b where a.{p} = b.;",
    "select a, b from Article a, Foo b where a.{p: p} = b.;",
    "select a, b from Article a, Foo b where a.{p: p. = '42'} = b.;",
    "select a from Article a where a.uri = '42' order a;",
    "select a from Article a where a.uri = '42' order by 42;",
    "select a from Article a where a.uri = '42' order by a.b;",
    "select a from Article a where a.uri = '42' order by a limit;",
    "select a from Article a where a.uri = '42' order by a limit foo;",
    "select a from Article a where a.uri = '42' order by a limit 4 offset;",
    "select a from Article a where a.uri = '42' order by a limit 4 offset foo;",
    "select a from Article a where a.uri = '42' order by a offset 4 limit 4;",
    "select a from Article a where a.uri = '42' limit 4 order by a offset 4;",
    "select a from Article a where a.uri = '42' limit 4 offset 4 order by a;",
    "",
    ";",
  };

  private Session session = null;

  private Session getSession() throws OtmException {
    if (session == null) {
      SessionFactory factory = new SessionFactory();
      factory.preload(ReplyThread.class);
      factory.preload(PublicAnnotation.class);
      factory.preload(Article.class);

      ModelConfig mc = new ModelConfig("ri", URI.create("local:///topazproject#otmtest1"), null);
      factory.addModel(mc);

      session = factory.openSession();
    }

    return session;
  }

  public void testParser() throws Exception {
    boolean success = true;

    parse(parseOkQueries[0], true);
    long t0 = System.currentTimeMillis();

    for (String qry : parseOkQueries)
      success &= parse(qry, true);
    for (String qry : parseErrQueries)
      success &= parse(qry, false);

    long t1 = System.currentTimeMillis();
    //System.out.println("avg parse time: " +
    //                   (1.0 * (t1-t0) / (parseOkQueries.length + parseErrQueries.length)));

    assertTrue(success);
  }

  private static final String NL = System.getProperty("line.separator");

  private boolean parse(final String qry, final boolean ok) throws Exception {
    final StringBuilder sb = new StringBuilder();

    QueryLexer  lexer  = new QueryLexer(new StringReader(qry));
    QueryParser parser = new QueryParser(lexer);
    parser.query();

    String errs = parser.getErrors(null);
    if (errs.length() > 0 && ok)
      System.out.println("Failed to parse valid query '" + qry + "': " + errs);
    else if (errs.length() == 0 && !ok)
      System.out.println("Parsed invalid query '" + qry + "': " + parser.getAST().toStringTree());

    return (errs.length() > 0 != ok);
  }

  public void testPredTransformer() throws Exception {
    //String qry = "select a.categories.* cat, count(pp.creator) from Article a where a.title = '42' or foobar(a) and pp := a.replies and x:foobar(cast(a.categories, org.topazproject.otm.samples.Reply).type, a.<topaz:hasCategory>, blah(a.replies.creator, pp.creator)) and a.{p: p = a.replies or foo(p) and cast(p, org.topazproject.otm.samples.Reply).title = <foo:bar>} = '42';";
    String qry = "select a.categories.* cat, count(pp.creator) from Article a where a.title = '42' or pp := a.replies and cast(a.categories, Reply).type = pp and a.{p: p = a.replies or cast(p, Reply).title = <foo:bar>} = '42';";
    //String qry = "select a from Article a where a = <42> or a = <52>;";
    //String qry = "select ann n from Annotation ann where cast(ann.annotates, Article).title != 'Yo ho ho' order by n;";
    //String qry = "select art a, count(art.publicAnnotations) from Article art where p := art.publicAnnotations order by a;";

    Session sess = getSession();

    QueryLexer  lexer  = new QueryLexer(new StringReader(qry));
    QueryParser parser = new QueryParser(lexer);
    parser.query();
    printErrorsAndWarnings(parser, "parsing query");

    FieldTranslator ft = new FieldTranslator(sess);
    ft.query(parser.getAST());
    printErrorsAndWarnings(ft, "transforming query");

    ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess);
    cg.query(ft.getAST());
    printErrorsAndWarnings(cg, "transforming translated query");

    ItqlRedux ir = new ItqlRedux();
    ir.query(cg.getAST());
    printErrorsAndWarnings(cg, "reducing query");

    ItqlWriter wr = new ItqlWriter();
    QueryInfo qi = wr.query(ir.getAST());
    printErrorsAndWarnings(wr, "writing query");

    /*
    if (qi != null)
      System.out.println("generated query: '" + qi.getQuery() + "'; types='" + qi.getTypes() +
                         "'; vars='" + qi.getVars() + "'");
    */

    /*
    ASTFrame frame1 = new ASTFrame("Parse Tree", parser.getAST());
    frame1.setVisible(true);

    ASTFrame frame2 = new ASTFrame("Predicates Tree", ft.getAST());
    frame2.setVisible(true);

    ASTFrame frame3 = new ASTFrame("Constraints Tree", cg.getAST());
    frame3.setVisible(true);

    ASTFrame frame4 = new ASTFrame("Redux Tree", ir.getAST());
    frame4.setVisible(true);

    while (frame4.isDisplayable())
      Thread.sleep(100);
    */

    assertTrue(qi != null);
  }

  private static void printErrorsAndWarnings(ErrorCollector ec, String op) {
    String errs = ec.getErrors(null);
    String wrns = ec.getWarnings(null);
    AST ast = (ec instanceof OqlParser) ? ((OqlParser) ec).getAST() : ((OqlTreeParser) ec).getAST();

    if (errs.length() > 0)
      System.out.println("Error " + op + ": '" + errs + "';\n" + ast);
    else
      ; //System.out.println("Success " + op + ": '" + (ast != null ? ast.toStringTree() : "") + "'");

    if (wrns.length() > 0)
      ;//System.out.println("Warnings " + op + ": '" + wrns + "'");

    assertTrue(errs.length() == 0);
  }
}
