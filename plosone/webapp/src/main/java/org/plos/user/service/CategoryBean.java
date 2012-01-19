/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.service;

/**
 * A comfort class to help render the alerts page. It provide the presentable for a alert category,
 * whether it is available for a weekly/monthly alert
 */
public class CategoryBean {
  private final String key;
  private final String name;
  private final boolean weeklyAvailable;
  private final boolean monthlyAvailable;

  public CategoryBean(final String key, final String name, final boolean weeklyAvailable, final boolean monthlyAvailable) {
    this.key = key;
    this.name = name;
    this.weeklyAvailable = weeklyAvailable;
    this.monthlyAvailable = monthlyAvailable;
  }

  /**
   * Getter for property 'name'.
   * @return Value for property 'name'.
   */
  public String getName() {
    return name;
  }

  /**
   * Getter for property 'key'.
   * @return Value for property 'key'.
   */
  public String getKey() {
    return key;
  }

  /**
   * Getter for property 'monthlyAvailable'.
   * @return Value for property 'monthlyAvailable'.
   */
  public boolean isMonthlyAvailable() {
    return monthlyAvailable;
  }

  /**
   * Getter for property 'weeklyAvailable'.
   * @return Value for property 'weeklyAvailable'.
   */
  public boolean isWeeklyAvailable() {
    return weeklyAvailable;
  }
}
