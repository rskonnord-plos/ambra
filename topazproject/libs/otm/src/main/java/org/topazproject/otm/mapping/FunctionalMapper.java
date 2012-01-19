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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * Mapper for a functional property field.
 *
 * @author Pradeep Krishnan
 */
public class FunctionalMapper extends AbstractMapper {
  /**
   * Creates a new FunctionalMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param dataType of literals or null for un-typed
   * @param rdfType of associations or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model the model where this field is persisted
   * @param mapperType the mapper type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   */
  public FunctionalMapper(String uri, Field field, Method getter, Method setter,
                          Serializer serializer, String dataType, String rdfType, boolean inverse,
                          String model, MapperType mapperType, boolean entityOwned,
                          IdentifierGenerator generator) {
    super(uri, field, getter, setter, serializer, field.getType(), dataType, rdfType, inverse,
          model, mapperType, entityOwned, generator);
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
      throw new OtmException("Too many values for '" + getField().toGenericString() + "'");

    Object value = (size == 0) ? null : deserialize(vals.get(0));
    setRawValue(o, value);
  }
}
