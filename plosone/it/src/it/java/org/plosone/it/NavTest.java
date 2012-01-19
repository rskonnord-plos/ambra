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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.jwebunit.junit.WebTester;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plosone.it.jwebunit.PlosOneWebTester;
import org.plosone.it.pages.AbstractPage;
import org.plosone.it.pages.ArticlePage;
import org.plosone.it.pages.HomePage;
import org.plosone.it.pages.SearchResultsPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * PlosOne Tests
 * 
 * @author Pradeep Krishnan
 */
public class NavTest extends AbstractPlosOneTest {
  private static final Log log = LogFactory.getLog(NavTest.class);

  private Map<String, String[]> articles = new HashMap();

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
    if (!("true".equalsIgnoreCase(System.getProperty("skipStartEnv")))) {
      getBasicEnv().start();
    }
    setUpArticles();
  }

  private void setUpArticles() {
    String both[] = new String[] { HomePage.J_PONE, HomePage.J_CT };
    String pone[] = new String[] { HomePage.J_PONE };
    String ct[] = new String[] { HomePage.J_CT };
    articles.put("info:doi/10.1371/journal.pone.0000021", both);
    articles.put("info:doi/10.1371/journal.pone.0000155", pone);
    articles.put("info:doi/10.1371/journal.pctr.0020028", ct);
  }

  @DataProvider(name = "articles")
  public Object[][] articlesTestData(Method m) {
    ArrayList<String[]> l = new ArrayList<String[]>();

    String methodsThatUseSmallSet[] = new String[] { "Annotation", "Discussion" };
    boolean useSmallSet = false;
    String mName = m.getName();

    for (String name : methodsThatUseSmallSet)
      if (mName.indexOf(name) > 0)
        useSmallSet = true;

    Set<String> dois = useSmallSet ? Collections.singleton("info:doi/10.1371/journal.pone.0000021") : articles.keySet();

    for (String article : dois) {
      for (String journal : articles.get(article)) {
        for (String browser : browsers.keySet()) {
          // TODO: fix java script for firefox
          if (FIREFOX2.equals(browser))
            continue;
          // TODO: fix java script for IE6 on CT
          if (IE6.equals(browser) && HomePage.J_CT.equals(journal))
            continue;
          l.add(new String[] { article, journal, browser });
        }
      }
    }
    return (Object[][]) l.toArray(new Object[0][]);
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dataProvider = "journals")
  public void testHome(String journal, String browser) {
    log.info("Testing home page [journal='" + journal + "', browser='" + browser + "'] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    hp.verifyPage();
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dataProvider = "journals")
  public void testLogin(String journal, String browser) {
    log.info("Testing login [journal='" + journal + "', browser='" + browser + "'] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    hp.loginAs("admin", "plosadmin@gmail.com");
    hp.verifyPage();
    hp.logOut();
    hp.verifyPage();
  }

  @Test(dataProvider = "journals")
  public void testSearch(String journal, String browser) {
    log.info("Testing search [journal='" + journal + "', browser='" + browser + "'] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    SearchResultsPage srp = hp.search("Natural", new String[] { "info:doi/10.1371/journal.pone.0000021" });
    srp.verifyPage();
  }

  /**
   * Verifies that it is possible to navigate directly to an article. 
   * 
   * @param article
   * @param journal
   * @param browser
   */
  @Test(dataProvider = "articles")
  public void testArticle(String article, String journal, String browser) {
    log.info("Testing article-view [article='" + article + "', journal='" + journal + "', browser='" + browser + "'] ... ");
    PlosOneWebTester tester = getTester(journal, browser);
    ArticlePage ap = new ArticlePage(tester, journal, article);
    ap.gotoPage();
    ap.verifyPage();
  }

  @Test(dataProvider = "articles")
  public void testAnnotationLoginRedirect(String article, String journal, String browser) {
    log.info("Testing annotation-login-redirect [article='" + article + "', journal='" + journal + "', browser='" + browser
        + "'] ... ");
    PlosOneWebTester tester = getTester(journal, browser);
    ArticlePage ap = new ArticlePage(tester, journal, article);
    ap.gotoPage();
    ap.logOut();
    ap.createAnnotation("Test title", "Test Body");
    ap.loginAs("test", "plostest@gmail.com");
    ap.verifyPage();
    ap.logOut();
    ap.verifyPage();
  }

  @Test(dataProvider = "articles")
  public void testDiscussionLoginRedirect(String article, String journal, String browser) {
    log.info("Testing discussion-login-redirect [article='" + article + "', journal='" + journal + "', browser='" + browser
        + "'] ... ");
    PlosOneWebTester tester = getTester(journal, browser);
    ArticlePage ap = new ArticlePage(tester, journal, article);
    ap.gotoPage();
    ap.logOut();
    ap.startDiscussion("Test title", "Test Body");
    ap.loginAs("test", "plostest@gmail.com");
    ap.verifyPage();
    ap.logOut();
    ap.verifyPage();
  }

  /**
   * testBrowseByDate verifies that browse by date is working for all supported
   * browsers. The browse by date page is navigated to via the drop down menu on
   * the home page. The article retrieved is common to both PLos and CT
   * Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseBySubjectToPlosArticle(String browser) {
    if (AbstractPlosOneTest.FIREFOX2.equals(browser)) {
      // TODO - need to fix FIREFOX2 javascript simulation
      log.info("Skipping test with browser type [browser='" + 
               AbstractPlosOneTest.FIREFOX2 + "'] since the javascript engine is broken.");
      return;
    }
    log.info("Testing Browse PLOS Atricle by Subject [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Subject");
    // Click on the Dec -> 20 link
    hp.getTester().clickLinkWithExactText("Ecology (1)");
    hp.getTester().clickLinkWithExactText("No Intra-Locus Sexual Conflict over Reproductive " +
    		"Fitness or Ageing in Field Crickets");
    hp.getTester().assertTextPresent("Differences in the ways in which " +
    		"males and females maximize evolutionary fitness");
  }
  
  /**
   * testBrowseByDate verifies that browse by date is working for all supported
   * browsers. The browse by date page is navigated to via the drop down menu on
   * the home page. The article retrieved is common to both PLos and CT
   * Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseByDate(String browser) {
    log.info("Testing Browse Atricles by Date [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    hp.getTester().clickLinkWithExactText("20");
    hp.getTester().assertTextPresent("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
  }

  /**
   * testBrowseBySubject verifies that browse by subject is working for all
   * supported browsers. The browse by subject page is navigated to via the drop
   * down menu on the home page. The article retrieved is common to both PLos
   * and CT Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseBySubject(String browser) {
    log.info("Testing Browse Atricles by Subject [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Subject");
    hp.getTester().clickLinkWithExactText("Infectious Diseases (1)");
    hp.getTester().assertTextPresent("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the CT Journal are
   * not retrieved using browse by date within the PLoS Journal.
   */
  @Test
  public void testBrowseByDateJournalFilterPLoS() {
    // just grab the first browser
    String browser = browsers.keySet().iterator().next();
    log.info("Testing that CT-only articles are not presented when using " +
    		"browse by date in PLoS Journal [browser='"
        + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Publication Date");
    hp.getTester().assertLinkNotPresentWithExactText("15"); // The date for Jun 15th
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the PONE Journal are
   * not retrieved using browse by date within the CT Journal.
   */
  @Test
  public void testBrowseByDateJournalFilterCT() {
    // just grab the first browser
    String browser = browsers.keySet().iterator().next();
    log.info("Testing that PLoS-only articles are not presented when using " +
    		"browse by date in CT Journal [browser='"
        + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_CT);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Publication Date");
    // Click the date for Jan 17th 2007
    hp.getTester().assertLinkNotPresentWithExactText("17");
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the CT Journal are
   * not retrieved using browse by subject within the PLoS Journal.
   */
  @Test
  public void testBrowseBySubjectJournalFilterPLoS() {
    // just grab the first browser
    String browser = browsers.keySet().iterator().next();
    log.info("Testing that CT-only articles are not presented when " +
             "browse by subject in PLoS Journal [browser='"
             + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    hp.getTester().clickLinkWithExactText("By Subject");
    hp.getTester().assertLinkNotPresentWithExactText("Women's Health (1)");
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the PLoS Journal
   * are not retrieved using browse by subject within the CT Journal.
   */
  @Test
  public void testBrowseBySubjectJournalFilterCT() {
    // just grab the first browser
    String browser = browsers.keySet().iterator().next();
    log.info("Testing that PLoS-only articles are not presented when " + 
             "browse by subject in CT Journal [browser='" 
             + browser + "']");
    PlosOneWebTester testerCt = getTester(AbstractPage.J_CT, browser);
    HomePage hpCt = new HomePage(testerCt, AbstractPage.J_CT);
    hpCt.gotoPage();
    hpCt.getTester().clickLinkWithExactText("By Subject");
    hpCt.getTester().assertLinkNotPresentWithExactText("Evolutionary Biology (1)");
  }
  
  /**
   * Verify that we can navigate to an annotation via the "See all commentary" side bar. 
   * Verify that the annotation is from the correct user
   */
  @Test(dataProvider="browsers")
  public void testNavigateAnnotationNonAjax(String browser) {
      if (AbstractPlosOneTest.FIREFOX2.equals(browser)) {
        log.info("Skipping test with browser type [browser='" + 
                 AbstractPlosOneTest.FIREFOX2 + "'] since the javascript engine is broken.");
        return;
      }
      log.info("Testing view annotation on [browser=" + browser);
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in " +
      		                          "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("Test Sputum Annotation");
      tester.assertTextPresent("This is some test text for sputum");
      tester.clickLinkWithExactText("plostest");
      tester.assertTextPresent("San Francisco, Uganda");
  }

  /**
   * Verify that we can navigate to an annotation via the "See all commentary"
   * side bar under the CT journal. Verify that the annotation is from the
   * correct user. We don't need to test all browsers since this navigation has
   * been covered by testNavigateAnnotationNonAjax()
   */
  @Test
  public void testNavigateAnnotationNonAjaxFromCT() {
    String browser = AbstractPlosOneTest.IE7;
    log.info("Testing view annotation on [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_CT);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.clickLinkWithText("See all commentary");
    tester.clickLinkWithText("Test Sputum Annotation");
    tester.assertTextPresent("This is some test text for sputum");
    tester.clickLinkWithExactText("plostest");
    tester.assertTextPresent("San Francisco, Uganda");
  }

  /**
   * Verify that we can navigate to an annotation (created by plostest) via the "See all commentary" 
   * side bar and that a response to the annotation is available from plosuser. 
   */
  @Test(dataProvider = "browsers")
  public void testNavigateAnnotationReply(String browser) {
    if (AbstractPlosOneTest.FIREFOX2.equals(browser)) {
      log.info("Skipping test with browser type [browser='" + 
               AbstractPlosOneTest.FIREFOX2 + "'] since the javascript engine is broken.");
      return;
    }
    log.info("Testing view annotation on [browser=" + browser);
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.clickLinkWithText("See all commentary");
    tester.clickLinkWithText("Mycobacterial culture test annotation");
    tester.assertTextPresent("Some test annotation text for Mycobacterial culture added from the PLOS journal");
    tester.assertTextPresent("response from plosuser");
    tester.clickLinkWithExactText("plosuser");
    tester.assertTextPresent("plos user");
  }
  
  /**
   * Verify that we can navigate to and see a discussion on an article.
   */
  @Test(dataProvider="browsers")
  public void testNavigateDiscussion(String browser) {
    if (AbstractPlosOneTest.FIREFOX2.equals(browser)) {
      log.info("Skipping test with browser type [browser='" + 
               AbstractPlosOneTest.FIREFOX2 + "'] since the javascript engine is broken.");
      return;
    }
    log.info("Testing view annotation on [browser=" + browser);
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.clickLinkWithText("See all commentary");
    tester.clickLinkWithText("This is a test discussion started by plostest");
    tester.assertTextPresent("Some discussion test text entered by plostest regarding "
        + "Isolation of Non-Tuberculous Mycobacteria article...");
    tester.assertTextPresent("RE: This is a test discussion started by plostest");
    tester.assertTextPresent("test response from plostest");
  }

  /**
   * Verify that we can navigate to and see a discussion on an article from CT
   * journal. We don't need to test all browsers since this navigation has been
   * covered by testNavigateDiscussion()
   */
  @Test
  public void testNavigateDiscussionFromCT() {
    String browser = AbstractPlosOneTest.IE7;
    log.info("Testing view annotation on [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_CT);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.clickLinkWithText("See all commentary");
    tester.clickLinkWithText("This is a test discussion started by plostest");
    tester.assertTextPresent("Some discussion test text entered by plostest regarding "
        + "Isolation of Non-Tuberculous Mycobacteria article...");
    tester.assertTextPresent("RE: This is a test discussion started by plostest");
    tester.assertTextPresent("test response from plostest");
  }
  
  /**
   * Verify that we can navigate a user rating and see the comments
   */
  @Test(dataProvider = "browsers")
  public void testNavigateRating(String browser) {
    // TODO - The javascript sim engine for Firefox 2 appears to be broken,
    // Need to investigate.
    if (AbstractPlosOneTest.FIREFOX2.equals(browser)) {
      log.info("Skipping test with browser type [browser='" + 
               AbstractPlosOneTest.FIREFOX2 + "'] since the javascript engine is broken.");
      return;
    }
    log.info("Testing view annotation on [browser=" + browser);
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.assertTextPresent("Currently 3.5/5 Stars.");
    tester.clickLinkWithText("(1 User Rating)");
    tester.assertLinkPresentWithExactText("plosuser");
    tester.assertTextPresent("Test rating title from plosuser");
    tester.assertTextPresent("Test rating comment text from plosuser");
  }
  
  /**
   * Verify that we can navigate a user rating and see the comments from CT
   * journal. The original rating was added from the PLOS journal. We only test
   * IE7 since other browser types were covered in testNavigateRating()
   */
  @Test
  public void testNavigateRatingFromCT() {
    String browser = AbstractPlosOneTest.IE7;
    log.info("Testing view annotation on [browser='" + browser + "']");
    PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
    HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
    hp.gotoPage();
    tester.clickLinkWithExactText("By Publication Date");
    // Click on the Dec -> 20 link
    tester.clickLinkWithExactText("20");
    tester.clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
        + "Children Investigated for Pulmonary Tuberculosis");
    tester.assertTextPresent("Currently 3.5/5 Stars.");
    tester.clickLinkWithText("(1 User Rating)");
    tester.assertTextPresent("Test rating title from plosuser");
    tester.assertTextPresent("Test rating comment text from plosuser");
  }
}
