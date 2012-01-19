/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.filter;

import java.util.Set;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;

/**
 * This holds definition of an OTM filter. Filter definitions are registered with the
 * SessionFactory, and the filters can then be enabled and disabled on a per Session basis.
 *
 * @author Ronald Tschalär
 */
public interface FilterDefinition {
  /** 
   * Get the name of the filter. Filters are referred to by name when enabling and disabling them.
   * 
   * @return the name associated with the filter
   */
  String getFilterName();

  /** 
   * Get the class this filter is applied to. This may be either the entity name associated with
   * the class or the fully qualified class name - see also {@link
   * org.topazproject.otm.SessionFactory#getClassMetadata(java.lang.String)
   * SessionFactory.getClassMetadata()}.
   * 
   * @return the class name
   */
  String getFilteredClass();

  /**
   * Get the list of parameter names in the filter.
   *
   * @return the parameter names; will be an empty list if the filter has no parameters.
   */
  Set<String> getParameterNames();

  /** 
   * Create a filter from this definition. For use by {@link Session Session} only. 
   * 
   * @param sess  the session the filter is attached to
   * @return the new filter
   * @throws OtmException if an error occurred creating the filter
   */
  Filter createFilter(Session sess) throws OtmException;
}
