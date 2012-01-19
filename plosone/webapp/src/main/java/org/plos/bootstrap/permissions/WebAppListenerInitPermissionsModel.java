/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.bootstrap.permissions;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.plos.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.plos.permission.service.PermissionsImpl;

/**
 * A listener class for initializing permissions impl.
 *
 * @author Pradeep Krishnan
 */
public class WebAppListenerInitPermissionsModel implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListenerInitPermissionsModel.class);

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
    ItqlHelper itql = null;

    try {
      Configuration    conf = ConfigurationStore.getInstance().getConfiguration();

      ProtectedService service =
        ProtectedServiceFactory.createService(conf.subset("topaz.services.itql-admin"), null);

      itql = new ItqlHelper(service);

      PermissionsImpl.initializeModel(itql);
    } catch (Exception e) {
      log.warn("initializing permissions impl failed", e);
    } finally {
      try {
        if (itql != null)
          itql.close();
      } catch (Throwable t) {
      }
    }

    log.info("Successfully initialized permissions impl.");
  }
}
