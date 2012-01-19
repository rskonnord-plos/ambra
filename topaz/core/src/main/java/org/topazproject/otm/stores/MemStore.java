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
package org.topazproject.otm.stores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Filter;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.SearchStore;
import org.topazproject.otm.Session;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Junction;
import org.topazproject.otm.criterion.PredicateCriterion;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.metadata.SearchableDefinition;
import org.topazproject.otm.query.GenericQuery;
import org.topazproject.otm.query.Results;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
 */
public class MemStore extends AbstractTripleStore implements SearchStore {
  private Storage storage = new Storage();

  /*
   * inherited javadoc
   */
  public Connection openConnection(Session sess, boolean readOnly) throws OtmException {
    return new MemStoreConnection(storage, sess);
  }

  /*
   * inherited javadoc
   */
  public <T> void insert(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException {
    MemStoreConnection msc     = (MemStoreConnection) con;
    Storage            storage = msc.getStorage();

    storage.insert(cm.getGraph(), id, Rdf.rdf + "type", cm.getAllTypes().toArray(new String[0]));

    for (RdfMapper m : fields) {
      if (m.hasInverseUri())
        continue;

      PropertyBinder b = m.getBinder(msc.getSession());
      if (!m.isAssociation())
        storage.insert(cm.getGraph(), id, m.getUri(), (String[]) b.get(o).toArray(new String[0]));
      else
        storage.insert(cm.getGraph(), id, m.getUri(),
                       msc.getSession().getIds(b.get(o)).toArray(new String[0]));
    }
  }

  /*
   * inherited javadoc
   */
  public <T> void delete(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException {
    MemStoreConnection msc     = (MemStoreConnection) con;
    Storage            storage = msc.getStorage();
    String             graph   = cm.getGraph();

    for (RdfMapper m : fields)
      storage.remove(graph, id, m.getUri());
  }

  /*
   * inherited javadoc
   */
  public TripleStore.Result get(ClassMetadata cm, final String id, Connection con,
                   final List<Filter> filters, boolean filterObj) throws OtmException {
    if (filters != null && filters.size() > 0)
      throw new OtmException("Filters are not supported");

    final MemStoreConnection        msc     = (MemStoreConnection) con;
    Storage                   storage = msc.getStorage();
    String                    graph   = cm.getGraph();

    final Map<String, List<String>> value   = new HashMap<String, List<String>>();
    final Map<String, List<String>> rvalue  = new HashMap<String, List<String>>();

    value.put(Rdf.rdf + "type",
              new ArrayList<String>(storage.getProperty(graph, id, Rdf.rdf + "type")));

    for (RdfMapper p : cm.getRdfMappers()) {
      String uri = p.getUri();

      if (!p.hasInverseUri())
        value.put(uri, new ArrayList<String>(storage.getProperty(graph, id, uri)));
      else {
        String inverseGraph = p.getGraph();

        if (inverseGraph == null)
          inverseGraph = graph;

        Set<String> invProps = storage.getInverseProperty(inverseGraph, id, uri);
        rvalue.put(uri, new ArrayList<String>(invProps));
      }
    }

    return new TripleStore.Result() {
      public Map<String, List<String>> getFValues() {
        return value;
      }

      public Map<String, List<String>> getRValues() {
        return rvalue;
      }
    };
  }

  /*
   * inherited javadoc
   */
  public List list(Criteria criteria, Connection con)
            throws OtmException {
    MemStoreConnection msc      = (MemStoreConnection) con;
    Storage            storage  = msc.getStorage();
    ClassMetadata   cm       = criteria.getClassMetadata();
    Set<String>        subjects = conjunction(criteria.getCriterionList(), criteria, storage);
    List               results  = new ArrayList();

    for (String id : subjects) {
      Object o = msc.getSession().get(cm, id, true);

      if (o != null)
        results.add(o);
    }

    return results;
  }

  /*
   * inherited javadoc
   */
  public Results doQuery(GenericQuery query, Collection<Filter> filters, Connection con)
                  throws OtmException {
    throw new OtmException("OQL queries not supported");
  }

  /*
   * inherited javadoc
   */
  public Results doNativeQuery(String query, Connection con) throws OtmException {
    throw new OtmException("Native queries not supported");
  }

  /*
   * inherited javadoc
   */
  public void doNativeUpdate(String query, Connection con) throws OtmException {
    throw new OtmException("Native updates not supported");
  }

  /**
   * {@inheritDoc}
   */
  public void createGraph(GraphConfig conf, Connection con) throws OtmException {
  }

  /**
   * {@inheritDoc}
   */
  public void dropGraph(GraphConfig conf, Connection con) throws OtmException {
  }

  /*
   * inherited javadoc
   */
  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return null;
  }

  /*
   * inherited javadoc
   */
  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
  }

