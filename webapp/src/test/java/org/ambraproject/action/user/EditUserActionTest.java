package org.ambraproject.action.user;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import org.ambraproject.Constants;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.user.UserAlert;
import org.ambraproject.service.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 10/23/12
 */
public class EditUserActionTest extends AmbraWebTest {
  
  @Autowired
  protected EditUserAction action;
  
  @Override
  protected BaseActionSupport getAction() {
    return action; 
  }

  @Autowired
  protected UserService userService;

  @DataProvider(name = "user")
  public Object[][] getUser() {
    UserProfile user = new UserProfile();
    user.setEmail("userActionTest@ambraproject.org");
    user.setDisplayName("TEST_USERNAME");
    user.setGivenNames("my GIVENNAMES");
    user.setSurname("my Surnames");
    user.setPositionType("my POSITION_TYPE");
    user.setOrganizationType("my organizationType");
    user.setPostalAddress("my postalAddress");
    user.setBiography("my biographyText");
    user.setInterests("my interestsText");
    user.setResearchAreas("my researchAreasText");
    user.setCity("my city");
    user.setCountry("my country");
    user.setAuthId("auth-id-for-MemberUserActionTest");
    user.setPassword("pass");
    user.setAlertsJournals("journal_weekly,journal_monthly");
    dummyDataStore.store(user);

    String[] expectedWeeklyAlerts = new String[]{"journal"};
    String[] expectedMonthlyAlerts = new String[]{"journal"};

    //these come from the config
    List<UserAlert> alerts = new ArrayList<UserAlert>(2);
    alerts.add(new UserAlert("journal", "Journal", true, true));
    alerts.add(new UserAlert("journal1", "Journal 1", false, true));

    return new Object[][]{
        {user, alerts, expectedWeeklyAlerts, expectedMonthlyAlerts}
    };
  }

  @BeforeMethod
  public void setupSession() {
    //add email to the session to ensure that the action doesn't try and talk to CAS
    UserProfile user = (UserProfile) getUser()[0][0];
    login(user);
  }

  @BeforeMethod
  public void resetAction() {
    action.setEmail(null);
    action.setDisplayName(null);
    action.setGivenNames(null);
    action.setSurnames(null);
    action.setPositionType(null);
    action.setOrganizationType(null);
    action.setPostalAddress(null);
    action.setBiographyText(null);
    action.setInterestsText(null);
    action.setResearchAreasText(null);
    action.setCity(null);
    action.setCountry(null);
    action.setWeblog(null);
    action.setHomePage(null);
    action.setAlertsJournals(null);
    action.setMonthlyAlerts(null);
    action.setWeeklyAlerts(null);
  }

  /**
   * Regression test for a bug that never made it into production in which updating a profile would overwrite the user's roles
   *
   * @param user
   */
  @Test(dataProvider = "user")
  public void testUpdateDoesNotOverwriteRoles(UserProfile user, List<UserAlert> expectedAlerts,
                                              String[] expectedWeeklyAlerts, String[] expectedMonthlyAlerts) throws Exception {
    UserRole role = new UserRole("test role for UserProfileActionTest");
    dummyDataStore.store(role);
    UserProfile storedUser = userService.getUserByAuthId(user.getAuthId());
    storedUser.getRoles().add(role);
    dummyDataStore.update(storedUser);

    action.setDisplayName(storedUser.getDisplayName());
    action.setGivenNames(storedUser.getGivenNames());
    action.setSurnames(storedUser.getSurname());
    String result = action.saveUser();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "action had errors: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "action had field errors: " + StringUtils.join(action.getFieldErrors().values(), ";"));


    storedUser = userService.getUserByAuthId(user.getAuthId());
    assertTrue(storedUser.getRoles().size() > 0, "Roles got erased");
  }

