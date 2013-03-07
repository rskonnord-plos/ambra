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

package org.ambraproject.action.article;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAsset;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.Trackback;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.AuthorView;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;

/**
 * @author Alex Kudlick 2/16/12
 */
public class FetchArticleTabsActionTest extends FetchActionTest {

  @Autowired
  protected FetchArticleTabsAction action;

  @Autowired
  protected UserService userService;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @BeforeMethod
  public void resetAction() {
    action.getFormalCorrections().clear();
    action.getMinorCorrections().clear();
    action.getRetractions().clear();
  }

  @DataProvider(name = "articleAffiliations")
  public Object[][] getArticleAffiliations(){

    //multiple affiliates with only some having authors contained
    String doi = "info:doi/10.1371/journal.pmed.0020073";
    Article a = new Article(doi);
    //add the asset record for the XML.  The fileService needs this
    List<ArticleAsset> assetsForArticle1 = new LinkedList<ArticleAsset>();
    ArticleAsset asset1ForArticle1 = new ArticleAsset();
    asset1ForArticle1.setContentType("XML");
    asset1ForArticle1.setContextElement("Fake ContextElement for asset1ForArticle1");
    asset1ForArticle1.setDoi("info:doi/10.1371/journal.pmed.0020073");
    asset1ForArticle1.setExtension("XML");
    asset1ForArticle1.setSize(1000001l);
    asset1ForArticle1.setCreated(new Date());
    asset1ForArticle1.setLastModified(new Date());
    assetsForArticle1.add(asset1ForArticle1);
    a.setAssets(assetsForArticle1);

    dummyDataStore.store(a);

    //LinkedHashMap for ordering
    Map<String, List<AuthorView>> affiliationSets = new LinkedHashMap<String, List<AuthorView>>() {{
      put("Program in Cancer Biology and Genetics, Memorial Sloan-Kettering Cancer Center, New York, New York, United States of America",
          new ArrayList<AuthorView>() {{
            add(AuthorView.builder()
                .setGivenNames("William")
                .setSurnames("Pao")
                .build());
          }});

      put("Thoracic Oncology Service, Department of Medicine, Memorial Sloan-Kettering Cancer Center, New York, New York, United States of America",
          new ArrayList<AuthorView>(){{
            add(AuthorView.builder()
                .setGivenNames("William")
                .setSurnames("Pao")
                .build());
          }});

    }};

    return new Object[][]{{doi, affiliationSets}};
  }

  /**
   * Check if article has expression of concern
   * @throws Exception
   */
  @Test
  public void testArticleInfoForEoc() throws Exception {

    Article eocArticle = new Article();
    eocArticle.setDoi("id:doi-object-of-concern-relationship-article");
    eocArticle.setState(Article.STATE_ACTIVE);
    eocArticle.setTitle("foo");
    Set<String> typesForEocArticle = new HashSet<String>();
    typesForEocArticle.add("http://rdf.plos.org/RDF/articleType/Expression%20of%20Concern");
    eocArticle.setTypes(typesForEocArticle);

    List<ArticleRelationship> articleRelationships = new ArrayList<ArticleRelationship>(1);
    //Expression of Concern relationship
    ArticleRelationship eocRelationship = new ArticleRelationship();
    eocRelationship.setParentArticle(getArticleToFetch());
    eocRelationship.setOtherArticleID(Long.valueOf(dummyDataStore.store(eocArticle)));
    eocRelationship.setType("object-of-concern");
    eocRelationship.setOtherArticleDoi(eocArticle.getDoi());
    articleRelationships.add(eocRelationship);
    getArticleToFetch().setRelatedArticles(articleRelationships);

    dummyDataStore.store(articleRelationships);
    action.setExpressionOfConcern("Dummy Expression of Concern");
    action.setArticleURI(getArticleToFetch().getDoi());
    action.fetchArticle();

    String eoc = action.getExpressionOfConcern();
    assertEquals(eoc,"Dummy Expression of Concern","eoc didn't match in fetch tab" );


  }

