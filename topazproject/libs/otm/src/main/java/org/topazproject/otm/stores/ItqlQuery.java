/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.ItqlConstraintGenerator;
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
  private final GenericQueryImpl   query;
  private final Collection<Filter> filters;
  private final Session            sess;

  /** 
   * Create a new itql-query instance. 
   */
  public ItqlQuery(GenericQueryImpl query, Collection<Filter> filters, Session sess) {
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
      ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess, "oqltmp2_", true, fInfos);
      curParser = cg;
      cg.query(query.getResolvedQuery());
      checkMessages(cg.getErrors(), cg.getWarnings(), query);

      ItqlRedux ir = new ItqlRedux();
      curParser = ir;
      ir.query(cg.getAST());
      checkMessages(ir.getErrors(), ir.getWarnings(), query);

      ItqlWriter wr = new ItqlWriter();
      curParser = wr;
      QueryInfo qi = wr.query(ir.getAST());
      checkMessages(wr.getErrors(), wr.getWarnings(), query);

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
      filters.add(parseItqlFilter(cf, pfx + "j" + idx++));

    return new ItqlFilter(jf, filters);
  }

  private ItqlFilter parsePlainItqlFilter(AbstractFilterImpl f, String pfx) throws OtmException {
    GenericQueryImpl fqry = f.getQuery();

    if (log.isDebugEnabled())
      log.debug("parsing filter '" + fqry + "'");

    ItqlConstraintGenerator cg = new ItqlConstraintGenerator(sess, pfx, false, null);
    try {
      cg.query(fqry.getResolvedQuery());
      checkMessages(cg.getErrors(), cg.getWarnings(), fqry);
      return new ItqlFilter(f, cg.getAST());
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      if (cg != null && cg.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + cg.getErrors(null), e);
        throw new QueryException("error parsing query '" + query + "'", cg.getErrors());
      } else
        throw new QueryException("error parsing query '" + query + "'", e);
    }
  }
}
