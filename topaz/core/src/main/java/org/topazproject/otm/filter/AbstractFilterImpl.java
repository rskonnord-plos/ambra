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
