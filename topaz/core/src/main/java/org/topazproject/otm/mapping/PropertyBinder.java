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

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.topazproject.otm.Blob;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.serializer.Serializer;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface PropertyBinder {
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
   * Gets the streamer for blob fields.
   *
   * @return the streamer or null
   */
  public Streamer getStreamer();

  /**
   * Load the values into this field of the given object instance.
   *
   * @param instance the object
   * @param values the values to set
   * @param mapper the mapper that this loader is associated to
   * @param session the session under which the load is performed. Used for resolving associations
   *        etc.
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void load(Object instance, List<String> values,
                   RdfMapper mapper, Session session) throws OtmException;

  /**
   * Tests whether this field of the given object instance is loaded completely. Used mainly
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
    public EntityBinder.LazyLoaded getRootInstance();
  }

  /**
   * Not easy to sub-class PropertyBinder because of embedded promotes. So added this as a
   * contained object available to Blobs. Revisit once the Mappers are removed.
   */
  public interface Streamer {
    /**
     * Checks if the field is managed by OTM. OTM managed fields, follow
     * this semantics:
     *
     * <ul>
     *   <li>OTM is the factory. The fields are created during a load or attach.</li>
     *   <li>The field is valid only when the containing object is attached to a session</li>
     *   <li>
     *       Managed fields are not change-tracked by Session. So anything that the
     *       application does to change the value of this field is completely ignored by OTM.
     *    </li>
     *   <li>
     *       Making method calls on the managed object is the only way to affect a change
     *       in state. This means for blobs, reading and writing streams. Deletion/Creation of
     *       the blob is also supported if the application is directly using
     *       {@link org.topazproject.otm.Blob} objects. Otherwise if for example using a
     *       {@link javax.activation.DataSource}, 'creation' is implicit when writing
     *       to the stream the first time and 'deletion' requires the containing object
     *       of this field be deleted.
     *   </li>
     * </ul>
     *
     * @return true for managed fields and false otherwise
     */
    public boolean isManaged();

    /**
     * Gets the bytes from the field. (Not the Blob).
     *
     * @param binder the binder this field is attached to
     * @param instance the object instance
     * @throws OtmException if an error in  the bytes from the field.
     *
     * @return the raw blob contents from the field
     */
    public byte[] getBytes(PropertyBinder binder, Object instance) throws OtmException ;

    /**
     * Sets the bytes on the field. (Not the Blob). Works only for un-managed
     * fields and it is an error to perform this on an un-managed Stream.
     *
     * @param binder the binder this field is attached to
     * @param instance the object instance
     * @param bytes the raw blob contents for the field
     *
     * @throws OtmException if an error in copying the bytes to the field.
     */
    public void setBytes(PropertyBinder binder, Object instance, byte[] bytes) throws OtmException;

    /**
     * Attaches the Blob field on the instance. This operation is valid only for managed
     * fields. For application fields, attaching is an error since that can potentially
     * overwrite the field's value set by the application.
     *
     * @param binder the binder this field is attached to
     * @param instance the object instance
     * @param blob the object instance

     * @throws OtmException if this is not a managed field or on other errors
     */
    public void attach(PropertyBinder binder, Object instance, Blob blob) throws OtmException;
  }

  public static abstract class ManagedStreamer implements Streamer {

    public byte[] getBytes(PropertyBinder binder, Object instance) throws OtmException {
      throw new OtmException("Cannot get bytes from a Managed field");
    }

    public void setBytes(PropertyBinder binder, Object instance, byte[] bytes) {
      throw new OtmException("Cannot set bytes on a Managed field");
    }

    public final boolean isManaged() {
      return true;
    }
  }

  public static abstract class UnManagedStreamer implements Streamer {
    public final boolean isManaged() {
      return false;
    }

    public void attach(PropertyBinder binder, Object instance, Blob blob) throws OtmException {
      throw new OtmException("Cannot attach a Blob on an unmanaged field");
    }
  }

  public static class SerializingStreamer extends UnManagedStreamer {
    public byte[] getBytes(PropertyBinder binder, Object instance) throws OtmException {
      List vals = binder.get(instance);
      if ((vals == null) || (vals.size() == 0))
        return null;

      if (vals.size() > 1)
        throw new OtmException("Can't deal with arrays to blob conversion");

      Object val = vals.get(0);
      if (!(val instanceof String))
        throw new OtmException("Must serialize to a String for this Streamer to work");

      try {
        return ((String)val).getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new OtmException("No UTF-8!", e);
      }
    }

    public void setBytes(PropertyBinder binder, Object instance, byte[] bytes)
        throws OtmException {
      if (bytes == null)
        binder.setRawValue(instance, null);
      else
        binder.set(instance, Collections.singletonList(new String(bytes)));
    }
  }
}
