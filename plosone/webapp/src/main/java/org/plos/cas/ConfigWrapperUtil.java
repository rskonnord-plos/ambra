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

import org.apache.commons.lang.StringUtils;

import java.util.Enumeration;
import java.util.Map;

/**
 * Itsy bitsy methods to help with the custom config wrappers around ServletConfig and FilterConfig
 */
public class ConfigWrapperUtil {
  /**
   * Copy the init params to the given map.
   * @param initParamProvider initParamProvider
   * @param params params
   */
  public static void copyInitParams(final InitParamProvider initParamProvider, final Map<String, String> params) {
    final Enumeration en = initParamProvider.getInitParameterNames();
    while (en.hasMoreElements()) {
      String key = (String) en.nextElement();
      params.put(key, initParamProvider.getInitParameter(key));
    }
  }

  /**
   * Set the init param into the map with the custom value if found or from the init provider otherwise
   * @param initParamName initParamName
   * @param customValue customValue
   * @param defaultInitParamProvider defaultInitParamProvider
   * @param params params
   */
  public static void setInitParamValue(final String initParamName, final String customValue, final InitParamProvider defaultInitParamProvider, final Map<String, String> params) {
    String finalValue = StringUtils.isBlank(customValue)
                          ? defaultInitParamProvider.getInitParameter(initParamName)
                          : customValue;

    params.put(initParamName, finalValue);
  }
}
