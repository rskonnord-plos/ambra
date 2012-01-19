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

import java.util.Map;

/**
 * Base class for user actions in order to have a userService object accessible
 * 
 * @author Stephen Cheng
 * 
 */
public class UserActionSupport extends BaseActionSupport {
  private UserService userService;
  private static final Log log = LogFactory.getLog(UserActionSupport.class);
  /**
   * Note: The visibility of this method is default so that the JSON serializer does not get into
   * an infinite circular loop when trying to serialize the action.
   * @return Returns the userService.
   */
  UserService getUserService() {
    return userService;
  }

  /**
   * @param userService
   *          The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected Map<String, Object> getSessionMap() {
    return userService.getUserContext().getSessionMap();
  }
}
