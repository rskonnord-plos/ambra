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
package org.topazproject.ambra.it.pages;

import java.net.URLEncoder;

import org.topazproject.ambra.it.jwebunit.AmbraWebTester;

/**
 * Ambra Search Results
 *
 * @author Pradeep Krishnan
 */
public class SearchResultsPage extends CommonBasePage {

  public static final String PAGE_URL = "/search/simpleSearch.action?query=";

  private final String[] expected;
  private final String query;


  public SearchResultsPage(AmbraWebTester tester, String journal, String query, 
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
