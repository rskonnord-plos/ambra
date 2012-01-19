package org.plosone.it.pages;

import org.plosone.it.jwebunit.PlosOneWebTester;

public class SiteMapPage extends CommonBasePage {

  public static final String PAGE_URL = "/static/sitemap.action";

  public SiteMapPage(PlosOneWebTester tester, String journal) {
    super(tester,journal, PAGE_URL);
  }

  public void verifyPage() {
    if (J_PONE.equals(getJournal())) {
       tester.assertTitleEquals("PLoS ONE : Publishing science, accelerating research");
    }
    
    if (J_CT.equals(getJournal())) {
       tester.assertTitleEquals("PLoS Hub - Clinical Trials: Connecting communities with open-access research");
    }
    tester.assertTextPresent("Site Map");
    super.verifyPage();
  }
}
