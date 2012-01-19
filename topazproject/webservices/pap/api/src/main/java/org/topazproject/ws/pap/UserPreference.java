/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

/** 
 * This defines a single user preference.
 *
 * <p>This class is a bean.
 * 
 * @author Ronald Tschalär
 */
public class UserPreference {
  /** The name of the preference. */
  private String name;
  /** The values of the preference. */
  private String[] values;

  /**
   * Get the name of the preference.
   *
   * @return the preference name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the preference.
   *
   * @param name the preference name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the values of the preference.
   *
   * @return the preference values; may be null. Note that the order of the entries will be
   *         arbitrary.
   */
  public String[] getValues() {
    return values;
  }

  /**
   * Set the values of the preference.
   *
   * @param values the preference values; may be null. Note that the order will not be preserved.
   */
  public void setValues(String[] values) {
    this.values = values;
  }
}
