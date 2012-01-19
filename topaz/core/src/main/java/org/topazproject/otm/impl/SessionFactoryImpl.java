/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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

import java.lang.annotation.Annotation;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.BinderFactory;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.metadata.AnnotationClassMetaFactory;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.ClassDefinition;
import org.topazproject.otm.metadata.PropertyDefinition;
import org.topazproject.otm.metadata.ClassBinding;
import org.topazproject.otm.metadata.EmbeddedDefinition;
import org.topazproject.otm.metadata.EntityDefinition;
import org.topazproject.otm.metadata.ViewDefinition;
import org.topazproject.otm.query.DefaultQueryFunctionFactory;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Rdfs;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.SearchStore;
import org.topazproject.otm.SubClassResolver;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.impl.btm.TransactionManagerHelper;

import org.topazproject.util.FileProcessor;
import org.topazproject.util.DirProcessor;
import org.topazproject.util.JarProcessor;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and graph configurations. This class is multi-thread safe,
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

  /**
   * The default marker resource to use when searching for Entity definitions on the classpath.
   */
  private static final String DEFAULT_RES_MARKER = "topaz.xml";

  /**
   * The filename extension given to class files.
   */
  private static final String CLASS_EXT = ".class";

  /**
   * The character used to separate packages in a class name.
   */
  private static final String DOT = ".";

  /**
   * definition name to definition map
   */
  private final Map<String, Definition> defs = new HashMap<String, Definition>();

  /**
   * class definition name to class-bindings map
   */
  private final Map<String, SFClassBindings> classDefs = new HashMap<String, SFClassBindings>();

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
   * Graph to config mapping (uris, types etc.)
   */
  private final Map<String, GraphConfig> graphsByName = new HashMap<String, GraphConfig>();

  /**
   * Graph-type to config mapping (uris, types etc.)
   */
  private final Map<URI, List<GraphConfig>> graphsByType = new HashMap<URI, List<GraphConfig>>();

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

  private final Map<String, Set<SubClassResolver>> subClassResolvers
                                                  = new HashMap<String, Set<SubClassResolver>>();

  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                tripleStore;
  private BlobStore                  blobStore;
  private TransactionManager         txMgr;
  private CurrentSessionContext      currentSessionContext;
  private boolean                    validated = true;

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

    // set up root of sub-class heirarchy
    subClasses.put(null, new HashSet<ClassMetadata>());
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
    // Check if already preloaded
    ClassBinding cb = getClassBinding(cmf.getEntityName(c));

    if (cb != null) {
      EntityBinder b = cb.getBinders().get(EntityMode.POJO);

      if ((b != null) && (b instanceof ClassBinder) && c.equals(((ClassBinder) b).getSourceClass()))
        c = null;
    }

    if ((c != null) && !Object.class.equals(c)) {
      preload(c.getSuperclass());
      for (Class<?> i : c.getInterfaces())
        preload(i);
      cmf.create(c);
      if (log.isDebugEnabled())
        log.debug("pre-loaded: " + c);
    }
  }

  public void validate() throws OtmException {
    if (validated)
      return;

    HashSet<EntityDefinition>  entities = new HashSet<EntityDefinition>();
    HashSet<ViewDefinition>  views      = new HashSet<ViewDefinition>();

    for (Definition def : defs.values()) {
      def.resolveReference(this);
      if (def instanceof EntityDefinition)
        entities.add((EntityDefinition) def);
      else if (def instanceof ViewDefinition)
        views.add((ViewDefinition) def);
    }

    for (EntityDefinition def : entities) {
        buildClassMetadata(def);
    }

    for (ViewDefinition def : views) {
      if (entitymap.get(def.getName()) == null)
        setClassMetadata(def.buildClassMetadata(this));
    }

    validated = true;
  }

  private void buildClassMetadata(EntityDefinition def) throws OtmException {
    if (entitymap.get(def.getName()) != null)
      return;

    for (String sup : def.getSuperEntities()) {
      Definition d = defs.get(sup);

      if (!(d instanceof EntityDefinition))
        throw new OtmException("Invalid super '" + sup + "' in " + def.getName());

      buildClassMetadata((EntityDefinition) d);
    }

    for (String prop : getClassBinding(def.getName()).getProperties()) {
      Definition d = defs.get(prop);
      if (d instanceof EmbeddedDefinition)
        buildClassMetadata((EntityDefinition) defs.get(((EmbeddedDefinition) d).getEmbedded()));
    }

    setClassMetadata(def.buildClassMetadata(this));
  }

  /*
   * inherited javadoc
   */
  public void preloadFromClasspath() throws OtmException {
    preloadFromClasspath(DEFAULT_RES_MARKER);
  }

  /*
   * inherited javadoc
   */
  public void preloadFromClasspath(ClassLoader cl) throws OtmException {
    preloadFromClasspath(DEFAULT_RES_MARKER, cl);
  }

  /*
   * inherited javadoc
   */
  public void preloadFromClasspath(String res) throws OtmException {
    preloadFromClasspath(res, Thread.currentThread().getContextClassLoader());
  }

  /*
   * inherited javadoc
   */
  public void preloadFromClasspath(String res, ClassLoader cl) throws OtmException {
    try {
      Enumeration<URL> rs = cl.getResources(res);
      while (rs.hasMoreElements())
        new ResourceProcessor(rs.nextElement()).run();
    } catch (IOException e) {
      throw new OtmException("Unable to load resources", e);
    }
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

    validated = false;
    defs.put(def.getName(), def);

    if (log.isDebugEnabled())
      log.debug("Added definition : " + def.getName());

    if (def instanceof ClassDefinition)
      classDefs.put(def.getName(), new SFClassBindings((ClassDefinition) def));
  }

  /*
   * inherited javadoc
   */
  public void removeDefinition(String name) {
    validated = false;
    defs.remove(name);
    classDefs.remove(name);

    if (log.isDebugEnabled())
      log.debug("Removed definition : " + name);
  }

  public Collection<Definition> listDefinitions() {
    return Collections.unmodifiableCollection(defs.values());
  }

  /*
   * inherited javadoc
   */
  public Collection<? extends ClassBinding> listClassBindings() {
    return Collections.unmodifiableCollection(classDefs.values());
  }


  /*
   * inherited javadoc
   */
  public  ClassBinding getClassBinding(String name) {
    return classDefs.get(name);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getAnySubClassMetadata(ClassMetadata clazz, Collection<String> typeUris) {
    return getSubClassMetadata(clazz, null, typeUris, null);
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode,
                                Collection<String> typeUris, TripleStore.Result result) {
    Set<ClassMetadata> candidates = new HashSet<ClassMetadata>();

    if (typeUris == null)
      typeUris = Collections.emptySet();

    buildCandidates(candidates, clazz, clazz, mode, typeUris);

    Set<ClassMetadata> solutions = new HashSet<ClassMetadata>(candidates);

    // Eliminate super classes from an rdf:type perspective
    for (ClassMetadata sup : candidates) {
      for (ClassMetadata cm : candidates) {
        if ((sup != cm) && cm.getAllTypes().size() > sup.getAllTypes().size()
                        && cm.getAllTypes().containsAll(sup.getAllTypes())) {
          solutions.remove(sup);
          break;
        }
      }
    }

    if (solutions.isEmpty())
      return null;

    if (solutions.size() == 1)
      return solutions.iterator().next();

    // narrow down based on other rdf statements
    if (result != null) {
      LinkedHashSet<SubClassResolver> resolvers = new LinkedHashSet<SubClassResolver>();

      // resolvers for sub-classes of the solutions (excluding the solutions)
      for (ClassMetadata cl : solutions)
        gatherSub(cl.getName(), resolvers);
      for (ClassMetadata cl : solutions)
        resolvers.removeAll(listRegisteredSubClassResolvers(cl.getName()));

      // resolvers for the solutions
      for (ClassMetadata cl : solutions)
        resolvers.addAll(listRegisteredSubClassResolvers(cl.getName()));

      // resolvers for the super-classes
      for (ClassMetadata cl : solutions)
        gatherSup(cl.getName(), resolvers);

      // add the root as the last
      Set<SubClassResolver> rs = subClassResolvers.get(null);
      if (rs != null)
        resolvers.addAll(rs);

      for (SubClassResolver r : resolvers) {
        ClassMetadata cm = r.resolve(clazz, mode, this, typeUris, result);
        if ((cm != null) && isAcceptable(cm, clazz, mode, typeUris))
          return cm;
      }
    }

    // That didn't help. Eliminate super classes from an EntityBinder perspective
    if (mode != null) {
      candidates = new HashSet<ClassMetadata>(solutions);
      for (ClassMetadata sup : candidates) {
        EntityBinder supBinder = sup.getEntityBinder(mode);
        for (ClassMetadata cm : candidates) {
          EntityBinder binder = cm.getEntityBinder(mode);
          if ((sup != cm)  && supBinder.isAssignableFrom(binder)) {
            solutions.remove(sup);
            break;
          }
        }
      }
    }

    ClassMetadata solution = solutions.iterator().next();

    if (solutions.size() > 1) {
      // That didn't help either. Pick the first in the solutions set
      log.warn("Randomly chose " + solution + " as a subclass for " + clazz
          + " from the set " + solutions);
    }

    return solution;
  }

  private void buildCandidates(Collection<ClassMetadata> candidates, ClassMetadata cm,
                               ClassMetadata clazz, EntityMode mode, Collection<String> typeUris) {
    String key = (cm == null) ? null : cm.getName();
    Set<ClassMetadata> subs = subClasses.get(key);
    if (subs != null)
      for (ClassMetadata cl : subs)
        buildCandidates(candidates, cl, clazz, mode, typeUris);

    if ((cm != null) && isAcceptable(cm, clazz, mode, typeUris))
        candidates.add(cm);
  }

  private boolean isAcceptable(ClassMetadata cm, ClassMetadata clazz, EntityMode mode,
                               Collection<String> typeUris) {
    // assignable test
    if ((clazz != null) && !clazz.isAssignableFrom(cm, mode))
      return false;

    // type membership test
    if (!typeUris.containsAll(cm.getTypes()))
      return false;

    // instantiability test
    if ((mode != null) && !cm.getEntityBinder(mode).isInstantiable())
      return false;

    return true;
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
        if ((cm != null) && ((candidate == null) || candidate.isAssignableFrom(cm, mode)))
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

    for (String name : cm.getNames()) {
      entitymap.put(name, cm);
      if (log.isDebugEnabled())
        log.info("Registered: " + name + " as " + cm);
    }

    Set<ClassMetadata> set = null;
    for (String sup : cm.getSuperEntities()) {
      set = subClasses.get(sup);

      if (set == null)
        subClasses.put(sup, set = new HashSet<ClassMetadata>());

      set.add(cm);
    }

    // add to root
    if (set == null)
      subClasses.get(null).add(cm);
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
  public GraphConfig getGraph(String graphId) {
    return graphsByName.get(graphId);
  }

  /*
   * inherited javadoc
   */
  public List<GraphConfig> getGraphs(URI graphType) {
    return graphsByType.get(graphType);
  }

  /**
   * {@inheritDoc}
   */
  public Collection<GraphConfig> listGraphs() {
    return graphsByName.values();
  }

  /*
   * inherited javadoc
   */
  public void addGraph(GraphConfig graph) {
    graphsByName.put(graph.getId(), graph);

    List<GraphConfig> graphs = graphsByType.get(graph.getType());
    if (graphs == null)
      graphsByType.put(graph.getType(), graphs = new ArrayList<GraphConfig>());
    graphs.add(graph);
  }

  /*
   * inherited javadoc
   */
  public void removeGraph(GraphConfig graph) {
    graphsByName.remove(graph.getId());

    List<GraphConfig> graphs = graphsByType.get(graph.getType());
    if (graphs != null) {
      graphs.remove(graph);
      if (graphs.size() == 0)
        graphsByType.remove(graph.getType());
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

  protected SearchStore getSearchStore() {
    return (SearchStore) tripleStore;
  }

  public void setTransactionManager(TransactionManager tm) {
    this.txMgr = tm;
  }

  /**
   * Get JTA transaction manager. If non is set in setTransactionManager, use the default.
   *
   * @return JTA transaction manager
   * @throws OtmException
   */
  public TransactionManager getTransactionManager() throws OtmException {
    if (txMgr != null) {
      return txMgr;
    } else {
      return TransactionManagerHelper.getTransactionManager();
    }
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
    return new HashSet<QueryFunctionFactory>(qffMap.values());
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

  public void addSubClassResolver(String entity, SubClassResolver resolver) {
    Set<SubClassResolver> resolvers = subClassResolvers.get(entity);
    if (resolvers == null) {
      resolvers = new HashSet<SubClassResolver>();
      subClassResolvers.put(entity, resolvers);
    }
    resolvers.add(resolver);
  }

  public LinkedHashSet<SubClassResolver> listEffectiveSubClassResolvers(String entity) {
    LinkedHashSet<SubClassResolver> resolvers = new LinkedHashSet<SubClassResolver>();
    gatherSub(entity, resolvers);
    gatherSup(entity, resolvers);
    // add the root as the last
    Set<SubClassResolver> r = subClassResolvers.get(null);
    if (r != null)
      resolvers.addAll(r);
    return resolvers;
  }

  private void gatherSub(String entity, LinkedHashSet<SubClassResolver> resolvers) {
    Set<ClassMetadata> subs = subClasses.get(entity);
    if (subs != null)
      for (ClassMetadata cm : subClasses.get(entity))
        gatherSub(cm.getName(), resolvers);

    Set<SubClassResolver> r = subClassResolvers.get(entity);
    if (r != null)
      resolvers.addAll(r);
  }

  private void gatherSup(String entity, LinkedHashSet<SubClassResolver> resolvers) {
    ClassMetadata cm = getClassMetadata(entity);
    Set<String> sups = null;
    if (cm != null)
      sups = cm.getSuperEntities();

    if (sups != null) {
      // breadth first
      for (String s : sups) {
        Set<SubClassResolver> r = subClassResolvers.get(s);
        if (r != null)
          resolvers.addAll(r);
      }
      // now depth
      for (String s : sups)
        gatherSup(s, resolvers);
    }
  }

  public Collection<SubClassResolver> listRegisteredSubClassResolvers(String entity) {
    Set<SubClassResolver> resolvers = subClassResolvers.get(entity);
    if (resolvers == null)
      return Collections.emptySet();

    return Collections.unmodifiableSet(resolvers);
  }

  public void removeSubClassResolver(SubClassResolver resolver) {
    for (Set<SubClassResolver> resolvers : subClassResolvers.values())
      resolvers.remove(resolver);
  }

  private class SFClassBindings extends ClassBinding {
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
    public void addBinderFactory(BinderFactory bf) throws OtmException {
      ClassMetadata cm = entitymap.get(getName());
      Mapper m = null;
      if (cm != null) {
        Definition d = defs.get(bf.getPropertyName());
        if (!(d instanceof PropertyDefinition))
          throw new OtmException("No such property definition: " + bf.getPropertyName());

        PropertyDefinition pd = (PropertyDefinition) d;
        m  = cm.getMapperByName(pd.getLocalName());
        if (m == null)
          throw new OtmException("Cannot add a new property to " + getName()
             + " since a ClassMetadata is already created for this Class");
      }
      super.addBinderFactory(bf);

      if (m != null)
        m.getBinders().put(bf.getEntityMode(), bf.createBinder(SessionFactoryImpl.this));
    }
  }


  /**
   * This class is used to take resources requested from the classpath,
   * and use them as the basis for finding Entity classes that need to be
   * registered with this SessionFactory.
   */
  private class ResourceProcessor implements Runnable {
    private static final String JAR = "jar";
    private static final String FILE = "file";
    private static final String FILE_PROT = FILE + ":";
    private static final String BANG = "!";

    /** The path root of all the resources to be found by this processor. */
    private URL resRoot;

    /**
     * Create a resource processor for handling resources in the same part of
     * the classpath as a single resource identified by the given URL.
     * @param resRoot A resource at the root of the classpath to scan.
     */
    public ResourceProcessor(URL resRoot) {
      this.resRoot = resRoot;
    }

    /**
     * Determine the type of resource to be processed,
     * and send it to the appropriate processor type.
     */
    public void run() {
      String protocol = resRoot.getProtocol();
      String path = resRoot.getPath();
      if (protocol.equals(JAR)) {
        if (path.startsWith(FILE_PROT))
          processJar(path.substring(FILE_PROT.length(), path.indexOf(BANG)));
        else
          processUnknown(path);
      } else if (protocol.equals(FILE))
        processPath(new File(path).getParent());
      else
        processUnknown(resRoot.toString());
    }

    /**
     * Deal with resources that we don't understand.
     * @param u The string representation of the URL for the resource root.
     */
    void processUnknown(String u) {
      log.info("Unable to search <" + u + "> for Entity classes");
    }

    /**
     * Process a Jar path.
     * @param path The path of the Jar to be processed.
     */
    void processJar(String path) {
      try {
        int fileCount = new JarProcessor(new EntityFileProcessor(""), CLASS_EXT).process(path);
        log.info("Loaded " + fileCount + " Entities from " + path);
      } catch (IOException e) {
        log.error("Error while searching classpath for entities", e);
      }
    }

    /**
     * Take a filesystem path, and process everything under it.
     * @param path The filesystem path to process.
     */
    void processPath(final String path) {
      // create a processor that can test files and load them if we want them.
      FileProcessor fp = new EntityFileProcessor(path);

      // process the path for class files
      try {
        int fileCount = new DirProcessor(fp, CLASS_EXT).process(path);
        log.info("Loaded " + fileCount + " Entities from the filesystem");
      } catch (IOException e) {
        log.error("Error while searching classpath for entities", e);
      }
    }

  }

  /**
   * This class is a functor for processing classes that have entity annotations.
   */
  private class EntityFileProcessor implements FileProcessor {
    private String pathRoot;

    /**
     * Create a new functor that will look for classes rooted with a particular path.
     * @param pathRoot The root of the classes. This is part of a greater classpath.
     */
    public EntityFileProcessor(String pathRoot) {
      this.pathRoot = pathRoot;
    }

    /**
     * Processes a file. This will preload a file if it is a class that is annotated as an Entity.
     * @param f The path to process.
     * @return The number of files processed. 0 or 1.
     */
    public Integer fn(String f) {
      Class<?> c = null;
      try {
        c = Class.forName(toClassName(pathRoot, f));
        for (Class<? extends Annotation> a: cmf.getKnownAnnotations()) {
          if (c.isAnnotationPresent(a)) {
            preload(c);
            return 1;
          }
        }
      } catch (OtmException oe) {
        log.warn("Unable to preload annotated class: " + c.getName(), oe);
      } catch (ClassNotFoundException cnf) {
        if (log.isDebugEnabled())
          log.debug("Class file on scanned classpath could not be loaded: " + f, cnf);
      } catch (NoClassDefFoundError ncd) {
        if (log.isDebugEnabled())
          log.debug("Class on scanned classpath is missing a required reference: " + f, ncd);
      } catch (Throwable t) {
        log.warn("Unexpected error when attempting to preload <" + f + ">", t);
      }
      return 0;
    }
  }

  /**
   * Take an absolute path to a class file, and convert it to a class name.
   * @param root The path to the point where the class hierarchy starts.
   *             This will be empty for a Jar file.
   * @param path The path for the class file.
   */
  private static String toClassName(String root, String path) {
    assert path.endsWith(CLASS_EXT) : "Conversion to class on: " + path;
    int start = root.length();
    if (start != 0 && !root.endsWith(File.separator))
      start++;
    path = path.substring(start, path.length() - CLASS_EXT.length());

    String pathSeparator = (root.length() == 0) ? JarProcessor.JAR_PATH_SEP : File.separator;
    return path.replace(pathSeparator, DOT);
  }

}
