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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.EntityBinder.LazyLoaded;
import org.topazproject.otm.mapping.EntityBinder.LazyLoader;
import org.topazproject.otm.mapping.PropertyBinder.Streamer;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.metadata.SearchableDefinition;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.Blob;
import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.SearchStore;
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
  private final Map<Id, Set<Wrapper>>          orphanTrack    = new HashMap<Id, Set<Wrapper>>();
  private final Set<Id>                        currentIds     = new HashSet<Id>();
  private final Map<Id, Wrapper>               currentGets    = new HashMap<Id, Wrapper>();
  private boolean flushing                                    = false;
  private int flushLoopLimit                                  = 100; // XXX: expose this to the app

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

    if (bsCon != null)
      sessionFactory.getBlobStore().flush(bsCon);
    if (tsCon != null)
      sessionFactory.getTripleStore().flush(tsCon);
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

    for (int iterations = 0; iterations < flushLoopLimit; iterations++) {
      Map<Id, Object> workDelete = new HashMap<Id, Object>(deleteMap);
      Map<Id, Object> workDirty = new HashMap<Id, Object>(dirtyMap);

      for (Map.Entry<Id, Object> e : workDelete.entrySet())
        write(e.getKey(), e.getValue(), true);

      for (Object o : workDirty.values())
        if (o instanceof PreInsertEventListener)
          ((PreInsertEventListener) o).onPreInsert(this, o);

      for (Map.Entry<Id, Object> e : workDirty.entrySet())
        write(e.getKey(), e.getValue(), false);

      deleteMap.keySet().removeAll(workDelete.keySet());
      dirtyMap.keySet().removeAll(workDirty.keySet());
      cleanMap.putAll(workDirty);
      if ((deleteMap.size() == 0) && (dirtyMap.size() == 0))
        return;
    }
    throw new OtmException("Flush is unable to converge. An Interceptor or PreInsertListener " +
        "is updating the Session. Looped " + flushLoopLimit + " times and still have " +
        "deleteMap=" + deleteMap.keySet() + " dirtyMap=" + dirtyMap.keySet());
  }

  /*
   * inherited javadoc
   */
  public void clear() {
    cleanMap.clear();
    dirtyMap.clear();
    deleteMap.clear();
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

        PropertyBinder b = m.getBinder(getEntityMode());
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

      ClassMetadata cm     = id.getClassMetadata();
      Set<Wrapper>     assocs = new HashSet<Wrapper>();

      for (RdfMapper m : cm.getRdfMappers()) {
        if (!m.isAssociation())
          continue;

        // ignore this association if evict does not cascade
        if (!m.isCascadable(CascadeType.evict))
          continue;

        PropertyBinder b = m.getBinder(getEntityMode());
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

    Id     id = new Id(cm, oid, getEntityMode());
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      o = newLazyLoadedInstance(id);
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
    Id     id = new Id(cm, oid, getEntityMode());
    Object o  = deleteMap.get(id);

    if (o != null)
      return null;

    o = cleanMap.get(id);

    if (o == null)
      o = dirtyMap.get(id);

    if (o == null) {
      Wrapper w = getFromStore(id, null, filterObj);

      if (w.get() != null) {
        // Must merge here. Associations may have a back-pointer to this Id.
        // If those associations are loaded from the store even before
        // we complete this get() operation, there will be an instance
        // in our cache for this same Id.
        if (cm.isView())
          o = w.get();
        else {
          o = sync(w.get(), w.getId(), true, false, null, true);
          if (w.get() == o)    // only track if we are going to use this
            startTracking(w);
          else
            log.warn("Discarding loaded instance in favor of one already in session");
        }
      }
    }

    if (o instanceof LazyLoaded)
      o.equals(null); // ensure it is loaded

    return o;
  }

  /*
   * inherited javadoc
   */
  public void delayedLoadComplete(Object o, Mapper field) throws OtmException {
    Id id = checkObject(null, o, true, false);

    if ((deleteMap.get(id) != null) || (currentGets.get(id) != null)) {
      if (log.isDebugEnabled())
        log.debug("delayedLoadComplete ignored for '" + field.getName() + "' on " + id);
      return;
    }

    if (log.isDebugEnabled())
      log.debug("delayedLoadComplete for '" + field.getName() + "' on " + id,
          trimStackTrace(new Throwable("trimmed-stack-trace"), 3, 8));

    if (interceptor != null)
      interceptor.onPostRead(this, id.getClassMetadata(), id.getId(), o, field);

    if (o instanceof PostLoadEventListener)
      ((PostLoadEventListener) o).onPostLoad(this, o, field);

    if (field instanceof BlobMapper)
      states.digestUpdate(o, id.getClassMetadata(), this);
    else if (field instanceof RdfMapper) {
      RdfMapper m = (RdfMapper) field;
      states.delayedLoadComplete(o, m, this);

      if (m.isCascadable(CascadeType.deleteOrphan)) {
        Set<Wrapper> assocs = orphanTrack.get(id);
        if (assocs == null)
          orphanTrack.put(id, assocs = new HashSet<Wrapper>());

        PropertyBinder b = m.getBinder(getEntityMode());
        for (Object ao : b.get(o)) {
          Id aid = checkObject(m, ao, true, false);
          assocs.add(new Wrapper(aid, ao));
        }
      }
    }
  }

  /*
   * inherited javadoc
   */
  public <T> T merge(T o) throws OtmException {
    Id id = checkObject(null, o, true, false);

    // make sure it is loaded
    ClassMetadata cm = id.getClassMetadata();
    T ao = (T) load(cm, id.getId());

    if (ao != null) {
      Map<Id, Object> loopDetect = new HashMap<Id, Object>();
      loopDetect.put(id, ao);
      copy(id, ao, o, loopDetect); // deep copy
      ao = (T) sync(ao, id, true, true, CascadeType.merge, false);
    }

    return ao;
  }

  /*
   * inherited javadoc
   */
  public void refresh(Object o) throws OtmException {
    Id id = checkObject(null, o, false, true);

    if (dirtyMap.containsKey(id) || cleanMap.containsKey(id)) {
      Wrapper w = getFromStore(id, o, true);
      if (w.get() != null) {
        sync(w.get(), id, true, false, CascadeType.refresh, true);
        startTracking(w);
      }
    }
  }

  private <T> boolean isPristineProxy(Id id, T o) throws OtmException {
    if (!(o instanceof LazyLoaded))
      return false;
    LazyLoaded ll  = (LazyLoaded) o;
    LazyLoader llm = ll.getLazyLoader(ll);
    return (llm != null) ? !llm.isLoaded() : false;
  }

  private <T> void write(Id id, T o, boolean delete) throws OtmException {
    ClassMetadata         cm            = sessionFactory.getInstanceMetadata(id.getClassMetadata(),
                                                                               getEntityMode(), o);
    TripleStore           store         = sessionFactory.getTripleStore();
    SearchStore           ss            = sessionFactory.getSearchStore();
    boolean               bf            = (cm.getBlobField() != null);
    boolean               tp            = (cm.getRdfMappers().size() + cm.getAllTypes().size()) > 0;

    if (delete) {
      if (log.isDebugEnabled())
        log.debug("deleting from store: " + id);

     if (interceptor != null)
       interceptor.onPreDelete(this, cm, id.getId(), o);
     states.remove(o);
     if (bf) {
       Blob blob = getBlob(cm, id.toString(), o);
       blob.delete();
       updateBlobSearch(ss, blob, cm, id, o);
     }
     if (tp) {
       updateTripleSearch(ss, cm, cm.getRdfMappers(), id, o, true);
       store.delete(cm, cm.getRdfMappers(), id.getId(), o, getTripleStoreCon());
     }
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
        updateTripleSearch(ss, cm, fields, id, o, true);
        store.delete(cm, fields, id.getId(), o, getTripleStoreCon());
        store.insert(cm, fields, id.getId(), o, getTripleStoreCon());
        updateTripleSearch(ss, cm, fields, id, o, false);
        wroteSomething = true;
      }
      if (bf) {
        Blob blob = getBlob(cm, id.toString(), o);
        switch (states.digestUpdate(o, cm, this)) {
          case delete:
            blob.delete();
            break;
          case update:
          case insert:
            PropertyBinder binder = cm.getBlobField().getBinder(getEntityMode());
            byte[] b = binder.getStreamer().getBytes(binder, o);
            blob.writeAll(b);
            break;
        }

        if (blob.mark() == Blob.ChangeState.NONE)
          bf = false;
        else
         updateBlobSearch(ss, blob, cm, id, o);

        if (updates != null)
          updates.blobChanged = bf;
        if (bf)
          wroteSomething = true;
      }
      if ((interceptor != null) && wroteSomething)
        interceptor.onPostWrite(this, cm, id.getId(), o, updates);
    }
  }

  /**
   * Yuck: this assumes all (non-blob) searchable fields are stored in the triple-store (an
   * assumption that is also made by the triple store.
   */
  private <T> void updateTripleSearch(SearchStore store, ClassMetadata cm,
                                      Collection<RdfMapper> fields, Id id, T o, boolean delete)
      throws OtmException {
   List<SearchableDefinition> sdList = new ArrayList<SearchableDefinition>();
   for (RdfMapper m : fields) {
     SearchableDefinition sd =
         SearchableDefinition.findForProp(sessionFactory, cm.getName() + ':' + m.getName());
     if (sd != null)
       sdList.add(sd);
   }
   if (sdList.size() == 0)
     return;

   if (delete)
     store.remove(cm, sdList, id.getId(), o, getSearchStoreCon());
   else
     store.index(cm, sdList, id.getId(), o, getSearchStoreCon());
  }

  private <T> void updateBlobSearch(SearchStore store, Blob blob, ClassMetadata cm, Id id, T o)
      throws OtmException {
    SearchableDefinition sd =
      SearchableDefinition.findForProp(sessionFactory, cm.getBlobField().getDefinition().getName());
    if (sd == null)
      return;

    switch (blob.getChangeState()) {
      case DELETED:
        store.remove(cm, sd, id.getId(), o, getSearchStoreCon());
        break;
      case WRITTEN:
        store.remove(cm, sd, id.getId(), o, getSearchStoreCon());
      case CREATED:
        store.index(cm, Collections.singleton(sd), id.getId(), o, getSearchStoreCon());
    }
  }

  private Wrapper getFromStore(Id id, Object instance, boolean filterObj) throws OtmException {
    Wrapper w = currentGets.get(id);
    if (w != null)
      return w;

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
        if ((cm.getAllTypes().size() + cm.getRdfMappers().size()) > 0) {
          instance = instantiate(instance, id, store.get(cm, id.getId(), getTripleStoreCon(),
                                  new ArrayList<Filter>(filters.values()), filterObj));
          if (instance != null)
            cm = sessionFactory.getInstanceMetadata(cm, getEntityMode(), instance);
        }
        if (cm.getBlobField() != null) {
          Blob blob = getBlob(cm, id.getId(), instance);
          instance = instantiate(instance, cm, id.getId(), blob);
        }
      }
    }

    try {
      w = new Wrapper(new Id(cm, id.getId(), getEntityMode()), instance);
      currentGets.put(w.getId(), w);

      if (!cm.isView() && (instance != null)) {
        for (RdfMapper m : cm.getRdfMappers())
          if (m.getFetchType() == FetchType.eager)
            m.getBinder(getEntityMode()).get(instance);
      }

      if (interceptor != null)
        interceptor.onPostRead(this, cm, id.getId(), instance);

      if (instance instanceof PostLoadEventListener)
        ((PostLoadEventListener) instance).onPostLoad(this, instance);

      if (log.isDebugEnabled()) {
        if (instance == null)
          log.debug("Object not found in store: " + id.getId());
        else
          log.debug("Object loaded from " + (found ? "interceptor: " : "store: ") + id.getId());
      }

      return w;
    } finally {
      currentGets.remove(id);
    }
  }

  private Object instantiate(Object instance, ClassMetadata cm, String id, Blob blob)
             throws OtmException {
    boolean blobOnly = cm.getAllTypes().isEmpty() && cm.getRdfMappers().isEmpty();
    if (!blobOnly && (instance == null))
      return null;

    PropertyBinder binder = cm.getBlobField().getBinder(getEntityMode());
    Streamer streamer = binder.getStreamer();

    // avoid exists() test if possible
    boolean testExists = !streamer.isManaged() || (instance == null);

    if (!testExists || blob.exists()) {
      if (instance == null)
        instance = cm.getEntityBinder(getEntityMode()).newInstance();
      cm.getIdField().getBinder(getEntityMode()).set(instance, Collections.singletonList(id));

      if (streamer.isManaged())
        streamer.attach(binder, instance, createProxyBlob(cm, id, instance));
      else
        streamer.setBytes(binder, instance, blob.readAll());
    }
    return instance;
  }

  private Object instantiate(Object instance, Id id, TripleStore.Result result)
                            throws OtmException {
    final Map<String, List<String>> fvalues = result.getFValues();
    final Map<String, List<String>> rvalues = result.getRValues();

    if (fvalues.size() == 0 && rvalues.size() == 0) {
      if (log.isDebugEnabled())
        log.debug("No statements found about '" + id.getId() + "' for " + id.getClassMetadata());
      return null;
    }

    // figure out sub-class to instantiate
    List<String> t = fvalues.get(Rdf.rdf + "type");
    ClassMetadata cm = getSessionFactory().getSubClassMetadata(id.getClassMetadata(),
                                                               getEntityMode(), t, result);
    if (cm == null) {
      HashSet<String> props = new HashSet<String>(fvalues.keySet());
      props.addAll(rvalues.keySet());
      log.warn("Properties " + props + " on '" + id
          + "' does not satisfy the restrictions imposed by " + id.getClassMetadata()
          + ". No object will be instantiated.");
      return null;
    }
    if (t != null)
      t.removeAll(cm.getAllTypes());

    return cm.getEntityBinder(this).loadInstance(instance, id.getId(), result, this);
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
      instance = cm.getEntityBinder(this).loadInstance(instance, id, r, this);
    else
      instance = null;

    r.close();

    return instance;
  }

  private void startTracking(Wrapper w) throws OtmException {
    ClassMetadata cm = w.getId().getClassMetadata();
    if (!cm.isView()) {
      states.update(w.get(), cm, this);
      if (cm.getBlobField() != null)
        states.digestUpdate(w.get(), cm, this);
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

      if (update && (cm.getBlobField() != null)) {
        PropertyBinder binder = cm.getBlobField().getBinder(getEntityMode());
        Streamer streamer = binder.getStreamer();
        // attach the Blob
        if (streamer.isManaged()) {
          streamer.attach(binder, o, createProxyBlob(cm, id.getId(), o));
        }
      }

      Set<Wrapper> assocs = new HashSet<Wrapper>();

      for (RdfMapper m : cm.getRdfMappers()) {
        PropertyBinder b = m.getBinder(getEntityMode());
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
                            getEntityMode(), o);

    if (!cm.isAssignableFrom(ocm, getEntityMode()))
      throw new OtmException(cm.toString() + " is not assignable from " + ocm);

    if (log.isDebugEnabled())
      log.debug("Copy(" + ((loopDetect == null) ? "shallow" : "deep") + ") merging " + id);

    for (RdfMapper m : cm.getRdfMappers()) {
      Mapper p = ocm.getMapperByName(m.getName());

      if (!(p instanceof RdfMapper))
        continue;

      RdfMapper om = (RdfMapper) p;
      PropertyBinder b  = m.getBinder(getEntityMode());
      PropertyBinder ob = om.getBinder(getEntityMode());
      if ((loopDetect == null) || !m.isAssociation())
        b.set(o, ob.get(other));
      else {
        List cc = new ArrayList();

        for (Object ao : ob.get(other)) {
          Id     aid  = checkObject(om, ao, false, false);
          Object aoc  = loopDetect.get(aid);

          if (aoc == null) {
            aoc = load(aid.getClassMetadata(), aid.getId());

            loopDetect.put(aid, aoc);
            copy(aid, aoc, ao, loopDetect);
          }

          cc.add(aoc);
        }

        b.get(o);       // force load of lazy-loaded associations for orphanTrack
        b.set(o, cc);
      }
    }

    if (cm.getIdField() != null)
      cm.getIdField().getBinder(getEntityMode()).set(o, Collections.singletonList(id.getId()));

    if ((cm.getBlobField() != null) && (ocm.getBlobField() != null)) {
      List blob = ocm.getBlobField().getBinder(getEntityMode()).get(other);
      cm.getBlobField().getBinder(getEntityMode()).set(o, blob);
    }

    return o;
  }

  private LazyLoaded newLazyLoadedInstance(final Id id) throws OtmException {
    LazyLoader mi =
      new LazyLoader() {
        private boolean loading = false;
        private boolean loaded = false;
        private Map<PropertyBinder, PropertyBinder.RawFieldData> rawData = new HashMap<PropertyBinder, PropertyBinder.RawFieldData>();

        public void ensureDataLoad(LazyLoaded self, String operation) throws OtmException {
          if (!loaded && !loading) {
            if (log.isDebugEnabled())
              log.debug(operation + " on " + id + " is forcing a load from store",
                   trimStackTrace(new Throwable("trimmed-stack-trace"), 3, 8));

            loading = true;
            try {
              Wrapper w = getFromStore(id, self, false);
              loaded = true;
              if (w.get() != null)
                startTracking(w);
            } finally {
              loading = false;
            }
          }
        }

        public boolean isLoaded() {
          return loaded;
        }

        public boolean isLoaded(PropertyBinder b) {
          return loaded && !rawData.containsKey(b);
        }

        public void setRawFieldData(PropertyBinder b, PropertyBinder.RawFieldData d) {
          if (d == null)
            rawData.remove(b);
          else
            rawData.put(b, d);
        }

        public PropertyBinder.RawFieldData getRawFieldData(PropertyBinder b) {
          return rawData.get(b);
        }

        public Session getSession() {
          return SessionImpl.this;
        }

        public String getId() {
          return id.getId();
        }

        public ClassMetadata getClassMetadata() {
          return id.getClassMetadata();
        }
      };

    ClassMetadata cm = id.getClassMetadata();
    LazyLoaded o = cm.getEntityBinder(getEntityMode()).newLazyLoadedInstance(mi);

    if (log.isDebugEnabled())
      log.debug("Lazy loaded instance created for " + id);

    return o;
  }

  private Id checkObject(RdfMapper assoc, Object o, boolean isUpdate, boolean dupCheck)
      throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    ClassMetadata cm = null;
    if (assoc != null) {
      cm = sessionFactory.getClassMetadata(assoc.getAssociatedEntity());
      if (cm == null)
        throw new OtmException("Undefined association '" + assoc.getAssociatedEntity() + "' in '"
                               + assoc.getDefinition().getName());
    }

    cm = checkClass(sessionFactory.getInstanceMetadata(cm, getEntityMode(), o),
                    o.getClass().getName());
    if (cm.isView() && isUpdate)
      throw new OtmException("View's may not be updated: " + o.getClass());

    IdMapper idField = cm.getIdField();

    if (idField == null)
      throw new OtmException("Must have an id field for " + cm);

    PropertyBinder b   = idField.getBinder(getEntityMode());
    List           ids = b.get(o);
    String         id;

    if (ids.size() == 0) {
      IdentifierGenerator generator = idField.getGenerator();

      if (generator == null)
        throw new OtmException("No id generation for " + cm + " on " + idField);

      id = generator.generate(cm, this);
      b.set(o, Collections.singletonList(id)); // So user can get at it after saving

      if (log.isDebugEnabled())
        log.debug(generator.getClass().getSimpleName() + " generated '" + id + "' for " + cm);
    } else {
      id = (String) ids.get(0);
    }

    Id oid = new Id(cm, id, getEntityMode());
    if (dupCheck) {
      for (Object ex : new Object[] { deleteMap.get(oid), cleanMap.get(oid), dirtyMap.get(oid) })
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
      System.arraycopy(trace, offset, copy, 0, levels);
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
      return (other instanceof Wrapper) && id.equals(((Wrapper) other).id);
    }
  }
}
