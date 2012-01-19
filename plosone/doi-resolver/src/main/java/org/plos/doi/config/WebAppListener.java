/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.doi.config;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A listener class for web-apps to load a configuration at startup.
 *
 * @author Pradeep Krishnan
 */
public class WebAppListener implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListener.class);

  /**
   * A property used to define the <code>ConfigurationFactory</code>  configuration
   * <code>URL</code>. It can be defined as a System property (eg.
   * <code>-Dorg.topazproject.configuration=file:///etc/topaz/config.xml</code>) or as a web
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
   * The default <code>ConfigurationFactory</code> config file.
   */
  public static final String DEFAULT_CONFIG_FACTORY_CONFIG = "/WEB-INF/classes/config.xml";

  /**
   * Destroy the configuration singleton since this web application is getting un-deployed.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    ConfigurationStore.getInstance().unloadConfiguration();
  }

  /**
   * Initialize the configuration singleton since this web application is getting deployed.
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
      if (!config.name.startsWith("/"))
        url = new URL(config.name);
      else {
        url = context.getResource(config.name);

        if (url == null)
          throw new MalformedURLException("'" + config.name + "' not found in the web-app context");
      }
      if (log.isTraceEnabled()){
        log.trace("loading config file with URL: " + url);
      }
      // Now load the config
      ConfigurationStore.getInstance().loadConfiguration(url);
    } catch (MalformedURLException e) {
      // Bad config file url. Try to abort. 
      log.fatal(config.name + " defined by " + config.source + " is not a valid URL", e);
      throw new Error("Failed to load configuration", e);
    } catch (ConfigurationException e) {
      // Bad config file. Try to abort.
      log.fatal("Failed to initialize configuration factory.", e);
      throw new Error("Failed to load configuration", e);
    }
  }

  private FactoryConfig getFactoryConfig(ServletContext context) {
    // Allow JVM level property to override everything else
    String name = System.getProperty(CONFIG_FACTORY_CONFIG_PROPERTY);

    if (name != null)
      return new FactoryConfig(name, "JVM System property " + CONFIG_FACTORY_CONFIG_PROPERTY);

    // Now look for a config specified in web.xml
    name = context.getInitParameter(CONFIG_FACTORY_CONFIG_PROPERTY);

    if (name != null)
      return new FactoryConfig(name,
                               "Web-app context initialization parameter "
                               + CONFIG_FACTORY_CONFIG_PROPERTY);

    // Return a default 
    return new FactoryConfig(DEFAULT_CONFIG_FACTORY_CONFIG, "default");
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
