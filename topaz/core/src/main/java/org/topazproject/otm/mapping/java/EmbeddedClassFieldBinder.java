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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.EmbeddedBinder;

/**
 * FieldBinder for an embedded class field. The {@link #get} and {@link #set} methods are 
 * meaningless and hence this loader is used only by its field loaders to get/set the 
 * parent object's field.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedClassFieldBinder extends AbstractFieldBinder implements EmbeddedBinder {
  /**
   * Creates a new EmbeddedClassFieldBinder object.
   *
   * @param field the field that is embedding this class
   * @param getter the get method or null for this embedded class field
   * @param setter the set method or null for this embedded classs field
   */
  public EmbeddedClassFieldBinder(Field field, Method getter, Method setter) {
    super(field, getter, setter, null, null);
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

  /*
   * inherited javadoc
   */
  public void load(Object root, Object instance, List<String> values, 
      Map<String, Set<String>> types, Mapper mapper, 
      Session session) throws OtmException {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }

  /*
   * inherited javadoc
   */
  public Binder promote(Binder binder) {
    return new EmbeddedClassMemberFieldBinder(this, (FieldBinder)binder);
  }

}
