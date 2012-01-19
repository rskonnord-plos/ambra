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

package org.topazproject.otm.stores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Filter;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.filter.JunctionFilterDefinition;
import org.topazproject.otm.filter.AbstractFilterImpl;
import org.topazproject.otm.query.ErrorCollector;
import org.topazproject.otm.query.GenericQuery;
import org.topazproject.otm.query.ItqlConstraintGenerator;
import org.topazproject.otm.query.ItqlFilterApplicator;
import org.topazproject.otm.query.ItqlRedux;
import org.topazproject.otm.query.ItqlWriter;
import org.topazproject.otm.query.QueryException;
import org.topazproject.otm.query.QueryImplBase;
import org.topazproject.otm.query.QueryInfo;

/**
 * Common code for query handlers.
 *
 * @author Ronald Tschalär
 */
class ItqlQuery extends QueryImplBase {
  private static final Log log = LogFactory.getLog(ItqlQuery.class);
  private final GenericQuery       query;
  private final Collection<Filter> filters;
  private final Session            sess;

  /**
   * Create a new itql-query instance.
   */
  public ItqlQuery(GenericQuery query, Collection<Filter> filters, Session sess) {
    this.query   = query;
    this.filters = filters;
    this.sess    = sess;

    warnings.addAll(query.getWarnings());
  }

  public QueryInfo parseItqlQuery() throws OtmException {
    Collection<ItqlFilter> fInfos = new ArrayList<ItqlFilter>();
    int idx = 1;
    for (Filter f : filters)
      fInfos.add(parseItqlFilter((AbstractFilterImpl) f, "oqltmpf" + idx++ + "_"));

    if (log.isDebugEnabled())
      log.debug("parsing query '" + query + "'");

    ErrorCollector curParser = null;

    try {
      ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess, "oqltmp2_", true);
      curParser = cg;
      cg.query(query.getResolvedQuery());
      checkMessages(cg, query);

      ItqlRedux ir = new ItqlRedux(sess);
      curParser = ir;
      ir.query(cg.getAST());
      checkMessages(ir, query);

      ItqlFilterApplicator fa = new ItqlFilterApplicator(sess, "oqltmp3_", fInfos);
      curParser = fa;
      fa.query(ir.getAST());
      checkMessages(fa, query);

      ItqlWriter wr = new ItqlWriter();
      curParser = wr;
      QueryInfo qi = wr.query(fa.getAST());
      checkMessages(wr, query);

      if (log.isDebugEnabled())
        log.debug("parsed query '" + query + "': itql='" + qi.getQuery() + "', vars='" +
                  qi.getVars() + "', types='" + qi.getTypes() + "'");

      return qi;
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      if (curParser != null && curParser.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + curParser.getErrors(null), e);
        throw new QueryException("error parsing query '" + query + "'", curParser.getErrors());
      } else
        throw new QueryException("error parsing query '" + query + "'", e);
    }
  }

  private ItqlFilter parseItqlFilter(AbstractFilterImpl f, String pfx) throws OtmException {
    if (!(f instanceof JunctionFilterDefinition.JunctionFilter))
      return parsePlainItqlFilter(f, pfx);

    JunctionFilterDefinition.JunctionFilter jf = (JunctionFilterDefinition.JunctionFilter) f;
    Set<ItqlFilter> filters = new HashSet<ItqlFilter>();
    int idx = 0;
    for (AbstractFilterImpl cf : jf.getFilters())
      filters.add(parseItqlFilter(cf, pfx + "j" + idx++ + "_"));

    return new ItqlFilter(jf, filters);
  }

  private ItqlFilter parsePlainItqlFilter(AbstractFilterImpl f, String pfx) throws OtmException {
    GenericQuery fqry = f.getQuery();

    if (log.isDebugEnabled())
      log.debug("parsing filter '" + fqry + "'");

    ErrorCollector curParser = null;
    try {
      ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess, pfx, false);
      curParser = cg;
      cg.query(fqry.getResolvedQuery());
      checkMessages(cg, fqry);

      ItqlRedux ir = new ItqlRedux(sess);
      curParser = ir;
      ir.query(cg.getAST());
      checkMessages(ir, query);

      if (log.isDebugEnabled())
        log.debug("parsed filter '" + fqry + "': ast='" + ir.getAST().toStringTree() + "'");

      return new ItqlFilter(f, ir.getAST());
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      if (curParser != null && curParser.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + curParser.getErrors(null), e);
        throw new QueryException("error parsing query '" + query + "'", curParser.getErrors());
      } else
        throw new QueryException("error parsing query '" + query + "'", e);
    }
  }
}
