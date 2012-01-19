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

import java.util.List;

/** 
 * This holds the results of parsing an OQL query. 
 * 
 * @author Ronald Tschalär
 */
public class QueryInfo {
  private final String                   query;
  private final List<Object>             pTypes;
  private final List<String>             pVars;
  private final List<ProjectionFunction> pFuncs;

  /** 
   * Create a new query-info instance. 
   * 
   * @param query  the query string
   * @param pTypes the types for each projection element
   * @param pVars  the variables in each projection element
   * @param pFuncs the functions for each projection element
   */
  public QueryInfo(String query, List<Object> pTypes, List<String> pVars,
                   List<ProjectionFunction> pFuncs) {
    if (pTypes.size() != pVars.size())
      throw new IllegalArgumentException("number of types doesn't match number of variables: " +
                                         pTypes.size() + " != " + pVars.size());

    this.query  = query;
    this.pTypes = pTypes;
    this.pVars  = pVars;
    this.pFuncs = pFuncs;
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
   * Get the list of variables of the projection, in the same order and position as
   * the types from {@link #getTypes getTypes}; in places where the query did not
   * specify a variable the element will be null.
   * 
   * @return the projection variables
   */
  public List<String> getVars() {
    return pVars;
  }

  /** 
   * Get the list of functions in the projection, in the same order and position as
   * the types from {@link #getTypes getTypes}; in places where the query did not
   * specify a function the element will be null.
   * 
   * @return the projection functions
   */
  public List<ProjectionFunction> getFuncs() {
    return pFuncs;
  }
}
