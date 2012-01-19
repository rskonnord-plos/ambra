/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.mulgara.resolver;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.log4j.Logger;

import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.server.SessionFactory;

/** 
 * The factory for {@link FilterResolver}s.
 *
 * <p>The configuration for this and for the {@link FilterHandler}s is retrieved as follows:
 * <ol>
 *   <li>If {@link #CONFIG_FACTORY_CONFIG_PROPERTY} system property has been set, use it as the
 *       location of the config; otherwise use {@link #DEFAULT_FACTORY_CONFIG}.</li>
 *   <li>If the resulting location string starts with a '/' assume it's a resource and try to
 *       load it from the classpath; otherwise assume it's a URL.</li>
 *   <li>The resulting config is passed to {@link DefaultConfigurationBuilder}.
 * </ol>
 * 
 * @author Ronald Tschalär
 */
public class FilterResolverFactory implements ResolverFactory {
  /**
   * the system property that can be used to defined a non-default configuration-factory config:
   * {@value}
   */
  public static final String CONFIG_FACTORY_CONFIG_PROPERTY =
                                            "org.topazproject.mulgara.resolver.configuration";

  /** the location of the default factory config: {@value} */
  public static final String DEFAULT_FACTORY_CONFIG         = "/conf/topaz-factory-config.xml";

  private static final Logger logger = Logger.getLogger(FilterResolverFactory.class);

  private final URI             dbURI;
  private final long            sysGraphType;
  private final FilterHandler[] handlers;

  /** 
   * Create a new filter-resolver-factory instance. 
   * 
   * @param initializer the factory initializer
   * @return the new filter-resolver-factory instance
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
    throws InitializerException
  {
    return new FilterResolverFactory(initializer);
  }

  /**
   * Instantiate a {@link FilterResolverFactory}.
   */
  private FilterResolverFactory(ResolverFactoryInitializer resolverFactoryInitializer)
      throws InitializerException {
    // Validate parameters
    if (resolverFactoryInitializer == null)
      throw new IllegalArgumentException("Null \"resolverFactoryInitializer\" parameter");

    // Claim the filter graph type
    resolverFactoryInitializer.addModelType(FilterResolver.GRAPH_TYPE, this);

    /*
      Nasty hack to deal with change from "models" to "graphs"
      Necessary for WebAppListenerInitModels.dropObsoleteGraphs() to work
      TODO: Remove this after 0.9.2
    */
    resolverFactoryInitializer.addModelType(URI.create("http://topazproject.org/models#filter"), this);
    // end nasty hack
  
    // remember the database uri
    dbURI = resolverFactoryInitializer.getDatabaseURI();

    // remember the system-graph type
    sysGraphType = resolverFactoryInitializer.getSystemModelType();

    // load the configuration
    Configuration config = null;

    String fConf = System.getProperty(CONFIG_FACTORY_CONFIG_PROPERTY, DEFAULT_FACTORY_CONFIG);
    URL fConfUrl = null;
    try {
      fConfUrl = fConf.startsWith("/") ? getClass().getResource(fConf) : new URL(fConf);
      if (fConfUrl == null)
        throw new InitializerException("'" + fConf + "' not found in classpath");

      logger.info("Using filter-resolver config '" + fConfUrl + "'");

      DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(fConfUrl);
      config = builder.getConfiguration();
    } catch (MalformedURLException mue) {
      throw new InitializerException("Error parsing '" + fConf + "'", mue);
    } catch (ConfigurationException ce) {
      throw new InitializerException("Error reading '" + fConfUrl + "'", ce);
    } catch (RuntimeException re) {
      throw new InitializerException("Configuration error '" + fConfUrl + "'", re);
    }

    String base = "topaz.fr";
    config = config.subset(base);

    // Set up the filter handlers
    SessionFactory sf = resolverFactoryInitializer.getSessionFactory();

    List<FilterHandler> hList = new ArrayList<FilterHandler>();
    for (int idx = 0; ; idx++) {
      String handlerClsName = config.getString("filterHandler.class_" + idx, null);
      if (handlerClsName == null)
        break;

      handlerClsName = handlerClsName.trim();
      if (handlerClsName.length() == 0)
        continue;

      hList.add(instantiateHandler(handlerClsName, config, base, sf, dbURI));
      logger.info("Loaded handler '" + handlerClsName + "'");
    }

    if (hList.size() == 0)
      logger.info("No handlers configured");

    handlers = hList.toArray(new FilterHandler[hList.size()]);
  }

  private static FilterHandler instantiateHandler(String clsName, Configuration config, String base,
                                                  SessionFactory sf, URI dbURI)
      throws InitializerException {
    try {
      Class<FilterHandler> clazz;
      try {
        clazz = (Class<FilterHandler>)
            Class.forName(clsName, true, Thread.currentThread().getContextClassLoader());
      } catch (Exception e) {
        clazz = (Class<FilterHandler>) Class.forName(clsName);
      }
      Constructor<FilterHandler> c =
          clazz.getConstructor(Configuration.class, String.class, SessionFactory.class, URI.class);
      return c.newInstance(config, base, sf, dbURI);
    } catch (Exception e) {
      throw new InitializerException("Error creating handler instance for '" + clsName + "'", e);
    }
  }

  public Graph[] getDefaultGraphs() {
    return null;
  }

  /**
   * @return true
   */
  public boolean supportsExport() {
    return true;
  }

  /**
   * Close the session factory.
   */
  public void close() throws ResolverFactoryException {
    for (FilterHandler h : handlers)
      h.close();

    logger.info("All handlers closed");
  }

  /**
   * Delete the session factory.
   */
  public void delete() throws ResolverFactoryException {
  }

  /**
   * Obtain a filter resolver.
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
                              Resolver systemResolver)
                              throws ResolverFactoryException {
    return new FilterResolver(dbURI, sysGraphType, systemResolver, resolverSession, this, handlers);
  }
}

