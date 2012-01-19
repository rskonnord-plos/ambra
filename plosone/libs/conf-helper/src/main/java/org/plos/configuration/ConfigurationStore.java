/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.configuration;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;

/**
 * A singleton that manages the load/unload/re-load of Configuration.
 *
 * @author Pradeep Krishnan
 * @author Eric Brown
 */
public class ConfigurationStore {
  private static final ConfigurationStore instance      = new ConfigurationStore();
  private Configuration                   configuration = null;

  /**
   * A property used to define the <code>ConfigurationFactory</code>  configuration
   * <code>URL</code>. It can be defined as a System property (eg.
   * <code>-Dorg.plos.configuration=file:///etc/topaz/config.xml</code>) or as a web
   * application context initialization parameter. If specified in both places, then the System
   * property will take precedence over the context initialization parameter.
   *
   * <p>
   * If the value of this property starts with a '/', then it is assumed to be relative to the web
   * application context. Otherwise it must be a valid <code>URL</code>.
   * </p>
   *
   * @see org.apache.commons.configuration.ConfigurationFactory for details on how to configure.
   */
  public static final String CONFIG_FACTORY_CONFIG_PROPERTY = "org.plos.configuration";

  /**
   * The default commons-config <code>ConfigurationFactory</code> config file.
   */
  public static final String DEFAULT_CONFIG_FACTORY_CONFIG = "/commons-config.xml";

  /**
   * Create the singleton instance.
   */
  private ConfigurationStore() {
  }

  /**
   * Gets the singleton instance.
   *
   * @return Returns the only instance.
   */
  public static ConfigurationStore getInstance() {
    return instance;
  }

  /**
   * Gets the current configuration root.
   *
   * @return Returns the currently loaded configuration root
   *
   * @throws RuntimeException if the configuration factory is not initialized
   */
  public Configuration getConfiguration() {
    if (configuration != null)
      return configuration;

    throw new RuntimeException("ERROR: Configuration not loaded or initialized.");
  }

  /**
   * Load/Reload the configuration from the factory config url.
   *
   * @param configURL URL to the config file for ConfigurationFactory
   *
   * @throws ConfigurationException when the config factory configuration has an error
   */
  public void loadConfiguration(URL configURL) throws ConfigurationException {
    ConfigurationFactory factory = new ConfigurationFactory();

    factory.setConfigurationURL(configURL);

    configuration = factory.getConfiguration();
  }

  /**
   * Use the default commons configuration specified by this library.
   *
   * @throws ConfigurationException when the configuration can't be found.
   */
  public void loadDefaultConfiguration() throws ConfigurationException {
    // Allow JVM level property to override everything else
    String name = System.getProperty(CONFIG_FACTORY_CONFIG_PROPERTY);
    if (name != null) {
      try {
        loadConfiguration(new URL(name));
      } catch (MalformedURLException e) {
        // If it wasn't a URL, then try to load it from the class-path
        URL url = ConfigurationStore.class.getResource(name);
        if (url == null)
          throw new ConfigurationException("Unable to configure commons config via URL '" + name +
                                           "' specified by '" + CONFIG_FACTORY_CONFIG_PROPERTY +
                                           "' system property");
        loadConfiguration(url);
      }
    } else {
      // Just use the resource in our library
      URL url = ConfigurationStore.class.getResource(DEFAULT_CONFIG_FACTORY_CONFIG);
      if (url == null)
        throw new ConfigurationException("Missing resource '" + DEFAULT_CONFIG_FACTORY_CONFIG +
                                         "' from conf-helper library.");
      loadConfiguration(url);
    }
  }

  /**
   * Unload the current configuration.
   */
  public void unloadConfiguration() {
    configuration = null;
  }
}
