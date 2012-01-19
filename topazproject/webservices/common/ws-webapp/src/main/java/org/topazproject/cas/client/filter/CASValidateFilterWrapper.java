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
package org.topazproject.cas.client.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.topazproject.cas.CASDeploymentConfig;

import edu.yale.its.tp.cas.client.CASAuthenticationException;
import edu.yale.its.tp.cas.client.filter.CASValidateFilter;

/**
 * A wrapper for the CASValidateFilter to return an HttpStatus code other than 500 on
 * CASAuthenticationException. (Currently it returns error 444)
 * 
 * <p>
 * Added one more hack to the configuration to make it easy to deploy. Read all deployment specific
 * stuff from config file.
 * </p>
 *
 * @author Pradeep Krishnan
 */
public class CASValidateFilterWrapper extends CASValidateFilter {
  private CASDeploymentConfig config      = null;
  private boolean             initialized = false;

  /*
   * @see javax.servlet.Filter
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    config = new CASDeploymentConfig(filterConfig);

    // delay calling super.init() till we rcv first request
  }

  /*
   * @see javax.servlet.Filter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc)
                throws ServletException, IOException {
    try {
      synchronized (config) {
        if (!initialized) {
          // first request; initialize
          config.initialize(request);
          super.init(config);

          // enable normal processing
          initialized = true;
        }
      }

      super.doFilter(request, response, fc);
    } catch (ServletException e) {
      Throwable cause = e.getRootCause();

      if (!(cause instanceof CASAuthenticationException))
        throw e;

      // not logging anything here since CASValidateFilter logs this exception
      ((HttpServletResponse) response).sendError(444, "Invalid CAS Ticket");

      // suspend further filter chain processing
      return;
    }
  }
}
