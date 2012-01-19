/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

/**
 * Specification of an order-by on a Criteria.
 *
 * @author Pradeep Krishnan
 */
public class Order {
  private String  name;
  private boolean ascending;

/**
   * Creates a new Order object.
   *
   * @param name the field name to order by
   * @param ascending ascending/descending order
   */
  public Order(String name, boolean ascending) {
    this.name        = name;
    this.ascending   = ascending;
  }

  /**
   * Gets the name of the field to order by.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Tests if ascending order.
   *
   * @return ascending or descending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * Creates a new ascending order object.
   *
   * @param name the field name to order by
   *
   * @return the newly created Order object
   */
  public static Order asc(String name) {
    return new Order(name, true);
  }

  /**
   * Creates a new descending order object.
   *
   * @param name the field name to order by
   *
   * @return the newly created Order object
   */
  public static Order desc(String name) {
    return new Order(name, false);
  }
}
