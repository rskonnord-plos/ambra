/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.topazproject.ambra.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.support.WebApplicationContextUtils;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

import org.apache.struts2.ServletActionContext;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.cond.EvaluationResult;

/**
 * A XACML extension function to execute an OTM query. The OTM session is retrieved from the spring
 * bean factory, which in turn is retrieved from the current servlet context.
 *
 * @author Ronald Tschalär
 */
public abstract class OtmQueryFunction extends DBQueryFunction {
  private static final Logger log = LoggerFactory.getLogger(OtmQueryFunction.class);

  /**
   * Creates an OtmQueryFunction instance.
   *
   * @param functionName The function name as it appears in XACML policies
   */
  protected OtmQueryFunction(String functionName) {
    super(functionName);
  }

  /**
   * Creates an OtmQueryFunction instance.
   *
   * @param functionName The function name as it appears in XACML policies
   * @param returnType The return type for this function
   */
  protected OtmQueryFunction(String functionName, URI returnType) {
    super(functionName, returnType);
  }

  protected EvaluationResult executeQuery(EvaluationCtx context, String conf, final String query,
                                          final String[] bindings)
                                throws QueryException {
    try {
      Session s = (Session) WebApplicationContextUtils
        .getRequiredWebApplicationContext(ServletActionContext.getServletContext())
        .getBean("otmSession");

      Results answer = executeQuery(s, query, bindings);

      if (answer.getWarnings() != null)
        log.error("query '" + query + "' return warnings: " +
                  Arrays.asList(answer.getWarnings()));

      if (answer.getVariables().length != 1)
        throw new QueryException("query '" + query + "' execution returned " +
                                 answer.getVariables().length +
                                 " columns. Expects only 1 column in query results.");

      List<String> results = new ArrayList<String>();
      while (answer.next())
        results.add(answer.getString(0));

      return makeResult(results);
    } catch (OtmException oe) {
      throw new QueryException("query '" + query + "' execution failed.", oe);
    }
  }

  /**
   * Execute the query. The query currently is supposed to return only a single column of results.
   *
   * @param sess     the OTM session to use
   * @param query    the query string
   * @param bindings an array of values that may be bound to the query before executing it.
   * @return the Results
   * @throws OtmException 
   * @throws QueryException 
   */
  protected abstract Results executeQuery(Session sess, String query, String[] bindings)
      throws QueryException, OtmException;
}
