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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class ${Svc}ServiceTest extends TestCase {
  private ${Svc} service;

  public ${Svc}ServiceTest(String testName) throws MalformedURLException, ServiceException {
    super(testName);

    String uri = "http://localhost:9997/ws-${service}/services/${Svc}ServicePort";
    service = ${Svc}ClientFactory.create(uri);
  }

  protected void setUp() {
  }

  public void testAll() throws RemoteException, IOException {
    basic${Svc}Test();
  }

  private void basic${Svc}Test() throws RemoteException, IOException {
  }
}
