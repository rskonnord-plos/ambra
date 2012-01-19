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

import java.net.URI;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import org.plos.permission.service.PermissionsImpl;

import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

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
    Session sess = null;
    try {
      Configuration conf    = ConfigurationStore.getInstance().getConfiguration();
      URI           service = new URI(conf.getString("topaz.services.itql-admin.uri"));

      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service));

      sess = factory.openSession();
      TransactionHelper.doInTx(sess, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          PermissionsImpl.initializeModel(tx.getSession());
          return null;
        }
      });
    } catch (Exception e) {
      log.warn("initializing permissions impl failed", e);
    } finally {
      try {
        if (sess != null)
          sess.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }

    log.info("Successfully initialized permissions impl.");
  }
}
