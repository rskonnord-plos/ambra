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
package org.topazproject.otm.mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.serializer.Serializer;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface Binder {
  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Get a value from a field of an object.
   *
   * @param o the object
   *
   * @return the list containing the field's values (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException;

  /**
   * Set a value for an object field.
   *
   * @param o the object
   * @param vals the list of values to set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException;

  /**
   * Get the raw object field value.
   *
   * @param o the object
   * @param create whether to create an instance
   *
   * @return the raw field value
   *
   * @throws OtmException if a field's value cannot be retrieved
   */
  public Object getRawValue(Object o, boolean create) throws OtmException;

  /**
   * Set the raw object field value
   *
   * @param o the object
   * @param value the value to set
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void setRawValue(Object o, Object value) throws OtmException;

  /**
   * Gets the serializer used. Note that there won't be any serializer set up for
   * associations.
   *
   * @return the serializer or null
   */
  public Serializer getSerializer();

  /**
   * Load the values into this field of the given object instance.
   *
   * @param instance the object
   * @param values the values to set
   * @param types the type look ahead for associations
   * @param mapper the mapper that this loader is associated to
   * @param session the session under which the load is performed. Used for resolving associations
   *        etc.
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void load(Object instance, List<String> values, Map<String, Set<String>> types,
                   RdfMapper mapper, Session session) throws OtmException;

  /**
   * Tests wether this field of the given object instance is loaded completely. Used mainly
   * for testing if a lazy-loaded field is loaded or is still a proxy.
   *
   * @param instance the object
   *
   * @return true for all eager-loaded fields and lazy-loaded fields that are loaded completely
   *
   * @throws OtmException on an error
   */
  public boolean isLoaded(Object instance) throws OtmException;

  /**
   * Gets the raw field data from which this field was loaded. Usually this information is only 
   * available for proxy fields that are yet to be loaded. Implementations are not expected to
   * retain this information once a field has been fully loaded and instantiated.
   *
   * @param instance the object
   *
   * @return the raw data used in load or null if not available.
   *
   * @throws OtmException on an error
   */
  public RawFieldData getRawFieldData(Object instance) throws OtmException;

  /**
   * The raw data from which a field is loaded.
   */
  public static interface RawFieldData {
    public List<String> getValues();
    Map<String, Set<String>> getTypeLookAhead();
  }

}
