/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.topazproject.ambra.article.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.model.MockBlob;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.rating.action.ArticleRatingSummary;

import javax.activation.DataSource;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.*;

/**
 * @author Alex Kudlick Date: 5/9/11
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
public class ArticlePersistenceServiceTest extends BaseTest {

  @Autowired
  protected ArticlePersistenceService articlePersistenceService;
  @Autowired
  protected SessionFactory sessionFactory;

  @DataProvider(name = "savedArticles")
  public Object[][] savedArticles() {
    Article article1 = new Article();
    DublinCore db1 = new DublinCore();
    db1.setIdentifier("info:doi/10.1371/test-article1/dc");

    article1.setId(URI.create("info:doi/10.1371/test-article1"));
    article1.setArchiveName("Archive 1");
    article1.setContextElement("some fake context element");
    article1.seteIssn("fake eIssn");
    article1.setState(1);
    article1.setDublinCore(db1);
    article1.setState(Article.STATE_ACTIVE);

    Article article2 = new Article();
    DublinCore db2 = new DublinCore();
    db2.setIdentifier("info:doi/10.1371/test-article2/dc");

    article2.setId(URI.create("info:doi/10.1371/test-article2"));
    article2.setArchiveName("Archive 2");
    article2.setContextElement("another fake context element");
    article2.seteIssn("fake eIssn number 2");
    article2.setState(1);
    article2.setDublinCore(db2);
    article2.setState(Article.STATE_ACTIVE);

    dummyDataStore.store(db1);
    dummyDataStore.store(db2);

    return new Object[][]{
        {dummyDataStore.store(article1), article1},
        {dummyDataStore.store(article2), article2}
    };
  }

  @DataProvider(name = "savedObjectInfos")
  public Object[][] savedObjectInfos() {
    Article article = new Article(); //need an article for the obj info to be a part of
    article.setDublinCore(new DublinCore());
    dummyDataStore.store(article.getDublinCore());
    dummyDataStore.store(article);
    ObjectInfo objectInfo1 = new ObjectInfo();
    objectInfo1.setId(URI.create("info:doi/10.1371/test-obj-for-getting"));
    objectInfo1.setContextElement("fake context element");
    objectInfo1.seteIssn("fake eIssn");
    objectInfo1.setIsPartOf(article);

    return new Object[][]{
        {dummyDataStore.store(objectInfo1), objectInfo1}
    };
  }

  @DataProvider(name = "savedArticleList")
  public Object[][] savedArticleList() {
    Object[][] articles = savedArticles();
    List<String> ids = new ArrayList<String>();
    List<Article> articleList = new ArrayList<Article>();
    for (Object[] article : articles) {
      ids.add((String) article[0]);
      articleList.add((Article) article[1]);
    }

    return new Object[][]{
        {ids, articleList}
    };
  }

  @Test(dataProvider = "savedArticles", groups = {"savedArticles"})
  public void testGetArticle(String articleId, Article expectedArticle) throws NoSuchArticleIdException {
    Article article = articlePersistenceService.getArticle(URI.create(articleId), DEFAULT_ADMIN_AUTHID);
    assertNotNull(article, "returned null article");
    assertEquals(article.getId(), URI.create(articleId), "returned article with incorrect id");
    compareBasicArticleProperties(article, expectedArticle);
  }

  @Test(dataProvider = "savedObjectInfos")
  public void testGetObjectInfo(String id, ObjectInfo expected) throws NoSuchObjectIdException {
    ObjectInfo objectInfo = articlePersistenceService.getObjectInfo(id, DEFAULT_ADMIN_AUTHID);
    assertNotNull(objectInfo, "returned null object info");
    assertEquals(objectInfo.getId(), URI.create(id), "returned objectInfo with incorrect id");
    compareBasicObjectInfoProperties(objectInfo, expected);
  }

  @Test(dataProvider = "savedArticleList", groups = {"savedArticles"})
  public void testGetArticles(List<String> articleIds, List<Article> expectedArticles)
    throws ParseException, NoSuchArticleIdException {
    List<Article> actualArticles = articlePersistenceService.getArticles(articleIds, DEFAULT_ADMIN_AUTHID);
    assertNotNull(actualArticles, "returned null list of articles");
    assertEquals(actualArticles.size(), articleIds.size(), "Didn't return correct number of articles");
    for (int i = 0; i < actualArticles.size(); i++) {
      assertEquals(actualArticles.get(i).getId(), URI.create(articleIds.get(i)), "Returned incorrect id");
      compareBasicArticleProperties(actualArticles.get(i), expectedArticles.get(i));
    }
  }

  //want to make sure to run this after methods that check the stored articles, but before the delete test
  @Test(dataProvider = "savedArticles",
      groups = {"updateArticles"}, dependsOnGroups = {"savedArticles"}, alwaysRun = true)
  public void testSetState(String articleId, Article notUsed) throws NoSuchArticleIdException {
    int state = 2139874;
    articlePersistenceService.setState(articleId, DEFAULT_ADMIN_AUTHID, state);
    // if the article state isn't ACTIVE, the article will not be returned from the articlePersistenceService.getArticle
    Session session = sessionFactory.openSession();
    Article article = (Article) session.get(Article.class, URI.create(articleId));
    assertEquals(article.getState(), state, "didn't set state on article");
    session.close();
  }

  //TODO current article delete functionality uses sql statements that will not run against hsql.
  // If and when article delete functionality changes, we should re-enable the unit test
  //Want to run this last
//  @Test(dataProvider = "savedArticles", dependsOnGroups = {"savedArticles", "updateArticles"}, alwaysRun = true)
//  public void testDeleteArticle(String articleId, Article notUsed) throws NoSuchArticleIdException {
//    articlePersistenceService.delete(articleId, DEFAULT_ADMIN_AUTHID);
//    try {
//      articlePersistenceService.getArticle(URI.create(articleId));
//      fail("articlePersistenceService should throw NoSuchArticleIdException on deleted article: " + articleId);
//    } catch (NoSuchArticleIdException e) {
//      //expected
//    }
//  }

  @DataProvider(name = "startAndEndDates")
  public Object[][] startAndEndDates() throws ParseException {

    //Categories
    Set<Category> oneCategorySet = new HashSet<Category>();
    Set<Category> categorySet = new HashSet<Category>();

    Category biology = new Category();
    biology.setMainCategory("biology");
    dummyDataStore.store(biology);

    Category econ = new Category();
    econ.setMainCategory("economics");
    dummyDataStore.store(econ);

    oneCategorySet.add(biology);
    categorySet.add(econ);
    categorySet.add(biology);

    //Authors
    List<ArticleContributor> authors = new ArrayList<ArticleContributor>();
    ArticleContributor author1 = new ArticleContributor();
    author1.setFullName("Hilary Putnam");
    authors.add(author1);
    ArticleContributor author2 = new ArticleContributor();
    author2.setFullName("Michel Foucault");
    authors.add(author2);
    dummyDataStore.store(authors);

    //Create dates
    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    Date twoThousandNine = dateFormatter.parse("2009-02-03");
    Date twoThousandTen = dateFormatter.parse("2010-03-03");
    Date twoThousandEleven = dateFormatter.parse("2011-06-23");

    //Articles
    Article article = new Article();
    article.setId(URI.create("info:doi/10.1371/test-article-for-dates1"));
    article.setState(1);
    article.setCategories(categorySet);
    article.setDublinCore(new DublinCore());
    article.setAuthors(authors);
    article.getDublinCore().setCreated(twoThousandNine);
    dummyDataStore.store(article.getDublinCore());
    dummyDataStore.store(article);

    Article noCategories = new Article();
    noCategories.setId(URI.create("info:doi/10.1371/test-article-for-dates2"));
    noCategories.setState(1);
    noCategories.setDublinCore(new DublinCore());
    noCategories.setAuthors(authors);
    noCategories.getDublinCore().setCreated(twoThousandTen);
    dummyDataStore.store(noCategories.getDublinCore());
    dummyDataStore.store(noCategories);

    Article oneCategory = new Article();
    oneCategory.setId(URI.create("info:doi/10.1371/test-article-for-dates-with-1-category"));
    oneCategory.setState(0);
    oneCategory.setDublinCore(new DublinCore());
    oneCategory.setAuthors(authors);
    oneCategory.getDublinCore().setCreated(twoThousandTen);
    oneCategory.setCategories(oneCategorySet);
    dummyDataStore.store(oneCategory.getDublinCore());
    dummyDataStore.store(oneCategory);

    Article oneAuthor = new Article();
    oneAuthor.setId(URI.create("info:doi/10.1371/test-article-for-dates3"));
    oneAuthor.setState(0);
    oneAuthor.setCategories(categorySet);
    oneAuthor.setAuthors(authors.subList(0,1));
    oneAuthor.setDublinCore(new DublinCore());
    oneAuthor.getDublinCore().setCreated(twoThousandNine);
    dummyDataStore.store(oneAuthor.getDublinCore());
    dummyDataStore.store(oneAuthor);


    Article noAuthors = new Article();
    noAuthors.setId(URI.create("info:doi/10.1371/test-article-for-dates4"));
    noAuthors.setState(0);
    noAuthors.setCategories(categorySet);
    noAuthors.setDublinCore(new DublinCore());
    noAuthors.getDublinCore().setCreated(twoThousandEleven);
    dummyDataStore.store(noAuthors.getDublinCore());
    dummyDataStore.store(noAuthors);

    Calendar start = Calendar.getInstance();
    start.add(Calendar.SECOND, -10);
    Calendar end = Calendar.getInstance();
    end.add(Calendar.SECOND, 1);

    String[] allCategories = new String[]{"biology", "economics"};
    String[] oneCategoryArray = new String[]{"biology"};
    String[] authorArray = new String[authors.size()];
    for (int i = 0; i < authors.size(); i++) {
      authorArray[i] = authors.get(i).getFullName();
    }
    String[] oneAuthorArray = Arrays.copyOf(authorArray, 1);
    int[] allStates = new int[]{0, 1};
    int[] oneState = new int[]{1};

    return new Object[][]{
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, null, null, 5, "no filter"},
        {twoThousandTen.toString(), twoThousandEleven.toString(), null, null, null, 3, "no filter"},
        {twoThousandEleven.toString(), twoThousandEleven.toString(), null, null, null, 1, "dates only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), allStates, null, null, 5, "filter on states only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), oneState, null, null, 2, "filter on states only"},
        {twoThousandNine.toString(), null, null, null, null, 5, "no filter, no end date"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, allCategories, authorArray, 1, "categories and authors"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, null, authorArray, 3, "authors only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, null, oneAuthorArray, 4, "one author only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, allCategories, null, 3, "categories only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, oneCategoryArray, null, 4, "one category only"},
        {twoThousandNine.toString(), twoThousandEleven.toString(), null, oneCategoryArray, authorArray, 2, "one category, filter by authors too"}
    };
  }


  @Test(dataProvider = "startAndEndDates", groups = {"savedArticles"})
  public void testGetArticleIdsByDate(String startDate, String endDate, int[] states,
                                      String[] categories, String[] authors,
                                      int expectedNumberOfResults, String filterMessage)
      throws ParseException {
    List<String> results = articlePersistenceService.getArticleIds(
        startDate, //start date
        endDate,   //end date
        categories,      //categories - null for no filter
        authors,      //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_DATE_CREATED, //orderField - null for no ordering
        false, //isOrderAscending
        100);        //limit - 0 for no limit

    assertNotNull(results, "returned null list of ids");
    assertEquals(results.size(), expectedNumberOfResults, "Returned incorrect number of results" +
        " when filtering on: " + filterMessage);
  }

  @Test(dataProvider = "startAndEndDates", groups = {"savedArticles"},
      dependsOnMethods = {"testGetArticleIdsByDate"}, alwaysRun = true)
  public void testGetArticlesByDate(String startDate, String endDate, int[] states,
                                    String[] categories, String[] authors,
                                    int expectedNumberOfResults, String filterMessage)
      throws ParseException {
    List<Article> results = articlePersistenceService.getArticles(
        startDate, //start date
        endDate, //end date
        categories, //categories - null for no filter
        authors, //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_DATE_CREATED, //orderField - null for no ordering
        false, //isOrderAscending
        100); //limit - 0 for no limit

    assertNotNull(results, "returned null list of articles");
    assertEquals(results.size(), expectedNumberOfResults, "returned incorrect number of articles" +
        " when filtering on: " + filterMessage);
    for (Article article : results) {
      assertNotNull(article, "returned null article");
    }
  }

  @Test(dataProvider = "startAndEndDates", groups = {"savedArticles"},
      dependsOnMethods = {"testGetArticlesByDate"}, alwaysRun = true)
  public void testGetArticlesWithOrdering(String startDate, String endDate, int[] states,
                                          String[] categories, String[] authors,
                                          int expectedNumberOfResults, String filterMessage)
      throws ParseException {
    List<Article> results = articlePersistenceService.getArticles(
        startDate, //start date
        endDate, //end date
        categories, //categories - null for no filter
        authors, //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_DATE_CREATED, //orderField - null for no ordering
        true, //isOrderAscending
        100); //limit - 0 for no limit
    Date prevDate = null;
    for (Article article : results) {
      if (prevDate != null) {
        assertTrue(prevDate.compareTo(article.getDublinCore().getCreated()) <= 0,
            "Articles weren't ordered correctly by date ascending");
      }
      prevDate = article.getDublinCore().getCreated();
    }
    results = articlePersistenceService.getArticles(
        startDate, //start date
        endDate, //end date
        categories, //categories - null for no filter
        authors, //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_DATE_CREATED, //orderField - null for no ordering
        false, //isOrderAscending
        100); //limit - 0 for no limit
    prevDate = null;
    for (Article article : results) {
      if (prevDate != null) {
        assertTrue(prevDate.compareTo(article.getDublinCore().getCreated()) >= 0,
            "Articles weren't ordered correctly by date descending");
      }
      prevDate = article.getDublinCore().getCreated();
    }
    results = articlePersistenceService.getArticles(
        startDate, //start date
        endDate, //end date
        categories, //categories - null for no filter
        authors, //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_ARTICLE_DOI, //orderField - null for no ordering
        true, //isOrderAscending
        100); //limit - 0 for no limit
    URI prevId = null;
    for (Article article : results) {
      if (prevId != null) {
        assertTrue(prevId.compareTo(article.getId()) <= 0,
            "Articles weren't ordered correctly by doi asc");
      }
      prevId = article.getId();
    }
    results = articlePersistenceService.getArticles(
        startDate, //start date
        endDate, //end date
        categories, //categories - null for no filter
        authors, //authors - null for no filter
        null,
        states,      //states - null for no filter
        ArticlePersistenceService.ORDER_BY_FIELD_ARTICLE_DOI, //orderField - null for no ordering
        false, //isOrderAscending
        100); //limit - 0 for no limit
    prevId = null;
    for (Article article : results) {
      if (prevId != null) {
        assertTrue(prevId.compareTo(article.getId()) >= 0,
            "Articles weren't ordered correctly by doi asc");
      }
      prevId = article.getId();
    }
  }

  @DataProvider(name = "ratingSummaries")
  public Object[][] ratingSummaries() {
    Article article = (Article) savedArticles()[0][1];

    RatingSummary ratingSummary = new RatingSummary();
    ratingSummary.setAnnotates(article.getId());
    ratingSummary.setAnonymousCreator("anonymous creator 1");
    ratingSummary.setContext("test context");
    ratingSummary.setCreated(new Date());

    RatingSummary ratingSummary2 = new RatingSummary();
    ratingSummary2.setAnnotates(article.getId());
    ratingSummary2.setCreated(new Date());
    ratingSummary2.setContext("test context 2");

    return new Object[][]{
        {article.getId(), new String[]{dummyDataStore.store(ratingSummary), dummyDataStore.store(ratingSummary2)}}
    };
  }

  @Test(dataProvider = "ratingSummaries", groups = {"savedArticles"})
  public void testGetRatingSummaries(URI articleUri, String[] expectedIds) {
    List<RatingSummary> ratingSummaries = articlePersistenceService.getRatingSummaries(articleUri);
    assertNotNull(ratingSummaries, "returned null list of rating summaries");
    assertEquals(ratingSummaries.size(), expectedIds.length, "didn't return correct number of rating summaries");
    String[] actualIds = new String[ratingSummaries.size()];
    for (int i = 0; i < actualIds.length; i++) {
      actualIds[i] = ratingSummaries.get(i).getId().toString();
    }
    assertEqualsNoOrder(actualIds, expectedIds);
  }

  @DataProvider(name = "ratings")
  public Object[][] ratings() {
    Article article = (Article) savedArticles()[0][1];

    Rating rating1 = new Rating();
    rating1.setAnnotates(article.getId());
    rating1.setAnonymousCreator("anonymous creator 1");
    rating1.setContext("test context");
    rating1.setCreated(new Date());
    rating1.setBody(new RatingContent());
    rating1.setCreator("id://rating-creator");

    Rating rating2 = new Rating();
    rating2.setAnnotates(article.getId());
    rating2.setCreated(new Date());
    rating2.setContext("test context 2");
    rating2.setBody(new RatingContent());
    rating2.setCreator("id://rating-creator");

    UserProfile up = new UserProfile();
    up.setRealName("Foo user");

    UserAccount ua = new UserAccount();
    ua.setId(URI.create("id://rating-creator"));
    ua.setProfile(up);

    dummyDataStore.store(up);
    dummyDataStore.store(ua);

    return new Object[][]{
        {article.getId(), new String[]{dummyDataStore.store(rating1), dummyDataStore.store(rating2)}}
    };
  }

  @Test(dataProvider = "ratings", groups = {"savedArticles"})
  public void testGetArticleRatingSummaries(URI articleUri, String[] expectedIds) {
    List<ArticleRatingSummary> ratingSummaries = articlePersistenceService.getArticleRatingSummaries(articleUri);
    assertNotNull(ratingSummaries, "returned null list of rating summaries");

    assertEquals(ratingSummaries.size(), expectedIds.length, "didn't return correct number of rating summaries");

    String[] actualIds = new String[ratingSummaries.size()];

    for (int i = 0; i < ratingSummaries.size(); i++) {
      ArticleRatingSummary articleRatingSummary = ratingSummaries.get(i);
      assertEquals(articleRatingSummary.getArticleURI(), articleUri.toString(),
          "returned rating summary for incorrect article");
      actualIds[i] = articleRatingSummary.getRatingId();
    }
    assertEqualsNoOrder(actualIds, expectedIds, "Didn't return correct ratings");
  }

  @DataProvider(name = "content")
  public Object[][] objectsWithContent() {
    Article article = new Article(); //Need something for the objectInfos to be part of
    article.setId(URI.create("id:test-article-for-content"));
    article.setDublinCore(new DublinCore());
    dummyDataStore.store(article.getDublinCore());
    dummyDataStore.store(article);

    ObjectInfo objectInfo = new ObjectInfo();
    objectInfo.setId(URI.create("info:doi/10.1371/test-obj-for-content"));
    objectInfo.setIsPartOf(article);
    String id = dummyDataStore.store(objectInfo);

    Representation rep1 = new Representation(objectInfo, "test-rep1");
    rep1.setContentType("text/html");
    rep1.setObject(objectInfo);
    rep1.setBody(new MockBlob("id://test-blob-1"));
    dummyDataStore.store(rep1);

    Representation rep2 = new Representation(objectInfo, "test-rep2");
    rep2.setContentType("text/plain");
    rep2.setObject(objectInfo);
    rep2.setBody(new MockBlob("id://test-blob-2"));
    dummyDataStore.store(rep2);

    Set<Representation> representations = new HashSet<Representation>();
    representations.add(rep1);
    representations.add(rep2);
    objectInfo.setRepresentations(representations);

    dummyDataStore.update(objectInfo);

    return new Object[][]{
        {id, "test-rep1", "text/html"},
        {id, "test-rep2", "text/plain"}
    };
  }

  @Test(dataProvider = "content", groups = {"savedArticles"})
  public void testGetContent(String id, String rep, String expectedContentType) throws NoSuchObjectIdException,
      NoSuchArticleIdException {
    DataSource content = articlePersistenceService.getContent(id, rep, DEFAULT_ADMIN_AUTHID);
    assertNotNull(content, "returned null content");
    assertEquals(content.getContentType(), expectedContentType, "returned content with incorrect content type");
    assertEquals(content.getName(), id + "#" + rep, "returned content with incorrect name");
  }

  @DataProvider(name = "trackbacks")
  public Object[][] trackbacks() {
    Article article1 = (Article) savedArticles()[0][1];
    Article article2 = (Article) savedArticles()[1][1];
    Trackback trackback1 = new Trackback();
    trackback1.setAnnotates(article1.getId());
    Trackback trackback2 = new Trackback();
    trackback2.setAnnotates(article1.getId());
    Trackback trackback3 = new Trackback();
    trackback3.setAnnotates(article2.getId());

    String id1 = article1.getId().toString();
    String id2 = article2.getId().toString();
    return new Object[][]{
        {id1, new String[]{dummyDataStore.store(trackback1), dummyDataStore.store(trackback2)}},
        {id2, new String[]{dummyDataStore.store(trackback3)}}
    };
  }

  @Test(dataProvider = "trackbacks", groups = {"savedArticles"})
  public void testGetTrackbacks(String articleId, String[] expectdTrackbackIds) {
    List<Trackback> trackbacks = articlePersistenceService.getTrackbacks(articleId);
    assertNotNull(trackbacks, "returned null list of trackbacks");
    assertEquals(trackbacks.size(), expectdTrackbackIds.length, "returned incorrect number of trackbacks");
    String[] actualIds = new String[trackbacks.size()];
    for (int i = 0; i < trackbacks.size(); i++) {
      actualIds[i] = trackbacks.get(i).getId().toString();
    }

    assertEqualsNoOrder(actualIds, expectdTrackbackIds);

  }

  @DataProvider(name = "articlesWithParts")
  public Object[][] articlesWithParts() {
    Article article1 = new Article();
    article1.setId(URI.create("id://test-art-with-parts1"));
    article1.setDublinCore(new DublinCore());
    dummyDataStore.store(article1.getDublinCore());
    String articleId = dummyDataStore.store(article1);

    List<ObjectInfo> parts = new ArrayList<ObjectInfo>();
    ObjectInfo part1 = new ObjectInfo();
    part1.setId(URI.create("id://test-part1"));
    part1.setContextElement("fig"); //figure
    part1.setIsPartOf(article1);

    ObjectInfo part2 = new ObjectInfo();
    part2.setId(URI.create("id://test-part2"));
    part2.setContextElement("context-element");
    part2.setIsPartOf(article1);

    parts.add(part1);
    parts.add(part2);

    dummyDataStore.store(parts);
    article1.setParts(parts);
    dummyDataStore.update(article1);

    return new Object[][]{
        {articleId}
    };
  }

  @Test(dataProvider = "articlesWithParts")
  public void testListSecondaryObjects(String articleId) throws NoSuchArticleIdException {
    SecondaryObject[] secondaryObjects = articlePersistenceService.listSecondaryObjects(articleId, DEFAULT_ADMIN_AUTHID);
    assertNotNull(secondaryObjects, "returned null array of secondary objects");
    assertTrue(secondaryObjects.length > 0, "returned empty array of secondary objects");

    for (SecondaryObject secondaryObject : secondaryObjects) {
      assertNotNull(secondaryObject, "returned null secondary object");
    }
  }

  @Test(dataProvider = "articlesWithParts")
  public void testListFiguresTables(String articleId) throws NoSuchArticleIdException {
    SecondaryObject[] secondaryObjects = articlePersistenceService.listFiguresTables(articleId, DEFAULT_ADMIN_AUTHID);
    assertNotNull(secondaryObjects, "returned null array of secondary objects");
    assertTrue(secondaryObjects.length > 0, "returned empty array of secondary objects");

    List<String> figureTypes = new LinkedList<String>();
    figureTypes.add("fig");
    figureTypes.add("table-wrap");

    for (SecondaryObject secondaryObject : secondaryObjects) {
      assertNotNull(secondaryObject, "returned null secondary object");
      assertTrue(figureTypes.contains(secondaryObject.getContextElement()),
          "Returned secondary object with incorrect context element; " +
              "expected either 'fig' or 'table-wrap' but got " + secondaryObject.getContextElement());
    }
  }

  @DataProvider(name = "journalVolumeIssue")
  public Object[][] journalVolumeIssue() {
    List<URI> articleURIs = new LinkedList<URI>();
    articleURIs.add(((Article) savedArticles()[0][1]).getId());
    articleURIs.add(((Article) savedArticles()[1][1]).getId());

    //Issues
    Issue bothArticleIssue = new Issue(); //Contains both articles
    bothArticleIssue.setSimpleCollection(articleURIs);
    bothArticleIssue.setDisplayName("issue with both articles");
    String issueId = dummyDataStore.store(bothArticleIssue);

    DublinCore dc1 = new DublinCore();
    dc1.setIdentifier(issueId);
    dc1.setCreated(Calendar.getInstance().getTime());
    dummyDataStore.store(dc1);
    bothArticleIssue.setDublinCore(dc1);
    dummyDataStore.update(bothArticleIssue);

    Issue oneArticleIssue = new Issue(); //contains only the first article
    oneArticleIssue.setSimpleCollection(articleURIs.subList(0, 1));
    oneArticleIssue.setDisplayName("issue with one article");
    String issue2Id = dummyDataStore.store(oneArticleIssue);

    DublinCore dc2 = new DublinCore();
    dc2.setIdentifier(issue2Id);
    dc2.setCreated(Calendar.getInstance().getTime());
    dummyDataStore.store(dc2);
    oneArticleIssue.setDublinCore(dc2);
    dummyDataStore.update(oneArticleIssue);

    List<URI> issueList = new LinkedList<URI>();
    issueList.add(URI.create(issueId));
    issueList.add(URI.create(issue2Id));

    //Volumes - each issue can only be in one volume
    Volume volume1 = new Volume();
    volume1.setIssueList(issueList.subList(0,1));
    volume1.setDisplayName("volume with both issues");
    String volumeId = dummyDataStore.store(volume1);

    Volume volume2 = new Volume();
    volume2.setIssueList(issueList.subList(1, 2));
    volume2.setDisplayName("volume with one issue");
    String volume2Id = dummyDataStore.store(volume2);


    List<URI> volumeList = new LinkedList<URI>();
    volumeList.add(URI.create(volumeId));
    volumeList.add(URI.create(volume2Id));

    Journal journal = new Journal();
    journal.setVolumes(volumeList);
    journal.setKey("journal-key");
    String journalId = dummyDataStore.store(journal);

    List<String> possibleJournalIds = new ArrayList<String>(1);
    possibleJournalIds.add(journalId);
    List<String> possibleJournalKeys = new ArrayList<String>(1);
    possibleJournalKeys.add(journal.getKey());

    List<String> possibleVolumeIds = new ArrayList<String>(2);
    possibleVolumeIds.add(volumeId);
    possibleVolumeIds.add(volume2Id);
    List<String> possibleVolumeNames = new ArrayList<String>(2);
    possibleVolumeNames.add(volume1.getDisplayName());
    possibleVolumeNames.add(volume2.getDisplayName());

    List<String> possibleIssueIds = new ArrayList<String>(2);
    possibleIssueIds.add(issueId);
    possibleIssueIds.add(issue2Id);
    List<String> possibleIssueNames = new ArrayList<String>(2);
    possibleIssueNames.add(bothArticleIssue.getDisplayName());
    possibleIssueNames.add(oneArticleIssue.getDisplayName());

    return new Object[][]{
        {articleURIs.get(0), 2, //The first article is in both issues
            possibleJournalIds, possibleJournalKeys,
            possibleVolumeIds, possibleVolumeNames,
            possibleIssueIds, possibleIssueNames},
        {articleURIs.get(1), 1, //The second article is only in the first issue
            possibleJournalIds, possibleJournalKeys,
            possibleVolumeIds.subList(0,1), possibleVolumeNames.subList(0,1),
            possibleIssueIds.subList(0,1), possibleIssueNames.subList(0,1)}
    };
  }

  @Test(dataProvider = "journalVolumeIssue", groups = {"savedArticles"})
  public void testGetArticleIssues(URI articleId, int expectedNumberOfResults,
                                   List<String> journalIds, List<String> journalKeys,
                                   List<String> volumeIds, List<String> volumeNames,
                                   List<String> issueIds, List<String> issueNames) {
    List<List<String>> articleIssues = articlePersistenceService.getArticleIssues(articleId);
    assertNotNull(articleIssues, "returned null list of article issues");
    assertEquals(articleIssues.size(), expectedNumberOfResults, "returned incorrect number of article issues");
    for (List<String> row : articleIssues) {
      assertEquals(row.size(), 6, "Returned issue row with incorrect number of entries");

      assertTrue(journalIds.contains(row.get(0)), "returned incorrect first entry (journal id); expected one of "
          + Arrays.toString(journalIds.toArray()) + ", but got " + row.get(0));

      assertTrue(journalKeys.contains(row.get(1)), "returned incorrect second entry (journal key); expected one of "
          + Arrays.toString(journalKeys.toArray()) + ", but got " + row.get(1));

      assertTrue(volumeIds.contains(row.get(2)), "returned incorrect third entry (volume id); expected one of "
          + Arrays.toString(volumeIds.toArray()) + ", but got " + row.get(2));

      assertTrue(volumeNames.contains(row.get(3)), "returned incorrect fourth entry (volume name); expected one of "
          + Arrays.toString(volumeNames.toArray()) + ", but got " + row.get(3));

      assertTrue(issueIds.contains(row.get(4)), "returned incorrect fifth entry (issue id); expected one of "
          + Arrays.toString(issueIds.toArray()) + ", but got " + row.get(4));

      assertTrue(issueNames.contains(row.get(5)), "returned incorrect sixth entry (issue name); expected one of "
          + Arrays.toString(issueNames.toArray()) + ", but got " + row.get(5));

    }
  }

  private void compareBasicArticleProperties(Article actual, Article expected) {
    assertEquals(actual.getArchiveName(), expected.getArchiveName(), "Article didn't have correct archive name");
    assertEquals(actual.getContextElement(), expected.getContextElement(), "Article didn't have correct context element");
    assertEquals(actual.geteIssn(), expected.geteIssn(), "Article didn't have correct eIssn");
    assertEquals(actual.getState(), expected.getState(), "Article didn't have correct state");
  }

  private void compareBasicObjectInfoProperties(ObjectInfo actual, ObjectInfo expected) {
    assertEquals(actual.getContextElement(), expected.getContextElement(), "ObjectInfo had incorrect context element");
    assertEquals(actual.geteIssn(), expected.geteIssn(), "ObjectInfo had incorrect eIssn");
  }
}
