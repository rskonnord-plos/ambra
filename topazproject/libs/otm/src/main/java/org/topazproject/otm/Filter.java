/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import org.topazproject.otm.filter.FilterDefinition;

/**
 * An OTM filter.
 *
 * @author Ronald Tschalär
 */
public interface Filter extends Parameterizable<Filter> {
  /**
   * Get the underlying filter definition.
   *
   * @return the filter definition
   */
  FilterDefinition getFilterDefinition();

  /** 
   * Get the name of this filter.
   * 
   * @return the name of with this filter
   */
  String getName();
}
