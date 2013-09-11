package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.SavedSearchRunner;
import org.ambraproject.testutils.EmbeddedSolrServerFactory;
import org.ambraproject.util.TextUtils;
import org.jvnet.mock_javamail.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Joe Osowski
 *
 * Base test for setting up SOLR and DB data
 *
 */
public class SavedSearchRouteBaseTest extends BaseTest {
  private static final Logger log = LoggerFactory.getLogger(SavedSearchRouteBaseTest.class);

  @Autowired
  protected EmbeddedSolrServerFactory solrServerFactory;

  @Autowired
  protected SavedSearchRunner savedSearchRunner;

  protected static final String DOI_1         = "10.1371/journal.pone.1002222";
  protected static final String DOI_2         = "10.1371/journal.pmed.1002223";
  protected static final String DOI_3         = "10.1371/journal.pone.1002224";
  protected static final String DOI_4         = "10.1371/journal.pmed.1002225";
  protected static final String JOURNAL_KEY_1 = "PLoSONE";
  protected static final String JOURNAL_KEY_2 = "PLoSMedicine";
  protected static final String CATEGORY_1    = "Category1";
  protected static final String CATEGORY_2    = "Category2";

  protected void setupUsers() throws Exception {
    //Let's make sure the database is clean
    dummyDataStore.deleteAll(SavedSearch.class);
    dummyDataStore.deleteAll(SavedSearchQuery.class);
    dummyDataStore.deleteAll(UserProfile.class);

    Calendar searchTime = Calendar.getInstance();
    searchTime.set(Calendar.YEAR, 2007);
    searchTime.set(Calendar.MONTH, 5);
    searchTime.set(Calendar.DAY_OF_MONTH, 15);

    String query1 = "{\"query\":\"test\",\"unformattedQuery\":\"\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSONE\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query2 = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query3 = "{\"query\":\"\",\"unformattedQuery\":\"everything:debug\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";

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

  protected void checkEmail(String email, int expectedEmails, Map emailContents) throws Exception {
    List<Message> inboxMessages = Mailbox.get(email);
    int subjectsMatched = 0;

    log.debug("Inbox Size ({}): {} Expected: {}", new Object[] { email, inboxMessages.size(), expectedEmails });

    assertEquals(inboxMessages.size(), expectedEmails, "Inbox sizes off");

    //Now we check that the subjects are correct and also check the content of the mail
    //for the correct DOIs

    for(Message m : inboxMessages) {
      String subject = m.getSubject();
      MimeMultipart contents = (MimeMultipart)m.getContent();

      assertTrue(emailContents.containsKey(subject), "Email subject wrong: '" + subject + "'");
      log.debug("Email subject: '{}' Correct", subject);
      subjectsMatched++;

      //Check both the HTML and text versions of the contents
      for(int a = 0; a < contents.getCount(); a++) {
        BodyPart bp = contents.getBodyPart(a);
        String body = getContent(bp.getContent());
        int doisFound = 0;

        log.debug("Checking content: '{}'", bp.getContentType());

        //Check DOIS for existence
        String[] dois = (String[])emailContents.get(subject);

        for(String doi : dois) {
          int foundAt = body.indexOf(doi);

          assertTrue(foundAt > 0, "DOI '" + doi + "' not found in message '" + subject + "', or not found in correct order");

          log.debug("DOI: {} found in email body", doi);

          //For the next search, start where this one ended
          //To assert that order is correct
          body = body.substring(foundAt + doi.length());

          doisFound++;
        }

        assertEquals(dois.length, doisFound, "Not all DOIs found in sent email");
      }
    }

    //Multiply the size by two as each email is matched
    assertEquals(emailContents.size(), subjectsMatched, "Email message subjects likely wrong, " +
      "not all emails defined where sent");
  }

  /**
   * Get a string of the content of the email wether the part is text or HTML
   */
  protected String getContent(Object content) throws Exception {
    if(content instanceof String) {
      return (String) content;
    }

    if(content instanceof MimeMultipart) {
      ByteArrayOutputStream s = new ByteArrayOutputStream();
      MimeMultipart multiPart = (MimeMultipart)content;
      BodyPart bodyPart = multiPart.getBodyPart(0);

      bodyPart.writeTo(s);

      return s.toString();
    }

    throw new Exception("Unknown content type: " + content.getClass());
  }

  @AfterClass
  public void restoreUserData() {
    restoreDefaultUsers();
  }
}
