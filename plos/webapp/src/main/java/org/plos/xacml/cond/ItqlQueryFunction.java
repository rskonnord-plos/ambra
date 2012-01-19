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

import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

/**
 * A XACML extension function to execute an ITQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public class ItqlQueryFunction extends OtmQueryFunction {
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

  protected Results executeQuery(Session sess, String query, String[] bindings)
      throws QueryException, OtmException {
    query = bindStatic(query, bindings);
    return sess.doNativeQuery(query);
  }

  private String bindStatic(String query, String[] bindings) throws QueryException {
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
        RdfUtil.validateUri(bindings[i], "xacml query parameter " + i);
      else if (bracket == '\'')
        bindings[i] = RdfUtil.escapeLiteral(bindings[i]);

      s.append(bindings[i]);
    }

    s.append(parts[i]);

    return s.toString();
  }
}
