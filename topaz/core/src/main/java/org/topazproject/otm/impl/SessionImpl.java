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
package org.topazproject.otm.impl;

import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
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
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.VarMapper;
import org.topazproject.otm.mapping.java.FieldBinder;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.Rdf;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
public class SessionImpl extends AbstractSession {
  private static final Log                     log            = LogFactory.getLog(SessionImpl.class);
  private final Map<Id, Object>                cleanMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                dirtyMap       = new HashMap<Id, Object>();
  private final Map<Id, Object>                deleteMap      = new HashMap<Id, Object>();
  private final Map<Id, LazyLoadMethodHandler> proxies        = new HashMap<Id, LazyLoadMethodHandler>();
  private final Map<Id, Set<Wrapper>>          orphanTrack    = new HashMap<Id, Set<Wrapper>>();
  private final Set<Id>                        currentIds     = new HashSet<Id>();
  private boolean flushing                                    = false;

  private static final StateCache              states         = new StateCache() {
    public void insert(Object o, ClassMetadata cm, Session session) throws OtmException {
      synchronized(this) {
        super.insert(o, cm, session);
      }
    }
    public Interceptor.Updates update(Object o, ClassMetadata cm, Session session)
        throws OtmException {
      synchronized(this) {
        return super.update(o, cm, session);
      }
    }
    public void remove(Object o) {
      synchronized(this) {
        super.remove(o);
      }
    }
  };

  /**
   * Creates a new SessionImpl object.
   *
   * @param sessionFactory the session factory that created this session
   */
  SessionImpl(SessionFactoryImpl sessionFactory) {
    this(sessionFactory, null);
  }

  /**
   * Creates a new SessionImpl object.
   *
   * @param sessionFactory the session factory that created this session
   * @param interceptor    the interceptor that listens for object state changes
   */
  SessionImpl(SessionFactoryImpl sessionFactory, Interceptor interceptor) {
    super(sessionFactory, interceptor);
  }

  /*
   * inherited javadoc
   */
  public EntityMode getEntityMode() {
    return EntityMode.POJO;
  }

  /*
   * inherited javadoc
   */
  public void flush() throws OtmException {
    if (!flushing) {
      try {
        // prevent re-entrant flushing. (Occurs if interceptor attempts a query)
        flushing = true;
        doFlush();
      } finally {
        flushing = false;
      }
    }

    if (tsCon != null)
      sessionFactory.getTripleStore().flush(tsCon);
    if (bsCon != null)
      sessionFactory.getBlobStore().flush(bsCon);
  }

  private void doFlush() throws OtmException {
    // auto-save objects that aren't explicitly saved
    for (Id id : new HashSet<Id>(cleanMap.keySet())) {
      Object o = cleanMap.get(id);
      if (o != null) {
        ClassMetadata cm = id.getClassMetadata();
        cm = sessionFactory.getInstanceMetadata(cm, getEntityMode(), o);
        if (!cm.isView())
          sync(o, id, false, true, CascadeType.saveOrUpdate, true);
      }
    }

    do {
      Map<Id, Object> workDelete = new HashMap<Id, Object>(deleteMap);
      Map<Id, Object> workDirty = new HashMap<Id, Object>(dirtyMap);

      for (Map.Entry<Id, Object> e : workDelete.entrySet())
        write(e.getKey(), e.getValue(), true);

      for (Object o : workDirty.values())
        if (o instanceof PreInsertEventListener)
          ((PreInsertEventListener)o).onPreInsert(this, o);

      for (Map.Entry<Id, Object> e : workDirty.entrySet())
        write(e.getKey(), e.getValue(), false);

      deleteMap.keySet().removeAll(workDelete.keySet());
      dirtyMap.keySet().removeAll(workDirty.keySet());
      cleanMap.putAll(workDirty);
    } while ((deleteMap.size() > 0) || (dirtyMap.size() > 0));
  }

