package org.plosone.it;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plosone.it.jwebunit.PlosOneWebTester;
import org.plosone.it.pages.AbstractPage;
import org.plosone.it.pages.HomePage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BrowseTest extends AbstractPlosOneTest {
  public static final Log log = LogFactory.getLog(BrowseTest.class);

  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    if (!("true".equalsIgnoreCase(System.getProperty("skipStartEnv")))) {
      getBasicEnv().start();
    }
  }

  /**
   * testBrowseByDate verifies that browse by date is working for all supported
   * browsers. The browse by date page is navigated to via the drop down menu on
   * the home page. The article retrieved is common to both PLos and CT
   * Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseBySubjectToPlosArticle(String browser) {
    try {
      log.info("Testing Browse PLOS Atricle by Subject using browser='"
          + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Subject");
      // Click on the Dec -> 20 link
      hp.getTester().clickLinkWithExactText("Ecology (1)");
      hp.getTester().clickLinkWithExactText(
          "No Intra-Locus Sexual Conflict over Reproductive "
              + "Fitness or Ageing in Field Crickets");
      hp.getTester().assertTextPresent(
          "Differences in the ways in which "
              + "males and females maximize evolutionary fitness");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseByDate verifies that browse by date is working for all supported
   * browsers. The browse by date page is navigated to via the drop down menu on
   * the home page. The article retrieved is common to both PLos and CT
   * Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseByDate(String browser) {
    try {
      log.info("Testing Browse Atricles by Date using browser='" + browser
          + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      hp.getTester().clickLinkWithExactText("20");
      hp.getTester().assertTextPresent(
          "Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseBySubject verifies that browse by subject is working for all
   * supported browsers. The browse by subject page is navigated to via the drop
   * down menu on the home page. The article retrieved is common to both PLos
   * and CT Journals.
   */
  @Test(dataProvider = "browsers")
  public void testBrowseBySubject(String browser) {
    try {
      log.info("Testing Browse Atricles by Subject using browser='" + browser
          + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Subject");
      hp.getTester().clickLinkWithExactText("Infectious Diseases (1)");
      hp.getTester().assertTextPresent(
          "Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the CT Journal are
   * not retrieved using browse by date within the PLoS Journal.
   */
  @Test
  public void testBrowseByDateJournalFilterPLoS() {
    try {
      // just grab the first browser
      String browser = AbstractPlosOneTest.IE7;
      log.info("Testing that CT-only articles are not presented when using "
          + "browse by date in PLoS Journal using browser='" + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Publication Date");
      hp.getTester().assertLinkNotPresentWithExactText("15"); // The date for
                                                              // Jun 15th
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the PONE Journal
   * are not retrieved using browse by date within the CT Journal.
   */
  @Test
  public void testBrowseByDateJournalFilterCT() {
    try {
      // just grab the first browser
      String browser = AbstractPlosOneTest.IE7;
      log.info("Testing that PLoS-only articles are not presented when using "
          + "browse by date in CT Journal using browser='" + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_CT);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Publication Date");
      // Click the date for Jan 17th 2007
      hp.getTester().assertLinkNotPresentWithExactText("17");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the CT Journal are
   * not retrieved using browse by subject within the PLoS Journal.
   */
  @Test
  public void testBrowseBySubjectJournalFilterPLoS() {
    try {
      // just grab the first browser
      String browser = AbstractPlosOneTest.IE7;
      log
          .info("Testing that CT-only articles are not presented when "
              + "browse by subject in PLoS Journal using browser='" + browser
              + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      hp.getTester().clickLinkWithExactText("By Subject");
      hp.getTester().assertLinkNotPresentWithExactText("Women's Health (1)");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * testBrowseByDateJournalFilter verifies that articles in the PLoS Journal
   * are not retrieved using browse by subject within the CT Journal.
   */
  @Test
  public void testBrowseBySubjectJournalFilterCT() {
    try {
      // just grab the first browser
      String browser = AbstractPlosOneTest.IE7;
      log.info("Testing that PLoS-only articles are not presented when "
          + "browse by subject in CT Journal using browser='" + browser + "'");
      PlosOneWebTester testerCt = getTester(AbstractPage.J_CT, browser);
      HomePage hpCt = new HomePage(testerCt, AbstractPage.J_CT);
      hpCt.gotoPage();
      hpCt.getTester().clickLinkWithExactText("By Subject");
      hpCt.getTester().assertLinkNotPresentWithExactText(
          "Evolutionary Biology (1)");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate to an annotation via the "See all commentary"
   * side bar. Verify that the annotation is from the correct user
   */
  @Test(dataProvider = "browsers")
  public void testNavigateAnnotationNonAjax(String browser) {
    try {
      log
          .info("Testing view annotation using left panel (non-ajax) using browser='"
              + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("Test Sputum Annotation");
      tester.assertTextPresent("This is some test text for sputum");
      tester.clickLinkWithExactText("plostest");
      tester.assertTextPresent("San Francisco, Uganda");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate to an annotation via the "See all commentary"
   * side bar under the CT journal. Verify that the annotation is from the
   * correct user. We don't need to test all browsers since this navigation has
   * been covered by testNavigateAnnotationNonAjax()
   */
  // TODO - this test is broken. Don't know how to fix the javascript errors!
  // @Test
  public void testNavigateAnnotationNonAjaxFromCT() {
    try {
      String browser = AbstractPlosOneTest.IE7;
      log.info("Testing view annotation using left panel (non-ajax) from CT "
          + "using browser='" + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_CT);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("Test Sputum Annotation");
      tester.assertTextPresent("This is some test text for sputum");
      tester.clickLinkWithExactText("plostest");
      tester.assertTextPresent("San Francisco, Uganda");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate to an annotation (created by plostest) via the
   * "See all commentary" side bar and that a response to the annotation is
   * available from plosuser.
   */
  // TODO - this test is broken for all browser types due to javascript engine
  // error
  // @Test(dataProvider = "browsers")
  public void testNavigateAnnotationReply(String browser) {
    try {
      log.info("Testing navigate annotaion reply using browser='" + browser
          + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("Mycobacterial culture test annotation");
      tester
          .assertTextPresent("Some test annotation text for Mycobacterial culture added "
              + "from the PLOS journal");
      tester.assertTextPresent("response from plosuser");
      tester.clickLinkWithExactText("plosuser");
      tester.assertTextPresent("plos user");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate to and see a discussion on an article.
   */
  @Test(dataProvider = "browsers")
  public void testNavigateDiscussion(String browser) {
    try {
      log.info("Testing navigate discussion using browser='" + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("This is a test discussion started by plostest");
      tester
          .assertTextPresent("Some discussion test text entered by plostest regarding "
              + "Isolation of Non-Tuberculous Mycobacteria article...");
      tester
          .assertTextPresent("RE: This is a test discussion started by plostest");
      tester.assertTextPresent("test response from plostest");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate to and see a discussion on an article from CT
   * journal. We don't need to test all browsers since this navigation has been
   * covered by testNavigateDiscussion()
   */
  @Test(dataProvider = "browsers")
  public void testNavigateDiscussionFromCT(String browser) {
    try {
      log.info("Testing navigate discussion from CT using browser='" + browser
          + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_CT);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.clickLinkWithText("See all commentary");
      tester.clickLinkWithText("This is a test discussion started by plostest");
      tester
          .assertTextPresent("Some discussion test text entered by plostest regarding "
              + "Isolation of Non-Tuberculous Mycobacteria article...");
      tester
          .assertTextPresent("RE: This is a test discussion started by plostest");
      tester.assertTextPresent("test response from plostest");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate a user rating and see the comments
   */
  @Test(dataProvider = "browsers")
  public void testNavigateRating(String browser) {
    try {
      log.info("Testing Navigate to Rating using browser='" + browser + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_PONE, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_PONE);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      try {
        tester
            .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
                + "Children Investigated for Pulmonary Tuberculosis");
      } catch (RuntimeException e) {
        throw e;
      }
      tester.assertTextPresent("Currently 3.5/5 Stars.");
      tester.clickLinkWithText("(1 User Rating)");
      tester.assertLinkPresentWithExactText("plosuser");
      tester.assertTextPresent("Test rating title from plosuser");
      tester.assertTextPresent("Test rating comment text from plosuser");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }

  /**
   * Verify that we can navigate a user rating and see the comments from CT
   * journal. The original rating was added from the PLOS journal. We only test
   * FIREFOX2 since other browser types were covered in testNavigateRating()
   */
  @Test(dataProvider = "browsers")
  public void testNavigateRatingFromCT(String browser) {
    try {
      log.info("Testing navigate to rating from CT using browser='" + browser
          + "'");
      PlosOneWebTester tester = getTester(AbstractPage.J_CT, browser);
      HomePage hp = new HomePage(tester, AbstractPage.J_CT);
      hp.gotoPage();
      tester.clickLinkWithExactText("By Publication Date");
      // Click on the Dec -> 20 link
      tester.clickLinkWithExactText("20");
      tester
          .clickLinkWithExactText("Isolation of Non-Tuberculous Mycobacteria in "
              + "Children Investigated for Pulmonary Tuberculosis");
      tester.assertTextPresent("Currently 3.5/5 Stars.");
      tester.clickLinkWithText("(1 User Rating)");
      tester.assertTextPresent("Test rating title from plosuser");
      tester.assertTextPresent("Test rating comment text from plosuser");
    } catch (Error error) {
      log.error("Test Failed:", error);
      throw error;
    }
  }
}
