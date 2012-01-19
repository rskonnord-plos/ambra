/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.it;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.DataProvider;
import org.topazproject.ambra.it.jwebunit.AmbraTestContext;
import org.topazproject.ambra.it.jwebunit.AmbraWebTester;
import org.topazproject.ambra.it.pages.AbstractPage;
import org.topazproject.ambra.it.pages.HomePage;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestingEngineRegistry;

/**
 * A base class for plosone tests.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractAmbraTest {
  private static final Log log = LogFactory.getLog(AbstractAmbraTest.class);

  /**
   * DOCUMENT ME!
   */
  public static final String TEST_ENGINE = AmbraHtmlUnitDialog.class.getName();

  /**
   * DOCUMENT ME!
   */
  public static final Map<String, BrowserVersion> browsers =
    new HashMap<String, BrowserVersion>();

  public static final String IE7 = "Internet Explorer 7";
  public static final String IE6 = "Internet Explorer 6";
  public static final String FIREFOX2 = "Firefox 2";

  public static final Map<String, Map<String,String>> journals =
    new HashMap<String, Map<String, String>>();
  public Map<String, String[]> articles = new HashMap<String, String[]>();

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

    HashMap<String, String> ctHeaders = new HashMap<String, String>();
    ctHeaders.put("plosJournal", "clinicaltrials");   // add this HttpHeader in every request
    journals.put(AbstractPage.J_PONE, new HashMap<String, String>());
    journals.put(AbstractPage.J_CT, ctHeaders);
  }

  /**
   * DOCUMENT ME!
   */
  private final Env[] envs =
    new Env[] {
                new Env("install/basic", "org.topazproject.ambra:plosone-it-data-basic:0.81.5"),
                new Env("install/empty", null)
    };

  /**
   * DOCUMENT ME!
   */
  private final Map<TesterId, AmbraWebTester> testers =
    new HashMap<TesterId, AmbraWebTester>();

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
    AmbraDAO dao = new AmbraDAO();
    for (String journal : journals.keySet()) {
      for (String browser : browsers.keySet()) {
        AmbraWebTester tester  = new AmbraWebTester();
        tester.setTestContext(new AmbraTestContext(browsers.get(browser), journals.get(journal)));
        tester.setTestingEngineKey(TEST_ENGINE);
        tester.getTestContext().setBaseUrl("http://localhost:8080/plosone-webapp");
        tester.setDao(dao);
        tester.setScriptingEnabled(true);
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

  public AmbraWebTester getTester(String journal, String browser) {
    return testers.get(new TesterId(journal, browser));
  }

  @DataProvider(name = "browsers")
  public Object[][] browserTestData() {
    ArrayList<String[]> l = new ArrayList<String[]>();
    for (String browser : browsers.keySet()) {
      l.add(new String[] {browser});
    }

    Object[][] dataArray = (Object[][])l.toArray(new Object[0][]);
    return (dataArray);
  }

  @DataProvider(name = "journals")
  public Object[][] journalsTestData() {
    ArrayList<String[]> l = new ArrayList<String[]>();
    for (String journal : journals.keySet())
      for (String browser : browsers.keySet()) {
        // TODO: fix java script for IE6 on CT
        if (IE6.equals(browser) && HomePage.J_CT.equals(journal))
          continue;
        l.add(new String[] { journal, browser });
      }

    return (Object[][]) l.toArray(new Object[0][]);
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

    Set<String> dois = useSmallSet ?
        Collections.singleton("info:doi/10.1371/journal.pone.0000021") : articles.keySet();

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
