/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.pages.HomePage;
import org.plosone.it.jwebunit.PlosOneWebTester;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;




/**
 * BootStrap tests for an empty environment
 *
 * @author Pradeep Krishnan
 */
public class BootStrapTest extends AbstractPlosOneTest {
  private static final Log    log         = LogFactory.getLog(BootStrapTest.class);

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    getEmptyEnv().restore();
    getEmptyEnv().start();
  }

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testJournalInstall() {
    Env env = getEmptyEnv();
    String script = env.resource("/createjournal.groovy");
    String plosone = env.resource("/journal-plosone.xml");
    String ct = env.resource("/journal-clinicaltrials.xml");
    env.script(new String[]{script, "-j", plosone});
    env.script(new String[]{script, "-j", ct});
    env.stop();
    env.start();
  }

  @Test(dependsOnMethods={"testJournalInstall"})
  public void testPlosOneHomePage() {
    log.info("Testing home-page after journal install  ... ");
    PlosOneWebTester tester = getTester(HomePage.J_PONE, IE7);
    HomePage hp = new HomePage(tester, HomePage.J_PONE);
    hp.beginAt();
    hp.verifyPage();
  }

}
