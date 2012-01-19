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
package org.topazproject.cas.proxy;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.topazproject.cas.CASDeploymentConfig;

import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;

/**
 * A wrapper for CAS ProxyTicket Receptor that supplies config values from topaz config.
 *
 * @author Pradeep Krishnan
 */
public class ProxyTicketReceptorWrapper extends ProxyTicketReceptor {
  private CASDeploymentConfig config      = null;
  private boolean             initialized = false;

  /*
   * @see javax.servlet.Servlet
   */
  public void init(ServletConfig servletConfig) throws ServletException {
    config = new CASDeploymentConfig(servletConfig);

    // delay calling super.init() till we rcv first request
  }

  /*
   * @see javax.servlet.Servlet
   */
  public ServletConfig getServletConfig() {
    return config;
  }

  /*
   * @see javax.servlet.Servlet
   */
  public void service(ServletRequest request, ServletResponse response)
               throws ServletException, IOException {
    synchronized (config) {
      if (!initialized) {
        // first request; initialize
        config.initialize(request);
        super.init(config);

        // enable normal processing
        initialized = true;
      }
    }

    super.service(request, response);
  }
}
