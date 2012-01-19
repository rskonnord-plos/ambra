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
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.PermissionsClientFactory;
import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

/**
 * Simple tests for the profiles service.
 *
 * @author Ronald Tschalär
 */
public class ProfilesServiceTest extends TestCase {
  private Profiles     service;
  private UserAccounts userService;
  private Permissions  permsService;
  private String       userId;
  private String[]     guestIds;

  public ProfilesServiceTest(String testName) {
    super(testName);
  }

  protected void setUp()
      throws MalformedURLException, ServiceException, DuplicateAuthIdException, RemoteException {
    String uri = "http://localhost:9997/ws-pap/services/ProfilesServicePort";
    service = ProfilesClientFactory.create(uri);

    uri = "http://localhost:9997/ws-users/services/UserAccountsServicePort";
    userService = UserAccountsClientFactory.create(uri);

    uri = "http://localhost:9997/ws-permissions/services/PermissionsServicePort";
    permsService = PermissionsClientFactory.create(uri);

    // create the users
    userId   = userService.createUser("musterAuth");
    guestIds = new String[] { userService.createUser("guestAuth") };
  }

  protected void tearDown() throws RemoteException {
    try {
      service.setProfile(userId, null);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }

    for (int idx = 0; idx < guestIds.length; idx++) {
      String[] l = permsService.listGrants(userId, guestIds[idx]);
      if (l.length > 0)
        permsService.cancelGrants(userId, l, new String[] { guestIds[idx] });

      l = permsService.listRevokes(userId, guestIds[idx]);
      if (l.length > 0)
      permsService.cancelRevokes(userId, l, new String[] { guestIds[idx] });

      try {
        userService.deleteUser(guestIds[idx]);
      } catch (NoSuchUserIdException nsie) {
        // looks like it was clean
      }
    }

    try {
      userService.deleteUser(userId);
    } catch (NoSuchUserIdException nsie) {
      // looks like it was clean
    }
  }

  public void testBasicProfiles() throws RemoteException, NoSuchUserIdException, IOException {
    // test non-existent user
    boolean gotE = false;
    try {
      service.setProfile("id:muster42", null);
    } catch (NoSuchUserIdException nsuie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    gotE = false;
    try {
      service.getProfile("id:muster42");
    } catch (NoSuchUserIdException nsie) {
      gotE = true;
    }
    assertTrue("Failed to get expected NoSuchUserIdException", gotE);

    // test empty profile
    UserProfile prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);

    // test profile
    prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch", "hans@muster.eu", prof.getEmail());
    assertEquals("Homepage mismatch", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null given-name, got '" + prof.getGivenNames() + "'", prof.getGivenNames());
    assertNull("non-null surname, got '" + prof.getSurnames() + "'", prof.getSurnames());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null position-type, got '" + prof.getPositionType() + "'",
               prof.getPositionType());
    assertNull("non-null organization-name, got '" + prof.getOrganizationName() + "'",
               prof.getOrganizationName());
    assertNull("non-null organization-type, got '" + prof.getOrganizationType() + "'",
               prof.getOrganizationType());
    assertNull("non-null postal-address, got '" + prof.getPostalAddress() + "'",
               prof.getPostalAddress());
    assertNull("non-null city, got '" + prof.getCity() + "'", prof.getCity());
    assertNull("non-null country, got '" + prof.getCountry() + "'", prof.getCountry());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());
    assertNull("non-null biography-text, got '" + prof.getBiographyText() + "'",
               prof.getBiographyText());
    assertNull("non-null interests-text, got '" + prof.getInterestsText() + "'",
               prof.getInterestsText());
    assertNull("non-null research-areas-text, got '" + prof.getResearchAreasText() + "'",
               prof.getResearchAreasText());

