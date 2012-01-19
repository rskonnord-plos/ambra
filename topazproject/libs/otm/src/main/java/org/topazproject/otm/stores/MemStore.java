/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Junction;
import org.topazproject.otm.criterion.PredicateCriterion;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.mapping.Mapper;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
 */
public class MemStore implements TripleStore {
  private Storage storage = new Storage();

  /*
   * inherited javadoc
   */
  public Connection openConnection() {
    return new MemStoreConnection(storage);
  }

  /*
   * inherited javadoc
   */
  public void closeConnection(Connection con) {
  }

  /*
   * inherited javadoc
   */
  public void insert(ClassMetadata cm, String id, Object o, Transaction txn)
              throws OtmException {
    MemStoreConnection msc     = (MemStoreConnection) txn.getConnection();
    Storage            storage = msc.getStorage();

    storage.insert(cm.getModel(), id, Rdf.rdf + "type", cm.getTypes().toArray(new String[0]));

    for (Mapper p : cm.getFields()) {
      if (p.hasInverseUri())
        continue;

      if (p.getSerializer() != null)
        storage.insert(cm.getModel(), id, p.getUri(), (String[]) p.get(o).toArray(new String[0]));
      else
        storage.insert(cm.getModel(), id, p.getUri(),
                       txn.getSession().getIds(p.get(o)).toArray(new String[0]));
    }
  }

  /*
   * inherited javadoc
   */
  public void delete(ClassMetadata cm, String id, Transaction txn)
              throws OtmException {
    MemStoreConnection msc     = (MemStoreConnection) txn.getConnection();
    Storage            storage = msc.getStorage();
    String             model   = cm.getModel();

    for (Mapper m : cm.getFields())
      storage.remove(model, id, m.getUri());
  }

  /*
   * inherited javadoc
   */
  public ResultObject get(ClassMetadata cm, String id, Transaction txn)
                   throws OtmException {
    MemStoreConnection        msc     = (MemStoreConnection) txn.getConnection();
    Storage                   storage = msc.getStorage();
    String                    model   = cm.getModel();

    Map<String, List<String>> value   = new HashMap<String, List<String>>();
    Map<String, List<String>> rvalue  = new HashMap<String, List<String>>();

    value.put(Rdf.rdf + "type",
              new ArrayList<String>(storage.getProperty(model, id, Rdf.rdf + "type")));

    for (Mapper p : cm.getFields()) {
      String uri = p.getUri();

      if (!p.hasInverseUri())
        value.put(uri, new ArrayList<String>(storage.getProperty(model, id, uri)));
      else {
        String inverseModel = p.getInverseModel();

        if (inverseModel == null)
          inverseModel = model;

        Set<String> invProps = storage.getInverseProperty(inverseModel, id, uri);
        rvalue.put(uri, new ArrayList<String>(invProps));
      }
    }

    return instantiate(txn.getSession().getSessionFactory(), cm.getSourceClass(), id, value, rvalue);
  }

  /*
   * inherited javadoc
   */
  public List<ResultObject> list(Criteria criteria, Transaction txn)
                          throws OtmException {
    MemStoreConnection msc      = (MemStoreConnection) txn.getConnection();
    Storage            storage  = msc.getStorage();
    ClassMetadata      cm       = criteria.getClassMetadata();
    Set<String>        subjects = conjunction(criteria.getCriterionList(), criteria, storage);
    List<ResultObject> results  = new ArrayList<ResultObject>();

    for (String id : subjects)
      results.add(get(cm, id, txn));

    return results;
  }

  /*
   * inherited javadoc
   */
  public void createModel(ModelConfig conf) throws OtmException {
  }

