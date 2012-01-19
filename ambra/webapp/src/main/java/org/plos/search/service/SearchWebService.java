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
package org.plos.search.service;

import org.plos.configuration.ConfigurationStore;

import org.topazproject.ws.search.SearchClientFactory;
import org.topazproject.fedoragsearch.service.FgsOperations;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper over search service
 *
 * @author Viru
 * @author Eric Brown
 */
public class SearchWebService {
   private final FgsOperations fgsOperations;
  /**
   * Initialize the service
   * @throws java.io.IOException IOException
   * @throws javax.xml.rpc.ServiceException ServiceException
   * @throws java.net.URISyntaxException URISyntaxException
   */
  public SearchWebService() throws IOException, URISyntaxException, ServiceException {
    String uri = ConfigurationStore.getInstance().getConfiguration().getString("ambra.spring.service.search");
    fgsOperations = SearchClientFactory.create(uri);
  }

  /**
   * @see FgsOperations#gfindObjects(String, long, int, int, int, String, String)
   */
  public String find(final String query, final int hitStartPage, final int pageSize,
                     final int snippetsMax, final int fieldMaxLength, final String indexName,
                     final String resultPageXslt) throws RemoteException {
    return fgsOperations.gfindObjects(query, hitStartPage, pageSize, snippetsMax,
                                      fieldMaxLength, indexName, resultPageXslt);
  }
}
