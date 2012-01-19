#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
/* 
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package ${package};

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.UnProtectedService;
import org.topazproject.authentication.reauth.AbstractReAuthStubFactory;

/**
 * Factory class to generate ${Svc} web-service client stubs.
 *
 * @author foo
 */
public class ${Svc}ClientFactory extends AbstractReAuthStubFactory {
  private static ${Svc}ClientFactory instance = new ${Svc}ClientFactory();

  /**
   * Creates a ${Svc} service client stub..
   *
   * @param service the service configuration (url and credentials)
   *
   * @return Returns an ${Svc} service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static ${Svc} create(ProtectedService service)
                             throws MalformedURLException, ServiceException {
    ${Svc} stub = instance.createStub(service);

    if (service.hasRenewableCredentials())
      stub = (${Svc}) instance.newProxyStub(stub, service);

    return stub;
  }

  private ${Svc} createStub(ProtectedService service)
                           throws MalformedURLException, ServiceException {
    URL                        url     = new URL(service.getServiceUri());
    ${Svc}ServiceLocator locator = new ${Svc}ServiceLocator();

    locator.setMaintainSession(true);

    ${Svc} client = locator.get${Svc}ServicePort(url);

    if (service.requiresUserNamePassword()) {
      Stub stub = (Stub) client;
      stub._setProperty(Stub.USERNAME_PROPERTY, service.getUserName());
      stub._setProperty(Stub.PASSWORD_PROPERTY, service.getPassword());
    }

    return client;
  }

  /**
   * Creates a client that does not require any authentication.
   *
   * @param serviceUri the uri for ${Svc} service
   *
   * @return Returns a ${Svc} service client stub.
   *
   * @throws MalformedURLException If the service url is misconfigured
   * @throws ServiceException If there is an error in creating the stub
   */
  public static ${Svc} create(String serviceUri)
                             throws MalformedURLException, ServiceException {
    return create(new UnProtectedService(serviceUri));
  }

  /*
   * @see org.topazproject.authentication.StubFactory#newStub
   */
  public Object newStub(ProtectedService service) throws Exception {
    return createStub(service);
  }
}
