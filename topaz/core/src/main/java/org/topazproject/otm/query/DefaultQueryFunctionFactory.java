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
      CountFunction.FUNC_NAME, IndexFunction.FUNC_NAME, "gt", "ge", "lt", "le" };

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
    return new BinaryCompare(name, args, types, sf);
  }
}
