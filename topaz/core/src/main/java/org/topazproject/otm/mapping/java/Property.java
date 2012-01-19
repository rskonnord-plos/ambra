/* $HeadURL:: http://gandalf.topazproject.org/svn/head/topaz/core/src/main/java/org/topa#$
 * $Id: AnnotationClassMetaFactory.java 6329 2008-08-14 17:46:24Z pradeep $
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

import java.beans.Introspector;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.Collection;

/**
 * A java bean property.
 *
 * @author Pradeep Krishnan
 */
public class Property {
  private final Class<?> clazz;
  private final String   name;
  private final Method   getter;
  private final Method   setter;
  private final Class<?> type;
  private final Class<?> componentType;

  /**
   * Creates a new Property object.
   *
   * @param clazz the class this property is a member of
   * @param name name of the property
   */
  public Property(Class<?> clazz, String name) {
    this(clazz, name, getReadMethod(clazz, name), getWriteMethod(clazz, name));
  }

  /**
   * Creates a new Property object.
   *
   * @param clazz the class this property is a member of
   * @param name name of the property
   * @param getter the get method for this property
   * @param setter the set method for this property
   */
  public Property(Class<?> clazz, String name, Method getter, Method setter) {
    this(clazz, name, getter, setter, getPropertyType(getter, setter),
         getComponentType(getter, setter));
  }

  /**
   * Creates a new Property object.
   *
   * @param clazz the class this property is a member of
   * @param name name of the property
   * @param getter the get method for this property
   * @param setter the set method for this property
   * @param type the type of this property
   */
  public Property(Class<?> clazz, String name, Method getter, Method setter, Class<?> type) {
    this(clazz, name, getter, setter, type, getComponentType(getter, setter, type));
  }

  /**
   * Creates a new Property object.
   *
   * @param clazz the class this property is a member of
   * @param name name of the property
   * @param getter the get method for this property
   * @param setter the set method for this property
   * @param type the type of this property
   * @param componentType the componentType. See {@link #getComponentType}
   */
  public Property(Class<?> clazz, String name, Method getter, Method setter, Class<?> type,
                  Class<?> componentType) {
    this.clazz           = clazz;
    this.name            = name;
    this.getter          = getter;
    this.setter          = setter;
    this.type            = type;
    this.componentType   = componentType;
  }

  /**
   * Tries to create a property from a method. A property will be created only if the method
   * is a get or set method as per the Java Beans Specification.
   *
   * @param m the method to introspect and create a property
   *
   * @return a newly created property or null if this method is not a get or set method
   */
  public static Property toProperty(Method m) {
    if (Modifier.isStatic(m.getModifiers()))
      return null;

    String capitalized = getCapitalizedName(m);

    if (capitalized == null)
      return null;

    Class<?> clazz    = m.getDeclaringClass();
    String   propName = Introspector.decapitalize(capitalized);
    Method   setter;
    Method   getter;

    if (!m.getName().startsWith("set")) {
      getter          = m;

      // Find the set method that may take a super class or a super interface
      // of the return type. The Class#getMethod is not sufficient.
      setter          = findSetter(clazz, "set" + capitalized, getter.getReturnType());
    } else {
      setter = m;

      Class<?> type = setter.getParameterTypes()[0];
      getter = (!type.equals(Boolean.TYPE)) ? null
               : getMethodR(clazz, "is" + capitalized, Boolean.TYPE);

      if (getter == null)
        getter = getMethod(clazz, "get" + capitalized);

      // Note that if there are multiple methods with the same name, the
      // one with the return type that is more specific than others is
      // returned. So the only check we do is to ensure that the set
      // method's type is assignable from the return type of the get method.
      if ((getter != null) && !type.isAssignableFrom(getter.getReturnType()))
        getter = null;
    }

    return new Property(clazz, propName, getter, setter);
  }

