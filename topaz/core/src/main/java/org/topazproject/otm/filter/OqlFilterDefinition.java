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

import java.util.Collections;
import java.util.Set;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.query.GenericQuery;
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
   * Create a new filter-definition based on the given OQL query. The query must conform to the
   * following restrictions:
   * <ul>
   *   <li>The select clause may have only one entry.</li>
   *   <li>That entry (projection expression) may only consist of a variable; in particular it may
   *       not contain a constant, a function, a derefence, a cast, or a wildcard projector.</li>
   *   <li>In the where clause, dereferences may not contain a cast, an immediate url
   *       (x.&lt;foo:bar&gt;), or a predicate expression.</li>
   *   <li>One side of equality comparisons ('=', '!=', 'lt()', 'le()', etc) must be a constant</li>
   *   <li>If there are multiple constraints on an item (as represented by a variable) which are
   *       'and'd or 'or'd together, then all of these that contain a dereference of the variable
   *       which is an association must be 'and'd together and 'and'd with the rest of the
   *       constraints on that item. I.e.
   *       <pre>
   *       ... $v.name = 'john' and $v.address.zip = '42' ...
   *       ... $v.address.city = 'london' and $v.address.zip = '42' ...
   *       ... ($v.name = 'john' or lt($v.age, 65)) and $v.address.zip = '42' ...
   *       </pre>
   *       are legal, but these are not:
   *       <pre>
   *       ... $v.name = 'john' or $v.address.zip = '42' ...
   *       ... $v.address.city = 'london' or $v.address.zip = '42' ...
   *       </pre>
   *   </li>
   * </ul>
   *
   * <p>Example: the query
   * <pre>
   *   select a from Article a where a.title = 'foo';
   * </pre>
   * together with a <var>filteredClass</var> of <var>Article</var> would create a filter that only
   * accepts articles with the title 'foo'.
   *
   * @param filterName    the name of the filter
   * @param filteredClass the entity-name or fully-qualified class name of the class being filtered;
   *                      this must match the type of the projection expression.
   * @param query         the query
   */
  public OqlFilterDefinition(String filterName, String filteredClass, String query)
      throws OtmException {
    super(filterName, filteredClass);

    this.query = new GenericQueryImpl(query.trim(), log);
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
    }

    public Criteria getCriteria() throws OtmException {
      Criteria cr = query.toCriteria(sess);
      cr.applyParameterValues(paramValues);
      return cr;
    }

    public GenericQuery getQuery() throws OtmException {
      query.prepareQuery(sess.getSessionFactory());
      query.applyParameterValues(paramValues, sess.getSessionFactory());
      return query;
    }
  }
}
