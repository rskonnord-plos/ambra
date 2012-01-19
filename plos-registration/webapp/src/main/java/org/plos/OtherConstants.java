/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos;

import java.util.Map;

/**
 * A place to be able to access all constants which don't belong anywhere else.
 */
public class OtherConstants {
  private Map map;

  /**
   * Getter for map.
   * @param key key
   * @return Value for map.
   */
  public Object getValue(final String key) {
    return map.get(key);
  }

  /**
   * Setter for property map.
   * @param map Value to map.
   */
  public void setAllConstants(final Map map) {
    this.map = map;
  }
}
