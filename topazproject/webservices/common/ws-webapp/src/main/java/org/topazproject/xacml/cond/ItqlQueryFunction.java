/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.StringAnswer;

import org.topazproject.xacml.ws.ServletEndpointContextAttribute;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;

/**
 * A XACML extension function to execute an ITQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public class ItqlQueryFunction extends DBQueryFunction {
  /**
   * The name of this function as it appears in XACML policies.
   */
  public static String           FUNCTION_NAME = FUNCTION_BASE + "itql";
  private static KeyedObjectPool pool;

  static {
    GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
    poolConfig.maxActive                       = 20;
    poolConfig.maxIdle                         = 20;
    poolConfig.maxTotal                        = 20;
    poolConfig.maxWait                         = 1000; // time to wait for handle if none available
    poolConfig.minEvictableIdleTimeMillis      = 5 * 60 * 1000; // 5 min
    poolConfig.minIdle                         = 0;
    poolConfig.numTestsPerEvictionRun          = 5;
    poolConfig.testOnBorrow                    = false;
    poolConfig.testOnReturn                    = false;
    poolConfig.testWhileIdle                   = true;
    poolConfig.timeBetweenEvictionRunsMillis   = 5 * 60 * 1000; // 5 min
    poolConfig.whenExhaustedAction             = GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK;

    pool = new GenericKeyedObjectPool(new ItqlHelperFactory(), poolConfig);
  }

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
    query = bindStatic(query, bindings);

    ItqlHelper itql;

    // Create an ItqlHelper
    try {
      itql = (ItqlHelper) pool.borrowObject(conf);
    } catch (QueryException e) {
      throw e;
    } catch (Exception e) {
      throw new QueryException("Unable to initialize connector to ITQL interpreter bean service "
                               + conf, e);
    }

    StringAnswer answer;

    // Execute the query
    try {
      answer = new StringAnswer(itql.doQuery(query, null));
    } catch (Exception e) {
      throw new QueryException("query '" + query + "' execution failed.", e);
    } finally {
      try {
        pool.returnObject(conf, itql);
      } catch (Throwable t) {
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

    return makeResult((String[]) results.toArray(new String[results.size()]));
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

  private static class ItqlHelperFactory extends BaseKeyedPoolableObjectFactory {
    private Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

    public Object makeObject(Object key) throws Exception {
      Configuration subset = configuration.subset((String) key);

      if ((subset == null) || (subset.getString("uri") == null))
        throw new QueryException("Can't find configuration " + key);

      ProtectedService service;

      try {
        service = ProtectedServiceFactory.createService(subset, (HttpSession) null);
      } catch (Exception e) {
        throw new QueryException("Unable to obtain an itql service configuration instance", e);
      }

      String     serviceUri = service.getServiceUri();

      ItqlHelper itql;

      // Create an ItqlHelper
      try {
        itql = new ItqlHelper(service);
      } catch (Exception e) {
        throw new QueryException("Unable to initialize connector to ITQL interpreter bean service at "
                                 + serviceUri, e);
      }

      return itql;
    }

    public void destroyObject(Object key, Object obj) throws Exception {
      ItqlHelper itql = (ItqlHelper) obj;
      itql.close();
    }
  }
}
