/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.topazproject.ambra.util;

import org.hibernate.Hibernate;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for operations involving basic beans that require knowledge of which classes are mapped as entities
 *
 * @author Alex Kudlick Date: 6/15/11
 *         <p/>
 *         org.topazproject.ambra.util
 */
public class HibernateEntityUtil implements EntityUtil {

  private static final Logger log = LoggerFactory.getLogger(HibernateEntityUtil.class);
  private Map<String, ClassMetadata> classMetadata;

  /**
   * Set the class metadata for persistent entities.  This enables the bean to have knowledge of which entities are
   * mapped by Hibernate, and can be retrieved by calling SessionFactory.getAllClassMetadata()
   *
   * @param classMetadata - the class metadata of persistent objects
   */
  public HibernateEntityUtil(Map<String, ClassMetadata> classMetadata) {
    this.classMetadata = classMetadata;
  }

  /**
   * Copy properties from a transient object to an already persisted object to be updated.  This is necessary if you
   * want to update an object in the database with all the properties from a transient instance.
   * <p/>
   * This works by calling all getters and setters on the objects to be copied. NOTES: <ul> <li>If the property is
   * mapped as a persistent object, then the properties are recursively copied over.</li> <li>Collections of
   * non-persistent objects are just replaced</li><li>Collections of persistent entities are updated by copying over
   * elements with matching ids.  Elements from the old collection that don't have a match in the new one will be
   * removed, and elements in the new collection that don't have a match in the old will be added.  Lists will be
   * reordered to have the same order as the new collection</li> <li>Arrays are copied over element by element</li>
   * <li>The id property is kept the same</li> <li>All other properties are simply replaced</li> </ul>
   * <p/>
   * See <a href="http://stackoverflow.com/questions/4779239/update-persistent-object-with-transient-object-using-hibernate">this
   * post on StackOverflow</a> for more information.
   *
   * @param from - the transient object with properties to be copied
   * @param to   - the persistent object to which to copy the properties
   * @throws Exception - Reflection exceptions
   */
  public void copyPropertiesFromTransientInstance(Object from, Object to) throws Exception {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Trying to copy " + (from == null ? "from" : "to") + " a null entity");
    }
    final Class<?> clazz = from.getClass();
    if (!clazz.isAssignableFrom(to.getClass())) {
      throw new IllegalArgumentException("Incompatible types; trying to copy from " + clazz + " to " + to.getClass());
    }
    final String className = Hibernate.getClass(to).getName();
    if (!classMetadata.containsKey(className)) {
      throw new IllegalArgumentException("Attempting to copy to unmapped entity; " +
          "use Apache BeanUtils.copyProperties() or similar method");
    }
    //remember the old id
    String idPropertyName = classMetadata.get(className).getIdentifierPropertyName();
    final String idGetterName = "get" + idPropertyName.substring(0, 1).toUpperCase() + idPropertyName.substring(1);
    final String idSetterName = idGetterName.replaceAll("get", "set");
    Object oldId = clazz.getMethod(idGetterName).invoke(to);


