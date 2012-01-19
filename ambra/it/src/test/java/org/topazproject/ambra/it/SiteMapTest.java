/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
import org.topazproject.ambra.it.pages.SiteMapPage;


public class SiteMapTest extends AbstractAmbraTest {
  private static final Log log = LogFactory.getLog(SiteMapTest.class);
  
  /**
   * setUp() is called before any tests are run and will set up the testing environment
   * TODO - Probably could refactor this to 
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    if (!("true".equalsIgnoreCase(System.getProperty("skipStartEnv")))) {
      getBasicEnv().start();
    }
  }
  
  @Test(dataProvider="journals")
  public void testSiteMapHome(String journal, String browser) {
    log.info("Verifying SiteMap for browser=['" + browser + "'] and journal=['" + journal + "']");
    AmbraWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();
  }
  
  @SuppressWarnings("deprecation")
  @Test(dataProvider="journals")
  public void testLoginLink(String journal, String browser) {
    log.info("Verifying that the Login link is displayed on SiteMap when user not in session. "+
        "browser=['" + browser + "'] and journal=['" + journal + "']");
    AmbraWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();

    tester.assertTextInElement("loginLogoutLink", "Login");
  }

  // @Test(dataProvider="journals")
  public void testLogoutLink(String journal, String browser) {
    log.info("Verifying that the Logout link is displayed on SiteMap when user is in session. "+
        "browser=['" + browser + "'] and journal=['" + journal + "']");
    AmbraWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();
    smp.loginAs("test", "plostest@gmail.com");
    tester.assertTextInElement("loginLogoutLink", "Logout");
    smp.logOut();
  }
}
