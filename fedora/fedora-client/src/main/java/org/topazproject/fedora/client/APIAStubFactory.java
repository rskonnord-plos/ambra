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
package org.topazproject.fedora.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;


/**
 * Factory class to generate FedoraAPIA web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class APIAStubFactory {

  /**
   * Creates a client that does not require any authentication.
   *
   * @param serviceUri the uri for Fedora API-A service
   *
   * @return Returns a Fedora API-A client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FedoraAPIA create(String serviceUri) throws MalformedURLException, ServiceException {
    URL                      url      = new URL(serviceUri);
    String                   protocol = url.getProtocol();
    FedoraAPIAServiceLocator locator  = new FedoraAPIAServiceLocator();

    locator.setMaintainSession(true);

    FedoraAPIA apia;

    if ("http".equals(protocol))
      apia = locator.getFedoraAPIAPortSOAPHTTP(url);
    else if ("https".equals(protocol))
      apia = locator.getFedoraAPIAPortSOAPHTTPS(url);
    else
      throw new ServiceException(protocol
                                 + " not supported for service url. (must be 'http' or 'https')");

    return apia;
  }

}
