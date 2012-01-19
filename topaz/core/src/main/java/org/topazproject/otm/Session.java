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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Mapper.CascadeType;
import org.topazproject.otm.query.Results;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public class Session {
  private static final Log                     log            = LogFactory.getLog(Session.class);
  private final SessionFactory                 sessionFactory;
  private       Transaction                    txn            = null;
  private final Map<Id, Object>                cleanMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                dirtyMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                deleteMap      = new HashMap<Id, Object>();
  private final Map<Id, LazyLoadMethodHandler> proxies        = new HashMap<Id, LazyLoadMethodHandler>();
  private final Map<Id, Set<Wrapper>>          associations   = new HashMap<Id, Set<Wrapper>>();
  private final Set<Id>                        currentIds     = new HashSet<Id>();
  private final Map<String, Filter>            filters        = new HashMap<String, Filter>();

  /**
   * Empty constructor for spring scoped proxy. The resulting session instance is not usable
   * for anything but as a proxy, as the session-factory is null.
   */
  protected Session() {
    sessionFactory = null;
  }

  /**
   * Creates a new Session object.
   *
   * @param sessionFactory the session factory that created this session
   */
  Session(SessionFactory sessionFactory) {
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
    else
      throw new OtmException("A transaction is already active on this session");

    return txn;
  }

  /**
   * Clears the txn when committed or rolled back.
   */
  void endTransaction() {
    txn = null;
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

    for (Object o : dirtyMap.values())
      if (o instanceof PreInsertEventListener)
        ((PreInsertEventListener)o).onPreInsert(this, o);

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
    proxies.clear();
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
    sync(o, id, false, true, CascadeType.saveOrUpdate);

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
        if ((p.getSerializer() != null) || (p.getUri() == null))
          continue;

        // ignore this association if delete does not cascade
        if (!p.isCascadable(CascadeType.delete))
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

    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to load() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
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
    return get(clazz, oid, true);
  }

  /**
   * Internal method. DO NOT USE.
   *
   * @see #get(java.lang.Class, java.lang.String)
   */
  public <T> T get(Class<T> clazz, String oid, boolean filterObj) throws OtmException {
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
      o = getFromStore(id, checkClass(clazz), null, filterObj);

      if (o != null) {
        // Must merge here. Associations may have a back-pointer to this Id.
        // If those associations are loaded from the store even before
        // we complete this get() operation, there will be an instance
        // in our cache for this same Id.
        o = sync(o, id, true, false, null);
      }
    }

    if (o instanceof ProxyObject)
        o.equals(null); // ensure it is loaded

    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to get() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
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

    ao = (T) sync(o, id, true, true, CascadeType.merge);

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
      o = getFromStore(id, checkClass(o.getClass()), o, true);
      sync(o, id, true, false, CascadeType.refresh);
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
    return new Criteria(this, null, null, checkClass(clazz),
                        new ArrayList<Filter>(filters.values()));
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

    return new Criteria(this, criteria, m, checkClass(m.getComponentType()),
                        new ArrayList<Filter>(filters.values()));
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

    TripleStore store = sessionFactory.getTripleStore();

    return store.list(criteria, txn);
  }

  /**
   * Create an OQL query.
   *
   * @param query the OQL query
   * @return the query object
   */
  public Query createQuery(String query) throws OtmException {
    return new Query(this, query, new ArrayList<Filter>(filters.values()));
  }

  /**
   * Execute a native(ITQL, SPARQL etc.) query.
   *
   * @param query the native query
   * @return the results object
   * @throws OtmException on an error
   */
  public Results doNativeQuery(String query) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.doNativeQuery(query, txn);
  }

  /**
   * Execute a native(ITQL, SPARQL etc.) update.
   *
   * @param command the native command(s) to execute
   * @throws OtmException on an error
   */
  public void doNativeUpdate(String command) throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    flush(); // so that ordering is preserved

    TripleStore store = sessionFactory.getTripleStore();
    store.doNativeUpdate(command, txn);
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

  /** 
   * Enable the named filter. This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to enable
   * @return the enabled filter, or null if no filter definition can be found.
   * @throws OtmException on an error
   */
  public Filter enableFilter(String name) throws OtmException {
    if (filters.containsKey(name))
      return filters.get(name);

    FilterDefinition fd = sessionFactory.getFilterDefinition(name);
    if (fd == null)
      return null;

    Filter f = fd.createFilter(this);
    filters.put(f.getName(), f);
    return f;
  }

  /** 
   * Enable the filter defined by the specified filter-definition. Unlike {@link
   * #enableFilter(java.lang.String) enableFilter(String)} this filter-definition does not have to
   * be pre-registered on the session-factory. The filter can be disabled by name, just like with
   * filters from pre-registered filter-definitions.
   *
   * <p>This does not affect existing queries or criteria.
   * 
   * @param fd the filter-definition whose filter to enable
   * @return the enabled filter
   * @throws OtmException if a filter-definition with the same name has been registered with the
   *                      session-factory or a filter with the same name is already enabled
   */
  public Filter enableFilter(FilterDefinition fd) throws OtmException {
    if (sessionFactory.getFilterDefinition(fd.getFilterName()) != null)
      throw new OtmException("a filter with the name '" + fd.getFilterName() +
                             "' is registered with the session-factory");
    if (filters.containsKey(fd.getFilterName()))
      throw new OtmException("a filter with the name '" + fd.getFilterName() +
                             "' has already been enabled");

    Filter f = fd.createFilter(this);
    filters.put(f.getName(), f);
    return f;
  }

  /** 
   * Disable the named filter. This does nothing if no filter by the given name has been enabled.
   * This does not affect existing queries or criteria.
   * 
   * @param name the name of the filter to disable
   */
  public void disableFilter(String name) throws OtmException {
    filters.remove(name);
  }

  /** 
   * Get the set of enabled filters' names. 
   * 
   * @return the names of the enabled filters
   */
  public Set<String> listFilters() {
    return new HashSet<String>(filters.keySet());
  }

  private void write(Id id, Object o, boolean delete) throws OtmException {
    LazyLoadMethodHandler llm           = (o instanceof ProxyObject) ? proxies.get(id) : null;
    boolean               pristineProxy = (llm != null) ? !llm.isLoaded() : false;
    ClassMetadata         cm            = sessionFactory.getClassMetadata(o.getClass());
    TripleStore           store         = sessionFactory.getTripleStore();

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

  private Object getFromStore(Id id, ClassMetadata cm, Object instance, boolean filterObj)
                       throws OtmException {
    if (txn == null)
      throw new OtmException("No transaction active");

    TripleStore store = sessionFactory.getTripleStore();

    instance = store.get(cm, id.getId(), instance, txn, 
                                new ArrayList<Filter>(filters.values()), filterObj);

    if (instance instanceof PostLoadEventListener)
      ((PostLoadEventListener)instance).onPostLoad(this, instance);

    return instance;
  }

  private Object sync(final Object other, final Id id, final boolean merge, final boolean update,
                      final CascadeType cascade) throws OtmException {
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
        if ((p.getSerializer() != null) || (p.getUri() == null))
          continue;

        if (log.isDebugEnabled())
          log.debug("Checking object '" + cm.getName() + "' field '" + p.getName() +
                    "' literal type '" + p.getDataType() + "' rdf type '" + p.getRdfType() + "'");
        boolean deep = ((cascade != null) && p.isCascadable(cascade));
        boolean deepDelete = p.isCascadable(CascadeType.delete);
        for (Object ao : p.get(o)) {
          Id aid = checkObject(ao);

          // note: sync() here will not return a merged object. see copy()
          if (deep)
            sync(ao, aid, merge, update, cascade);
          if (deepDelete)
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

    if (log.isDebugEnabled())
      log.debug("Copy merging " + checkObject(o));

    for (Mapper p : cm.getFields()) {
      Mapper op = ocm.getMapperByUri(p.getUri(), p.hasInverseUri(), p.getRdfType());

      if (op == null)
        continue;

      if ((loopDetect == null) || (p.getSerializer() != null) || (p.getUri() == null))
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
    LazyLoadMethodHandler mi =
      new LazyLoadMethodHandler() {
        private boolean loaded = false;
        private boolean loading = false;

        public Object invoke(Object self, Method m, Method proceed, Object[] args)
                      throws Throwable {
          if (!loaded && !loading) {
            if (log.isDebugEnabled())
              log.debug(m.getName() + " on " + id + " is forcing a load from store");

            loading = true;
            try {
              getFromStore(id, cm, self, false);
            } finally {
              loading = false;
            }
            loaded = true;
          }

          try {
            return proceed.invoke(self, args);
          } catch (InvocationTargetException ite) {
            if (log.isDebugEnabled())
              log.debug("Caught ite while invoking '" + proceed + "' on '" + self + "'", ite);
            throw ite.getCause();
          }
        }

        public boolean isLoaded() {
          return loaded;
        }

      };

    try {
      Object o = sessionFactory.getProxyMapping(clazz).newInstance();
      cm.getIdField().set(o, Collections.singletonList(id.getId()));
      ((ProxyObject) o).setHandler(mi);
      proxies.put(id, mi);

      if (log.isDebugEnabled())
        log.debug("Dynamic proxy created for " + id);

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

    if (idField == null)
      throw new OtmException("Must have an id field for " + o.getClass());

    List          ids     = idField.get(o);
    String        id      = null;

    if (ids.size() == 0) {
      IdentifierGenerator generator = idField.getGenerator();

      if (generator == null)
        throw new OtmException("No id generation for " + o.getClass() + " on " + idField);

      id = generator.generate();
      idField.set(o, Collections.singletonList(id)); // So user can get at it after saving

      if (log.isDebugEnabled())
        log.debug(generator.getClass().getSimpleName() + " generated '" + id + "' for "
                  + o.getClass().getSimpleName());
    } else {
      id = (String) ids.get(0);
    }

    return new Id(o.getClass(), id);
  }

  private ClassMetadata checkClass(Class clazz) throws OtmException {
    ClassMetadata cm = sessionFactory.getClassMetadata(clazz);

    if (cm == null)
      throw new OtmException("No class metadata found for " + clazz);

    if (cm.getModel() == null)
      throw new OtmException("No graph/model found for " + clazz);

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

  private interface LazyLoadMethodHandler extends MethodHandler {
    public boolean isLoaded();
  }
}
