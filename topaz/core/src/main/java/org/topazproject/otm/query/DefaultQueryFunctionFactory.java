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
import org.topazproject.otm.SessionFactory;

/**
 * The default query function factory. This currently provides the following functions:
 * <var>count</var>, <var>index</var>, <var>gt</var>, <var>ge</var>, <var>lt</var>, and
 * <var>le</var>.
 *
 * @author Ronald Tschalär
 */
public class DefaultQueryFunctionFactory implements QueryFunctionFactory {
  private static final String[] FUNC_NAMES = new String[] {
      CountFunction.FUNC_NAME, IndexFunction.FUNC_NAME, TextSearchFunction.FUNC_NAME,
      "gt", "ge", "lt", "le"
  };

  public String[] getNames() {
    return FUNC_NAMES;
  }

  public QueryFunction createFunction(String name, List<OqlAST> args, List<ExprType> types,
                                      SessionFactory sf)
      throws RecognitionException {
    if (name.equals(CountFunction.FUNC_NAME))
      return new CountFunction(args, types);
    if (name.equals(IndexFunction.FUNC_NAME))
      return new IndexFunction(args, types);
    if (name.equals(TextSearchFunction.FUNC_NAME))
      return new TextSearchFunction(args, types, sf);
    return new BinaryCompare(name, args, types, sf);
  }
}
