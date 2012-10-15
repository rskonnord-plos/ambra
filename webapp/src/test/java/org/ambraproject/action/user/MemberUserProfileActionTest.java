/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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
package org.ambraproject.action.user;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.Constants;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link MemberUserProfileAction}.  This test creates mock instances that are prevented from talking to CAS,
 * but behave the same in all other respects
 *
 * @author Alex Kudlick
 */
public class MemberUserProfileActionTest extends AmbraWebTest {

  @Autowired
  protected MemberUserProfileAction action;

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
    dummyDataStore.store(user);

    return new Object[][]{
        {user}
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
  }

  /**
   * Regression test for a bug that never made it into production in which updating a profile would overwrite the user's roles
   *
   * @param user
   */
  @Test(dataProvider = "user")
  public void testUpdateDoesNotOverwriteRoles(UserProfile user) throws Exception {
    UserRole role = new UserRole("test role for UserProfileActionTest");
    dummyDataStore.store(role);
    UserProfile storedUser = userService.getUserByAuthId(user.getAuthId());
    storedUser.getRoles().add(role);
    dummyDataStore.update(storedUser);

    action.setDisplayName(storedUser.getDisplayName());
    action.setGivenNames(storedUser.getGivenNames());
    action.setSurnames(storedUser.getSurname());
    String result = action.executeSaveUser();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "action had errors: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "action had field errors: " + StringUtils.join(action.getFieldErrors().values(), ";"));


    storedUser = userService.getUserByAuthId(user.getAuthId());
    assertTrue(storedUser.getRoles().size() > 0, "Roles got erased");
  }

  @Test(dataProvider = "user")
  public void testExecuteWithExistingUser(UserProfile user) throws Exception {
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
  }

  @Test(dataProvider = "user", dependsOnMethods = {"testExecuteWithExistingUser"})
  public void testEditAlreadySavedUser(UserProfile user) throws Exception {
    long testStart = Calendar.getInstance().getTime().getTime();
    String newOrganizationType = "new organization type";
    String newPositionType = "new position type";

    action.setPositionType(newPositionType);
    action.setOrganizationType(newOrganizationType);
    action.setOrgVisibility("public");

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

    assertEquals(action.executeSaveUser(), Action.SUCCESS, "Action didn't return success");
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

    String result = action.executeSaveUser();
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

    String result = action.executeSaveUser();
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

    String result = action.executeSaveUser();
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

    String result = action.executeSaveUser();
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

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("weblog"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