  @Test(dataProvider = "user")
  public void testExecuteWithExistingUser(UserProfile user, List<UserAlert> expectedAlerts,
                                          String[] expectedWeeklyAlerts, String[] expectedMonthlyAlerts) throws Exception {
    assertEquals(action.execute(), Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages: " + StringUtils.join(action.getActionErrors(), ";"));

    assertEquals(action.getEmail(), user.getEmail(), "action didn't have correct email");
    assertEquals(action.getDisplayName(), user.getDisplayName(), "action didn't have correct display name");
    assertEquals(action.getGivenNames(), user.getGivenNames(), "action didn't have correct given names");
    assertEquals(action.getSurnames(), user.getSurname(), "action didn't have correct surnames");
    assertEquals(action.getPositionType(), user.getPositionType(), "action didn't have correct position type");
    assertEquals(action.getOrganizationType(), user.getOrganizationType(), "action didn't have correct organization type");
    assertEquals(action.getPostalAddress(), user.getPostalAddress(), "action didn't have correct postal address");
    assertEquals(action.getBiographyText(), user.getBiography(), "action didn't have correct biography text");
    assertEquals(action.getInterestsText(), user.getInterests(), "action didn't have correct interests text");
    assertEquals(action.getResearchAreasText(), user.getResearchAreas(), "action didn't have correct research areas text");
    assertEquals(action.getCity(), user.getCity(), "action didn't have correct city");
    assertEquals(action.getCountry(), user.getCountry(), "action didn't have correct country");

    assertEquals(action.getUserAlerts().size(), expectedAlerts.size(), "Action didn't return correct number of alerts");
    for (UserAlert alert : action.getUserAlerts()) {
      UserAlert matchingAlert = null;
      for (UserAlert expectedAlert : expectedAlerts) {
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
    assertEqualsNoOrder(action.getWeeklyAlerts(), expectedWeeklyAlerts, "Action had incorrect weekly alerts for user");
    assertEqualsNoOrder(action.getMonthlyAlerts(), expectedMonthlyAlerts, "Action had incorrect monthly alerts for user");

  }

  @Test(dataProvider = "user", dependsOnMethods = {"testExecuteWithExistingUser"})
  public void testEditAlreadySavedUser(UserProfile user, List<UserAlert> expectedAlerts,
                                       String[] expectedWeeklyAlerts, String[] expectedMonthlyAlerts) throws Exception {
    long testStart = Calendar.getInstance().getTime().getTime();
    String newOrganizationType = "new organization type";
    String newPositionType = "new position type";

    action.setPositionType(newPositionType);
    action.setOrganizationType(newOrganizationType);
    action.setOrganizationVisibility(true);

    action.setEmail(user.getEmail());
    action.setDisplayName(user.getDisplayName());
    action.setGivenNames(user.getGivenNames());
    action.setSurnames(user.getSurname());
    action.setPostalAddress(user.getPostalAddress());
    action.setBiographyText(user.getBiography());
    action.setInterestsText(user.getInterests());
    action.setResearchAreasText(user.getResearchAreas());
    action.setCity(user.getCity());
    action.setCountry(user.getCountry());

    assertEquals(action.saveUser(), Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages: " + StringUtils.join(action.getActionErrors(), ";"));

    UserProfile savedUser = userService.getUserByAuthId(user.getAuthId());

    assertEquals(savedUser.getOrganizationType(), newOrganizationType, "user didn't get organization type set");
    assertEquals(savedUser.getPositionType(), newPositionType, "user didn't get position type set on it");
    assertTrue(savedUser.getOrganizationVisibility(), "user didn't get organization visibility set");

    //make sure none of the other properties got changed
    assertEquals(savedUser.getEmail(), user.getEmail(), "user's email changed");
    assertEquals(savedUser.getRealName(), user.getGivenNames() + " " + user.getSurname(), "user's real name changed");
    assertEquals(savedUser.getDisplayName(), user.getDisplayName(), "user's display name changed");
    assertEquals(savedUser.getGivenNames(), user.getGivenNames(), "user's given names changed");
    assertEquals(savedUser.getSurname(), user.getSurname(), "user's surnames changed");
    assertEquals(savedUser.getPostalAddress(), user.getPostalAddress(), "user's postal address changed");
    assertEquals(savedUser.getBiography(), user.getBiography(), "user's biography text changed");
    assertEquals(savedUser.getInterests(), user.getInterests(), "user's interests text changed");
    assertEquals(savedUser.getResearchAreas(), user.getResearchAreas(), "user's research areas text changed");
    assertEquals(savedUser.getCity(), user.getCity(), "user's city changed");
    assertEquals(savedUser.getCountry(), user.getCountry(), "user's country changed");

    assertTrue(savedUser.getLastModified().getTime() >= testStart, "user didn't have lastmodified date updated");

    UserProfile cachedUser = (UserProfile) getFromSession(Constants.AMBRA_USER_KEY);
    assertNotNull(cachedUser, "user didn't get cached in session");
    assertEquals(cachedUser.getID(), savedUser.getID(), "incorrect user got cached in session");
    assertEquals(cachedUser.getOrganizationType(), newOrganizationType, "cached user didn't have new organization type");
    assertEquals(cachedUser.getPositionType(), newPositionType, "cached user didn't have new position type");
    assertTrue(cachedUser.getOrganizationVisibility(), "cached user user didn't have new organization visibility");
  }

  @Test
  public void testSaveWithNullGivenNames() throws Exception {
    action.setDisplayName("testdisplayname");
    action.setGivenNames(null);
    action.setSurnames("foo");

    String result = action.saveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("givenNames"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithNullSurnames() throws Exception {
    action.setDisplayName("testdisplayname");
    action.setGivenNames("foo");
    action.setSurnames(null);

    String result = action.saveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("surnames"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithProfantiy() throws Exception {
    action.setDisplayName("fooadmin");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setBiographyText("ass");
    action.setHomePage(null);
    action.setWeblog(null);

    String result = action.saveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("biographyText"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithBadHomePage() throws Exception {
    action.setDisplayName("testDisplayName");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setHomePage("hello this is a bad home page");

    String result = action.saveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("homePage"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithBadBlog() throws Exception {
    action.setDisplayName("testDisplayName");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setWeblog("hello this is a bad blog");

    String result = action.saveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("weblog"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testEditAlerts() throws Exception {
    UserProfile user = new UserProfile();
    user.setEmail("email@testEditAlerts.org");
    user.setAuthId("authIdForTestEditAlerts");
    user.setDisplayName("displayNameForTestEditAlerts");
    user.setAlertsJournals("journal_weekly,journal_monthly");
    user.setPassword("pass");
    dummyDataStore.store(user);

    String[] newWeeklyAlerts = new String[]{"journal_weekly", "journal1_weekly"};
    String[] newMonthlyAlerts = new String[]{};
    String[] expectedAlerts = new String[]{"journal_weekly", "journal1_weekly"};

    ActionContext.getContext().getSession().put(Constants.AUTH_KEY, user.getAuthId());
    action.setSession(ActionContext.getContext().getSession());
    action.setWeeklyAlerts(newWeeklyAlerts);
    action.setMonthlyAlerts(newMonthlyAlerts);

    String result = action.saveAlerts();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    String storedAlerts = dummyDataStore.get(UserProfile.class, user.getID()).getAlertsJournals();
    assertEqualsNoOrder(storedAlerts.split(","), expectedAlerts, "action didn't store correct alerts to the database");
  }

  @Test
  public void testAddAlertsForUserWithNoAlerts() throws Exception {
    UserProfile user = new UserProfile();
    user.setEmail("email@testAddAlerts.org");
    user.setAuthId("authIdForTestAddAlerts");
    user.setDisplayName("displayNameForTestAddAlerts");
    user.setPassword("pass");
    dummyDataStore.store(user);

    String[] newWeeklyAlerts = new String[]{"journal_weekly", "journal1_weekly"};
    String[] newMonthlyAlerts = new String[]{"journal_monthly"};
    String[] expectedAlerts = new String[]{"journal_weekly", "journal1_weekly", "journal_monthly"};

    ActionContext.getContext().getSession().put(Constants.AUTH_KEY, user.getAuthId());
    action.setSession(ActionContext.getContext().getSession());
    action.setWeeklyAlerts(newWeeklyAlerts);
    action.setMonthlyAlerts(newMonthlyAlerts);

    String result = action.saveAlerts();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    String storedAlerts = dummyDataStore.get(UserProfile.class, user.getID()).getAlertsJournals();
    assertEqualsNoOrder(storedAlerts.split(","), expectedAlerts, "action didn't store correct alerts to the database");
  }

}
