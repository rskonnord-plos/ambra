/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.users.filter;

import java.io.IOException;

import java.net.URLEncoder;

import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.authentication.ProtectedServiceFactory;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.users.UserAccountLookup;
import org.topazproject.ws.users.impl.UserAccountsImpl;

/**
 * A servlet filter that maps the authenticated user id to an internal topaz-user-id. The topaz
 * user-id is setup in the HttpSession using the key {@link #USER_KEY}. The original user id is
 * available using the session key {@link #AUTH_KEY}. In addition a wrapper for
 * <code>HttpServletRequest</code> is setup so that the <code>getRemoteUser</code> and
 * <code>getUserPrincipal</code> returns the topaz-user.
 *
 * @author Pradeep Krishnan
 */
public class UserAccountsFilter implements Filter {
  private static Log log = LogFactory.getLog(UserAccountsFilter.class);

  /**
   * The session attribute key used to store the topaz-user-id in HttpSession.
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

  /**
   * The session attribute key used to store the state of mapping (mainly to prevent look-ups on
   * every request for <code>null</code> user-ids)
   */
  public static String MAP_STATUS = UserAccountsFilter.class.getName() + ".map-status";

  /**
   * The config key for itql service configuration
   */
  protected static String ITQL_CONF_KEY = "topaz.services.itql";

  private boolean wrap = true;

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(FilterConfig config) {
    if ("false".equals(config.getInitParameter("wrapRequest")))
      wrap = false;
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
    if (request instanceof HttpServletRequest) {
      String user = lookupUser((HttpServletRequest) request);
      if (wrap)
        request = wrapRequest((HttpServletRequest) request, user);
    }

    fc.doFilter(request, response);
  }

  /**
   * Looks up the topaz-user for the currently authenticated user and caches it in
   * <code>HttpSession</code>.
   *
   * @param request The servlet request
   *
   * @return the topaz user id (may be <code>null</code>)
   *
   * @throws ServletException when an error occurs while obtaining the mapping
   */
  protected String lookupUser(HttpServletRequest request)
                       throws ServletException {
    HttpSession session = request.getSession(true);
    String      user = (String) session.getAttribute(USER_KEY);

    if ((user != null) || (session.getAttribute(MAP_STATUS) != null)) {
      if (log.isDebugEnabled())
        log.debug("Changed user to '" + user + "' using value found in session-id: "
                  + session.getId());

      return user;
    }

    String authId = getAuthenticatedUser(request);

    int[]  state = new int[1];
    user = lookupUser(authId, session, state);

    // xxx: move to lookup
    if (user == null)
      user = "anonymous:user/" + ((authId == null) ? "" : URLEncoder.encode(authId));

    session.setAttribute(USER_KEY, user);
    session.setAttribute(STATE_KEY, new Integer(state[0]));
    session.setAttribute(AUTH_KEY, authId);

    // prevent further lookups in this session
    session.setAttribute(MAP_STATUS, "mapped");

    if (log.isDebugEnabled())
      log.debug("Changed user to '" + user + "' from '" + authId + "' with state '" + state[0]
                + "' and cached in session-id: " + session.getId());

    return user;
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

    return new HttpServletRequestWrapper((HttpServletRequest) request) {
        public String getRemoteUser() {
          return user;
        }

        public Principal getUserPrincipal() {
          return principal;
        }
      };
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
   * Looks up a topaz user in a {@link org.topazproject.ws.users.UserAccountLookup
   * UserAccountLookup} impl.
   *
   * @param authId the authenticated-id
   * @param session the http session
   * @param state an array of at least 1 element; on output the 0'th element will contain the state
   *        of the user account, or unchanged if user  account was found
   *
   * @return the topaz-user-id
   *
   * @throws ServletException if the lookup fails
   */
  protected String lookupUser(String authId, HttpSession session, int[] state)
                       throws ServletException {
    try {
      UserAccountLookup ual  = getUserAccountLookupImpl(session);
      String            user = ual.lookUpUserByAuthIdNoAC(authId);

      if (user != null)
        state[0] = ual.getStateNoAC(user);

      return user;
    } catch (Exception e) {
      log.warn("Failed to look-up topaz-user-id for '" + authId + "'", e);
      throw new ServletException("Failed to look-up topaz-user-id for '" + authId + "'", e);
    }
  }

  /**
   * Gets an instance of <code>UserAccountLookup</code>
   *
   * @param session the http session
   *
   * @return the instance
   *
   * @throws Exception if there is an error in creating the instance
   */
  protected UserAccountLookup getUserAccountLookupImpl(HttpSession session)
                                                throws Exception {
    return new UserAccountsImpl(getItqlHelper(session));
  }

  /**
   * Gets an instance of kowari itql client for setting up the <code>UserAccountLookup</code>
   *
   * @param session the http session
   *
   * @return returns the <code>ItqlHelper</code> instance
   *
   * @throws Exception if there is an error in creating the instance
   */
  protected ItqlHelper getItqlHelper(HttpSession session)
                              throws Exception {
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    conf = conf.subset(ITQL_CONF_KEY);

    ProtectedService itqlService = ProtectedServiceFactory.createService(conf, session);

    return new ItqlHelper(itqlService);
  }
}
