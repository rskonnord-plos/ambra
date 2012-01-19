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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.jwebunit.PlosOneTestContext;
import org.plosone.it.jwebunit.PlosOneWebTester;
import org.plosone.it.pages.AbstractPage;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestingEngineRegistry;

/**
 * A base class for plosone tests.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractPlosOneTest {
  private static final Log log = LogFactory.getLog(AbstractPlosOneTest.class);

  /**
   * DOCUMENT ME!
   */
  public static final String TEST_ENGINE = PlosOneHtmlUnitDialog.class.getName();

  /**
   * DOCUMENT ME!
   */
  public static final String BASE_URL = "http://localhost:8080/plosone-webapp";

  /**
   * DOCUMENT ME!
   */
  public static final Map<String, BrowserVersion> browsers = new HashMap();

  public static final String IE7 = "Internet Explorer 7";
  public static final String IE6 = "Internet Explorer 6";
  public static final String FIREFOX2 = "Firefox 2";

  public static final Map<String, Map<String,String>> journals = new HashMap();

  static {
    browsers.put(IE6, BrowserVersion.INTERNET_EXPLORER_6_0);
    browsers.put(IE7, BrowserVersion.INTERNET_EXPLORER_7_0);
    browsers.put(FIREFOX2, BrowserVersion.FIREFOX_2);

    // Note: Point the browser to emulate at http://htmlunit.sourceforge.net/cgi-bin/browserVersion 
    //       for the code to generate to add to the list of browsers above.
    try {
      TestingEngineRegistry.addTestingEngine(TEST_ENGINE, TEST_ENGINE);
    } catch (Throwable t) {
      throw new Error("Registration of test-engine failed", t);
    }

    HashMap ctHeaders = new HashMap();
    ctHeaders.put("plosJournal", "clinicaltrials");   // add this HttpHeader in every request
    journals.put(AbstractPage.J_PONE, new HashMap());
    journals.put(AbstractPage.J_CT, ctHeaders);
  }

  /**
   * DOCUMENT ME!
   */
  private final Env[] envs =
    new Env[] {
                new Env("install/basic", "org.plosone:plosone-it-data-basic:0.8"),
                new Env("install/empty", null)
    };

  /**
   * DOCUMENT ME!
   */
  private final Map<TesterId, PlosOneWebTester> testers = new HashMap();

  /**
   * DOCUMENT ME!
   */
  public void installEnvs() {
    for (Env env : envs)
      env.install();
  }

  /**
   * DOCUMENT ME!
   */
  public void initTesters() {
    PlosOneDAO dao = new PlosOneDAO();
    for (String journal : journals.keySet()) {
      for (String browser : browsers.keySet()) {
        PlosOneWebTester tester  = new PlosOneWebTester();
        tester.setTestContext(new PlosOneTestContext(browsers.get(browser), journals.get(journal)));
        tester.setTestingEngineKey(TEST_ENGINE);
        tester.getTestContext().setBaseUrl("http://localhost:8080/plosone-webapp");
        tester.setDao(dao);
        testers.put(new TesterId(journal, browser), tester);
      }
    }
  }

  public Env getBasicEnv() {
    return envs[0];
  }

  public Env getEmptyEnv() {
    return envs[1];
  }

  public PlosOneWebTester getTester(String journal, String browser) {
    return testers.get(new TesterId(journal, browser));
  }

  public static class TesterId {
    private final String browser;
    private final String journal;

    public TesterId(String journal, String browser) {
      this.browser = browser;
      this.journal = journal;
    }

    public int hashCode() {
      return browser.hashCode() + journal.hashCode();
    }

    public boolean equals(Object other) {
      if (!(other instanceof TesterId))
        return false;
      if (other == this)
        return true;
      return browser.equals(((TesterId)other).browser) 
        && journal.equals(((TesterId)other).journal);
    }
  }
}
