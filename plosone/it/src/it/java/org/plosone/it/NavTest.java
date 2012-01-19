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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.pages.HomePage;
import org.plosone.it.pages.ArticlePage;
import org.plosone.it.pages.SearchResultsPage;
import org.plosone.it.jwebunit.PlosOneWebTester;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * PlosOne Tests
 *
 * @author Pradeep Krishnan
 */
public class NavTest extends AbstractPlosOneTest {
  private static final Log    log         = LogFactory.getLog(NavTest.class);

  private Map<String, String[]> articles = new HashMap();

  /**
   * DOCUMENT ME!
   *
   * @throws Error DOCUMENT ME!
   */
  @BeforeClass
  public void setUp() {
    installEnvs();
    initTesters();
    getBasicEnv().start();
    setUpArticles();
  }

  private void setUpArticles() {
    String both[] = new String[] {HomePage.J_PONE, HomePage.J_CT};
    String pone[] = new String[] {HomePage.J_PONE};
    String ct[] = new String[] {HomePage.J_CT};
    articles.put("info:doi/10.1371/journal.pone.0000021", both);
    articles.put("info:doi/10.1371/journal.pone.0000155", pone);
    articles.put("info:doi/10.1371/journal.pctr.0020028", ct);
  }

  @DataProvider(name="articles")
  public Object[][] articlesTestData(Method m) {
    ArrayList l = new ArrayList();

    String methodsThatUseSmallSet[] = new String[] {"Annotation", "Discussion"};
    boolean useSmallSet = false;
    String mName = m.getName();

    for (String name : methodsThatUseSmallSet)
      if (mName.indexOf(name) > 0)
        useSmallSet = true;

    Set<String> dois = useSmallSet ?
      Collections.singleton("info:doi/10.1371/journal.pone.0000021") :
      articles.keySet();

    for (String article : dois) {
      for (String journal : articles.get(article)) {
        for (String browser: browsers.keySet()) {
          // TODO: fix java script for firefox
          if (FIREFOX2.equals(browser))
            continue;
          // TODO: fix java script for IE6 on CT
          if (IE6.equals(browser) && HomePage.J_CT.equals(journal))
            continue;
          l.add(new String[]{article, journal, browser});
        }
      }
    }
    return (Object[][])l.toArray(new Object[0][]);
  }

  @DataProvider(name="journals")
  public Object[][] journalsTestData() {
    ArrayList l = new ArrayList();
    for (String journal : journals.keySet())
      for (String browser: browsers.keySet()) {
        // TODO: fix java script for IE6 on CT
        if (IE6.equals(browser) && HomePage.J_CT.equals(journal))
          continue;
        l.add(new String[]{journal, browser});
      }

    return (Object[][])l.toArray(new Object[0][]);
  }


  /**
   * DOCUMENT ME!
   */
  @Test(dataProvider="journals")
  public void testHome(String journal, String browser) {
    log.info("Testing home page [journal=" + journal + ", browser=" + browser + "] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    hp.verifyPage();
  }

  /**
   * DOCUMENT ME!
   */
  @Test(dataProvider="journals")
  public void testLogin(String journal, String browser) {
    log.info("Testing login [journal=" + journal + ", browser=" + browser + "] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    hp.loginAs("admin", "plosadmin@gmail.com");
    hp.verifyPage();
    hp.logOut();
    hp.verifyPage();
  }

  @Test(dataProvider="journals")
  public void testSearch(String journal, String browser) {
    log.info("Testing search [journal=" + journal + ", browser=" + browser + "] ... ");
    HomePage hp = new HomePage(getTester(journal, browser), journal);
    hp.gotoPage();
    SearchResultsPage srp = hp.search("Natural", 
        new String[]{"info:doi/10.1371/journal.pone.0000021"});
    srp.verifyPage();
  }

  @Test(dataProvider="articles")
  public void testArticle(String article, String journal, String browser) {
    log.info("Testing article-view [article=" + article + ", journal=" + journal 
        + ", browser=" + browser + "] ... ");
    PlosOneWebTester tester = getTester(journal, browser);
    ArticlePage ap = new ArticlePage(tester, journal, article);
    ap.gotoPage();
    ap.verifyPage();
  }

  @Test(dataProvider="articles")
  public void testAnnotationLoginRedirect(String article, String journal, String browser) {
    log.info("Testing annotation-login-redirect [article=" + article + ", journal=" + journal 
        + ", browser=" + browser + "] ... ");
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

  @Test(dataProvider="articles")
  public void testDiscussionLoginRedirect(String article, String journal, String browser) {
    log.info("Testing discussion-login-redirect [article=" + article + ", journal=" + journal 
        + ", browser=" + browser + "] ... ");
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
}
