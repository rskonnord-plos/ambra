/* $HeadURL::                                                                                     $
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
package org.topazproject.ws.search;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.configuration.ConfigurationStore;

import dk.defxws.fedoragsearch.server.Operations; // API
import dk.defxws.fedoragsearch.server.SOAPImpl;
import dk.defxws.fedoragsearch.server.Config;


public class FedoraGenericSearchServicePortSoapBindingImpl implements Operations, ServiceLifecycle {
  private static final Log log =
      LogFactory.getLog(FedoraGenericSearchServicePortSoapBindingImpl.class);

  public static final String FEDORA_G_SEARCH_CONFIG_KEY = "ambra.services.search.fedoraGSearch";

  private Operations   impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // override fgs' config from our commons-config stuff
      Configuration tcfg = ConfigurationStore.getInstance().getConfiguration();
      tcfg = tcfg.subset(FEDORA_G_SEARCH_CONFIG_KEY);
      Properties scfg = Config.getCurrentConfig().getRepositoryProps(tcfg.getString("repository"));
      for (Iterator iter = tcfg.getKeys("fgsrepository."); iter.hasNext(); ) {
        String key = (String) iter.next();
        scfg.setProperty(key, tcfg.getString(key));
      }

      // create the impl
      impl = new SOAPImpl();
    } catch (Exception e) {
      log.error("Failed to initialize SearchImpl", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    impl = null;
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#updateIndex
   */
  public String updateIndex(String action, String value, String repositoryName, String indexName,
                            String indexDocXslt, String resultPageXslt) throws RemoteException {
    return impl.updateIndex(action, value, repositoryName, indexName, indexDocXslt, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#gfindObjects
   */
  public String gfindObjects(String query, long hitPageStart, int hitPageSize, int snippetsMax,
                             int fieldMaxLength, String indexName, String resultPageXslt)
      throws RemoteException {
    return impl.gfindObjects(query, hitPageStart, hitPageSize, snippetsMax, fieldMaxLength,
                             indexName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#browseIndex
   */
  public String browseIndex(String startTerm, int termPageSize, String fieldName, String indexName,
                            String resultPageXslt) throws RemoteException {
    return impl.browseIndex(startTerm, termPageSize, fieldName, indexName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#getRepositoryInfo
   */
  public String getRepositoryInfo(String repositoryName, String resultPageXslt)
      throws RemoteException {
    return impl.getRepositoryInfo(repositoryName, resultPageXslt);
  }

  /**
   * @see dk.defxws.fedoragsearch.server.Operations#getIndexInfo
   */
  public String getIndexInfo(String indexName, String resultPageXslt) throws RemoteException {
    return impl.getIndexInfo(indexName, resultPageXslt);
  }

}
