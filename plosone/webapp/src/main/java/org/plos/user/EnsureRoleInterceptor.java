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
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.Constants;
import org.plos.user.service.UserService;

import java.util.Map;

/**
 * Ensures that the user has the required role.
 */
public class EnsureRoleInterceptor implements Interceptor {
  private UserService userService;
  private String roleToCheck;
  private static final Log log = LogFactory.getLog(EnsureRoleInterceptor.class);

  public String intercept(final ActionInvocation actionInvocation) throws Exception {
    log.debug("EnsureRoleInterceptor called for role:" + roleToCheck);
    final Map<String, Object> sessionMap = getUserSessionMap();
    final String userId = (String) sessionMap.get(Constants.SINGLE_SIGNON_USER_KEY);

    if (null == userId) {
      log.debug("No user account found. Make sure the user gets logged in before calling this.");
      return Action.ERROR;
    }

    final PlosOneUser plosUser = (PlosOneUser) sessionMap.get(Constants.PLOS_ONE_USER_KEY);
    if (null != plosUser) {
      final String[] userRoles = userService.getRole(plosUser.getUserId());

      if (ArrayUtils.contains(userRoles, roleToCheck)) {
        log.debug("User found with admin role:" + userId);
        return actionInvocation.invoke();
      } else {
        return Constants.ReturnCode.NOT_SUFFICIENT_ROLE;
      }
    }
    return Action.ERROR;
  }

  /**
   * Get the user session map.
   * @return session map
   */
  protected Map<String, Object> getUserSessionMap() {
    return userService.getUserContext().getSessionMap();
  }

  /**
   * Set the userService
   * @param userService userService
   */
  public void setUserService(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Set the role to check for
   * @param roleToCheck roleToCheck
   */
  public void setRoleToCheck(final String roleToCheck) {
    this.roleToCheck = roleToCheck;
  }

  /**
   * Getter for roleToCheck.
   * @return Value of roleToCheck.
   */
  public String getRoleToCheck() {
    return roleToCheck;
  }

  public void destroy() {}
  public void init() {}
}
