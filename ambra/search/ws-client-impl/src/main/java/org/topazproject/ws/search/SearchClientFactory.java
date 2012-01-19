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
