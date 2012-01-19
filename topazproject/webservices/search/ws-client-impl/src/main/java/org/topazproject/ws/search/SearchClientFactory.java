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
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsService;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;

/**
 * Factory class to generate Search web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class SearchClientFactory extends AbstractReAuthStubFactory {
  private static SearchClientFactory instance = new SearchClientFactory();

  /**
   * Creates an Search service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Search service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static FgsOperations create(ProtectedService service)
                        throws MalformedURLException, ServiceException {
    FgsOperations stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (FgsOperations) instance.newProxyStub(stub, service);

    return stub;
  }

  private FgsOperations createStub(ProtectedService service)
                      throws MalformedURLException, ServiceException {
    URL                         url     = new URL(service.getServiceUri());
    FgsOperationsServiceLocator locator = new FgsOperationsServiceLocator();

    locator.setMaintainSession(true);

    FgsOperations search = locator.getOperations(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) search;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return search;
  }

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
    return create(new UnProtectedService(searchServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
