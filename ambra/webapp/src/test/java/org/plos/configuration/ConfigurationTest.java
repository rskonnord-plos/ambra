/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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