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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class HibernateEntityUtil {

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
   * This works by calling all getters and setters on the objects to be copied.
   * NOTES:
   * <ul>
   *   <li>If the property is mapped as a persistent object, then the properties are recursively copied over.</li>
   *   <li>Lists and arrays are copied over element by element, recursively if the elements are mapped as persistent entities</li>
   *   <li>Sets and Maps are replaced wholesale.</li>
   *   <li>The id property is kept the same</li>
   *   <li>All other properties are simply replaced</li>
   * </ul>
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
      } else if (List.class.isAssignableFrom(getter.getReturnType())) {
        List newList = (List) getter.invoke(from);
        List oldList = (List) getter.invoke(to);
        if (newList == null || oldList == null) {
          setter.invoke(to, newList);
        } else {
          copyListProperties(newList, oldList);
        }
      } else if (getter.getReturnType().isArray()) {
        Object newArray = getter.invoke(from);
        Object oldArray = setter.invoke(to);
        if (newArray == null || oldArray == null) {
          setter.invoke(to, getter.invoke(from));
        } else {
          copyArrayProperties(to, getter, setter, newArray, oldArray);
        }
      }
      //TODO: what about Sets and maps?
      else {
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

  @SuppressWarnings("unchecked")
  private void copyListProperties(List newList, List oldList) throws Exception {
    int max = Math.min(newList.size(), oldList.size());
    for (int i = 0; i < max; i++) {
      Object newEntry = newList.get(i);
      Object oldEntry = oldList.get(i);
      if (classMetadata.containsKey(Hibernate.getClass(oldEntry).getName())) {
        copyPropertiesFromTransientInstance(newEntry, oldEntry);
      } else {
        oldList.set(i, newEntry);
      }
    }
    if (newList.size() < max) {
      for (int i = max; i < oldList.size(); i++) {
        oldList.remove(i);
      }
    } else if (newList.size() > max) {
      for (int i = max; i < newList.size(); i++) {
        oldList.add(newList.get(i));
      }
    }
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


  private Method setter(Class<?> clazz, Method getter) throws NoSuchMethodException {
    return clazz.getMethod(getter.getName().replaceAll("get", "set"), getter.getReturnType());
  }
}
