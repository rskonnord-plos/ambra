/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.web;

import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;

import java.util.Map;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides a way to get a handle on the session objects when running in a web application
 */
public class UserContext {
  private Map sessionMap;
  private static final Log log = LogFactory.getLog(UserContext.class);

  public UserContext() {
    if (log.isDebugEnabled()){
      log.debug("UserContext constructed with hashcode" + this.hashCode());
    }
  }

  /**
   * @return the session variables for the user session in a map
   */
  public Map getSessionMap() {
//    if (null == sessionMap) {
      sessionMap = ActionContext.getContext().getSession();
      //    }
    return sessionMap;
  }

  /**
   * @return the session variables for the user session in a map
   */
  public HttpSession getHttpSession() {
    if (log.isDebugEnabled()){
      HttpSession initialSession = ServletActionContext.getRequest().getSession();
      log.debug("UserContext getting HttpSession of classname" +
          ServletActionContext.getRequest().getSession().getClass().getName());
      log.debug("UserContext hashcode: " + this.hashCode());
      log.debug("Session is hashCode: " + initialSession.hashCode());
      log.debug("Session is id: " + initialSession.getId());
      log.debug("Session is String: " + initialSession);
    }

    return ServletActionContext.getRequest().getSession();
  }
}
