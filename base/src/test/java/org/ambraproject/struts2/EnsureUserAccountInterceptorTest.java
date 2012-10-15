/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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
package org.ambraproject.struts2;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseInterceptorTest;
import org.ambraproject.models.UserLogin;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.ambraproject.Constants;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class EnsureUserAccountInterceptorTest extends BaseInterceptorTest {

  @Autowired
  protected EnsureUserAccountInterceptor interceptor;

  @Test
  public void testInterceptWithNoCASReciept() throws Exception {
    removeFromSession(Constants.AUTH_KEY);
    String result = interceptor.intercept(actionInvocation);
    assertEquals(result, Action.SUCCESS, "interceptor didn't forward to action invocation");
  }
  @Test
  public void testShouldForwardToOriginalAction() throws Exception {
    final String GUID = "ASDASDASD12312313EDB";
    final UserProfile ambraUser = new UserProfile();
    ambraUser.setAuthId(GUID);
    ambraUser.setEmail("viru@home.com");
    ambraUser.setDisplayName("Viru");  //Display name is already set
    ambraUser.setRealName("virender");

    putInSession(Constants.AUTH_KEY, ambraUser.getAuthId());
    putInSession(Constants.SINGLE_SIGNON_EMAIL_KEY, ambraUser.getEmail());
    putInSession(Constants.AMBRA_USER_KEY, ambraUser);

    final String result = interceptor.intercept(actionInvocation);
    assertEquals(result, Action.SUCCESS, "Interceptor didn't allow action invocation to proceed");
  }

  @Test
  public void testLookupUserAndLogin() throws Exception {
    UserProfile user = new UserProfile();
    user.setDisplayName("displayNameForUserAccountInterceptor");
    user.setEmail("email@UserAccountInterceptor.org");
    user.setPassword("pass");
    dummyDataStore.store(user);

    putInSession(Constants.AUTH_KEY, user.getAuthId());
    putInSession(Constants.SINGLE_SIGNON_EMAIL_KEY, user.getEmail());
    //make sure there's no ambra user in session (which forces the interceptor to look it up from the db)
    assertNull(getFromSession(Constants.AMBRA_USER_KEY), "Session already had an ambra user in it");

    String result = interceptor.intercept(actionInvocation);
    assertEquals(result, Action.SUCCESS, "Interceptor didn't forward to action");
    UserProfile cachedUser = (UserProfile) getFromSession(Constants.AMBRA_USER_KEY);
    assertNotNull(cachedUser, "interceptor didn't cache a user in session");
    assertEquals(cachedUser.getID(), user.getID(), "Session cached incorrect user");
    assertEquals(cachedUser.getEmail(), user.getEmail(), "cached user had incorrect email");
    assertEquals(cachedUser.getDisplayName(), user.getDisplayName(), "cached user had incorrect display name");
    assertEquals(cachedUser.getAuthId(), user.getAuthId(), "cached user had incorrect auth id");

    //check that the user got a login object saved
    assertEquals(countLogins(user.getID()), 1,
        "Interceptor didn't store a login object for the user");
  }

  private int countLogins(Long userId) {
    List<UserLogin> allLogins = dummyDataStore.getAll(UserLogin.class);
    int count = 0;
    for (UserLogin login : allLogins) {
      if (login.getUserProfileID().equals(userId)) {
        count++;
      }
    }
    return count;
  }
}