  /*
   * inherited javadoc
   */
  public void clear() {
    cleanMap.clear();
    dirtyMap.clear();
    deleteMap.clear();
    proxies.clear();
    orphanTrack.clear();
  }

  /*
   * inherited javadoc
   */
  public String saveOrUpdate(Object o) throws OtmException {
    Id id = checkObject(null, o, true, true);
    sync(o, id, false, true, CascadeType.saveOrUpdate, true);

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public String delete(Object o) throws OtmException {
    Id id = checkObject(null, o, true, true);

    if (currentIds.contains(id))
      return id.getId(); // loop

    try {
      currentIds.add(id);

      ClassMetadata cm     = id.getClassMetadata();
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (RdfMapper m : cm.getRdfMappers()) {
        if (!m.isAssociation())
          continue;

        // ignore this association if delete does not cascade
        if (!m.isCascadable(CascadeType.delete))
          continue;

        Binder b = m.getBinder(getEntityMode());
        for (Object ao : b.get(o))
          assocs.add(new Wrapper(checkObject(m, ao, true, true), ao));
      }

      Set<Wrapper> old = orphanTrack.remove(id);

      if (old != null)
        assocs.addAll(old);

      for (Wrapper ao : assocs)
        delete(ao.get());

      cleanMap.remove(id);
      dirtyMap.remove(id);
      deleteMap.put(id, o);
    } finally {
      currentIds.remove(id);
    }

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public String evict(Object o) throws OtmException {
    final Id id = checkObject(null, o, true, true);

    if (currentIds.contains(id))
      return id.getId(); // loop

    try {
      currentIds.add(id);

      cleanMap.remove(id);
      dirtyMap.remove(id);
      proxies.remove(id);

      ClassMetadata cm     = id.getClassMetadata();
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (RdfMapper m : cm.getRdfMappers()) {
        if (!m.isAssociation())
          continue;

        // ignore this association if evict does not cascade
        if (!m.isCascadable(CascadeType.evict))
          continue;

        Binder b = m.getBinder(getEntityMode());
        for (Object ao : b.get(o))
          assocs.add(new Wrapper(checkObject(m, ao, true, true), ao));
      }

      for (Wrapper ao : assocs)
        evict(ao.get());
    } finally {
      currentIds.remove(id);
    }

    return id.getId();
  }

  /*
   * inherited javadoc
   */
  public boolean contains(Object o) throws OtmException {
    final Id id = checkObject(null, o, true, false);

    return (cleanMap.get(id) == o) || (dirtyMap.get(id) == o);
  }

  /*
   * inherited javadoc
   */
  public <T> T load(Class<T> clazz, String oid) throws OtmException {
    Object o = load(checkClass(clazz), oid);
    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to load() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
  }

  /*
   * inherited javadoc
   */
  public Object load(String entity, String oid) throws OtmException {
    return load(checkClass(entity), oid);
  }

  /*
   * inherited javadoc
   */
  public Object load(ClassMetadata cm, String oid) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    Id     id = new Id(cm, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = newDynamicProxy(id);
      cleanMap.put(id, o);
    }
    return o;

  }

  /*
   * inherited javadoc
   */
  public <T> T get(Class<T> clazz, String oid) throws OtmException {
    Object o = get(checkClass(clazz), oid, true);

    if ((o == null) || clazz.isInstance(o))
      return clazz.cast(o);

    throw new OtmException("something wrong: asked to get() <" + oid + "> and map to class '"
                           + clazz + "' but what we ended up with is an instance of '"
                           + o.getClass());
  }

  /*
   * inherited javadoc
   */
  public Object get(String entity, String oid) throws OtmException {
    return get(checkClass(entity), oid, true);
  }

  /*
   * inherited javadoc
   */
  public Object get(ClassMetadata cm, String oid, boolean filterObj) throws OtmException {
    if ((oid == null) || oid.equals(Rdf.rdf + "nil"))
      return null;

    checkClass(cm, oid);
    Id     id = new Id(cm, oid);
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = getFromStore(id, null, filterObj);

      if (o != null) {
        // Must merge here. Associations may have a back-pointer to this Id.
        // If those associations are loaded from the store even before
        // we complete this get() operation, there will be an instance
        // in our cache for this same Id.
        if (!cm.isView())
          o = sync(o, id, true, false, null, true);
      }
    }

    if (o instanceof ProxyObject)
        o.equals(null); // ensure it is loaded

    return o;
  }

