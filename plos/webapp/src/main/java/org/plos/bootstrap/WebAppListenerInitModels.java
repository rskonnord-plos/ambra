/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.bootstrap;

import java.net.URI;
import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.stores.ItqlStore;

/**
 * A listener class for web-apps to initialize things at startup.
 *
 * @author Pradeep Krishnan
 */
public class WebAppListenerInitModels implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListenerInitModels.class);

  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
  }

  /**
   * Initialize things.
   *
   * @param event destroyed event
   */
  public void contextInitialized(ServletContextEvent event) {
    // xxx" may be this can itself be driven by config
    initModels();
  }

  private void initModels() {
    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("topaz.services.itql-admin.uri"));

      ItqlStore     store   = new ItqlStore(service);

      conf                  = conf.subset("topaz.models");

      Iterator it           = conf.getKeys();

      while (it.hasNext()) {
        String key = (String) it.next();

        if ((key.indexOf("[") >= 0) || (key.indexOf(".") >= 0))
          continue;

        String model  = conf.getString(key);
        String type   = conf.getString(key + "[@type]", "mulgara:Model");

        store.createModel(new ModelConfig("", new URI(model), new URI(type)));
      }
    } catch (Exception e) {
      log.warn("bootstrap of models failed", e);
    }

    log.info("Successfully created all configured ITQL Models.");
  }
}
