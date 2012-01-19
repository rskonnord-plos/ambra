/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

/**
 * Factory class to generate FedoraAPIM web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class APIMStubFactory {

  /**
   * Creates a client that require a username/password authentication.
   *
   * @param serviceUri the uri for Fedora API-M service
   *
   * @return Returns a Fedora API-M client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FedoraAPIM create(String serviceUri, String username, String password) 
      throws MalformedURLException, ServiceException {
    URL                      url      = new URL(serviceUri);
    String                   protocol = url.getProtocol();
    FedoraAPIMServiceLocator locator  = new FedoraAPIMServiceLocator();

    locator.setMaintainSession(true);

    FedoraAPIM apim;

    if ("http".equals(protocol))
      apim = locator.getFedoraAPIMPortSOAPHTTP(url);
    else if ("https".equals(protocol))
      apim = locator.getFedoraAPIMPortSOAPHTTPS(url);
    else
      throw new ServiceException(protocol
                                 + " not supported for service url. (must be 'http' or 'https')");

    Stub stub = (Stub) apim;
    stub._setProperty(Stub.USERNAME_PROPERTY, username);
    stub._setProperty(Stub.PASSWORD_PROPERTY, password);

    return apim;
  }

}