  /*
   * inherited javadoc
   */
  public void delayedLoadComplete(Object o, RdfMapper field) throws OtmException {
    Id id = checkObject(null, o, true, false);

    if (log.isDebugEnabled())
      log.debug("loaded lazy loaded field '" + field.getName() + "' on " + id, 
          trimStackTrace(new Throwable("trimmed-stack-trace"), 3, 8));

    if (deleteMap.get(id) != null)
      return;

    states.delayedLoadComplete(o, field, this);

    if (!field.isCascadable(CascadeType.deleteOrphan))
      return;

    Set<Wrapper> assocs = orphanTrack.get(id);
    if (assocs == null)
      orphanTrack.put(id, assocs = new HashSet<Wrapper>());

    Binder b = field.getBinder(getEntityMode());
    for (Object ao : b.get(o)) {
      Id aid = checkObject(field, ao, true, false);
      assocs.add(new Wrapper(aid, ao));
    }
  }

  /*
   * inherited javadoc
   */
  public <T> T merge(T o) throws OtmException {
    Id id = checkObject(null, o, true, false);

    // make sure it is loaded
    ClassMetadata cm = id.getClassMetadata();
    T ao = (T) get(cm, id.getId(), true);

    if (ao == null) {
      // does not exist; so make a copy first
      ao = (T) cm.getEntityBinder(getEntityMode()).newInstance();

      Map<Id, Object> loopDetect = new HashMap<Id, Object>();
      loopDetect.put(id, ao);
      copy(id, ao, o, loopDetect); // deep copy
      o = ao;
    }

    ao = (T) sync(o, id, true, true, CascadeType.merge, false);

    return ao;
  }

  /*
   * inherited javadoc
   */
  public void refresh(Object o) throws OtmException {
    Id id = checkObject(null, o, false, true);

    if (dirtyMap.containsKey(id) || cleanMap.containsKey(id)) {
      o = getFromStore(id, o, true);
      sync(o, id, true, false, CascadeType.refresh, true);
    }
  }

  private <T> boolean isPristineProxy(Id id, T o) {
    LazyLoadMethodHandler llm           = (o instanceof ProxyObject) ? proxies.get(id) : null;
    return (llm != null) ? !llm.isLoaded() : false;
  }

