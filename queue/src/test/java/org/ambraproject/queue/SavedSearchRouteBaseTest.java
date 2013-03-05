package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.SavedSearchRunner;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.ambraproject.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joe Osowski
 *
 * Base test for setting up SOLR and DB data
 *
 */
public class SavedSearchRouteBaseTest extends BaseTest {
  @Autowired
  protected EmbeddedSolrServerFactory solrServerFactory;

  @Autowired
  protected SavedSearchRunner savedSearchRunner;

  private static final String DOI_1         = "10.1371/journal.pone.1002222";
  private static final String DOI_2         = "10.1371/journal.pmed.1002223";
  private static final String DOI_3         = "10.1371/journal.pone.1002224";
  private static final String DOI_4         = "10.1371/journal.pmed.1002225";
  private static final String JOURNAL_KEY_1 = "PLoSONE";
  private static final String JOURNAL_KEY_2 = "PLoSMedicine";
  private static final String CATEGORY_1    = "Category1";
  private static final String CATEGORY_2    = "Category2";

  protected void setupUsers() throws Exception {
    Calendar searchTime = Calendar.getInstance();
    searchTime.set(Calendar.YEAR, 2007);
    searchTime.set(Calendar.MONTH, 5);
    searchTime.set(Calendar.DAY_OF_MONTH, 15);

    String query1 = "{\"query\":\"test\",\"unformattedQuery\":\"\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSONE\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query2 = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query3 = "{\"query\":\"\",\"unformattedQuery\":\"everything:debug\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";

    SavedSearchQuery ssq1 = new SavedSearchQuery(query1, TextUtils.createHash(query1));
    dummyDataStore.store(ssq1);

    SavedSearchQuery ssq2 = new SavedSearchQuery(query2, TextUtils.createHash(query2));
    dummyDataStore.store(ssq2);

    SavedSearchQuery ssq3 = new SavedSearchQuery(query3, TextUtils.createHash(query3));
    dummyDataStore.store(ssq3);

    for (int i = 0; i < 3; i++) {
      UserProfile user = new UserProfile("savedSearch" + i + "@unittestexample.org", "savedSearch-" + i, "savedSearch" + i);

      SavedSearch savedSearch1 = new SavedSearch("weekly-" + i, ssq1);
      savedSearch1.setWeekly(true);
      savedSearch1.setMonthly(false);
      savedSearch1.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch1.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch1.setLastMonthlySearchTime(searchTime.getTime());

      SavedSearch savedSearch2 = new SavedSearch("monthly-" + i, ssq2);
      savedSearch2.setWeekly(false);
      savedSearch2.setMonthly(true);
      savedSearch2.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch2.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch2.setLastMonthlySearchTime(searchTime.getTime());

      SavedSearch savedSearch3 = new SavedSearch("both-" + i, ssq3);
      savedSearch3.setWeekly(true);
      savedSearch3.setMonthly(true);
      savedSearch3.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch3.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch3.setLastMonthlySearchTime(searchTime.getTime());

      user.setSavedSearches(Arrays.asList(savedSearch1, savedSearch2, savedSearch3));

      dummyDataStore.store(user);
    }
  }

  protected void setupSolr() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //5 days into the past
    Calendar topdayMinus5 = Calendar.getInstance();
    topdayMinus5.add(Calendar.DAY_OF_MONTH, -5);

    Map<String, String[]> document1 = new HashMap<String, String[]>();
    document1.put("id", new String[]{DOI_1});
    document1.put("title", new String[]{"The First Title"});
    document1.put("title_display", new String[]{"The First Title"});
    document1.put("author", new String[]{"author name"});
    document1.put("body", new String[]{"Body of the document"});
    document1.put("everything", new String[]{"contents of the document test testing debug"});
    document1.put("elocation_id", new String[]{"111"});
    document1.put("volume", new String[]{"1"});
    document1.put("doc_type", new String[]{"full"});
    document1.put("publication_date", new String[]{sdf.format(topdayMinus5.getTime()) + "T00:00:00Z"});
    document1.put("cross_published_journal_key", new String[]{JOURNAL_KEY_1});
    document1.put("subject", new String[]{CATEGORY_1, CATEGORY_2});
    document1.put("article_type_facet", new String[]{"Not an issue image"});
    document1.put("author", new String[]{"doc1 author"});
    document1.put("author_display", new String[]{"doc1 creator"});

    Map<String, String[]> document2 = new HashMap<String, String[]>();
    document2.put("id", new String[]{DOI_2});
    document2.put("title", new String[]{"The Second Title"});
    document2.put("title_display", new String[]{"The Second Title"});
    document2.put("author", new String[]{"author name"});
    document2.put("body", new String[]{"Body of the document"});
    document2.put("everything", new String[]{"contents of the document test testing debug"});
    document2.put("elocation_id", new String[]{"222"});
    document2.put("volume", new String[]{"2"});
    document2.put("doc_type", new String[]{"full"});
    document2.put("publication_date", new String[]{sdf.format(topdayMinus5.getTime()) + "T00:00:00Z"});
    document2.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document2.put("subject", new String[]{CATEGORY_2});
    document2.put("article_type_facet", new String[]{"Not an issue image"});
    document2.put("author", new String[]{"doc2 author"});
    document2.put("author_display", new String[]{"doc2 creator"});

    Map<String, String[]> document3 = new HashMap<String, String[]>();
    document3.put("id", new String[]{DOI_3});
    document3.put("title", new String[]{"The Third Title"});
    document3.put("title_display", new String[]{"The Third Title"});
    document3.put("author", new String[]{"author name"});
    document3.put("body", new String[]{"Body of the document"});
    document3.put("everything", new String[]{"contents of the document test testing debug"});
    document3.put("elocation_id", new String[]{"333"});
    document3.put("volume", new String[]{"3"});
    document3.put("doc_type", new String[]{"full"});
    document3.put("publication_date", new String[]{sdf.format(topdayMinus5.getTime()) + "T00:00:00Z"});
    document3.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document3.put("subject", new String[]{CATEGORY_1});
    document3.put("article_type_facet", new String[]{"Not an issue image"});
    document3.put("author", new String[]{"doc3 author"});
    document3.put("author_display", new String[]{"doc3 creator"});

    // 2 occurrences in "everything": "Yak"
    Map<String, String[]> document4 = new HashMap<String, String[]>();
    document4.put("id", new String[]{DOI_4});
    document4.put("title", new String[]{"The Fourth Title"});
    document4.put("title_display", new String[]{"The Fourth Title"});
    document4.put("author", new String[]{"author name"});
    document4.put("body", new String[]{"Body of the document"});
    document4.put("everything", new String[]{"contents of the document test testing debug"});
    document4.put("elocation_id", new String[]{"222"});
    document4.put("volume", new String[]{"2"});
    document4.put("doc_type", new String[]{"full"});
    document4.put("publication_date", new String[]{sdf.format(topdayMinus5.getTime()) + "T00:00:00Z"});
    document4.put("cross_published_journal_key", new String[]{JOURNAL_KEY_2});
    document4.put("subject", new String[]{CATEGORY_2});
    document4.put("article_type_facet", new String[]{"Not an issue image"});
    document4.put("author", new String[]{"doc4 author"});
    document4.put("author_display", new String[]{"doc4 creator"});

    solrServerFactory.addDocument(document1);
    solrServerFactory.addDocument(document2);
    solrServerFactory.addDocument(document3);
    solrServerFactory.addDocument(document4);
  }
}
