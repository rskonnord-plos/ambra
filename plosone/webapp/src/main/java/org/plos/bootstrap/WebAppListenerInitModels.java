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

import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;

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
    ItqlHelper itql = null;

    try {
      Configuration    conf = ConfigurationStore.getInstance().getConfiguration();

      ProtectedService service =
        ProtectedServiceFactory.createService("topaz.services.itql-admin", null);

      itql   = new ItqlHelper(service);

      conf = conf.subset("topaz.models");

      Iterator it = conf.getKeys();

      while (it.hasNext()) {
        String key = (String) it.next();

        if ((key.indexOf("[") >= 0) || (key.indexOf(".") >= 0))
          continue;

        String model  = conf.getString(key);
        String type   = conf.getString(key + "[@type]", "tucana:Model");
        String create = "create <" + model + "> <" + type + ">;";

        if (log.isDebugEnabled())
          log.debug("topaz.models." + key + ": " + create);

        itql.doUpdate(create, null);
      }
    } catch (Exception e) {
      log.warn("bootstrap of models failed", e);
    } finally {
      try {
        if (itql != null)
          itql.close();
      } catch (Throwable t) {
      }
    }

    log.info("Successfully created all configured ITQL Models.");
  }
}