  private <T> void write(Id id, T o, boolean delete) throws OtmException {
    ClassMetadata         cm            = sessionFactory.getInstanceMetadata(id.getClassMetadata(), 
                                                                               getEntityMode(), o);
    TripleStore           store         = sessionFactory.getTripleStore();
    BlobStore             bs            = sessionFactory.getBlobStore();
    boolean               bf            = (cm.getBlobField() != null);
    boolean               tp            = (cm.getRdfMappers().size() + cm.getTypes().size()) > 0;

    if (delete) {
      if (log.isDebugEnabled())
        log.debug("deleting from store: " + id);

     states.remove(o);
     if (bf)
       bs.delete(cm, id.getId(), o, getBlobStoreCon());
     if (tp)
       store.delete(cm, cm.getRdfMappers(), id.getId(), o, getTripleStoreCon());
     if (interceptor != null)
       interceptor.onPostDelete(this, cm, id.getId(), o);
    } else if (isPristineProxy(id, o)) {
      if (log.isDebugEnabled())
        log.debug("Update skipped for " + id + ". This is a proxy object and is not even loaded.");
    } else {
      Interceptor.Updates updates = states.update(o, cm, this);
      Collection<RdfMapper> fields;
      boolean firstTime = (updates == null);
      if (firstTime)
        fields = cm.getRdfMappers();
      else
        fields = updates.pmapChanged ? cm.getRdfMappers() : updates.rdfMappers;

      int nFields = fields.size();
      if (log.isDebugEnabled()) {
        if (firstTime)
          log.debug("Saving " + id + " to store.");
        else if (nFields == cm.getRdfMappers().size())
            log.debug("Full update for " + id + ".");
        else if (nFields == 0)
          log.debug("Update skipped for " + id + ". No changes to the object state.");
        else {
          Collection<RdfMapper> skips = new ArrayList(cm.getRdfMappers());
          skips.removeAll(fields);
          StringBuilder buf = new StringBuilder(100);
          char sep = ' ';
          for (RdfMapper m : skips) {
            buf.append(sep).append(m.getName());
            sep = ',';
          }
          log.debug("Partial update for " + id + ". Skipped:" + buf);
        }
      }

      boolean wroteSomething = false;
      if (tp && (firstTime || (nFields > 0))) {
        store.delete(cm, fields, id.getId(), o, getTripleStoreCon());
        store.insert(cm, fields, id.getId(), o, getTripleStoreCon());
        wroteSomething = true;
      }
      if (bf) {
        switch(states.digestUpdate(o, cm, this)) {
        case delete:
          bs.delete(cm, id.getId(), o, getBlobStoreCon());
          break;
        case update:
          bs.delete(cm, id.getId(), o, getBlobStoreCon());
        case insert:
          bs.insert(cm, id.getId(), o, getBlobStoreCon());
          break;
        case noChange:
        default:
          bf = false;
          break;
        }
        if (updates != null)
          updates.blobChanged = bf;
        if (bf)
          wroteSomething = true;
      }
      if ((interceptor != null) && wroteSomething)
        interceptor.onPostWrite(this, cm, id.getId(), o, updates);
    }
  }

  private Object getFromStore(Id id, Object instance, boolean filterObj)
                       throws OtmException {
    TripleStore store = sessionFactory.getTripleStore();
    ClassMetadata cm = id.getClassMetadata();

    boolean found = false;
    if (interceptor != null) {
      Object val = interceptor.getEntity(this, cm, id.getId(), instance);
      if (found = (val != null)) {
        instance = (val == Interceptor.NULL) ? null : val;
        if (instance != null)
          cm = sessionFactory.getInstanceMetadata(cm, getEntityMode(), instance);
      }
    }

    if (!found) {
      if (cm.isView())
        instance = loadView(cm, id.getId(), instance);
      else {
        if ((cm.getTypes().size() + cm.getRdfMappers().size()) > 0) {
          instance = store.get(cm, id.getId(), instance, getTripleStoreCon(), 
                                  new ArrayList<Filter>(filters.values()), filterObj);
          if (instance != null)
            cm = sessionFactory.getInstanceMetadata(cm, getEntityMode(), instance);
        }
        if (cm.getBlobField() != null)
          instance = sessionFactory.getBlobStore().get(cm, id.getId(), instance, getBlobStoreCon());
      }

      if (interceptor != null)
        interceptor.onPostRead(this, cm, id.getId(), instance);
    }

    if (instance == null) {
      if (log.isDebugEnabled())
        log.debug("Object not found in store: " + id.getId());
      return null;
    }

    if (!cm.isView()) {
      for (RdfMapper m : cm.getRdfMappers())
        if (m.getFetchType() == FetchType.eager) {
          Binder b = m.getBinder(getEntityMode());
          for (Object o : b.get(instance))
            if (o != null)
              o.equals(null);
        }
    }

    if (instance instanceof PostLoadEventListener)
      ((PostLoadEventListener)instance).onPostLoad(this, instance);

    if (!cm.isView()) {
      states.insert(instance, cm, this);
      if (cm.getBlobField() != null)
        states.digestUpdate(instance, cm, this);
    }

    if (log.isDebugEnabled())
      log.debug("Object loaded from " + (found ? "interceptor: " : "store: ") + id.getId());
    return instance;
  }

