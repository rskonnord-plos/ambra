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

package org.plos.article.service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;

/**
 * Utilities for dealing with the search engine. This is somewhat temporary, to be replaced
 * when topaz supports search-engines "natively".
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public class SearchUtil {
  private static final Log           log      = LogFactory.getLog(SearchUtil.class);
  private static final Configuration CONF     = ConfigurationStore.getInstance().getConfiguration();
  private static final List          FGS_URLS =
                                  CONF.getList("ambra.services.search.fedoraGSearch.urls.url");
  private static final String        FGS_REPO =
                                  CONF.getString("ambra.services.search.fedoraGSearch.repository");

  private static final FgsOperationsServiceLocator fgsLoc = new FgsOperationsServiceLocator();

  private SearchUtil() {
  }

  /**
   * Temporary hack: info:doi/ -> doi: . FIXME: In theory we should re-encode using fedora's
   * reserved/allowed character list, but the difference between that and what URLEncoder encodes
   * is just '~' and '*' and we currently don't have either of those in our doi; and hopefully this
   * hack will go away before we do.
   */
  private static String objIdToFedoraPid(String uri) {
    try {
      return "doi:" + URLEncoder.encode(URLDecoder.decode(uri.substring(9), "UTF-8"), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected exception", uee);
    }
  }

  /**
   * Add the given item to the search index.
   *
   * @param uri  the uri of the object to index
   * @throws RemoteException if there was a problem talking to search
   * @throws ServiceException if the search service could not be located
   */
  public static void index(String uri) throws RemoteException, ServiceException {
    FgsOperations[] fgs = getFgsOperations();
    String pid = objIdToFedoraPid(uri);

    int i = 0;
    String result = null;
    try {
      for (i = 0; i < fgs.length; i++)
        result = fgs[i].updateIndex("fromPid", pid, FGS_REPO, null, null, null);

      if (log.isDebugEnabled())
        log.debug("Indexed '" + pid + "':\n" + result);
    } catch (RemoteException re) {
      log.warn("Error ingesting to fgs server " + FGS_URLS.get(i) +
               " unwinding ingests to servers 0-" + i + " (topaz.xml:<fedoragsearch><urls><url>)");
      for (int j = 0; j < i; j++) {
        try {
          result = fgs[j].updateIndex("deletePid", pid, FGS_REPO, null, null, null);
          log.info("Deleted pid '" + pid + "' from fgs server " + FGS_URLS.get(j) + ":\n" + result);
        } catch (RemoteException re2) {
          log.warn("Unable to delete pid '" + pid + "' from fgs server " + FGS_URLS.get(j) +
                   " after indexing failed on server " + FGS_URLS.get(i) +
                   " (topaz.xml:<fedoragsearch><urls><url>)", re2);
        }
      }
      throw re;
    }
  }

  /**
   * Remove an item from search. Note: we throw the first exception encountered - manual
   * cleanup will be needed anyway, so keeping it simple for now.
   *
   * @param uri  the uri of the object to remove
   * @throws RemoteException if there was a problem talking to search
   * @throws ServiceException if the search service could not be located
   */
  public static void delete(String uri) throws RemoteException, ServiceException {
    String pid = objIdToFedoraPid(uri);
    for (FgsOperations fgs : getFgsOperations()) {
      String result = fgs.updateIndex("deletePid", pid, FGS_REPO, null, null, null);
      if (log.isDebugEnabled())
        log.debug("Removed '" + pid + "' from full-text index:\n" + result);
    }
  }

  /**
   * Get the list of search-service clients.
   *
   * @throws ServiceException if the search service could not be located
   */
  public static FgsOperations[] getFgsOperations() throws ServiceException {
    FgsOperations ops[] = new FgsOperations[FGS_URLS.size()];
    for (int i = 0; i < ops.length; i++) {
      String url = FGS_URLS.get(i).toString();
      try {
        ops[i] = fgsLoc.getOperations(new URL(url));
      } catch (MalformedURLException mue) {
        throw new ServiceException("Invalid fedoragsearch URL '" + url + "'", mue);
      }
      if (ops[i] == null)
        throw new ServiceException("Unable to create fedoragsearch service at '" + url + "'");
    }
    return ops;
  }
}