  /*
   * inherited javadoc
   */
  public void dropModel(ModelConfig conf) throws OtmException {
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
    String        model = cm.getModel();
    Set<String>   ids;

    if (c instanceof PredicateCriterion) {
      String name       = ((PredicateCriterion) c).getName();
      Object o          = ((PredicateCriterion) c).getValue();
      String value;

      try {
        Mapper m        = cm.getMapperByName(name);
        value           = (m.getSerializer() != null) ? m.getSerializer().serialize(o) : o.toString();
      } catch (Exception e) {
        throw new OtmException("Serializer exception", e);
      }

      String uri = cm.getMapperByName(name).getUri();
      ids = storage.getIds(model, uri, value);
    } else if (c instanceof SubjectCriterion) {
      String  id     = ((SubjectCriterion) c).getId();
      boolean exists =
        (cm.getType() != null) ? (storage.getProperty(model, id, Rdf.rdf + "type").size() != 0)
        : storage.exists(model, id);

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

  /*
     public T <Collection<T>> find(Class<T> clazz, List<Criteria> criteria,
         List<Field> orderBy, long offset, long size);
   */
  private ResultObject instantiate(SessionFactory sessionFactory, Class clazz, String id,
                                   Map<String, List<String>> props, Map<String, List<String>> rprops)
                            throws OtmException {
    List<String> types = props.get(Rdf.rdf + "type");

    clazz = sessionFactory.mostSpecificSubClass(clazz, types);

    ClassMetadata cm = sessionFactory.getClassMetadata(clazz);

    if ((types.size() == 0) && (cm.getType() != null))
      return null;

    ResultObject ro;

    try {
      ro = new ResultObject(clazz.newInstance(), id);
    } catch (Exception e) {
      throw new OtmException("instantiation failed", e);
    }

    cm.getIdField().set(ro.o, Collections.singletonList(id));

    // A field could be of rdf:type. So remove the values used in class identification
    // from the set of rdf:type values before running through and setting field values. 
    types.removeAll(cm.getTypes());

    int count = types.size();

    for (Mapper p : cm.getFields()) {
      List<String> vals = p.hasInverseUri() ? rprops.get(p.getUri()) : props.get(p.getUri());
      count += vals.size();

      if (p.getSerializer() != null)
        p.set(ro.o, vals);
      else
        ro.unresolvedAssocs.put(p, vals);
    }

    // Put back what we removed
    types.addAll(cm.getTypes());

    return (count > 0) ? ro : null;
  }

  private static class PropertyId {
    private String model;
    private String id;
    private String uri;

    public PropertyId(String model, String id, String uri) {
      this.model   = model;
      this.id      = id;
      this.uri     = uri;
    }

    public String getModel() {
      return model;
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

      return model.equals(o.model) && id.equals(o.id) && uri.equals(o.uri);
    }

    public int hashCode() {
      return model.hashCode() + id.hashCode() + uri.hashCode();
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

    public void insert(String model, String id, String uri, String[] val) {
      Set<String> data = getProperty(model, id, uri);

      for (String v : val)
        data.add(v);

      if (backingStore != null) {
        PropertyId p = new PropertyId(model, id, uri);
        pendingInserts.add(p);
      }
    }

    public void remove(String model, String id, String uri) {
      Set<String> data = getProperty(model, id, uri);
      data.clear();

      if (backingStore != null) {
        PropertyId p = new PropertyId(model, id, uri);
        pendingDeletes.add(new PropertyId(model, id, uri));
      }
    }

    public Set<String> getProperty(PropertyId prop) {
      return getProperty(prop.getModel(), prop.getId(), prop.getUri());
    }

    public Set<String> getProperty(String model, String id, String uri) {
      Map<String, Set<String>> subjectData = getSubjectData(model, id);
      Set<String>              val         = subjectData.get(uri);

      if (val == null) {
        val = new HashSet<String>();
        subjectData.put(uri, val);
      }

      if (backingStore != null) {
        if (!pendingDeletes.contains(new PropertyId(model, id, uri)))
          synchronized (backingStore) {
            val.addAll(backingStore.getProperty(model, id, uri));
          }
      }

      return val;
    }

    public Set<String> getInverseProperty(String model, String id, String uri) {
      Set<String>                           results   = new HashSet<String>();
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData != null) {
        for (String subject : modelData.keySet()) {
          Set<String> objs = modelData.get(subject).get(uri);

          if ((objs != null) && objs.contains(id))
            results.add(subject);
        }
      }

      if (backingStore != null) {
        synchronized (backingStore) {
          results.addAll(backingStore.getInverseProperty(model, id, uri));
        }

        for (Iterator<String> it = results.iterator(); it.hasNext();) {
          if (pendingDeletes.contains(new PropertyId(model, it.next(), uri)))
            it.remove();
        }
      }

      return results;
    }

    public Set<String> getIds(String model, String uri, String val) {
      Set<String>                           results   = new HashSet<String>();
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData != null) {
        for (Map.Entry<String, Map<String, Set<String>>> e : modelData.entrySet()) {
          Set<String> objs = e.getValue().get(uri);

          if ((objs != null) && objs.contains(val))
            results.add(e.getKey());
        }
      }

      if (backingStore != null) {
        synchronized (backingStore) {
          results.addAll(backingStore.getIds(model, uri, val));
        }

        for (Iterator<String> it = results.iterator(); it.hasNext();) {
          if (pendingDeletes.contains(new PropertyId(model, it.next(), uri)))
            it.remove();
        }
      }

      return results;
    }

    public boolean exists(String model, String id) {
      boolean                               found     = false;
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData != null)
        found = (modelData.get(id) != null);

      if (!found && (backingStore != null)) {
        synchronized (backingStore) {
          found = backingStore.exists(model, id);
        }
      }

      return found;
    }

    public void commit() {
      if (backingStore == null)
        return;

      synchronized (backingStore) {
        for (PropertyId pd : pendingDeletes)
          backingStore.remove(pd.getModel(), pd.getId(), pd.getUri());

        for (PropertyId pd : pendingInserts)
          backingStore.insert(pd.getModel(), pd.getId(), pd.getUri(),
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

    private Map<String, Set<String>> getSubjectData(String model, String id) {
      Map<String, Map<String, Set<String>>> modelData   = getModelData(model);
      Map<String, Set<String>>              subjectData = modelData.get(id);

      if (subjectData == null) {
        subjectData = new HashMap<String, Set<String>>();
        modelData.put(id, subjectData);
      }

      return subjectData;
    }

    private Map<String, Map<String, Set<String>>> getModelData(String model) {
      Map<String, Map<String, Set<String>>> modelData = data.get(model);

      if (modelData == null) {
        modelData = new HashMap<String, Map<String, Set<String>>>();
        data.put(model, modelData);
      }

      return modelData;
    }
  }

  private static class MemStoreConnection implements Connection {
    private Storage storage;

    public MemStoreConnection(Storage backingStore) {
      this.storage = new Storage(backingStore);
    }

    public void beginTransaction() {
    }

    public void commit() {
      storage.commit();
    }

    public void rollback() {
      storage.rollback();
    }

    public Storage getStorage() {
      return storage;
    }
  }
}
