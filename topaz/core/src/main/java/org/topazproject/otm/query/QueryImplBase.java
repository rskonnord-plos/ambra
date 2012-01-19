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

import java.util.ArrayList;
import java.util.List;

/** 
 * Common code for query handlers.
 * 
 * @author Ronald Tschalär
 */
public abstract class QueryImplBase {
  protected List<String> warnings = new ArrayList<String>();

  /** 
   * Check the errors and warnings. If there were any errors, generate an exception; else if there
   * were any warnings add them to the list.
   * 
   * @param errors   the list of parse errors
   * @param warnings the list of parse warnings
   * @param query    the query that was being parsed
   * @throws QueryException if <var>errors</var> is non empty
   */
  protected void checkMessages(List<String> errors, List<String> warnings, Object query)
      throws QueryException {
    if (errors != null && errors.size() > 0)
      throw new QueryException("Error parsing query '" + query + "'", errors);
    else if (warnings != null)
      this.warnings.addAll(warnings);
  }

  /** 
   * Get all warnings emitted while parsing query. 
   * 
   * @return the warnings; may be empty
   */
  public List<String> getWarnings() {
    return warnings;
  }

  /** 
   * Clear the current list of warnings.
   */
  public void clearWarnings() {
    warnings.clear();
  }

  @Override
  protected QueryImplBase clone() throws CloneNotSupportedException {
    QueryImplBase c = (QueryImplBase) super.clone();
    c.warnings = new ArrayList<String>(warnings);
    return c;
  }
}
