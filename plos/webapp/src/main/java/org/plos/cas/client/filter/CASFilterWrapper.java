/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.cas.client.filter;

//import edu.yale.its.tp.cas.client.filter.CASFilter;
import org.apache.commons.configuration.Configuration;
import org.plos.cas.ConfigWrapper;
import org.plos.cas.ConfigWrapperUtil;
import org.plos.cas.InitParamProvider;
import org.plos.configuration.ConfigurationStore;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper to read the configuration values from our own configuration file.
 */
public class CASFilterWrapper extends CASFilter {
  /**
   * @see javax.servlet.Servlet
   */
  public void init(final FilterConfig filterConfig) throws ServletException {
    final Map<String, String> params = new HashMap<String, String>(6);

    final InitParamProvider initParamProvider = new InitParamProvider(){
      public Enumeration<?> getInitParameterNames() {
        return filterConfig.getInitParameterNames();
      }
      public String getInitParameter(final String key) {
        return filterConfig.getInitParameter(key);
      }
    };

    ConfigWrapperUtil.copyInitParams(initParamProvider, params);

    final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

    final String plosServerHost = configuration.getString("pub.host");
    final String casProxyValidateUrl = configuration.getString("cas.url.proxy-validate");
    final String casLoginUrl = configuration.getString("cas.url.login");

    ConfigWrapperUtil.setInitParamValue(CASFilter.LOGIN_INIT_PARAM,
            casLoginUrl, initParamProvider, params);
    ConfigWrapperUtil.setInitParamValue(CASFilter.VALIDATE_INIT_PARAM,
            casProxyValidateUrl, initParamProvider, params);
    ConfigWrapperUtil.setInitParamValue(CASFilter.SERVERNAME_INIT_PARAM,
            plosServerHost, initParamProvider, params);

    final FilterConfig customFilterConfig = new ConfigWrapper(filterConfig, params);
    super.init(customFilterConfig);
  }

}
