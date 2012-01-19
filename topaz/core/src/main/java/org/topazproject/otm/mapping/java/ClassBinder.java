/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping.java;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.EntityBinder;

/**
 * Binds an entity to an {@link org.topazproject.otm.EntityMode#POJO} specific implementation.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> The object type instantiated by this class
 */
public class ClassBinder<T> implements EntityBinder {
  private final Class<T>          clazz;
  private final Class<?extends T> proxy;
  private final boolean           instantiable;

  /**
   * Creates a new ClassBinder object.
   *
   * @param clazz the java class to bind this entity to
   * @param ignore the methods to ignore when lazily loaded
   */
  public ClassBinder(Class<T> clazz, Method... ignore) {
    this.clazz = clazz;

    int mod = clazz.getModifiers();

    instantiable   = !Modifier.isAbstract(mod) && !Modifier.isInterface(mod)
                      && Modifier.isPublic(mod);
    proxy          = instantiable ? createProxy(clazz, ignore) : null;
  }

  /*
   * inherited javadoc
   */
  public T newInstance() throws OtmException {
    try {
      return clazz.newInstance();
    } catch (Exception t) {
      throw new OtmException("Failed to create a new instance of " + clazz, t);
    }
  }

  /*
   * inherited javadoc
   */
  public ProxyObject newProxyInstance() throws OtmException {
    try {
      return (ProxyObject) proxy.newInstance();
    } catch (Exception t) {
      throw new OtmException("Failed to create a new proxy instance of " + clazz, t);
    }
  }

  /*
   * inherited javadoc
   */
  public boolean isInstantiable() {
    return instantiable;
  }

  /*
   * inherited javadoc
   */
  public boolean isInstance(Object o) {
    return clazz.isInstance(o);
  }

  /*
   * inherited javadoc
   */
  public boolean isAssignableFrom(EntityBinder other) {
    return (other instanceof ClassBinder) && clazz.isAssignableFrom(((ClassBinder) other).clazz);
  }

  /**
   * Gets the java class that this binder binds to.
   *
   * @return the java class bound by this
   */
  public Class<T> getSourceClass() {
    return clazz;
  }

  public static <T> Class<?extends T> createProxy(Class<T> clazz, final Method[] ignoreList) {
    MethodFilter mf =
      new MethodFilter() {
        public boolean isHandled(Method m) {
          if (m.getName().equals("finalize"))
            return false;

          for (Method ignore : ignoreList)
            if (m.equals(ignore))
              return false;

          return true;
        }
      };

    ProxyFactory f  = new ProxyFactory();
    f.setSuperclass(clazz);

    if (Serializable.class.isAssignableFrom(clazz))
      f.setInterfaces(new Class[] { WriteReplace.class });

    f.setFilter(mf);

    Class<?extends T> c = f.createClass();

    return c;
  }

  /*
   * inherited javadoc
   */
  public String[] getNames() {
    return new String[] { clazz.getName() };
  }

  public static interface WriteReplace {
    public Object writeReplace() throws ObjectStreamException;
  }
}
