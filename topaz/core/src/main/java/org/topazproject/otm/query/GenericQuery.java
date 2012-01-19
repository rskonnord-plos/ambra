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

import antlr.collections.AST;

/**
 * This represents the result of the generic part of the query processing, i.e. the result of the
 * parsing and transforms that are not specific to the target query language.
 *
 * @author Ronald Tschalär
 */
public interface GenericQuery {
  /**
   * Get the list warnings emitted while parsing query.
   *
   * @return the list of warnings; may be be empty
   */
  public List<String> getWarnings();

  /**
   * Get the AST of the query with all parameters resolved.
   *
   * @return the query AST
   */
  public AST getResolvedQuery();
}
