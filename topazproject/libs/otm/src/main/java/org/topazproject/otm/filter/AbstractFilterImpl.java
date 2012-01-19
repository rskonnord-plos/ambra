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

import org.topazproject.otm.AbstractParameterizable;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.GenericQueryImpl;

/**
 * This defines the internal filter-impl interface and implements some common code for all filters.
 *
 * @author Ronald Tschalär
 */
public abstract class AbstractFilterImpl extends AbstractParameterizable<Filter> implements Filter {
  protected final FilterDefinition fd;
  protected final Session          sess;

  protected AbstractFilterImpl(FilterDefinition fd, Session sess) throws OtmException {
    this.fd   = fd;
    this.sess = sess;
  }

  public FilterDefinition getFilterDefinition() {
    return fd;
  }

  public String getName() {
    return fd.getFilterName();
  }

  public Set<String> getParameterNames() {
    return fd.getParameterNames();
  }

  /** 
   * Get the filter as a Critieria.
   * 
   * @return the criteria representing this filter
   */
  public abstract Criteria getCriteria() throws OtmException;

  /** 
   * Get the filter as an OQL query.
   * 
   * @return the query representing this filter
   */
  public abstract GenericQueryImpl getQuery() throws OtmException;

  public int hashCode() {
    return getName().hashCode();
  }

  public boolean equals(Object other) {
    if (!(other instanceof Filter))
      return false;
    return getName().equals(((Filter) other).getName());
  }
}
