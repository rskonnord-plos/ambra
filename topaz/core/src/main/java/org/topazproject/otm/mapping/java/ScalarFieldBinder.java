/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm.mapping.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.serializer.Serializer;

/**
 * Mapper for a functional property field.
 *
 * @author Pradeep Krishnan
 */
public class ScalarFieldBinder extends AbstractFieldBinder {
  private static Map<Class, Object> primitiveNulls = new HashMap<Class, Object>();
  static {
    primitiveNulls.put(Byte.TYPE, (byte) 0);
    primitiveNulls.put(Short.TYPE, (short) 0);
    primitiveNulls.put(Integer.TYPE, 0);
    primitiveNulls.put(Long.TYPE, 0L);
    primitiveNulls.put(Float.TYPE, 0.0f);
    primitiveNulls.put(Double.TYPE, 0.0d);
    primitiveNulls.put(Character.TYPE, '\u0000');
    primitiveNulls.put(Boolean.TYPE, false);
  }

  /**
   * Creates a new FunctionalMapper object.
   *
   * @param property the java beans property
   * @param serializer the serializer or null
   * @param streamer the streamer or null
   */
  public ScalarFieldBinder(Property property, Serializer serializer, Streamer streamer) {
    super(property, serializer, streamer);
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

    if (size > 1) // XXX: should be optional
      throw new OtmException("Too many values for '" + getSetter().toGenericString() + "' : "
                             + vals);

    Object value = (size == 0) ? nullValue(getComponentType()) : deserialize(vals.get(0));
    setRawValue(o, value);
  }

  static Object nullValue(Class<?> clazz) throws OtmException {
    if (!clazz.isPrimitive())
      return null;

    return primitiveNulls.get(clazz);
  }
}
