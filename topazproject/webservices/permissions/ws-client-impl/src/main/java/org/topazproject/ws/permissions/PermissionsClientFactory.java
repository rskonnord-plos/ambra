/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate Permissions web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class PermissionsClientFactory extends AbstractReAuthStubFactory {
  private static PermissionsClientFactory instance = new PermissionsClientFactory();

  /**
   * Creates an Permissions service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Permissions service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Permissions create(ProtectedService service)
                            throws MalformedURLException, ServiceException {
    Permissions stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (Permissions) instance.newProxyStub(stub, service);

    return stub;
  }

  private Permissions createStub(ProtectedService service)
                          throws MalformedURLException, ServiceException {
    URL                       url     = new URL(service.getServiceUri());
    PermissionsServiceLocator locator = new PermissionsServiceLocator();

    locator.setMaintainSession(true);

    Permissions permissions = locator.getPermissionsServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) permissions;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return permissions;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param permissionsServiceUri the uri for permissions service
   *
   * @return Returns an Permissions service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Permissions create(String permissionsServiceUri)
                            throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(permissionsServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
