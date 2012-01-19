/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.user.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.action.BaseSessionAwareActionSupport;
import org.topazproject.ambra.user.service.UserService;

import com.googlecode.jsonplugin.annotations.JSON;

/**
 * Base class for user actions in order to have a userService object accessible
 * 
 * @author Stephen Cheng
 * 
 */
public class UserActionSupport extends BaseSessionAwareActionSupport {
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
}
