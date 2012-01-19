/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

/** 
 * This holds the results of parsing an OQL query. 
 * 
 * @author Ronald Tschalär
 */
public class QueryInfo {
  private final String        query;
  private final List<Object>  pTypes;
  private final List<String>  pVars;

  /** 
   * Create a new query-info instance. 
   * 
   * @param query  the query string
   * @param pTypes a 
   * @param pVars 
   */
  public QueryInfo(String query, List<Object> pTypes, List<String> pVars) {
    if (pTypes.size() != pVars.size())
      throw new IllegalArgumentException("number of types doesn't match number of variables: " +
                                         pTypes.size() + " != " + pVars.size());

    this.query  = query;
    this.pTypes = pTypes;
    this.pVars  = pVars;
  }

  /** 
   * Get the built query string.
   * 
   * @return the query string
   */
  public String getQuery() {
    return query;
  }

  /** 
   * Get the list of types of the projection expressions, in the order they will be
   * returned in the query results. Each element will either be a Class or a QueryInfo
   * in the case of a subquery.
   * 
   * @return the projection types
   */
  public List<Object> getTypes() {
    return pTypes;
  }

  /** 
   * Get the list of variables of the projections, in the same order and position as
   * the classes from {@link #getClasses getClasses}; in places where the query did
   * not specify a variable the element will be null.
   * 
   * @return the projection variables
   */
  public List<String> getVars() {
    return pVars;
  }
}
