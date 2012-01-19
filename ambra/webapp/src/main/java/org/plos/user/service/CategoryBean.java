/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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