  /**
   * Resolves this generic property in the parameterized sub-class and create a new synthetic
   * property in that sub-class.
   *
   * @param sub the parameterized sub-class of this class
   * @param pt  the parameterized this class in the sub-class declaration
   *
   * @return the synthetic property in the parameterized sub-class or null
   */
  public Property resolveGenericsType(Class sub, ParameterizedType pt) {
    TypeVariable[]    types = clazz.getTypeParameters();
    Type t                  = getGenericType();

    if (t instanceof TypeVariable) {
      Class<?> ptype = resolve((TypeVariable) t, pt, types);

      return (ptype == null) ? null
             : new Property(sub, getName(), getReadMethod(), getWriteMethod(), ptype, ptype);
    }

    if (t instanceof ParameterizedType) {
      Type[] args = ((ParameterizedType) t).getActualTypeArguments();

      if (!isCollection() || (args.length != 1) || !(args[0] instanceof TypeVariable))
        return null;

      Class<?> ptype = resolve((TypeVariable) args[0], pt, types);

      return (ptype == null) ? null
             : new Property(sub, getName(), getReadMethod(), getWriteMethod(), getPropertyType(),
                            ptype);
    }

    if (t instanceof GenericArrayType) {
      Type     comp  = ((GenericArrayType) t).getGenericComponentType();
      Class<?> ptype;

      if (!isArray() || !(comp instanceof TypeVariable)
           || ((ptype = resolve((TypeVariable) comp, pt, types)) == null))
        return null;

      return new Property(sub, getName(), getReadMethod(), getWriteMethod(), getPropertyType(),
                          ptype);
    }

    return null;
  }

  private static Class<?> resolve(TypeVariable t, ParameterizedType pt, TypeVariable[] types) {
    for (int i = 0; i < types.length; i++) {
      if (t.equals(types[i])) {
        Type ptype = pt.getActualTypeArguments()[i];

        return (ptype instanceof Class) ? (Class<?>) ptype : null;
      }
    }

    return null;
  }

  /**
   * Gets the clazz this property belongs to.
   *
   * @return the clazz this property belongs to
   */
  public Class<?> getContainingClass() {
    return clazz;
  }

  /**
   * Gets the name of this property.
   *
   * @return name of the property following java beans naming conventions
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the getter for this property.
   *
   * @return the getter or null if there is no get method.
   */
  public Method getReadMethod() {
    return getter;
  }

  /**
   * Gets the setter for this property.
   *
   * @return the setter or null if there is no set method.
   */
  public Method getWriteMethod() {
    return setter;
  }

  /**
   * Gets the type of this property.
   *
   * @return the type of this property
   */
  public Class<?> getPropertyType() {
    return type;
  }

  /**
   * Gets the component type of this property. For {@link java.util.Collection} and arrays,
   * the components represent the members. Everything else the componentType is same as {@link
   * #getPropertyType}. Note also that if the java.util.Collection is a {@link
   * java.lang.reflect.ParameterizedType}, then the actual type is looked up to find the
   * componentType. Otherwise for java.util.Collection, the componentType  will be Object.class.
   *
   * @return the component type of this property.
   */
  public Class<?> getComponentType() {
    return componentType;
  }

  /**
   * Gets the generic type of this property.
   *
   * @return the generic type or null
   */
  public Type getGenericType() {
    return getGenericType(getter, setter);
  }

  /**
   * Tests if this property type is an array.
   *
   * @return true if the type is an array, false otherwise
   */
  public boolean isArray() {
    return type.isArray();
  }

  /**
   * Tests if this property type is a Collection.
   *
   * @return true if the type is a {@link java.util.Collection}
   */
  public boolean isCollection() {
    return Collection.class.isAssignableFrom(type);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "'" + name + "' in " + clazz;
  }

  /*
   * inherited javadoc
   */
  public boolean equals(Object other) {
    return (!(other instanceof Property)) ? false
           : (name.equals(((Property) other).name) && clazz.equals(((Property) other).clazz));
  }

  /*
   * inherited javadoc
   */
  public int hashCode() {
    return clazz.hashCode() + name.hashCode();
  }

  /**
   * Gets the property type from a pair of getter and setter methods
   *
   * @param getter the get method or null
   * @param setter the set method or null
   *
   * @return the property type or null
   */
  public static Class<?> getPropertyType(Method getter, Method setter) {
    if (getter != null)
      return getter.getReturnType();

    if (setter != null)
      return setter.getParameterTypes()[0];

    return null;
  }

  /**
   * Gets the component type from a pair of getter and setter methods.
   *
   * @param getter the get method or null
   * @param setter the set method or null
   *
   * @return the component type or null
   */
  public static Class<?> getComponentType(Method getter, Method setter) {
    return getComponentType(getter, setter, getPropertyType(getter, setter));
  }

  /**
   * Gets the component type from a pair of getter and setter methods and the property type.
   *
   * @param getter the get method or null
   * @param setter the set method or null
   * @param type the property type from the get/set methods
   *
   * @return the component type or null if type is null
   */
  public static Class<?> getComponentType(Method getter, Method setter, Class<?> type) {
    if (type == null)
      return null;

    if (type.isArray())
      return type.getComponentType();

    if (Collection.class.isAssignableFrom(type))
      return collectionType(getGenericType(getter, setter));

    return type;
  }

