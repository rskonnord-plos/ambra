/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.configuration;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import junit.framework.TestCase;

/**
 * Test the configuration store.
 *
 * @author Stephen Cheng
 */
public class ConfigurationTest extends TestCase {
  private ConfigurationStore store = ConfigurationStore.getInstance();

  /**
   * Creates a new ConfigurationTest object.
   *
   * @param testName name of the test
   */
  public ConfigurationTest(String testName) {
    super(testName);
  }

  /**
   * Sets up the test.
   */
  protected void setUp() {
  }

  /**
   * Does the test.
   */
  public void testAll() {
    basicConfigurationTest();
  }

  private void basicConfigurationTest() {
    boolean gotExc;

    store.unloadConfiguration();

    Configuration conf = store.getConfiguration();
    assertNull("Expected getConfiguration() to return null", conf);

    gotExc = false;

    try {
      store.loadConfiguration(getClass().getResource("/plosOneConfig.xml"));
      conf = store.getConfiguration();
      assertTrue("Expected getConfiguration() to return a non-null", conf != null);
    } catch (ConfigurationException e) {
      gotExc = true;
    }

    assertTrue("Not expecting a ConfigurationException", !gotExc);

    Iterator it = conf.getKeys();
    assertTrue("Configuration has some keys", it.hasNext());

    conf = conf.subset("page");
    assertNotNull("Configuration has a non-null topaz subset", conf);

    it = conf.getKeys();
    assertTrue("Configuration has page.* keys", it.hasNext());

    
  }
}