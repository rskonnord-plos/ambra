/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.ambraproject.service.annotation;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.UserProfile;
import org.ambraproject.views.AnnotationView;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for methods of {@link AnnotationService}.  The test just references methods of the interface so that different
 * implementations can be autowired in.  The test creates it's own dummy data and so shouldn't be dependent on external
 * databases
 *
 * @author Alex Kudlick
 */
public class AnnotationServiceTest extends BaseTest {

  @Autowired
  protected AnnotationService annotationService;

  @DataProvider(name = "articleAnnotations")
  public Object[][] getArticleAnnotations() {
    Article article = new Article("id:doi-for-annotationsTopLevel-test");
    article.setJournal("test journal");
    article.setTitle("test article title");
    article.seteLocationId("1234");
    article.setAuthors(Arrays.asList(new ArticleAuthor("article", "author", "1"), new
        ArticleAuthor("article", "author", "2")));
    article.setDate(Calendar.getInstance().getTime());
    article.setCollaborativeAuthors(Arrays.asList("Collab Author 1", "Collab Author 2", "Collab Author 3"));
    dummyDataStore.store(article);

    UserProfile creator = new UserProfile(
        "email@annotationsTopLevel.org",
        "user-1-annotationsTopLevel",
        "pass");
    dummyDataStore.store(creator);

    //dates to be able to differentiate expected order of results
    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);
    Calendar lastMonth = Calendar.getInstance();
    lastMonth.add(Calendar.MONTH, -1);

    Map<Long, List<Annotation>> replyMap = new HashMap<Long, List<Annotation>>(2);


    Article article1 = new Article("id:doi-for-annotationsTopLevel-test-1");
    article1.setJournal("test journal");
    article1.seteLocationId("1234");
    article1.setTitle("test article1 title");
    article1.setAuthors(Arrays.asList(new ArticleAuthor("article", "author", "1"), new
        ArticleAuthor("article", "author", "2")));
    article1.setDate(Calendar.getInstance().getTime());
    article1.setCollaborativeAuthors(Arrays.asList("Collab Author 1", "Collab Author 2", "Collab Author 3"));
    dummyDataStore.store(article1);


    Article article2 = new Article("id:doi-for-annotationsTopLevel-test-2");
    article2.setJournal("test journal");
    article2.setTitle("test article2 title");
    article2.seteLocationId("1234");
    article2.setAuthors(Arrays.asList(new ArticleAuthor("article", "author", "1"), new
        ArticleAuthor("article", "author", "2")));
    article2.setDate(Calendar.getInstance().getTime());
    article2.setCollaborativeAuthors(Arrays.asList("Collab Author 1", "Collab Author 2", "Collab Author 3"));
    dummyDataStore.store(article2);

    UserProfile creator2 = new UserProfile(
        "email2@annotationsTopLevel.org",
        "user-2-annotationsTopLevel",
        "pass");
    dummyDataStore.store(creator2);

    /**********RETURN ORDER SHOULD BE comment, retraction*******/


    Annotation comment = new Annotation(creator2, AnnotationType.COMMENT, article2.getID());
    comment.setAnnotationUri("comment-annotationsTopLevel-r1");
    comment.setTitle("comment-annotationsTopLevel-r1");
    comment.setBody("comment-annotationsTopLevel-body-r1");
    comment.setCreated(lastMonth.getTime());
    dummyDataStore.store(comment);

