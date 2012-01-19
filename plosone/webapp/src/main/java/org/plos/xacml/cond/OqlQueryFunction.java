/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.cond.EvaluationResult;

/**
 * A XACML extension function to execute an OQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 * The query parameters must be named ':p1', ':p2', etc and will be bound to 1st, 2nd, etc bind
 * value.
 *
 * @author Pradeep Krishnan
 * @author Ronald Tschalär
 */
public class OqlQueryFunction extends DBQueryFunction {
  private static final Log log = LogFactory.getLog(OqlQueryFunction.class);

  /**
   * The name of this function as it appears in XACML policies.
   */
  public static String FUNCTION_NAME = FUNCTION_BASE + "oql";

  /**
   * Creates an OqlQueryFunction instance.
   */
  public OqlQueryFunction() {
    super(FUNCTION_NAME);
  }

  /**
   * Creates an OqlQueryFunction instance.
   *
   * @param returnType the return type of this function
   */
  public OqlQueryFunction(URI returnType) {
    super(FUNCTION_NAME, returnType);
  }

  /**
   * Executes an OQL query.
   *
   * @param context the xacml evaluation context
   * @param conf the configuration ID for the OQL interpreter bean service
   * @param query OQL query with '?' used for parameter placeholders
   * @param bindings the values that are to be bound to query parameters.
   *
   * @return Returns query results as an array of Strings
   *
   * @throws QueryException when there is an error in executing the query
   */
  public EvaluationResult executeQuery(EvaluationCtx context, String conf, String query,
                                       String[] bindings)
                                throws QueryException {
    // Create the query
    List<String> results       = new ArrayList<String>();
    Transaction  txn           = null;
    boolean      wasActive     = false;

    // Create an ItqlHelper
    try {
      Session s = (Session) WebApplicationContextUtils
        .getRequiredWebApplicationContext(ServletActionContext.getServletContext())
        .getBean("otmSession");

      txn         = s.getTransaction();
      wasActive   = (txn != null);

      if (txn == null)
        txn = s.beginTransaction();

      if (txn == null)
        throw new QueryException("Failed to start an otm transaction");

      Query q = s.createQuery(query);
      if (q.getParameterNames().size() != bindings.length)
        throw new QueryException("query '" + query + "' requires " + q.getParameterNames().size() +
                                 " parameters; got " + bindings.length + " parameters instead.");
      for (int idx = 0; idx < bindings.length; idx++)
        q.setParameter("p" + (idx + 1), bindings[idx]);

      Results answer = q.execute();
      if (answer.getWarnings() != null)
        log.error("query '" + query + "' return warnings: " + Arrays.asList(answer.getWarnings()));

      if (answer.getVariables().length != 1)
        throw new QueryException("query '" + query + "' execution returned " +
                                 answer.getVariables().length +
                                 " columns. Expects only 1 column in query results.");

      while (answer.next())
        results.add(answer.getString(0));

      if (!wasActive)
        txn.commit();

      txn = null;
    } catch (Exception e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    } finally {
      try {
        if (!wasActive && (txn != null))
          txn.rollback();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("rollback failed", t);
      }
    }

    return makeResult(results);
  }
}
