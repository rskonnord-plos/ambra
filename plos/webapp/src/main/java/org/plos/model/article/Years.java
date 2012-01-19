/* $HeadURL$
 * $Id$ 
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.model.article;

import java.util.TreeMap;



/**
 * An ordered list of years (as year numbers). Each year has a list of months.
 */
public class Years extends TreeMap<Integer, Months> {
  /**
   * @return the list of months (possibly emtpy, but always non-null)
   */
  public Months getMonths(Integer year) {
    Months months = get(year);
    if (months == null)
      put(year, months = new Months());
    return months;
  }
}