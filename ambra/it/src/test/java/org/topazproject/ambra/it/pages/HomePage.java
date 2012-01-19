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
package org.topazproject.ambra.it.pages;

import static org.testng.AssertJUnit.*;

import org.topazproject.ambra.it.jwebunit.AmbraWebTester;

/**
 * Ambra Home page
 *
 * @author Pradeep Krishnan
 */
public class HomePage extends CommonBasePage {

  public static final String PAGE_URL = "/home.action";

  public HomePage(AmbraWebTester tester, String journal) {
    super(tester,journal, PAGE_URL);
  }

  public void verifyPage() {
    if (J_PONE.equals(getJournal())) {
      // tester.assertTitleEquals("PLoS [Journals : A Peer-Reviewed, Open-Access Journal");
      tester.assertTitleEquals("PLoS ONE : Publishing science, accelerating research");
    }
    if (J_CT.equals(getJournal()))
    	tester.assertTitleEquals("PLoS Hub - Clinical Trials: Connecting communities with open-access research");
  }

}
