/* $HeadURL::                                                                                      $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
  private static Configuration conf = null;

  protected void setUp() throws ConfigurationException {
    ConfigurationStore store = ConfigurationStore.getInstance();
    store.loadDefaultConfiguration();
    conf = store.getConfiguration();
  }

  protected void tearDown() {
  }

  public void testGlobalDefaults() {
    assertEquals("global-defaults plosconf.test", "hello world", conf.getString("plosconf.test"));
  }

  public void testDefaults() {
    assertEquals("defaults defaults", "value", conf.getString("defaults"));
  }

  public void testDefaultsOverrideGlobal() {
    assertEquals("defaults override", "override", conf.getString("plosconf.def"));
  }
}
