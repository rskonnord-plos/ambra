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
package org.ambraproject.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.ambraproject.Constants;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.service.permission.PermissionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Ensures that the user has the required role.
 *
 * TODO: This doesn't check for roles any longer, it shouldn't be named as such
 *
 */
public class EnsureRoleInterceptor extends AbstractInterceptor {
  private static final Logger log = LoggerFactory.getLogger(EnsureRoleInterceptor.class);

  private PermissionsService permissionsService;

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    log.debug("EnsureRoleInterceptor called");

    Map session = actionInvocation.getInvocationContext().getSession();
    final String authId = (String)session.get(Constants.AUTH_KEY);


    try {
      permissionsService.checkPermission(Permission.ACCESS_ADMIN, authId);
      return actionInvocation.invoke();
    } catch (SecurityException ex) {
      log.debug("User does not have ACCESS_ADMIN permission");
      return Constants.ReturnCode.NOT_SUFFICIENT_ROLE;
    }
  }

  /**
   * Set the permissionsService
   * @param permissionsService permissionsService
   */
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }
}
