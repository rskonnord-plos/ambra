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

import java.lang.ref.WeakReference;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.naming.Reference;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.resource.ResourceRegistrar;
import bitronix.tm.resource.common.AbstractXAResourceHolder;
import bitronix.tm.resource.common.ResourceBean;
import bitronix.tm.resource.common.XAResourceHolder;
import bitronix.tm.resource.common.XAResourceProducer;
import bitronix.tm.resource.common.XAStatefulHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.metadata.AnnotationClassMetaFactory;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.ClassDefinition;
import org.topazproject.otm.metadata.ClassBindings;
import org.topazproject.otm.query.DefaultQueryFunctionFactory;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Rdfs;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.Interceptor;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and model/graph configurations. This class is multi-thread safe,
 * so long as the preload and configuration  operations are done at boot-strap time.
 *
 * <p>Instances are preloaded with the following aliases by default: <var>rdf</var>,
 * <var>rdfs</var>, <var>owl</var>, <var>xsd</var>, <var>dc</var>, <var>dc_terms</var>,
 * <var>mulgara</var>, <var>topaz</var>. Also, the {@link DefaultQueryFunctionFactory
 * DefaultQueryFunctionFactory} is always added.
 *
 * @author Pradeep Krishnan
 */
public class SessionFactoryImpl implements SessionFactory {
  private static final Log log = LogFactory.getLog(SessionFactory.class);

  private static BitronixTransactionManager defTxnMgr;
  private static Object                     txnMgrCleaner;

  /**
   * definition name to definition map
   */
  private final Map<String, Definition> defs = new HashMap<String, Definition>();

  /**
   * class definition name to class-bindings map
   */
  private final Map<String, SFClassBindings> classDefs = new HashMap<String, SFClassBindings>();

  /**
   * rdf:type to entity mapping
   */
  private final Map<String, Set<ClassMetadata>> typemap = new HashMap<String, Set<ClassMetadata>>();

  /**
   * Name to metadata mapping.
   */
  private final Map<String, ClassMetadata> entitymap = new HashMap<String, ClassMetadata>();

  /**
   * Entity name to sub-class entity name mapping. Note that 'null' key is used to indicate
   * root classes.
   */
  private final Map<String, Set<ClassMetadata>> subClasses = new HashMap<String, Set<ClassMetadata>>();

  /**
   * Model to config mapping (uris, types etc.)
   */
  private final Map<String, ModelConfig> modelsByName = new HashMap<String, ModelConfig>();

  /**
   * Model-type to config mapping (uris, types etc.)
   */
  private final Map<URI, List<ModelConfig>> modelsByType = new HashMap<URI, List<ModelConfig>>();

  /**
   * Filter definitions by name.
   */
  private final Map<String, FilterDefinition> filterDefs = new HashMap<String, FilterDefinition>();

  /**
   * QueryFunction factories by function name.
   */
  private final Map<String, QueryFunctionFactory> qffMap =
                                                      new HashMap<String, QueryFunctionFactory>();

  /**
   * Aliases
   */
  private final Map<String, String> aliases = new HashMap<String, String>();

  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                tripleStore;
  private BlobStore                  blobStore;
  private TransactionManager         txMgr;
  private CurrentSessionContext      currentSessionContext;

  {
    // set up defaults
    addQueryFunctionFactory(new DefaultQueryFunctionFactory());

    addAlias("rdf",      Rdf.rdf);
    addAlias("rdfs",     Rdfs.base);
    addAlias("xsd",      Rdf.xsd);
    addAlias("dc",       Rdf.dc);
    addAlias("dcterms",  Rdf.dc_terms);
    addAlias("mulgara",  Rdf.mulgara);
    addAlias("topaz",    Rdf.topaz);
  }

  /*
   * inherited javadoc
   */
  public Session openSession() {
    return openSession(null);
  }

  /*
   * inherited javadoc
   */
  public Session openSession(Interceptor interceptor) {
    return new SessionImpl(this, interceptor);
  }

  /*
   * inherited javadoc
   */
  public Session getCurrentSession() throws OtmException {
    if (currentSessionContext == null)
      throw new OtmException("CurrentSessionContext is not configured");

    return currentSessionContext.currentSession();
  }

  /*
   * inherited javadoc
   */
  public void preload(Class<?>[] classes) throws OtmException {
    for (Class<?> c : classes)
      preload(c);
  }