    prof.setHomePage(null);
    prof.setEmail("hans.muster@sample.com");
    prof.setWeblog("http://muster.blogs.org/yak");
    prof.setInterests(new String[] { "http://i1.org/", "http://i2.org/" });
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans.muster@sample.com", prof.getEmail());
    assertEquals("Weblog mismatch;", "http://muster.blogs.org/yak", prof.getWeblog());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null surname, got '" + prof.getSurnames() + "'", prof.getSurnames());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null position-type, got '" + prof.getPositionType() + "'",
               prof.getPositionType());
    assertNull("non-null organization-name, got '" + prof.getOrganizationName() + "'",
               prof.getOrganizationName());
    assertNull("non-null organization-type, got '" + prof.getOrganizationType() + "'",
               prof.getOrganizationType());
    assertNull("non-null postal-address, got '" + prof.getPostalAddress() + "'",
               prof.getPostalAddress());
    assertNull("non-null city, got '" + prof.getCity() + "'", prof.getCity());
    assertNull("non-null country, got '" + prof.getCountry() + "'", prof.getCountry());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    String[] i = prof.getInterests();
    assertEquals("Interests mismatch;", 2, i.length);
    if (!(i[0].equals("http://i1.org/") && i[1].equals("http://i2.org/") ||
          i[1].equals("http://i1.org/") && i[0].equals("http://i2.org/")))
      fail("Interests mismatch; i0='" + i[0] + "', i1='" + i[1] + "'");
    assertNull("non-null biography-text, got '" + prof.getBiographyText() + "'",
               prof.getBiographyText());
    assertNull("non-null interests-text, got '" + prof.getInterestsText() + "'",
               prof.getInterestsText());
    assertNull("non-null research-areas-text, got '" + prof.getResearchAreasText() + "'",
               prof.getResearchAreasText());

    prof.setRealName("rn");
    prof.setGivenNames("a b");
    prof.setSurnames("c d");
    prof.setTitle("mr");
    prof.setGender("male");
    prof.setPositionType("type1");
    prof.setOrganizationName("acme");
    prof.setOrganizationType("general");
    prof.setPostalAddress("Easy St 42\nLightcity");
    prof.setCity("Lightcity");
    prof.setCountry("universe");
    prof.setBiography("http://bio/");
    prof.setPublications("http://pubs/");
    prof.setInterests(null);
    prof.setBiographyText("I was born");
    prof.setInterestsText("sex");
    prof.setResearchAreasText("molusks");
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans.muster@sample.com", prof.getEmail());
    assertEquals("Weblog mismatch;", "http://muster.blogs.org/yak", prof.getWeblog());
    assertEquals("Publications mismatch;", "http://pubs/", prof.getPublications());
    assertEquals("Real-name mismatch;", "rn", prof.getRealName());
    assertEquals("Given-names mismatch;", "a b", prof.getGivenNames());
    assertEquals("Surnames mismatch;", "c d", prof.getSurnames());
    assertEquals("Title mismatch;", "mr", prof.getTitle());
    assertEquals("Gender mismatch;", "male", prof.getGender());
    assertEquals("Position-type mismatch;", "type1", prof.getPositionType());
    assertEquals("Organization-name mismatch;", "acme", prof.getOrganizationName());
    assertEquals("Organization-type mismatch;", "general", prof.getOrganizationType());
    assertEquals("Postal-address mismatch;", "Easy St 42\nLightcity", prof.getPostalAddress());
    assertEquals("City mismatch;", "Lightcity", prof.getCity());
    assertEquals("Country mismatch;", "universe", prof.getCountry());
    assertEquals("Biography mismatch;", "http://bio/", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());
    assertEquals("Biography-text mismatch;", "I was born", prof.getBiographyText());
    assertEquals("Interests-text mismatch;", "sex", prof.getInterestsText());
    assertEquals("Research-areas-text mismatch;", "molusks", prof.getResearchAreasText());

    service.setProfile(userId, null);
    prof = service.getProfile(userId);
    assertNull("non-null profile for user '" + userId + "'", prof);
  }

  public void testFindUsers() throws RemoteException, NoSuchUserIdException, IOException {
    // simple empty queries
    String[] users = service.findUsersByProfile(null, null);
    assertEquals("non-empty user list for empty query", 0, users.length);

    users = service.findUsersByProfile(new UserProfile[0], null);
    assertEquals("non-empty user list for empty query", 0, users.length);

    // single template queries
    UserProfile templ = new UserProfile();

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("non-empty user list for empty query", 0, users.length);

    templ.setDisplayName("foo");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("non-empty user list", 0, users.length);

    UserProfile prof = getExampleProfile();
    service.setProfile(userId, prof);

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("non-empty user list", 0, users.length);

    templ.setDisplayName(null);
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setDisplayName("Hans");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setTitle("mr");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setTitle("mrs");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("non-empty user list", 0, users.length);

    templ.setDisplayName(null);
    templ.setTitle(null);
    templ.setRealName("rn");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setRealName(null);
    templ.setGivenNames("a b");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setGivenNames(null);
    templ.setSurnames("c d");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setSurnames(null);
    templ.setTitle("mr");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setTitle(null);
    templ.setGender("male");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setGender(null);
    templ.setPositionType("type1");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setPositionType(null);
    templ.setOrganizationName("acme");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setOrganizationName(null);
    templ.setOrganizationType("general");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setOrganizationType(null);
    templ.setPostalAddress("Easy St 42\nLightcity");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setPostalAddress(null);
    templ.setCity("Lightcity");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setCity(null);
    templ.setCountry("universe");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setCountry(null);
    templ.setEmail("hans@muster.eu");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setEmail(null);
    templ.setHomePage("http://www.muster.eu/");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setHomePage(null);
    templ.setBiography("http://bio/");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setBiography(null);
    templ.setPublications("http://pubs/");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setPublications(null);
    templ.setWeblog("http://muster.blogs.org/yak");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setWeblog(null);
    templ.setInterests(new String[] { "http://i1.org/", "http://i2.org/" });
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setInterests(null);
    templ.setBiographyText("I was born");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setBiographyText(null);
    templ.setInterestsText("sex");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setInterestsText(null);
    templ.setResearchAreasText("molusks");
    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    // multi-template tests
    UserProfile templ2 = new UserProfile();

    templ2.setDisplayName("Hans");
    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ.setDisplayName("Hans1");
    templ2.setDisplayName("Hans");
    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    // multi-user tests
    UserProfile prof2 = getExampleProfile();
    prof2.setDisplayName("Hans2");
    prof2.setRealName("Hans Muster");
    service.setProfile(guestIds[0], prof2);

    templ.setDisplayName(null);
    templ2.setDisplayName(null);

    users = service.findUsersByProfile(new UserProfile[] { templ2 }, new boolean[] { false });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    templ.setResearchAreasText(null);
    templ2.setResearchAreasText(null);
    templ.setRealName("rn");
    templ2.setDisplayName("Hans2");
    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    templ.setDisplayName("Hans1");
    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", guestIds[0], users[0]);

    // case-insensitive tests
    templ  = new UserProfile();
    templ2 = new UserProfile();

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { true });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    templ.setDisplayName("hans");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { true });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    templ2.setRealName("hans muster");

    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, false });
    assertEquals("wrong number of users from query", 0, users.length);

    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { true, false });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", userId, users[0]);

    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { false, true });
    assertEquals("wrong number of users from query", 1, users.length);
    assertEquals("wrong user-id from query", guestIds[0], users[0]);

    users = service.findUsersByProfile(new UserProfile[] { templ, templ2 },
                                       new boolean[] { true, true });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    templ.setDisplayName(null);
    templ2.setRealName(null);
    templ.setResearchAreasText("Molusks");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { true });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);
    templ2.setHomePage("http://WWW.Muster.EU/");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 0, users.length);

    templ.setResearchAreasText(null);
    templ.setHomePage("http://WWW.Muster.EU/");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);
    assertEquals("wrong user-id from query", guestIds[0], users[1]);

    templ2.setHomePage(null);
    templ.setCity("lightCity");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { true });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 0, users.length);

    templ.setCity(null);
    templ.setWeblog("http://Muster.Blogs.Org/Yak");

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { true });
    assertEquals("wrong number of users from query", 2, users.length);
    Arrays.sort(users);
    assertEquals("wrong user-id from query", userId, users[0]);

    users = service.findUsersByProfile(new UserProfile[] { templ }, new boolean[] { false });
    assertEquals("wrong number of users from query", 0, users.length);

    // clean up
    service.setProfile(userId, null);
    service.setProfile(guestIds[0], null);
  }

  private UserProfile getExampleProfile() {
    UserProfile prof = new UserProfile();

    prof.setDisplayName("Hans");
    prof.setRealName("rn");
    prof.setGivenNames("a b");
    prof.setSurnames("c d");
    prof.setTitle("mr");
    prof.setGender("male");
    prof.setPositionType("type1");
    prof.setOrganizationName("acme");
    prof.setOrganizationType("general");
    prof.setPostalAddress("Easy St 42\nLightcity");
    prof.setCity("Lightcity");
    prof.setCountry("universe");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    prof.setBiography("http://bio/");
    prof.setPublications("http://pubs/");
    prof.setWeblog("http://muster.blogs.org/yak");
    prof.setInterests(new String[] { "http://i1.org/", "http://i2.org/" });
    prof.setBiographyText("I was born");
    prof.setInterestsText("sex");
    prof.setResearchAreasText("molusks");

    return prof;
  }

  /* We need to be able to log in in order to run these tests.
  public void testProfilesPermissions() throws RemoteException, IOException {
    UserProfile prof = new UserProfile();
    prof.setDisplayName("Hans");
    prof.setEmail("hans@muster.eu");
    prof.setHomePage("http://www.muster.eu/");
    service.setProfile(userId, prof);

    prof = service.getProfile(userId);
    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans@muster.eu", prof.getEmail());
    assertEquals("Homepage mismatch;", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null name, got '" + prof.getDisplayName() + "'", prof.getDisplayName());
    assertNull("non-null email, got '" + prof.getEmail() + "'", prof.getEmail());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    permsService.grant(userId, new String[] { "profiles:getEmail" }, guestIds);

    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null name, got '" + prof.getDisplayName() + "'", prof.getDisplayName());
    assertEquals("Email mismatch;", "hans@muster.eu", prof.getEmail());
    assertNull("non-null homepage, got '" + prof.getHomePage() + "'", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    permsService.revoke(userId, new String[] { "profiles:getEmail" }, guestIds);
    permsService.grant(userId, new String[] { "profiles:getHomePage", "profiles:getDisplayName" },
                       guestIds);

    doAsUser(guestIds[0], prof = service.getProfile(userId));

    assertEquals("Name mismatch;", "Hans", prof.getDisplayName());
    assertNull("non-null email, got '" + prof.getEmail() + "'", prof.getEmail());
    assertEquals("Homepage mismatch;", "http://www.muster.eu/", prof.getHomePage());
    assertNull("non-null real-name, got '" + prof.getRealName() + "'", prof.getRealName());
    assertNull("non-null title, got '" + prof.getTitle() + "'", prof.getTitle());
    assertNull("non-null gender, got '" + prof.getGender() + "'", prof.getGender());
    assertNull("non-null weblog, got '" + prof.getWeblog() + "'", prof.getWeblog());
    assertNull("non-null pubs, got '" + prof.getPublications() + "'", prof.getPublications());
    assertNull("non-null biography, got '" + prof.getBiography() + "'", prof.getBiography());
    assertNull("non-null interests, got '" + prof.getInterests() + "'", prof.getInterests());

    service.setProfile(userId, null);
    doAsUser(guestIds[0], prof = service.getProfile(userId));
    assertNull("non-null profile for user '" + userId + "'", prof);
  }
  */
}
