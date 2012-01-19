/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Simple tests for the user-roles service.
 *
 * @author Ronald Tschalär
 */
public class UserRolesServiceTest extends TestCase {
  private UserRoles    service;
  private UserAccounts userService;
  private String       userId;

  public UserRolesServiceTest(String testName) {
    super(testName);
  }

  protected void setUp()
      throws MalformedURLException, ServiceException, DuplicateAuthIdException, RemoteException {
    String uri = "http://localhost:9997/ws-users/services/UserRolesServicePort";
    service = UserRolesClientFactory.create(uri);

    uri = "http://localhost:9997/ws-users/services/UserAccountsServicePort";
    userService = UserAccountsClientFactory.create(uri);
    // create a user
    userId = userService.createUser("musterAuth");
  }

  protected void tearDown() throws RemoteException {
    try {
      if (userId != null)
        service.setRoles(userId, null);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }

    try {
      userService.deleteUser(userId);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }
  }

  public void testNonExistentUser() throws RemoteException, IOException {
    boolean gotE = false;
    try {
      service.getRoles("id:muster");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.setRoles("id:muster", null);
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);
  }

  public void testInvalidUser() throws RemoteException, NoSuchUserIdException, IOException {
    boolean gotE = false;
    try {
      service.getRoles("muster");
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);

    gotE = false;
    try {
      service.setRoles("muster", null);
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);
  }

  public void testNullUser() throws RemoteException, NoSuchUserIdException, IOException {
    boolean gotE = false;
    try {
      service.getRoles(null);
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("NullPointerException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);

    gotE = false;
    try {
      service.setRoles(null, null);
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("NullPointerException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);
  }

  public void testBasicUserRoles()
      throws RemoteException, NoSuchUserIdException, DuplicateAuthIdException, IOException {
    String[] got = service.getRoles(userId);
    assertNull("got non-null roles", got);

    String[] roles = new String[] { "user", "admin" };
    service.setRoles(userId, roles);
    got = service.getRoles(userId);
    checkRoles(userId, got, roles);

    roles = new String[0];
    service.setRoles(userId, roles);
    got = service.getRoles(userId);
    assertNull("got non-null roles", got);

    roles = null;
    service.setRoles(userId, roles);
    got = service.getRoles(userId);
    assertNull("got non-null roles", got);

    roles = new String[] { "user" };
    service.setRoles(userId, roles);
    got = service.getRoles(userId);
    checkRoles(userId, got, roles);

    String user2Id = userService.createUser("musterAuth1");
    got = service.getRoles(user2Id);
    assertNull("got non-null roles", got);

    String[] roles2 = new String[] { "admin" };
    service.setRoles(user2Id, roles2);
    got = service.getRoles(userId);
    checkRoles(userId, got, roles);
    got = service.getRoles(user2Id);
    checkRoles(user2Id, got, roles2);

    userService.deleteUser(user2Id);
  }

  private void checkRoles(String userId, String[] got, String[] exp) {
    assertNotNull("got null roles for '" + userId + "'", got);
    assertEquals("Number of roles mismatch;", exp.length, got.length);

    Arrays.sort(got);
    Arrays.sort(exp);

    for (int idx = 0; idx < got.length; idx++)
      assertEquals("Role mismatch, ", exp[idx], got[idx]);
  }
}