  /*
   * inherited javadoc
   */
  public List<String> getRdfList(String id, String pUri, String mUri, Connection isc)
                                   throws OtmException {
    throw new OtmException("Not implemented yet");
  }

  /*
   * inherited javadoc
   */
  public List<String> getRdfBag(String id, String pUri, String mUri, Connection isc)
                                  throws OtmException {
    throw new OtmException("Not implemented yet");
  }

  public <T> void index(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                        Connection con) throws OtmException {
    // no-op
   }

  public <T> void remove(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                         Connection con) throws OtmException {
    // no-op

  }

  public <T> void remove(ClassMetadata cm, SearchableDefinition field, String id, T o,
                         Connection con) throws OtmException {
    // no-op
  }

  private Set<String> conjunction(List<Criterion> criterions, Criteria criteria, Storage storage)
                           throws OtmException {
    Set<String> subjects = new HashSet<String>();
    boolean     first    = true;

    for (Criterion c : criterions) {
      Set<String> ids = evaluate(c, criteria, storage);

      if (!first)
        subjects.retainAll(ids);
      else {
        subjects.addAll(ids);
        first = false;
      }
    }

    return subjects;
  }

  private Set<String> disjunction(List<Criterion> criterions, Criteria criteria, Storage storage)
                           throws OtmException {
    Set<String> subjects = new HashSet<String>();

    for (Criterion c : criterions)
      subjects.addAll(evaluate(c, criteria, storage));

    return subjects;
  }

  private Set<String> evaluate(Criterion c, Criteria criteria, Storage storage)
                        throws OtmException {
    ClassMetadata cm    = criteria.getClassMetadata();
    String           graph = cm.getGraph();
    Set<String>      ids;

    if (c instanceof PredicateCriterion) {
      String name       = ((PredicateCriterion) c).getName();
      Object o          = ((PredicateCriterion) c).getValue();
      String value;
      RdfMapper m = (RdfMapper) cm.getMapperByName(name);

      try {
        value           = o.toString();
      } catch (Exception e) {
        throw new OtmException("Serializer exception", e);
      }

      String uri = m.getUri();
      ids = storage.getIds(graph, uri, value);
    } else if (c instanceof SubjectCriterion) {
      String  id     = ((SubjectCriterion) c).getId();
      boolean exists =
        (!cm.getTypes().isEmpty()) ? (storage.getProperty(graph, id, Rdf.rdf + "type").size() != 0)
        : storage.exists(graph, id);

      if (!exists)
        ids = Collections.emptySet();
      else
        ids = Collections.singleton(id);
    } else if (c instanceof Conjunction) {
      ids = conjunction(((Junction) c).getCriterions(), criteria, storage);
    } else if (c instanceof Disjunction) {
      ids = disjunction(((Junction) c).getCriterions(), criteria, storage);
    } else {
      throw new OtmException("MemStor can't handle " + c.getClass());
    }

    return ids;
  }


  private static class PropertyId {
    private String graph;
    private String id;
    private String uri;

    public PropertyId(String graph, String id, String uri) {
      this.graph   = graph;
      this.id      = id;
      this.uri     = uri;
    }

    public String getGraph() {
      return graph;
    }

    public String getId() {
      return id;
    }

    public String getUri() {
      return uri;
    }

    public boolean equals(Object other) {
      if (!(other instanceof PropertyId))
        return false;

      PropertyId o = (PropertyId) other;

      return graph.equals(o.graph) && id.equals(o.id) && uri.equals(o.uri);
    }

    public int hashCode() {
      return graph.hashCode() + id.hashCode() + uri.hashCode();
    }
  }

  private static class Storage {
    private Map<String, Map<String, Map<String, Set<String>>>> data           =
      new HashMap<String, Map<String, Map<String, Set<String>>>>();
    private Storage                                            backingStore   = null;
    private Set<PropertyId>                                    pendingDeletes;
    private Set<PropertyId>                                    pendingInserts;

    public Storage() {
    }

    public Storage(Storage backingStore) {
      this.backingStore                                                       = backingStore;

      if (backingStore != null) {
        pendingDeletes   = new HashSet<PropertyId>();
        pendingInserts   = new HashSet<PropertyId>();
      }
    }

    public void insert(String graph, String id, String uri, String[] val) {
      Set<String> data = getProperty(graph, id, uri);

      for (String v : val)
        data.add(v);

      if (backingStore != null) {
        PropertyId p = new PropertyId(graph, id, uri);
        pendingInserts.add(p);
      }
    }

    public void remove(String graph, String id, String uri) {
      Set<String> data = getProperty(graph, id, uri);
      data.clear();

      if (backingStore != null)
        pendingDeletes.add(new PropertyId(graph, id, uri));
    }

    public Set<String> getProperty(PropertyId prop) {
      return getProperty(prop.getGraph(), prop.getId(), prop.getUri());
    }

