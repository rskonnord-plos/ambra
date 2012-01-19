/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.search;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;

/**
 * Factory class to generate Search web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class SearchClientFactory {
  /**
   * Creates a client that does not require any authentication.
   *
   * @param searchServiceUri the uri for article service
   *
   * @return Returns an Search service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FgsOperations create(String searchServiceUri)
                        throws MalformedURLException, ServiceException {
    URL                         url     = new URL(searchServiceUri);
    FgsOperationsServiceLocator locator = new FgsOperationsServiceLocator();
    locator.setMaintainSession(true);
    FgsOperations search = locator.getOperations(url);
    return search;
  }

}
