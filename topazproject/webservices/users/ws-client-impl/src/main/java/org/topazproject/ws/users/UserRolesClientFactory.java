/* $HeadURL::                                                                          $
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
 * Factory class to generate UserRoles web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class UserRolesClientFactory extends AbstractReAuthStubFactory {
  private static UserRolesClientFactory instance = new UserRolesClientFactory();

  /**
   * Creates an UserRoles service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an UserRoles service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static UserRoles create(ProtectedService service)
                          throws MalformedURLException, ServiceException {
    UserRoles stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (UserRoles) instance.newProxyStub(stub, service);

    return stub;
  }

  private UserRoles createStub(ProtectedService service)
                        throws MalformedURLException, ServiceException {
    URL                     url     = new URL(service.getServiceUri());
    UserRolesServiceLocator locator = new UserRolesServiceLocator();

    locator.setMaintainSession(true);

    UserRoles userRoles = locator.getUserRolesServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) userRoles;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return userRoles;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param userRolesServiceUri the uri for userRoles service
   *
   * @return Returns an UserRoles service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static UserRoles create(String userRolesServiceUri)
                          throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(userRolesServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
