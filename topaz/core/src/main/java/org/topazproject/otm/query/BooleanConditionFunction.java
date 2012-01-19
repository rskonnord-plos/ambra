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

/** 
 * Indicates an OQL query function that's valid in the where clause and that returns a boolean.
 * I.e. this function can be used directly as a factor in the where clause expression, but not
 * in a selector.
 *
 * @author Ronald Tschalär
 */
public interface BooleanConditionFunction extends QueryFunction {
  /** 
   * Whether this is a binary comparison function or not. For binary comparison functions the
   * standard type compatibility checking will be performed on the arguments, just as for '='
   * and '!='.
   *
   * @return true if this is a binary comparison function
   */
  boolean isBinaryCompare();
}
