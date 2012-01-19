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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.GenericQueryImpl;

/**
 * This represents a filter-definition that is the result of a logical combination of other
 * filter-definitions.
 *
 * <p>Logical combinations can only be applied on the same filtered-class, i.e. all component
 * filter-definitions must have the same filtered-class as this filter-definition.
 *
 * @author Ronald Tschalär
 */
public abstract class JunctionFilterDefinition extends AbstractFilterDefinition {
  /** the list of filter-definitions in this junction */
  protected final List<FilterDefinition> filterDefList = new ArrayList<FilterDefinition>();

  /** 
   * Create junction-filter-definition.
   * 
   * @param filterName    the name of the filter
   * @param filteredClass the class being filtered; either an alias or a fully-qualified class name
   */
  protected JunctionFilterDefinition(String filterName, String filteredClass) {
    super(filterName, filteredClass);
  }

  public Set<String> getParameterNames() {
    return Collections.EMPTY_SET;
  }

  /** 
   * Add a filter-definition to the list of component filter-definitions. 
   * 
   * @param fd the filter-definition to add
   * @return this
   * @throws OtmException if the filter-definition's filtered-class is not the same as this
   *                      definition's filtered-class
   */
  public JunctionFilterDefinition addFilterDefinition(FilterDefinition fd) throws OtmException {
    if (!fd.getFilteredClass().equals(getFilteredClass()))
      throw new OtmException("added filter-definition's class '" + fd.getFilteredClass() +
                             "' does not match this junction's class '" + getFilteredClass() + "'");
    filterDefList.add(fd);
    return this;
  }

  private static String NL = System.getProperty("line.separator");

  public String toString() {
    StringBuilder sb = new StringBuilder(20 + filterDefList.size() * 50);
    sb.append(getClass().getName().replace("org.topazproject.otm.filter.", "")).append("[");

    for (FilterDefinition fd : filterDefList)
      sb.append(NL).append(fd);

    sb.append("]");
    return sb.toString();
  }

  /** 
   * Common filter implementation for junction filters. The {@link #getCriteria getCriteria()} and
   * {@link @getQuery getQuery()} methods must not be invoked, and throw an
   * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}. Instead, {@link
   * #getFilters getFilters()} may be invoked to get the list component filters.
   */
  public static abstract class JunctionFilter extends AbstractFilterImpl {
    private final List<AbstractFilterImpl> filters = new ArrayList<AbstractFilterImpl>();

    protected JunctionFilter(JunctionFilterDefinition jfd, Session sess) throws OtmException {
      super(jfd, sess);

      for (FilterDefinition fd : jfd.filterDefList)
        filters.add((AbstractFilterImpl) fd.createFilter(sess));
    }

    public Criteria getCriteria() {
      throw new UnsupportedOperationException("not supported on junction filters");
    }

    public GenericQueryImpl getQuery() {
      throw new UnsupportedOperationException("not supported on junction filters");
    }

    /** 
     * Get the list of component filters making up this junction filter.
     * 
     * @return the filters
     */
    public List<AbstractFilterImpl> getFilters() {
      return filters;
    }

    /** 
     * Get the component filters with the given filter name.
     * 
     * @param name the name of the filters to retrieve
     * @return the filters, or the empty list if none found
     */
    public List<AbstractFilterImpl> getFilters(String name) {
      List<AbstractFilterImpl> res = new ArrayList<AbstractFilterImpl>();
      for (AbstractFilterImpl f : filters) {
        if (f.getName().equals(name))
          res.add(f);
      }
      return res;
    }
  }
}
