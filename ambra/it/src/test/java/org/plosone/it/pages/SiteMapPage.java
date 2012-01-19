/* $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
 * $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
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
