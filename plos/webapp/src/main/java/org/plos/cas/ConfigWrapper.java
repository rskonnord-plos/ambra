/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.cas;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Wrapper around a FilterConfig 
 */
public class ConfigWrapper implements FilterConfig, ServletConfig {
  private FilterConfig filterConfig;
  private ServletConfig servletConfig;
  private final Map<String, String> params;

  public ConfigWrapper(final FilterConfig filterConfig, final Map<String, String> params) {
    this.filterConfig = filterConfig;
    this.params = params;
  }

  public ConfigWrapper(final ServletConfig servletConfig, final Map<String, String> params) {
    this.servletConfig = servletConfig;
    this.params = params;
  }

  /**
   * @see javax.servlet.FilterConfig#getFilterName()
   */
  public String getFilterName() {
    return filterConfig.getFilterName();
  }

  /**
   * @see javax.servlet.ServletConfig#getServletName()
   */
  public String getServletName() {
    return servletConfig.getServletName();
  }

  public ServletContext getServletContext() {
    return filterConfig.getServletContext();
  }

  /**
   * @see javax.servlet.FilterConfig#getInitParameter(String)
   * @see javax.servlet.ServletConfig#getInitParameter(String)
   */
  public String getInitParameter(final String name) {
    return params.get(name);
  }

  /**
   * @see javax.servlet.FilterConfig#getInitParameterNames()
   * @see javax.servlet.ServletConfig#getInitParameterNames()
   */
  public Enumeration getInitParameterNames() {
    return Collections.enumeration(params.keySet());
  }
}
