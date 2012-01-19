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

import java.util.Collections;
import java.util.Set;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.GenericQueryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This represents an OTM filter definition defined via an OQL query.
 *
 * @author Ronald Tschalär
 */
public class OqlFilterDefinition extends AbstractFilterDefinition {
  private static final Log log = LogFactory.getLog(OqlFilterDefinition.class);

  private final GenericQueryImpl query;

  /** 
   * Create a new filter-definition based on the given OQL query fragment. The query fragment is
   * the <var>where</var> clause of an OQL query preceeded by the variable representing the
   * filtered class instances. E.g. the query fragment
   * <pre>
   *   a where a.title = 'foo'
   * </pre>
   * together with a <var>filteredClass</var> of <var>Article</var> would create a filter that only
   * accepts articles with the title 'foo'.
   * 
   * @param filterName    the name of the filter
   * @param filteredClass the entity-name or fully-qualified class name of the class being filtered
   * @param query         the query fragment
   */
  public OqlFilterDefinition(String filterName, String filteredClass, String query)
      throws OtmException {
    super(filterName, filteredClass);

    query = query.trim();
    String var = query.substring(0, query.indexOf(' '));
    String q   = "select " + var + " from " + filteredClass + " " + query + ";";
    this.query = new GenericQueryImpl(q, log);
  }

  public Set<String> getParameterNames() {
    return Collections.unmodifiableSet(query.getParameterNames());
  }

  public Filter createFilter(Session sess) throws OtmException {
    return new OqlFilter(this, query, sess);
  }

  public String toString() {
    return "OqlFilterDefinition[" + query + "]";
  }

  private static class OqlFilter extends AbstractFilterImpl {
    private final GenericQueryImpl query;

    private OqlFilter(FilterDefinition fd, GenericQueryImpl query, Session sess)
        throws OtmException {
      super(fd, sess);

      this.query = query.clone();
      this.query.prepareQuery(sess.getSessionFactory());
    }

    public Criteria getCriteria() throws OtmException {
      Criteria cr = query.toCriteria(sess);
      cr.applyParameterValues(paramValues);
      return cr;
    }

    public GenericQueryImpl getQuery() throws OtmException {
      query.applyParameterValues(paramValues);
      return query;
    }
  }
}
