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
package org.topazproject.otm;

import java.net.URI;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.topazproject.otm.TripleStore.Result;
import org.topazproject.otm.context.CurrentSessionContext;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.metadata.ClassBinding;
import org.topazproject.otm.metadata.ClassDefinition;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.query.QueryFunctionFactory;
import org.topazproject.otm.serializer.SerializerFactory;

/**
 * A factory for otm sessions. It should be preloaded with the classes that would be persisted.
 * Also it holds the triple store and graph configurations. This class is multi-thread safe,
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
   * Open a new otm session with an Interceptor.
   *
   * @param interceptor the Interceptor to notify object state changes
   *
   * @return the newly created session
   */
  public Session openSession(Interceptor interceptor);

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
   * Preload all classes that can be found using the current class loader to
   * seach for the default resource marker.
   *
   * @throws OtmException on an error
   */
  public void preloadFromClasspath() throws OtmException;

  /**
   * Preload all classes that can be found using a given class loader
   * using the default resource marker.
   *
   * @param cl The class loader to use for searching for classes.
   *
   * @throws OtmException on an error
   */
  public void preloadFromClasspath(ClassLoader cl) throws OtmException;

  /**
   * Preload all classes that can be found with the current class loader and using
   * a given resource marker.
   *
   * @param res The name of a resource to use when searching the class path.
   *
   * @throws OtmException on an error
   */
  public void preloadFromClasspath(String res) throws OtmException;

  /**
   * Preload all classes that can be found with a given class loader and using
   * a given resource marker.
   *
   * @param res The name of a resource to use when searching the class path.
   * @param cl The class loader to use for searching for classes.
   *
   * @throws OtmException on an error
   */
  public void preloadFromClasspath(String res, ClassLoader cl) throws OtmException;

  /**
   * Validate the registered definitions and bindings and build the {@link ClassMetadata}
   * for all entities. Could be called incrementally as new definitions are added. But
   * the added definitions must all have resolvable references. eg. referece to an undefined
   * association or an undefined property definition could result in a failure here.
   *
   * @throws OtmException on an error
   */
  public void validate() throws OtmException;

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
   * List all definitions.
   *
   * @return the list of definitions
   *
   * @throws OtmException in case of an error
   */
  public Collection<Definition> listDefinitions();

  /**
   * Gets the list of all class bindings.
   *
   * @return all entity definitions
   */
  public Collection<? extends ClassBinding> listClassBindings();

  /**
   * Gets the binding for a class definition.
   *
   * @return the class bindings
   */
  public  ClassBinding getClassBinding(String name);

  /**
   * Return the most specific instantiable subclass metadata for the given class metadata that
   * is mapped to one of the given rdf types. If there are multiple candidates all matching
   * the given set of rdf:type values, other rdf statements are looked into for resolving
   * a subclass. For this to work, a {@link SubClassResolver} must be registered using the
   * {@link #addSubClassResolver} method. The registered SubClassResolvers are called in the
   * following order:
   * <ul>
   *   <li> resolvers registered for the sub-classes of potential candidates </li>
   *   <li> resolvers registered for the potential candidates </li>
   *   <li> resolvers registered for the super-classes of potential candidates </li>
   * </ul>
   * <p>
   * If any one of the resolvers returns a non-null sub-class, the scan is halted and no other
   * resolvers are consulted. For this reason resolvers at the leaf node levels are expected to
   * be as thorough as possible in positively identifying a sub-class and leave the guessing to
   * super-class resolvers. A resolver at a super-class level is may have a fall-back
   * default.
   * <p>
   * Note: There is no need to register a SubClassResolver for entities if all entities are
   * uniquely identifiable with rdf:type alone. Conversely, if there are multiple entities
   * for the same set of rdf:types, it is recommended that the application register
   * SubClassResolver(s) rather than relying on the random selection by OTM.
   *
   * @param clazz      the super class to start the search from or null
   * @param mode       the EntityMode in which the sub-class must be instantiable
   * @param typeUris   collection of type uris
   * @param statements rdf statements to further disambiguate. If null and multiple classes
   *                   qualify, then a candidate is randomly selected.
   * @return the most specific sub class
   */
  public ClassMetadata getSubClassMetadata(ClassMetadata clazz, EntityMode mode,
                                     Collection<String> typeUris, TripleStore.Result statements);

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
   * Gets the graph configuration.
   *
   * @param graphId the graph id
   *
   * @return the configuration
   */
  public GraphConfig getGraph(String graphId);

  /**
   * Get the graphs of the given type.
   *
   * @param graphType the graph type
   *
   * @return the list of graphs with the given type, or null if there are none
   */
  public List<GraphConfig> getGraphs(URI graphType);

  /**
   * Get all the graphs.
   * @return The complete collection of graphs.
   */
  public Collection<GraphConfig> listGraphs();

  /**
   * Adds a graph configuration.
   *
   * @param graph the graph configuration
   */
  public void addGraph(GraphConfig graph);

  /**
   * Removes a graph configuration.
   *
   * @param graph the graph configuration
   */
  public void removeGraph(GraphConfig graph);

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

  /**
   * Registers a conflict resolver to help with the most-specific-subclass determination.
   * The resolver may be called to resolve conflicts for any entity in the class-hierarchy
   * that this entity belongs to. (both up and down).
   *
   * @param entity the canonical name of an entity.
   *
   * @param resolver the resolver that may be called to resolve
   *
   * @see #getSubClassMetadata
   */
  public void addSubClassResolver(String entity, SubClassResolver resolver);

  /**
   * Removes a registered resolver.
   *
   * @param resolver the resolver to remove
   */
  public void removeSubClassResolver(SubClassResolver resolver);

  /**
   * Lists registered resolvers for an entity.
   *
   * @param entity the canonical name of an entity
   *
   * @return the collection of resolvers all registered with the same entity. Could be empty.
   */
  public Collection<SubClassResolver> listRegisteredSubClassResolvers(String entity);

  /**
   * Returns an ordered list of resolvers to be polled to resolve the most-specific-subclass.
   *
   * @param entity the canonical name of an entity
   *
   * @return the list of resolvers. Could be empty.
   */
  public LinkedHashSet<SubClassResolver> listEffectiveSubClassResolvers(String entity);
}
