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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.user.service.UserService;

import org.springframework.beans.factory.annotation.Required;
import com.googlecode.jsonplugin.annotations.JSON;

import java.util.Map;

/**
 * Base class for user actions in order to have a userService object accessible
 * 
 * @author Stephen Cheng
 * 
 */
public class UserActionSupport extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(UserActionSupport.class);
  private UserService userService;

  /**
   * @return the userService.
   */
  @JSON(serialize = false)
  protected UserService getUserService() {
    return userService;
  }

  /**
   * @param userService
   *          The userService to set.
   */
  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected Map<String, Object> getSessionMap() {
    return userService.getUserContext().getSessionMap();
  }
}
