/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.configuration;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import junit.framework.TestCase;

/**
 * Test the configuration store.
 *
 * @author Pradeep Krishnan
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
      store.loadConfiguration(getClass().getResource("/config.xml"));
      conf = store.getConfiguration();
      assertTrue("Expected getConfiguration() to return a non-null", conf != null);
    } catch (ConfigurationException e) {
      gotExc = true;
    }

    assertTrue("Not expecting a ConfigurationException", !gotExc);

    Iterator it = conf.getKeys();
    assertTrue("Configuration has some keys", it.hasNext());

    conf = conf.subset("topaz");
    assertNotNull("Configuration has a non-null topaz subset", conf);

    it = conf.getKeys();
    assertTrue("Configuration has topaz.* keys", it.hasNext());

    conf = conf.subset("services");
    assertNotNull("Configuration has a non-null topaz.services subset", conf);

    it = conf.getKeys();
    assertTrue("Configuration has topaz.services.* keys", it.hasNext());

    conf = conf.subset("fedora");
    assertNotNull("Configuration has a non-null topaz.services.fedora subset", conf);

    it = conf.getKeys();
    assertTrue("Configuration has topaz.services.fedora.* keys", it.hasNext());

    assertNotNull("Configuration has topaz.services.fedora.uri", conf.getString("uri"));
  }
}
