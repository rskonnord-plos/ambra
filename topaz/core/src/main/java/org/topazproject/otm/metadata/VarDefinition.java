/* $HeadURL::                                                                            $
 * $Var: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
