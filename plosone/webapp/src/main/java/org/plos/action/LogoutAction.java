/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

import org.plos.web.UserContext;
import com.opensymphony.webwork.dispatcher.SessionMap;

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
