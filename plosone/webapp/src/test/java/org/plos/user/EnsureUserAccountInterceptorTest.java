/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import org.plos.ApplicationException;
import org.plos.BasePlosoneTestCase;
import org.plos.Constants;
import org.plos.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class EnsureUserAccountInterceptorTest extends BasePlosoneTestCase {

  public void testShouldForwardToCreateNewAccount() throws Exception {
    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor();
    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final Map<String, Object> map = new HashMap<String, Object>();
    map.put(Constants.SINGLE_SIGNON_USER_KEY, "ASDASDASD12312313EDB");
    ActionContext.getContext().setSession(map);

    interceptor.setUserService(getUserService());

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.NEW_PROFILE, result);
  }

  public void testShouldForwardToUpdateNewAccount() throws Exception {
    final String GUID = "ASDASDASD12312313EDB";
    final PlosOneUser plosOneUser = new PlosOneUser(GUID);
    plosOneUser.setUserId("topazId");
    plosOneUser.setEmail("viru@home.com");
    plosOneUser.setDisplayName(null); //Display name is not set
    plosOneUser.setRealName("virender");

    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor() {
      protected Map<String, Object> getUserSessionMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.SINGLE_SIGNON_USER_KEY, "SINGLE_SIGNON_KEY_ASDASDASD12312313EDB");
        map.put(Constants.PLOS_ONE_USER_KEY, plosOneUser);
        return map;
      }
    };

    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final UserService mockUserService = new UserService() {
      public PlosOneUser getUserByAuthId(final String guid) throws ApplicationException {
        return plosOneUser;
      }
    };
    interceptor.setUserService(mockUserService);

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.UPDATE_PROFILE, result);

  }

  public void testShouldForwardToOriginalAction() throws Exception {
    final String GUID = "ASDASDASD12312313EDB";
    final PlosOneUser plosOneUser = new PlosOneUser(GUID);
    plosOneUser.setUserId("topazId");
    plosOneUser.setEmail("viru@home.com");
    plosOneUser.setDisplayName("Viru");  //Display name is already set
    plosOneUser.setRealName("virender");

    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor() {
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

    final UserService mockUserService = new UserService() {
      public PlosOneUser getUserByAuthId(final String guid) throws ApplicationException {
        return plosOneUser;
      }
    };

    interceptor.setUserService(mockUserService);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(actionCalledStatus, result);
  }

}
