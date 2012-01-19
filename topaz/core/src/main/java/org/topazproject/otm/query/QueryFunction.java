/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
   * Post-predicate processing hook. 
   * 
   * @param pre  the query before the field-to-predicates translation
   * @param post the query after the field-to-predicates translation
   */
  void postPredicatesHook(OqlAST pre, OqlAST post) throws RecognitionException;

  /**   
   * Generate the ITQL statements.
   *  
   * @param args   the arguments to the function; these are ITQL statements
   * @param vars   the variables holding the result of each argument (where the i-th variable
   *               is for the i-th argument)
   * @param resVar the variable into which to put the result; for boolean condition functions
   *               this can be ignored.
   * @param astFactory the ast-factory to use
   * @return the AST to add to the where clause (if the AST is a COUNT or SUBQ then it's addd
   *         to the projection instead)
   * @throws RecognitionException if an error occurred
   */
  OqlAST toItql(List<OqlAST> args, List<OqlAST> vars, OqlAST resVar, ASTFactory astFactory)
      throws RecognitionException;
}
