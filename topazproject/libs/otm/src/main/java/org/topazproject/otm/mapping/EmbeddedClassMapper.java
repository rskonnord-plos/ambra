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

/**
 * Mapper for an embedded class field. It is only a "half" mapper in the sense, there aren't
 * any corresponding rdf triples. So the {@link #get} and {@link #set} methods are meaningless and
 * hence this mapper is used only by its field mappers to get/set the parent object's field.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedClassMapper extends AbstractMapper {
  /**
   * Creates a new EmbeddedClassMapper object.
   *
   * @param field the field that is embedding this class
   * @param getter the get method or null for this embedded class field
   * @param setter the set method or null for this embedded classs field
   */
  public EmbeddedClassMapper(Field field, Method getter, Method setter) {
    super(null, field, getter, setter, null, null, null, null, false, null, null, true, null);
  }

  /**
   * Unsupported operation.
   *
   * @param o not used
   *
   * @return none
   *
   * @throws UnsupportedOperationException
   */
  public List get(Object o) {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }

  /**
   * Unsupported operation.
   *
   * @param o not used
   * @param vals not used
   *
   * @throws UnsupportedOperationException
   */
  public void set(Object o, List vals) {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }
}
