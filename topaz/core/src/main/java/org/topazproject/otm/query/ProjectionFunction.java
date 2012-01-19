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

import org.topazproject.mulgara.itql.Answer;
import org.topazproject.mulgara.itql.AnswerException;
import org.topazproject.otm.OtmException;

/**
 * Indicates an OQL query function that's valid in a projection expression.
 *
 * @author Ronald Tschalär
 */
public interface ProjectionFunction extends QueryFunction {
  /**
   * Get the list of projection elements in the native query for this function. If this is null a
   * normal projection element is generated, i.e. a generated variable; if non null these nodes are
   * used instead.
   *
   * @return the list of projection elements, or null
   */
  OqlAST[] getItqlProjections();

  /**
   * Initialize the result variables. This can be used to adjust the list of variables (columns)
   * returned to the application when it differs from the number of variables in the native query;
   * and indeed, if {@link #getItqlProjections} returned anything other than null or a
   * single-element array then this must adjust (i.e. reduce) the number of variables so that there
   * is only one for this function. This is invoked while initializing the OQL results object to be
   * returned to the application, and it is invoked once for each variable of this function in the
   * native query (i.e. each element in the list returned by {@link #getItqlProjections}.
   *
   * <p>Example: if {@link #getItqlProjections} returned three elements, then this callback will
   * be invoked three times with col=n, col=n+1, and col=n+2, respectively; the list of variables
   * returned by the last of these three invocations must be 2 elements shorter than the original
   * list passed to the first invocation.
   *
   * @param vars the current list of variables
   * @param col  the current column:
   * @return the new list of variables
   * @throws OtmException if an error occurs
   */
  List<String> initVars(List<String> vars, int col) throws OtmException;

  /**
   * Initialize the types of the result variables. This is invoked analogously to {@link #initVars}
   * and this callback must adjust the list of types to match that of the list of variables - see
   * the discussion in {@link #initVars} for details.
   *
   * @param types the current list of types
   * @param col   the current column
   * @return the new list of types
   * @throws OtmException if an error occurs
   */
  List<Object> initTypes(List<Object> types, int col) throws OtmException;

  /**
   * Initialize the tql answer object. This allows the function to do any processing on the answer
   * before it is returned to the application. Like {@link #initVars} it is invoked once for each
   * column belonging to this function in the native answer. And like {@link #initVars} it must
   * adjust the columns appropriately if this function has 0 or more than 1 columns in the native
   * query; in this case it must effectively return a new Answer object, usually wrapping the
   * original answer object and mapping the columns appropriately.
   *
   * @param qa  the current answer
   * @param col the current column
   * @return the same answer, or a new one; both cases the function should ensure the answer has
   *         been rewound to the beginning
   * @throws OtmException if an error occurs
   */
  Answer initItqlResult(Answer qa, int col) throws OtmException;

  /**
   * Get the result object for a single column in the answer. This is only invoked for the column
   * belonging to this function.
   *
   * @param qa    the underlying native query answer
   * @param row   the current row number
   * @param col   the OQL result column for which to get the result object
   * @param type  the expected type of the result object
   * @param eager whether to eager or lazy load the result object; this only makes
   *              sense for class objects
   * @return the result object for the specified row and column
   * @throws OtmException if an error occurs
   * @throws AnswerException if an error occurs
   */
  Object getItqlResult(Answer qa, int row, int col, Results.Type type, boolean eager)
    throws OtmException, AnswerException;
}
