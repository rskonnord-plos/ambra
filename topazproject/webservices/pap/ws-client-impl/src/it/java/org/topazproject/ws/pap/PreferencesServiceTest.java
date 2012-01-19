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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

/**
 * Simple tests for the preferences service.
 *
 * @author Ronald Tschalär
 */
public class PreferencesServiceTest extends TestCase {
  private Preferences  service;
  private UserAccounts userService;
  private String       userId;
  private int testId = -1;

  public PreferencesServiceTest(String testName) {
    super(testName);
  }

  public PreferencesServiceTest(String testName, int testId) {
    super(testName);
    this.testId = testId;
  }

  public void setUp()
      throws MalformedURLException, ServiceException, DuplicateAuthIdException, RemoteException {
    String uri = "http://localhost:9997/ws-pap/services/PreferencesServicePort";
    service = PreferencesClientFactory.create(uri);

    uri = "http://localhost:9997/ws-users/services/UserAccountsServicePort";
    userService = UserAccountsClientFactory.create(uri);

    // create a user
    String authId = "musterAuth";
    if (testId >= 0)
      authId += testId;
    try {
      userId = userService.createUser(authId);
    } catch (DuplicateAuthIdException e) {
      userId = userService.lookUpUserByAuthId(authId);
    }
  }

  public void tearDown() throws RemoteException {
    try {
      service.setPreferences(null, userId, null);
    } catch (NoSuchUserIdException nsuie) {
      // looks like it was clean
    }

    try {
      userService.deleteUser(userId);
    } catch (NoSuchUserIdException nsuie) {
      // looks like it was clean
    }
  }

  public void testBasicPreferences() throws RemoteException, NoSuchUserIdException, IOException {
    UserPreference[] prefs, got, exp, exp2;

    // test NoSuchUserIdException
    boolean gotE = false;
    try {
      service.setPreferences("testApp1", "foo:muster42", null);
    } catch (NoSuchUserIdException nsuie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    // test null prefs
    service.setPreferences("testApp1", userId, null);

    prefs = service.getPreferences("testApp1", userId);
    assertNull("non-null prefs, got " + prefs, prefs);

    prefs = service.getPreferences(null, userId);
    assertNull("non-null prefs, got " + prefs, prefs);

    // test 0-length prefs
    service.setPreferences("testApp1", userId, new UserPreference[0]);
    prefs = service.getPreferences(null, userId);
    assertNull("non-null prefs, got " + prefs, prefs);

    // test simple prefs
    prefs = new UserPreference[3];
    prefs[0] = new UserPreference();
    prefs[1] = new UserPreference();
    prefs[2] = new UserPreference();

    prefs[0].setName("empty");
    prefs[0].setValues(new String[0]);
    prefs[1].setName("email");
    prefs[1].setValues(new String[] { "no" });
    prefs[2].setName("langs");
    prefs[2].setValues(new String[] { "english", "swahili" });

    service.setPreferences("testApp1", userId, prefs);

    got = service.getPreferences("testApp1", userId);

    exp = new UserPreference[2];
    exp[0] = prefs[1];
    exp[1] = prefs[2];
    compare(got, exp);

    // test app-ids
    got = service.getPreferences(null, userId);
    compare(got, exp);

    prefs = new UserPreference[3];
    prefs[0] = new UserPreference();
    prefs[1] = new UserPreference();
    prefs[2] = new UserPreference();

    prefs[0].setName("foo");
    prefs[0].setValues(new String[] { "bar" });
    prefs[1].setName("email2");
    prefs[1].setValues(new String[] { "no" });
    prefs[2].setName("langs2");
    prefs[2].setValues(new String[] { "french", "korean" });
    service.setPreferences("testApp2", userId, prefs);

    got = service.getPreferences("testApp1", userId);
    compare(got, exp);

    got = service.getPreferences("testApp2", userId);
    compare(got, prefs);

    got = service.getPreferences(null, userId);
    exp2 = new UserPreference[5];
    exp2[0] = prefs[0];
    exp2[1] = prefs[1];
    exp2[2] = prefs[2];
    exp2[3] = exp[0];
    exp2[4] = exp[1];
    compare(got, exp2);

    service.setPreferences("testApp1", userId, null);

    got = service.getPreferences("testApp2", userId);
    compare(got, prefs);

    got = service.getPreferences(null, userId);
    compare(got, prefs);

    service.setPreferences(null, userId, null);
  }

  private static void sort(UserPreference[] prefs) {
    // first sort by name
    Arrays.sort(prefs, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((UserPreference) o1).getName().compareTo(((UserPreference) o2).getName());
      }
    });

    // now sort values
    for (int idx = 0; idx < prefs.length; idx++)
      Arrays.sort(prefs[idx].getValues());
  }

  private static void compare(UserPreference[] got, UserPreference[] exp) {
    assertNotNull("got null prefs", got);
    assertEquals("wrong nubmer of prefs,", exp.length,  got.length);

    sort(got);
    sort(exp);

    for (int idx = 0; idx < got.length; idx++) {
      assertEquals("Pref name mismatch", exp[idx].getName(), got[idx].getName());
      assertNotNull("Pref null values", got[idx].getValues());
      assertTrue("values mismatch for " + got[idx].getName(),
                 Arrays.equals(got[idx].getValues(), exp[idx].getValues()));
    }
  }
}
