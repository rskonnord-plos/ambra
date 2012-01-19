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


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.query.Results;

import org.topazproject.otm.Blob;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Query;
import org.topazproject.otm.SearchStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;

/**
 * An otm session (similar to hibernate session). And similar to hibernate session, not thread
 * safe.
 *
 * @author Pradeep Krishnan
 */
abstract class AbstractSession implements Session {
  private static final Log                      log = LogFactory.getLog(AbstractSession.class);

  protected final SessionFactoryImpl            sessionFactory;
  protected final Interceptor                   interceptor;
  protected       TransactionImpl               locTxn         = null;
  protected       javax.transaction.Transaction jtaTxn         = null;
  protected       boolean                       txnIsRO        = false;
  protected       Connection                    tsCon          = null;
  protected       Connection                    bsCon          = null;
  protected       FlushMode                     flushMode      = FlushMode.always;
  protected final Map<String, Filter>           filters        = new HashMap<String, Filter>();

  /**
   * Creates a new Session object.
   *
   * @param sessionFactory the session factory that created this session
   * @param interceptor    the interceptor that listens for object state changes
   */
  protected AbstractSession(SessionFactoryImpl sessionFactory, Interceptor interceptor) {
    this.sessionFactory = sessionFactory;
    this.interceptor    = interceptor;
  }

  /*
   * inherited javadoc
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /*
   * inherited javadoc
   */
  public Interceptor getInterceptor() {
    return interceptor;
  }

  /*
   * inherited javadoc
   */
  public Transaction beginTransaction() throws OtmException {
    return beginTransaction(false, -1);
  }

  /*
   * inherited javadoc
   */
  public Transaction beginTransaction(boolean readOnly, int txTimeout) throws OtmException {
    if (jtaTxn != null)
      throw new OtmException("A transaction is already active on this session");

    ensureTxActive(true, txTimeout);
    locTxn = new TransactionImpl(this, jtaTxn);

    txnIsRO = readOnly;

    return locTxn;
  }

  /**
   * Clears the transaction flags when committed or rolled back.
   */
  private void endTransaction() {
    locTxn = null;
    jtaTxn = null;

    if (tsCon != null)
      tsCon.close();
    tsCon  = null;

    if (bsCon != null)
      bsCon.close();
    bsCon  = null;
  }

  /*
   * inherited javadoc
   */
  public Transaction getTransaction() throws OtmException {
    if (jtaTxn == null) {
      try {
        jtaTxn = getSessionFactory().getTransactionManager().getTransaction();
      } catch (Exception e) {
        throw new OtmException("Error getting transaction", e);
      }
    }

    if (jtaTxn != null && locTxn == null)
      locTxn = new TransactionImpl(this, jtaTxn);

    return locTxn;
  }

  /*
   * inherited javadoc
   */
  public void close() throws OtmException {
    clear();

    if (jtaTxn != null) {
      try {
        jtaTxn.rollback();
      } catch (Exception e) {
        throw new OtmException("Error setting rollback-only", e);
      }
    }
  }

  /*
   * inherited javadoc
   */
  public void setFlushMode(FlushMode flushMode) {
    this.flushMode = flushMode;
  }

  /*
   * inherited javadoc
   */
  public FlushMode getFlushMode() {
    return this.flushMode;
  }

