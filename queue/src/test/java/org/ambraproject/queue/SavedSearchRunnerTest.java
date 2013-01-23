/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.queue;

import org.ambraproject.ApplicationException;
import org.ambraproject.action.BaseTest;
import org.ambraproject.search.SavedSearchRunner;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.ambraproject.views.SavedSearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA. User: stumu Date: 10/8/12 Time: 1:58 PM To change this template use File | Settings |
 * File Templates.
 */
@ContextConfiguration
public class SavedSearchRunnerTest extends BaseTest {

  private static final Logger log = LoggerFactory.getLogger(SavedSearchRunnerTest.class);

  @Autowired
  protected SavedSearchRunner savedSearchRunner;

  @Autowired
  protected EmbeddedSolrServerFactory solrServerFactory;

  private static final String DOI_1         = "10.1371/journal.pgen.1000096";
  private static final String DOI_2         = "10.1371/journal.pbio.1000100";
  private static final String DOI_3         = "10.1371/journal.pbio.1000101";
  private static final String JOURNAL_KEY_1 = "PLoSONE";
  private static final String JOURNAL_KEY_2 = "PLoSMedicine";
  private static final String CATEGORY_1    = "Category1";
  private static final String CATEGORY_2    = "Category2";

  /**
   * Seed the solr server with some articles to test
   *
   * @throws Exception - if there's a problem
   */
  @BeforeMethod
  public void sendArticlesToSolr() throws Exception {

    // 2 occurrences in "everything": "Spleen"
    Map<String, String[]> document1 = new HashMap<String, String[]>();
    document1.put("id", new String[]{DOI_1});
    document1.put("title", new String[]{"The First Title, with Spleen testing"});
    document1.put("title_display", new String[]{"The First Title, with Spleen testing"});
    document1.put("author", new String[]{"alpha delta epsilon"});
    document1.put("body", new String[]{"Body of the first document: Yak and Spleen test"});
    document1.put("everything", new String[]{
        "body first document yak spleen first title with spleen test"});
    document1.put("elocation_id", new String[]{"111"});
    document1.put("volume", new String[]{"1"});
    document1.put("doc_type", new String[]{"full"});
    document1.put("publication_date", new String[]{"2008-06-13T00:00:00Z"});
    document1.put("cross_published_journal_key", new String[]{JOURNAL_KEY_1});
    document1.put("subject", new String[]{CATEGORY_1, CATEGORY_2});
    document1.put("article_type_facet", new String[]{"Not an issue image"});
    document1.put("author", new String[]{"doc1 author"});
    document1.put("author_display", new String[]{"doc1 creator"});

    // 2 occurrences in "everything": "Yak"
    Map<String, String[]> document2 = new HashMap<String, String[]>();
    document2.put("id", new String[]{DOI_2});
    document2.put("title", new String[]{"The Second Title, with Yak YEk"});
    document2.put("title_display", new String[]{"The Second Title, with Yak YAK"});
    document2.put("author", new String[]{"beta delta epsilon"});
    document2.put("body", new String[]{"Description of the second document: Yak and Islets of Langerhans testing"});
    document2.put("everything", new String[]{
        "description second document yak islets Langerhans second title with yak testing"});
    document2.put("elocation_id", new String[]{"222"});
    document2.put("volume", new String[]{"2"});
    document2.put("doc_type", new String[]{"full"});
    document2.put("publication_date", new String[]{"2008-06-20T00:00:00Z"});
    document2.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document2.put("subject", new String[]{CATEGORY_2});
    document2.put("article_type_facet", new String[]{"Not an issue image"});
    document2.put("author", new String[]{"doc2 author"});
    document2.put("author_display", new String[]{"doc2 creator"});

    // 2 occurrences in "everything": "Gecko"
    Map<String, String[]> document3 = new HashMap<String, String[]>();
    document3.put("id", new String[]{DOI_3});
    document3.put("title", new String[]{"The Third Title, with Gecko  "});
    document3.put("title_display", new String[]{"The Second Title, with Yak Gecko"});
    document3.put("author", new String[]{"gamma delta"});
    document3.put("body", new String[]{"Contents of the second document: Gecko and Islets of Langerhans"});
    document3.put("everything", new String[]{
        "contents of the second document gecko islets langerhans third title with gecko testjournal"});
    document3.put("elocation_id", new String[]{"333"});
    document3.put("volume", new String[]{"3"});
    document3.put("doc_type", new String[]{"full"});
    document3.put("publication_date", new String[]{"2008-06-22T00:00:00Z"});
    document3.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document3.put("subject", new String[]{CATEGORY_1});
    document3.put("article_type_facet", new String[]{"Not an issue image"});
    document3.put("author", new String[]{"doc3 author"});
    document3.put("author_display", new String[]{"doc3 creator"});

    solrServerFactory.addDocument(document1);
    solrServerFactory.addDocument(document2);
    solrServerFactory.addDocument(document3);

  }

  @Test (dataProvider= "savedSearchViewData")
  public void testRunSavedSearch(SavedSearchView view, int expectedHits) throws ApplicationException {

    SavedSearchView searchView = null;
    searchView = savedSearchRunner.runSavedSearch(view);
    log.debug("Search name: {} and Search query: {}", view.getSearchName(),view.getSearchParameters());
    assertNotNull(searchView.getSearchHitList(), "No results for the query");
    assertEquals(searchView.getSearchHitList().size(), expectedHits , "Wrong number of search results returned");

  }

  @DataProvider(name="savedSearchViewData")
  public Object[][] savedSearchViewData(){

    Calendar lastWeeklySearchTime = Calendar.getInstance();
    lastWeeklySearchTime.set(Calendar.YEAR, 2007);
    lastWeeklySearchTime.set(Calendar.MONTH, 5);
    lastWeeklySearchTime.set(Calendar.DAY_OF_MONTH, 15);

    Calendar lastMonthlySearchTime = Calendar.getInstance();
    lastMonthlySearchTime.set(Calendar.YEAR, 2007);
    lastMonthlySearchTime.set(Calendar.MONTH, 4);
    lastMonthlySearchTime.set(Calendar.DAY_OF_MONTH, 15);

    SavedSearchView savedSearchView1 = new SavedSearchView(
        new Long(1),
        "testString1",
        "{\"query\":\"test\",\"unformattedQuery\":\"\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSONE\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}",
        lastWeeklySearchTime.getTime(),
        "search1@plos.org"
    ) ;

    SavedSearchView savedSearchView2 = new SavedSearchView(
        new Long(2),
        "testingString2",
        "{\"query\":\"\",\"unformattedQuery\":\"everything:testjournal\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}",
        lastMonthlySearchTime.getTime(),
        "search2@plos.org") ;

    SavedSearchView savedSearchView3 = new SavedSearchView(
        new Long(3),
        "testString3",
        "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\",\"PLoSONE\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}",
        lastWeeklySearchTime.getTime(),
        "search3@plos.org") ;


    return new Object[][]{
      {savedSearchView1, 1 },{savedSearchView2, 1},{savedSearchView3, 2}
    };
  }

}
