/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.service;

import com.opensymphony.xwork.Action;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.BasePlosoneTestCase;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;
import org.plos.user.action.DisplayUserAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class UserServiceTest extends BasePlosoneTestCase {
  private static final String TEST_EMAIL = "testcase@topazproject.org";
  private static final String REAL_NAME = "Test User";
  private static final String AUTH_ID = "Test AuthID";
  private static final String USERNAME= "TEST_USERNAME";

  private static final Log log = LogFactory.getLog(UserServiceTest.class);

  public void testCreateAdminUser() throws Exception {
    final String ADMIN_PREFIX = "admintest";
    final String USER_EMAIL = ADMIN_PREFIX + UserServiceTest.TEST_EMAIL;
    final String REAL_NAME = ADMIN_PREFIX + UserServiceTest.REAL_NAME;
    final String AUTH_ID = ADMIN_PREFIX + UserServiceTest.AUTH_ID;
    final String USERNAME = ADMIN_PREFIX + UserServiceTest.USERNAME;

    final String topazId = createUser(AUTH_ID, USER_EMAIL, USERNAME, REAL_NAME);
    ArrayList<String> topazIds = new ArrayList<String>();
    topazIds.add(topazId);  //add for teardown

    final DisplayUserAction displayUserAction = getDisplayUserAction();
    displayUserAction.setUserId(topazId);
    assertEquals(Action.SUCCESS, displayUserAction.execute());
    final PlosOneUser pou = displayUserAction.getPou();
    assertEquals(USER_EMAIL, pou.getEmail());
    assertEquals(REAL_NAME, pou.getRealName());
    assertEquals(USERNAME, pou.getDisplayName());

    {
      final String roleId = "admin";
      getUserService().setRole(topazId, roleId);
      final String[] returnedRoles = getUserService().getRole(topazId);
      assertTrue(Arrays.toString(returnedRoles).contains(roleId));
    }

    {
      final String[] roles = new String[]{"role1", "role2"};
      getUserService().setRole(topazId, roles);
      final String[] returnedRoles = getUserService().getRole(topazId);
      assertTrue(Arrays.toString(roles).equals(Arrays.toString(returnedRoles)));
    }

    for (final String tId : topazIds) {
      getUserService().deleteUser(tId);
    }
  }

  public void testUserProfilePermission() {
    final UserProfileGrant[] selected = new UserProfileGrant[]{
            UserProfileGrant.DISPLAY_NAME,
            UserProfileGrant.BIOGRAPHY,
            UserProfileGrant.FIND_USERS_BY_PROF_FIELD,
            UserProfileGrant.EMAIL
    };

    final String[] grants = new String[selected.length];

    for (int i = 0; i < selected.length; i++) {
      UserProfileGrant profileGrantEnum = selected[i];
      grants[i] = profileGrantEnum.getGrant();
    }

    final Collection<UserProfileGrant> collection = UserProfileGrant.getProfileGrantsForGrants(grants);
    
    for (final UserProfileGrant profileGrantEnum : collection) {
      assertTrue(ArrayUtils.contains(selected, profileGrantEnum));
      assertTrue(ArrayUtils.contains(grants, profileGrantEnum.getGrant()));
    }
  }

  public void testCreateUserWithFieldVisibilitySet() throws Exception {
//    deleteTopazIdSeries(201,301,20);

    final String USER_EMAIL = UserServiceTest.TEST_EMAIL;
    final String REAL_NAME = UserServiceTest.REAL_NAME;
    final String AUTH_ID = UserServiceTest.AUTH_ID;
    final String USERNAME = UserServiceTest.USERNAME;

    final String topazId = createUser(AUTH_ID, USER_EMAIL, USERNAME, REAL_NAME);
    ArrayList<String> topazIds = new ArrayList<String>();
    topazIds.add(topazId);  //add for teardown

    {
      final UserProfileGrant[] selectedGrantEnums = new UserProfileGrant[]{
                                                            UserProfileGrant.DISPLAY_NAME,
                                                            UserProfileGrant.EMAIL,
                                                            UserProfileGrant.BIOGRAPHY_TEXT
                                                          };

      getUserService().setProfileFieldsPublic(topazId, selectedGrantEnums);

      final Collection<UserProfileGrant> fieldsThatArePublic = getUserService().getProfileFieldsThatArePublic(topazId);
      for (final UserProfileGrant profileGrantEnum : fieldsThatArePublic) {
        assertTrue(ArrayUtils.contains(selectedGrantEnums, profileGrantEnum));
      }

      assertFalse(ArrayUtils.contains(selectedGrantEnums, UserProfileGrant.COUNTRY));
    }

    {
      final UserProfileGrant[] selectedGrantEnums = new UserProfileGrant[]{
                                                              UserProfileGrant.EMAIL,
                                                          };

      getUserService().setProfileFieldsPrivate(topazId, selectedGrantEnums);

      final Collection<UserProfileGrant> fieldsThatArePublic = getUserService().getProfileFieldsThatArePublic(topazId);
      for (final UserProfileGrant profileGrantEnum : fieldsThatArePublic) {
        assertFalse(ArrayUtils.contains(selectedGrantEnums, profileGrantEnum));
      }

      assertTrue(fieldsThatArePublic.contains(UserProfileGrant.EMAIL));
    }
    for (final String tId : topazIds) {
      getUserService().deleteUser(tId);
    }

  }

  private void deleteTopazIdSeries(final int startNum, final int endNum, final int step) throws Exception {
    for (int i = startNum; i < endNum; i+= step) {
        deleteAccount(i);
    }
  }

  private void deleteAccount(final int num) throws Exception {
    try {
      getUserService().deleteUser("info:doi/10.1371/account/" + num);
    } catch (Exception ex) {

    }
  }

  private String createUser(final String AUTH_ID, final String USER_EMAIL, final String USERNAME, final String REAL_NAME) throws ApplicationException, DisplayNameAlreadyExistsException {
    String topazId = getUserService().createUser(AUTH_ID);
    log.debug("topazId = " + topazId);

    final PlosOneUser newUser = new PlosOneUser(AUTH_ID);
    newUser.setUserId(topazId);
    newUser.setEmail(USER_EMAIL);
    newUser.setDisplayName(USERNAME);
    newUser.setRealName(REAL_NAME);

    getUserService().setProfile(newUser, new String[]{}, true);
    return topazId;
  }
}
