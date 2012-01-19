/* $HeadURL::                                                                                    $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.configuration;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.apache.commons.configuration.Configuration;
import java.util.Properties;

/**
 * A Spring Configurer that uses the commons-configuration singleton
 * <code>ConfigurationStore</code> in this package to get its configuration.
 *
 * @author Eric Brown
 */
public class SpringPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
  protected String resolvePlaceholder(String placeholder, Properties properties) {
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    String value = null;

    if (conf != null)
      value = conf.getString(placeholder);
    if (value == null)
      value = super.resolvePlaceholder(placeholder, properties);

    return value;
  }
}
