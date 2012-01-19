/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.users;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate UserAccounts web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class UserAccountsClientFactory extends AbstractReAuthStubFactory {
  private static UserAccountsClientFactory instance = new UserAccountsClientFactory();

  /**
   * Creates an UserAccounts service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an UserAccounts service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static UserAccounts create(ProtectedService service)
                             throws MalformedURLException, ServiceException {
    UserAccounts stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (UserAccounts) instance.newProxyStub(stub, service);

    return stub;
  }

  private UserAccounts createStub(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    URL                        url     = new URL(service.getServiceUri());
    UserAccountsServiceLocator locator = new UserAccountsServiceLocator();

    locator.setMaintainSession(true);

    UserAccounts userAccounts = locator.getUserAccountsServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) userAccounts;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return userAccounts;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param userAccountsServiceUri the uri for userAccounts service
   *
   * @return Returns an UserAccounts service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static UserAccounts create(String userAccountsServiceUri)
                             throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(userAccountsServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
