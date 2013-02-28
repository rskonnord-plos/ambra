/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.UserProfile;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.ambraproject.util.TextUtils;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for the savedSearch camel route
 *
 * @author stumu
 * @author
 */
@ContextConfiguration
public class SavedSearchEmailRoutesTest extends BaseTest {

  @Produce(uri = "direct:getsearches")
  protected ProducerTemplate start;

  @Autowired
  protected EmbeddedSolrServerFactory solrServerFactory;

  private static final String DOI_1         = "10.1371/journal.pone.1002222";
  private static final String DOI_2         = "10.1371/journal.pmed.1002223";
  private static final String DOI_3         = "10.1371/journal.pone.1002224";
  private static final String DOI_4         = "10.1371/journal.pmed.1002225";
  private static final String JOURNAL_KEY_1 = "PLoSONE";
  private static final String JOURNAL_KEY_2 = "PLoSMedicine";
  private static final String CATEGORY_1    = "Category1";
  private static final String CATEGORY_2    = "Category2";

  @BeforeMethod
  public void seedUserData() {
    Calendar searchTime = Calendar.getInstance();
    searchTime.set(Calendar.YEAR, 2007);
    searchTime.set(Calendar.MONTH, 5);
    searchTime.set(Calendar.DAY_OF_MONTH, 15);

    for (int i = 1; i <= 3; i++) {
      UserProfile user = new UserProfile("savedSearch-" + i, i + "savedSearch1@example.org", "savedSearch" + i);

      String query = "{\"query\":\"test\",\"unformattedQuery\":\"\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSONE\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      SavedSearchQuery params = new SavedSearchQuery(query, TextUtils.createHash(query));
      dummyDataStore.store(params);

      SavedSearch savedSearch1 = new SavedSearch("weekly-" + i, params);
      savedSearch1.setWeekly(true);
      savedSearch1.setMonthly(false);
      savedSearch1.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch1.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchQuery(query, TextUtils.createHash(query));
      dummyDataStore.store(params);

      SavedSearch savedSearch2 = new SavedSearch("monthly-" + i, params);
      savedSearch2.setWeekly(false);
      savedSearch2.setMonthly(true);
      savedSearch2.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch2.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchQuery(query, TextUtils.createHash(query));
      dummyDataStore.store(params);

      SavedSearch savedSearch3 = new SavedSearch("both-" + i, params);
      savedSearch3.setMonthly(true);
      savedSearch3.setWeekly(true);
      savedSearch3.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch3.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:debug\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchQuery(query, TextUtils.createHash(query));
      dummyDataStore.store(params);

      SavedSearch savedSearch4 = new SavedSearch("both-" + i, params);
      savedSearch4.setMonthly(true);
      savedSearch4.setWeekly(true);
      savedSearch4.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch4.setLastMonthlySearchTime(searchTime.getTime());

      user.setSavedSearches(Arrays.asList(savedSearch1, savedSearch2, savedSearch3,savedSearch4));
      dummyDataStore.store(user);
    }
  }

  @BeforeMethod
  public void seedSolrData() throws Exception{

    // 2 occurrences in "everything": "Spleen"
    Map<String, String[]> document1 = new HashMap<String, String[]>();
    document1.put("id", new String[]{DOI_1});
    document1.put("title", new String[]{"The First Title, with Spleen testing"});
    document1.put("title_display", new String[]{"The First Title, with Spleen testing"});
    document1.put("author", new String[]{"alpha delta epsilon"});
    document1.put("body", new String[]{"Body of the first document: Yak and Spleen"});
    document1.put("everything", new String[]{
        "body first document yak spleen first title with spleen"});
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
    document3.put("title", new String[]{"The Third Title, with Gecko savedsearch "});
    document3.put("title_display", new String[]{"The Second Title, with Yak Gecko"});
    document3.put("author", new String[]{"gamma delta"});
    document3.put("body", new String[]{"Contents of the second document: Gecko and Islets of Langerhans"});
    document3.put("everything", new String[]{
        "contents of the second document gecko islets langerhans third title with gecko savedsearch"});
    document3.put("elocation_id", new String[]{"333"});
    document3.put("volume", new String[]{"3"});
    document3.put("doc_type", new String[]{"full"});
    document3.put("publication_date", new String[]{"2008-06-22T00:00:00Z"});
    document3.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document3.put("subject", new String[]{CATEGORY_1});
    document3.put("article_type_facet", new String[]{"Not an issue image"});
    document3.put("author", new String[]{"doc3 author"});
    document3.put("author_display", new String[]{"doc3 creator"});

    // 2 occurrences in "everything": "Yak"
    Map<String, String[]> document4 = new HashMap<String, String[]>();
    document4.put("id", new String[]{DOI_4});
    document4.put("title", new String[]{"The Second Title, with Yak YEk"});
    document4.put("title_display", new String[]{"The Second Title, with Yak YAK"});
    document4.put("author", new String[]{"beta delta epsilon"});
    document4.put("body", new String[]{"Description of the second document: Yak and Islets of Langerhans testing"});
    document4.put("everything", new String[]{
        "description second document yak islets Langerhans second title with yak testing everything debug"});
    document4.put("elocation_id", new String[]{"222"});
    document4.put("volume", new String[]{"2"});
    document4.put("doc_type", new String[]{"full"});
    document4.put("publication_date", new String[]{"2008-06-20T00:00:00Z"});
    document4.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document4.put("subject", new String[]{CATEGORY_2});
    document4.put("article_type_facet", new String[]{"Not an issue image"});
    document4.put("author", new String[]{"doc2 author"});
    document4.put("author_display", new String[]{"doc4 creator"});

    solrServerFactory.addDocument(document1);
    solrServerFactory.addDocument(document2);
    solrServerFactory.addDocument(document3);
    solrServerFactory.addDocument(document4);
  }

  @Test
  public void testWeeklyCron() throws InterruptedException {
    start.sendBody("WEEKLY");

    //WAIT 10 seconds
    //TODO: Check for emails

  }

  @Test
  public void testMonthlyCron() throws InterruptedException {
    start.sendBody("MONTHLY");

    //WAIT 10 seconds
    //TODO: Check for emails
  }


}