  private Object loadView(ClassMetadata cm, String id, Object instance) throws OtmException {
    Query q = createQuery(cm.getQuery());
    Set<String> paramNames = q.getParameterNames();
    if (paramNames.size() != 1 || !"id".equals(paramNames.iterator().next()))
      throw new OtmException("View queries must have exactly one parameter, and the parameter " +
                             "name must be 'id'; entity='" + cm.getName() +
                             "', query='" + cm.getQuery() + "'");

    Results r = q.setParameter("id", id).execute();

    if (r.next())
      instance = createInstance(cm, instance, id, r);
    else
      instance = null;

    r.close();

    return instance;
  }

  private Object createInstance(ClassMetadata cm, Object obj, String id, Results r)
      throws OtmException {
    if (obj == null)
      obj = cm.getEntityBinder(getEntityMode()).newInstance();

    if (id != null && cm.getIdField() != null)
      cm.getIdField().getBinder(getEntityMode()).set(obj, Collections.singletonList(id));

    // a proj-var may appear multiple times, so have to delay closing of subquery results
    Set<Results> sqr = new HashSet<Results>();

    for (VarMapper m : cm.getVarMappers()) {
      int    idx = r.findVariable(m.getProjectionVar());
      // XXX: Other EntityModes
      Object val = getValue(r, idx, ((FieldBinder)m.getBinder(getEntityMode())).getComponentType(),
                               m.getFetchType() == FetchType.eager, sqr);
      Binder b = m.getBinder(getEntityMode());
      if (val instanceof List)
        b.set(obj, (List) val);
      else
        b.setRawValue(obj, val);
    }

    for (Results sr : sqr)
      sr.close();

    return obj;
  }

  private Object getValue(Results r, int idx, Class<?> type, boolean eager, Set<Results> sqr)
      throws OtmException {
    switch (r.getType(idx)) {
      case CLASS:
        return (type == String.class) ? r.getString(idx) : r.get(idx, eager);

      case LITERAL:
        return (type == String.class) ? r.getString(idx) : r.getLiteralAs(idx, type);

      case URI:
        return (type == String.class) ? r.getString(idx) : r.getURIAs(idx, type);

      case SUBQ_RESULTS:
        ClassMetadata scm = sessionFactory.getClassMetadata(type);
        boolean isSubView = scm != null && !scm.isView() && !scm.isPersistable();

        List<Object> vals = new ArrayList<Object>();

        Results sr = r.getSubQueryResults(idx);

        sr.beforeFirst();
        while (sr.next())
          vals.add(isSubView ? createInstance(scm, null, null, sr) :
                               getValue(sr, 0, type, eager, null));

        if (sqr != null)
          sqr.add(sr);
        else
          sr.close();

        return vals;

      default:
        throw new Error("unknown type " + r.getType(idx) + " encountered");
    }
  }

