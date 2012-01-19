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

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.it.jwebunit.AmbraWebTester;

/**
 * Ambra Article Display Page
 *
 * @author Pradeep Krishnan
 */
public class ArticlePage extends CommonBasePage {

  public static final String PAGE_URL = "/article/";

  public static final String ANNOTATION_LINK        = "Add your annotation";
  public static final String DISCUSSION_LINK        = "Start a discussion";
  public static final String DOWNLOAD_XML_LINK      = "Download Article XML";
  public static final String DOWNLOAD_PDF_LINK      = "Download Article PDF";
  public static final String DOWNLOAD_CITATION_LINK = "Download Citation";
  public static final String EMAIL_THIS_LINK        = "E-mail this Article";
  public static final String ORDER_REPRINTS_LINK    = "Order Reprints";
  public static final String PRINT_THIS_LINK        = "Print this Article";

  private static final String[] links = new String[] {
    ANNOTATION_LINK, DISCUSSION_LINK,
    DOWNLOAD_XML_LINK, DOWNLOAD_PDF_LINK, DOWNLOAD_CITATION_LINK,
    EMAIL_THIS_LINK, ORDER_REPRINTS_LINK, PRINT_THIS_LINK,
  };

  private final String doi;

  public ArticlePage(AmbraWebTester tester, String journal, String doi) {
    super(tester,journal, PAGE_URL + URLEncoder.encode(doi));
    this.doi = doi;
  }

  public static String getArticleUrl(String doi) {
    return PAGE_URL + URLEncoder.encode(doi);
  }


  public void verifyPage() {
    super.verifyPage();
    for (String link : links)
      tester.assertLinkPresentWithText(link);
    Article article = tester.getDao().getArticle(doi);
    String prefix = "";
    if (J_PONE.equals(getJournal()))
      prefix = "PLoS ONE: ";
    else if (J_CT.equals(getJournal()))
      prefix = "PLoS Hub - Clinical Trials: ";

    tester.assertTitleEquals(prefix + article.getDublinCore().getTitle());
    tester.assertTextPresent(article.getDublinCore().getTitle());
  }

  public void createAnnotation(String title, String body) {
     tester.clickLinkWithText(ANNOTATION_LINK);
     if (!tester.isLoggedIn() && isLoginPage())
       return;
     // TODO : Can't figure out a way to higlight an area to annotate.
     // So may be we emulate the submission part only by replaying 
     // a recorded POST.
  }

  public void startDiscussion(String title, String body) {
     tester.clickLinkWithText(DISCUSSION_LINK);
     if (!tester.isLoggedIn() && isLoginPage())
       return;
     // TODO : Can't figure out a way to higlight an area to annotate.
     // So may be we emulate the submission part only by replaying 
     // a recorded POST.
  }

}
