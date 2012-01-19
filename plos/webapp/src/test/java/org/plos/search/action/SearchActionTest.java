/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.action;

import static com.opensymphony.xwork2.Action.SUCCESS;
import org.plos.BasePlosoneTestCase;
import org.plos.search.SearchResultPage;
import org.plos.search.SearchUtil;
import org.plos.util.FileUtils;

import java.io.File;

public class SearchActionTest extends BasePlosoneTestCase {
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
