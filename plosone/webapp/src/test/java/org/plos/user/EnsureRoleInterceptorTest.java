/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.mock.MockActionInvocation;
import org.plos.BasePlosoneTestCase;
import org.plos.Constants;
import org.plos.user.service.UserRoleWebService;
import org.plos.user.service.UserService;
import org.topazproject.ws.users.NoSuchUserIdException;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class EnsureRoleInterceptorTest extends BasePlosoneTestCase {

  public void testShouldReturnErrorAsUserNotLoggedIn() throws Exception {
    final EnsureRoleInterceptor interceptor = new EnsureRoleInterceptor();
    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final Map<String, Object> map = new HashMap<String, Object>();
    map.put(Constants.SINGLE_SIGNON_USER_KEY, "A_GUID");
    ActionContext.getContext().setSession(map);

    final UserService userService = getUserServiceReturningRole("webUser");
    interceptor.setUserService(userService);
    interceptor.setRoleToCheck(Constants.ADMIN_ROLE);

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Action.ERROR, result);
  }

  public void testShouldReturnNotSufficientRole() throws Exception {
    final String GUID = "A_GUID";
    final PlosOneUser plosOneUser = new PlosOneUser(GUID);
    plosOneUser.setUserId("topazId");
    plosOneUser.setEmail("viru@home.com");
    plosOneUser.setDisplayName("Viru");  //Display name is already set
    plosOneUser.setRealName("virender");

    final EnsureRoleInterceptor interceptor = new EnsureRoleInterceptor() {
      protected Map<String, Object> getUserSessionMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.SINGLE_SIGNON_USER_KEY, "A_SINGLE_SIGNON_KEY");
        map.put(Constants.PLOS_ONE_USER_KEY, plosOneUser);
        return map;
      }
    };

    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final Map<String, Object> map = new HashMap<String, Object>();
    map.put(Constants.SINGLE_SIGNON_USER_KEY, "ASDASDASD12312313EDB");
    ActionContext.getContext().setSession(map);

    final UserService userService = getUserServiceReturningRole("webUser");
    interceptor.setUserService(userService);
    interceptor.setRoleToCheck(Constants.ADMIN_ROLE);

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.NOT_SUFFICIENT_ROLE, result);
  }

  public void testShouldForwardToOriginalActionAsUserIsAdmin() throws Exception {
    final String GUID = "ASDASDASD12312313EDB";
    final PlosOneUser plosOneUser = new PlosOneUser(GUID);
    plosOneUser.setUserId("topazId");
    plosOneUser.setEmail("viru@home.com");
    plosOneUser.setDisplayName("Viru");  //Display name is already set
    plosOneUser.setRealName("virender");

    final EnsureRoleInterceptor interceptor = new EnsureRoleInterceptor() {
      protected Map<String, Object> getUserSessionMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.SINGLE_SIGNON_USER_KEY, "SINGLE_SIGNON_KEY_ASDASDASD12312313EDB");
        map.put(Constants.PLOS_ONE_USER_KEY, plosOneUser);
        return map;
      }
    };

    final String actionCalledStatus = "actionCalled";

    final ActionInvocation actionInvocation = new MockActionInvocation() {
      public String invoke() throws Exception {
        return actionCalledStatus;
      }
    };

    final UserService userService = getUserServiceReturningRole(Constants.ADMIN_ROLE);
    interceptor.setUserService(userService);
    interceptor.setRoleToCheck(Constants.ADMIN_ROLE);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(actionCalledStatus, result);
  }

  private UserService getUserServiceReturningRole(final String roleToReturn) {
    final UserService userService = getUserService();
    final UserRoleWebService userRoleWebService = new UserRoleWebService() {
      public String[] getRoles(final String topazId) throws NoSuchUserIdException, RemoteException {
        return new String[]{roleToReturn};
      }
    };
    userService.setUserRoleWebService(userRoleWebService);
    return userService;
  }

}
