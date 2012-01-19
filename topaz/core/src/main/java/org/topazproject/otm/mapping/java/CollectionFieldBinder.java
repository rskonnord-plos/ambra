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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * FieldBinder for {@link java.util.Collection collection} fields.
 *
 * @author Pradeep Krishnan
 */
public class CollectionFieldBinder extends AbstractFieldBinder {
  /**
   * Creates a new CollectionFieldBinder object.
   *
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType the collection component type
   */
  public CollectionFieldBinder(Field field, Method getter, Method setter,
                          Serializer serializer, Class componentType) {
    super(field, getter, setter, serializer, componentType);
  }

  /**
   * Retrieve elements from a collection field of an object.
   *
   * @param o the object
   *
   * @return the list of array elements (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException {
    Collection value = (Collection) getRawValue(o, false);

    if (value == null)
      return Collections.emptyList();

    ArrayList res = new ArrayList(value.size());

    for (Object v : value)
      if (v != null)
        res.add(serialize(v));

    return res;
  }

  /**
   * Populate a collection field of an object.
   *
   * @param o the object
   * @param vals the values to be set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException {
    Collection value = null;
    if (getGetter() != null || getField().isAccessible() ||
        Modifier.isPublic(getField().getModifiers()))
      value = (Collection) getRawValue(o, false);

    boolean    create = (value == null) || isOurProxy(value);

    if (create)
      value = newInstance();

    value.clear();

    for (Object v : vals)
      value.add(deserialize(v));

    if (create)
      setRawValue(o, value);
  }

  /*
   * inherited javadoc
   */
  public void load(Object root, Object instance, List<String> values, 
      Map<String, Set<String>> types, RdfMapper mapper, 
      Session session) throws OtmException {

    if (mapper.isAssociation() && (mapper.getFetchType() == FetchType.lazy) 
        && getType().isInterface())
      setRawValue(instance, newProxy(values, types, session, root, mapper));
    else
      super.load(root, instance, values, types, mapper, session);
  }

  /**
   * Create a new collection instance. The current implementation will instantiate classes
   * for various field types as follows:<dl><dt>Collection, List, AbstractList<dd>ArrayList<dt>Set,
   * AbstractSet<dd>HashSet<dt>SortedSet<dd>TreeSet<dt>AbstractSequentialList<dd>LinkedList<dt>anything
   * else<dd>assumed to be a concrete implementation and instantiated directly</dl><p>Override
   * this method in order to fine tune the instance creation.</p>
   *
   * @return an empty collection instance
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected Collection newInstance() throws OtmException {
    try {
      Class t = getType();

      if (t == Collection.class)
        return new ArrayList();

      if (t == List.class)
        return new ArrayList();

      if (t == Set.class)
        return new HashSet();

      if (t == SortedSet.class)
        return new TreeSet();

      if (t == AbstractList.class)
        return new ArrayList();

      if (t == AbstractSequentialList.class)
        return new LinkedList();

      if (t == AbstractSet.class)
        return new HashSet();

      return (Collection) t.newInstance();
    } catch (Exception e) {
      throw new OtmException("Can't instantiate " + getType(), e);
    }
  }

  /**
   * Creates a new proxy instance of the collection to support lazy loading.
   *
   * Note: There is a corner case where the type look ahead cache we are using becoming stale. 
   * This would happen if the app did a type conversion on some objects (ie. changing the rdf:type
   * used in sub-class determination) by the time we look at it to do the loads. So in those
   * cases it is likely that we may end up loading the wrong sub-class. The solution really
   * is to manage the type look ahead cache in a shared manner (perhaps in Session).
   *
   * @param values   the values to set
   * @param types    the type look ahead for associations
   * @param session  the session under which the load is performed.
   *                 Used for resolving associations etc.
   * @param instance the object
   * @param mapper   the mapper that this loader is associated to
   *
   * @return the dynamic proxy instance of
   */
  protected Collection newProxy(final List<String> values, 
      final Map<String, Set<String>> types, final Session session, 
      final Object instance, final RdfMapper mapper) throws OtmException {
    final Class t = getType();
    final Collection real = newInstance();

    InvocationHandler handler = new OtmInvocationHandler() {
      private boolean loaded = false;
      public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
        if (!loaded) {
          // Note: no short-cuts here. See orphan-delete tracking in session.
          // Also session.load() may return null because of deletes etc. Hence
          // no assumption can be made regarding values.size() the same as
          // the collection size.
          loadAssocs(values, types, session, real, mapper);
          loaded = true;
          session.delayedLoadComplete(instance, mapper);
        }
        return method.invoke(real, args);
      }
      public boolean isLoaded() {
        return loaded;
      }
    };

    Collection value = (Collection) Proxy.newProxyInstance(t.getClassLoader(), 
          new Class[] {t}, handler);
    assert t.isInstance(value) : "expecting " + t + ", got " + value.getClass();
    return value;
  }

  /**
   * Tests to see if the collection is a proxy that we created.
   *
   * @param col the collection to test
   */
  protected boolean isOurProxy(Collection col) {
    return Proxy.isProxyClass(col.getClass())
      && Proxy.getInvocationHandler(col) instanceof OtmInvocationHandler;
  }

  /*
   * inherited javadoc
   */
  public boolean isLoaded(Object instance) throws OtmException {
    Collection val = (Collection)getRawValue(instance, false);
    return (val == null) || !isOurProxy(val) ||
      ((OtmInvocationHandler)Proxy.getInvocationHandler(val)).isLoaded();
  }

  /**
   * A marker interface to detect when it is ours
   */
  private static interface OtmInvocationHandler extends InvocationHandler {

    public boolean isLoaded();

  }

}
