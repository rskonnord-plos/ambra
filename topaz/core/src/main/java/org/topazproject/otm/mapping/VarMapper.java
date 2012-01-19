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
package org.topazproject.otm.mapping;

import org.topazproject.otm.FetchType;

/**
 * Mapper for a property that is a projection in a view.
 *
 * @author Pradeep Krishnan
 */
public interface VarMapper extends Mapper {
  /**
   * Gets the projection variable. All fields in a view must have a projection-variable which
   * specifies which element in projection list to tie this field to.
   *
   * @return the projection variable
   */
  public String getProjectionVar();

  /**
   * Checks if the type is an association and not a serialized literal/URI.
   *
   * @return true if this field is an association
   */
  public boolean isAssociation();

  /**
   * Get the fetch options for this field. Only applicable for associations.
   *
   * @return the FetchType option
   */
  public FetchType getFetchType();

  /**
   * For associations, the name of the associated entity.
   *
   * @return the name of the associated entity or null if this is not an association mapping
   */
  public String getAssociatedEntity();
}