  /**
   * Gets the generic type from a pair of getter and setter methods.
   *
   * @param getter the get method or null
   * @param setter the set method or null
   *
   * @return the generic type or null
   */
  public static Type getGenericType(Method getter, Method setter) {
    if (getter != null)
      return getter.getGenericReturnType();

    if (setter != null)
      return setter.getGenericParameterTypes()[0];

    return null;
  }

  /**
   * Gets the read method for a given property name.
   *
   * @param clazz the clazz to find the method in
   * @param name the name of the property (Java beans property name)
   *
   * @return the read method or null
   */
  public static Method getReadMethod(Class<?> clazz, String name) {
    name = capitalize(name);

    Method m = getMethodR(clazz, "is" + name, Boolean.TYPE);

    if (m == null)
      m = getMethod(clazz, "get" + name);

    return m;
  }

  /**
   * Gets the write method for a given property name. Since there could be multiple set
   * methods in a class with differing signatures for the same property, it tries to find the get
   * method first and then tries to locate a matching set method. If no get method is found, then
   * any one of the matching methods could be returned.
   *
   * @param clazz the clazz to find the method in
   * @param name the name of the property (Java beans property name)
   *
   * @return the write method or null
   */
  public static Method getWriteMethod(Class<?> clazz, String name) {
    Method read = getReadMethod(clazz, name);
    name = "set" + capitalize(name);

    if (read != null)
      return findSetter(clazz, name, read.getReturnType());

    for (Method m : clazz.getMethods())
      if (m.getName().equals(name) && (m.getParameterTypes().length == 1))
        return m;

    return null;
  }

  private static Method findSetter(Class<?> clazz, String name, Class<?> type) {
    if (type == null)
      return null;

    Method setter = getMethod(clazz, name, type);

    if (setter != null)
      return setter;

    if (superClass(type) != null) {
      setter = findSetter(clazz, name, superClass(type));

      if (setter != null)
        return setter;
    }

    for (Class<?> t : interfaces(type)) {
      setter = findSetter(clazz, name, t);

      if (setter != null)
        return setter;
    }

    return null;
  }

  private static Class<?> superClass(Class<?> type) {
    if (!type.isArray())
      return type.getSuperclass();

    Class<?> ct = superClass(type.getComponentType());

    return (ct == null) ? null : Array.newInstance(ct, 0).getClass();
  }

  private static Class<?>[] interfaces(Class<?> type) {
    if (!type.isArray())
      return type.getInterfaces();

    Class<?>[] cifs = interfaces(type.getComponentType());
    Class<?>[] ifs  = new Class[cifs.length];

    for (int i = 0; i < ifs.length; i++)
      ifs[i] = Array.newInstance(cifs[i], 0).getClass();

    return ifs;
  }

  private static String getCapitalizedName(Method m) {
    if (m.getName().startsWith("set") && (m.getParameterTypes().length == 1))
      return m.getName().substring(3);

    if (m.getName().startsWith("get") && (m.getParameterTypes().length == 0))
      return m.getName().substring(3);

    if (m.getName().startsWith("is") && (m.getParameterTypes().length == 0))
      return m.getName().substring(2);

    return null;
  }

  private static String capitalize(String name) {
    if ((name == null) || (name.length() == 0))
      return name;

    if ((name.length() > 1) && Character.isUpperCase(name.charAt(1)))
      return name;

    char[] chars = name.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);

    return new String(chars);
  }

  private static Class<?> collectionType(Type type) {
    Class result = Object.class;

    if (type instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) type;
      Type[]            targs = ptype.getActualTypeArguments();

      if ((targs.length > 0) && (targs[0] instanceof Class))
        result = (Class) targs[0];

      if ((targs.length > 0) && (targs[0] instanceof TypeVariable)
           && ((TypeVariable) targs[0]).getBounds()[0] instanceof Class)
        result = (Class) ((TypeVariable) targs[0]).getBounds()[0];
    }

    return result;
  }

  private static Method getMethod(Class<?> clazz, String name, Class... parameterTypes) {
    Method m;

    try {
      m = clazz.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException t) {
      m = null;
    }

    return m;
  }

  private static Method getMethodR(Class<?> clazz, String name, Class returnType,
                                   Class... parameterTypes) {
    Method m = getMethod(clazz, name, parameterTypes);

    if ((m != null) && !m.getReturnType().equals(returnType))
      m = null;

    return m;
  }
}
