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
package org.topazproject.ambra.user;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.mock.MockActionInvocation;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.user.service.DisplayNameAlreadyExistsException;
import org.topazproject.ambra.user.service.UserService;

import java.util.Collection;
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

    final UserService mockUserService = new TestUserService(ambraUser);
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

    final UserService mockUserService = new TestUserService(ambraUser);

    interceptor.setUserService(mockUserService);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(actionCalledStatus, result);
  }

  /**
   * Wrapper around UserService for testing
   */
  private class TestUserService implements UserService {

    private AmbraUser ambraUser;

    public TestUserService(AmbraUser ambraUser) {
      this.ambraUser = ambraUser;
    }

    @Override
    public String createUser(String authId) throws ApplicationException {
      return getUserService().createUser(authId);
    }

    @Override
    public void deleteUser(String userId) throws ApplicationException {
      getUserService().deleteUser(userId);
    }

    @Override
    public String getUsernameById(String userId) throws ApplicationException {
      return getUserService().getUsernameById(userId);
    }

    @Override
    public AmbraUser getUserById(String userId) throws ApplicationException {
      return ambraUser;
    }

    @Override
    public AmbraUser getUserWithProfileLoaded(String topazUserId) throws ApplicationException {
      return ambraUser;
    }

    @Override
    public AmbraUser getUserByAuthId(String authId) throws ApplicationException {
      return ambraUser;
    }

    @Override
    public UserAccount getUserAccountByAuthId(String authId) throws ApplicationException {
      return getUserService().getUserAccountByAuthId(authId);
    }

    @Override
    public void setState(String userId, int state) throws ApplicationException {
      getUserService().setState(userId, state);
    }

    @Override
    public int getState(String userId) throws ApplicationException {
      return getUserService().getState(userId);
    }

    @Override
    public String getAuthenticationId(String userId) throws ApplicationException {
      return getUserService().getAuthenticationId(userId);
    }

    @Override
    public String lookUpUserByAuthId(String authId) throws ApplicationException {
      return getUserService().lookUpUserByAuthId(authId);
    }

    @Override
    public String lookUpUserByAccountId(String accountId) throws ApplicationException {
      return getUserService().lookUpUserByAccountId(accountId);
    }

    @Override
    public String lookUpUserByEmailAddress(String emailAddress) throws ApplicationException {
      return getUserService().lookUpUserByEmailAddress(emailAddress);
    }

    @Override
    public String lookUpUserByDisplayName(String name) throws ApplicationException {
      return getUserService().lookUpUserByDisplayName(name);
    }

    @Override
    public void setProfile(AmbraUser inUser) throws ApplicationException, DisplayNameAlreadyExistsException {
      getUserService().setProfile(inUser);
    }

    @Override
    public void setProfile(AmbraUser inUser, String[] privateFields, boolean userNameIsRequired) throws ApplicationException, DisplayNameAlreadyExistsException {
      getUserService().setProfile(inUser, privateFields, userNameIsRequired);
    }

    @Override
    public void setProfile(AmbraUser inUser, boolean userNameIsRequired) throws ApplicationException, DisplayNameAlreadyExistsException {
      getUserService().setProfile(inUser, userNameIsRequired);
    }

    @Override
    public Collection<UserProfileGrant> getProfileFieldsThatArePublic(String topazId) throws ApplicationException {
      return getUserService().getProfileFieldsThatArePublic(topazId);
    }

    @Override
    public Collection<UserProfileGrant> getProfileFieldsThatArePrivate(String topazId) throws ApplicationException {
      return getUserService().getProfileFieldsThatArePrivate(topazId);
    }

    @Override
    public void setProfileFieldsPrivate(String topazId, UserProfileGrant[] profileGrantEnums) throws ApplicationException {
      getUserService().setProfileFieldsPrivate(topazId, profileGrantEnums);
    }

    @Override
    public void setProfileFieldsPublic(String topazId, UserProfileGrant[] profileGrantEnums) throws ApplicationException {
      getUserService().setProfileFieldsPublic(topazId, profileGrantEnums);
    }

    @Override
    public void setPreferences(AmbraUser inUser) throws ApplicationException {
      getUserService().setPreferences(inUser);
    }

    @Override
    public void setRole(String topazId, String roleId) throws ApplicationException {
      getUserService().setRole(topazId, roleId);
    }

    @Override
    public void setRole(String topazId, String[] roleIds) throws ApplicationException {
      getUserService().setRole(topazId, roleIds);
    }

    @Override
    public String[] getRole(String topazId) throws ApplicationException {
      return getUserService().getRole(topazId);
    }

    @Override
    public boolean allowAdminAction() {
      return getUserService().allowAdminAction();
    }

    @Override
    public String getEmailAddressUrl() {
      return getUserService().getEmailAddressUrl();
    }
  }
}

