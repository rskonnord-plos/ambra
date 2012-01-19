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

/** 
 * Indicates an OQL query function that's valid in the where clause and that performs some function
 * on the arguments other than a boolean comparison. I.e. this function cannot be used directly as
 * a factor in the where clause expression, but instead may be used in a selector.
 *
 * @author Ronald Tschalär
 */
public interface FunctionalConditionFunction extends QueryFunction {
}
