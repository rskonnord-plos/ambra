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

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.EntityBinder.LazyLoaded;
import org.topazproject.otm.mapping.EntityBinder.LazyLoader;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractFieldBinder implements FieldBinder {
  private final Serializer serializer;
  private final Streamer   streamer;
  private final Property   property;

  /**
   * Creates a new AbstractFieldBinder object for a regular class.
   *
   * @param property the java beans property
   * @param serializer the serializer or null
   * @param streamer the streamer or null
   */
  public AbstractFieldBinder(Property property, Serializer serializer, Streamer streamer) {
    this.property     = property;
    this.serializer   = serializer;
    this.streamer     = streamer;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    Object value;

    try {
      value = getGetter().invoke(o);

      if ((value == null) && create) {
        value = property.getPropertyType().newInstance();
        setRawValue(o, value);
      }
    } catch (Exception e) {
      throw new OtmException("Failed to get a value from '" + getGetter().toGenericString() + "'", e);
    }

    return value;
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    try {
      getSetter().invoke(o, value);
    } catch (Exception e) {
      throw new OtmException("Failed to set value '" + value + "' for '" 
                              + getSetter().toGenericString() + "'", e);
    }
  }

  /*
   * inherited javadoc
   */
  public Method getGetter() {
    return property.getReadMethod();
  }

  /*
   * inherited javadoc
   */
  public Method getSetter() {
    return property.getWriteMethod();
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return property.getName();
  }

  /*
   * inherited javadoc
   */
  public Class getType() {
    return property.getPropertyType();
  }

  /*
   * inherited javadoc
   */
  public Class getComponentType() {
    return property.getComponentType();
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return serializer;
  }

  /*
   * inherited javadoc
   */
  public Streamer getStreamer() {
    return streamer;
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object serialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.serialize(o) : o;
    } catch (Exception e) {
      throw new OtmException("Serialization error", e);
    }
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object deserialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.deserialize((String) o, getComponentType()) : o;
    } catch (Exception e) {
      throw new OtmException("Deserialization error on " + toString(), e);
    }
  }

  /*
   * inherited javadoc
   */
  public final void load(Object instance, List<String> values,
                         RdfMapper mapper, Session session)
                  throws OtmException {
    load(instance, instance, values, mapper, session);
  }

  /*
   * inherited javadoc
   */
  public void load(Object root, Object instance, List<String> values,
                   RdfMapper mapper, Session session)
            throws OtmException {
    if (!mapper.isAssociation()) {
      this.set(instance, values);

      return;
    }

    List assocs = new ArrayList();
    loadAssocs(values, session, assocs, mapper);
    this.set(instance, assocs);
  }

  /**
   * Instantiate and load the associations into the given list.
   *
   * @param ids the association ids
   * @param session the session under which the load is performed. Used for resolving associations
   *        etc.
   * @param assocs the collection to load association instances to
   * @param mapper the mapper associated with this binder
   *
   * @throws OtmException on a failure in instantiating associations
   */
  protected void loadAssocs(List<String> ids, Session session,
                            Collection assocs, RdfMapper mapper)
                     throws OtmException {
    SessionFactory sf = session.getSessionFactory();
    ClassMetadata  cm = sf.getClassMetadata(mapper.getAssociatedEntity());

    if (cm != null) {
      for (String id : ids) {
        Object a = session.get(cm, id, true);

        if (a != null)
          assocs.add(a);
      }
    }
  }

  /*
   * inherited javadoc
   */
  public boolean isLoaded(Object instance) throws OtmException {
    if (!(instance instanceof LazyLoaded))
      return true;
    LazyLoaded ll = (LazyLoaded) instance;
    LazyLoader lh = ll.getLazyLoader(ll);

    return lh.isLoaded(this);
  }

  /*
   * inherited javadoc
   */
  public RawFieldData getRawFieldData(Object instance)
                               throws OtmException {
    if (!(instance instanceof LazyLoaded))
      return null;
    LazyLoaded ll = (LazyLoaded) instance;
    LazyLoader lh = ll.getLazyLoader(ll);

    return lh.getRawFieldData(this);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    Class<?> type          = property.getPropertyType();
    Class<?> componentType = property.getComponentType();

    return getClass().getName() + "[property=" + property.getName() + ", type="
           + ((type != null) ? type.getName() : "-null-") + ", componentType="
           + ((componentType != null) ? componentType.getName() : "-null-") + ", serializer="
           + serializer + "]";
  }
}
