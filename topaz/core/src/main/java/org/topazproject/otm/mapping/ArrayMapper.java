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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * Mapper for array type fileds.
 *
 * @author Pradeep Krishnan
 */
public class ArrayMapper extends AbstractMapper {
  /**
   * Creates a new ArrayMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the array component type
   * @param dataType of literals or null for un-typed
   * @param rdfType of associations or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model the model where this field is persisted
   * @param mapperType the mapper type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   * @param cascade cascade options for this field
   */
  public ArrayMapper(String uri, Field field, Method getter, Method setter, Serializer serializer,
                     Class componentType, String dataType, String rdfType, boolean inverse,
                     String model, MapperType mapperType, boolean entityOwned,
                     IdentifierGenerator generator, CascadeType [] cascade) {
    super(uri, field, getter, setter, serializer, componentType, dataType, rdfType, inverse, model,
          mapperType, entityOwned, generator, cascade);
  }

  /**
   * Retrieve elements from an array field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if the field's value cannot be retrieved and serialized
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
   * Populate an array field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be deserilized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Object value;

    if (vals.size() == 0)
      value = null; // xxx: this should be an option
    else {
      value = Array.newInstance(getComponentType(), vals.size());

      int i = 0;

      for (Object val : vals)
        Array.set(value, i++, deserialize(val));
    }

    setRawValue(o, value);
  }
}