  /*
   * inherited javadoc
   */
  public Criteria createCriteria(Class clazz) throws OtmException {
    return new Criteria(this, null, null, false, checkClass(clazz),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public Criteria createCriteria(String entity) throws OtmException {
    return new Criteria(this, null, null, false, checkClass(entity),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public Criteria createCriteria(Criteria parent, String referrer, String path, String assocType)
                          throws OtmException {
    ClassMetadata cm = checkClass(referrer != null ? referrer :
                                                     parent.getClassMetadata().getName());
    Mapper        m  = cm.getMapperByName(path);

    if (!(m instanceof RdfMapper))
      throw new OtmException(path + " is not a valid field name for " + cm);
    RdfMapper rm = (RdfMapper) m;

    if (assocType == null) {
      assocType = rm.getAssociatedEntity();
      if (assocType == null)
        throw new OtmException(path + " is not an association of " + cm);
    }

    if (referrer != null &&
        !checkClass(assocType).isAssignableFrom(parent.getClassMetadata(), getEntityMode()))
      throw new OtmException("'" + path + "' in " + cm + " does not point to '" +
                             parent.getClassMetadata().getName() + "'");

    return new Criteria(this, parent, rm, referrer != null,
                        checkClass(referrer != null ? referrer : assocType),
                        new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public List list(Criteria criteria) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.list(criteria, getTripleStoreCon());
  }

  /*
   * inherited javadoc
   */
  public Query createQuery(String query) throws OtmException {
    return new QueryImpl(this, query, new ArrayList<Filter>(filters.values()));
  }

  /*
   * inherited javadoc
   */
  public Results doNativeQuery(String query) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();

    return store.doNativeQuery(query, getTripleStoreCon());
  }

  /*
   * inherited javadoc
   */
  public void doNativeUpdate(String command) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that ordering is preserved

    TripleStore store = sessionFactory.getTripleStore();
    store.doNativeUpdate(command, getTripleStoreCon());
  }

  public void createGraph(String graph) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that mods are visible to queries

    TripleStore store = sessionFactory.getTripleStore();
    store.createGraph(sessionFactory.getGraph(graph), getTripleStoreCon());
  }

  public void dropGraph(String graph) throws OtmException {
    if (flushMode.implies(FlushMode.always))
      flush(); // so that updates to the graph are applied before the drop

    TripleStore store = sessionFactory.getTripleStore();
    store.dropGraph(sessionFactory.getGraph(graph), getTripleStoreCon());
  }

  /*
   * inherited javadoc
   */
  public List<String> getIds(List objs) throws OtmException {
    List<String> results = new ArrayList<String>(objs.size());

    for (Object o : objs)
      results.add(getId(o));

    return results;
  }

  /*
   * inherited javadoc
   */
  public String getId(Object o) throws OtmException {
    if (o == null)
      throw new NullPointerException("Null object");

    ClassMetadata cm = sessionFactory.getInstanceMetadata(null, getEntityMode(), o);

    if (cm == null)
      throw new OtmException("No class metadata found for " + o);

    IdMapper           idField = cm.getIdField();
    if (idField == null)
      throw new OtmException("No id-field found for " + cm);

    PropertyBinder  b = idField.getBinder(getEntityMode());
    List          ids     = b.get(o);
    if (ids.size() == 0)
      throw new OtmException("No id set for " + cm + " instance " + o);

    return (String) ids.get(0);
  }

  public Blob getBlob(Class<?> containingClass, String oid) throws OtmException {
    return getBlob(checkClass(containingClass), oid);
  }

  public Blob getBlob(String containingEntity, String oid) throws OtmException {
    return getBlob(checkClass(containingEntity), oid);
  }

  protected Blob getBlob(ClassMetadata cm, String oid) throws OtmException {
    return getBlob(cm, oid, get(cm, oid, true));
  }

  protected Blob getBlob(ClassMetadata cm, String oid, Object instance) throws OtmException {
    return getSessionFactory().getBlobStore().getBlob(cm, oid, instance, getBlobStoreCon());
  }

  protected Blob createProxyBlob(final ClassMetadata cm, final String oid, final Object instance)
                      throws OtmException {

    return new Blob() {

      public Blob getRealBlob() throws OtmException {
        return getBlob(cm, oid, instance);
      }

      public boolean create() throws OtmException {
        return getRealBlob().create();
      }

      public boolean delete() throws OtmException {
        return getRealBlob().delete();
      }

      public boolean exists() throws OtmException {
        return getRealBlob().exists();
      }

      public ChangeState getChangeState() {
        return getRealBlob().getChangeState();
      }

      public String getId() {
        return oid;
      }

      public InputStream getInputStream() throws OtmException {
        return getRealBlob().getInputStream();
      }

      public OutputStream getOutputStream() throws OtmException {
        return getRealBlob().getOutputStream();
      }

      public ChangeState mark() {
        return getRealBlob().mark();
      }

      public byte[] readAll() throws OtmException {
        return getRealBlob().readAll();
      }

      public void writeAll(byte[] b) throws OtmException {
        getRealBlob().writeAll(b);
      }
    };
  }

  /*
   * inherited javadoc
   */
  public Connection getTripleStoreCon() throws OtmException {
    if (tsCon == null) {
      ensureTxActive(false, -1);
      tsCon = sessionFactory.getTripleStore().openConnection(this, txnIsRO);
    }

    return tsCon;
  }

  /*
   * inherited javadoc
   */
  public Connection getBlobStoreCon() throws OtmException {
    if (bsCon == null) {
      ensureTxActive(false, -1);
      bsCon = sessionFactory.getBlobStore().openConnection(this, txnIsRO);
    }

    return bsCon;
  }

  protected Connection getSearchStoreCon() throws OtmException {
    if (sessionFactory.getTripleStore() instanceof SearchStore)
      return getTripleStoreCon();

    throw new OtmException("Triple-Store '" + sessionFactory.getTripleStore() +
                           "' does not double as a Search-Store - text-search is currently " +
                           "only supported when using certain triple-stores");
  }

  private void ensureTxActive(boolean start, int txTimeout) throws OtmException {
    if (jtaTxn != null)
      return;

    try {
      TransactionManager tm = getSessionFactory().getTransactionManager();

      jtaTxn = tm.getTransaction();
      if (jtaTxn == null) {
        if (!start)
          throw new OtmException("No active transaction");

        tm.setTransactionTimeout(txTimeout > 0 ? txTimeout : 0);
        tm.begin();
        jtaTxn = tm.getTransaction();
      }

      jtaTxn.registerSynchronization(new Synchronization() {
        public void beforeCompletion() {
          if (getFlushMode().implies(FlushMode.commit) && !isRollback())
            flush();
        }

        public void afterCompletion(int status) {
          endTransaction();
        }

        private boolean isRollback() throws OtmException {
          try {
            return (jtaTxn.getStatus() == Status.STATUS_MARKED_ROLLBACK);
          } catch (Exception e) {
            throw new OtmException("Error getting rollback-only status", e);
          }
        }
      });
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      throw new OtmException("Error setting up transaction", e);
    }
  }

  /*
   * inherited javadoc
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

  /*
   * inherited javadoc
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

  /*
   * inherited javadoc
   */
  public void disableFilter(String name) throws OtmException {
    filters.remove(name);
  }

  /*
   * inherited javadoc
   */
  public Set<String> listFilters() {
    return new HashSet<String>(filters.keySet());
  }

  protected ClassMetadata checkClass(Class<?> clazz) throws OtmException {
    if (getEntityMode() != EntityMode.POJO)
      throw new UnsupportedOperationException("Only supported when EntityMode is "
          + EntityMode.POJO);
    return checkClass(sessionFactory.getClassMetadata(clazz), clazz.getName());
  }

  protected ClassMetadata checkClass(String entity) throws OtmException {
    return checkClass(sessionFactory.getClassMetadata(entity), entity);
  }

  protected ClassMetadata checkClass(ClassMetadata cm, String whatever) throws OtmException {
    if (cm == null)
      throw new OtmException("No class metadata found for " + whatever);

    if ((cm.getGraph() == null) && !cm.isView() && (cm.getBlobField() == null))
      throw new OtmException("No graph found for " + cm);

    return cm;
  }

}
