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

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A listener class for web-apps to load a configuration at startup.
 *
 * @author Pradeep Krishnan
 * @author Eric Brown
 */
public class WebAppListener implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListener.class);

  /**
   * Destroy the configuration singleton since this web application is getting un-deployed.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    ConfigurationStore.getInstance().unloadConfiguration();
  }

  /**
   * Initialize the configuration singleton since this web application is getting deployed.<p>
   *
   * By default, WebAppListener looks for /commons-config.xml in the classpath. (This can
   * be overridden by setting the org.topazproject.configuration system property to a URL or
   * a name resolvable as a resource.) However, if we cannot find the configuration this way,
   * we use our own /commons-config-default.xml that is included with this library.
   *
   * @param event destroyed event
   *
   * @throws Error on non-recoverable config load error
   */
  public void contextInitialized(ServletContextEvent event) {
    ServletContext context = event.getServletContext();
    FactoryConfig  config = getFactoryConfig(context);

    try {
      URL url;

      // Locate the config url.
      if (config.name.startsWith("/WEB-INF")) {
        url = context.getResource(config.name);

        if (url == null)
          throw new MalformedURLException("'" + config.name + "' not found in the web-app context");
      } else {
        try {
          // First see if it is a valid URL
          url = new URL(config.name);
        } catch (MalformedURLException e) {
          // Otherwise, load as a resource
          url = WebAppListener.class.getResource(config.name);
          if (url == null)
            throw e;
        }
      }

      // Now load the config
      log.info("Loading '" + url + "' (" + config.name + ") configured via " + config.source);
      ConfigurationStore.getInstance().loadConfiguration(url);
    } catch (MalformedURLException e) {
      // Bad config file url. Try to abort.
      log.fatal(config.name + " defined by " + config.source + " is not a valid URL or resource", e);
      throw new Error("Failed to load configuration", e);
    } catch (ConfigurationException e) {
      // Bad config file. Try to abort.
      log.fatal("Failed to initialize configuration factory.", e);
      throw new Error("Failed to load configuration", e);
    }
  }

  private FactoryConfig getFactoryConfig(ServletContext context) {
    // Allow JVM level property to override everything else
    String name = System.getProperty(ConfigurationStore.CONFIG_FACTORY_CONFIG_PROPERTY);

    if (name != null)
      return new FactoryConfig(name, "JVM System property " +
                               ConfigurationStore.CONFIG_FACTORY_CONFIG_PROPERTY);

    // Now look for a config specified in web.xml
    name = context.getInitParameter(ConfigurationStore.CONFIG_FACTORY_CONFIG_PROPERTY);

    if (name != null)
      return new FactoryConfig(name,
                               "Web-app context initialization parameter "
                               + ConfigurationStore.CONFIG_FACTORY_CONFIG_PROPERTY);

    // Return a default
    return new FactoryConfig(ConfigurationStore.DEFAULT_CONFIG_FACTORY_CONFIG, "default");
  }

  private class FactoryConfig {
    String name;
    String source;

    FactoryConfig(String name, String source) {
      this.name     = name;
      this.source   = source;
    }
  }
}
