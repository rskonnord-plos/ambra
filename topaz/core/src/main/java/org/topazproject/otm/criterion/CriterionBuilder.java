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
package org.topazproject.otm.criterion;

import org.topazproject.otm.OtmException;

/**
 * An interface for a builder/factory of Criterions.
 *
 * @author Pradeep Krishnan
 * @see org.topazproject.otm.TripleStore#setCriterionBuilder
  */
public interface CriterionBuilder {
  /**
   * Creates a Criterion based on a function name
   *
   * @param func the store specific function
   *
   * @return the newly created Criterion
   *
   * @throws OtmException if an error occurred
   */
  public Criterion create(String func, Object... args)
                   throws OtmException;
}
