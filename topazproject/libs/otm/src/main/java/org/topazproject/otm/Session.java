/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.lang.reflect.Method;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.mapping.Mapper;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public class Session {
  private static final Log      log             = LogFactory.getLog(Session.class);
  private SessionFactory        sessionFactory;
  private Transaction           txn             = null;
  private Map<Id, Object>       cleanMap        = new HashMap<Id, Object>();
  private Map<Id, Object>       dirtyMap        = new HashMap<Id, Object>();
  private Map<Id, Object>       deleteMap       = new HashMap<Id, Object>();
  private Set<Id>               pristineProxies = new HashSet<Id>();
  private Map<Id, Set<Wrapper>> associations    = new HashMap<Id, Set<Wrapper>>();
  private Set<Id>               currentIds      = new HashSet<Id>();

/**
   * Creates a new Session object.
   *
   * @param sessionFactory the session factory that created this session
   */
  public Session(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Gets the session factory that created this session.
   *
   * @return the session factory
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Begins a new transaction. All session usage is within transaction scope.
   *
   * @return the transaction
   *
   * @throws OtmException on an error
   */
  public Transaction beginTransaction() throws OtmException {
    if (txn == null)
      txn = new Transaction(this);

    return txn;
  }

  /**
   * Gets the current transaction.
   *
   * @return the transaction
   */
  public Transaction getTransaction() {
    return txn;
  }

  /**
   * Close and release all resources.
   *
   * @throws OtmException on an error
   */
  public void close() throws OtmException {
    clear();

    if (txn != null)
      txn.rollback();

    txn = null;
  }

  /**
   * Flushes all modified objects to the triple-store. Usually called automatically on a
   * transaction commit.
   *
   * @throws OtmException on an error
   */
  public void flush() throws OtmException {
    if (txn == null)
      throw new OtmException("No active transaction");

    for (Map.Entry<Id, Object> e : deleteMap.entrySet())
      write(e.getKey(), e.getValue(), true);

    for (Map.Entry<Id, Object> e : dirtyMap.entrySet())
      write(e.getKey(), e.getValue(), false);

    deleteMap.clear();
    cleanMap.putAll(dirtyMap);
    dirtyMap.clear();
  }

  /**
   * Clear persistence state of all objects.
   */
  public void clear() {
    cleanMap.clear();
    dirtyMap.clear();
    deleteMap.clear();
    pristineProxies.clear();
    associations.clear();
  }

  /**
   * Marks an object for storage. All associated objects are stored too.
   *
   * @param o the object to store
   *
   * @return the object id
   *
   * @throws OtmException on an error
   */
  public String saveOrUpdate(Object o) throws OtmException {
    Id id = checkObject(o);
    sync(o, id, false, true, true);

    return id.getId();
  }

  /**
   * Mark an object for deletion from storage. All associated objects are deleted too.
   *
   * @param o the object to delete
   *
   * @return the object id.
   *
   * @throws OtmException on an error
   */
  public String delete(Object o) throws OtmException {
    Id id = checkObject(o);

    if (currentIds.contains(id))
      return id.getId(); // loop

    try {
      currentIds.add(id);

      cleanMap.remove(id);
      dirtyMap.remove(id);
      deleteMap.put(id, o);

      ClassMetadata cm     = sessionFactory.getClassMetadata(o.getClass());
      Set<Wrapper>  assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (p.getSerializer() != null)
          continue;

        for (Object ao : p.get(o))
          assocs.add(new Wrapper(checkObject(ao), ao));
      }

      Set<Wrapper> old = associations.remove(id);

      if (old != null)
        assocs.addAll(old);

      for (Wrapper ao : assocs)
        delete(ao.get());
    } finally {
      currentIds.remove(id);
    }

    return id.getId();
  }

  /**
   * Loads an object from the session or a newly created dynamic proxy for it. Does not hit
   * the triplestore.
   *
   * @param <T> the type of object
   * @param clazz the class of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted from session
   *
   * @throws OtmException on an error
   */
  public <T> T load(Class<T> clazz, String oid) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    Id     id = new Id(clazz, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = newDynamicProxy(clazz, id, checkClass(clazz));
      cleanMap.put(id, o);
    }

    return clazz.cast(o);
  }

  /**
   * Gets an object from the session or from the triple store.
   *
   * @param <T> the type of the object
   * @param clazz the class of the object
   * @param oid the id of the object
   *
   * @return the object or null if deleted or does not exist in store
   *
   * @throws OtmException on an error
   */
  public <T> T get(Class<T> clazz, String oid) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    Id     id = new Id(clazz, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = getFromStore(clazz, id, checkClass(clazz));

      if (o != null)
        sync(o, id, false, false, false);
    }

    return clazz.cast(o);
  }

  /**
   * Merges the given object. The returned object in all cases is an attached object with the
   * state info merged. If the  supplied object is a detached object, it will remain detached even
   * after the call.
   *
   * @param <T> the type of object
   * @param o the detached object
   *
   * @return an attached object with merged values
   *
   * @throws OtmException on an error
   */
  public <T> T merge(T o) throws OtmException {
    Id id = checkObject(o);

    // make sure it is loaded
    T ao = (T) get(o.getClass(), id.getId());

    if (ao == null) {
      // does not exist; so make a copy first
      try {
        ao = (T) o.getClass().newInstance();
      } catch (Exception e) {
        throw new OtmException("instantiation failed", e);
      }

      Map<Id, Object> loopDetect = new HashMap<Id, Object>();
      loopDetect.put(id, ao);
      copy(ao, o, loopDetect); // deep copy
      o = ao;
    }

    ao = (T) sync(o, id, true, true, true);

    return ao;
  }

  /**
   * Refreshes an attached object with values from the database.
   *
   * @param o the attached object to refresh
   *
   * @throws OtmException on an error
   */
  public void refresh(Object o) throws OtmException {
    Id id = checkObject(o);

    if (dirtyMap.containsKey(id) || cleanMap.containsKey(id)) {
      Class clazz = o.getClass();
      o = getFromStore(clazz, id, checkClass(clazz));
      sync(o, id, true, false, false);
    }
  }

  /**
   * Creates the 'criteria' for retrieving a set of objects of a class.
   *
   * @param clazz the class
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(Class clazz) throws OtmException {
    return new Criteria(this, null, null, checkClass(clazz));
  }

  /**
   * Creates a 'sub-criteria' for retrieving a set of objects for an association in a parent
   * class. Usually called by {@link
   * org.topazproject.otm.Criteria#createCriteria(java.lang.String) Criteria#createCriteria}
   *
   * @param criteria the parent class criteria
   * @param path path to the associatyion field
   *
   * @return a newly created Criteria.
   *
   * @throws OtmException on an error
   */
  public Criteria createCriteria(Criteria criteria, String path)
                          throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(path);

    if (m == null)
      throw new OtmException(path + " is not a valid field name for " + cm);

    return new Criteria(this, criteria, m, checkClass(m.getComponentType()));
  }

  /**
   * Gets a list of objects based on a criteria.
   *
   * @param criteria the criteria
   *
   * @return the results list
   *
   * @throws OtmException on an error
   */
  public List list(Criteria criteria) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    flush(); // so that mods are visible to queries

    TripleStore store  = sessionFactory.getTripleStore();

    List        result = new ArrayList();

    for (TripleStore.ResultObject ro : store.list(criteria, txn)) {
      Object io = instantiate(ro);
      Object r  = sync(io, new Id(io.getClass(), ro.id), true, false, false);

      if (r != null)
        result.add(r);
    }

    return result;
  }

  /**
   * Gets the ids for a list of objects.
   *
   * @param objs list of objects
   *
   * @return the list of ids
   *
   * @throws OtmException on an error
   */
  public List<String> getIds(List objs) throws OtmException {
    List<String> results = new ArrayList<String>(objs.size());

    for (Object o : objs)
      results.add(checkObject(o).getId());

    return results;
  }

  private void write(Id id, Object o, boolean delete) throws OtmException {
    boolean       pristineProxy = pristineProxies.contains(id);
    ClassMetadata cm            = sessionFactory.getClassMetadata(o.getClass());
    TripleStore   store         = sessionFactory.getTripleStore();

    if (delete || !pristineProxy) {
      if (log.isDebugEnabled())
        log.debug("deleting from store: " + id);

      store.delete(cm, id.getId(), txn);
    }

    if (!delete && !pristineProxy) {
      if (log.isDebugEnabled())
        log.debug("inserting into store: " + id);

      store.insert(cm, id.getId(), o, txn);
    }
  }

  private Object getFromStore(Class clazz, Id id, ClassMetadata cm)
                       throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    TripleStore              store = sessionFactory.getTripleStore();

    TripleStore.ResultObject ro    = store.get(cm, id.getId(), txn);

    return (ro == null) ? null : instantiate(ro);
  }

  private Object instantiate(TripleStore.ResultObject ro)
                      throws OtmException {
    for (Map.Entry<Mapper, List<String>> e : ro.unresolvedAssocs.entrySet()) {
      List   assocs = new ArrayList();
      Mapper p      = e.getKey();

      for (String val : e.getValue()) {
        // lazy load
        Object a = load(p.getComponentType(), val);

        if (a != null)
          assocs.add(a);
      }

      p.set(ro.o, assocs);
    }

    for (Map.Entry<Mapper, List<TripleStore.ResultObject>> e : ro.resolvedAssocs.entrySet()) {
      List   assocs = new ArrayList();
      Mapper p      = e.getKey();

      for (TripleStore.ResultObject val : e.getValue()) {
        Object io = instantiate(val);
        Object a  = sync(io, new Id(io.getClass(), val.id), true, false, false);

        if (a != null)
          assocs.add(a);
      }

      p.set(ro.o, assocs);
    }

    return ro.o;
  }

  private Object sync(final Object other, final Id id, final boolean merge, final boolean update,
                      final boolean cascade) throws OtmException {
    if (currentIds.contains(id))
      return null; // loop and hence the return value is unused

    Object o = null;

    try {
      currentIds.add(id);
      o = deleteMap.remove(id);

      if (o == null)
        o = dirtyMap.get(id);

      boolean dirtyExists = o != null;

      if (!dirtyExists)
        o = cleanMap.get(id);

      if (merge && (o != null) && (other != o))
        copy(o, other, null); // shallow copy
      else
        o = other;

      if (update || dirtyExists) {
        dirtyMap.put(id, o); // re-put since it may have been deleted
        cleanMap.remove(id);
      } else {
        cleanMap.put(id, o);
      }

      ClassMetadata cm     = checkClass(o.getClass());
      Set<Wrapper>  assocs = new HashSet<Wrapper>();

      for (Mapper p : cm.getFields()) {
        if (p.getSerializer() != null)
          continue;

        for (Object ao : p.get(o)) {
          Id aid = checkObject(ao);

          // note: sync() here will not return a merged object. see copy()
          if (cascade)
            sync(ao, aid, merge, update, cascade);

          assocs.add(new Wrapper(aid, ao));
        }
      }

      Set<Wrapper> old = associations.put(id, assocs);

      if (update && (old != null)) {
        old.removeAll(assocs);

        for (Wrapper ao : old)
          delete(ao.get());
      }
    } finally {
      currentIds.remove(id);
    }

    return o;
  }

  private Object copy(Object o, Object other, Map<Id, Object> loopDetect)
               throws OtmException {
    ClassMetadata ocm = checkClass(other.getClass());
    ClassMetadata cm  = checkClass(o.getClass());

    if (!cm.getSourceClass().isAssignableFrom(ocm.getSourceClass()))
      throw new OtmException(cm.toString() + " is not assignable from " + ocm);

    for (Mapper p : cm.getFields()) {
      Mapper op = ocm.getMapperByUri(p.getUri(), p.hasInverseUri());

      if (op == null)
        continue;

      if ((loopDetect == null) || (p.getSerializer() != null))
        p.set(o, op.get(other));
      else {
        List cc = new ArrayList();

        for (Object ao : op.get(other)) {
          Id     id  = checkObject(ao);
          Object aoc = loopDetect.get(id);

          if (aoc == null) {
            try {
              aoc = ao.getClass().newInstance();
            } catch (Exception e) {
              throw new OtmException("instantiation failed", e);
            }

            loopDetect.put(id, aoc);
            copy(aoc, ao, loopDetect);
          }

          cc.add(aoc);
        }

        p.set(o, cc);
      }
    }

    return o;
  }

  private Object newDynamicProxy(final Class clazz, final Id id, final ClassMetadata cm)
                          throws OtmException {
    MethodHandler mi =
      new MethodHandler() {
        private Object loaded = null;

        public Object invoke(Object self, Method m, Method proceed, Object[] args)
                      throws Throwable {
          if (loaded == null) {
            log.info(m.getName() + " on " + id + " is forcing a load from store");
            loaded = getFromStore(clazz, id, cm); // xxx: associations? sub-classes?
            pristineProxies.remove(id);
          }

          return m.invoke(loaded, args);
        }
      };

    try {
      Object o = sessionFactory.getProxyMapping(clazz).newInstance();
      cm.getIdField().set(o, Collections.singletonList(id.getId()));
      ((ProxyObject) o).setHandler(mi);
      pristineProxies.add(id);

      return o;
    } catch (Exception e) {
      throw new OtmException("Dynamic proxy instantiation failed", e);
    }
  }

  private Id checkObject(Object o) throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    ClassMetadata cm      = checkClass(o.getClass());

    Mapper        idField = cm.getIdField();
    List          ids     = idField.get(o);

    if (ids.size() == 0)
      throw new OtmException("No id generation support yet " + o.getClass());

    return new Id(o.getClass(), (String) ids.get(0));
  }

  private ClassMetadata checkClass(Class clazz) throws OtmException {
    ClassMetadata cm = sessionFactory.getClassMetadata(clazz);

    if (cm == null) {
      Class c = sessionFactory.getProxyMapping(clazz);

      if (c != null)
        cm = sessionFactory.getClassMetadata(c);

      if (cm == null)
        throw new OtmException("No class metadata found for " + clazz);
    }

    return cm;
  }

  // For use in sets where we want id equality rather than object equality.
  // eg. associations that are yet to be loaded from the triplestore
  private static class Wrapper {
    private Id     id;
    private Object o;

    public Wrapper(Id id, Object o) {
      this.id   = id;
      this.o    = o;
    }

    public Object get() {
      return o;
    }

    public Id getId() {
      return id;
    }

    public int hashCode() {
      return id.hashCode();
    }

    public boolean equals(Object other) {
      return (other instanceof Wrapper) ? id.equals(((Wrapper) other).id) : false;
    }
  }

  private static class Id {
    private String id;
    private Class  clazz;

    public Id(Class clazz, String id) {
      this.id      = id;
      this.clazz   = clazz;
    }

    public String getId() {
      return id;
    }

    public Class getClazz() {
      return clazz;
    }

    public int hashCode() {
      return id.hashCode();
    }

    public boolean equals(Object other) {
      if (!(other instanceof Id))
        return false;

      Id o = (Id) other;

      return id.equals(o.id)
              && (clazz.isAssignableFrom(o.clazz) || o.clazz.isAssignableFrom(clazz));
    }

    public String toString() {
      return id;
    }
  }
}
