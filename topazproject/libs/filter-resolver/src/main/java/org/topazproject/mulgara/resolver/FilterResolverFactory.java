/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
import org.apache.log4j.Logger;

import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

import org.topazproject.configuration.ConfigurationStore;

/** 
 * The factory for {@link FilterResolver}s.
 *
 * <p>The configuration for this and for the {@link FilterHandler}s is retrieved from the
 * the {@link ConfigurationStore}; if that has not been initialized then it is first initialized
 * a config from one of the following locations:
 * <ol>
 *   <li>If {@link #CONFIG_FACTORY_CONFIG_PROPERTY CONFIG_FACTORY_CONFIG_PROPERTY} system property
 *       has been set, use it's value; other use {@link @DEFAULT_FACTORY_CONFIG
 *       DEFAULT_FACTORY_CONFIG}.</li>
 *   <li>If the resulting location string starts with a '/' assume it's a resource and try to
 *       load it as such; otherwise assume it's a URL.</li>
 * </ol>
 * 
 * @author Ronald Tschalär
 */
public class FilterResolverFactory implements ResolverFactory {
  /**
   * the system property that can be used to defined a non-default configuration-factory config:
   * {@value}
   */
  public static final String CONFIG_FACTORY_CONFIG_PROPERTY = "org.topazproject.configuration";

  /** the location of the default factory config: {@value} */
  public static final String DEFAULT_FACTORY_CONFIG         = "/conf/topaz-factory-config.xml";

  private static final Logger logger = Logger.getLogger(FilterResolverFactory.class);

  private final URI             dbURI;
  private final long            sysModelType;
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

    // Claim the filter model type
    resolverFactoryInitializer.addModelType(FilterResolver.MODEL_TYPE, this);

    // remember the database uri
    dbURI = resolverFactoryInitializer.getDatabaseURI();

    // remember the system-model type
    sysModelType = resolverFactoryInitializer.getSystemModelType();

    // load the configuration
    ConfigurationStore store = ConfigurationStore.getInstance();
    Configuration config = store.getConfiguration();
    if (config == null) {
      String fConf = System.getProperty(CONFIG_FACTORY_CONFIG_PROPERTY, DEFAULT_FACTORY_CONFIG);
      URL fConfUrl = null;
      try {
        fConfUrl = fConf.startsWith("/") ? getClass().getResource(fConf) : new URL(fConf);
        if (fConfUrl == null)
          throw new InitializerException("'" + fConf + "' not found in classpath");

        logger.info("Using filter-resolver config '" + fConfUrl + "'");

        store.loadConfiguration(fConfUrl);
        config = store.getConfiguration();
      } catch (MalformedURLException mue) {
        throw new InitializerException("Error parsing '" + fConf+ "'", mue);
      } catch (ConfigurationException ce) {
        throw new InitializerException("Error reading '" + fConfUrl + "'", ce);
      }
    }

    String base = "topaz.fr";
    config = config.subset(base);

    // Set up the filter handlers
    List hList = new ArrayList();
    for (int idx = 0; ; idx++) {
      String handlerClsName = config.getString("filterHandler.class_" + idx, null);
      if (handlerClsName == null)
        break;

      hList.add(instantiateHandler(handlerClsName, config, base, dbURI));
      logger.info("Loaded handler '" + handlerClsName + "'");
    }

    if (hList.size() == 0)
      logger.info("No handlers configured");

    handlers = (FilterHandler[]) hList.toArray(new FilterHandler[hList.size()]);
  }

  private static FilterHandler instantiateHandler(String clsName, Configuration config, String base,
                                                  URI dbURI)
      throws InitializerException {
    try {
      Class clazz = Class.forName(clsName, true, Thread.currentThread().getContextClassLoader());
      Constructor c =
          clazz.getConstructor(new Class[] { Configuration.class, String.class, URI.class });
      return (FilterHandler) c.newInstance(new Object[] { config, base, dbURI });
    } catch (Exception e) {
      throw new InitializerException("Error creating handler instance for '" + clsName + "'", e);
    }
  }


  /**
   * Close the session factory.
   */
  public void close() throws ResolverFactoryException {
    for (int idx = 0; idx < handlers.length; idx++)
      handlers[idx].close();
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
    return new FilterResolver(dbURI, sysModelType, systemResolver, resolverSession, handlers);
  }
}

