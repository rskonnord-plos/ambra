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

import java.util.List;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
  private static Configuration conf = null;

  protected void setUp() throws ConfigurationException {
    ConfigurationStore store = ConfigurationStore.getInstance();
    store.loadConfiguration(null);
    conf = store.getConfiguration();
    /* We want to use a test version of global-defaults.
     * However there is no way to override it. So we add this
     * at the end (last place looked).
     */
    ConfigurationStore.addResources((CombinedConfiguration)conf, 
        "/org/plos/configuration/global-defaults-test.xml");
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

  private void checkExpTest(List l) {
    assertNotNull(l);
    assertEquals(3, l.size());
    assertEquals("http://test1:8080/", l.get(0));
    assertEquals("http://test2:8080/", l.get(1));
    assertEquals("http://test1:8080/", l.get(2));
  }

  public void testExpandedDefaultsOverride() {
    checkExpTest(conf.getList("exptest.overrides.item"));
  }

  public void testExpandedDefaults() {
    checkExpTest(conf.getList("exptest.local.item"));
  }

  public void testSubsetDefaults1() {
    Configuration conf = this.conf.subset("exptest.local");
    checkExpTest(conf.getList("item"));
  }

  public void testSubsetDefaults2() {
    Configuration conf = this.conf.subset("exptest");
    checkExpTest(conf.getList("local.item"));
  }

  public void testSubsetDefaultsOverride1() {
    Configuration conf = this.conf.subset("exptest.overrides");
    checkExpTest(conf.getList("item"));
  }

  public void testSubsetDefaultsOverride2() {
    Configuration conf = this.conf.subset("exptest");
    checkExpTest(conf.getList("overrides.item"));
  }
}
