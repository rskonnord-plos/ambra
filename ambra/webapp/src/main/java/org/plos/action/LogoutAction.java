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

package org.plos.action;

import org.plos.web.UserContext;
import org.apache.struts2.dispatcher.SessionMap;

/**
 * Action that kills the current user session to log the user out.  The CAS logout call
 * redirects to this action to clean up the application logout after the signon logout.
 * 
 * @author Stephen Cheng
 *
 */
public class LogoutAction extends BaseActionSupport {
  private String goTo;
  private UserContext userContext;

  /**
   * Invalidates sesssion and returns SUCCESS if successful, else returns ERROR
   * 
   */
  public String execute() throws Exception {
    if (userContext != null) {
      SessionMap session = (SessionMap)userContext.getSessionMap();
      if (session != null) {
        session.invalidate();
        return SUCCESS;
      }
    }
    return ERROR;
  }

  /**
   * @return Returns the goTo.
   */
  public String getGoTo() {
    return goTo;
  }

  /**
   * @param goTo The goTo to set.
   */
  public void setGoTo(String goTo) {
    this.goTo = goTo;
  }

  /**
   * @return Returns the userContext.
   */
  public UserContext getUserContext() {
    return userContext;
  }

  /**
   * @param userContext The userContext to set.
   */
  public void setUserContext(UserContext userContext) {
    this.userContext = userContext;
  }
}
