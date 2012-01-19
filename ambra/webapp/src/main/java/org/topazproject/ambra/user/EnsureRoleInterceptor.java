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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.topazproject.ambra.Constants;
import org.topazproject.ambra.user.service.UserService;

/**
 * Ensures that the user has the required role.
 *
 */
public class EnsureRoleInterceptor extends AbstractInterceptor {
  private static final Logger log = LoggerFactory.getLogger(EnsureRoleInterceptor.class);

  private UserService userService;

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    log.debug("EnsureRoleInterceptor called");

    if (userService.allowAdminAction())
      return actionInvocation.invoke();

    return Constants.ReturnCode.NOT_SUFFICIENT_ROLE;
  }

  /**
   * Set the userService
   * @param userService userService
   */
  public void setUserService(final UserService userService) {
    this.userService = userService;
  }
}
