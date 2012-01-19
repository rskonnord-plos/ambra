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
package org.topazproject.ws.access;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class AccessServiceTest extends TestCase {
  private Access service;

  /**
   * Creates a new AccessServiceTest object.
   *
   * @param testName DOCUMENT ME!
   */
  public AccessServiceTest(String testName) {
    super(testName);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws MalformedURLException DOCUMENT ME!
   * @throws ServiceException DOCUMENT ME!
   * @throws RemoteException DOCUMENT ME!
   */
  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri = "http://localhost:9997/ws-access/services/AccessServicePort";
    service = AccessClientFactory.create(uri);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws RemoteException DOCUMENT ME!
   * @throws IOException DOCUMENT ME!
   */
  public void testAll() throws RemoteException, IOException {
    basicAccessTest();
  }

  private void basicAccessTest() throws RemoteException, IOException {
    try {
      service.checkAccess(null, "joe", "foo:bar", "fedora:read");
    } catch (SecurityException e) {
    }

    try {
      service.checkAccess("standard-pdp", "joe", "foo:bar", "fedora:read");
    } catch (SecurityException e) {
    }
  }
}
