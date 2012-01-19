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
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.tree.OverrideCombiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A singleton that manages the load/unload/re-load of Configuration.<p>
 *
 * Configuration consists of a layered set of configuration files where configuration
 * in a higher layer overrides those of the lower layers. Starting from the lowest layer,
 * configuration consists of:
 * <ul>
 *   <li>/global-defaults.xml - A resource in this library
 *   <li>/defaults.xml - A resource or resources on libraries and webapps using this lib
 *   <li>org.plos.configuration.overrides - If set, this defines a named resource or URL
 *        of a resource that is added to the configuration tree - usually supplementing
 *        and overriding settings in /global-defaults.xml and /defaults.xml.
 *   <li>file:/etc/topaz/plosone.xml (or org.plos.configuration) - A set of user overrides
 *        in /etc. The name of this file can be changed for webapps that use WebAppInitializer
 *        by changing web.xml or by setting the org.plos.configuraiton system property.
 *   <li>System properties
 * </ul>
 *
 * @author Pradeep Krishnan
 * @author Eric Brown
 */
public class ConfigurationStore {
  private static final Log                log       = LogFactory.getLog(ConfigurationStore.class);
  private static final ConfigurationStore instance  = new ConfigurationStore();
  private CombinedConfiguration          root = null;

  /**
   * A property used to define the location of the master set of configuration overrides.
   * This is usually a xml or properties file in /etc somewhere. Note that this must be
   * a URL. (For example: file:///etc/topaz/plosone.xml.)
   */
  public static final String CONFIG_URL = "org.plos.configuration";

  /**
   * A property used to define overrides. This is primarily to support something like
   * a development mode. If a valid URL, the resource is found from the URL. If not a
   * URL, it is treated as the name of a resource.
   */
  public static final String OVERRIDES_URL = "org.plos.configuration.overrides";

  /**
   * Default configuration overrides in /etc
   */
  public static final String DEFAULT_CONFIG_URL = "file:///etc/topaz/plosone.xml";

  /**
   * Name of resource(s) that contain defaults in a given library or web application.<p>
   *
   * Note that multiple copies of these may exist in the same classpath. ALL are read
   * and added to the configuration. It is assumed that developers use appropriate
   * namespaces to avoid collisions.<p>
   *
   * Also note that this does not begin with a / as ClassLoader.getResources() does
   * not want this to begin with a /.
   */
  public static final String DEFAULTS_RESOURCE = "org/plos/configuration/defaults.xml";

  /**
   * The name of the global defaults that exist in this library.<p>
   *
   * It is assumed there is only one of these in the classpath. If somebody defines
   * a second copy of this, the results are undefined. (TODO: Detect this.)
   */
  public static final String GLOBAL_DEFAULTS_RESOURCE = "/org/plos/configuration/global-defaults.xml";

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
    if (root != null)
      return root;

    throw new RuntimeException("ERROR: Configuration not loaded or initialized.");
  }

  /**
   * Overrides all existing configuration with the given conifguration object 
   * (useful for JUnit testing!)
   * @param newConfig
   */
  public void setConfiguration(CombinedConfiguration newConfig) {
    root = newConfig;
  }

  /**
   * Load/Reload the configuration from the factory config url.
   *
   * @param configURL URL to the config file for ConfigurationFactory
   * @throws ConfigurationException when the config factory configuration has an error
   */
  public void loadConfiguration(URL configURL) throws ConfigurationException {
    root = new CombinedConfiguration(new OverrideCombiner());

    // System properties override everything
    root.addConfiguration(new SystemConfiguration());

    // Load from org.plos.configuration -- /etc/... (optional)
    if (configURL != null) {
      try {
        root.addConfiguration(getConfigurationFromUrl(configURL));
        log.info("Added URL '" + configURL + "'");
      } catch (ConfigurationException ce) {
        if (!(ce.getCause() instanceof FileNotFoundException))
          throw ce;
        log.info("Unable to open '" + configURL + "'");
      }
    }

    // Add org.plos.configuration.overrides (if defined)
    String overrides = System.getProperty(OVERRIDES_URL);
    if (overrides != null) {
      try {
        root.addConfiguration(getConfigurationFromUrl(new URL(overrides)));
        log.info("Added override URL '" + overrides + "'");
      } catch (MalformedURLException mue) {
        // Must not be a URL, so it must be a resource
        addResources(root, overrides);
      }
    }

    // Add defaults.xml found in classpath
    addResources(root, DEFAULTS_RESOURCE);

    // Add global-defaults.xml (presumably found in this jar)
    addResources(root, GLOBAL_DEFAULTS_RESOURCE);
  }

  /**
   * Use the default commons configuration specified by this library.
   *
   * @throws ConfigurationException when the configuration can't be found.
   */
  public void loadDefaultConfiguration() throws ConfigurationException {
    // Allow JVM level property to override everything else
    String name = System.getProperty(CONFIG_URL);
    if (name == null)
      name = DEFAULT_CONFIG_URL;

    try {
      loadConfiguration(new URL(name));
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Invalid value of '" + name + "' for '" + CONFIG_URL +
                                       "'. Must be a valid URL.");
    }
  }

  /**
   * Unload the current configuration.
   */
  public void unloadConfiguration() {
    root = null;
  }

  /**
   * Given a URL, determine whether it represents properties or xml and load it as a
   * commons-config Configuration instance.
   */
  private static AbstractConfiguration getConfigurationFromUrl(URL url)
      throws ConfigurationException {
    if (url.getFile().endsWith("properties"))
      return new PropertiesConfiguration(url);
    else
      return new XMLConfiguration(url);
  }

  /**
   * Iterate over all the resources of the given name and add them to our root
   * configuration.
   */
  public static void addResources(CombinedConfiguration root, String resource)
      throws ConfigurationException {
    Class klass = ConfigurationStore.class;
    if (resource.startsWith("/")) {
      root.addConfiguration(getConfigurationFromUrl(klass.getResource(resource)));
      log.info("Added resource '" + resource + "'");
    } else {
      try {
        int cnt = 0;
        Enumeration<URL> rs = klass.getClassLoader().getResources(resource);
        while (rs.hasMoreElements()) {
          root.addConfiguration(getConfigurationFromUrl(rs.nextElement()));
          cnt++;
        }
        log.info("Added " + cnt + " instances of resource '" + resource + "' to configuration");
      } catch (IOException ioe) {
        // Don't understand how this could ever happen
        throw new Error("Unexpected error", ioe);
      }
    }
  }
}