  /**
   * Synchronize the Session Cache entries with the current state of the object; either
   * loaded from the database or supplied by the application.
   *
   * @param other an object that may or may not be known to the session and may even be 
   *              a duplicate instance (same Id, different instance)
   * @param id    the object id
   * @param merge the 'other' object needs to be merged to existing instance in Session 
   * @param update this operation is performed because of an update by the application
   *               (as opposed to a load from the store) 
   * @param cascade the applicable cascade control (or 'null' for certain internal operations)
   * @param skipProxy whether to avoid the force-loading of proxy object
   *
   * @return the object from Session Cache.
   */
  private Object sync(final Object other, final Id id, final boolean merge, final boolean update,
                      final CascadeType cascade, final boolean skipProxy) throws OtmException {
    if (currentIds.contains(id))
      return null; // loop and hence the return value is unused

    Object o = null;

    try {
      currentIds.add(id);
      o = deleteMap.get(id);

      if (o != null)
        throw new OtmException("Attempt to use a deleted object. Remove all references to <" 
                                + id + "> from all 'Persisted' objects");

      o = dirtyMap.get(id);

      boolean dirtyExists = o != null;

      if (!dirtyExists)
        o = cleanMap.get(id);

      if (merge && (o != null) && (other != o))
        copy(id, o, other, null); // shallow copy
      else
        o = other;

      if (update || dirtyExists) {
        dirtyMap.put(id, o);
        cleanMap.remove(id);
      } else {
        cleanMap.put(id, o);
      }

      // avoid loading lazy loaded objects
      if (skipProxy && isPristineProxy(id, o))
        return o;

      ClassMetadata cm    = sessionFactory.getInstanceMetadata(id.getClassMetadata(),
                                              getEntityMode(), o);
      Set<Wrapper> assocs = new HashSet<Wrapper>();

      for (RdfMapper m : cm.getRdfMappers()) {
        Binder b = m.getBinder(getEntityMode());
        if (!m.isAssociation() || (m.getUri() == null))
          continue;

        if (skipProxy && !b.isLoaded(o))
          continue;

        boolean deep = ((cascade != null) && m.isCascadable(cascade));
        boolean deepDelete = m.isCascadable(CascadeType.deleteOrphan);
        for (Object ao : b.get(o)) {
          // Note: duplicate check is not performed when merging
          Id aid = checkObject(m, ao, update, !merge);

          // Note: sync() here will not return a merged object. see copy()
          if (deep)
            sync(ao, aid, merge, update, cascade, skipProxy);
          if (deepDelete)
            assocs.add(new Wrapper(aid, ao));
        }
      }

      // Note: We do this unconditionally. ie. update==false or update==true
      // We do the orphan deletes only when update==true. update==false is
      // used in cases where a fresh load from the database was performed
      // and therefore discarding the previous orphanTrack entries is just fine.
      Set<Wrapper> old = orphanTrack.put(id, assocs);

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

  private Object copy(Id id, Object o, Object other, Map<Id, Object> loopDetect)
               throws OtmException {
    ClassMetadata ocm = sessionFactory.getInstanceMetadata(id.getClassMetadata(),
                            getEntityMode(), other);
    ClassMetadata cm  = sessionFactory.getInstanceMetadata(id.getClassMetadata(),
                            getEntityMode(), other);

    if (!cm.isAssignableFrom(ocm))
      throw new OtmException(cm.toString() + " is not assignable from " + ocm);

    log.warn ("Copy(" + ((loopDetect==null) ? "shallow" : "deep") + ") merging " + id);

    for (RdfMapper m : cm.getRdfMappers()) {
      Mapper p = ocm.getMapperByName(m.getName());

      if (!(p instanceof RdfMapper))
        continue;

      RdfMapper om = (RdfMapper)p;
      Binder b  = m.getBinder(getEntityMode());
      Binder ob = om.getBinder(getEntityMode());
      if ((loopDetect == null) || !m.isAssociation())
        b.set(o, ob.get(other));
      else {
        List cc = new ArrayList();

        for (Object ao : ob.get(other)) {
          Id     aid  = checkObject(om, ao, false, false);
          Object aoc  = loopDetect.get(aid);

          if (aoc == null) {
            aoc = aid.getClassMetadata().getEntityBinder(getEntityMode()).newInstance();

            loopDetect.put(aid, aoc);
            copy(aid, aoc, ao, loopDetect);
          }

          cc.add(aoc);
        }

        b.set(o, cc);
      }
    }

    return o;
  }

  private Object newDynamicProxy(final Id id)
                          throws OtmException {
    LazyLoadMethodHandler mi =
      new LazyLoadMethodHandler() {
        private boolean loaded = false;
        private boolean loading = false;

        public Object invoke(Object self, Method m, Method proceed, Object[] args)
                      throws Throwable {
          if (!loaded && !loading) {
            if (log.isDebugEnabled())
              log.debug(m.getName() + " on " + id + " is forcing a load from store",
                   trimStackTrace(new Throwable("trimmed-stack-trace"), 3, 8));

            loading = true;
            try {
              getFromStore(id, self, false);
            } finally {
              loading = false;
            }
            loaded = true;
          }

          try {
            if (m.getName().equals("writeReplace") && (self instanceof Serializable) 
                && (args.length == 0))
              return getSerializableReplacement(self);
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

        private Object getSerializableReplacement(Object o) throws Throwable {
          ClassMetadata cm = id.getClassMetadata();
          Object rep = cm.getEntityBinder(getEntityMode()).newInstance();
          for (Mapper m : new Mapper[] {cm.getIdField(), cm.getBlobField()})
            if (m != null) {
              Binder b = m.getBinder(getEntityMode());
              b.setRawValue(rep, b.getRawValue(o, false));
            }
          for (Mapper m : cm.getRdfMappers()) {
            Binder b = m.getBinder(getEntityMode());
            b.setRawValue(rep, b.getRawValue(o, false));
          }

          if (log.isDebugEnabled())
            log.debug("Serializable replacement created for " + o);

          return rep;
        }
      };

    try {
      ClassMetadata cm = id.getClassMetadata();
      ProxyObject o = cm.getEntityBinder(getEntityMode()).newProxyInstance();
      cm.getIdField().getBinder(getEntityMode()).set(o, Collections.singletonList(id.getId()));
      o.setHandler(mi);
      proxies.put(id, mi);

      if (log.isDebugEnabled())
        log.debug("Dynamic proxy created for " + id);

      return o;
    } catch (Exception e) {
      throw new OtmException("Dynamic proxy instantiation failed", e);
    }
  }

  private Id checkObject(RdfMapper assoc, Object o, boolean isUpdate, boolean dupCheck) throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    ClassMetadata cm = null;
    if (assoc != null)
      cm = sessionFactory.getClassMetadata(assoc.getAssociatedEntity());

    cm = checkClass(sessionFactory.getInstanceMetadata(cm, getEntityMode(), o), o.getClass().getName());
    if (cm.isView() && isUpdate)
      throw new OtmException("View's may not be updated: " + o.getClass());

    IdMapper           idField = cm.getIdField();

    if (idField == null)
      throw new OtmException("Must have an id field for " + cm);

    Binder        b       = idField.getBinder(getEntityMode());
    List          ids     = b.get(o);
    String        id      = null;

    if (ids.size() == 0) {
      IdentifierGenerator generator = idField.getGenerator();

      if (generator == null)
        throw new OtmException("No id generation for " + cm + " on " + idField);

      id = generator.generate(cm, this);
      b.set(o, Collections.singletonList(id)); // So user can get at it after saving

      if (log.isDebugEnabled())
        log.debug(generator.getClass().getSimpleName() + " generated '" + id + "' for "
                  + cm);
    } else {
      id = (String) ids.get(0);
    }

    Id oid = new Id(cm, id);
    if (dupCheck) {
      for (Object ex : new Object[] {deleteMap.get(oid), cleanMap.get(oid), dirtyMap.get(oid)})
        if ((ex != null) && (ex != o))
          throw new OtmException("Session already contains another object instance with id <" 
            + id + ">");
    }
    return oid;
  }

  private static Throwable trimStackTrace(Throwable t, int offset, int levels) {
    StackTraceElement[] trace = t.getStackTrace();
    if (trace.length > offset) {
      if ((trace.length - offset) < levels)
        levels = trace.length - offset;
      StackTraceElement[] copy = new StackTraceElement[levels];
      for (int i = 0; i < levels; i++)
        copy[i] = trace[i+offset];
      t.setStackTrace(copy);
    }
    return t;
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

  private interface LazyLoadMethodHandler extends MethodHandler {
    public boolean isLoaded();
  }
}
