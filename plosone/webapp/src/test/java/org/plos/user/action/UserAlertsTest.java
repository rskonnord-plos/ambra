/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.action;

import static com.opensymphony.xwork.Action.SUCCESS;
import org.apache.commons.lang.ArrayUtils;
import org.plos.BasePlosoneTestCase;
import org.plos.Constants;
import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.user.PlosOneUser;

import java.util.HashMap;
import java.util.Map;

public class UserAlertsTest extends BasePlosoneTestCase {
  final String AUTH_ID = UserAlertsTest.class.getName();

  public void testCreateAlerts() throws Exception {
//    getUserWebService().deleteUser("info:doi/10.1371/account/141");
    final String topazId = createUser(AUTH_ID);
    final UserAlertsAction alertsAction = getMockUserAlertsAction(AUTH_ID, topazId);
    final String[] weeklyAlertCategories = new String[]{
                "biology",
                "clinical_trials",
                "computational_biology",
                "genetics",
                "pathogens"
          };

    final String[] monthlyAlertCategories = new String[]{
                "plosone",
                "clinical_trials",
                "genetics",
                "pathogens"
          };
    final String ALERT_EMAIL = "alert@emailaddress.com";

    alertsAction.setMonthlyAlerts(monthlyAlertCategories);
    alertsAction.setWeeklyAlerts(weeklyAlertCategories);

    assertEquals(SUCCESS, alertsAction.saveAlerts());
    assertEquals(SUCCESS, alertsAction.retrieveAlerts());

    for (final String monthlyAlert : alertsAction.getMonthlyAlerts()) {
      assertTrue(ArrayUtils.contains(monthlyAlertCategories, monthlyAlert));
    }

    for (final String weeklyAlert : alertsAction.getWeeklyAlerts()) {
      assertTrue(ArrayUtils.contains(weeklyAlertCategories, weeklyAlert));
    }

    getUserWebService().deleteUser(topazId);
  }

  protected UserAlertsAction getMockUserAlertsAction(final String authId, final String topazId) {
    final UserAlertsAction userAlertsAction = super.getMemberUserAlertsAction();
    final UserAlertsAction newUserAlertsAction = new MemberUserAlertsAction() {
      private final Map<String, Object> mockSessionMap = createMockSessionMap(authId, topazId);
      protected Map<String, Object> getSessionMap() {
        return mockSessionMap;
      }
    };

    newUserAlertsAction.setUserService(userAlertsAction.getUserService());

    return newUserAlertsAction;
  }

  private String createUser(final String authId) throws Exception {
    final UserProfileAction createUserAction = getMockCreateUserAction(authId);
    createUserAction.setEmail("UserAlertsTest@test.com");
    createUserAction.setRealName("UserAlertsTest test com");
    createUserAction.setAuthId(authId);
    createUserAction.setDisplayName("UserAlertsTest");
    assertEquals(SUCCESS, createUserAction.executeSaveUser());
    final String topazId = createUserAction.getInternalId();
    assertNotNull(topazId);

    return topazId;
  }

  protected UserProfileAction getMockCreateUserAction(final String authId) {
    final UserProfileAction createUserAction = super.getMemberUserProfileAction();
    final UserProfileAction newCreateUserAction = new MemberUserProfileAction() {
      private Map<String,Object> mockSessionMap = createMockSessionMap(authId, null);
      protected Map<String, Object> getSessionMap() {
        return mockSessionMap;
      }
    };

    newCreateUserAction.setUserService(createUserAction.getUserService());

    return newCreateUserAction;
  }

  private Map<String, Object> createMockSessionMap(final String authId, final String topazId) {
    final PlosOneUser plosOneUser = new PlosOneUser(authId);
    if (null != topazId) {
      plosOneUser.setUserId(topazId);
    }

    final Map<String, Object> sessionMap = new HashMap<String, Object>();
    sessionMap.put(PLOS_ONE_USER_KEY, plosOneUser);
    sessionMap.put(Constants.SINGLE_SIGNON_USER_KEY, authId);

    return sessionMap;
  }

}
