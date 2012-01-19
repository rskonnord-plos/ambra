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

import java.net.URI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.metadata.ClassBindings;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and model/graph configurations. This class is multi-thread safe,
 * so long as the preload and configuration  operations are done at boot-strap time.
 *
 * @author Pradeep Krishnan
 */
public interface SessionFactory {

  /**
   * Open a new otm session.
   *
   * @return the newly created session
   */
  public Session openSession();

  /**
   * Obtains the current session.  The definition of what exactly "current" means is
   * controlled by the {@link org.topazproject.otm.context.CurrentSessionContext} impl configured
   * for use.
   *
   * @return The current session.
   *
   * @throws OtmException Indicates an issue locating a suitable current session.
   */
  public Session getCurrentSession() throws OtmException;

  /**
   * Preload some classes. Must be called as part of the factory initialization.
   *
   * @param classes the classes to load
   *
   * @throws OtmException on an error
   */
  public void preload(Class<?>[] classes) throws OtmException;

  /**
   * Preload a class. Must be called as part of the factory initialization.
   *
   * @param c the class to load
   *
   * @throws OtmException on an error
   */
  public void preload(Class<?> c) throws OtmException;

  /**
   * Gets a previously added definition.
   *
   * @param name the name of the definition.
   *
   * @return the definition or null
   */
  public Definition getDefinition(String name);

  /**
   * Adds a new definition. 
   *
   * @param def the definition to add
   *
   * @throws OtmException in case of a duplicate
   */
  public void addDefinition(Definition def) throws OtmException;

  /**
   * Removes a definition.
   *
   * @param name the name of the definition.
   *
   * @throws OtmException in case of an error
   */
  public void removeDefinition(String name);

  /**
   * Gets the list of all class definitions.
   *
   * @return all entity definitions
   */
  public Collection<String> listClassDefinitions();

  /**
   * Gets the binding for a class definition.
   *
   * @return the class bindings
   */
  public  ClassBindings getClassBindings(String name);

