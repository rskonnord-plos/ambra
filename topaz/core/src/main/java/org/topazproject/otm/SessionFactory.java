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
import java.lang.reflect.Modifier;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.mapping.SerializerFactory;
import org.topazproject.otm.metadata.AnnotationClassMetaFactory;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and model/graph configurations. This class is multi-thread safe,
 * so long as the preload and configuration  operations are done at boot-strap time.
 *
 * @author Pradeep Krishnan
 */
public class SessionFactory {
  private static final Log log = LogFactory.getLog(SessionFactory.class);

  /**
   * rdf:type to Class mapping.
   */
  private final Map<String, Set<Class>> classmap = new HashMap<String, Set<Class>>();

  /**
   * Class to metadata mapping.
   */
  private final Map<Class, ClassMetadata> metadata = new HashMap<Class, ClassMetadata>();

  /**
   * Class name to metadata mapping.
   */
  private final Map<String, ClassMetadata> cnamemap = new HashMap<String, ClassMetadata>();

  /**
   * Entity name to metadata mapping.
   */
  private final Map<String, ClassMetadata> entitymap = new HashMap<String, ClassMetadata>();

  /**
   * Class to proxy class mapping.
   */
  private final Map<Class, Class> proxyClasses = new HashMap<Class, Class>();

  /**
   * Model to config mapping (uris, types etc.)
   */
  private final Map<String, ModelConfig> models = new HashMap<String, ModelConfig>();

  /**
   * Filter definitions by name.
   */
  private final Map<String, FilterDefinition> filterDefs = new HashMap<String, FilterDefinition>();
  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                store;
  private CurrentSessionContext      currentSessionContext;

  /**
   * Open a new otm session.
   *
   * @return the newly created session
   */
  public Session openSession() {
    return new Session(this);
  }

  /**
   * Obtains the current session.  The definition of what exactly "current" means is
   * controlled by the {@link org.topazproject.otm.context.CurrentSessionContext} impl configured
   * for use.
   *
   * @return The current session.
   *
   * @throws OtmException Indicates an issue locating a suitable current session.
   */
  public Session getCurrentSession() throws OtmException {
    if (currentSessionContext == null)
      throw new OtmException("CurrentSessionContext is not configured");

    return currentSessionContext.currentSession();
  }

  /**
   * Preload some classes. Must be called as part of the factory initialization.
   *
   * @param classes the classes to load
   *
   * @throws OtmException on an error
   */
  public void preload(Class[] classes) throws OtmException {
    for (Class c : classes)
      preload(c);
  }

  /**
   * Preload a class. Must be called as part of the factory initialization.
   *
   * @param c the class to load
   *
   * @throws OtmException on an error
   */
  public void preload(Class c) throws OtmException {
    if ((c == null) || Object.class.equals(c))
      return;

    try {
      preload(c.getSuperclass());
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Preload: skipped for " + c.getSuperclass(), e);
    }

    ClassMetadata cm           = cmf.create(c);

    setClassMetadata(cm);
  }

  /**
   * Return the most specific subclass of clazz that is mapped to one of the given rdf types.
   * We assume that we don't have two subclasses of <code>clazz</code> that are associated with
   * the same rdf type. If this is not the case then the returned class will be randomly selected
   * from one of the available ones.
   *
   * @param clazz the super class
   * @param typeUris collection of type uris
   *
   * @return the most specific sub class
   */
  public Class mostSpecificSubClass(Class clazz, Collection<String> typeUris) {
    return mostSpecificSubClass(clazz, typeUris, false);
  }

  public Class mostSpecificSubClass(Class clazz, Collection<String> typeUris, boolean any) {
    if (typeUris.size() == 0)
      return clazz;

    ClassMetadata solution  = null;

    for (String uri : typeUris) {
      Set<Class> classes = classmap.get(uri);

      if (classes == null)
        continue;

      Class candidate = clazz;

      //find the most specific class with the same rdf:type
      for (Class cl : classes) {
        if (candidate.isAssignableFrom(cl) && (any || isInstantiable(cl)))
          candidate = cl;
      }

      if (classes.contains(candidate)) {
        ClassMetadata cm = metadata.get(candidate);
        if (solution == null)
          solution = cm;
        else if ((cm != null) && (solution.getTypes().size() < cm.getTypes().size()))
          solution = cm;
      }
    }

    return (solution != null) ? solution.getSourceClass() : null;
  }

  /**
   * Sets/registers a ClassMetadata.
   *
   * @param cm the class metadata
   *
   * @throws OtmException on an error
   */
  public void setClassMetadata(ClassMetadata cm) throws OtmException {
    if (entitymap.containsKey(cm.getName())
         && !entitymap.get(cm.getName()).getSourceClass().equals(cm.getSourceClass()))
      throw new OtmException("An entity with name '" + cm.getName() + "' already exists.");

    entitymap.put(cm.getName(), cm);

    Class c = cm.getSourceClass();
    metadata.put(c, cm);
    cnamemap.put(c.getName(), cm);
    if (cm.isEntity())
      createProxy(c, cm);

    String type = cm.getType();

    if (type != null) {
      Set<Class> set = classmap.get(type);

      if (set == null) {
        set = new HashSet<Class>();
        classmap.put(type, set);
      }

      set.add(c);
    }

    if (log.isDebugEnabled())
      log.debug("setClassMetadata: type(" + cm.getType() + ") ==> " + cm);
  }