    //Iterate over all getters
    for (Method getter : allGetters(clazz)) {
      Method setter;
      try {
        setter = clazz.getMethod(getter.getName().replaceAll("get", "set"), getter.getReturnType());
      } catch (Exception e) {
        log.warn("Coulnd't find setter for getter: " + getter.getName() + " on class: " + from.getClass());
        continue;
      }
      //check if property is a persitent entity
      if (classMetadata.containsKey(getter.getReturnType().getName())) {
        //property is a persistent entity, so copy all the properties
        if (getter.invoke(to) == null) {
          setter.invoke(to, getter.invoke(from));
        } else {
          copyPropertiesFromTransientInstance(getter.invoke(from), getter.invoke(to));
        }
      } else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
        //collections
        final Collection newCollection = (Collection) getter.invoke(from);
        final Collection oldCollection = (Collection) getter.invoke(to);
        copyCollections(newCollection, oldCollection, (ParameterizedType) getter.getGenericReturnType());
      } else if (getter.getReturnType().isArray()) {
        Object newArray = getter.invoke(from);
        Object oldArray = setter.invoke(to);
        if (newArray == null || oldArray == null) {
          setter.invoke(to, getter.invoke(from));
        } else {
          copyArrayProperties(to, getter, setter, newArray, oldArray);
        }
      } else {
        //nothing special about this property
        setter.invoke(to, getter.invoke(from));
      }
    }
    //set the old id back
    clazz.getMethod(idSetterName, clazz.getMethod(idGetterName).getReturnType()).invoke(to, oldId);
  }

  private void copyArrayProperties(Object to, Method getter, Method setter, Object newArray, Object oldArray) throws IllegalAccessException, InvocationTargetException {
    Object arrayToSet = Array.newInstance(getter.getReturnType().getComponentType(), Array.getLength(newArray));
    for (int i = 0; i < Array.getLength(arrayToSet); i++) {
      final Object newEntry = Array.get(newArray, i);
      if (classMetadata.containsKey(newEntry.getClass().getName())) {
        try {
          copyPropertiesFromTransientInstance(newEntry, Array.get(oldArray, i));
          Array.set(arrayToSet, i, Array.get(oldArray, i));
        } catch (Exception e) {
          Array.set(arrayToSet, i, newEntry);
        }
      } else {
        Array.set(arrayToSet, i, newEntry);
      }
    }
    setter.invoke(to, arrayToSet);
  }

  /**
   * Helper method to copy collections.  If the collections don't contain entities, the entries in the old collection
   * are simply replaced with those in the new one.  Else the method removes any elements from the old collection that
   * aren't in the new one, updates existing entries that have the same ids as an element in the new collection, and
   * adds new elements.
   *
   * @param newCollection
   * @param oldCollection
   * @param type
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void copyCollections(Collection newCollection, Collection oldCollection, ParameterizedType type) throws Exception {
    //collections pulled up from hibernate are never null.  If we want to set a null collection,
    // instead of dereferencing, just clear.  See http://www.onkarjoshi.com/blog/188/hibernateexception-a-collection-with-cascade-all-delete-orphan-was-no-longer-referenced-by-the-owning-entity-instance/
    if (newCollection == null || newCollection.size() == 0) {
      oldCollection.clear();
      return;
    }
    Class clazz = (Class) type.getActualTypeArguments()[0];
    //if it's a collection of entities, copy over by id
    if (classMetadata.containsKey(clazz.getName())) {
      //set up some stuff we'll need
      String idPropertyName = classMetadata.get(clazz.getName()).getIdentifierPropertyName();
      final String idGetterName = "get" + idPropertyName.substring(0, 1).toUpperCase() + idPropertyName.substring(1);
      final Method idGetter = clazz.getMethod(idGetterName);
      List elementsToRemove = new LinkedList();
      List elementsToAdd = new LinkedList();
      //check if there are any entries in the old collection that aren't in the new one.
      // If so, remove them later (to avoid concurrent modification exceptions)
      for (Object oldEntry : oldCollection) {
        if (getByMatchingId(newCollection, oldEntry, idGetter) == null) {
          elementsToRemove.add(oldEntry);
        }
      }
      for (Object removeThis : elementsToRemove) {
        oldCollection.remove(removeThis);
      }

      //copy over the elements
      for (Object newEntry : newCollection) {
        //check if the old collection has an entry corresponding to this new object's id
        Object matching = getByMatchingId(oldCollection, newEntry, idGetter);
        if (matching != null) {
          copyPropertiesFromTransientInstance(newEntry, matching);
        } else {
          //no matching instance, so just add this new entry
          elementsToAdd.add(newEntry);
        }
      }
      for (Object addThis : elementsToAdd) {
        oldCollection.add(addThis);
      }
      //make sure things are in the same order if it's a list
      if (List.class.isAssignableFrom(oldCollection.getClass())) {
        final List<Serializable> ids = new ArrayList<Serializable>(newCollection.size());
        for (Object o : newCollection) {
          ids.add((Serializable) idGetter.invoke(o));
        }
        Collections.sort((List) oldCollection, new Comparator() {
          @Override
          public int compare(Object o, Object o1) {
            try {
              int leftIndex = ids.indexOf(idGetter.invoke(o));
              int rightIndex = ids.indexOf(idGetter.invoke(o1));
              if (leftIndex < rightIndex) {
                return -1;
              } else if (leftIndex > rightIndex) {
                return 1;
              }
            } catch (Exception e) {
              log.error("Error invoking id getter method: " + idGetter.getName() + " on class " + o.getClass(), e);
            }
            return 0;
          }
        });
      }

    } else {
      //collection of non-persistent objects, just copy it over
      oldCollection.clear();
      oldCollection.addAll(newCollection);
    }
  }

  /**
   * Helper method to get an object out of a collection with matching id.  Returns null if no match was found
   *
   * @param collection
   * @param objectToMatch
   * @return
   * @throws Exception
   */
  private Object getByMatchingId(Collection collection, Object objectToMatch, Method idGetter) throws Exception {
    Object id = idGetter.invoke(objectToMatch);
    for (Object entry : collection) {
      Object entryId = idGetter.invoke(entry);
      if (entryId != null && entryId.equals(id)) {
        return entry;
      }
    }
    //didn't find a match return null
    return null;
  }

  private List<Method> allGetters(Class<?> clazz) {
    List<Method> getters = new LinkedList<Method>();
    for (Method method : clazz.getMethods()) {
      if (method.getName().startsWith("get")
          && method.getParameterTypes().length == 0
          && !method.getName().equals("getClass")
          && !method.getName().equals("getIsPartOf")) //Hack to get around infinite loops in our article object model
      {
        getters.add(method);
      }
    }
    return getters;
  }
}
