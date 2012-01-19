/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
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
package org.topazproject.ambra.user;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.user.EnsureUserAccountInterceptor;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class EnsureUserAccountInterceptorTest extends BaseAmbraTestCase {

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
    final AmbraUser ambraUser = new AmbraUser(GUID);
    ambraUser.setUserId("topazId");
    ambraUser.setEmail("viru@home.com");
    ambraUser.setDisplayName(null); //Display name is not set
    ambraUser.setRealName("virender");

    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor() {
      protected Map<String, Object> getUserSessionMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.SINGLE_SIGNON_USER_KEY, "SINGLE_SIGNON_KEY_ASDASDASD12312313EDB");
        map.put(Constants.AMBRA_USER_KEY, ambraUser);
        return map;
      }
    };

    final MockActionInvocation actionInvocation = new MockActionInvocation();

    final UserService mockUserService = new UserService() {
      public AmbraUser getUserByAuthId(final String guid) throws ApplicationException {
        return ambraUser;
      }
    };
    interceptor.setUserService(mockUserService);

    actionInvocation.setAction(null);
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(Constants.ReturnCode.UPDATE_PROFILE, result);

  }

  public void testShouldForwardToOriginalAction() throws Exception {
    final String GUID = "ASDASDASD12312313EDB";
    final AmbraUser ambraUser = new AmbraUser(GUID);
    ambraUser.setUserId("topazId");
    ambraUser.setEmail("viru@home.com");
    ambraUser.setDisplayName("Viru");  //Display name is already set
    ambraUser.setRealName("virender");

    final EnsureUserAccountInterceptor interceptor = new EnsureUserAccountInterceptor() {
      protected Map<String, Object> getUserSessionMap() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.SINGLE_SIGNON_USER_KEY, "SINGLE_SIGNON_KEY_ASDASDASD12312313EDB");
        map.put(Constants.AMBRA_USER_KEY, ambraUser);
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
      public AmbraUser getUserByAuthId(final String guid) throws ApplicationException {
        return ambraUser;
      }
    };

    interceptor.setUserService(mockUserService);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(actionCalledStatus, result);
  }

}
