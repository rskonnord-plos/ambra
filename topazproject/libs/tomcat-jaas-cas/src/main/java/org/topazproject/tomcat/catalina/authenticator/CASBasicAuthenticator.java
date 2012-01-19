/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.tomcat.catalina.authenticator;

import java.io.IOException;
import java.io.InputStream;

import java.net.URLEncoder;

import java.security.Principal;

import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.deploy.LoginConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An authenticator and Valve implementation of 'CAS-BASIC' Authentication.
 * 
 * <p>
 * This Authenticator is inserted into the request processing pipe-line for a web-app when  the
 * &lt;auth-method&gt; element of &lt;login-config&gt; in the WEB-INF/web.xml file is  configured
 * with the value <code>CAS-BASIC</code>.
 * </p>
 * 
 * <p>
 * In addition the file <code>org/apache/catalina/startup/Authenticators.properties</code> inside
 * tomcats <code>server/lib/catalina.jar</code> should map <code>CAS-BASIC</code>  to this class
 * name so that tomcats standard context configuration class
 * (<code>org.apache.catalina.startup.ContextConfig</code>)  will load and initialize  this class
 * (and ofcourse all jar files needed must be present in the tomcat classpath).
 * </p>
 * 
 * <p>
 * Also note that as with all catalina Authenticators, authentication is delegated to the
 * <code>Realm</code> configured for the web-app in its deployment descriptor. The
 * <code>Realm</code> should expect either a username/password pair from RFC 2617 BASIC
 * authentication header or a <code>ticket/service</code> pair where <code>ticket</code> is the
 * value of the CAS single signon request parameter named <code>ticket</code> and
 * <code>service</code> is the URL of the request being authenticated.
 * </p>
 * 
 * <p>
 * The above requirement on <code>Realm</code> means, that the <code>Realm</code> has to be  a
 * <code>JAASRealm</code> or should provide capabilities similar to JAAS. If using JAAS,  a
 * configuration example is:
 * <pre>
 * Login {
 *  org.my.local.auth.LoginModule optional
 *  org.my.cas.auth.CASLoginModule optional
 *         cas_validate_url="https://localhost:7443/cas/proxyValidate"
 *  org.my.roles.LookupRolesLoginModule optional
 *  };
 * </pre>
 * See <code>javax.security.auth.login.Configuration</code> for details.
 * </p>
 * 
 * <p>
 * On authentication failure, this does the following:
 * </p>
 * 
 * <p>
 * 
 * <ul>
 * <li>
 * If <code>CAS.properties</code> file exists in this classpath and a CAS login URL is configured
 * using the <code>loginUrl</code> property, then a redirect is done to the  CAS login.
 * </li>
 * <li>
 * Otherwise issues a BASIC authentication challenge to  the client; in keeping with its primary
 * behavior as a BASIC authenticator.
 * </li>
 * </ul>
 * </p>
 *
 * @author Pradeep Krishnan (pradeep&#64;topazproject.org)
 */
public class CASBasicAuthenticator extends BasicAuthenticator {
  private static Log log        = LogFactory.getLog(CASBasicAuthenticator.class);
  private String     casLogin   = null;
  private boolean    casRenew   = false;
  private boolean    casGateway = false;

  /**
   * Load our properties at startup.
   *
   * @throws LifecycleException if this authenticator cannot be started.
   */
  public void start() throws LifecycleException {
    super.start();

    Properties props  = new Properties();
    boolean    loaded = false;

    try {
      InputStream is = getClass().getClassLoader().getResourceAsStream("CAS.properties");

      if (is != null) {
        props.load(is);
        loaded = true;
      }
    } catch (IOException t) {
      LifecycleException e = new LifecycleException(t.getMessage());
      e.initCause(t);
      throw e;
    }

    if (!loaded)
      log.warn("Failed to load CAS.properties. Redirects to CAS login will be disabled.");

    casLogin = props.getProperty("loginUrl");

    // to do: load other properties
  }

  /**
   * Authenticate the user making this request, based on the specified login configuration in
   * WEB-INF/web.xml.
   *
   * @param request Request we are processing
   * @param response Response we are creating
   * @param config Login configuration describing how authentication should be performed
   *
   * @return Return <code>true</code> if any specified constraint has been satisfied, or
   *         <code>false</code> if we have created a response challenge already.
   *
   * @exception IOException if an input/output error occurs
   */
  public boolean authenticate(HttpRequest request, HttpResponse response, LoginConfig config)
                       throws IOException {
    if (log.isDebugEnabled())
      log.debug("authenticating...");

    // If BASIC auth attempt, then let the super class deal with it
    if (request.getAuthorization() != null) {
      if (log.isDebugEnabled())
        log.debug("Request has an authorization header. Delegating to super class.");

      return super.authenticate(request, response, config);
    }

    // Check for Catalina's SSO implementation
    if (authenticateCatalinaSSOUser(request)) {
      if (log.isDebugEnabled())
        log.debug("Already authenticated.");

      return true;
    }

    if (log.isDebugEnabled())
      log.debug("Looking for CAS ticket...");

    // attempt to authenticate using CAS ticket
    HttpServletRequest hreq   = (HttpServletRequest) request.getRequest();
    String             ticket = hreq.getParameter("ticket");

    if (ticket != null) {
      ticket = ticket.trim();

      if ("".equals(ticket))
        ticket = null;
    }

    if (ticket == null) {
      if (log.isDebugEnabled())
        log.debug("No CAS ticket found. Not attempting a CAS Validate.");
    } else {
      if (log.isDebugEnabled())
        log.debug("Found a CAS ticket. Attempting CAS Validate.");

      String service;

      try {
        service = getService(hreq);
      } catch (ServletException e) {
        IOException ie = new IOException("huh?");
        ie.initCause(e);
        throw ie;
      }

      if (log.isDebugEnabled()) {
        log.debug("ticket : " + ticket);
        log.debug("service: " + service);
      }

      Principal principal = context.getRealm().authenticate(ticket, service);

      if (principal != null) {
        // Force create a session for proxies
        if (ticket.startsWith("PT"))
          createSession(request, ticket);

        register(request, response, principal, Constants.BASIC_METHOD, principal.getName(), null);

        if (log.isDebugEnabled())
          log.debug("CAS validate success.");

        return true;
      }

      if (log.isDebugEnabled())
        log.debug("CAS validate failed. ticket: " + ticket + " service: " + service);
    }

    // Redirect to cas login only for direct user-agent access. Not for proxies. 
    if ((casLogin != null) && ((ticket == null) || !ticket.startsWith("PT"))) {
      try {
        redirectToCAS(hreq, (HttpServletResponse) response.getResponse());
      } catch (ServletException e) {
        IOException ie = new IOException("redirect to CAS login failed.");
        ie.initCause(e);
        throw ie;
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Login failed. Sending a BASIC challenge response.");

      // Send an "unauthorized" response and an appropriate challenge
      String realmName = config.getRealmName();

      if (realmName == null)
        realmName = hreq.getServerName() + ":" + hreq.getServerPort();

      HttpServletResponse hres = (HttpServletResponse) response.getResponse();
      hres.setHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
      hres.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    return false;
  }

  private boolean authenticateCatalinaSSOUser(HttpRequest request) {
    // Have we already authenticated someone?
    Principal principal = ((HttpServletRequest) request.getRequest()).getUserPrincipal();
    String    ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);

    if (principal != null) {
      if (log.isDebugEnabled())
        log.debug("Already authenticated '" + principal.getName() + "'");

      // Associate the session with any existing SSO session
      if (ssoId != null)
        associate(ssoId, getSession(request, true));

      return (true);
    }

    // Is there an SSO session against which we can try to reauthenticate?
    if (ssoId != null) {
      if (log.isDebugEnabled())
        log.debug("SSO Id " + ssoId + " set; attempting " + "reauthentication");

      /* Try to reauthenticate using data cached by SSO.  If this fails,
         either the original SSO logon was of DIGEST or SSL (which
         we can't reauthenticate ourselves because there is no
         cached username and password), or the realm denied
         the user's reauthentication for some reason.
         In either case we have to prompt the user for a logon */
      if (reauthenticateFromSSO(ssoId, request))
        return true;
    }

    return false;
  }

  /**
   * Copied from CAS client Util  - don't want to do the URL encoding.
   *
   * @param request The servlet request to convert into a service url.
   *
   * @return The request URL with the query string suitable for use as a service url.
   *
   * @throws ServletException if access to request components fail (ie. never should happen)
   */
  private String getService(HttpServletRequest request)
                     throws ServletException {
    StringBuffer sb = request.getRequestURL();

    if (request.getQueryString() != null) {
      // first, see whether we've got a 'ticket' at all
      int ticketLoc = request.getQueryString().indexOf("ticket=");

      // if ticketLoc == 0, then it's the only parameter and we ignore
      // the whole query string
      // if no ticket is present, we use the query string wholesale
      if (ticketLoc == -1)
        sb.append("?" + request.getQueryString());
      else if (ticketLoc > 0) {
        ticketLoc = request.getQueryString().indexOf("&ticket=");

        if (ticketLoc == -1) {
          // there was a 'ticket=' unrelated to a parameter named 'ticket'
          sb.append("?" + request.getQueryString());
        } else if (ticketLoc > 0) {
          // otherwise, we use the query string up to "&ticket="
          sb.append("?" + request.getQueryString().substring(0, ticketLoc));
        }
      }
    }

    return sb.toString();
  }

  /**
   * Redirects the user to CAS, determining the service from the request.
   *
   * @param request The request
   * @param response The response
   *
   * @throws IOException on redirect failure
   * @throws ServletException on redirect failure
   */
  private void redirectToCAS(HttpServletRequest request, HttpServletResponse response)
                      throws IOException, ServletException {
    if (log.isTraceEnabled()) {
      log.trace("entering redirectToCAS()");
    }

    String casLoginString =
      casLogin + "?service=" + URLEncoder.encode(getService((HttpServletRequest) request), "UTF-8")
      + ((casRenew) ? "&renew=true" : "") + (casGateway ? "&gateway=true" : "");

    if (log.isDebugEnabled()) {
      log.debug("Redirecting browser to [" + casLoginString + "]");
    }

    ((HttpServletResponse) response).sendRedirect(casLoginString);

    if (log.isTraceEnabled()) {
      log.trace("returning from redirectToCAS()");
    }
  }

  private Session createSession(HttpRequest request, String ticket) {
    Session s = getSession(request, true);

    if (log.isDebugEnabled()) {
      if (s == null)
        log.debug("Failed to create a session for : " + ticket);
      else
        log.debug("Created a session for ticket: " + ticket);
    }

    return s;
  }
}
