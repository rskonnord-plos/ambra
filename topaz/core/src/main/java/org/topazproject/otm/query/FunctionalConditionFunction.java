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
 * Indicates an OQL query function that's valid in the where clause and that performs some function
 * on the arguments other than a boolean comparison. I.e. this function cannot be used directly as
 * a factor in the where clause expression, but instead may be used in a selector.
 *
 * @author Ronald Tschalär
 */
public interface FunctionalConditionFunction extends QueryFunction {
}
