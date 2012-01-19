/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.web;

import java.util.Collection;

/**
 * The virtual journal context.
 *
 * Various values relating to the virtual journal context and the current Request.
 * Designed to be put into the Request as an attribute for easy access by the web application.
 * In particular, allows templates full consistent access to the same information that is available
 * to the Java Action.
 *
 * Note that templates do not have full access to the Request, so a Request
 * attribute is used to bridge between the two.
 *
 * Application usage:
 * <pre>
 * VirtualJournalContext requestContent = ServletRequest.getAttribute(PUB_VIRTUALJOURNAL_CONTEXT);
 * String requestJournal = requestContext.getJournal();
 * </pre>
 */
public class VirtualJournalContext {

  /** ServletRequest attribute for the virtual journal context. */
  public static final String PUB_VIRTUALJOURNAL_CONTEXT = "pub.virtualjournal.context";

  /** Default virtual journal name. */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL = "";

  /** Default virtual journal description. */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_DESCRIPTION = "";

  /** Default virtual journal mapping prefix. */
  public static final String PUB_VIRTUALJOURNAL_DEFAULT_MAPPINGPREFIX = "";

  private final String journal;
  private final String description;
  private final String mappingPrefix;
  private final String requestScheme;
  private final int    requestPort;
  private final String requestServerName;
  private final String requestContext;
  private final String baseUrl;
  private final String baseHostUrl;
  private final Collection<String> virtualJournals;

  /**
   * Construct an immutable VirtualJournalContext.
   */
  public VirtualJournalContext(final String journal, final String description,
          final String mappingPrefix, final String requestScheme, final int requestPort,
          final String requestServerName, final String requestContext,
          final Collection<String> virtualJournals) {

    this.journal           = journal;
    this.description       = description;
    this.mappingPrefix     = mappingPrefix;
    this.requestScheme     = requestScheme;
    this.requestPort       = requestPort;
    this.requestServerName = requestServerName;
    this.requestContext    = requestContext;

    StringBuilder urlBaseValue = new StringBuilder();

    // assume that we're dealing with http or https schemes for now
    // TODO: understand how scheme can be null in request? for now, guard against null
    if (requestScheme != null) {
      urlBaseValue.append(requestScheme).append("://").append(requestServerName);

      //assume that we don't want to put the default ports numbers on the URL
      if (("http".equals(requestScheme.toLowerCase()) && requestPort != 80) ||
          ("https".equals(requestScheme.toLowerCase()) && requestPort != 443)) {
        urlBaseValue.append(":").append(requestPort);
      }
    }
    this.baseHostUrl = urlBaseValue.toString();
    urlBaseValue.append(requestContext);
    this.baseUrl = urlBaseValue.toString();

    this.virtualJournals = virtualJournals;
  }

  /**
   * Virtualize URI values.
   *
   * If URI is already prefixed with the mappingPrefix, it is already virtualized.<br/>
   * If URI is prefixed with the virtual journal name, replace with mappingPrefix to
   *   virtualize.<br/>
   * Else, prefix with mappingPrefix to virtualize.
   *
   * @return String[] of mapped {contextPath, servletPath, pathInfo, requestUri}
   */
  public String[] virtualizeUri(
    final String contextPath, final String servletPath, final String pathInfo) {

    // will need to examine, and possibly modify, Request Path Elements
    String virtualContextPath = contextPath;
    String virtualServletPath = servletPath;
    String virtualPathInfo    = pathInfo;

    // in tests below, be flexible with servletPath and pathInfo, not all containers adhere tightly
    // to Servlet SRV.4.4.  e.g. prefix could be in servletPath or pathInfo.

    // does URI already contain mappingPrefix?
    if ((virtualServletPath != null && virtualServletPath.startsWith(mappingPrefix))
      || (virtualPathInfo != null && virtualPathInfo.startsWith(mappingPrefix))) {
      return new String [] {virtualContextPath, virtualServletPath, virtualPathInfo,
        virtualContextPath + virtualServletPath + (virtualPathInfo != null ? virtualPathInfo : "")};
    }

    // does URI contain journal name?
    if (virtualServletPath != null && virtualServletPath.startsWith("/" + journal)) {
      virtualServletPath = mappingPrefix + virtualServletPath.substring(journal.length() + 1);
      return new String [] {virtualContextPath, virtualServletPath, virtualPathInfo,
        virtualContextPath + virtualServletPath + (virtualPathInfo != null ? virtualPathInfo : "")};
    }
    if (virtualPathInfo != null && virtualPathInfo.startsWith("/" + journal)) {
      virtualPathInfo = mappingPrefix + virtualPathInfo.substring(journal.length() + 1);
      return new String [] {virtualContextPath, virtualServletPath, virtualPathInfo,
        virtualContextPath + virtualServletPath + virtualPathInfo};
    }

    // need to add mappingPrefix to URI
    if (virtualServletPath != null && virtualServletPath.length() > 0) {
      virtualServletPath = mappingPrefix + virtualServletPath;
    } else {
      virtualPathInfo = mappingPrefix + (virtualPathInfo != null ? virtualPathInfo : "");
    }

      return new String [] {virtualContextPath, virtualServletPath, virtualPathInfo,
        virtualContextPath + virtualServletPath + (virtualPathInfo != null ? virtualPathInfo : "")};
   }

