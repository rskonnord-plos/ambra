/* $HeadURL::                                                                            $
 * $Var: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
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
package org.topazproject.otm.metadata;

import org.topazproject.otm.FetchType;

/**
 * The definition for an OQL projection var field.
 *
 * @author Pradeep Krishnan
 */
public class VarDefinition extends PropertyDefinition {
  private final String    projectionVar;
  private final String    associatedEntity;
  private final FetchType fetchType;

  /**
   * Creates a new VarDefinition object.
   *
   * @param name   The name of this definition.
   * @param projectionVar The projection variable for views.
   * @param fetchType The fetch type for associations or null.
   * @param associatedEntity The associatedEntity or null
   */
  public VarDefinition(String name, String projectionVar, FetchType fetchType,
                       String associatedEntity) {
    super(name);
    this.projectionVar      = projectionVar;
    this.fetchType          = fetchType;
    this.associatedEntity   = associatedEntity;
  }

  /**
   * Get projectionVar.
   *
   * @return projectionVar as String.
   */
  public String getProjectionVar() {
    return projectionVar;
  }

  /**
   * Get associatedEntity.
   *
   * @return associatedEntity as String.
   */
  public String getAssociatedEntity() {
    return associatedEntity;
  }

  /**
   * Get fetchType.
   *
   * @return fetchType as FetchType.
   */
  public FetchType getFetchType() {
    return fetchType;
  }
}
