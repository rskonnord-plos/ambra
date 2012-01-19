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

import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.EntityBinder.LazyLoaded;
import org.topazproject.otm.mapping.EmbeddedBinder;
import org.topazproject.otm.mapping.Mapper;

/**
 * FieldBinder for an embedded class field. The {@link #get} and {@link #set} methods are
 * meaningless and hence this loader is used only by its field loaders to get/set the  parent
 * object's field.
 *
 * @author Pradeep Krishnan
 */
public class EmbeddedClassFieldBinder extends AbstractFieldBinder implements EmbeddedBinder {
  private final ClassMetadata ecm;
  private final EntityBinder eb;
  /**
   * Creates a new EmbeddedClassFieldBinder object.
   *
   * @param property the java bean property
   * @param ecm the embedded class metadata
   */
  public EmbeddedClassFieldBinder(Property property, ClassMetadata ecm) {
    super(property, null, null);
    this.ecm = ecm;
    this.eb = ecm.getEntityBinder(EntityMode.POJO);
  }

  public Object getRawValue(Object o, boolean create) throws OtmException {
    if (!(o instanceof LazyLoaded))
      return super.getRawValue(o, create);

    Object value;

    try {
      value = getGetter().invoke(o);

      if (create && !(value instanceof LazyLoaded)) {
        LazyLoaded ll = (LazyLoaded) o;

        // Share the same lazy loader as the embedding class.
        // This means the stashed area is all shared.
        // We just have a proxy that can intercept method calls.
        // The stashing will be done by embedding class.
        value = eb.newLazyLoadedInstance(ll.getLazyLoader(ll));
        setRawValue(o, value);
      }
    } catch (Exception e) {
      throw new OtmException("Failed to get a value from '" + getGetter().toGenericString() + "'", e);
    }

    return value;
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
                   Mapper mapper, Session session)
            throws OtmException {
    throw new UnsupportedOperationException("Only raw get/set allowed");
  }

  /*
   * inherited javadoc
   */
  public PropertyBinder promote(PropertyBinder propertyBinder) {
    return new EmbeddedClassMemberFieldBinder(this, (FieldBinder) propertyBinder);
  }
}
