/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.pap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate Preferences web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class PreferencesClientFactory extends AbstractReAuthStubFactory {
  private static PreferencesClientFactory instance = new PreferencesClientFactory();

  /**
   * Creates an Preferences service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Preferences service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Preferences create(ProtectedService service)
                            throws MalformedURLException, ServiceException {
    Preferences stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (Preferences) instance.newProxyStub(stub, service);

    return stub;
  }

  private Preferences createStub(ProtectedService service)
                          throws MalformedURLException, ServiceException {
    URL                       url     = new URL(service.getServiceUri());
    PreferencesServiceLocator locator = new PreferencesServiceLocator();

    locator.setMaintainSession(true);

    Preferences preferences = locator.getPreferencesServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) preferences;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return preferences;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param preferencesServiceUri the uri for preferences service
   *
   * @return Returns an Preferences service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Preferences create(String preferencesServiceUri)
                            throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(preferencesServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
