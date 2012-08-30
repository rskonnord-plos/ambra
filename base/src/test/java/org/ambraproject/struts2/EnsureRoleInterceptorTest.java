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
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.ambraproject.Constants;

import static org.testng.Assert.assertEquals;

public class EnsureRoleInterceptorTest extends BaseInterceptorTest {

  @Autowired
  protected EnsureRoleInterceptor interceptor;

  @Test
  public void testShouldReturnNotSufficientRole() throws Exception {
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(result, Constants.ReturnCode.NOT_SUFFICIENT_ROLE, "Interceptor didn't block action invocation");
  }

  @Test
  public void testShouldForwardToOriginalActionAsUserIsAdmin() throws Exception {
    setupAdminContext();
    final String result = interceptor.intercept(actionInvocation);
    assertEquals(result, Action.SUCCESS, "Interceptor didn't allow action invocation to continue");
  }
}
