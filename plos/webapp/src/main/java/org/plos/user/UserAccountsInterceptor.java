/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.user;

import java.net.URLEncoder;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.plos.models.UserAccount;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * A webwork interceptor that maps the authenticated user id to an internal plos-user-id. The plos
 * user-id is setup in the HttpSession using the key {@link #USER_KEY}. The original user id is
 * available using the session key {@link #AUTH_KEY}. In addition a wrapper for
 * <code>HttpServletRequest</code> is setup so that the <code>getRemoteUser</code> and
 * <code>getUserPrincipal</code> returns the plos-user.
 *
 * @author Pradeep Krishnan
 */
@SuppressWarnings("serial")
public class UserAccountsInterceptor extends AbstractInterceptor {
  private static Log log = LogFactory.getLog(UserAccountsInterceptor.class);

  /**
   * The session attribute key used to store the plos-user-id in HttpSession.
   */
  public static String USER_KEY = "org.topazproject.user-id";

  /**
   * The session attribute key used to store the user-account-state in HttpSession.
   */
  public static String STATE_KEY = "org.topazproject.account-state";

  /**
   * The session attribute key used to store the authenticated-user-id in HttpSession.
   */
  public static String AUTH_KEY = "org.topazproject.auth-id";

  private Session session;
  private boolean wrap = false;
  
  /**
   * Internal key used for detecting whether or not this interceptor 
   * has already been applied for the targeted action.  
   * This check is necessary when considering action chaining. 
   */
  private static final String REENTRANT_KEY = UserAccountsInterceptor.class.getName() + ".reentrant";
  
  /**
   * Checks for and sets the {@link #REENTRANT_KEY} value for the current {@link ActionContext}
   * and reports on whether or not it was previously set.
   * @param invocation The {@link ActionInvocation}
   * @return <code>true</code> if this interceptor has already been applied for the current {@link ActionContext}.
   */
  private boolean reentrantCheck(ActionInvocation invocation) {
    Object obj = invocation.getInvocationContext().get(REENTRANT_KEY);
    if(obj == null) {
      invocation.getInvocationContext().put(REENTRANT_KEY, true);
    }
    return obj != null;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    if(!reentrantCheck(invocation)) {
      String user = lookupUser(ServletActionContext.getRequest());
      if(wrap) ServletActionContext.setRequest(wrapRequest(ServletActionContext.getRequest(), user));
    }
    return invocation.invoke();
  }

  /**
   * Wraps the HttpServletRequest so that <code>getRemoteUser</code> and
   * <code>getUserPrincipal</code>  will return the mapped topaz-user.
   *
   * @param request the servlet request to wrap
   * @param user the topaz-user
   *
   * @return the wrapped request
   */
  protected HttpServletRequest wrapRequest(HttpServletRequest request, final String user) {
    final Principal principal = new Principal() {
        public String getName() {
          return user;
        }
      };

    return new HttpServletRequestWrapper(request) {
        @Override
        public String getRemoteUser() {
          return user;
        }

        @Override
        public Principal getUserPrincipal() {
          return principal;
        }
      };
  }

  /**
   * Looks up the plos-user for the currently authenticated user and saves it in
   * <code>HttpSession</code>.
   *
   * @throws Exception when an error occurs while obtaining the mapping
   */
  protected String lookupUser(HttpServletRequest request) throws Exception {
    HttpSession session = request.getSession(true);
    String      user    = (String) session.getAttribute(USER_KEY);
    String      authId  = getAuthenticatedUser(request);

    if ((user != null) && (authId == null)) {
      if (log.isDebugEnabled())
        log.debug("Existing user '" + user + "' found in session-id: " + session.getId());
      return user;
    }

    UserAccount ua = lookupUser(authId);

    if (ua != null)
      user = ua.getId().toString();
    else
      user = "anonymous:user/" + ((authId == null) ? "" : URLEncoder.encode(authId, "UTF-8"));

    session.setAttribute(USER_KEY, user);
    session.setAttribute(STATE_KEY, ua != null ? ua.getState() : 0);
    session.setAttribute(AUTH_KEY, authId);

    if (log.isDebugEnabled())
      log.debug("Changed user to '" + user + "' from '" + authId + "' with state '" +
                session.getAttribute(STATE_KEY) + "' and cached in session-id: " + session.getId());

    return user;
  }

  /**
   * Returns the currently authenticated user for a servlet request.
   *
   * @param request the servlet request
   *
   * @return returns the authenticated-id
   */
  protected String getAuthenticatedUser(HttpServletRequest request) {
    String authId = request.getRemoteUser();

    if (authId == null) {
      Principal p = request.getUserPrincipal();

      if (p != null)
        authId = p.getName();
    }

    return authId;
  }

  /**
   * Looks up a plos user.
   *
   * @param authId the authenticated-id
   * @return the user-account, or null if either <var>authId</var> is null or the user
   *         can't be found (happens for newly created accounts)
   * @throws OtmException if the lookup fails
   */
  protected UserAccount lookupUser(final String authId) throws OtmException {
    if (authId == null)
      return null;

    Results r = session.
        createQuery("select ua from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();

    if (!r.next()) {
      if (log.isDebugEnabled())
        log.debug("Failed to look up plos-user with auth-id '" + authId + "'");
      return null;
    }

    return (UserAccount) r.get(0);
  }

  /** 
   * Set the OTM session. Called by spring's bean wiring. 
   * 
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set whether to wrap requests, setting the user to the looked-up user-id. Called by spring's
   * bean wiring.
   *
   * @param wrap true if wrapping is desired
   */
  public void setWrap(boolean wrap) {
    this.wrap = wrap;
  }
}