  /*
   * inherited javadoc
   */
  public void preload(Class<?> c) throws OtmException {
    if ((c == null) || Object.class.equals(c))
      return;

    preload(c.getSuperclass());

    if (getClassMetadata(c) == null)
      setClassMetadata(cmf.create(c));
  }

  /*
   * inherited javadoc
   */
  public Definition getDefinition(String name) {
    return defs.get(name);
  }

  /*
   * inherited javadoc
   */
  public void addDefinition(Definition def) throws OtmException {
    if (defs.containsKey(def.getName()))
      throw new OtmException("Duplicate definition :" + def.getName());

    defs.put(def.getName(), def);

    if (log.isDebugEnabled())
      log.debug("Added definition : " + def.getName());

    if (def instanceof ClassDefinition)
      classDefs.put(def.getName(), new SFClassBindings((ClassDefinition)def));
  }

  /*
   * inherited javadoc
   */
  public void removeDefinition(String name) {
    defs.remove(name);
    classDefs.remove(name);

    if (log.isDebugEnabled())
      log.debug("Removed definition : " + name);
  }

  /*
   * inherited javadoc
   */
  public Collection<String> listClassDefinitions() {
    return classDefs.keySet();
  }


  /*
   * inherited javadoc
   */
  public  ClassBindings getClassBindings(String name) {
    return classDefs.get(name);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode, 
                                Collection<String> typeUris) {
    return getSubClassMetadata(clazz, mode, typeUris, true);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getAnySubClassMetadata(ClassMetadata clazz, Collection<String> typeUris) {
    return getSubClassMetadata(clazz, null, typeUris, false);
  }

  private ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode, 
                                Collection<String> typeUris, boolean instantiable) {
    if ((typeUris == null) || (typeUris.size() == 0))
      return (clazz == null) ? null : ((clazz.getType() != null) ? null : clazz);

    Set<ClassMetadata> solutions = null;

    if ((clazz != null) && (clazz.getType() == null)) {
      solutions = new HashSet<ClassMetadata>();
      solutions.add(clazz);
    }

    for (String uri : typeUris) {
      Set<ClassMetadata> classes = typemap.get(uri);

      if (classes == null)
        continue;

      Set<ClassMetadata> candidates = new HashSet<ClassMetadata>();

      for (ClassMetadata cl : classes) {
        if (instantiable && !cl.getEntityBinder(mode).isInstantiable())
          continue;
        if (typeUris.contains(cl.getType()) &&
           ((clazz == null) || clazz.isAssignableFrom(cl)))
          candidates.add(cl);
      }

      if ((solutions == null) || solutions.isEmpty())
        solutions = candidates;
      else if (!candidates.isEmpty()) {
        Set<ClassMetadata> intersection = new HashSet<ClassMetadata>(solutions);
        intersection.retainAll(candidates);
        if (intersection.isEmpty())
          solutions.addAll(candidates);
        else
          solutions = intersection;
      }
    }

    if (solutions == null)
      return null;

    if (solutions.size() == 1)
      return solutions.iterator().next();

    ClassMetadata random = null;
    for (ClassMetadata cl : solutions)
      if ((random == null) || random.isAssignableFrom(cl))
        random = cl;

    return random;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getInstanceMetadata(ClassMetadata clazz, EntityMode mode, Object instance) {
    if ((clazz != null) && !clazz.getEntityBinder(mode).isInstance(instance))
      return null;

    ClassMetadata candidate = clazz;
    Collection<ClassMetadata> sub = subClasses.get((clazz == null) ? null : clazz.getName());

    if (sub != null) {
      for (ClassMetadata s : sub) {
        ClassMetadata cm = getInstanceMetadata(s, mode, instance);
        if ((cm != null) && ((candidate == null) || candidate.isAssignableFrom(cm)))
          candidate = cm;
      }
    }
    return candidate;
  }

  /*
   * inherited javadoc
   */
  public void setClassMetadata(ClassMetadata cm) throws OtmException {
    for (String name : cm.getNames()) {
      ClassMetadata other = entitymap.get(name);
      if (other != null)
        throw new OtmException("An entity with name or alias of '" + name + "' already exists.");
    }

    for (String name : cm.getNames())
      entitymap.put(name, cm);

    for (String type : cm.getTypes()) {
      Set<ClassMetadata> set = typemap.get(type);

      if (set == null) {
        set = new HashSet<ClassMetadata>();
        typemap.put(type, set);
      }

      set.add(cm);
    }

    String             sup = cm.getSuperEntity();
    Set<ClassMetadata> set = subClasses.get(sup);

    if (set == null)
      subClasses.put(sup, set = new HashSet<ClassMetadata>());

    set.add(cm);

    if (log.isDebugEnabled())
      log.debug("Registered " + cm);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getClassMetadata(Class<?> clazz) {
    return getClassMetadata(clazz.getName());
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getClassMetadata(String entity) {
    return entitymap.get(entity);
  }

  /*
   * inherited javadoc
   */
  public Collection<ClassMetadata> listClassMetadata() {
    return new HashSet<ClassMetadata>(entitymap.values());
  }

  /*
   * inherited javadoc
   */
  public ModelConfig getModel(String modelId) {
    return modelsByName.get(modelId);
  }

  /*
   * inherited javadoc
   */
  public List<ModelConfig> getModels(URI modelType) {
    return modelsByType.get(modelType);
  }

  /*
   * inherited javadoc
   */
  public void addModel(ModelConfig model) {
    modelsByName.put(model.getId(), model);

    List<ModelConfig> models = modelsByType.get(model.getType());
    if (models == null)
      modelsByType.put(model.getType(), models = new ArrayList<ModelConfig>());
    models.add(model);
  }

  /*
   * inherited javadoc
   */
  public void removeModel(ModelConfig model) {
    modelsByName.remove(model.getId());

    List<ModelConfig> models = modelsByType.get(model.getType());
    if (models != null) {
      models.remove(model);
      if (models.size() == 0)
        modelsByType.remove(model.getType());
    }
  }

  /*
   * inherited javadoc
   */
  public TripleStore getTripleStore() {
    return tripleStore;
  }

  /*
   * inherited javadoc
   */
  public void setTripleStore(TripleStore store) {
    this.tripleStore = store;
  }

  /*
   * inherited javadoc
   */
  public BlobStore getBlobStore() {
    return blobStore;
  }

  /*
   * inherited javadoc
   */
  public void setBlobStore(BlobStore store) {
    this.blobStore = store;
  }

  public void setTransactionManager(TransactionManager tm) {
    this.txMgr = tm;
  }

  public TransactionManager getTransactionManager() throws OtmException {
    if (txMgr == null) {
      try {
        txMgr = getDefaultTransactionManager();
      } catch (RuntimeException re) {
        throw new OtmException("Failed to create default transaction-manager", re);
      }
    }

    return txMgr;
  }

  private static synchronized TransactionManager getDefaultTransactionManager() {
    if (defTxnMgr == null) {
      defTxnMgr = TransactionManagerServices.getTransactionManager();
      txnMgrCleaner = new Object() {
        protected void finalize() {
          defTxnMgr.shutdown();
        }
      };

      ResourceRegistrar.register(new SimpleXAResourceProducer());
    }

    return defTxnMgr;
  }

  /*
   * inherited javadoc
   */
  public CurrentSessionContext getCurrentSessionContext() {
    return currentSessionContext;
  }

  /*
   * inherited javadoc
   */
  public void setCurrentSessionContext(CurrentSessionContext currentSessionContext) {
    this.currentSessionContext = currentSessionContext;
  }

  /*
   * inherited javadoc
   */
  public SerializerFactory getSerializerFactory() {
    return serializerFactory;
  }

  /*
   * inherited javadoc
   */
  public void addFilterDefinition(FilterDefinition fd) {
    filterDefs.put(fd.getFilterName(), fd);
  }

  /*
   * inherited javadoc
   */
  public void removeFilterDefinition(String filterName) {
    filterDefs.remove(filterName);
  }

  /*
   * inherited javadoc
   */
  public Collection<FilterDefinition> listFilterDefinitions() {
    return new ArrayList<FilterDefinition>(filterDefs.values());
  }

  /** 
   * Get the filter definition for the named filter. 
   * 
   * @param name the name of the filter
   * @return the filter definition, or null
   */
  FilterDefinition getFilterDefinition(String name) {
    return filterDefs.get(name);
  }

  public void addQueryFunctionFactory(QueryFunctionFactory qff) {
    for (String name : qff.getNames())
      qffMap.put(name, qff);
  }

  public void removeQueryFunctionFactory(QueryFunctionFactory qff) {
    for (String name : qff.getNames())
      qffMap.remove(name);
  }

  public Set<QueryFunctionFactory> listQueryFunctionFactories() {
    return new HashSet(qffMap.values());
  }

  public QueryFunctionFactory getQueryFunctionFactory(String funcName) {
    return qffMap.get(funcName);
  }

  public void addAlias(String alias, String replacement) {
    aliases.put(alias, replacement);
  }

  public void removeAlias(String alias) {
    aliases.remove(alias);
  }

  public Map<String, String> listAliases() {
    return new HashMap<String, String>(aliases);
  }

  public String expandAlias(String uri) {
    for (String alias : aliases.keySet()) {
      if (uri.startsWith(alias + ":")) {
        uri = aliases.get(alias) + uri.substring(alias.length() + 1);
        break;
      }
    }
    return uri;
  }


  private class SFClassBindings extends ClassBindings {
    public SFClassBindings(ClassDefinition def) {
      super(def);
    }

    @Override
    public void bind(EntityMode mode, EntityBinder binder)
            throws OtmException {

      for (String alias : binder.getNames())
        if (entitymap.containsKey(alias))
          throw new OtmException("An entity with name or alias of '" + alias + "' already exists.");

      super.bind(mode, binder);

      // If we already have a class-metadata, then make that discoverable by
      // alternate names supplied by the binder.
      ClassMetadata cm = entitymap.get(getName());
      if (cm != null) {
        for (String alias : binder.getNames())
          entitymap.put(alias, cm);
      }
    }

    @Override
    public void addAndBindProperty(String prop, EntityMode mode, Binder binder) throws OtmException {
      if (getProperties().contains(prop) && entitymap.containsKey(getName()))
        throw new OtmException("Cannot add a new property to " + getName() 
            + " since a ClassMetadata is already created for this Class");
      super.addAndBindProperty(prop, mode, binder);
    }
  }

  private static class SimpleXAResourceProducer implements XAResourceProducer {
    private final Map<XAResource, WeakReference<XAResourceHolder>> xaresHolders =
                                    new WeakHashMap<XAResource, WeakReference<XAResourceHolder>>();

    public void init()                           { }
    public void close()                          { }
    public String getUniqueName()                { return "OTM-Simple-Resource-Producer"; }
    public XAResourceHolderState startRecovery() {
      return createResHolder(new RecoveryXAResource()).getXAResourceHolderState();
    }
    public void endRecovery()                    { }
    public Reference getReference()              { return null; }
    public XAStatefulHolder createPooledConnection(Object xaFactory, ResourceBean bean) {
      return null;
    }

    public XAResourceHolder findXAResourceHolder(final XAResource xaResource) {
      WeakReference<XAResourceHolder> resHolderRef = xaresHolders.get(xaResource);
      XAResourceHolder resHolder = (resHolderRef != null) ? resHolderRef.get() : null;

      if (resHolder == null)
        xaresHolders.put(xaResource, new WeakReference(resHolder = createResHolder(xaResource)));

      return resHolder;
    }

    private static XAResourceHolder createResHolder(XAResource xaResource) {
      ResourceBean rb = new ResourceBean() {
        public XAResourceProducer createResource() { return null; }
      };
      rb.setUniqueName(xaResource.getClass().getName() + System.identityHashCode(xaResource));
      rb.setApplyTransactionTimeout(true);

      XAResourceHolder resHolder = new SimpleXAResourceHolder(xaResource);
      resHolder.setXAResourceHolderState(new XAResourceHolderState(resHolder, rb));

      return resHolder;
    }

    private static class SimpleXAResourceHolder extends AbstractXAResourceHolder {
      private final XAResource xares;

      SimpleXAResourceHolder(XAResource xares) { this.xares = xares; }

      public void       close()                { }
      public Object     getConnectionHandle()  { return null; }
      public Date       getLastReleaseDate()   { return null; }
      public List       getXAResourceHolders() { return null; }
      public boolean    isEmulatingXA()        { return false; }
      public XAResource getXAResource()        { return xares; }
    }

    private static class RecoveryXAResource implements XAResource {
      public void start(Xid xid, int flags) { }
      public void end(Xid xid, int flags) { }
      public int prepare(Xid xid) { return XA_OK; }
      public void commit(Xid xid, boolean onePhase) { }
      public void rollback(Xid xid) { }
      public Xid[] recover(int flag) { return null; /* recovery not supported (yet) */ }
      public void forget(Xid xid) { }
      public int getTransactionTimeout() { return 10; }
      public boolean setTransactionTimeout(int transactionTimeout) { return false; }
      public boolean isSameRM(XAResource xaResource) { return xaResource == this; }
    }
  }
}
