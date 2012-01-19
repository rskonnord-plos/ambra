/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

/**
 * A type converter to/from triple store value.
 *
 * @author Pradeep Krishnan
  */
public interface Serializer<T> {
  /**
   * Convert to triple store value.
   *
   * @param o the object to serialize
   *
   * @return the triple store value as a String
   *
   * @throws Exception on a conversion error
   */
  public String serialize(T o) throws Exception;

  /**
   * Convert from a triple store value
   *
   * @param o the triple store value as a String
   *
   * @return the java object
   *
   * @throws Exception on a conversion error
   */
  public T deserialize(String o) throws Exception;
}
