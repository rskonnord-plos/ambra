/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.topazproject.ambra.permission.service;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

/**
 * Load the implied permissions to the Database.
 *
 * @author Pradeep Krishnan
 */
public class ImpliedPermissionsLoader {
  private static final Log log = LogFactory.getLog(ImpliedPermissionsLoader.class);

  private Configuration configuration;
  private SessionFactory sf;

  public void load() throws OtmException {
    TransactionHelper.doInTxE(sf, new TransactionHelper.ActionE<Integer, OtmException>() {
      public Integer run(Transaction tx) throws OtmException {
        return load(tx.getSession());
      }
    });
  }

  public int load(Session session) throws OtmException {
    Configuration conf        = configuration.subset("ambra.permissions.impliedPermissions");
    StringBuilder sb          = new StringBuilder();
    List<?>  permissions      = conf.getList("permission[@uri]");
    int           c           = permissions.size();
    for (int i = 0; i < c; i++) {
      List<?> implies = conf.getList("permission(" + i + ").implies[@uri]");
      log.info("config contains " + permissions.get(i) + " implies " + implies);

      for (int j = 0; j < implies.size(); j++) {
        sb.append("<").append(permissions.get(i)).append("> ");
        sb.append("<").append(PermissionsService.IMPLIES).append("> ");
        sb.append("<").append(implies.get(j)).append("> ");
      }
    }
    String triples   = sb.toString();
    String cmd = "insert " + triples + " into " + PermissionsService.PP_GRAPH + ";";

    if (c > 0)
      session.doNativeUpdate(cmd);
    else
      log.info("No implied permissions configured.");

    return c;
  }

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  /**
   * Setter method for configuration. Injected through Spring.
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
