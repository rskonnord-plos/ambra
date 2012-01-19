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

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate FedoraAPIA web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class APIAStubFactory extends AbstractReAuthStubFactory {
  private static APIAStubFactory instance = new APIAStubFactory();

  /**
   * Creates a FedoraAPIA service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns a Fedora API-A client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FedoraAPIA create(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    FedoraAPIA stub = instance.create(service);

    if (service.hasRenewableCredentials())
      stub = (FedoraAPIA) instance.newProxyStub(stub, service);

    return stub;
  }

  private FedoraAPIA createStub(ProtectedService service)
                         throws MalformedURLException, ServiceException {
    URL                      url      = new URL(service.getServiceUri());
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

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) apia;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return apia;
  }

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
    return create(new UnProtectedService(serviceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
