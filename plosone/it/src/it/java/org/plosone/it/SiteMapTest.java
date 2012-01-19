package org.plosone.it;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plosone.it.NavTest;
import org.plosone.it.jwebunit.PlosOneWebTester;
import org.plosone.it.pages.AbstractPage;
import org.plosone.it.pages.SiteMapPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class SiteMapTest extends AbstractPlosOneTest {
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
    PlosOneWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();
  }
  
  @SuppressWarnings("deprecation")
  @Test(dataProvider="journals")
  public void testLoginLink(String journal, String browser) {
    log.info("Verifying that the Login link is displayed on SiteMap when user not in session. "+
        "browser=['" + browser + "'] and journal=['" + journal + "']");
    PlosOneWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();

    tester.assertTextInElement("loginLogoutLink", "Login");
  }

  // @Test(dataProvider="journals")
  public void testLogoutLink(String journal, String browser) {
    log.info("Verifying that the Logout link is displayed on SiteMap when user is in session. "+
        "browser=['" + browser + "'] and journal=['" + journal + "']");
    PlosOneWebTester tester = getTester(journal, browser);
    SiteMapPage smp = new SiteMapPage(tester, journal);
    smp.gotoPage();
    smp.verifyPage();
    smp.loginAs("test", "plostest@gmail.com");
    tester.assertTextInElement("loginLogoutLink", "Logout");
    smp.logOut();
  }
}
