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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.jwebunit.PlosOneWebTester;

/**
 * An abstract base class for PlosOne pages.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractPage {
  private static final Log    log         = LogFactory.getLog(AbstractPage.class);

  protected final PlosOneWebTester tester;
  protected final String url;
  protected final String journal;
  public static final String J_PONE = "pone";
  public static final String J_CT = "ct";

  public AbstractPage(PlosOneWebTester tester, String journal, String url) {
    this.tester = tester;
    this.journal = journal;
    this.url = url;
  }

  public String getJournal() {
    return journal;
  }

  public String getUrl() {
    return url;
  }

  public PlosOneWebTester getTester() {
    return tester;
  }


  public void beginAt() {
    log.debug("New browser to " + url + " ...");
    tester.beginAt(url);
  }

  public void gotoPage() {
    log.debug("Going to " + url + " ...");
    tester.gotoPage(url);
  }

  public abstract void verifyPage();


  public boolean isLoginPage() {
    return "Dummy-SSO Login".equals(tester.getDialog().getPageTitle());
  }

}