  /**
   * Return the most specific instantiable subclass metadata for the given class metadata that 
   * is mapped to one of the given rdf types. We assume that we don't have two subclasses that 
   * are associated with the same rdf type. If this is not the case then the returned class will 
   * be randomly selected from one of the available ones.
   *
   * @param clazz    the super class to start the search from or null
   * @param mode     the EntityMode in which the sub-class must be instantiable
   * @param typeUris collection of type uris
   *
   * @return the most specific sub class
   */
  public ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode, 
                                         Collection<String> typeUris);

  /**
   * Returns the most specific subclass metadata. Similar to {@link #getSubClassMetadata}, 
   * except the class returned may not be instantiable in a given EntityMode.
   *
   * @param clazz    the super class to start the search from or null
   * @param typeUris collection of type uris
   *
   * @return the most specific sub class
   */
  public ClassMetadata getAnySubClassMetadata(ClassMetadata clazz, Collection<String> typeUris);

  /**
   * Returns the class metadata for the given object instance and {@link EntityMode}.
   *
   * @param clazz    the super class to start search from or null
   * @param mode     the EntityMode of the object instance
   * @param instance the object instance
   *
   * @return the metadata for the given instane or null
   */
  public ClassMetadata getInstanceMetadata(ClassMetadata clazz, EntityMode mode, Object instance);

  /**
   * Sets/registers a ClassMetadata.
   *
   * @param cm the class metadata
   *
   * @throws OtmException on an error
   *
   * @deprecated exists only to support the RdfBuilder
   */
  public void setClassMetadata(ClassMetadata cm) throws OtmException;

  /**
   * Gets the class metadata of a pre-registered class. <b>WARN: This should
   * be used only when EntityMode is POJO.</b>
   *
   * @param clazz the class.
   *
   * @return metadata for the class, or null if not found
   */
  public ClassMetadata getClassMetadata(Class<?> clazz);

  /**
   * Gets the class metadata of a pre-registered entity. This first attempts to find a registered
   * class with an entity name of <var>entity</var>, and if that fails it looks for any registered
   * class with a fully-qualified class name of <var>entity</var>.
   *
   * @param entity the entity name or the fully qualified class name.
   *
   * @return metadata for the class, or null if not found
   */
  public ClassMetadata getClassMetadata(String entity);

  /**
   * Lists all registered ClassMetadata objects. The returned collection is a snapshot at the
   * time of the call. New changes made via {@link #setClassMetadata} will not be reflected in the
   * returned collection.
   *
   * @return the collection of ClassMetadata
   */
  public Collection<ClassMetadata> listClassMetadata();

  /**
   * Gets the model configuration.
   *
   * @param modelId the model id
   *
   * @return the configuration
   */
  public ModelConfig getModel(String modelId);

  /**
   * Get the models of the given type.
   *
   * @param modelType the model type
   *
   * @return the list of models with the given type, or null if there are none
   */
  public List<ModelConfig> getModels(URI modelType);

  /**
   * Adds a model configuration.
   *
   * @param model the model configuration
   */
  public void addModel(ModelConfig model);

  /**
   * Removes a model configuration.
   *
   * @param model the model configuration
   */
  public void removeModel(ModelConfig model);

  /**
   * Gets the triple store used.
   *
   * @return the store
   */
  public TripleStore getTripleStore();

  /**
   * Sets the triple store used
   *
   * @param store the store
   */
  public void setTripleStore(TripleStore store);

  /**
   * Gets the blob store used.
   *
   * @return the store
   */
  public BlobStore getBlobStore();

  /**
   * Sets the blob store used
   *
   * @param store the store
   */
  public void setBlobStore(BlobStore store);

  /** 
   * Set the JTA transaction-manager to use. If not set, a default (internal) tm will be used. 
   * 
   * @param tm  the transaction-manager to use
   */
  public void setTransactionManager(TransactionManager tm);

  /** 
   * Get the current JTA transaction-manager.
   * 
   * @return the transaction manager
   * @throws OtmException if an error occurred initializing the default transaction manager
   */
  public TransactionManager getTransactionManager() throws OtmException;

  /**
   * Get currentSessionContext.
   *
   * @return currentSessionContext as CurrentSessionContext.
   */
  public CurrentSessionContext getCurrentSessionContext();

  /**
   * Set currentSessionContext.
   *
   * @param currentSessionContext the value to set.
   */
  public void setCurrentSessionContext(CurrentSessionContext currentSessionContext);

  /**
   * Gets the serializer factory used.
   *
   * @return the serializer factory
   */
  public SerializerFactory getSerializerFactory();

  /** 
   * Add a new filter definition. If one has already been registered with the same name it is
   * replaced. 
   * 
   * @param fd the filter definition to register 
   */
  public void addFilterDefinition(FilterDefinition fd);

  /** 
   * Remove the filter definition with the given name. Does nothing if none was registered with
   * that name. 
   * 
   * @param filterName the filter name
   */
  public void removeFilterDefinition(String filterName);

  /** 
   * List all registered filter definitions. 
   * 
   * @return the list of registered filter definitions; will be empty if no filter definitions have
   *         been registered.
   */
  public Collection<FilterDefinition> listFilterDefinitions();

  /** 
   * Register a new query-function-factory. The factory is registered for each function name it
   * exports, replacing any previous factory defined for each name.
   * 
   * @param qff the query-function-factory to register
   */
  public void addQueryFunctionFactory(QueryFunctionFactory qff);

  /** 
   * Unregister a query-function-factory. The factory is unregistered for each function name it
   * exports.
   * 
   * @param qff the query-function-factory to unregister
   */
  public void removeQueryFunctionFactory(QueryFunctionFactory qff);

  /** 
   * List all registered query-function-factories. 
   * 
   * @return the set of registered query-function-factories
   */
  public Set<QueryFunctionFactory> listQueryFunctionFactories();

  /** 
   * Get the query-function-factory for the given function. 
   * 
   * @param funcName the name of the function for which to return the function-factory
   * @return the function-factory, or null if not found
   */
  public QueryFunctionFactory getQueryFunctionFactory(String funcName);

  /** 
   * Add an alias to the list. 
   * 
   * @param alias       the alias to add
   * @param replacement the string being aliased
   */
  public void addAlias(String alias, String replacement);

  /** 
   * Remove an alias from the list. 
   * 
   * @param alias the alias to remove
   */
  public void removeAlias(String alias);

  /** 
   * Get the current list of aliases. 
   * 
   * @return a map where the keys are the aliases and the values are the replacement strings
   */
  public Map<String, String> listAliases();

  /** 
   * Perform alias expansion on the uri. The uri must start with '&lt;alias&gt;:' (e.g. 'rdf:')
   * in order for expansion to occur. Expansion is not recursive.
   * 
   * @param uri the uri on which to perform alias expansion
   * @return the uri with the alias (if any) expanded
   */
  public String expandAlias(String uri);
}
