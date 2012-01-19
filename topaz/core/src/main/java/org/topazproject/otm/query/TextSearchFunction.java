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

package org.topazproject.otm.query;

import java.net.URI;
import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.collections.AST;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * This provides an OQL text search function. Usage:
 *
 * <pre>  select a from ... where ... search(a.body, 'genes cells') ...</pre>
 * or
 * <pre>  select a, score from ... where ... search(a.body, 'genes cells', score) ...</pre>
 *
 * <p>Note: because of the way text-search indexes are stored in mulgara using the lucene-resolver,
 * the following two selects have slightly different behaviour:
 *
 * <pre>  select a from ... where ... search(a.body, 'genes cells') ...</pre>
 * and
 * <pre>  select a from ... where ... b := a.body and search(b, 'genes cells') ...</pre>
 *
 * The second query assumes (and generates a constraint for it) that there is a triple in the
 * triple-store corresponding to a.body, whereas the first query does not. This is important for
 * blobs since there is no triple corresponding to the blob field in the triple-store.
 *
 * @author Ronald Tschalär
 */
public class TextSearchFunction
    implements BooleanConditionFunction, ConstraintsTokenTypes, TransformListener  {
  public  static final String FUNC_NAME = "search";

  private static final URI LUCENE_GRAPH_TYPE = URI.create("http://mulgara.org/mulgara#LuceneModel");

  /** the current session-factory */
  protected final SessionFactory sf;
  /** the first argument to search */
  protected       OqlAST         arg0;
  /** the predicate */
  protected       OqlAST         predicate;
  /** the variable before the predicate */
  protected       OqlAST         var;

  /**
   * Create a new text-search function instance.
   *
   * @param args  the arguments to the function
   * @param types the argument types
   * @param sf    the current session-factory
   * @throws RecognitionException if the number of arguments is not 2 or if the argument's types
   *                              are not identical
   */
  protected TextSearchFunction(List<OqlAST> args, List<ExprType> types, SessionFactory sf)
      throws RecognitionException {
    this.sf = sf;

    // check number of arguments
    if (args.size() < 2 || args.size() > 3)
      throw new RecognitionException("search() must have 2 or 3 arguments, but found " +
                                     args.size());

    // check argument types
    ExprType atype = types.get(0);
    if (atype != null &&
        atype.getType() != ExprType.Type.TYPED_LIT && atype.getType() != ExprType.Type.UNTYPED_LIT)
      throw new RecognitionException("search(): argument 1 must be a literal, but is of type " +
                                     atype);

    ExprType stype = types.get(1);
    if (stype == null ||
        stype.getType() != ExprType.Type.TYPED_LIT && stype.getType() != ExprType.Type.UNTYPED_LIT)
      throw new RecognitionException("search(): argument 2 must be a literal, but is " + stype);

    ExprType ctype = (args.size() > 2) ? types.get(2) : null;
    if (ctype != null &&
        ctype.getType() != ExprType.Type.TYPED_LIT && ctype.getType() != ExprType.Type.UNTYPED_LIT)
      throw new RecognitionException("search(): argument 3 must be a variable of unknown or " +
                                     "literal type, but is of type " + ctype);

    // save the first argument and check it's a REF
    arg0 = args.get(0);
    if (arg0.getType() != REF)
      throw new RecognitionException("search(): argument 1 must be a reference, but is of type " +
                                     arg0.getType());

    /* remove the last dereference on arg0 (if there is one) so the corresponding triples constraint
     * is not generated. This is necessary because the constraint is generated as part of the search
     * constraints, and because for things like blob's there is no corresponding triple (meaning the
     * query would always fail).
     */
    AST deref = arg0.getFirstChild();
    AST prev  = null;

    while (deref.getNextSibling() != null) {
      while (deref.getNextSibling() != null) {
        prev = deref;
        deref = deref.getNextSibling();
      }

      if (deref.getType() == REF) {
        deref = deref.getNextSibling().getFirstChild();
        continue;
      }
    }

    if (prev != null) {
      predicate = (OqlAST) prev.getNextSibling();
      prev.setNextSibling(null);
    }
  }

  public String getName() {
    return FUNC_NAME;
  }

  public ExprType getReturnType() {
    return null;
  }

  public boolean isBinaryCompare() {
    return false;
  }

  public ExprType getOutputVarType(int arg) throws RecognitionException {
    if (arg < 2)
      throw new RecognitionException("Argument " + arg + " to search() is not an output");
    return ExprType.literalType(Rdf.xsd + "double", null);
  }

  public void postPredicatesHook(OqlAST pre, OqlAST post) throws RecognitionException {
    if (predicate != null)
      return;

    assert post.getType() == SELECT;

    OqlAST f = (OqlAST) post.getFirstChild();
    OqlAST w = (OqlAST) f.getNextSibling();

    assert f.getType() == FROM;
    assert w.getType() == WHERE;

    predicate = ASTUtil.getLastDeref((OqlAST) arg0.getFirstChild(), (OqlAST) w.getFirstChild());
    if (predicate != null)
      predicate.addListener(this);
    else
      throw new RecognitionException("search(): argument 1 must be a field value of some class, " +
                                     "it doesn't appear to be");
  }

  /**
   * Remember the variable node before the predicate.
   */
  public void deref(OqlAST reg, OqlAST[] nodes) {
    var = nodes[0];
  }

  public OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory af,
                       String locVarPfx)
      throws RecognitionException {
    OqlAST larg = args.get(0);
    OqlAST rarg = args.get(1);
    OqlAST carg = (args.size() > 2) ? args.get(2) : null;

    OqlAST lvar = vars.get(0);
    OqlAST rvar = vars.get(1);
    OqlAST cvar = (vars.size() > 2) ? vars.get(2) : null;

    // create expression
    SearchableDefinition sd      = getSearchDef(predicate, sf);
    String               graph   = ASTUtil.getGraphUri(sd.getIndex(), sf);
    String               predUri = "<" + sd.getUri() + ">";

    OqlAST srch = ASTUtil.makeID(locVarPfx + "s", af);
    srch.setIsVar(true);

    if (var == null)
      var = lvar;

    OqlAST res = ASTUtil.makeTree(af, AND, "and", af.dupTree(larg), af.dupTree(rarg),
                                  ASTUtil.makeTriple(var, "<mulgara:search>", srch, graph, af),
                                  ASTUtil.makeTriple(srch, predUri, rvar, graph, af));
    if (cvar != null) {
        res.addChild(af.dupTree(carg));
        res.addChild(ASTUtil.makeTriple(srch, "<mulgara:score>", cvar, graph, af));
    }

    return res;
  }

  private static SearchableDefinition getSearchDef(OqlAST pred, SessionFactory sf)
      throws RecognitionException {
    SearchableDefinition def = SearchableDefinition.findForProp(sf, pred.getPropName());
    if (def == null)
      throw new RecognitionException("'" + pred.getPropName() + "' is not searchable");

    return def;
  }
}
