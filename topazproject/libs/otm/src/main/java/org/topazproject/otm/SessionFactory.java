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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   * Class to proxy class mapping.
   */
  private final Map<Class, Class> proxyClasses = new HashMap<Class, Class>();

  /**
   * Model to config mapping (uris, types etc.)
   */
  private final Map<String, ModelConfig> models = new HashMap<String, ModelConfig>();
  private AnnotationClassMetaFactory cmf = new AnnotationClassMetaFactory(this);
  private SerializerFactory          serializerFactory = new SerializerFactory(this);
  private TripleStore                store;

  /**
   * Open a new otm session.
   *
   * @return the newly created session
   */
  public Session openSession() {
    return new Session(this);
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

    preload(c.getSuperclass());

    ClassMetadata cm           = cmf.create(c);

    setClassMetadata(cm);

    if (log.isDebugEnabled())
      log.debug("Preload: type(" + cm.getType() + ") ==> " + cm);
  }

  /**
   * Return the most specific subclass of clazz that is mapped to one of the given rdf types.
   * We assume that we don't have two subclasses of <code>clazz</code> that are associated with
   * the same rdf type. If this is not the case then the returned class will be randomly selected
   * from one of the available ones.
   *
   * @param clazz the super class
   * @param typeUris list of type uris
   *
   * @return the most specific sub class
   */
  public Class mostSpecificSubClass(Class clazz, List<String> typeUris) {
    Set<Class> mappedClasses = new HashSet<Class>();

    for (String uri : typeUris) {
      Set<Class> classes = classmap.get(uri);

      if (classes != null)
        mappedClasses.addAll(classes);
    }

    Class solution = clazz;

    //now we have to find the most specific class
    for (Class candidate : mappedClasses) {
      if (solution.isAssignableFrom(candidate) && isInstantiable(candidate))
        solution = candidate;
    }

    return solution;
  }

  /**
   * Sets/registers a ClassMetadata.
   *
   * @param cm the class metadata
   *
   * @throws OtmException on an error
   */
  public void setClassMetadata(ClassMetadata cm) throws OtmException {
    if (!cm.isEntity())
      throw new OtmException(cm.toString() + " is not an entity");

    Class c = cm.getSourceClass();
    metadata.put(c, cm);
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
  }

  /**
   * Gets the class metadata of a pre-registered class.
   *
   * @param clazz the class.
   *
   * @return metadata for the class
   */
  public ClassMetadata getClassMetadata(Class clazz) {
    return metadata.get(clazz);
  }

  /**
   * Gets the proxy mapping. class --> proxy or proxy --> class
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
   * Gets the serializer factory used.
   *
   * @return the serializer factory
   */
  public SerializerFactory getSerializerFactory() {
    return serializerFactory;
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
    f.setFilter(mf);

    Class c = f.createClass();

    proxyClasses.put(clazz, c);
    proxyClasses.put(c, clazz);
  }
}