  /**
   * Default URI values.
   *
   * If URI is already prefixed with the mappingPrefix, remove it.<br/>
   * If URI is prefixed with the virtual journal name, remove it.<br/>
   * Else, already default values.
   *
   * @return String[] of defaulted {contextPath, servletPath, pathInfo, requestUri}
   */
  public String[] defaultUri(final String contextPath, final String servletPath, final String pathInfo) {

    // will need to examine, and possibly modify, Request Path Elements
    String defaultContextPath = contextPath;
    String defaultServletPath = servletPath;
    String defaultPathInfo    = pathInfo;

    // in tests below, be flexible with servletPath and pathInfo, not all containers adhere tightly
    // to Servlet SRV.4.4.  e.g. prefix could be in servletPath or pathInfo.

    // does URI already contain mappingPrefix?
    if (defaultServletPath != null && defaultServletPath.startsWith(mappingPrefix)) {
      defaultServletPath = defaultServletPath.substring(mappingPrefix.length());
      return new String [] {defaultContextPath, defaultServletPath, defaultPathInfo,
        defaultContextPath + defaultServletPath + (defaultPathInfo != null ? defaultPathInfo : "")};
    }
    if (defaultPathInfo != null && defaultPathInfo.startsWith(mappingPrefix)) {
      defaultPathInfo = defaultPathInfo.substring(mappingPrefix.length());
      return new String [] {defaultContextPath, defaultServletPath, defaultPathInfo,
        defaultContextPath + defaultServletPath + (defaultPathInfo != null ? defaultPathInfo : "")};
    }

    // does URI contain journal name?
    if (defaultServletPath != null && defaultServletPath.startsWith("/" + journal)) {
      defaultServletPath = defaultServletPath.substring(journal.length() + 1);
      return new String [] {defaultContextPath, defaultServletPath, defaultPathInfo,
        defaultContextPath + defaultServletPath + (defaultPathInfo != null ? defaultPathInfo : "")};
    }
    if (defaultPathInfo != null && defaultPathInfo.startsWith("/" + journal)) {
      defaultPathInfo = defaultPathInfo.substring(journal.length() + 1);
      return new String [] {defaultContextPath, defaultServletPath, defaultPathInfo,
        defaultContextPath + defaultServletPath + (defaultPathInfo != null ? defaultPathInfo : "")};
    }

    // already defaulted
    return new String [] {defaultContextPath, defaultServletPath, defaultPathInfo,
      defaultContextPath + defaultServletPath + (defaultPathInfo != null ? defaultPathInfo : "")};
   }

  /**
   * Get the virtual journal name.
   *
   * @return Journal name, may be <code>null</code>.
   */
  public String getJournal() {
    return journal;
  }

  /**
   * Get the virtual journal description.
   *
   * @return Journal description, may be <code>null</code>.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the virtual journal mapping prefix.
   *
   * @return Mapping prefix, may be "".
   */
  public String getMappingPrefix() {
    return mappingPrefix;
  }

  /**
   * Get the virtual journal Request scheme.
   *
   * @return Request scheme.
   */
  public String getRequestScheme() {
    return requestScheme;
  }

  /**
   * Get the virtual journal Request port.
   *
   * @return Request port.
   */
  public int getRequestPort() {
    return requestPort;
  }

  /**
   * Get the virtual journal Request server name.
   *
   * @return Request server name.
   */
  public String getRequestServerName() {
    return requestServerName;
  }

  /**
   * Get the virtual journal Request context.
   *
   * @return Request context.
   */
  public String getRequestContext() {
    return requestContext;
  }

  /**
   * Get the base url of the request which consists of the scheme, server name, server port, and
   * context.
   *
   * @return string representing the base request URL
   */
  public String getBaseUrl () {
    return baseUrl;
  }

  /**
   * Get the base host url of the request which consists of the scheme, server name, and server port
   *
   * @return string representing the base host URL
   */
  public String getBaseHostUrl () {
    return baseHostUrl;
  }

  /**
   * Get the virtual journals that are available.
   *
   * @return Available virtual journals.
   */
  public Collection<String> getVirtualJournals() {
    return virtualJournals;
  }
}
