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

import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import junit.framework.TestCase;

public class OverrideTest extends TestCase {
  private static Configuration conf = null;

  protected void setUp() throws ConfigurationException {
    System.setProperty(ConfigurationStore.OVERRIDES_URL, "org/plos/configuration/defaults-dev.xml");
    System.setProperty("plosconf.test", "goodbye world");
    ConfigurationStore store = ConfigurationStore.getInstance();
    store.loadConfiguration(null);
    conf = store.getConfiguration();
  }

  protected void tearDown() {
    // Make an attempt to remove the system property for other test classes
    Properties p = System.getProperties();
    p.remove(ConfigurationStore.OVERRIDES_URL);
    p.remove("plosconf.test");
    System.setProperties(p);
  }

  public void testDefaultsOverrideGlobal() {
    assertEquals("defaults override", "override-dev", conf.getString("plosconf.def"));
  }

  public void testSystemPropertyOverride() {
    assertEquals("system property override", "goodbye world", conf.getString("plosconf.test"));
  }
}
