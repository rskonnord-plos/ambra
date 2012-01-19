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
   * Get the list of projection elements for this function. If this is null a normal projection
   * element is generated, i.e. a generated variable; if non null these nodes are used instead.
   *
   * @return the list of projection elements, or null
   */
  OqlAST[] getItqlProjections();

  /**
   * Initialize the variables. The list may be modified, and indeed if {@link #getItqlProjections
   * getItqlProjections()} returned anything other than null or a single-element array then this
   * must adjust the number of variables.
   *
   * @param vars the current list of variables
   * @param col  the current column
   * @return the new list of variables
   */
  List<String> initVars(List<String> vars, int col) throws OtmException;

  /**
   * Initialize the types. The list may be modified, and indeed if {@link #getItqlProjections
   * getItqlProjections()} returned anything other than null or a single-element array then this
   * must adjust the number of types.
   *
   * @param types the current list of types
   * @param col   the current column
   * @return the new list of types
   */
  List<Object> initTypes(List<Object> types, int col) throws OtmException;

  Answer initItqlResult(Answer qa, int col) throws OtmException;

  Object getItqlResult(Answer qa, int row, int col, Results.Type type, boolean eager)
    throws OtmException, AnswerException;
}
