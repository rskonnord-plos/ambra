/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.auth.AuthConstants;
import org.plos.auth.service.UserService;
import org.plos.auth.db.DatabaseException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Replaces the Username with the user's GUID so that the username is the GUID for any responses to clients.
 * File to change when hosting
 * D:\java\topaz-install\esup-cas-quick-start-2.0.6-1\jakarta-tomcat-5.0.28\webapps\cas\WEB-INF\web.xml

  <filter>
    <filter-name>UsernameReplacementWithGuidFilter</filter-name>
    <filter-class>org.plos.auth.web.UsernameReplacementWithGuidFilter</filter-class>
  </filter>

  <filter-mapping >
    <filter-name>UsernameReplacementWithGuidFilter</filter-name>
    <url-pattern>/login</url-pattern>
  </filter-mapping>

*/
public class UsernameReplacementWithGuidFilter implements Filter {
  private static final Log log = LogFactory.getLog(UsernameReplacementWithGuidFilter.class);
  private UserService userService;
  public String USERNAME_PARAMETER = "username";

  public void init(final FilterConfig filterConfig) throws ServletException {
    try {
      userService = (UserService) filterConfig.getServletContext().getAttribute(AuthConstants.USER_SERVICE);
    } catch (final Exception ex) {
      log.error("UsernameReplacementWithGuidFilter init failed:", ex);
      throw new ServletException(ex);
    }
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;

      if (log.isDebugEnabled()) {
        dumpRequest(httpRequest, "before chain");
      }

      final String usernameParameter = request.getParameter(USERNAME_PARAMETER);

      if (!((null == usernameParameter) || (usernameParameter.length() == 0))) {
        final String username = usernameParameter.toString();
        httpRequest = new UsernameRequestWrapper(httpRequest, username);
      }

      filterChain.doFilter(httpRequest, response);

    } catch (final IOException ex) {
      log.error("", ex);
      throw ex;
    } catch (final ServletException ex) {
      log.error("", ex);
      throw ex;
    }
  }

  private class UsernameRequestWrapper extends HttpServletRequestWrapper {
    private final String username;

    public UsernameRequestWrapper(final HttpServletRequest httpRequest, final String username) {
      super(httpRequest);
      this.username = username;
    }

    public String getParameter(final String parameterName) {
      if (USERNAME_PARAMETER.equals(parameterName)) {
        final String guid = getUserGuid(username);
        log.debug("guid:" + guid);
        return guid;
      }
      return super.getParameter(parameterName);
    }

    private String getUserGuid(final String username) {
      try {
        return userService.getGuid(username);
      } catch (final DatabaseException e) {
        log.debug("No account found for userId:" + username, e);
        return "";
      }
    }
  }

  private String dumpResponse(final CharResponseWrapper wrapper) throws IOException {
    log.debug("Response ContentType:" + wrapper.getContentType());
    CharArrayWriter caw = new CharArrayWriter();
    caw.write(wrapper.toString());
    final String response = caw.toString();
    log.debug("Response generated:");
    log.debug(response);
    return response;
  }

  private void dumpRequest(final HttpServletRequest request, final String prefix) {
    log.debug(prefix + "----------------" + System.currentTimeMillis());
    log.debug("url:" + request.getRequestURL());
    log.debug("query string:" + request.getQueryString());
    {
      log.debug("Request Attributes:");
      final Enumeration attribs = request.getAttributeNames();
      while (attribs.hasMoreElements()) {
        final String attribName = (String) attribs.nextElement();
        log.debug(attribName + ":" + request.getAttribute(attribName).toString());
      }
    }

    {
      log.debug("Request Parameters:");
      final Enumeration params = request.getParameterNames();
      while (params.hasMoreElements()) {
        final String paramName = (String) params.nextElement();
        log.debug(paramName + ":" + request.getParameterValues(paramName).toString());
      }
    }
  }

  private void dumpSession(final HttpSession initialSession) {
    log.debug("Session Attributes:");
    final Enumeration attribs1 = initialSession.getAttributeNames();
    while (attribs1.hasMoreElements()) {
      final String attribName1 = (String) attribs1.nextElement();
      log.debug(attribName1 + ":" + initialSession.getAttribute(attribName1).toString());
    }
  }

  public void destroy() {
  }
}


class CharResponseWrapper extends HttpServletResponseWrapper {
  private CharArrayWriter output;

  public String toString() {
    return output.toString();
  }

  public CharResponseWrapper(final HttpServletResponse response) {
    super(response);
    output = new CharArrayWriter();
  }

  public PrintWriter getWriter() {
    return new PrintWriter(output);
  }
}