  /**
   * Make sure article affiliations are listed in order in which they appear in xml
   *
   * @param doi
   * @param affiliationSets
   */
  @Test(dataProvider = "articleAffiliations")
  public void testGetAffiliations(String doi, Map<String, List<AuthorView>> affiliationSets) throws Exception {

    //get action class conjiggered
    action.setArticleURI(doi);
    action.fetchArticle();

    //perhaps a sub-optimal implementation of duo iteration, but otherwise it seems like a lot of boilerplate
    //for just a test
    Iterator testData = affiliationSets.entrySet().iterator();
    for (Map.Entry<String, List<AuthorView>> mutEntry : action.getAuthorsByAffiliation()) {

      Map.Entry<String, List<AuthorView>> testDatum = (Map.Entry<String, List<AuthorView>>) testData.next();

      assertEquals(mutEntry.getKey(),testDatum.getKey(), "Article affiliation names don't match");

      Iterator testAuthors = testDatum.getValue().iterator();
      for(AuthorView mutAuthor : mutEntry.getValue()){

        AuthorView testAuthor = (AuthorView)testAuthors.next();
        assertEquals(mutAuthor.getFullName(), testAuthor.getFullName(),"Author names don't match");
      }

      //make sure we've covered all test data
      assertFalse(testAuthors.hasNext(),"Not all test authors accounted for");

    }
    //make sure we've covered all test data
    assertFalse(testData.hasNext(), "Not all test affiliations accounted for");
  }

  @Test
  public void testFetchArticle() throws Exception {
    UserProfile user = userService.getUserByAuthId(DEFAULT_ADMIN_AUTHID);
    login(user);

    //store some annotations on the article
    dummyDataStore.deleteAll(Annotation.class);
    Annotation formalCorrection = new Annotation(user, AnnotationType.FORMAL_CORRECTION, getArticleToFetch().getID());
    formalCorrection.setTitle("formal correction title");
    dummyDataStore.store(formalCorrection);

    Annotation retraction = new Annotation(user, AnnotationType.RETRACTION, getArticleToFetch().getID());
    retraction.setTitle("Retraction title");
    dummyDataStore.store(retraction);

    Annotation minorCorrection = new Annotation(user, AnnotationType.MINOR_CORRECTION, getArticleToFetch().getID());
    minorCorrection.setTitle("minor correction title");
    dummyDataStore.store(minorCorrection);

    Annotation comment = new Annotation(user, AnnotationType.COMMENT, getArticleToFetch().getID());
    comment.setTitle("comment title");
    dummyDataStore.store(comment);

    //replies don't get counted as a comment
    Annotation reply = new Annotation(user, AnnotationType.REPLY, getArticleToFetch().getID());
    reply.setTitle("reply title");
    reply.setParentID(comment.getID());
    dummyDataStore.store(reply);

    //check that an article view gets recorded
    int numArticleViews = dummyDataStore.getAll(ArticleView.class).size();

    action.setArticleURI(getArticleToFetch().getDoi());
    String result = action.fetchArticle();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    compareArticles(action.getArticleInfoX(), getArticleToFetch());

    assertEquals(dummyDataStore.getAll(ArticleView.class).size(), numArticleViews + 1,
        "Action didn't record this as an article view");

    //check the comments
    assertEquals(action.getFormalCorrections().toArray(), new AnnotationView[]{
        new AnnotationView(formalCorrection, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)},
        "Action returned incorrect formal corrections");
    assertEquals(action.getRetractions().toArray(), new AnnotationView[]{
        new AnnotationView(retraction, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)},
        "Action returned incorrect retractions");
    assertEquals(action.getMinorCorrections().toArray(), new AnnotationView[]{
        new AnnotationView(minorCorrection, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)},
        "Action returned incorrect formal corrections");
    assertEquals(action.getCommentary(), new AnnotationView[] {
        new AnnotationView(comment, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)
    }, "Action returned incorrect commentary");
    assertFalse(action.getIsResearchArticle(), "Expected article a research article");

    //TODO: Check the transformed html
  }

