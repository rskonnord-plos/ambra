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

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.query.GenericQuery;
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

    public GenericQuery getQuery() throws OtmException {
      StringBuilder qry = new StringBuilder("select o from ");
      qry.append(crit.getClassMetadata().getName()).append(" o");

      StringBuilder where = new StringBuilder(200);

      toOql(where, qry, getCriteria(), "o", "v");

      if (where.length() > 0)
        qry.append(" where ").append(where);
      qry.append(";");

      GenericQueryImpl q = new GenericQueryImpl(qry.toString(), CriteriaFilterDefinition.log);
      q.prepareQuery(sess.getSessionFactory());
      q.applyParameterValues(paramValues, sess.getSessionFactory());
      return q;
    }

    private static void toOql(StringBuilder where, StringBuilder qry, Criteria criteria, String var,
                              String pfx) throws OtmException{
      int idx = 0;
      for (Criterion c : criteria.getCriterionList())
        where.append('(').append(c.toOql(criteria, var, pfx + idx++)).append(") and ");

      for (Criteria c : criteria.getChildren()) {
        String sbj   = pfx + idx++;
        String field = c.getMapping().getName();
        String lhs, top, type, op;

        if (c.isReferrer()) {
          lhs  = var;
          top  = sbj;
          type = criteria.getClassMetadata().getName();
          op   = "=";

          qry.append(", ").append(c.getClassMetadata().getName()).append(" ").append(sbj);
        } else {
          lhs  = sbj;
          top  = var;
          type = c.getClassMetadata().getName();
          op   = ":=";
        }

        where.append('(').append(lhs).append(" ").append(op).append(" cast(").
              append(top).append('.').append(field).append(", ").append(type).append(") and (");
        toOql(where, qry, c, sbj, pfx + idx++);
        where.append(")) and ");
      }

      if (where.length() >= 5 && where.substring(where.length() - 5).equals(" and "))
        where.setLength(where.length() - 5);
    }
  }
}
