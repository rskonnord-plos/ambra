/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
/*  Copyright (c) 2000-2004 Yale University. All rights reserved.
 *  See full notice at end.
 */
package org.topazproject.cas;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;

import org.topazproject.configuration.ConfigurationStore;

/**
 * Deployment configuration for CAS-Filter and ProxyCallBack-Servlet. Provides values from topaz
 * config. Also the context path in urls are set based on the context path found in the first
 * request that the filter or servlet rcvs.
 *
 * @author Pradeep Krishnan
 */
public class CASDeploymentConfig implements FilterConfig, ServletConfig {
  private String         name;
  private ServletContext context;
  private Map            params = new HashMap();

  /**
   * Creates a new CASDeploymentConfig object.
   *
   * @param conf the original filter config
   */
  public CASDeploymentConfig(FilterConfig conf) {
    name      = conf.getFilterName();
    context   = conf.getServletContext();

    Enumeration en = conf.getInitParameterNames();

    while (en.hasMoreElements()) {
      String key = (String) en.nextElement();
      params.put(key, conf.getInitParameter(key));
    }
  }

  /**
   * Creates a new CASDeploymentConfig object.
   *
   * @param conf the original servlet config
   */
  public CASDeploymentConfig(ServletConfig conf) {
    name      = conf.getServletName();
    context   = conf.getServletContext();

    Enumeration en = conf.getInitParameterNames();

    while (en.hasMoreElements()) {
      String key = (String) en.nextElement();
      params.put(key, conf.getInitParameter(key));
    }
  }

  /**
   * Initialize the configuration.
   *
   * @param request the first servlet request rcvd by our wrapper
   *
   * @throws ServletException on an error
   */
  public void initialize(ServletRequest request) throws ServletException {
    Configuration conf       = ConfigurationStore.getInstance().getConfiguration();
    String        serverName = conf.getString("topaz.server.hostname", "localhost");
    String        casBaseUrl =
      conf.getString("topaz.cas-server.base-url", "https://localhost:7443/cas");

    String httpsPort;
    int    port = conf.getInt("topaz.server.https-port", 8443);

    if ((port == 0) || (port == 443))
      httpsPort = "";
    else
      httpsPort = ":" + port;

    if (!(request instanceof HttpServletRequest))
      throw new ServletException("Sorry; can handle only HttpServletRequest.");

    HttpServletRequest req         = (HttpServletRequest) request;
    String             contextPath = req.getContextPath();

    String             serverPort;
    port = req.getServerPort(); // xxx: assumes http request

    if ((port == 0) || (port == 80))
      serverPort = "";
    else
      serverPort = ":" + port;

    params.put("edu.yale.its.tp.cas.client.filter.validateUrl", casBaseUrl + "/proxyValidate");
    params.put("edu.yale.its.tp.cas.client.filter.serverName", serverName + serverPort);
    params.put("edu.yale.its.tp.cas.client.filter.proxyCallbackUrl",
               "https://" + serverName + httpsPort + contextPath + "/CasProxyServlet");
    params.put("edu.yale.its.tp.cas.proxyUrl", casBaseUrl + "/proxy");
  }

  /*
   * @see javax.servlet.FilterConfig
   */
  public String getFilterName() {
    return name;
  }

  /*
   * @see javax.servlet.ServletConfig
   */
  public String getServletName() {
    return name;
  }

  /*
   * @see javax.servlet.FilterConfig
   * @see javax.servlet.ServletConfig
   */
  public String getInitParameter(String name) {
    return (String) params.get(name);
  }

  /*
   * @see javax.servlet.FilterConfig
   * @see javax.servlet.ServletConfig
   */
  public Enumeration getInitParameterNames() {
    return Collections.enumeration(params.keySet());
  }

  /*
   * @see javax.servlet.FilterConfig
   * @see javax.servlet.ServletConfig
   */
  public ServletContext getServletContext() {
    return context;
  }
}
