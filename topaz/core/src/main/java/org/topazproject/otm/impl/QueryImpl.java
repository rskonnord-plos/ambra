/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.Session.FlushMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.TripleStore;


/** 
 * This represents an OQL query. Instances are obtained via {@link Session#createQuery
 * Session.createQuery()}.
 * 
 * @author Ronald Tschalär
 */
class QueryImpl extends Query {
  private static final Log log = LogFactory.getLog(Query.class);

  private final Session            sess;
  private final GenericQueryImpl   query;
  private final Collection<Filter> filters;
  private final Set<String>        paramNames;

  /** 
   * Create a new query instance. 
   * 
   * @param sess    the session this is attached to
   * @param query   the oql query string
   * @param filters the filters that should be applied to this query
   */
  QueryImpl(Session sess, String query, Collection<Filter> filters) throws OtmException {
    this.sess    = sess;
    this.filters = filters;

    this.query = new GenericQueryImpl(query, log);
    this.query.prepareQuery(sess.getSessionFactory());
    this.paramNames = Collections.unmodifiableSet(this.query.getParameterNames());
  }

  /** 
   * Execute this query. 
   * 
   * @return the query results
   * @throws OtmException
   */
  public Results execute() throws OtmException {
    if (sess.getTransaction() == null)
      throw new OtmException("No transaction active");

    if (sess.getFlushMode().implies(FlushMode.always))
      sess.flush(); // so that mods are visible to queries

    TripleStore store = sess.getSessionFactory().getTripleStore();

    query.applyParameterValues(paramValues, sess.getSessionFactory());
    return store.doQuery(query, filters, ((SessionImpl) sess).getTripleStoreCon());
  }

  public Set<String> getParameterNames() {
    return paramNames;
  }
}
