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

import edu.yale.its.tp.cas.client.CASReceipt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.Constants;

import org.plos.user.UserAccountsInterceptor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

/**
 * Create a new session when the user logs in using CAS or other authentication mechanisms
 */
public class InitializeSessionOnLoginFilter implements Filter {
  private static final Log log = LogFactory.getLog(InitializeSessionOnLoginFilter.class);
  private static final String PGT_IOU_KEY = "org.plos.web.InitilalizeSessionOnLoginFilter.PGT_IOU_KEY";

  private static final Set<String> excludedNames = new HashSet();

  static {
    excludedNames.add(UserAccountsInterceptor.USER_KEY);
    excludedNames.add(UserAccountsInterceptor.AUTH_KEY);
    excludedNames.add(UserAccountsInterceptor.STATE_KEY);
    excludedNames.add(UserAccountsInterceptor.MAP_STATUS);
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
    try {
      final HttpServletRequest servletRequest = (HttpServletRequest) request;
      final HttpSession initialSession = servletRequest.getSession();
      final CASReceipt casReceipt = (CASReceipt) initialSession.getAttribute(Constants.SINGLE_SIGNON_RECEIPT);
      final String sessionPgtIOU = (String)initialSession.getAttribute(PGT_IOU_KEY);

      if (log.isDebugEnabled()){
        log.debug("Session is hashCode: " + initialSession.hashCode());
        log.debug("Session is id: " + initialSession.getId());
        log.debug("Session is String: " + initialSession);
        log.debug("Original Session PGT_IOU_KEY: " + sessionPgtIOU);
        log.debug("CAS Receipt is: " + casReceipt);
      }


//    if member session was already recreated and I haven't received a new PGT_IOU

      // if user wasn't logged in before and he still isn't logged in  ||
      // if the user was logged in before and he is still logged in with the same PGT_IOU
      if (((null == casReceipt) && (null == sessionPgtIOU)) ||
          ((null != sessionPgtIOU) && (null != casReceipt) &&
           (sessionPgtIOU.equals(casReceipt.getPgtIou())))){
        filterChain.doFilter(request, response);
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Initial attributes in session were:");
          final Enumeration attribs = initialSession.getAttributeNames();
          while (attribs.hasMoreElements()) {
            final String attribName = (String) attribs.nextElement();
            log.debug(attribName + ":" + initialSession.getAttribute(attribName).toString());
          }
        }
        final HttpSession session = reInitializeSessionForMemberLogin(initialSession, servletRequest);

        if (casReceipt != null) {
          session.setAttribute(PGT_IOU_KEY, casReceipt.getPgtIou());
          if (log.isDebugEnabled()) {
            log.debug("new login from CAS with PGT_IOU: " + casReceipt.getPgtIou());
          }
        } else {
          session.removeAttribute(PGT_IOU_KEY);
        }

        filterChain.doFilter(request, response);
       }
    } catch (IOException io) {
      log.error("IOException raised in Filter1 Filter", io);
    } catch (ServletException se) {
      log.error("ServletException raised in Filter1 Filter", se);
    }
  }

  private HttpSession reInitializeSessionForMemberLogin(final HttpSession initialSession,
                                                        final HttpServletRequest request) {
    final Enumeration attributeNames = initialSession.getAttributeNames();
    int numObjsKept = 0;
    HashMap<String, Object> objectsToTransfer = new HashMap<String, Object>();
    String attributeName;
    Object attribute;
    while (attributeNames.hasMoreElements()) {
      attributeName = (String) attributeNames.nextElement();
      if (excludedNames.contains(attributeName))
        continue;
      attribute = initialSession.getAttribute(attributeName);

      if (!attributeMatchesPlosSessionObjects(attribute)) {
        objectsToTransfer.put(attributeName, attribute);
        numObjsKept ++;
        if (log.isDebugEnabled()){
          log.debug("Keeping object: " + attributeName + "  " +attribute);
        }
      }
    }

    if ((numObjsKept!= 0) && (log.isDebugEnabled())){
      log.debug("numberOfSessionObjects Kept was " + numObjsKept);
    }

    initialSession.invalidate();
    final HttpSession newSession = request.getSession(true);
    log.debug("new session id: " + newSession.getId());
    Set<Map.Entry<String, Object>> allObjs = objectsToTransfer.entrySet();
    Iterator<Map.Entry<String, Object>> iter = allObjs.iterator();
    Map.Entry<String, Object> attrib;
    while (iter.hasNext()) {
      attrib = iter.next();
      newSession.setAttribute(attrib.getKey(), attrib.getValue());
    }
    return newSession;
  }

  private boolean attributeMatchesPlosSessionObjects(final Object attribute) {
    final String className = attribute.getClass().getName();
    return className.startsWith(Constants.ROOT_PACKAGE);
  }

  public void init(final FilterConfig filterConfig) throws ServletException {}
  public void destroy() {}
}
