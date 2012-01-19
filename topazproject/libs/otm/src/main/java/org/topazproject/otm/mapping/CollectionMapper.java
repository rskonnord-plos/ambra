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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;

/**
 * Mapper for {@link java.util.Collection collection} fields.
 *
 * @author Pradeep Krishnan
 */
public class CollectionMapper extends AbstractMapper {
/**
   * Creates a new CollectionMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the collection component type
   * @param dataType of literals or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param inverseModel the model where this field is persisted if different from class model
   */
  public CollectionMapper(String uri, Field field, Method getter, Method setter,
                          Serializer serializer, Class componentType, String dataType,
                          boolean inverse, String inverseModel) {
    super(uri, field, getter, setter, serializer, componentType, dataType, inverse, inverseModel);
  }

  /**
   * Retrieve elements from a collection field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException {
    Collection value = (Collection) getRawValue(o, false);

    if (value == null)
      return Collections.emptyList();

    ArrayList res = new ArrayList(value.size());

    for (Object v : value)
      if (v != null)
        res.add(serialize(v));

    return res;
  }

  /**
   * Populate a collection field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Collection value  = (Collection) getRawValue(o, false);

    boolean    create = (value == null);

    if (create)
      value = newInstance();

    value.clear();

    for (Object v : vals)
      value.add(deserialize(v));

    if (create)
      setRawValue(o, value);
  }

  private Collection newInstance() throws OtmException {
    try {
      // xxx: handle interfaces and abstract collections
      return (Collection) getType().newInstance();
    } catch (Exception e) {
      throw new OtmException("Can't instantiate " + getType(), e);
    }
  }
}
