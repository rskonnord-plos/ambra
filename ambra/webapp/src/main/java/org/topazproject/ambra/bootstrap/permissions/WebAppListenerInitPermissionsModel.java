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
package org.topazproject.ambra.bootstrap.permissions;

import java.net.URI;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.configuration.WebappItqlClientFactory;
import org.topazproject.ambra.permission.service.PermissionsService;
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
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, WebappItqlClientFactory.getInstance()));

      sess = factory.openSession();
      TransactionHelper.doInTx(sess, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          PermissionsService.initializeModel(tx.getSession());
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
