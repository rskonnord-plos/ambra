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

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.serializer.Serializer;

/**
 * FieldBinder for array type fileds.
 *
 * @author Pradeep Krishnan
 */
public class ArrayFieldBinder extends AbstractFieldBinder {
  /**
   * Creates a new ArrayFieldBinder object.
   *
   * @param property the java beans property
   * @param serializer the serializer or null
   * @param streamer the streamer or null
   */
  public ArrayFieldBinder(Property property, Serializer serializer, Streamer streamer) {
    super(property, serializer, streamer);
  }

  /**
   * Retrieve elements from an array property of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if the property's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException {
    Object value = getRawValue(o, false);

    if (value == null)
      return Collections.emptyList();

    int  len = Array.getLength(value);
    List res = new ArrayList(len);

    for (int i = 0; i < len; i++) {
      Object v = Array.get(value, i);

      if (v != null)
        res.add(serialize(v));
    }

    return res;
  }

  /**
   * Populate an array property of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a properties's value cannot be deserilized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Object value;

    if (vals.size() == 0)
      value = null; // XXX: this should be an option
    else {
      value = Array.newInstance(getComponentType(), vals.size());

      int i = 0;

      for (Object val : vals)
        Array.set(value, i++, deserialize(val));
    }

    setRawValue(o, value);
  }
}
