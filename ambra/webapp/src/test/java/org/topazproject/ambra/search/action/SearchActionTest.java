/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.topazproject.ambra.search.action;

import static com.opensymphony.xwork2.Action.SUCCESS;

import org.topazproject.ambra.BaseAmbraTestCase;
import org.topazproject.ambra.search.SearchResultPage;
import org.topazproject.ambra.search.SearchUtil;
import org.topazproject.ambra.search.action.SearchAction;
import org.topazproject.ambra.util.FileUtils;

import java.io.File;

public class SearchActionTest extends BaseAmbraTestCase {
  public void testSimpleSearchShouldReturnSomething(){
    final SearchAction searchAction = getSearchAction();
    searchAction.setQuery("system");
    searchAction.setStartPage(0);
    searchAction.setPageSize(10);
    assertEquals(SUCCESS, searchAction.executeSimpleSearch());
    assertEquals(2, searchAction.getSearchResults().size());
    assertEquals(10, searchAction.getPageSize());
  }

  public void testSearchObjectFormed() throws Exception {
    final String searchResultXml = "webapp/src/test/resources/searchResult.xml";

    final String text = FileUtils.getTextFromUrl(new File(searchResultXml).toURL().toString());
    final SearchResultPage searchHits = SearchUtil.convertSearchResultXml(text);
    assertEquals(1, searchHits.getTotalNoOfResults());
  }

}
