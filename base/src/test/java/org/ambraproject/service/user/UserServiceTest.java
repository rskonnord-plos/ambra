/*
 * Copyright (c) 2006-2013 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.user;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserLogin;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.models.UserSearch;
import org.ambraproject.service.search.SearchParameters;
import org.ambraproject.views.SavedSearchView;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class UserServiceTest extends BaseTest {

  @Autowired
  protected UserService userService;

  @DataProvider(name = "userProfile")
  private Object[][] getUserProfile() {
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("nameForTestLogin");
    userProfile.setEmail("emailForTest1@Login.org");
    userProfile.setAuthId("authIdForTestLogin1");
    userProfile.setPassword("pass");
    Long id = Long.valueOf(dummyDataStore.store(userProfile));

    return new Object[][]{
        {id, userProfile}
    };
  }

  @DataProvider(name = "userProfileSave")
  private Object[][] getUserProfileForSave() {
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("saveTestLogin");
    userProfile.setEmail("emailForTest2@Login.org");
    userProfile.setAuthId("authIdForTestLogin2");
    userProfile.setPassword("pass");
    Long id = Long.valueOf(dummyDataStore.store(userProfile));

    return new Object[][]{
        {id, userProfile}
    };
  }

  @DataProvider(name = "userProfileUpdate")
  private Object[][] getUserProfileForUpdate() {
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("updateTestLogin");
    userProfile.setEmail("emailForTest3@Login.org");
    userProfile.setAuthId("authIdForTestLogin3");
    userProfile.setPassword("pass");
    Long id = Long.valueOf(dummyDataStore.store(userProfile));

    return new Object[][]{
        {id, userProfile}
    };
  }

  @DataProvider(name = "userProfileDelete")
  private Object[][] getUserProfileForDelete() {
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("deleteTestLogin");
    userProfile.setEmail("emailForTest3@Login.org");
    userProfile.setAuthId("authIdForTestLogin3");
    userProfile.setPassword("pass");
    Long id = Long.valueOf(dummyDataStore.store(userProfile));

    return new Object[][]{
        {id, userProfile}
    };
  }


  @Test(dataProvider = "userProfile")
  public void testGetUser(Long id, UserProfile userProfile) {
    UserProfile result = userService.getUser(id);
    assertNotNull(result, "User Service returned null user");
    assertEquals(result.getDisplayName(), userProfile.getDisplayName(), "User Service returned user with incorrect display name");
    assertEquals(result.getEmail(), userProfile.getEmail(), "User Service returned user with incorrect email");
    assertEquals(result.getAuthId(), userProfile.getAuthId(), "User Service returned user with incorrect auth id");
  }

  @Test(dataProvider = "userProfile")
  public void testGetUserByAuthId(Long id, UserProfile userProfile) {
    UserProfile result = userService.getUserByAuthId(userProfile.getAuthId());
    assertNotNull(result, "user service returned null profile");
    assertEquals(result.getID(), id, "user service returned incorrect user profile");
  }

  @Test(dataProvider = "userProfile")
  public void testLogin(Long id, UserProfile userProfile) throws Exception {
    UserLogin login = new UserLogin("sessionId", "IP", "userAgent");
    int numLogins = getUserLogins(id).size();
    userService.login(userProfile.getAuthId(), login);

    List<UserLogin> storedLogins = getUserLogins(id);
    assertEquals(storedLogins.size(), numLogins + 1, "login didn't get stored to the database");
    assertEquals(storedLogins.get(storedLogins.size() - 1).getUserAgent(), login.getUserAgent(),
        "stored login had incorrect user agent");
    assertEquals(storedLogins.get(storedLogins.size() - 1).getIP(), login.getIP(),
        "stored login had incorrect IP");
    assertEquals(storedLogins.get(storedLogins.size() - 1).getSessionId(), login.getSessionId(),
        "stored login had incorrect sessionID");
  }

  @Test(dataProvider = "userProfile")
  public void testRecordArticleView(Long userId, UserProfile userProfile) {
    Article article = new Article();
    article.setDoi("id:test-article-to-record-viewing");
    Long articleID = Long.valueOf(dummyDataStore.store(article));

    Long viewId = userService.recordArticleView(userId, articleID, ArticleView.Type.ARTICLE_VIEW);
    assertNotNull(viewId, "returned null view id");
    ArticleView storedView = dummyDataStore.get(ArticleView.class, viewId);
    assertNotNull(storedView, "didn't store article view");
    assertEquals(storedView.getType(), ArticleView.Type.ARTICLE_VIEW, "Stored view had incorrect type");
    assertEquals(storedView.getArticleID(), articleID, "Stored view had incorrect article id");
    assertEquals(storedView.getUserID(), userId, "Stored view had incorrect type");
  }

  @Test
  public void testLogSearchTerms() {
    userService.recordUserSearch(5L, "search terms", "search params");

    List<UserSearch> allSearches = dummyDataStore.getAll(UserSearch.class);

    assertEquals(allSearches.size(), 1);
    assertEquals(allSearches.get(0).getUserProfileID().longValue(), 5L);
    assertEquals(allSearches.get(0).getSearchParams(), "search params");
    assertEquals(allSearches.get(0).getSearchTerms(), "search terms");
  }

  @Test(dataProvider = "userProfileSave")
  public void testSaveSearch(Long id, UserProfile userProfile) {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.setQuery("test query");
    searchParameters.setStartPage(5);
    searchParameters.setVolume("volumne");

    userService.saveSearch(id, searchParameters, "testSave", true, false);

    List<SavedSearchView> savedSearch = userService.getSavedSearches(id);

    assertEquals(savedSearch.size(), 1);
    assertEquals(savedSearch.get(0).getSearchName(), "testSave");
    assertEquals(savedSearch.get(0).getMonthly(), false);
    assertEquals(savedSearch.get(0).getWeekly(), true);

    SearchParameters params = savedSearch.get(0).getSearchParameters();

    assertEquals(params.getQuery(), "test query", "Search params not parsed correctly");
    assertEquals(params.getStartPage(), 5, "Search params not parsed correctly");
    assertEquals(params.getVolume(), "volumne", "Search params not parsed correctly");
  }

  @Test
  public void testSaveSearchHashing() {
    //Make sure the database is empty, unit test order is arbitrary
    dummyDataStore.deleteAll(SavedSearchQuery.class);
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("nameForHashingTest");
    userProfile.setEmail("nameForHashingTest@Login.org");
    userProfile.setAuthId("authIDnameForHashingTest");
    userProfile.setPassword("pass");

    Long id1 = Long.valueOf(dummyDataStore.store(userProfile));

    SearchParameters searchParameters = new SearchParameters();

    searchParameters.setQuery("test query");
    searchParameters.setStartPage(5);
    searchParameters.setVolume("volumne");

    userService.saveSearch(id1, searchParameters, "testSave", true, false);

    userProfile = new UserProfile();
    userProfile.setDisplayName("nameForHashingTest1");
    userProfile.setEmail("nameForHashingTest1@Login.org");
    userProfile.setAuthId("authIdnameForHashingTest1");
    userProfile.setPassword("pass");

    Long id2 = Long.valueOf(dummyDataStore.store(userProfile));

    searchParameters = new SearchParameters();

    searchParameters.setQuery("test query");
    searchParameters.setStartPage(5);
    searchParameters.setVolume("volumne");

    userService.saveSearch(id2, searchParameters, "testSave", true, false);

    List<SavedSearchQuery> params = dummyDataStore.getAll(SavedSearchQuery.class);
    assertEquals(1, params.size(), "More then one searchparams created!");
  }


  @Test(dataProvider = "userProfileDelete")
  public void testDeleteSearch(Long id, UserProfile userProfile) {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.setQuery("test query");
    searchParameters.setStartPage(5);
    searchParameters.setVolume("volumne");

    userService.saveSearch(id, searchParameters, "testDelete", true, false);

    List<SavedSearchView> savedSearches = userService.getSavedSearches(id);
    assertEquals(savedSearches.size(), 1, "Saved search not saved");

    userService.deleteSavedSearch(userProfile.getID(), savedSearches.get(0).getSavedSearchId());

    savedSearches = userService.getSavedSearches(id);
    assertEquals(savedSearches.size(), 0, "Saved search not deleted");
  }

  @Test(dataProvider = "userProfileUpdate")
  public void testUpdateSearch(Long id, UserProfile userProfile) {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.setQuery("test query");
    searchParameters.setStartPage(5);
    searchParameters.setVolume("volumne");

    userService.saveSearch(id, searchParameters, "testUpdate", true, true);

    List<SavedSearchView> savedSearches = userService.getSavedSearches(id);

    assertEquals(savedSearches.size(), 1, "Saved search not saved");

    SavedSearchView ss = savedSearches.get(0);

    assertEquals(ss.getSearchType(), SavedSearchType.USER_DEFINED, "Search type is not user defined.");
    assertEquals(ss.getMonthly(), true, "Saved search not saved correctly");
    assertEquals(ss.getWeekly(), true, "Saved search not saved correctly");

    userService.updateSavedSearch(ss.getSavedSearchId(), false, false);

    savedSearches = userService.getSavedSearches(id);
    assertEquals(savedSearches.size(),1, "Saved search not updated");
    ss = savedSearches.get(0);

    assertEquals(ss.getMonthly(), false, "Saved search not updated correctly");
    assertEquals(ss.getWeekly(), false, "Saved search not updated correctly");
  }

  @Test
  public void testLoginWithNonexistentUser() {
    UserProfile login = userService.login("this-isnot-areal-authid", new UserLogin());
    assertNull(login, "User service didn't return null for non-existent user");
  }

  @Test
  public void testUpdateUser() throws DuplicateUserException, NoSuchUserException {
    UserProfile userProfile = new UserProfile();
    userProfile.setAuthId("authIdForUpdatingUser");
    userProfile.setEmail("email@updateUser.org");
    userProfile.setDisplayName("displayNameForUpdatingUser");
    userProfile.setPassword("pass");
    userProfile.setBiography("Regina is the mayor of Storybrooke and Henry’s adoptive mother—responsibilities she has " +
        "been balancing, without help, since she adopted Henry as a newborn. Despite the demands of her job, Regina " +
        "is an extremely attentive mother to Henry. At times, though, she can be a bit overbearing. This is " +
        "especially true whenever she crosses paths with Emma, Henry’s birthmother, with whom she makes no effort " +
        "to play nice.");
    Long id = Long.valueOf(dummyDataStore.store(userProfile));
    String newBio = "Upon Emma’s arrival in Storybrooke, Regina senses the very real threat she presents to her " +
        "relationship with Henry and immediately takes action to run Emma out of town. Nothing is too drastic " +
        "for Regina, who seems able to mobilize the entire population of Storybrooke to hassle Emma during her stay.";

    userProfile.setBiography(newBio);
    userService.updateProfile(userProfile);
    String storedBio = dummyDataStore.get(UserProfile.class, id).getBiography();
    assertEquals(storedBio, newBio, "User didn't get biography updated");
  }

  @Test
  public void testUpdateUserDoesNotOverwriteRoles() throws DuplicateUserException, NoSuchUserException {
    UserProfile user = new UserProfile(
        "email@overwriteRoles.org",
        "displayNameForOverwriteRoles",
        "pass");
    user.setRoles(new HashSet<UserRole>(dummyDataStore.getAll(UserRole.class)));
    int numRoles = user.getRoles().size();
    assertTrue(numRoles > 0, "There were no stored roles to assign"); //shouldn't happen

    Long id = Long.valueOf(dummyDataStore.store(user));

    user.setRoles(new HashSet<UserRole>());

    userService.updateProfile(user);
    Set<UserRole> storedRoles = dummyDataStore.get(UserProfile.class, id).getRoles();
    assertEquals(storedRoles.size(), numRoles, "Roles got overwritten");
  }

  @Test
  public void testUpdateDoesNotOverwriteProfileUri() throws DuplicateUserException, NoSuchUserException {
    String profileUri = "id:test-profile-uri-for-overwrite-check";
    UserProfile user = new UserProfile(
        "email@overwriteUris.org",
        "displayNameForOverwriteUris",
        "pass");
    user.setProfileUri(profileUri);
    Long id = Long.valueOf(dummyDataStore.store(user));

    user.setProfileUri(null);

    userService.updateProfile(user);
    UserProfile storedUser = dummyDataStore.get(UserProfile.class, id);
    assertEquals(storedUser.getProfileUri(), profileUri, "account uri got overwritten");
  }

  @Test(expectedExceptions = {NoSuchUserException.class})
  public void testUpdateNonexistentUser() throws NoSuchUserException {
    userService.updateProfile(new UserProfile("updateNonExistent@example.org", "updateNonExistent", "pass"));
  }

  @Test
  public void testGetAvailableAlerts() {
    //these come from the config
    List<UserAlert> alerts = new ArrayList<UserAlert>(2);
    alerts.add(new UserAlert("journal", "Journal", true, true, false));
    alerts.add(new UserAlert("journal1", "Journal 1", false, true, false));

    for (UserAlert alert : userService.getAvailableAlerts()) {
      UserAlert matchingAlert = null;
      for (UserAlert expectedAlert : alerts) {
        if (alert.getKey().equals(expectedAlert.getKey())) {
          matchingAlert = expectedAlert;
          break;
        }
      }
      assertNotNull(matchingAlert, "didn't find a matching alert for " + alert);
      assertEquals(alert.isMonthlyAvailable(), matchingAlert.isMonthlyAvailable(), "alert had incorrect monthly availability");
      assertEquals(alert.isWeeklyAvailable(), matchingAlert.isWeeklyAvailable(), "alert had incorrect weekly availability");
      assertEquals(alert.getName(), matchingAlert.getName(), "alert had incorrect name");
    }
  }

  @Test(dataProvider = "userProfile")
  public void testSetAlerts(Long id, UserProfile userProfile) {
    List<String> monthlyAlerts = new ArrayList<String>(2);
    monthlyAlerts.add("this_is_a_new_alert");
    monthlyAlerts.add("this_is_a_new_alert2");
    List<String> weeklyAlerts = new ArrayList<String>(1);
    weeklyAlerts.add("this_is_a_new_weekly_alert");

    String expectedAlerts = "";
    for (String monthly : monthlyAlerts) {
      expectedAlerts += (monthly + UserProfile.MONTHLY_ALERT_SUFFIX + UserProfile.ALERTS_SEPARATOR);
    }
    for (String weekly : weeklyAlerts) {
      expectedAlerts += (weekly + UserProfile.WEEKLY_ALERT_SUFFIX + UserProfile.ALERTS_SEPARATOR);
    }
    //remove the last comma
    expectedAlerts = expectedAlerts.substring(0, expectedAlerts.lastIndexOf(UserProfile.ALERTS_SEPARATOR));


    userService.setAlerts(userProfile.getAuthId(), monthlyAlerts, weeklyAlerts);

    UserProfile storedUser = dummyDataStore.get(UserProfile.class, id);
    assertEquals(storedUser.getAlertsJournals(), expectedAlerts, "User Service stored incorrect alerts");
    assertEquals(storedUser.getAlertsList().toArray(), expectedAlerts.split(","),
        "User Service stored incorrect alerts");

    //Now try removing all the alerts
    userService.setAlerts(userProfile.getAuthId(), new ArrayList<String>(0), null);
    storedUser = dummyDataStore.get(UserProfile.class, id);
    assertNull(storedUser.getAlertsJournals(), "User Service didn't remove alerts");
    assertEquals(storedUser.getAlertsList().size(), 0, "User Service didn't remove alerts");
    assertEquals(storedUser.getMonthlyAlerts().size(), 0, "User Service didn't remove monthly alerts");
    assertEquals(storedUser.getWeeklyAlerts().size(), 0, "User Service didn't remove weekly alerts");
  }

  @Test
  public void testGetUserForDisplay() {
    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("foo_mcFoo");
    userProfile.setGivenNames("Foo");
    userProfile.setOrganizationName("foo");
    userProfile.setOrganizationType("university");
    userProfile.setPostalAddress("123 fake st");
    userProfile.setPositionType("a position type");
    userProfile.setBiography("<a href=\"http://www.trainwithmeonline.com/stretching_exercises.html\"" +
        " rel=\"dofollow\">stretching exercises</a>");

    UserProfile display = userService.getProfileForDisplay(userProfile, false);
    assertEquals(userProfile.getDisplayName(), userProfile.getDisplayName(), "user service changed the display name");
    assertEquals(userProfile.getGivenNames(), userProfile.getGivenNames(), "user service changed the given names");
    assertNull(display.getOrganizationName(), "user service didn't clear organization name");
    assertNull(display.getOrganizationType(), "user service didn't clear organization type");
    assertNull(display.getPositionType(), "user service didn't clear position type");
    assertNull(display.getPostalAddress(), "user service didn't clear organization address");
    assertEquals(display.getBiography(),
        "&lt;a href=&quot;http://www.trainwithmeonline.com/stretching_exercises.html&quot;" +
            " rel=&quot;dofollow&quot;&gt;stretching exercises&lt;/a&gt;",
        "User Service didn't escape html in biography");


    display = userService.getProfileForDisplay(userProfile, true);
    assertEquals(display.getOrganizationName(),userProfile.getOrganizationName(),
        "user service didn't show organization name with showPrivateFields set to true");
    assertEquals(display.getOrganizationType(),userProfile.getOrganizationType(),
        "user service didn't show organization type with showPrivateFields set to true");
    assertEquals(display.getPostalAddress(), userProfile.getPostalAddress(),
        "user service didn't show organization address with showPrivateFields set to true");
  }

  @Test()
  public void testSavedFilteredWeeklySearchAlert() {
    dummyDataStore.deleteAll(SavedSearch.class);
    dummyDataStore.deleteAll(SavedSearchQuery.class);
    dummyDataStore.deleteAll(UserProfile.class);

    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName("foo_mcFoo");
    userProfile.setGivenNames("Foo");
    userProfile.setEmail("foo_mcFoo@Login.org");
    userProfile.setOrganizationName("foo");
    userProfile.setOrganizationType("university");
    userProfile.setPostalAddress("123 fake st");
    userProfile.setPositionType("a position type");
    userProfile.setPassword("bleh");

    Long userProfileID = Long.valueOf(dummyDataStore.store(userProfile));

    String[] subjects = new String[] { "subject1,", "subject2" };
    String journal = "PLOSOne";

    userService.setFilteredWeeklySearchAlert(userProfileID, subjects, journal);
    userProfile = userService.getUser(userProfileID);

    List<SavedSearch> savedSearches = userProfile.getSavedSearches();

    assertEquals(savedSearches.size(), 1);
    assertNotNull(savedSearches.get(0).getSearchQuery());
    SavedSearchQuery ssq1 = savedSearches.get(0).getSearchQuery();

    List<SavedSearchView> searchViews = userService.getSavedSearches(userProfile.getID());

    assertEquals(searchViews.size(), 1);

    SavedSearchView ssv = searchViews.get(0);
    SearchParameters ssp = ssv.getSearchParameters();

    assertEquals(ssv.getSearchType(), SavedSearchType.JOURNAL_ALERT);
    assertEquals(ssp.getFilterSubjectsDisjunction(), subjects);
    assertEquals(ssp.getFilterJournals(), new String[] { journal });

    userProfile = new UserProfile();
    userProfile.setDisplayName("foo_mcFoo1");
    userProfile.setGivenNames("Foo1");
    userProfile.setEmail("foo_mcFoo1@Login.org");
    userProfile.setOrganizationName("foo1");
    userProfile.setOrganizationType("university1");
    userProfile.setPostalAddress("123 fake st1");
    userProfile.setPositionType("a position type one");
    userProfile.setPassword("bleh");

    userProfileID = Long.valueOf(dummyDataStore.store(userProfile));

    userService.setFilteredWeeklySearchAlert(userProfileID, subjects, journal);

    userProfile = userService.getUser(userProfileID);
    savedSearches = userProfile.getSavedSearches();

    assertEquals(savedSearches.size(), 1);
    assertNotNull(savedSearches.get(0).getSearchQuery());

    SavedSearchQuery ssq2 = savedSearches.get(0).getSearchQuery();

    //assert queries match
    assertEquals(ssq1, ssq2);
  }

  private List<UserLogin> getUserLogins(Long userId) {
    List<UserLogin> allLogins = dummyDataStore.getAll(UserLogin.class);
    List<UserLogin> userLogins = new ArrayList<UserLogin>();
    for (UserLogin login : allLogins) {
      if (login.getUserProfileID().equals(userId)) {
        userLogins.add(login);
      }
    }
    return userLogins;
  }

  @AfterClass
  public void restoreUserData() {
    restoreDefaultUsers();
  }
}
