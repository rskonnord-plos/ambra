/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it.pages;

import java.net.URLEncoder;

import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * PlosOne Search Results
 *
 * @author Pradeep Krishnan
 */
public class SearchResultsPage extends CommonBasePage {

  public static final String PAGE_URL = "/search/simpleSearch.action?query=";

  private final String[] expected;
  private final String query;


  public SearchResultsPage(PlosOneWebTester tester, String journal, String query, 
      String[] expected) {
    super(tester,journal, PAGE_URL + URLEncoder.encode(query));
    this.query = query;
    this.expected = expected;
  }

  public void verifyPage() {
    super.verifyPage();
    tester.assertTextPresent("Search Results");
    tester.assertTextPresent("Viewing results 1 - " + expected.length + " of " 
        + expected.length + " results, sorted by relevance, for " + query + ".");

    for (String doi : expected)
      tester.assertElementPresentByXPath("//a[contains(@href, '" 
          + ArticlePage.getArticleUrl(doi) + "')]");
  }


}
