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

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.query.GenericQueryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This represents an OTM filter definition defined via a {@link
 * org.topazproject.otm.criterion.DetachedCriteria DetachedCriteria}.
 *
 * @author Ronald Tschalär
 */
public class CriteriaFilterDefinition extends AbstractFilterDefinition {
  private static final Log log = LogFactory.getLog(CriteriaFilterDefinition.class);

  private final DetachedCriteria crit;

  /** 
   * Create a new filter-definition based on the given criteria.
   * 
   * @param filterName    the name of the filter
   * @param crit          the criteria defining this filter
   */
  public CriteriaFilterDefinition(String filterName, DetachedCriteria crit) {
    super(filterName, crit.getAlias());
    this.crit = crit;
  }

  public Set<String> getParameterNames() {
    return crit.getParameterNames();
  }

  public Filter createFilter(Session sess) throws OtmException {
    return new CriteriaFilter(this, crit, sess);
  }

  public String toString() {
    return "CriteriaFilterDefinition[" + crit.toString("  ") + "]";
  }

  private static class CriteriaFilter extends AbstractFilterImpl {
    private final Criteria crit;

    private CriteriaFilter(FilterDefinition fd, DetachedCriteria crit, Session sess)
                         throws OtmException {
      super(fd, sess);
      this.crit = crit.getExecutableCriteria(sess);
    }

    public Criteria getCriteria() throws OtmException {
      crit.applyParameterValues(paramValues);
      return crit;
    }

    public GenericQueryImpl getQuery() throws OtmException {
      StringBuilder qry = new StringBuilder("select o from ");
      qry.append(crit.getClassMetadata().getName()).append(" o where ");
      toOql(qry, getCriteria(), "o", "v");
      if (qry.substring(qry.length() - 7).equals(" where "))
        qry.setLength(qry.length() - 7);
      qry.append(";");

      GenericQueryImpl q = new GenericQueryImpl(qry.toString(), CriteriaFilterDefinition.log);
      q.prepareQuery(sess.getSessionFactory());
      q.applyParameterValues(paramValues);
      return q;
    }

    private static void toOql(StringBuilder qry, Criteria criteria, String var, String pfx)
                            throws OtmException{
      int idx = 0;
      for (Criterion c : criteria.getCriterionList())
        qry.append('(').append(c.toOql(criteria, var, pfx + idx++)).append(") and ");

      for (Criteria c : criteria.getChildren()) {
        String sbj = pfx + idx++;
        qry.append('(').append(sbj).append(" := ").append(var).append('.').
            append(c.getMapping().getName()).append(" and (");
        toOql(qry, c, sbj, pfx + idx++);
        qry.append(")) and ");
      }

      if (qry.substring(qry.length() - 5).equals(" and "))
        qry.setLength(qry.length() - 5);
    }
  }
}
