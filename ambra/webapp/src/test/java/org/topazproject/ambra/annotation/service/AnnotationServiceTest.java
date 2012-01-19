/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
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

package org.topazproject.ambra.annotation.service;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.cache.MockCache;
import org.topazproject.ambra.cache.CacheManager;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

import org.easymock.classextension.IMocksControl;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.isA;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.verify;

import com.sun.xacml.PDP;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * @author Dragisa Krsmanovic
 */
public class AnnotationServiceTest {

  @Test
  public void listAnnotations() {

    IMocksControl ctl = createControl();

    PDP pdp = ctl.createMock(PDP.class);
    Session session = ctl.createMock(Session.class);
    Query query = ctl.createMock(Query.class);
    Results results = ctl.createMock(Results.class);
    CacheManager cacheManager = ctl.createMock(CacheManager.class);
    MockCache cache = new MockCache();


    String target = "info:doi/10.1371/journal.pone.0002250";
    String queryString = "select an.id from ArticleAnnotation an where an.annotates = :target and an.mediator = :mediator;";
    String applicationId = "test-app";
    URI annotation1id = URI.create("info:doi/10.1371/annotation1");
    URI annotation2id = URI.create("info:doi/10.1371/annotation2");
    URI annotation3id = URI.create("info:doi/10.1371/annotation3");

    Comment comment = new Comment();
    comment.setId(annotation1id);
    FormalCorrection formalCorrection = new FormalCorrection();
    formalCorrection.setId(annotation2id);
    MinorCorrection minorCorrection = new MinorCorrection();
    minorCorrection.setId(annotation3id);

    ArticleAnnotation[] expected = {comment, formalCorrection, minorCorrection};

    cacheManager.registerListener(isA(AbstractObjectListener.class));
    expectLastCall().times(0,1);

    expect(pdp.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    expect(session.getFlushMode())
        .andReturn(Session.FlushMode.always);
    session.flush();

    expect(session.createQuery(queryString))
        .andReturn(query);
    expect(query.setParameter("target", target))
        .andReturn(query);
    expect(query.setParameter("mediator", applicationId))
        .andReturn(query);
    expect(query.execute())
        .andReturn(results);

    expect(results.next())
        .andReturn(true).times(3)
        .andReturn(false);
    expect(results.getURI(0))
        .andReturn(annotation1id)
        .andReturn(annotation2id)
        .andReturn(annotation3id);


    expect(session.get(ArticleAnnotation.class, annotation1id.toString()))
        .andReturn(comment);
    expect(session.get(ArticleAnnotation.class, annotation2id.toString()))
        .andReturn(formalCorrection);
    expect(session.get(ArticleAnnotation.class, annotation3id.toString()))
        .andReturn(minorCorrection);

    ctl.replay();

    cache.setCacheManager(cacheManager);

    AnnotationService annotationService = new AnnotationService();

    annotationService.setArticleAnnotationCache(cache);
    annotationService.setAnnotationsPdp(pdp);
    annotationService.setOtmSession(session);
    annotationService.setApplicationId(applicationId);

    ArticleAnnotation[] annotations = annotationService.listAnnotations(
        target,
        AnnotationService.ALL_ANNOTATION_CLASSES);

    assertEquals(annotations, expected);

    ctl.verify();
  }


  @Test
  public void getAnnotationIds() throws ParseException, URISyntaxException {

    IMocksControl ctl = createControl();

    PDP pdp = ctl.createMock(PDP.class);
    Session session = ctl.createMock(Session.class);
    Query annQuery = ctl.createMock(Query.class);
    Results annResults = ctl.createMock(Results.class);
    CacheManager cacheManager = ctl.createMock(CacheManager.class);
    MockCache cache = new MockCache();

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    Date startDate = dateFormat.parse("02/01/2009");
    Date endDate = dateFormat.parse("03/10/2009");
    Set<String> annotType = new HashSet<String>();
    annotType.add("FormalCorrection");
    annotType.add("MinorCorrection");

    String queryString = "select a.id id, cr from Annotation a, Article ar " +
        "where a.annotates = ar " +
        "and cr := a.created " +
        "and (a.<rdf:type> = <" + Annotation.RDF_TYPE + "> " +
        "minus (a.<rdf:type> = <" + Annotation.RDF_TYPE + "> " +
        "and a.<rdf:type> = <" + RatingSummary.RDF_TYPE + ">)) " +
        "and ge(cr, :sd) " +
        "and le(cr, :ed) " +
        "and (a.<rdf:type> = :type0 or a.<rdf:type> = :type1) " +
        "order by cr desc, id asc limit 3;";

    String applicationId = "test-app";

    URI annotation1id = URI.create("info:doi/10.1371/annotation1");
    URI annotation2id = URI.create("info:doi/10.1371/annotation2");
    URI annotation3id = URI.create("info:doi/10.1371/annotation3");

    List<String> expected = new ArrayList<String>();
    expected.add(annotation1id.toString());
    expected.add(annotation2id.toString());
    expected.add(annotation3id.toString());

    cacheManager.registerListener(isA(AbstractObjectListener.class));
    expectLastCall().times(0,1);

    expect(pdp.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    // Run Annotation Query
    expect(session.createQuery(queryString))
        .andReturn(annQuery);
    expect(annQuery.setParameter("sd", startDate))
        .andReturn(annQuery);
    expect(annQuery.setParameter("ed", endDate))
        .andReturn(annQuery);

    int i = 0;
    for (String type : annotType) {
      expect(annQuery.setUri("type"+i++, URI.create(type)))
          .andReturn(annQuery);
    }

    expect(annQuery.execute())
        .andReturn(annResults);

    expect(annResults.next())
        .andReturn(true).times(3)
        .andReturn(false);
    expect(annResults.getURI(0))
        .andReturn(annotation1id)
        .andReturn(annotation2id)
        .andReturn(annotation3id);

    ctl.replay();

    cache.setCacheManager(cacheManager);

    AnnotationService annotationService = new AnnotationService();

    annotationService.setArticleAnnotationCache(cache);
    annotationService.setAnnotationsPdp(pdp);
    annotationService.setOtmSession(session);
    annotationService.setApplicationId(applicationId);



    List<String> annotationIds = annotationService.getFeedAnnotationIds(startDate, endDate,
        new HashSet<String>(annotType), 3);

    assertEquals(annotationIds, expected);

    ctl.verify();
  }

  @Test
  public void testDeletAnnotationWithoutBody() {
    IMocksControl ctl = createControl();

    PDP pdp = ctl.createMock(PDP.class);
    Session session = ctl.createMock(Session.class);
    PermissionsService permissionsService = ctl.createMock(PermissionsService.class);

    expect(pdp.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    String annotationId = "info:doi/123.456/annotation1";
    FormalCorrection annotation = new FormalCorrection();
    annotation.setId(URI.create(annotationId));

    expect(session.get(ArticleAnnotation.class, annotationId)).andReturn(annotation);
    expect(session.delete(annotation)).andReturn(annotationId);
    permissionsService.cancelPropagatePermissions(eq(annotationId), (String[]) anyObject());
    expectLastCall();
    permissionsService.cancelGrants(eq(annotationId), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    permissionsService.cancelRevokes(eq(annotationId), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall();

    AnnotationService annotationService = new AnnotationService();
    annotationService.setAnnotationsPdp(pdp);
    annotationService.setOtmSession(session);
    annotationService.setPermissionsService(permissionsService);


    ctl.replay();

    annotationService.deleteAnnotation(annotationId);

    ctl.verify();


  }

  @Test
  public void createDefaultCitationForFormalCorrection() throws Exception{
    FormalCorrection fc = new FormalCorrection();
    fc.setId(URI.create("info:doi/annotationId"));
    runCreateDefaultCitationTest(fc, "Correction: ");
  }

  @Test
  public void createDefaultCitationForRetraction() throws Exception{
    Retraction retraction = new Retraction();
    retraction.setId(URI.create("info:doi/annotationId"));
    runCreateDefaultCitationTest(retraction, "Retraction: ");
  }

  private void runCreateDefaultCitationTest(ArticleAnnotation annotation, String titlePrefix) throws Exception {


    Session session = createMock(Session.class);
    Citation articleCitation = createArticleCitation();
    expect(session.saveOrUpdate(isA(UserProfile.class))).andReturn("newId").times(3);
    expect(session.saveOrUpdate(isA(Citation.class))).andReturn("newCitationId").once();
    replay(session);

    Citation newCitation;

    if (annotation instanceof FormalCorrection) {
      AnnotationService.createDefaultCitation((FormalCorrection)annotation, articleCitation, session);
      newCitation = ((FormalCorrection)annotation).getBibliographicCitation();
    } else if (annotation instanceof Retraction) {
      AnnotationService.createDefaultCitation((Retraction)annotation, articleCitation, session);
      newCitation = ((Retraction)annotation).getBibliographicCitation();
    } else {
      throw new Exception("Wrong annotation type");
    }
    verify(session);
    assertNotNull(newCitation);
    assertNull(newCitation.getId());
    assertEquals(newCitation.getCitationType(), articleCitation.getCitationType());
    assertEquals(newCitation.getDay(), articleCitation.getDay());
    assertEquals(newCitation.getDisplayYear(), "(" + articleCitation.getDisplayYear() + ")");
    assertEquals(newCitation.getDoi(), annotation.getId().toString().replaceFirst("info:doi/",""));
    assertNull(newCitation.getELocationId());
    assertNull(newCitation.getVolume());
    assertNull(newCitation.getVolumeNumber());
    assertNull(newCitation.getIssue());
    assertEquals(newCitation.getJournal(), articleCitation.getJournal());
    assertEquals(newCitation.getMonth(), articleCitation.getMonth());
    assertEquals(newCitation.getYear(), articleCitation.getYear());
    assertEquals(newCitation.getTitle(), titlePrefix + articleCitation.getTitle());
    assertEquals(newCitation.getPages(), articleCitation.getPages());
    assertEquals(newCitation.getNote(), articleCitation.getNote());
    assertEquals(newCitation.getKey(), articleCitation.getKey());
    assertEquals(newCitation.getPublisherLocation(), articleCitation.getPublisherLocation());
    assertEquals(newCitation.getPublisherName(), articleCitation.getPublisherName());
    assertEquals(newCitation.getSummary(), articleCitation.getSummary());

    assertNotNull(newCitation.getAuthors());
    assertEquals(newCitation.getAuthors().size(), articleCitation.getAuthors().size());
    for (int i = 0; i < newCitation.getAuthors().size(); i++) {
      UserProfile newProfile =  newCitation.getAuthors().get(i);
      UserProfile articleProfile =  articleCitation.getAuthors().get(i);
      assertNull(newProfile.getId());
      assertUserFields(newProfile, articleProfile);
    }

    assertNotNull(newCitation.getEditors());
    assertEquals(newCitation.getEditors().size(), articleCitation.getEditors().size());
    for (int i = 0; i < newCitation.getEditors().size(); i++) {
      UserProfile newEditor =  newCitation.getAuthors().get(i);
      UserProfile articleEditor =  articleCitation.getAuthors().get(i);
      assertNull(newEditor.getId());
      assertUserFields(newEditor, articleEditor);
    }
    assertNotNull(newCitation.getCollaborativeAuthors());
    assertEquals(newCitation.getCollaborativeAuthors().size(), articleCitation.getCollaborativeAuthors().size());
    assertEquals(newCitation.getCollaborativeAuthors().toArray(), articleCitation.getCollaborativeAuthors().toArray());
  }

  /*
    @Test
    public void createDefaultCitationForRetraction() {
      Session session = EasyMock.createMock(Session.class);
    }
  */


  private void assertUserFields(UserProfile newProfile, UserProfile articleProfile) {
    assertEquals(newProfile.getRealName(), articleProfile.getRealName());
    assertEquals(newProfile.getSurnames(), articleProfile.getSurnames());
    assertEquals(newProfile.getSuffix(), articleProfile.getSuffix());
    assertEquals(newProfile.getGivenNames(), articleProfile.getGivenNames());
    assertEquals(newProfile.getCity(), articleProfile.getCity());
    assertEquals(newProfile.getCountry(), articleProfile.getCountry());
    assertEquals(newProfile.getEmail(), articleProfile.getEmail());
    assertEquals(newProfile.getEmailAsString(), articleProfile.getEmailAsString());
    assertEquals(newProfile.getPostalAddress(), articleProfile.getPostalAddress());
    assertEquals(newProfile.getPostalAddress(), articleProfile.getPostalAddress());
    assertEquals(newProfile.getPositionType(), articleProfile.getPositionType());
    assertEquals(newProfile.getDisplayName(), articleProfile.getDisplayName());
    assertEquals(newProfile.getOrganizationName(), articleProfile.getOrganizationName());
    assertEquals(newProfile.getOrganizationType(), articleProfile.getOrganizationType());
    assertEquals(newProfile.getBiography(), articleProfile.getBiography());
    assertEquals(newProfile.getBiographyText(), articleProfile.getBiographyText());
    assertEquals(newProfile.getResearchAreasText(), articleProfile.getResearchAreasText());
    assertEquals(newProfile.getHomePage(), articleProfile.getHomePage());
    assertEquals(newProfile.getGender(), articleProfile.getGender());
  }

  private Citation createArticleCitation() {
    Citation articleCitation = new Citation();
    articleCitation.setCitationType("type");
    articleCitation.setId(URI.create("articleCitationId"));
    articleCitation.setEditors(new ArrayList<UserProfile>());
    articleCitation.setCollaborativeAuthors(new ArrayList<String>());
    articleCitation.setAuthors(new ArrayList<UserProfile>());
    articleCitation.setDay("day");
    articleCitation.setDisplayYear("two thousant");
    articleCitation.setDoi("doi");
    articleCitation.setELocationId("eLocationID");
    articleCitation.setIssue("issue");
    articleCitation.setJournal("journal");
    articleCitation.setKey("key");
    articleCitation.setMonth("month");
    articleCitation.setNote("note");
    articleCitation.setPages("pages");
    articleCitation.setPublisherLocation("publisher location");
    articleCitation.setPublisherName("publisher name");
    articleCitation.setSummary("summary");
    articleCitation.setTitle("title");
    articleCitation.setUrl("url");
    articleCitation.setVolume("volume");
    articleCitation.setVolumeNumber(10);
    articleCitation.setYear(2009);

    articleCitation.getAuthors().add(createUserProfile("name1", "surname1", "suffix1"));
    articleCitation.getAuthors().add(createUserProfile("name1", "surname1", null));

    articleCitation.getEditors().add(createUserProfile("editor1", "editorsurname1", null));

    articleCitation.getCollaborativeAuthors().add("collab author 1");

    return articleCitation;
  }

  private UserProfile createUserProfile(String realName, String surnames, String suffix) {
    UserProfile user = new UserProfile();
    user.setId(URI.create("auth1"));
    user.setBiography("biography");
    user.setBiographyText("biography text");
    user.setCity("city");
    user.setCountry("country");
    user.setDisplayName("display name");
    user.setEmail(URI.create("nobdoy@plos.org"));
    user.setGender("M");
    user.setGivenNames("lastname");
    user.setHomePage(URI.create("www.homepage.com"));
    user.setInterests(new HashSet<URI>());
    user.getInterests().add(URI.create("interset1"));
    user.setOrganizationType("org type");
    user.setOrganizationName("organization name");
    user.setPositionType("position type");
    user.setPostalAddress("postal address");
    user.setRealName(realName);
    user.setSuffix(suffix);
    user.setSurnames(surnames);
    user.setTitle("title");
    user.setWeblog(URI.create("blog"));
    user.setPublications(URI.create("publications"));
    user.setResearchAreasText("research areas");
    user.setInterestsText("interests text");
    return user;
  }


}
