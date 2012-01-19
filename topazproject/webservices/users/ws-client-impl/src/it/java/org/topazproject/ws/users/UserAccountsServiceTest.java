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
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class UserAccountsServiceTest extends TestCase {
  private UserAccounts service;
  private String       userId;

  public UserAccountsServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    String uri = "http://localhost:9997/ws-users/services/UserAccountsServicePort";
    service = UserAccountsClientFactory.create(uri);
  }

  protected void tearDown() throws RemoteException {
    try {
      if (userId != null)
        service.deleteUser(userId);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }
  }

  public void testNonExistentUser() throws DuplicateAuthIdException, RemoteException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser("id:muster");
    } catch (NoSuchUserIdException nsie) {
      assertEquals("no-such-user mismatch", "id:muster", nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds("id:muster", null);
    } catch (NoSuchUserIdException nsie) {
      assertEquals("no-such-user mismatch", "id:muster", nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds("id:muster");
    } catch (NoSuchUserIdException nsie) {
      assertEquals("no-such-user mismatch", "id:muster", nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);
  }

  public void testDupAuthId()
      throws NoSuchUserIdException, DuplicateAuthIdException, RemoteException, IOException {
    String aid  = "id:muster42";
    String aid2 = "id:muster43";

    String uid  = service.createUser(aid);
    String uid2 = service.createUser(aid2);

    boolean gotE = false;
    try {
      service.createUser(aid);
    } catch (DuplicateAuthIdException daie) {
      assertEquals("dup-auth-id mismatch", aid, daie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected DuplicateAuthIdException", gotE);

    service.setAuthenticationIds(uid, new String[] { aid });

    gotE = false;
    try {
      service.setAuthenticationIds(uid2, new String[] { aid });
    } catch (DuplicateAuthIdException daie) {
      assertEquals("dup-auth-id mismatch", aid, daie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected DuplicateAuthIdException", gotE);

    String[] authIds = service.getAuthenticationIds(uid2);
    checkAuthIds(uid2, authIds, new String[] { aid2 });

    gotE = false;
    try {
      service.setAuthenticationIds(uid2, new String[] { "id:blah", aid, "id:foo" });
    } catch (DuplicateAuthIdException daie) {
      assertEquals("dup-auth-id mismatch", aid, daie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected DuplicateAuthIdException", gotE);

    authIds = service.getAuthenticationIds(uid2);
    checkAuthIds(uid2, authIds, new String[] { aid2 });

    service.deleteUser(uid);
    service.deleteUser(uid2);
  }

  public void testInvalidUser()
      throws DuplicateAuthIdException, NoSuchUserIdException, RemoteException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser("muster");
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds("muster", null);
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds("muster");
    } catch (RemoteException re) {
      if (re.getMessage().indexOf("IllegalArgumentException") < 0)
        throw re;
      gotE = true;
    }
    assertTrue("Failed to get expected IllegalArgumentException", gotE);
  }

  public void testNullUser()
      throws RemoteException, NoSuchUserIdException, DuplicateAuthIdException, IOException {
    boolean gotE = false;
    try {
      service.deleteUser(null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);

    gotE = false;
    try {
      service.setAuthenticationIds(null, null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);

    gotE = false;
    try {
      service.getAuthenticationIds(null);
    } catch (RemoteException re) {
      gotE = true;
    }
    assertTrue("Failed to get expected RemoteException", gotE);
  }

  public void testBasicUserAccounts()
      throws RemoteException, NoSuchUserIdException, DuplicateAuthIdException, IOException {
    userId = service.createUser("musterAuth");
    service.deleteUser(userId);

    userId = service.createUser("musterAuth");
    String[] authIds = service.getAuthenticationIds(userId);
    checkAuthIds(userId, authIds, new String[] { "musterAuth" });

    authIds = new String[] { "id1", "foo" };
    service.setAuthenticationIds(userId, authIds);
    String[] got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    authIds = new String[0];
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    authIds = null;
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, new String[0]);

    authIds = new String[] { "id1", "foo" };
    service.setAuthenticationIds(userId, authIds);
    got = service.getAuthenticationIds(userId);
    checkAuthIds(userId, got, authIds);

    String user2Id = service.createUser("musterAuth");
    got = service.getAuthenticationIds(user2Id);
    checkAuthIds(userId, got, new String[] { "musterAuth" });

    String id = service.lookUpUserByAuthId("foo");
    assertEquals("user-id mismatch,", userId, id);

    id = service.lookUpUserByAuthId("musterAuth");
    assertEquals("user-id mismatch,", user2Id, id);

    id = service.lookUpUserByAuthId("id1");
    assertEquals("user-id mismatch,", userId, id);

    id = service.lookUpUserByAuthId("bar");
    assertNull("user-id mismatch", id);

    service.deleteUser(userId);
    service.deleteUser(user2Id);
  }

  public void testUserAccountState()
      throws RemoteException, NoSuchUserIdException, DuplicateAuthIdException, IOException {
    userId = service.createUser("musterAuth");
    int state = service.getState(userId);
    assertEquals("state mismatch,", 0, state);

    service.setState(userId, 42);
    state = service.getState(userId);
    assertEquals("state mismatch,", 42, state);

    boolean gotE = false;
    try {
      service.setState("foo:bar", 42);
    } catch (NoSuchUserIdException nsie) {
      assertEquals("no-such-user mismatch", "foo:bar", nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getState("foo:bar");
    } catch (NoSuchUserIdException nsie) {
      assertEquals("no-such-user mismatch", "foo:bar", nsie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    service.deleteUser(userId);
  }

  private void checkAuthIds(String userId, String[] got, String[] exp) {
    assertNotNull("got null auth-ids for '" + userId + "'", got);
    assertEquals("Number of auth-ids mismatch;", exp.length, got.length);

    Arrays.sort(got);
    Arrays.sort(exp);

    for (int idx = 0; idx < got.length; idx++)
      assertEquals("Auth-id mismatch, ", exp[idx], got[idx]);
  }
}