  /**
   * Gets the class metadata of a pre-registered class.
   *
   * @param clazz the class.
   *
   * @return metadata for the class, or null if not found
   */
  public ClassMetadata getClassMetadata(Class clazz) {
    ClassMetadata cm = metadata.get(clazz);

    if (cm != null)
      return cm;

    clazz = getProxyMapping(clazz);

    if (clazz != null)
      cm = metadata.get(clazz);

    return cm;
  }

  /**
   * Gets the class metadata of a pre-registered entity. This first attempts to find a registered
   * class with an entity name of <var>entity</var>, and if that fails it looks for any registered
   * class with a fully-qualified class name of <var>entity</var>.
   *
   * @param entity the entity name or the fully qualified class name.
   *
   * @return metadata for the class, or null if not found
   */
  public ClassMetadata getClassMetadata(String entity) {
    ClassMetadata res = entitymap.get(entity);
    if (res == null)
      res = cnamemap.get(entity);

    return res;
  }

  /**
   * Lists all registered ClassMetadata objects. The returned collection is a snapshot at the
   * time of the call. New changes made via {@link #setClassMetadata} will not be reflected in the
   * returned collection.
   *
   * @return the collection of ClassMetadata
   */
  public Collection<ClassMetadata> listClassMetadata() {
    return new ArrayList<ClassMetadata>(metadata.values());
  }

  /**
   * Gets the proxy mapping. class to proxy or proxy to class.
   *
   * @param clazz the class or proxy
   *
   * @return proxy or class
   */
  public Class getProxyMapping(Class clazz) {
    return proxyClasses.get(clazz);
  }

  /**
   * Gets the model configuration.
   *
   * @param modelId the model id
   *
   * @return the configuration
   */
  public ModelConfig getModel(String modelId) {
    return models.get(modelId);
  }

  /**
   * Adds a model configuration.
   *
   * @param model the model configuration
   */
  public void addModel(ModelConfig model) {
    models.put(model.getId(), model);
  }

  /**
   * Gets the triple store used.
   *
   * @return the store
   */
  public TripleStore getTripleStore() {
    return store;
  }

  /**
   * Sets the triple store used
   *
   * @param store the store
   */
  public void setTripleStore(TripleStore store) {
    this.store = store;
  }

  /**
   * Get currentSessionContext.
   *
   * @return currentSessionContext as CurrentSessionContext.
   */
  public CurrentSessionContext getCurrentSessionContext() {
    return currentSessionContext;
  }

  /**
   * Set currentSessionContext.
   *
   * @param currentSessionContext the value to set.
   */
  public void setCurrentSessionContext(CurrentSessionContext currentSessionContext) {
    this.currentSessionContext = currentSessionContext;
  }

  /**
   * Gets the serializer factory used.
   *
   * @return the serializer factory
   */
  public SerializerFactory getSerializerFactory() {
    return serializerFactory;
  }

  /** 
   * Add a new filter definition. If one has already been registered with the same name it is
   * replaced. 
   * 
   * @param fd the filter definition to register 
   */
  public void addFilterDefinition(FilterDefinition fd) {
    filterDefs.put(fd.getFilterName(), fd);
  }

  /** 
   * Remove the filter definition with the given name. Does nothing if none was registered with
   * that name. 
   * 
   * @param filterName the filter name
   */
  public void removeFilterDefinition(String filterName) {
    filterDefs.remove(filterName);
  }

  /** 
   * List all registered filter definitions. 
   * 
   * @return the list of registered filter definitions; will be empty if no filter definitions have
   *         been registered.
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

  private boolean isInstantiable(Class clazz) {
    int mod = clazz.getModifiers();

    return !Modifier.isAbstract(mod) && !Modifier.isInterface(mod) && Modifier.isPublic(mod);
  }

  private void createProxy(Class clazz, ClassMetadata cm) {
    final Method getter = cm.getIdField().getGetter();

    MethodFilter mf     =
      new MethodFilter() {
        public boolean isHandled(Method m) {
          return !m.getName().equals("finalize") && !m.equals(getter);
        }
      };

    ProxyFactory f      = new ProxyFactory();
    f.setSuperclass(clazz);
    if (Serializable.class.isAssignableFrom(clazz))
      f.setInterfaces(new Class[]{WriteReplace.class});
    f.setFilter(mf);

    Class c = f.createClass();

    proxyClasses.put(clazz, c);
    proxyClasses.put(c, clazz);
  }

  public static interface WriteReplace {
    public Object writeReplace() throws ObjectStreamException;
  }
}
