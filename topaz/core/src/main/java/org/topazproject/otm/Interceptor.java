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
package org.topazproject.otm;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * Allows user code to inspect and/or change property values.
 * <p>
 * <i>This is similar to a Hibernate Interceptor and most of the Hibernate documentation
 * is applicable here too. The following description is mostly copied from Hibernate.</i>
 * <p>
 * Inspection occurs before property values are written and after they are read from the database.
 * <p>
 * There might be a single instance of Interceptor for a SessionFactory, or a new instance might
 * be specified for each Session.
 * <p>
 * The Session may not be invoked from a callback (nor may a callback cause a collection or proxy
 * to be lazily initialized).
 *
 * @author Pradeep Krishnan
 */
public interface Interceptor {
   public static String NULL = "NULL";

   /**
   * Gets a fully loaded entity instance that is cached externally,
   *
   * @param session the session that is doing the lookup
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance an instance object to refresh or null
   *
   * @return a cached instance or null if not in cache or @link{#NULL}
   *         if an instance does not exist.
   */
  public Object getEntity(Session session, ClassMetadata cm, String id, Object instance);

  /**
   * Called after all eager loaded fields of an entity instance is loaded from the store.
   * Interceptor may modify the instance value.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was loaded or nul if the object does not exist
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance);

  /**
   * Called after a delayed load of a field for an entity instance.
   * Interceptor may modify the instance value.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance for which the field was loaded
   * @param field the field that was loaded
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance, Mapper field);

  /**
   * Called after an entity instance is written out to the store.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was written out
   * @param update the updates that we detected or null if the instance just got attached
   */
  public void onPostWrite(Session session, ClassMetadata cm, String id, Object instance,
                           Updates update);

  /**
   * Called after an entity instance is deleted from the store.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that was inserted
   */
  public void onPostDelete(Session session, ClassMetadata cm, String id, Object instance);

  /**
   * Called befora an entity instance is deleted from the store. Note that there is
   * no guarantee that the delete will succeed. In addition, the interceptor could veto
   * the delete by throwing an exception.
   *
   * @param session the session that is reporting this event
   * @param cm class metadata for the entity
   * @param id the id of the instance
   * @param instance the instance that is about to be deleted.
   *
   * @throws OtmException to veto the delete operation
   */
  public void onPreDelete(Session session, ClassMetadata cm, String id, Object instance)
                             throws OtmException;

  /**
   * Change notification structure.
   */
  public static class Updates {
    public boolean pmapChanged = false;
    public boolean blobChanged = false;
    public List<RdfMapper> rdfMappers = new ArrayList<RdfMapper>();
    public List<List<String>> oldValues = new ArrayList<List<String>>();

    /**
     * Gets the old serialized value of a field.
     */
    public List<String> getOldValue(String name) {
      int idx = nameToIndex(name);
      return (idx < 0) ? null : oldValues.get(idx);
    }

    /**
     * Test if a property is changed.
     */
    public boolean isChanged(String name) {
      return nameToIndex(name) >= 0;
    }

    private int nameToIndex(String name) {
      int idx = 0;
      for (RdfMapper m : rdfMappers) {
        if (m.getName().equals(name))
          return idx;
        idx++;
      }
      return -1;
    }
  }
}