    public Set<String> getProperty(String graph, String id, String uri) {
      Map<String, Set<String>> subjectData = getSubjectData(graph, id);
      Set<String>              val         = subjectData.get(uri);

      if (val == null) {
        val = new HashSet<String>();
        subjectData.put(uri, val);
      }

      if ((backingStore != null) && (!pendingDeletes.contains(new PropertyId(graph, id, uri)))) {
        synchronized (backingStore) {
          val.addAll(backingStore.getProperty(graph, id, uri));
        }
      }

      return val;
    }

    public Set<String> getInverseProperty(String graph, String id, String uri) {
      Set<String>                           results   = new HashSet<String>();
      Map<String, Map<String, Set<String>>> graphData = data.get(graph);

      if (graphData != null) {
        for (String subject : graphData.keySet()) {
          Set<String> objs = graphData.get(subject).get(uri);

          if ((objs != null) && objs.contains(id))
            results.add(subject);
        }
      }

      if (backingStore != null) {
        synchronized (backingStore) {
          results.addAll(backingStore.getInverseProperty(graph, id, uri));
        }

        for (Iterator<String> it = results.iterator(); it.hasNext();) {
          if (pendingDeletes.contains(new PropertyId(graph, it.next(), uri)))
            it.remove();
        }
      }

      return results;
    }

    public Set<String> getIds(String graph, String uri, String val) {
      Set<String>                           results   = new HashSet<String>();
      Map<String, Map<String, Set<String>>> graphData = data.get(graph);

      if (graphData != null) {
        for (Map.Entry<String, Map<String, Set<String>>> e : graphData.entrySet()) {
          Set<String> objs = e.getValue().get(uri);

          if ((objs != null) && objs.contains(val))
            results.add(e.getKey());
        }
      }

      if (backingStore != null) {
        synchronized (backingStore) {
          results.addAll(backingStore.getIds(graph, uri, val));
        }

        for (Iterator<String> it = results.iterator(); it.hasNext();) {
          if (pendingDeletes.contains(new PropertyId(graph, it.next(), uri)))
            it.remove();
        }
      }

      return results;
    }

    public boolean exists(String graph, String id) {
      boolean                               found     = false;
      Map<String, Map<String, Set<String>>> graphData = data.get(graph);

      if (graphData != null)
        found = (graphData.get(id) != null);

      if (!found && (backingStore != null)) {
        synchronized (backingStore) {
          found = backingStore.exists(graph, id);
        }
      }

      return found;
    }

    public void commit() {
      if (backingStore == null)
        return;

      synchronized (backingStore) {
        for (PropertyId pd : pendingDeletes)
          backingStore.remove(pd.getGraph(), pd.getId(), pd.getUri());

        for (PropertyId pd : pendingInserts)
          backingStore.insert(pd.getGraph(), pd.getId(), pd.getUri(),
                              getProperty(pd).toArray(new String[0]));

        backingStore.commit();
      }

      pendingDeletes.clear();
      pendingInserts.clear();
      data.clear();
    }

    public void rollback() {
      if (backingStore == null)
        return;

      pendingDeletes.clear();
      pendingInserts.clear();
      data.clear();
    }

    private Map<String, Set<String>> getSubjectData(String graph, String id) {
      Map<String, Map<String, Set<String>>> graphData   = getGraphData(graph);
      Map<String, Set<String>>              subjectData = graphData.get(id);

      if (subjectData == null) {
        subjectData = new HashMap<String, Set<String>>();
        graphData.put(id, subjectData);
      }

      return subjectData;
    }

    private Map<String, Map<String, Set<String>>> getGraphData(String graph) {
      Map<String, Map<String, Set<String>>> graphData = data.get(graph);

      if (graphData == null) {
        graphData = new HashMap<String, Map<String, Set<String>>>();
        data.put(graph, graphData);
      }

      return graphData;
    }
  }

  private static class MemStoreConnection extends AbstractConnection {
    private Storage storage;

    public MemStoreConnection(Storage backingStore, Session sess) throws OtmException {
      super(sess);
      this.storage = new Storage(backingStore);

      XAResource xaRes = new XAResource() {
        public void    commit(Xid xid, boolean onePhase) { storage.commit(); }
        public void    rollback(Xid xid)                 { storage.rollback(); }
        public int     prepare(Xid xid)                  { return XA_OK; }

        public void    start(Xid xid, int flags)         { }
        public void    end(Xid xid, int flags)           { }
        public Xid[]   recover(int flag)                 { return new Xid[0]; }
        public void    forget(Xid xid)                   { }
        public boolean isSameRM(XAResource xares)        { return xares == this; }
        public int     getTransactionTimeout()           { return 0; }
        public boolean setTransactionTimeout(int secs)   { return false; }
      };

      enlistResource(xaRes);
    }

    public void close() {
    }

    public Storage getStorage() {
      return storage;
    }
  }
}
