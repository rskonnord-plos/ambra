/* $HeadURL::                                                                       $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.alerts;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate Alerts web-service client stubs.
 *
 * @author Pradeep Krishnan
 */
public class AlertsClientFactory extends AbstractReAuthStubFactory {
  private static AlertsClientFactory instance = new AlertsClientFactory();

  /**
   * Creates an Alerts service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an Alerts service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Alerts create(ProtectedService service)
                       throws MalformedURLException, ServiceException {
    Alerts stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (Alerts) instance.newProxyStub(stub, service);

    return stub;
  }

  private Alerts createStub(ProtectedService service)
                     throws MalformedURLException, ServiceException {
    URL                  url     = new URL(service.getServiceUri());
    AlertsServiceLocator locator = new AlertsServiceLocator();

    locator.setMaintainSession(true);

    Alerts alerts = locator.getAlertsServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) alerts;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return alerts;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param alertsServiceUri the uri for alerts service
   *
   * @return Returns an Alerts service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static Alerts create(String alertsServiceUri)
                       throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(alertsServiceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
