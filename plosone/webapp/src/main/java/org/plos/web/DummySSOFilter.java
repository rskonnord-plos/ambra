/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.web;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.Constants;

/**
 * A dummy filter for developement to set the sso key. Just add the query parameters "sso.auth.id"
 * and "sso.email" to any page in order to change the login; an empty auth-id is a logout (anonymous
 * user). The setting is cached in the session, i.e. once you've changed your login you don't need
 * the query parameters on subsequent pages.
 */
public class DummySSOFilter implements Filter {
  private static final Log log = LogFactory.getLog(DummySSOFilter.class);

  private String  authId;
  private String  email;
  private boolean wrap = false;

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {
    authId = filterConfig.getInitParameter("auth.id");
    email  = filterConfig.getInitParameter("email");
    wrap   = "true".equalsIgnoreCase(filterConfig.getInitParameter("wrapRequest"));

    log.info(
        "injecting dummy sso data: auth-id='" + authId + "', email='" + email + "', wrap=" + wrap);
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {
  }

  /*
   * @see javax.servlet.Filter#doFilter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc)
                throws ServletException, IOException {
    HttpSession session = ((HttpServletRequest) request).getSession();

    // dynamic change-user
    if (request.getParameter("sso.auth.id") != null) {
      authId = request.getParameter("sso.auth.id");
      email  = request.getParameter("sso.email");

      session.invalidate();
      session = ((HttpServletRequest) request).getSession(true);

      log.info("switched dummy sso data: auth-id='" + authId + "', email='" + email + "'");
    }

    // set up session attributes if not already done
    if (session.getAttribute(Constants.SINGLE_SIGNON_USER_KEY) == null && authId != null &&
        authId.length() > 0) {
      if (log.isDebugEnabled())
        log.debug("setting session attributes, session=" + session.getId());

      session.setAttribute(Constants.SINGLE_SIGNON_USER_KEY, authId);
      session.setAttribute(Constants.SINGLE_SIGNON_EMAIL_KEY, email);
    }

    if (wrap)
      request = wrapRequest((HttpServletRequest) request, authId);

    fc.doFilter(request, response);
  }

  protected HttpServletRequest wrapRequest(HttpServletRequest request, final String user) {
    final Principal principal = new Principal() {
        public String getName() {
          return user;
        }
      };

    return new HttpServletRequestWrapper(request) {
        public String getRemoteUser() {
          return user;
        }

        public Principal getUserPrincipal() {
          return principal;
        }
      };
  }
}
