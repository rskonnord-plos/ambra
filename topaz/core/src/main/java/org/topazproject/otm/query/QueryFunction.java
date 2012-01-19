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

import antlr.RecognitionException;
import antlr.ASTFactory;

/**
 * Implement an OQL query function. This interface must never be implemented directly, but
 * instead one (or more) of the sub-interfaces must be implemented.
 *
 * @author Ronald Tschalär
 */
public interface QueryFunction {
  /**
   * Get the function's name.
   *
   * @return the function's name
   */
  String getName();

  /**
   * Get the return type of the function. This is ignored for boolean condition functions.
   *
   * @return the return type, or null if unknown
   */
  ExprType getReturnType();

  /**
   * Post-predicate processing hook. This is invoked after all field-to-predicate translation has
   * occurred, but before the parameter replacement has taken place.
   *
   * @param pre  the query before the field-to-predicates translation
   * @param post the query after the field-to-predicates translation
   */
  void postPredicatesHook(OqlAST pre, OqlAST post) throws RecognitionException;

  /**
   * Generate the ITQL statements for this function.
   *
   * @param args   the arguments to the function; these are ITQL statements
   * @param vars   the variables holding the result of each argument (where the i-th variable
   *               is for the i-th argument)
   * @param resVar the variable into which to put the function result; for boolean condition
   *               functions this can be ignored.
   * @param astFactory the ast-factory to use
   * @param locVarPfx the prefix to use to generate local variables
   * @return the AST to add to the where clause (if the AST is a COUNT or SUBQ then it is added
   *         to the projection instead)
   * @throws RecognitionException if an error occurred
   */
  OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory astFactory,
                String locVarPfx)
      throws RecognitionException;
}
