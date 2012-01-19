/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
    String uri = ConfigurationStore.getInstance().getConfiguration().getString("pub.spring.service.search");
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
