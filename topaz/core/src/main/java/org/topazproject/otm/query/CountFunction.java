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

import java.util.List;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.collections.AST;

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.otm.Rdf;

/**
 * @author Ronald Tschalär
 */
class CountFunction implements ProjectionFunction, ConstraintsTokenTypes {
  public  static final String   FUNC_NAME = "count";
  private static final ExprType RET_TYPE  = ExprType.literalType(Rdf.xsd + "double", null);

  CountFunction(List<OqlAST> args, List<ExprType> types) throws RecognitionException {
    if (args.size() != 1)
      throw new RecognitionException("count() must have exactly 1 argument, but found " +
                                     args.size());
  }

  public String getName() {
    return FUNC_NAME;
  }

  public ExprType getReturnType() {
    return RET_TYPE;
  }

  public void postPredicatesHook(OqlAST pre, OqlAST post) {
  }

  public OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory af,
                       String locVarPfx)
      throws RecognitionException {
    OqlAST arg = args.get(0);
    OqlAST var = vars.get(0);

    resVar.setExprType(RET_TYPE);

    // handle count(<constant>)
    if (arg.getType() == TRIPLE)
      return (OqlAST) af.create(QSTRING, "'1'");

    assert arg.getType() == AND;

    // create subquery
    AST pexpr = af.dupTree(arg);

    AST from  = ASTUtil.makeTree(af, FROM, "from", af.create(ID, "dummy"), af.create(ID, "dummy"));
    AST where = ASTUtil.makeTree(af, WHERE, "where", pexpr);
    AST proj  = ASTUtil.makeTree(af, PROJ, "projection", af.dup(var), af.dup(var));

    AST subq = ASTUtil.makeTree(af, SELECT, "select", from, where, proj);
    return ASTUtil.makeTree(af, COUNT, "count", subq);
  }

  public OqlAST[] getItqlProjections() {
    return null;
  }

  public List<String> initVars(List<String> vars, int col) {
    return vars;
  }

  public List<Object> initTypes(List<Object> types, int col) {
    return types;
  }

  public Answer initItqlResult(Answer qa, int col) {
    return qa;
  }

  public Object getItqlResult(Answer qa, int row, int col, Results.Type type, boolean eager) {
    return null;
  }
}
