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
 * An ordered list of months (as month numbers, from 1 to 12). Each month has a list of days.
 */
public class Months extends TreeMap<Integer, Days> {
  /**
   * @return the list of days (possibly emtpy, but always non-null)
   */
  public Days getDays(Integer mon) {
    Days days = get(mon);
    if (days == null)
      put(mon, days = new Days());
    return days;
  }
}