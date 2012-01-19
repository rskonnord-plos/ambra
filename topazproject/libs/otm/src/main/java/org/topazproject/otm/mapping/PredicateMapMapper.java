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

import java.util.Collections;
import java.util.List;

import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.OtmException;

/**
 * Mapper for a mapper that allows a dynamic/run-time map of properties to values..
 *
 * @author Pradeep Krishnan
 */
public class PredicateMapMapper extends AbstractMapper {
  /**
   * Creates a new PredicateMapMapper object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   */
  public PredicateMapMapper(Field field, Method getter, Method setter) {
    super(null, field, getter, setter, null, null, null, null, false, null, 
          Mapper.MapperType.PREDICATE_MAP, true, null);
  }

  /**
   * Returns the value as 'raw' map.
   *
   * @param o the object
   *
   * @return the 'raw' value in a list
   *
   * @throws OtmException if a field's value cannot be retrieved
   */
  public List get(Object o) throws OtmException {
    Object value = getRawValue(o, false);

    return (value == null) ? Collections.emptyList() : Collections.singletonList(value);
  }

  /**
   * Sets the value from a 'raw' map.
   *
   * @param o the object
   * @param vals the values to be set 
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void set(Object o, List vals) throws OtmException {
    int size = vals.size();

    if (size > 1)
      throw new OtmException("Too many values for '" + getField().toGenericString() + "'");

    Object value = (size == 0) ? null : vals.get(0);
    setRawValue(o, value);
  }

  public IdentifierGenerator getGenerator() {
    throw new UnsupportedOperationException("Generated values not supported for PredicateMaps");
  }
}