  @Test
  public void testFetchArticleComments() {
    UserProfile user = userService.getUserByAuthId(DEFAULT_ADMIN_AUTHID);
    login(user);

    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    //store some annotations on the article
    dummyDataStore.deleteAll(Annotation.class);
    //corrections shouldn't get listed
    dummyDataStore.store(new Annotation(user, AnnotationType.FORMAL_CORRECTION, getArticleToFetch().getID()));
    dummyDataStore.store(new Annotation(user, AnnotationType.MINOR_CORRECTION, getArticleToFetch().getID()));
    dummyDataStore.store(new Annotation(user, AnnotationType.RETRACTION, getArticleToFetch().getID()));

    Annotation comment = new Annotation(user, AnnotationType.COMMENT, getArticleToFetch().getID());
    comment.setTitle("comment title");
    comment.setCreated(lastYear.getTime());
    dummyDataStore.store(comment);

    //replies don't get counted as a comment
    Annotation reply = new Annotation(user, AnnotationType.REPLY, getArticleToFetch().getID());
    reply.setTitle("reply title");
    reply.setParentID(comment.getID());
    dummyDataStore.store(reply);

    action.setArticleURI(getArticleToFetch().getDoi());
    String result = action.fetchArticleComments();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    //order his hard to check, we do it in annotation service test.
    assertEqualsNoOrder(action.getCommentary(), new AnnotationView[]{
        new AnnotationView(comment, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)
    }, "Action returned incorrect comments");

    //check that the reply got loaded up
    assertEquals(action.getCommentary()[0].getReplies().length, 1, "Reply to comment didn't get loaded up");
    assertEquals(action.getCommentary()[0].getReplies()[0],
        new AnnotationView(reply, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null),
        "Action returned incorrect reply");
    assertEquals(action.getNumCorrections(), 2, "Action didn't count corrections");
    assertEquals(action.getNumComments(), 1, "Action didn't count comments");
  }

  @Test
  public void testFetchArticleRelated() {
    action.setArticleURI(getArticleToFetch().getDoi());
    String result = action.fetchArticleRelated();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
  }

  @Test
  public void testFetchArticleMetrics() {
    //keep these url the same as the ones in the testFetchArticleRelated, so we don't create extra trackbacks, and we always know the number of trackbacks,
    //regardless of the order in which the tests are run
    Trackback trackback1 = new Trackback(getArticleToFetch().getID(), "http://coolblog.net");
    trackback1.setBlogName("My Cool Blog");
    trackback1.setTitle("Blog title 1");
    trackback1.setExcerpt("Once upon a time....");
    dummyDataStore.store(trackback1);
    Trackback trackback2 = new Trackback(getArticleToFetch().getID(), "http://coolblog.net/foo");
    trackback2.setBlogName("My Cool Blog");
    trackback2.setTitle("Blog title 2");
    trackback2.setExcerpt("There was a prince....");
    dummyDataStore.store(trackback2);

    action.setArticleURI(getArticleToFetch().getDoi());
    String result = action.fetchArticleMetrics();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getTrackbackCount(), 2, "Action returned incorrect trackback count");

    UserProfile user = userService.getUserByAuthId(DEFAULT_ADMIN_AUTHID);
    login(user);

    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    //store some annotations on the article
    dummyDataStore.deleteAll(Annotation.class);
    //corrections shouldn't get listed
    dummyDataStore.store(new Annotation(user, AnnotationType.FORMAL_CORRECTION, getArticleToFetch().getID()));
    dummyDataStore.store(new Annotation(user, AnnotationType.MINOR_CORRECTION, getArticleToFetch().getID()));
    dummyDataStore.store(new Annotation(user, AnnotationType.RETRACTION, getArticleToFetch().getID()));

    Annotation comment = new Annotation(user, AnnotationType.COMMENT, getArticleToFetch().getID());
    comment.setTitle("comment title");
    comment.setCreated(lastYear.getTime());
    dummyDataStore.store(comment);

    //replies don't get counted as a comment
    Annotation reply = new Annotation(user, AnnotationType.REPLY, getArticleToFetch().getID());
    reply.setTitle("reply title");
    reply.setParentID(comment.getID());
    dummyDataStore.store(reply);

    action.setArticleURI(getArticleToFetch().getDoi());
    String resultComments = action.fetchArticleComments();
    assertEquals(resultComments, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getNumComments(), 1, "Action returned incorrect comments count");
    assertEquals(action.getNumCorrections(), 2, "Action returned incorrect annotations count");
  }
}
