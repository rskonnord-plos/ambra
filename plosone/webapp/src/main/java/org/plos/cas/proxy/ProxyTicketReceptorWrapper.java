/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.cas.proxy;

import edu.yale.its.tp.cas.proxy.ProxyTicketReceptor;
import org.apache.commons.configuration.Configuration;
import org.plos.cas.ConfigWrapper;
import org.plos.cas.ConfigWrapperUtil;
import org.plos.cas.InitParamProvider;
import org.plos.configuration.ConfigurationStore;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper for CAS ProxyTicket Receptor that supplies config values from our own configuration file.
 */
public class ProxyTicketReceptorWrapper extends ProxyTicketReceptor {
  /**
   * @see javax.servlet.Servlet
   */
  public void init(final ServletConfig servletConfig) throws ServletException {
    final Map<String, String> params = new HashMap<String, String>(2);

    final InitParamProvider initParamProvider = new InitParamProvider(){
      public Enumeration getInitParameterNames() {
        return servletConfig.getInitParameterNames();
      }
      public String getInitParameter(final String key) {
        return servletConfig.getInitParameter(key);
      }
    };

    ConfigWrapperUtil.copyInitParams(initParamProvider, params);
    final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

    final String casProxyReceptorUrl = configuration.getString("cas.url.proxy-receptor");

    ConfigWrapperUtil.setInitParamValue(ProxyTicketReceptor.CAS_PROXYURL_INIT_PARAM,
            casProxyReceptorUrl, initParamProvider, params);

    final ServletConfig customServletConfig = new ConfigWrapper(servletConfig, params);
    super.init(customServletConfig);
  }

}

