/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

import org.topazproject.otm.Connection;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.stores.ItqlStore.ItqlStoreConnection;

import org.apache.struts2.ServletActionContext;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.cond.EvaluationResult;

/**
 * A XACML extension function to execute an ITQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public class ItqlQueryFunction extends DBQueryFunction {
  private static final Log log = LogFactory.getLog(ItqlQueryFunction.class);

  /**
   * The name of this function as it appears in XACML policies.
   */
  public static String FUNCTION_NAME = FUNCTION_BASE + "itql";

  /**
   * Creates an ItqlQueryFunction instance.
   */
  public ItqlQueryFunction() {
    super(FUNCTION_NAME);
  }

  /**
   * Creates an ItqlQueryFunction instance.
   *
   * @param returnType the return type of this function
   */
  public ItqlQueryFunction(URI returnType) {
    super(FUNCTION_NAME, returnType);
  }

  /**
   * Executes an ITQL query.
   *
   * @param context the xacml evaluation context
   * @param conf the configuration ID for the ITQL interpreter bean service
   * @param query ITQL query with '?' used for parameter placeholders
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
    query                      = bindStatic(query, bindings);

    StringAnswer answer;
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

      Connection isc = txn.getConnection();

      if (!(isc instanceof ItqlStoreConnection))
        throw new QueryException("Expecting an instance of " + ItqlStoreConnection.class);

      ItqlHelper itql = ((ItqlStoreConnection) isc).getItqlHelper();

      if (itql == null)
        throw new QueryException("Failed to get an instance of " + ItqlHelper.class);

      answer = new StringAnswer(itql.doQuery(query, null));

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

    // Convert the results to a single column
    List     answers = answer.getAnswers();
    Iterator it      = answers.iterator();
    List     results = new ArrayList();

    for (int i = 0; it.hasNext(); i++) {
      Object o = it.next();

      if (!(o instanceof StringAnswer.StringQueryAnswer))
        continue;

      StringAnswer.StringQueryAnswer result  = (StringAnswer.StringQueryAnswer) o;
      List                           rows    = result.getRows();
      String[]                       columns = result.getVariables();

      if (columns.length != 1)
        throw new QueryException("query '" + query + "' execution returned " + columns.length
                                 + " columns. Expects only 1 column in query results.");

      for (Iterator i2 = rows.iterator(); i2.hasNext();) {
        String[] row = (String[]) i2.next();
        results.add(row[0]);
      }
    }

    return makeResult(results);
  }

  private String bindStatic(String query, String[] bindings)
                     throws QueryException {
    String[] parts = query.split("\\?");

    if ((parts.length - 1) != bindings.length)
      throw new QueryException("query '" + query + "' requires " + (parts.length - 1)
                               + " parameters; got " + bindings.length + " parameters instead.");

    if (parts.length == 1)
      return parts[0];

    StringBuffer s = new StringBuffer(512);

    int          i;

    for (i = 0; i < bindings.length; i++) {
      s.append(parts[i]);

      char bracket = s.charAt(s.length() - 1);

      if (bracket == '<')
        ItqlHelper.validateUri(bindings[i], "xacml query parameter " + i);
      else if (bracket == '\'')
        bindings[i] = ItqlHelper.escapeLiteral(bindings[i]);

      s.append(bindings[i]);
    }

    s.append(parts[i]);

    return s.toString();
  }
}
