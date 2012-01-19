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

package org.topazproject.ambra.annotation.service;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import org.easymock.classextension.internal.MocksClassControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.topazproject.ambra.admin.service.CitationService;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.easymock.classextension.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author Dragisa Krsmanovic
 */
@ContextConfiguration
public class AnnotationServiceTest extends AbstractTestNGSpringContextTests {
  protected String applicationId = "test-app";

  @Autowired
  protected AnnotationService annotationService;
  @Autowired
  protected MocksClassControl mocksController;

  //Don't worry if intelliJ complains about these beans, it's because the createMock() method in
  //MocksClassControl is parameterized, so intelliJ doesn't know the correct type
  //They're provided for legacy unit tests that use easymock objects
  @Autowired
  protected Session mockSession;
  @Autowired
  protected PDP mockPDP;
  @Autowired
  protected PermissionsService mockPermissionsService;

  @Test
  @DirtiesContext
  public void listAnnotations() {

    Query query = mocksController.createMock(Query.class);
    Results results = mocksController.createMock(Results.class);

    String target = "info:doi/10.1371/journal.pone.0002250";
    String queryString = "select an.id from ArticleAnnotation an where an.annotates = :target and an.mediator = :mediator;";
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

    expect(mockPDP.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    expect(mockSession.getFlushMode())
        .andReturn(Session.FlushMode.always);
    mockSession.flush();

    expect(mockSession.createQuery(queryString))
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


    expect(mockSession.get(ArticleAnnotation.class, annotation1id.toString()))
        .andReturn(comment);
    expect(mockSession.get(ArticleAnnotation.class, annotation2id.toString()))
        .andReturn(formalCorrection);
    expect(mockSession.get(ArticleAnnotation.class, annotation3id.toString()))
        .andReturn(minorCorrection);

    mocksController.replay();

    ArticleAnnotation[] annotations = annotationService.listAnnotations(
        target,
        annotationService.getAllAnnotationClasses());

    assertEquals(annotations, expected);

    mocksController.verify();
  }


  @Test
  @DirtiesContext
  public void getAnnotationIds() throws ParseException, URISyntaxException {

    Query annQuery = mocksController.createMock(Query.class);
    Results annResults = mocksController.createMock(Results.class);

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


    URI annotation1id = URI.create("info:doi/10.1371/annotation1");
    URI annotation2id = URI.create("info:doi/10.1371/annotation2");
    URI annotation3id = URI.create("info:doi/10.1371/annotation3");

    List<String> expected = new ArrayList<String>();
    expected.add(annotation1id.toString());
    expected.add(annotation2id.toString());
    expected.add(annotation3id.toString());

    expect(mockPDP.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    // Run Annotation Query
    expect(mockSession.createQuery(queryString))
        .andReturn(annQuery);
    expect(annQuery.setParameter("sd", startDate))
        .andReturn(annQuery);
    expect(annQuery.setParameter("ed", endDate))
        .andReturn(annQuery);

    int i = 0;
    for (String type : annotType) {
      expect(annQuery.setUri("type" + i++, URI.create(type)))
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

    mocksController.replay();

    List<String> annotationIds = annotationService.getFeedAnnotationIds(startDate, endDate,
        new HashSet<String>(annotType), 3);

    assertEquals(annotationIds, expected);

    mocksController.verify();
  }

  @Test
  @DirtiesContext
  public void testDeletAnnotationWithoutBody() {

    expect(mockPDP.evaluate(isA(EvaluationCtx.class)))
        .andReturn(new ResponseCtx(new Result(Result.DECISION_PERMIT)))
        .anyTimes();

    String annotationId = "info:doi/123.456/annotation1";
    FormalCorrection annotation = new FormalCorrection();
    annotation.setId(URI.create(annotationId));

    expect(mockSession.get(ArticleAnnotation.class, annotationId)).andReturn(annotation);
    expect(mockSession.delete(annotation)).andReturn(annotationId);
    mockPermissionsService.cancelPropagatePermissions(eq(annotationId), (String[]) anyObject());
    expectLastCall();
    mockPermissionsService.cancelGrants(eq(annotationId), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall().once();
    mockPermissionsService.cancelRevokes(eq(annotationId), (String[]) anyObject(), (String[]) anyObject());
    expectLastCall();



    mocksController.replay();

    annotationService.deleteAnnotation(annotationId);

    mocksController.verify();


  }

  @Test
  @DirtiesContext
  public void createDefaultCitationForFormalCorrection() throws Exception {
    FormalCorrection fc = new FormalCorrection();
    fc.setId(URI.create("info:doi/annotationId"));
    runCreateDefaultCitationTest(fc, "Correction: ");
  }

  @Test
  @DirtiesContext
  public void createDefaultCitationForRetraction() throws Exception {
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
      TopazAnnotationUtil.createDefaultCitation((FormalCorrection) annotation, articleCitation, session);
      newCitation = ((FormalCorrection) annotation).getBibliographicCitation();
    } else if (annotation instanceof Retraction) {
      TopazAnnotationUtil.createDefaultCitation((Retraction) annotation, articleCitation, session);
      newCitation = ((Retraction) annotation).getBibliographicCitation();
    } else {
      throw new Exception("Wrong annotation type");
    }
    verify(session);
    assertNotNull(newCitation);
    assertNull(newCitation.getId());
    assertEquals(newCitation.getCitationType(), articleCitation.getCitationType());
    assertEquals(newCitation.getDay(), articleCitation.getDay());
    assertEquals(newCitation.getDisplayYear(), "(" + articleCitation.getDisplayYear() + ")");
    assertEquals(newCitation.getDoi(), annotation.getId().toString().replaceFirst("info:doi/", ""));
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
      UserProfile newProfile = newCitation.getAuthors().get(i);
      UserProfile articleProfile = articleCitation.getAuthors().get(i);
      assertNull(newProfile.getId());
      assertUserFields(newProfile, articleProfile);
    }

    assertNotNull(newCitation.getEditors());
    assertEquals(newCitation.getEditors().size(), articleCitation.getEditors().size());
    for (int i = 0; i < newCitation.getEditors().size(); i++) {
      UserProfile newEditor = newCitation.getAuthors().get(i);
      UserProfile articleEditor = articleCitation.getAuthors().get(i);
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
