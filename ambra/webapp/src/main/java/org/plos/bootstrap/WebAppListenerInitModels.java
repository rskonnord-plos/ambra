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
package org.plos.bootstrap;

import java.net.URI;
import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.configuration.WebappItqlClientFactory;

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
      URI           service = new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      ItqlStore     store   = new ItqlStore(service, WebappItqlClientFactory.getInstance());

      conf                  = conf.subset("ambra.models");

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
