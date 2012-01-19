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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.query.Results;

/**
 * Binds an entity to an {@link org.topazproject.otm.EntityMode} specific implementation.
 *
 * @author Pradeep Krishnan
 */
public interface EntityBinder {
  /**
   * A notification from ClassMetadata.
   *
   * @param cm the ClassMetadata of the entity which this binder is bound to
   *
   * @throws OtmException
   */
  public void bindComplete(ClassMetadata cm) throws OtmException;

  /**
   * Constructs a new instance using a no-argument constructor. TODO: support constructor
   * arguments
   *
   * @return the newly created instance
   *
   * @throws OtmException on an error
   */
  public Object newInstance() throws OtmException;

  /**
   * Constructs a proxy instance using a no-argument constructor. TODO: support constructor
   * arguments
   *
   * @param lazyLoader the loader for the new instance
   *
   * @return the newly created instance
   *
   * @throws OtmException on an error
   */
  public LazyLoaded newLazyLoadedInstance(LazyLoader lazyLoader)
                                   throws OtmException;

  /**
   * Load results onto an instance or if the instance is null create one and load.
   *
   * @param instance the instance to load to or null
   * @param id the entity id
   * @param result the Rdf statements
   * @param sess the session that is loading this
   *
   * @return the loaded instance
   *
   * @throws OtmException on an error
   */
  public Object loadInstance(Object instance, String id, TripleStore.Result result, Session sess)
                      throws OtmException;

  /**
   * Load an instance with View query results.
   *
   * @param obj the instance or null
   * @param id the view id
   * @param r the query results
   * @param sess the session that is loading this
   *
   * @return the loaded instance
   *
   * @throws OtmException on an error
   */
  public Object loadInstance(Object obj, String id, Results r, Session sess)
                      throws OtmException;

  /**
   * Tests if this entity is instantiable and not an abstract super-class.
   *
   * @return true if a new instance of this entity can be constructed
   */
  public boolean isInstantiable();

  /**
   * Tests if the given object is an instance of this entity. Sub-class instances also
   * qualify as instances of this entity.
   *
   * @param o the object to test
   *
   * @return true if the object is an instance of this entity.
   */
  public boolean isInstance(Object o);

  /**
   * Tests if the instances of this binder are assignment compatible with instances of
   * another.
   *
   * @param other the other binder to test
   *
   * @return true if instances of this binder are assignable from instances of the other
   */
  public boolean isAssignableFrom(EntityBinder other);

  /**
   * Gets all unique alias names by which this entity is known. For java classes this is the
   * class name.
   *
   * @return the alternate unique names for this entity published by this PropertyBinder.
   */
  public String[] getNames();

  /**
   * The lazy loader for an instance.
   */
  public static interface LazyLoader {
    /**
     * Gets the Session of this lazy-loader.
     *
     * @return the session
     */
    Session getSession();

    /**
     * Gets the ClassMetadata of the {@link LazyLoaded}  instance.
     *
     * @return the class-metadata
     */
    ClassMetadata getClassMetadata();

    /**
     * Gets the id of the {@link LazyLoaded} instance.
     *
     * @return the id
     */
    String getId();

    /**
     * A test to see if the triples for this object have been loaded.
     *
     * @return true if a load was performed
     */
    boolean isLoaded();

    /**
     * A test to see if a property has been loaded.
     *
     * @param b the binder to test
     *
     * @return true if a load was performed
     */
    boolean isLoaded(PropertyBinder b);

    /**
     * Stash/Clear the raw-data for a lazy-loaded field.
     *
     * @param b the binder to set the raw-data for
     *
     * @param d the raw-data to set or null to clear
     */
    void setRawFieldData(PropertyBinder b, PropertyBinder.RawFieldData d);

    /**
     * Get the raw-data for a field.
     *
     * @param b the binder to get the raw-data for
     *
     * @return the raw-data or null if the field is loaded
     */
    PropertyBinder.RawFieldData getRawFieldData(PropertyBinder b);

    /**
     * Load the data from Store.
     *
     * @param self the lazy-loaded object
     * @param operation the method that is causing this load
     *
     * @throws OtmException on an error
     */
    void ensureDataLoad(LazyLoaded self, String operation)
                 throws OtmException;
  }

  /**
   * An interface implemented by all Lazy loaded objects.
   */
  public static interface LazyLoaded {
    /**
     * Gets the lazy-loader associated with this instance.
     *
     * @param This must be same as 'this'. Provided purely to avoid name clashes with application's
     *        own methods.
     *
     * @return the lazy loader for this object.
     *
     * @throws OtmException if (This != this) or any other error
     */
    LazyLoader getLazyLoader(LazyLoaded This) throws OtmException;
  }
}
