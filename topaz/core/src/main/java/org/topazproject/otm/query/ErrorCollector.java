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
 * Common interface to retrieve error and warning messages.
 * 
 * @author Ronald Tschalär
 */
public interface ErrorCollector {
  /** the system's <var>line.separator</var> property */
  public static final String NL = System.getProperty("line.separator");

  /** 
   * Get the list of errors generated.
   * 
   * @return the list of errors, or an empty list if there were none.
   */
  public List<String> getErrors();

  /** 
   * Get the list of warnings generated.
   * 
   * @return the list of warnings, or an empty list if there were none.
   */
  public List<String> getWarnings();

  /** 
   * Get the errors generated as a single string.
   * 
   * @param join the string used to join the individual errors; if null, a {@link #NL newline} is
   *             used
   * @return the list of errors as a single string
   */
  public String getErrors(String join);

  /** 
   * Get the warnings generated as a single string.
   * 
   * @param join the string used to join the individual warnings; if null, a {@link #NL newline} is
   *             used
   * @return the list of warnings as a single string
   */
  public String getWarnings(String join);
}
