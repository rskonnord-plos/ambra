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

/**
 * When de-serializing an Integer that is stored as some non-integer, be sure to remove the
 * decimal point.
 *
 * @author Eric Brown
 *
 * @param <T> the java type of the class
 */
public class IntegerSerializer<T> extends SimpleSerializer<T> {
  /**
   * Creates a new IntegerSerializer object.
   *
   * @param clazz the class to serialize/deserialize
   */
  public IntegerSerializer(Class<T> clazz) {
    super(clazz);
  }

  /*
   * inherited javadoc
   */
  public T deserialize(String o, Class<T> c) throws Exception {
    int decimal = o.indexOf(".");

    if (decimal != -1)
      o = o.substring(0, decimal); // TODO: Round-off properly?

    return super.deserialize(o, c);
  }
}
