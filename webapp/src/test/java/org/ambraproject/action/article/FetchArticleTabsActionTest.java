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
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.ArticleEditor;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.Category;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.models.CitedArticleEditor;
import org.ambraproject.models.Trackback;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.AuthorView;
import org.ambraproject.views.LinkbackView;
import org.ambraproject.views.article.ArticleInfo;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

  @DataProvider(name = "articleWithEoc")
  public Article getArticle1() {
    Article article1 = new Article();
    article1.setDoi("info:doi/10.1371/Fake-Doi-For-article1-test-eoc");
    article1.setTitle("Fake Title for Article 1");
    article1.seteIssn("Fake EIssn for Article 1");
    article1.setState(Article.STATE_ACTIVE);
    article1.setArchiveName("Fake ArchiveName for Article 1");
    article1.setDescription("Fake Description for Article 1");
    article1.setRights("Fake Rights for Article 1");
    article1.setLanguage("Fake Language for Article 1");
    article1.setFormat("Fake Format for Article 1");
    article1.setVolume("Fake Volume for Article 1");
    article1.setIssue("Fake Issue for Article 1");
    article1.setPublisherLocation("Fake PublisherLocation for Article 1");
    article1.setPublisherName("Fake PublisherName for Article 1");
    article1.setJournal("Fake Journal for Article 1");


    List<String> collaborativeAuthorsForArticle1 = new LinkedList<String>();
    collaborativeAuthorsForArticle1.add("Fake CollaborativeAuthor ONE for Article 1");
    collaborativeAuthorsForArticle1.add("Fake CollaborativeAuthor TWO for Article 1");
    collaborativeAuthorsForArticle1.add("Fake CollaborativeAuthor THREE for Article 1");
    collaborativeAuthorsForArticle1.add("Fake CollaborativeAuthor FOUR for Article 1");
    article1.setCollaborativeAuthors(collaborativeAuthorsForArticle1);

    Set<Category> categoriesForArticle1 = new HashSet<Category>();
    Category category1ForArticle1 = new Category();
    category1ForArticle1.setPath("/Fake Main Category/for/category1ForArticle1");
    category1ForArticle1.setCreated(new Date());
    category1ForArticle1.setLastModified(new Date());
    categoriesForArticle1.add(category1ForArticle1);
    Category category2ForArticle1 = new Category();
    category2ForArticle1.setPath("/Fake Main Category/for/category2ForArticle1");
    category2ForArticle1.setCreated(new Date());
    category2ForArticle1.setLastModified(new Date());
    categoriesForArticle1.add(category2ForArticle1);
    Category category3ForArticle1 = new Category();
    category3ForArticle1.setPath("Fake Main Category/for/category3ForArticle1");
    categoriesForArticle1.add(category3ForArticle1);
    article1.setCategories(categoriesForArticle1);

    List<ArticleAsset> assetsForArticle1 = new LinkedList<ArticleAsset>();
    ArticleAsset asset1ForArticle1 = new ArticleAsset();
    asset1ForArticle1.setContentType("Fake ContentType for asset1ForArticle1");
    asset1ForArticle1.setContextElement("Fake ContextElement for asset1ForArticle1");
    asset1ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset1ForArticle1");
    asset1ForArticle1.setExtension("Fake Name for asset1ForArticle1");
    asset1ForArticle1.setSize(1000001l);
    asset1ForArticle1.setCreated(new Date());
    asset1ForArticle1.setLastModified(new Date());
    assetsForArticle1.add(asset1ForArticle1);
    ArticleAsset asset2ForArticle1 = new ArticleAsset();
    asset2ForArticle1.setContentType("Fake ContentType for asset2ForArticle1");
    asset2ForArticle1.setContextElement("Fake ContextElement for asset2ForArticle1");
    asset2ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset2ForArticle1");
    asset2ForArticle1.setExtension("Fake Name for asset2ForArticle1");
    asset2ForArticle1.setSize(1000002l);
    assetsForArticle1.add(asset2ForArticle1);
    ArticleAsset asset3ForArticle1 = new ArticleAsset();
    asset3ForArticle1.setContentType("Fake ContentType for asset3ForArticle1");
    asset3ForArticle1.setContextElement("Fake ContextElement for asset3ForArticle1");
    asset3ForArticle1.setDoi("info:doi/10.1371/Fake-Doi-For-asset3ForArticle1");
    asset3ForArticle1.setExtension("Fake Name for asset3ForArticle1");
    asset3ForArticle1.setSize(1000003l);
    assetsForArticle1.add(asset3ForArticle1);
    article1.setAssets(assetsForArticle1);

    List<CitedArticle> citedArticlesForArticle1 = new LinkedList<CitedArticle>();
    CitedArticle citedArticle1ForArticle1 = new CitedArticle();

    List<CitedArticleAuthor> citedArticleAuthorsForCitedArticle1ForArticle1 = new LinkedList<CitedArticleAuthor>();
    CitedArticleAuthor citedArticleAuthor1ForCitedArticle1ForArticle1 = new CitedArticleAuthor();
    citedArticleAuthor1ForCitedArticle1ForArticle1.setFullName("Fake FullName for citedArticleAuthor1ForCitedArticle1ForArticle1");
    citedArticleAuthor1ForCitedArticle1ForArticle1.setGivenNames("Fake GivenNames for citedArticleAuthor1ForCitedArticle1ForArticle1");
    citedArticleAuthor1ForCitedArticle1ForArticle1.setSuffix("Fake Suffix for citedArticleAuthor1ForCitedArticle1ForArticle1");
    citedArticleAuthor1ForCitedArticle1ForArticle1.setSurnames("Fake Surnames for citedArticleAuthor1ForCitedArticle1ForArticle1");
    citedArticleAuthorsForCitedArticle1ForArticle1.add(citedArticleAuthor1ForCitedArticle1ForArticle1);
    CitedArticleAuthor citedArticleAuthor2ForCitedArticle1ForArticle1 = new CitedArticleAuthor();
    citedArticleAuthor2ForCitedArticle1ForArticle1.setFullName("Fake FullName for citedArticleAuthor2ForCitedArticle1ForArticle1");
    citedArticleAuthor2ForCitedArticle1ForArticle1.setGivenNames("Fake GivenNames for citedArticleAuthor2ForCitedArticle1ForArticle1");
    citedArticleAuthor2ForCitedArticle1ForArticle1.setSuffix("Fake Suffix for citedArticleAuthor2ForCitedArticle1ForArticle1");
    citedArticleAuthor2ForCitedArticle1ForArticle1.setSurnames("Fake Surnames for citedArticleAuthor2ForCitedArticle1ForArticle1");
    citedArticleAuthorsForCitedArticle1ForArticle1.add(citedArticleAuthor2ForCitedArticle1ForArticle1);
    citedArticle1ForArticle1.setAuthors(citedArticleAuthorsForCitedArticle1ForArticle1);
    citedArticle1ForArticle1.setCitationType("Fake CitationType for citedArticle1ForArticle1");

    List<String> collaborativeAuthorsForCitedArticle1ForArticle1 = new LinkedList<String>();
    collaborativeAuthorsForCitedArticle1ForArticle1.add("Fake CollaborativeAuthor ONE for collaborativeAuthorsForCitedArticle1ForArticle1");
    collaborativeAuthorsForCitedArticle1ForArticle1.add("Fake CollaborativeAuthor TWO for collaborativeAuthorsForCitedArticle1ForArticle1");
    collaborativeAuthorsForCitedArticle1ForArticle1.add("Fake CollaborativeAuthor THREE for collaborativeAuthorsForCitedArticle1ForArticle1");
    collaborativeAuthorsForCitedArticle1ForArticle1.add("Fake CollaborativeAuthor FOUR for collaborativeAuthorsForCitedArticle1ForArticle1");
    citedArticle1ForArticle1.setCollaborativeAuthors(collaborativeAuthorsForCitedArticle1ForArticle1);

    citedArticle1ForArticle1.setDay("Fake Day for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setDisplayYear("Fake DisplayYear for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setDoi("Fake Doi for citedArticle1ForArticle1");

    List<CitedArticleEditor> citedArticleEditorsForArticle1 = new LinkedList<CitedArticleEditor>();
    CitedArticleEditor citedArticleEditor1ForCitedArticle1ForArticle1 = new CitedArticleEditor();
    citedArticleEditor1ForCitedArticle1ForArticle1.setFullName("Fake FullName for citedArticleEditor1ForCitedArticle1ForArticle1");
    citedArticleEditor1ForCitedArticle1ForArticle1.setGivenNames("Fake GivenNames for citedArticleEditor1ForCitedArticle1ForArticle1");
    citedArticleEditor1ForCitedArticle1ForArticle1.setSuffix("Fake Suffix for citedArticleEditor1ForCitedArticle1ForArticle1");
    citedArticleEditor1ForCitedArticle1ForArticle1.setSurnames("Fake Surnames for citedArticleEditor1ForCitedArticle1ForArticle1");
    citedArticleEditorsForArticle1.add(citedArticleEditor1ForCitedArticle1ForArticle1);
    CitedArticleEditor citedArticleEditor2ForCitedArticle1ForArticle1 = new CitedArticleEditor();
    citedArticleEditor2ForCitedArticle1ForArticle1.setFullName("Fake FullName for citedArticleEditor2ForCitedArticle1ForArticle1");
    citedArticleEditor2ForCitedArticle1ForArticle1.setGivenNames("Fake GivenNames for citedArticleEditor2ForCitedArticle1ForArticle1");
    citedArticleEditor2ForCitedArticle1ForArticle1.setSuffix("Fake Suffix for citedArticleEditor2ForCitedArticle1ForArticle1");
    citedArticleEditor2ForCitedArticle1ForArticle1.setSurnames("Fake Surnames for citedArticleEditor2ForCitedArticle1ForArticle1");
    citedArticleEditorsForArticle1.add(citedArticleEditor2ForCitedArticle1ForArticle1);
    citedArticle1ForArticle1.setEditors(citedArticleEditorsForArticle1);

    citedArticle1ForArticle1.seteLocationID("Fake eLocationID for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setIssue("Fake Issue for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setJournal("Fake Journal for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setKey("Fake Key for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setMonth("Fake Month for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setNote("Fake Note for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setPages("Fake Pages for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setPublisherLocation("Fake PublisherLocation for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setSummary("Fake Summary for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setTitle("Fake Title for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setUrl("Fake Url for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setVolume("Fake Volume for citedArticle1ForArticle1");
    citedArticle1ForArticle1.setVolumeNumber(new Integer(1000004));
    citedArticle1ForArticle1.setYear(new Integer(1000005));
    article1.setCitedArticles(citedArticlesForArticle1);

    List<ArticleAuthor> authorsForArticle1 = new LinkedList<ArticleAuthor>();
    ArticleAuthor articleAuthor1ForArticle1 = new ArticleAuthor();
    articleAuthor1ForArticle1.setFullName("Fake FullName for articleAuthor1ForArticle1");
    articleAuthor1ForArticle1.setGivenNames("Fake GivenNames for articleAuthor1ForArticle1");
    articleAuthor1ForArticle1.setSuffix("Fake Suffix for articleAuthor1ForArticle1");
    articleAuthor1ForArticle1.setSurnames("Fake Surnames for articleAuthor1ForArticle1");
    authorsForArticle1.add(articleAuthor1ForArticle1);
    ArticleAuthor articleAuthor2ForArticle1 = new ArticleAuthor();
    articleAuthor2ForArticle1.setFullName("Fake FullName for articleAuthor2ForArticle1");
    articleAuthor2ForArticle1.setGivenNames("Fake GivenNames for articleAuthor2ForArticle1");
    articleAuthor2ForArticle1.setSuffix("Fake Suffix for articleAuthor2ForArticle1");
    articleAuthor2ForArticle1.setSurnames("Fake Surnames for articleAuthor2ForArticle1");
    authorsForArticle1.add(articleAuthor2ForArticle1);
    ArticleAuthor articleAuthor3ForArticle1 = new ArticleAuthor();
    articleAuthor3ForArticle1.setFullName("Fake FullName for articleAuthor3ForArticle1");
    articleAuthor3ForArticle1.setGivenNames("Fake GivenNames for articleAuthor3ForArticle1");
    articleAuthor3ForArticle1.setSuffix("Fake Suffix for articleAuthor3ForArticle1");
    articleAuthor3ForArticle1.setSurnames("Fake Surnames for articleAuthor3ForArticle1");
    authorsForArticle1.add(articleAuthor3ForArticle1);
    article1.setAuthors(authorsForArticle1);

    List<ArticleEditor> editorsForArticle1 = new LinkedList<ArticleEditor>();
    ArticleEditor articleEditor1ForArticle1 = new ArticleEditor();
    articleEditor1ForArticle1.setFullName("Fake FullName for articleEditor1ForArticle1");
    articleEditor1ForArticle1.setGivenNames("Fake GivenNames for articleEditor1ForArticle1");
    articleEditor1ForArticle1.setSuffix("Fake Suffix for articleEditor1ForArticle1");
    articleEditor1ForArticle1.setSurnames("Fake Surnames for articleEditor1ForArticle1");
    editorsForArticle1.add(articleEditor1ForArticle1);
    ArticleEditor articleEditor2ForArticle1 = new ArticleEditor();
    articleEditor2ForArticle1.setFullName("Fake FullName for articleEditor2ForArticle1");
    articleEditor2ForArticle1.setGivenNames("Fake GivenNames for articleEditor2ForArticle1");
    articleEditor2ForArticle1.setSuffix("Fake Suffix for articleEditor2ForArticle1");
    articleEditor2ForArticle1.setSurnames("Fake Surnames for articleEditor2ForArticle1");
    editorsForArticle1.add(articleEditor2ForArticle1);
    ArticleEditor articleEditor3ForArticle1 = new ArticleEditor();
    articleEditor3ForArticle1.setFullName("Fake FullName for articleEditor3ForArticle1");
    articleEditor3ForArticle1.setGivenNames("Fake GivenNames for articleEditor3ForArticle1");
    articleEditor3ForArticle1.setSuffix("Fake Suffix for articleEditor3ForArticle1");
    articleEditor3ForArticle1.setSurnames("Fake Surnames for articleEditor3ForArticle1");
    editorsForArticle1.add(articleEditor3ForArticle1);
    article1.setEditors(editorsForArticle1);

    article1.setDate(new Date());

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
    eocRelationship.setParentArticle(article1);
    eocRelationship.setOtherArticleID(Long.valueOf(dummyDataStore.store(eocArticle)));
    eocRelationship.setType("object-of-concern");
    eocRelationship.setOtherArticleDoi(eocArticle.getDoi());
    articleRelationships.add(eocRelationship);


    Set<String> typesForArticle1 = new HashSet<String>();
    typesForArticle1.add("Fake Type ONE for Article1");
    typesForArticle1.add("Fake Type TWO for Article1");
    typesForArticle1.add("Fake Type THREE for Article1");
    typesForArticle1.add("Fake Type FOUR for Article1");
    article1.setTypes(typesForArticle1);

    return article1;
  }



  @DataProvider(name = "articlesWithEoc")
  public Object[][] getArticlesWithEoc()
  {
    //Test an  article with Expression of concern
    ArticleInfo a1 = new ArticleInfo("info:doi/10.1371/journal.pone.0049703");
    return new Object[][] { {a1} };
  }

  @Test(dataProvider = "articleWithEoc")
  public void testEoc(ArticleInfo articleInfo) throws Exception {
     getArticle1();
     action.fetchArticle();
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
    assertEquals(action.getTotalNumAnnotations(), 4, "Action returned incorrect number of comments");
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
    assertEquals(action.getTotalNumAnnotations(), 4, "Action returned incorrect annotation count");
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
    assertTrue(action.getIsRetracted(), "Action didn't count retraction");
    assertFalse(action.getIsDisplayingCorrections(), "Action didn't indicate that it was not displaying corrections");
  }

  @Test
  public void testFetchArticleCorrections() {
    UserProfile user = userService.getUserByAuthId(DEFAULT_ADMIN_AUTHID);
    login(user);

    //clear any annotations made by other tests
    dummyDataStore.deleteAll(Annotation.class);

    //store some annotations on the article
    Annotation formalCorrection = new Annotation(user, AnnotationType.FORMAL_CORRECTION, getArticleToFetch().getID());
    formalCorrection.setTitle("Title for formal correction");
    dummyDataStore.store(formalCorrection);

    Annotation minorCorrection = new Annotation(user, AnnotationType.MINOR_CORRECTION, getArticleToFetch().getID());
    minorCorrection.setTitle("minor correction for title");
    dummyDataStore.store(minorCorrection);

    Annotation retraction = new Annotation(user, AnnotationType.RETRACTION, getArticleToFetch().getID());
    retraction.setTitle("title for retraction");
    dummyDataStore.store(retraction);

    dummyDataStore.store(new Annotation(user, AnnotationType.COMMENT, getArticleToFetch().getID()));
    dummyDataStore.store(new Annotation(user, AnnotationType.REPLY, getArticleToFetch().getID()));

    action.setArticleURI(getArticleToFetch().getDoi());
    String result = action.fetchArticleCorrections();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    //check the comments
    assertEquals(action.getTotalNumAnnotations(), 4, "Action returned incorrect number of annotations");

    //order his hard to check, we do it in annotation service test.
    assertEqualsNoOrder(action.getCommentary(), new AnnotationView[]{
        new AnnotationView(formalCorrection, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null),
        new AnnotationView(minorCorrection, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null),
        new AnnotationView(retraction, getArticleToFetch().getDoi(), getArticleToFetch().getTitle(), null)
    }, "Action returned incorrect comments");

    assertEquals(action.getNumComments(), 1, "Action didn't count comments");
    assertTrue(action.getIsDisplayingCorrections(), "Action didn't indicate that it was displaying corrections");
  }

  @Test
  public void testFetchArticleRelated() {
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
    String result = action.fetchArticleRelated();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertNotNull(action.getTrackbackList(), "action had null trackback list");
    assertEqualsNoOrder(
        action.getTrackbackList().toArray(),
        new LinkbackView[]{
            new LinkbackView(trackback1, getArticleToFetch().getDoi(), getArticleToFetch().getTitle()),
            new LinkbackView(trackback2, getArticleToFetch().getDoi(), getArticleToFetch().getTitle())},
        "Action had incorrect trackback list");
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

  }
}
