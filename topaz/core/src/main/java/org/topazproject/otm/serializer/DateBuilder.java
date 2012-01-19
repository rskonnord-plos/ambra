/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.serializer;

import java.util.Date;

/**
 * A type converter to/from a Date data type.
 *
 * @author Pradeep Krishnan
 */
public interface DateBuilder<T> {
  /**
   * Convert the given type to a Date object.
   *
   * @param o the object to convert
   *
   * @return the converted date object
   */
  public Date toDate(T o);

  /**
   * Convert a Date object to the given type.
   *
   * @param d the date object
   *
   * @return the converted type
   */
  public T fromDate(Date d);
}
