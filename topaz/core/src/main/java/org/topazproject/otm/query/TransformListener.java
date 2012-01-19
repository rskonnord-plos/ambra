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

import antlr.RecognitionException;

/**
 * A listener for OQL transform events.
 *
 * @author Ronald Tschalär
 */
public interface TransformListener {
  /**
   * This callback is invoked once on each dereference it was registered on while converting the
   * generic query AST to a query-language specific query (i.e. after field-to-predicate translation
   * and parameter replacement has occurred). E.g. in the expression "a.b.c" this callback is
   * invoked twice, first for the ".b" and then for the ".c" dereference (assuming the listener was
   * registered for both "b" and "c").
   *
   * @param reg   the dereferencing predicate (i.e. the predicate for the ".b" or the ".c")
   * @param nodes a list of nodes representing the state at the time of this dereference
   * @throws RecognitionException if an error occurs
   */
  void deref(OqlAST reg, OqlAST[] nodes) throws RecognitionException;
}
