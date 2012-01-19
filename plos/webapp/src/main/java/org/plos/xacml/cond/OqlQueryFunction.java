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

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

/**
 * A XACML extension function to execute an OQL query. The arguments to the function are a
 * configuration identifier for the database, the query string, and followed by any bind values.
 * The query parameters must be named ':p1', ':p2', etc and will be bound to 1st, 2nd, etc bind
 * value.
 *
 * @author Pradeep Krishnan
 * @author Ronald Tschalär
 */
public class OqlQueryFunction extends OtmQueryFunction {
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

  protected Results executeQuery(Session sess, String query, String[] bindings)
      throws QueryException, OtmException {
    Query q = sess.createQuery(query);

    if (q.getParameterNames().size() != bindings.length)
      throw new QueryException("query '" + query + "' requires " + q.getParameterNames().size() +
                               " parameters; got " + bindings.length + " parameters instead.");
    for (int idx = 0; idx < bindings.length; idx++)
      q.setParameter("p" + (idx + 1), bindings[idx]);

    return q.execute();
  }
}
