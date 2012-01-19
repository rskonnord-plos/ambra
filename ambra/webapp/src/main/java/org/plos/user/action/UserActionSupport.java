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
