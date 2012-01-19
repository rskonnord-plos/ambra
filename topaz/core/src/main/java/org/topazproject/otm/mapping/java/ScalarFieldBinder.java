/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.serializer.Serializer;

/**
 * Mapper for a functional property field.
 *
 * @author Pradeep Krishnan
 */
public class ScalarFieldBinder extends AbstractFieldBinder {
  /**
   * Creates a new FunctionalMapper object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   */
  public ScalarFieldBinder(Field field, Method getter, Method setter,
                          Serializer serializer) {
    super(field, getter, setter, serializer, field.getType());
  }

  /**
   * Get the value of a field of an object.
   *
   * @param o the object
   *
   * @return a singelton or empty list (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException {
    Object value = getRawValue(o, false);

    return (value == null) ? Collections.emptyList() : Collections.singletonList(serialize(value));
  }

  /**
   * Set the value of a field of an object.
   *
   * @param o the object
   * @param vals a singelton or empty list (may be deserialized)
   *
   * @throws OtmException if too many values to set or the value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException {
    int size = vals.size();

    if (size > 1) // xxx: should be optional
      throw new OtmException("Too many values for '" + getField().toGenericString() 
          + "' : " + vals);

    Object value = (size == 0) ? null : deserialize(vals.get(0));
    setRawValue(o, value);
  }
}