    AnnotationView commentView = new AnnotationView(comment, article2.getDoi(), article2.getTitle(), replyMap);
    //Pass in query parameters and expected results for each set of articles / citations
    return new Object[][]{

        {
            article2,
            EnumSet.of(AnnotationType.COMMENT),
            AnnotationService.AnnotationOrder.MOST_RECENT_REPLY,
            new AnnotationView[]{
                commentView,  //comment has more recent created date
            },
            replyMap
        },
        {
            article2,
            EnumSet.of(AnnotationType.COMMENT),
            AnnotationService.AnnotationOrder.OLDEST_TO_NEWEST,
            new AnnotationView[]{

                commentView  //comment has more recent created date
            },
            replyMap
        },

    };
  }

  @Test(dataProvider = "articleAnnotations")
  public void testListAnnotations(
      final Article article,
      final Set<AnnotationType> annotationTypes,
      final AnnotationService.AnnotationOrder order,
      final AnnotationView[] expectedViews,
      final Map<Long, List<Annotation>> fullReplyMap
  ) {
    AnnotationView[] resultViews = annotationService.listAnnotations(article.getID(), annotationTypes, order);
    //Not just calling assertEquals here, so we can give a more informative message (the .toSting() on the arrays is huge)
    assertNotNull(resultViews, "returned null array of results");
    assertEquals(resultViews.length, expectedViews.length, "returned incorrect number of results");
    for (int i = 0; i < resultViews.length; i++) {
      assertEquals(resultViews[i].getCitation(), expectedViews[i].getCitation(),
          "Result " + (i + 1) + " had incorrect citation with order " + order);
      assertEquals(resultViews[i], expectedViews[i], "Result " + (i + 1) + " was incorrect with order " + order);
      recursivelyCheckReplies(resultViews[i], fullReplyMap);
    }

  }

  @Test(dataProvider = "articleAnnotations")
  public void testListAnnotationsNoReplies(
      final Article article,
      final Set<AnnotationType> annotationTypes,
      final AnnotationService.AnnotationOrder order,
      final AnnotationView[] expectedViews,
      final Map<Long, List<Annotation>> notUsed
  ) {
    if (order != AnnotationService.AnnotationOrder.MOST_RECENT_REPLY) {
      AnnotationView[] resultViews = annotationService.listAnnotationsNoReplies(article.getID(), annotationTypes, order);
      //Not just calling assertEquals here, so we can give a more informative message (the .toSting() on the arrays is huge)
      assertNotNull(resultViews, "returned null array of results");
      assertEquals(resultViews.length, expectedViews.length, "returned incorrect number of results");
      for (int i = 0; i < resultViews.length; i++) {
        assertEquals(resultViews[i].getCitation(), expectedViews[i].getCitation(),
            "Result " + (i + 1) + " had incorrect citation with order " + order);
        assertEquals(resultViews[i], expectedViews[i], "Result " + (i + 1) + " was incorrect with order " + order);
        assertTrue(ArrayUtils.isEmpty(resultViews[i].getReplies()), "returned annotation with replies loaded up");
      }
    }
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testListAnnotationNoRepliesWithBadOrderArgument() {
    annotationService.listAnnotationsNoReplies(1231l, null, AnnotationService.AnnotationOrder.MOST_RECENT_REPLY);
  }

  @Test
  public void testCreateComment() throws Exception {
    Article article = new Article("id:doiForCreateCommentByService");
    UserProfile user = new UserProfile(
        "email@createCommentService.org",
        "displayNAmeForCreateCommentService", "pass");
    dummyDataStore.store(article);
    dummyDataStore.store(user);

    String body = "Test body";
    String title = "test title";
    String ciStatement = "ciStatement";
    Long id = annotationService.createComment(user, article.getDoi(), title, body, ciStatement);
    assertNotNull(id, "Returned null annotation id");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, id);
    assertNotNull(storedAnnotation, "didn't store annotation to the db");

    assertEquals(storedAnnotation.getArticleID(), article.getID(), "stored annotation had incorrect article id");
    assertEquals(storedAnnotation.getBody(), body, "stored annotation had incorrect body");
    assertEquals(storedAnnotation.getTitle(), title, "stored annotation had incorrect title");
    assertEquals(storedAnnotation.getCompetingInterestBody(), ciStatement, "stored annotation had incorrect ci statement");
    assertEquals(storedAnnotation.getType(), AnnotationType.COMMENT, "Stored annotation had incorrect type");
    assertNotNull(storedAnnotation.getAnnotationUri(), "Service didn't generate an annotation uri");
  }

  @DataProvider(name = "annotationsByDate")
  public Object[][] getAnnotationsByDate() {
    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    Calendar twoMonthsAgo = Calendar.getInstance();
    twoMonthsAgo.add(Calendar.MONTH, -2);

    Calendar oneMonthAgo = Calendar.getInstance();
    oneMonthAgo.add(Calendar.MONTH, -1);

    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_MONTH, -1);

    UserProfile creator = new UserProfile(
        "email@getAnnotationsByDate.org",
        "displayNameForTestGetAnnotationsByDate",
        "pass");
    dummyDataStore.store(creator);

    Article article1 = new Article("id:doi-for-get-annotations-by-date1");
    article1.setTitle("Article title for testGetAnnotations1");
    article1.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(article1);

    Article article2 = new Article("id:doi-for-get-annotations-by-date2");
    article2.setTitle("Article title for testGetAnnotations2");
    article2.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(article2);

    List<AnnotationView> results = new ArrayList<AnnotationView>(4);
    for (int i = 1; i <= 3; i++) {
      Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, article1.getID());
      annotation.setTitle("comment on " + article1.getDoi() + i);
      annotation.setBody("comment on " + article1.getDoi() + i);
      annotation.setCreated(twoMonthsAgo.getTime());
      dummyDataStore.store(annotation);
      results.add(new AnnotationView(annotation, article1.getDoi(), article1.getTitle(), null));
    }

    return new Object[][]{
        {lastYear.getTime(), oneMonthAgo.getTime(), Arrays.asList("Comment"), 100, defaultJournal.getJournalKey(), results},
    };
  }

  @Test(dataProvider = "annotationsByDate")
  public void testGetAnnotations(Date startDate, Date endDate, Collection<String> annotationTypes,
                                 int maxResults, String journal, List<AnnotationView> expectedResults) throws URISyntaxException, ParseException {
    List<AnnotationView> results = annotationService.getAnnotations(
        startDate, endDate, new HashSet<String>(annotationTypes), maxResults, journal);
    assertNotNull(results, "returned null results");
    assertEqualsNoOrder(results.toArray(), expectedResults.toArray(), "returned incorrect results");
  }

  @Test
  public void testCreateInlineNote() throws Exception {
    Article article = new Article("id:doiForCreateInlineNoteByService");
    UserProfile user = new UserProfile(
        "email@createInlineNoteByService.org",
        "displayNAmeForCreateInlineNoteByService", "pass");
    dummyDataStore.store(article);
    dummyDataStore.store(user);

    String body = "Test body";
    String title = "test title";
    String ciStatement = "ciStatement";
    String expectedXpath = article.getDoi() + "#xpointer(string-range%28%2Farticle%5B1%5D%2Fbody%5B1%5D%2Fsec%5B1%5D%2Fp%5B3%5D%2C+%27%27%2C+107%2C+533%29%5B1%5D)";
    Long id = annotationService.createComment(user, article.getDoi(), title, body, ciStatement);
    assertNotNull(id, "Returned null annotation id");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, id);
    assertNotNull(storedAnnotation, "didn't store annotation to the db");

    assertEquals(storedAnnotation.getArticleID(), article.getID(), "stored annotation had incorrect article id");
    assertEquals(storedAnnotation.getBody(), body, "stored annotation had incorrect body");
    assertEquals(storedAnnotation.getTitle(), title, "stored annotation had incorrect title");
    assertEquals(storedAnnotation.getCompetingInterestBody(), ciStatement, "stored annotation had incorrect ci statement");
    assertEquals(storedAnnotation.getType(), AnnotationType.COMMENT, "Stored annotation had incorrect type");

    assertNotNull(storedAnnotation.getAnnotationUri(), "Service didn't generate an annotation uri");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateCommentWithNullBody() throws Exception {
    Article article = new Article("id:doiForCreateWithNullBody");
    UserProfile user = new UserProfile(
        "email@CreateWithNullBody.org",
        "displayNAmeForCreateWithNullBody", "pass");
    dummyDataStore.store(article);
    dummyDataStore.store(user);

    annotationService.createComment(user, article.getDoi(), "foo", null, "foo");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateCommentWithNullUser() throws Exception {
    Article article = new Article("id:doiForCreateWithNullUser");
    dummyDataStore.store(article);

    annotationService.createComment(null, article.getDoi(), "foo", "foo", "foo");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateCommentWithBadDoi() throws Exception {
    Article article = new Article("id:doiForCreateWithBadDoi");
    UserProfile user = new UserProfile(
        "email@CreateWithBadDoi.org",
        "displayNAmeForCreateWithBadDoi", "pass");
    dummyDataStore.store(user);

    annotationService.createComment(user, article.getDoi(), "foo", "foo", "foo");
  }

  @DataProvider(name = "storedAnnotation")
  public Object[][] getStoredAnnotation() {
    Article article = new Article("id:doi-for-annotationService-test");
    article.setJournal("test journal");
    article.seteLocationId("12340-37951");
    article.setVolume("articl volume");
    article.setTitle("article test title");
    article.setIssue("article issue");
    article.setDate(Calendar.getInstance().getTime());
    article.setCollaborativeAuthors(Arrays.asList("The Skoll Foundation", "The Bill and Melinda Gates Foundation"));
    dummyDataStore.store(article);
    UserProfile creator = new UserProfile(
        "email@annotationService.org",
        "displayNameForAnnotationService", "pass");
    dummyDataStore.store(creator);

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    annotation.setTitle("test title for annotation service test");
    annotation.setBody("test body for annotation service test");
    annotation.setAnnotationUri("id:annotationWithReplies");
    annotation.setAnnotationCitation(new AnnotationCitation(article));
    annotation.getAnnotationCitation().setSummary("Citation summary");
    annotation.getAnnotationCitation().setNote("Citation note");
    dummyDataStore.store(annotation.getAnnotationCitation());
    dummyDataStore.store(annotation);

    /*
       the reply tree structure is ...
                   original
                    /       \
                reply1   reply2
                           /        \
                         reply3  reply4
                                       \
                                       reply5
    */
    Map<Long, List<Annotation>> replies = new HashMap<Long, List<Annotation>>(3);

    List<Annotation> repliesToOriginal = new ArrayList<Annotation>(3);
    List<Annotation> repliesToReply2 = new ArrayList<Annotation>(2);
    List<Annotation> repliesToReply4 = new ArrayList<Annotation>(1);

    replies.put(annotation.getID(), repliesToOriginal);
    //set created dates for replies so we can check ordering by date
    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    //Reply to the original annotation
    Annotation reply1 = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply1.setParentID(annotation.getID());
    reply1.setTitle("title for reply to original comment");
    reply1.setBody("body for reply to original comment");
    reply1.setAnnotationUri("id:reply1ForTestAnnotationView");
    dummyDataStore.store(reply1);
    repliesToOriginal.add(reply1);

    //Reply to the original annotation. This should show up first since it has last year as created date
    Annotation reply2 = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply2.setParentID(annotation.getID());
    reply2.setTitle("title for 2nd reply to original comment");
    reply2.setBody("body for 2nd reply to original comment");
    reply2.setAnnotationUri("id:reply2ForTestAnnotationView");
    reply2.setCreated(lastYear.getTime());
    dummyDataStore.store(reply2);
    repliesToOriginal.add(reply2);

    replies.put(reply2.getID(), repliesToReply2);
    //reply to reply2
    Annotation reply3 = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply3.setParentID(reply2.getID());
    reply3.setTitle("title for 1st reply to reply");
    reply3.setBody("body for 1st reply to reply");
    reply3.setAnnotationUri("id:reply3ForTestAnnotationView");
    dummyDataStore.store(reply3);
    repliesToReply2.add(reply3);

    //reply to reply2. This should show up first since it has last year as created date
    Annotation reply4 = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply4.setParentID(reply2.getID());
    reply4.setTitle("title for 2nd reply to reply");
    reply4.setBody("body for 2nd reply to reply");
    reply4.setAnnotationUri("id:reply4ForTestAnnotationView");
    reply4.setCreated(lastYear.getTime());
    dummyDataStore.store(reply4);
    repliesToReply2.add(reply4);

    replies.put(reply4.getID(), repliesToReply4);

    //reply to reply4
    Annotation reply5 = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply5.setParentID(reply4.getID());
    reply5.setTitle("title for 3rd level reply");
    reply5.setBody("body for 3rd level reply");
    reply5.setAnnotationUri("id:reply5ForTestAnnotationView");
    dummyDataStore.store(reply5);
    repliesToReply4.add(reply5);


    return new Object[][]{
        {annotation, replies},
        {reply1, replies},
        {reply2, replies},
        {reply3, replies},
        {reply4, replies}
    };
  }


  @Test(dataProvider = "storedAnnotation")
  public void testGetFullAnnotationView(Annotation annotation, Map<Long, List<Annotation>> fullReplyMap) {
    AnnotationView result = annotationService.getFullAnnotationView(annotation.getID());
    assertNotNull(result, "Returned null annotation view");

    String expectedDoi = dummyDataStore.get(Article.class, annotation.getArticleID()).getDoi();
    String expectedTitle = dummyDataStore.get(Article.class, annotation.getArticleID()).getTitle();

    assertEquals(result.getArticleDoi(), expectedDoi, "AnnotationView had incorrect article doi");
    assertEquals(result.getArticleTitle(), expectedTitle, "AnnotationView had incorrect article title");

    checkAnnotationProperties(result, annotation);
    recursivelyCheckReplies(result, fullReplyMap);
    //check ordering of replies
    for (int i = 0; i < result.getReplies().length - 1; i++) {
      assertTrue(result.getReplies()[i].getCreated().before(result.getReplies()[i + 1].getCreated()),
          "Replies were out of order for annotation " + annotation.getAnnotationUri() +
              " (should be earliest -> latest)");
    }
  }

  private void recursivelyCheckReplies(AnnotationView annotationView, Map<Long, List<Annotation>> fullReplyMap) {
    List<Annotation> expectedReplies = fullReplyMap.get(annotationView.getID());
    if (expectedReplies != null) {
      assertEquals(annotationView.getReplies().length, expectedReplies.size(), "Returned incorrect number of replies");
      for (AnnotationView actual : annotationView.getReplies()) {
        boolean foundMatch = false;

        for (Annotation expected : expectedReplies) {
          if (actual.getID().equals(expected.getID())) {
            foundMatch = true;
            assertEquals(actual.getTitle(), expected.getTitle(), "Annotation view had incorrect title");
            assertEquals(actual.getBody(), "<p>" + expected.getBody() + "</p>", "Annotation view had incorrect body");
            assertEquals(actual.getCompetingInterestStatement(),
                expected.getCompetingInterestBody() == null ? "" : expected.getCompetingInterestBody(),
                "Annotation view had incorrect ci statement");
            assertEquals(actual.getAnnotationUri(), expected.getAnnotationUri(), "Annotation view had incorrect annotation uri");
          }
        }
        assertTrue(foundMatch, "Returned unexpected reply: " + actual);
        //recursively check the replies
        recursivelyCheckReplies(actual, fullReplyMap);
      }
    } else {
      assertTrue(ArrayUtils.isEmpty(annotationView.getReplies()),
          "Returned replies when none were expected: " + Arrays.deepToString(annotationView.getReplies()));
    }
  }

  @Test
  public void testAnnotationViewEscapesHtml() {
    UserProfile creator = new UserProfile(
        "email@EscapesHtml.org",
        "displayNameForAnnotationViewEscapesHtml",
        "pass");
    dummyDataStore.store(creator);
    Long articleId = Long.valueOf(dummyDataStore.store(new Article("id:doi-for-AnnotationViewEscapesViewHtml")));
    String title = "hello <p /> world!";
    String expectedTitle = "hello &lt;p /&gt; world!";
    String body = "You & I";
    String expectedBody = "<p>You &amp; I</p>";
    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, articleId);
    annotation.setTitle(title);
    annotation.setBody(body);
    dummyDataStore.store(annotation);

    AnnotationView result = annotationService.getFullAnnotationView(annotation.getID());
    assertNotNull(result, "returned null annotation view");
    assertEquals(result.getTitle(), expectedTitle, "AnnotationView didn't escape html in title");
    assertEquals(result.getBody(), expectedBody, "AnnotationView didn't escape html in body");
  }

  @Test(dataProvider = "storedAnnotation")
  public void testGetBasicAnnotationViewById(Annotation annotation, Map<Long, List<Annotation>> fullReplyMap) {
    AnnotationView result = annotationService.getBasicAnnotationView(annotation.getID());
    assertNotNull(result, "Returned null annotation view");
    String expectedDoi = dummyDataStore.get(Article.class, annotation.getArticleID()).getDoi();
    String expectedTitle = dummyDataStore.get(Article.class, annotation.getArticleID()).getTitle();

    assertEquals(result.getArticleDoi(), expectedDoi, "AnnotationView had incorrect article doi");
    assertEquals(result.getArticleTitle(), expectedTitle, "AnnotationView had incorrect article title");

    checkAnnotationProperties(result, annotation);
    assertTrue(ArrayUtils.isEmpty(result.getReplies()), "Returned annotation with replies");
  }

  @Test(dataProvider = "storedAnnotation")
  public void testGetBasicAnnotationViewByURI(Annotation annotation, Map<Long, List<Annotation>> fullReplyMap) {
    AnnotationView result = annotationService.getBasicAnnotationViewByUri(annotation.getAnnotationUri());
    assertNotNull(result, "Returned null annotation view");
    String expectedDoi = dummyDataStore.get(Article.class, annotation.getArticleID()).getDoi();
    String expectedTitle = dummyDataStore.get(Article.class, annotation.getArticleID()).getTitle();

    assertEquals(result.getArticleDoi(), expectedDoi, "AnnotationView had incorrect article doi");
    assertEquals(result.getArticleTitle(), expectedTitle, "AnnotationView had incorrect article title");

    checkAnnotationProperties(result, annotation);
    assertTrue(ArrayUtils.isEmpty(result.getReplies()), "Returned annotation with replies");
  }

  @Test
  public void testCreateFlag() throws Exception {
    UserProfile creator = new UserProfile("email@createFlag.org", "displayNameForCreateFlag", "pass");
    dummyDataStore.store(creator);
    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, 123l);
    Long annotationId = Long.valueOf(dummyDataStore.store(annotation));

    String comment = "This is spam";
    Long flagId = annotationService.createFlag(creator, annotationId, FlagReasonCode.SPAM, comment);
    assertNotNull(flagId, "returned null flag id");
    Flag storedFlag = dummyDataStore.get(Flag.class, flagId);
    assertNotNull(storedFlag, "didn't store flag to the database");
    assertEquals(storedFlag.getReason(), FlagReasonCode.SPAM, "stored flag had incorrect reason code");
    assertEquals(storedFlag.getComment(), comment, "stored flag had incorrect comment");
    assertNotNull(storedFlag.getFlaggedAnnotation(), "stored flag had null annotation");
    assertEquals(storedFlag.getFlaggedAnnotation().getID(), annotationId, "stored flag had incorrect annotation id");
  }

  @Test
  public void testCreateReply() {
    UserProfile creator = new UserProfile(
        "email@createReply.org",
        "displayNameForCreateReply", "pass");
    dummyDataStore.store(creator);
    Article article = new Article("id:doi-for-create-reply");
    Long articleId = Long.valueOf(dummyDataStore.store(article));
    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, articleId);
    Long annotationId = Long.valueOf(dummyDataStore.store(annotation));

    String title = "test title for reply";
    String body = "test body for reply";
    Long replyId = annotationService.createReply(creator, annotationId, title, body, null);
    assertNotNull(replyId, "returned null reply id");
    Annotation storedReply = dummyDataStore.get(Annotation.class, replyId);
    assertNotNull(storedReply, "didn't store reply to the database");
    assertEquals(storedReply.getType(), AnnotationType.REPLY, "Stored reply had incorrect type");
    assertEquals(storedReply.getParentID(), annotation.getID(), "Stored reply had incorrect parent id");
    assertEquals(storedReply.getArticleID(), annotation.getArticleID(), "stored reply had incorrect article id");
    assertNotNull(storedReply.getAnnotationUri(), "reply didn't get an annotation uri generated");
    assertEquals(storedReply.getTitle(), title, "reply had incorrect title");
    assertEquals(storedReply.getBody(), body, "reply had incorrect body");
  }

  @Test
  public void testCountComments() {
    UserProfile user = new UserProfile("email@testCountComments.org", "displayNameTestCountComments", "pass");
    dummyDataStore.store(user);
    Article article = new Article("id:doi-test-count-comments");
    dummyDataStore.store(article);

    Long commentId = Long.valueOf(dummyDataStore.store(new Annotation(user, AnnotationType.COMMENT, article.getID())));

    Annotation reply = new Annotation(user, AnnotationType.REPLY, article.getID());
    reply.setParentID(commentId);
    dummyDataStore.store(reply);

    assertEquals(annotationService.countAnnotations(article.getID(),
        EnumSet.of(AnnotationType.COMMENT)),
        1, "annotation service returned incorrect count of comments and notes");
    assertEquals(annotationService.countAnnotations(article.getID(), EnumSet.allOf(AnnotationType.class)),
        2, "annotation service returned incorrect count of comments and notes");
  }
}
