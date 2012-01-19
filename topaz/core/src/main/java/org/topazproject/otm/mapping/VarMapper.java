/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
