/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.ambra.it;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.topazproject.ambra.it.jwebunit.AmbraWebTester;
import org.topazproject.ambra.it.pages.HomePage;

/**
 * BootStrap tests for an empty environment
 * 
 * @author Pradeep Krishnan
 */
public class BootStrapTest extends AbstractAmbraTest {
  private static final Log log = LogFactory.getLog(BootStrapTest.class);

  /**
   * DOCUMENT ME!
   * 
   * @throws Error
   *           DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    getEmptyEnv().restore();
    try {
      log.info("BootStrapTest.setUp() --> sleeping...");
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    log.info("BootStrapTest.setUp() --> starting env services...");
    getEmptyEnv().start();
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testJournalInstall() {
    log.info("Starting Test: testJournalInstall...");
    Env env = getEmptyEnv();
    String script = env.resource("/createjournal.groovy");
    String plosone = env.resource("/journal-plosone.xml");
    String ct = env.resource("/journal-clinicaltrials.xml");
    env.script(new String[] { script, "-j", plosone });
    env.script(new String[] { script, "-j", ct });
    env.stop();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    env.start();
  }

  @Test(dependsOnMethods = { "testJournalInstall" })
  public void testAmbraHomePage() {
    log.info("Testing home-page after journal install  ... ");
    AmbraWebTester tester = getTester(HomePage.J_PONE, IE7);
    HomePage hp = new HomePage(tester, HomePage.J_PONE);
    hp.beginAt();
    hp.verifyPage();
  }

}
