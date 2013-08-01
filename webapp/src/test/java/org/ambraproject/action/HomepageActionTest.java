/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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
package org.ambraproject.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.models.Article;
import org.ambraproject.models.Journal;
import org.ambraproject.views.SearchHit;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.ambraproject.util.Pair;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 2/7/12
 */
public class HomepageActionTest extends AmbraWebTest {
  @Autowired
  protected HomePageAction action;

  @Autowired
  protected EmbeddedSolrServerFactory solr;

  private DateFormat dateFormatter;

  public HomepageActionTest() {
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider(name = "expectedInfo")
  public Object[][] getExpectedInfo() throws Exception {

    //make sure to use a journal for this test, so we don't get 'recent' articles that were added by other unit tests
    Journal journal = new Journal();
    journal.seteIssn("8675-309");
    journal.setJournalKey("HomePageActionTestJournal");
    dummyDataStore.store(journal);

    //create some recent articles and not recent articles w/ dates
    //creating 4 so that the algorithm has to look farther back in time to get them all

    List<Pair<String, String>> recentArticles = new ArrayList<Pair<String, String>>(5);

    Random r = new Random();
    for (int x = 1; x < 5; x++) {
      String doi = new StringBuilder("recent-article-doi-THAT-SHOULD-SHOW-doi").append(x).toString();
      String title = new StringBuilder("recent-article-doi-THAT-SHOULD-SHOW-title").append(x).toString();
      Article a = new Article(doi);
      a.setTitle(title);

      //randomize date - we know a priori recent articles should be within last 7 days; 86400000 milliseconds in a day
      Date d = new Date();
      d.setTime(d.getTime() - d.getTime() % 86400000L);    /* set to midnight */
      d.setTime(d.getTime() - (long) r.nextInt(604800000)); /*some random time within the last 7 days*/
      a.setDate(d);
      a.seteIssn("8675-309");
      dummyDataStore.store(a);

      recentArticles.add(new Pair<String, String>(doi, title));

      //solr
      solr.addDocument(new String[][]{
          {"id", doi},
          {"title_display", title},
          {"publication_date", dateFormatter.format(a.getDate())},
          {"subject_level_1", "Biology"},
          {"article_type_facet", "article"},
          {"doc_type", "full"},
          {"cross_published_journal_key", journal.getJournalKey()}
      });
    }

    //article within date w/ image-type doi that should be discarded on init - in HomePageAction.java:initRecentArticles()
    //"10.1371/image" is the string being searched on for discarding results
    String doi = new String("recent-article-that-SHOULD-NOT-SHOW-10.1371/image");
    String title = new String("recent-article-that-SHOULD-NOT-SHOW title");
    Article a = new Article(doi);
    a.setTitle(title);
    Date d = new Date();
    d.setTime(d.getTime() - (long)r.nextInt(604800000)); /*some random time within the last 7 days*/
    a.setDate(d);
    a.seteIssn("8675-309");
    dummyDataStore.store(a);

    solr.addDocument(new String[][]{
        {"id", doi},
        {"title_display", title},
        {"publication_date", dateFormatter.format(a.getDate())},
        {"subject_level_1", "Biology"},
        {"article_type_facet", "article"},
        {"doc_type", "full"},
        {"cross_published_journal_key", journal.getJournalKey()}
    });


    //article beyond the date that should show up
    a = new Article("not-recent-article-doi-THAT-SHOULD-SHOW-doi");
    a.setTitle("not-recent-article-doi-THAT-SHOULD-SHOW-title");
    d = new Date();
    d.setTime(d.getTime() - 691200000L);    /* set to time outside range */
    a.setDate(d);
    a.seteIssn("8675-309");
    dummyDataStore.store(a);
    recentArticles.add(new Pair<String, String>(a.getDoi(), a.getTitle()));

    solr.addDocument(new String[][]{
        {"id", "not-recent-article-doi-THAT-SHOULD-SHOW-doi"},
        {"title_display", "not-recent-article-doi-THAT-SHOULD-SHOW-title"},
        {"publication_date", dateFormatter.format(a.getDate())},
        {"subject_level_1", "Chemistry"},
        {"article_type_facet", "article"},
        {"doc_type", "full"},
        {"cross_published_journal_key", journal.getJournalKey()}
    });

    //article beyond days to show that should NOT show up
    a = new Article("not-recent-article-doi-that-SHOULD-NOT-show-doi");
    a.setTitle("not-recent-article-doi-that-should-NOT-show-title");
    d = new Date();
    d.setTime(d.getTime() - 950400000L);    /* set to time outside range + search interval used to go back in time*/
    a.setDate(d);
    a.seteIssn("8675-309");
    dummyDataStore.store(a);

    SortedMap<String, Long> subjectCounts = new TreeMap<String, Long>();

    subjectCounts.put("Biology", 5l);
    subjectCounts.put("Chemistry", 1l);

    return new Object[][]{
        {journal, recentArticles, subjectCounts}
    };
  }

  @Test(dataProvider = "expectedInfo")
  public void testExecute(Journal journal, List<Pair<String, String>> expectedRecentArticles, SortedMap<String, Long> expectedCategoryInfos) {
    //make sure to use a journal for this test, so we don't get 'recent' articles that were added by other unit tests
    final Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, new VirtualJournalContext(
        journal.getJournalKey(),
        "dfltJournal",
        "http",
        80,
        "localhost",
        "ambra-webapp",
        new ArrayList<String>()));

    action.setRequest(request);
    final String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionMessages().size(), 0,
        "Action returned messages on default request: " + StringUtils.join(action.getActionMessages(), ";"));
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

    assertEquals(action.getRecentArticles().size(), expectedRecentArticles.size(), "Action returned incorrect number of recent articles");
    for (Pair<String, String> recentArticle : expectedRecentArticles) {
      SearchHit matchingArticle = null;
      for (SearchHit searchHit : action.getRecentArticles()) {
        if (searchHit.getUri().equals(recentArticle.getFirst())) {
          matchingArticle = searchHit;
          break;
        }
      }
      assertNotNull(matchingArticle, "Didn't return expected recent article " + recentArticle.getFirst());
      assertEquals(matchingArticle.getTitle(), recentArticle.getSecond(),
          "Article " + matchingArticle.getUri() + " had incorrect title");
    }

    //category infos are used to put links to the browse by subject pages
    assertEquals(action.getCategoryInfos().size(), expectedCategoryInfos.size(),
        "Action returned incorrect number of category infos");
    for (String category : expectedCategoryInfos.keySet()) {
      assertTrue(action.getCategoryInfos().containsKey(category), "Action didn't return category: " + category);
      assertEquals(action.getCategoryInfos().get(category), expectedCategoryInfos.get(category),
          "Action returned incorrect value for category: " + category);
    }
  }

}
