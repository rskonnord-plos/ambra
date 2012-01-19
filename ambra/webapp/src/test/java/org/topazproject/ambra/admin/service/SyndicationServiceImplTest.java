/*
 * $HeadURL$
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

package org.topazproject.ambra.admin.service;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.assertEquals;
import org.testng.TestException;
import org.topazproject.ambra.admin.service.impl.SyndicationServiceImpl;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.queue.MessageService;
import org.topazproject.ambra.queue.MessageServiceImpl;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createStrictControl;
import org.easymock.classextension.IMocksControl;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The getMostRecentSyndications() method is not being tested because it does not do anything which
 * can be easily or effectively unit tested without a datastore against which to run the query.
 *
 * TODO: Add test to ensure that Articles of type "issue image" do NOT get a Syndication created.
 * TODO:   Even better, wait until there is a proper version of the
 * TODO:   "isCreateSyndicationForArticleType(Set<URI> articleTypes)" method and test that, instead.
 *
 * TODO: test other situations that make syndication process fail.  For instance: no archive file
 * TODO:   name in Article object.
 *
 * @author Scott Sterling
 */
public class SyndicationServiceImplTest {

  private Configuration configuration;

  /**
   * Access a test config file so that the complex behaviors of Configuration do not have to
   * be mocked.
   *
   * @throws ConfigurationException
   */
  @BeforeClass
  public void readConfiguration() throws ConfigurationException {
    String fileName = getClass().getClassLoader().getResource("queue/configuration.xml").getFile();
    configuration = new XMLConfiguration(fileName);
  }

  /**
   * Get new object every time to ensure that tests do not overwrite the contents
   * @return a <b>new</b> test object
   */
  private static Syndication getTestSyndicationOne() throws URISyntaxException {
    return new Syndication("fakeArticleIdOne", "testStatusOne", "testTargetOne", 1,
        new Date(1L), new Date(11L), "testErrorMessageOne");
  }
  private static Syndication getTestSyndicationTwo() throws URISyntaxException {
    return new Syndication("fakeArticleIdTwo", "testStatusTwo", "testTargetTwo", 2,
        new Date(2L), new Date(22L), "testErrorMessageTwo");
  }

  /**
   * Divides the milliseconds in parameter <code>d</code> by 10000 (10 seconds),
   * then rounds the result.  Used for comparing (newly-created) test dates
   * to dates which were auto-generated inside the methods being tested
   *
   * @return milliseconds in parameter <code>d</code> by 10000, rounded to make an int
   */
  private int roundTime(Date d) {
    return Math.round(d.getTime() / 10000);
  }

  /**
   * Test the isCreateSyndicationForArticleType(Set<URI> articleTypes) method.
   *
   * TODO: create this test once there is enough logic in the method to be worth testing.
   */
  @Test
  public void testIsCreateSyndicationForArticleType() {
  }

  @Test
  public void testGetSyndication() throws URISyntaxException {
    IMocksControl ctrl = createStrictControl();

    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // Test Syndication One
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", getTestSyndicationOne().getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationOne());

    // Test Syndication Two
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", getTestSyndicationTwo().getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationTwo());

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    Syndication synOne = synService.getSyndication("fakeArticleIdOne", "testTargetOne");
    Syndication synTwo = synService.getSyndication("fakeArticleIdTwo", "testTargetTwo");

    verify(session);

    assertEquals(synOne.getId(), getTestSyndicationOne().getId());
    assertEquals(synOne.getArticleId(), getTestSyndicationOne().getArticleId());
    assertEquals(synOne.getStatus(), getTestSyndicationOne().getStatus());
    assertEquals(synOne.getTarget(), getTestSyndicationOne().getTarget());
    assertEquals(synOne.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
    assertEquals(synOne.getStatusTimestamp(), getTestSyndicationOne().getStatusTimestamp());
    assertEquals(synOne.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
    assertEquals(synOne.getErrorMessage(), getTestSyndicationOne().getErrorMessage());

    assertEquals(synTwo.getId(), getTestSyndicationTwo().getId());
    assertEquals(synTwo.getArticleId(), getTestSyndicationTwo().getArticleId());
    assertEquals(synTwo.getStatus(), getTestSyndicationTwo().getStatus());
    assertEquals(synTwo.getTarget(), getTestSyndicationTwo().getTarget());
    assertEquals(synTwo.getSubmissionCount(), getTestSyndicationTwo().getSubmissionCount());
    assertEquals(synTwo.getSubmitTimestamp(), getTestSyndicationTwo().getSubmitTimestamp());
    assertEquals(synTwo.getStatusTimestamp(), getTestSyndicationTwo().getStatusTimestamp());
    assertEquals(synTwo.getErrorMessage(), getTestSyndicationTwo().getErrorMessage());
  }

  /**
   * Test the updateSyndication() method by setting a Syndication's <code>status</code>
   * to the "failure" constant.
   */
  @Test
  public void testUpdateSyndicationUpdateToFailure() throws URISyntaxException {
    String errorMessage = "fake error message for testUpdateSyndicationUpdateToFailure().";
    IMocksControl ctrl = createStrictControl();

    SessionFactory sessionFactory = ctrl.createMock(SessionFactory.class);
    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", getTestSyndicationOne().getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationOne());
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn("");

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setSessionFactory(sessionFactory);
    Syndication synReturned = synService.updateSyndication("fakeArticleIdOne", "testTargetOne",
        Syndication.STATUS_FAILURE, errorMessage);

    verify(session);

    assertEquals(synReturned.getId(), getTestSyndicationOne().getId());
    assertEquals(synReturned.getArticleId(), getTestSyndicationOne().getArticleId());
    assertEquals(synReturned.getStatus(), Syndication.STATUS_FAILURE); // Updated
    assertEquals(synReturned.getTarget(), getTestSyndicationOne().getTarget());
    assertEquals(synReturned.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
    assertEquals(roundTime(synReturned.getStatusTimestamp()), roundTime(new Date())); // Updated
    assertEquals(synReturned.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
    assertEquals(synReturned.getErrorMessage(), errorMessage); // Updated
  }

  /**
   * Test the updateSyndication() method by setting a Syndication's <code>status</code>
   * to the "pending" constant.  This update should fail, so the resulting Syndication is the same.
   */
  @Test
  public void testUpdateSyndicationUpdateToPending() throws URISyntaxException {
    IMocksControl ctrl = createStrictControl();

    SessionFactory sessionFactory = ctrl.createMock(SessionFactory.class);
    Session session = ctrl.createMock(Session.class);
    Results results = ctrl.createMock(Results.class);
    Query query = ctrl.createMock(Query.class);

    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", getTestSyndicationTwo().getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationTwo());
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn("");

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setSessionFactory(sessionFactory);
    Syndication synReturned = synService.updateSyndication("fakeArticleIdTwo", "testTargetTwo",
        Syndication.STATUS_PENDING, null);

    verify(session);

    assertEquals(synReturned.getId(), getTestSyndicationTwo().getId());
    assertEquals(synReturned.getArticleId(), getTestSyndicationTwo().getArticleId());
    assertEquals(synReturned.getStatus(), getTestSyndicationTwo().getStatus());
    assertEquals(synReturned.getTarget(), getTestSyndicationTwo().getTarget());
    assertEquals(synReturned.getSubmissionCount(), getTestSyndicationTwo().getSubmissionCount());
    assertEquals(synReturned.getStatusTimestamp(), getTestSyndicationTwo().getStatusTimestamp());
    assertEquals(synReturned.getSubmitTimestamp(), getTestSyndicationTwo().getSubmitTimestamp());
    assertEquals(synReturned.getErrorMessage(), getTestSyndicationTwo().getErrorMessage());
  }

  /**
   * Test the createSyndication() method for an Article which has no associated Syndication objects.
   */
  @Test
  public void testCreateSyndicationsNoExistingSyndications() throws URISyntaxException {
    Article article = new Article();
    try {
      article.setId(new URI("fakeArticleId"));
    } catch (URISyntaxException e) {
      throw new TestException("Could not set Article.id = fakeArticleId" +
          " for test Article returned by method getTestArticleWithSyndications()");
    }
    IMocksControl ctrl = createStrictControl();

    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // Target PMC
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "PMC"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(false);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // Target FOO
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "FOO"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(false);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // Target BAR
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "BAR"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(false);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // from method: querySyndication(String articleId)
    expect(session.createQuery("select syn from Syndication syn where syn.articleId = :articleId;"))
        .andReturn(query);
    expect(query.setParameter("articleId", new URI("fakeArticleId"))).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(new Syndication("fakeArticleId", "PMC"));
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(new Syndication("fakeArticleId", "FOO"));
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(new Syndication("fakeArticleId", "BAR"));
    expect(results.next()).andReturn(false); // End of the Results loop.

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setAmbraConfiguration(configuration); // Set from XML file
    List<SyndicationService.SyndicationDTO> syndications
        = synService.createSyndications("fakeArticleId");
    Object[] synArray = syndications.toArray();

    verify(session);

    //  Make sure the Syndications were created as expected.  Order given by XML config file.

    // First Syndication should be for target PMC.
    SyndicationService.SyndicationDTO synOne = (SyndicationService.SyndicationDTO)synArray[0];
    assertEquals(synOne.getSyndicationId(), Syndication.makeSyndicationId("fakeArticleId", "PMC"));
    assertEquals(synOne.getArticleId(), "fakeArticleId");
    assertEquals(synOne.getStatus(), Syndication.STATUS_PENDING);
    assertEquals(synOne.getTarget(), "PMC");
    assertEquals(synOne.getSubmissionCount(), 0);
    assertEquals(roundTime(synOne.getStatusTimestamp()), roundTime(new Date()));
    assertEquals(synOne.getSubmitTimestamp(), null); // Has not been submitted yet.
    assertEquals(synOne.getErrorMessage(), null);

    // Second Syndication should be for target BAR.
    SyndicationService.SyndicationDTO synTwo = (SyndicationService.SyndicationDTO)synArray[1];
    assertEquals(synTwo.getSyndicationId(), Syndication.makeSyndicationId("fakeArticleId", "FOO"));
    assertEquals(synTwo.getArticleId(), "fakeArticleId");
    assertEquals(synTwo.getStatus(), Syndication.STATUS_PENDING);
    assertEquals(synTwo.getTarget(), "FOO");
    assertEquals(synTwo.getSubmissionCount(), 0);
    assertEquals(roundTime(synTwo.getStatusTimestamp()), roundTime(new Date()));
    assertEquals(synTwo.getSubmitTimestamp(), null); // Has not been submitted yet.
    assertEquals(synTwo.getErrorMessage(), null);

    // Third Syndication should be for target FOO.
    SyndicationService.SyndicationDTO synThree = (SyndicationService.SyndicationDTO)synArray[2];
    assertEquals(synThree.getSyndicationId(), Syndication.makeSyndicationId("fakeArticleId", "BAR"));
    assertEquals(synThree.getArticleId(), "fakeArticleId");
    assertEquals(synThree.getStatus(), Syndication.STATUS_PENDING);
    assertEquals(synThree.getTarget(), "BAR");
    assertEquals(synThree.getSubmissionCount(), 0);
    assertEquals(roundTime(synThree.getStatusTimestamp()), roundTime(new Date()));
    assertEquals(synThree.getSubmitTimestamp(), null); // Has not been submitted yet.
    assertEquals(synThree.getErrorMessage(), null);
  }

  /**
   * Test the createSyndication() method for an Article which already has some associated
   * Syndication objects.
   */
  @Test
  public void testCreateSyndicationsWithExistingSyndications() throws URISyntaxException {
    Article article = new Article();
    try {
      article.setId(new URI("fakeArticleId"));
    } catch (URISyntaxException e) {
      throw new TestException("Could not set Article.id = fakeArticleId" +
          " for test Article returned by method getTestArticleWithSyndications()");
    }
    Syndication syndicationPMCAlreadyExists = new Syndication("fakeArticleId", "PMC");
    Syndication syndicationBARAlreadyExists = new Syndication("fakeArticleId", "BAR");

    IMocksControl ctrl = createStrictControl();

    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // Target PMC (Syndication for this target already exists for this article)
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "PMC"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndicationPMCAlreadyExists);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // Target FOO (Syndication for this target does NOT yet exist for this article)
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "FOO"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(false);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // Target BAR (Syndication for this target already exists for this article)
    // from method: createSyndications
    expect(session.get(eq(Article.class), eq("fakeArticleId"))).andReturn(article);
    // from method: querySyndication(String articleId, String syndicationTarget, Session session)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", new URI(Syndication.makeSyndicationId("fakeArticleId", "BAR"))))
        .andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndicationBARAlreadyExists);
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" new Syndication object to datastore.

    // from method: querySyndication(String articleId)
    expect(session.createQuery("select syn from Syndication syn where syn.articleId = :articleId;"))
        .andReturn(query);
    expect(query.setParameter("articleId", new URI("fakeArticleId"))).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndicationPMCAlreadyExists);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(new Syndication("fakeArticleId", "FOO"));
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndicationBARAlreadyExists);
    expect(results.next()).andReturn(false); // End of the Results loop.

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setAmbraConfiguration(configuration); // Set from XML file
    List<SyndicationService.SyndicationDTO> syndications
        = synService.createSyndications("fakeArticleId");
    Object[] synArray = syndications.toArray();

    verify(session);

    //  Make sure the Syndications were created as expected.  Order given by XML config file.

    // First Syndication should be for target PMC.  This one already existed.
    SyndicationService.SyndicationDTO synOne = (SyndicationService.SyndicationDTO)synArray[0];
    assertEquals(synOne.getSyndicationId(), syndicationPMCAlreadyExists.getId().toString());
    assertEquals(synOne.getArticleId(), syndicationPMCAlreadyExists.getArticleId().toString());
    assertEquals(synOne.getStatus(), syndicationPMCAlreadyExists.getStatus());
    assertEquals(synOne.getTarget(), syndicationPMCAlreadyExists.getTarget());
    assertEquals(synOne.getSubmissionCount(), syndicationPMCAlreadyExists.getSubmissionCount());
    assertEquals(synOne.getStatusTimestamp(), syndicationPMCAlreadyExists.getStatusTimestamp());
    assertEquals(synOne.getSubmitTimestamp(), syndicationPMCAlreadyExists.getSubmitTimestamp());
    assertEquals(synOne.getErrorMessage(), syndicationPMCAlreadyExists.getErrorMessage());

    // Second Syndication should be for target BAR.  This one was created.
    SyndicationService.SyndicationDTO synTwo = (SyndicationService.SyndicationDTO)synArray[1];
    assertEquals(synTwo.getSyndicationId(), Syndication.makeSyndicationId("fakeArticleId", "FOO"));
    assertEquals(synTwo.getArticleId(), "fakeArticleId");
    assertEquals(synTwo.getStatus(), Syndication.STATUS_PENDING);
    assertEquals(synTwo.getTarget(), "FOO");
    assertEquals(synTwo.getSubmissionCount(), 0);
    assertEquals(roundTime(synTwo.getStatusTimestamp()), roundTime(new Date()));
    assertEquals(synTwo.getSubmitTimestamp(), null); // Has not been submitted yet.
    assertEquals(synTwo.getErrorMessage(), null);

    // Third Syndication should be for target FOO.  This one already existed.
    SyndicationService.SyndicationDTO synThree = (SyndicationService.SyndicationDTO)synArray[2];
    assertEquals(synThree.getSyndicationId(), syndicationBARAlreadyExists.getId().toString());
    assertEquals(synThree.getArticleId(), syndicationBARAlreadyExists.getArticleId().toString());
    assertEquals(synThree.getStatus(), syndicationBARAlreadyExists.getStatus());
    assertEquals(synThree.getTarget(), syndicationBARAlreadyExists.getTarget());
    assertEquals(synThree.getSubmissionCount(), syndicationBARAlreadyExists.getSubmissionCount());
    assertEquals(synThree.getStatusTimestamp(), syndicationBARAlreadyExists.getStatusTimestamp());
    assertEquals(synThree.getSubmitTimestamp(), syndicationBARAlreadyExists.getSubmitTimestamp());
    assertEquals(synThree.getErrorMessage(), syndicationBARAlreadyExists.getErrorMessage());
  }

  @Test
  public void testGetFailedAndInProgressSyndications() throws URISyntaxException {
    String queryString = "select syn, timestamp, articleId from Syndication syn, Article a "
        + "where timestamp := syn.statusTimestamp and articleId := a.id "
        + "and syn.articleId = articleId "
        + "and ( syn.status = '" + Syndication.STATUS_IN_PROGRESS + "'"
        + "  or syn.status = '" + Syndication.STATUS_FAILURE + "' ) "
        + "and gt(timestamp, :sd) and lt(timestamp, :ed) "
        + "order by articleId asc;";
    Calendar start = Calendar.getInstance();
    start.set(Calendar.HOUR, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);

    Calendar end = (Calendar) start.clone(); // The most recent midnight (last night)

    start.add(Calendar.DATE, -(30));  //  Use the default value for "days in the past".
    end.add(Calendar.DATE, 1); // Include everything that happened today.

    IMocksControl ctrl = createStrictControl();

    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    expect(session.createQuery(queryString)).andReturn(query);
    expect(query.setParameter("sd", start)).andReturn(query);
    expect(query.setParameter("ed", end)).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationOne());
    expect(results.next()).andReturn(false);

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setAmbraConfiguration(configuration); // Set from XML file
    synService.getFailedAndInProgressSyndications("fakeJournalKey");

    verify(session);
    // No assertions, since we do not actually get anything back from the datastore.
  }

  /**
   * Test the asynchronousUpdateSyndication() method by setting a Syndication's <code>status</code>
   * to the "failure" constant.
   */
  @Test
  public void testAsynchronousUpdateSyndicationToFailure() throws Exception {
    String errorMessage = "fake error message for testUpdateSyndicationUpdateToFailure().";
    IMocksControl ctrl = createStrictControl();

    SessionFactory sessionFactory = ctrl.createMock(SessionFactory.class);
    Session session = ctrl.createMock(Session.class);
    Transaction transaction = ctrl.createMock(Transaction.class);
    Cache objectCache = ctrl.createMock(Cache.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // from method: asynchronousUpdateSyndication()
    expect(sessionFactory.openSession()).andReturn(session);
    expect(session.beginTransaction()).andReturn(transaction);
    // from method: querySyndication(session, articleId, syndicationTarget, status, errorMessage)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", getTestSyndicationOne().getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationOne());
    // from method: updateSyndication
    expect(session.saveOrUpdate(isA(Syndication.class)))
        .andReturn(getTestSyndicationOne().getId().toString()); // "commit" Syn to datastore
    // from method: asynchronousUpdateSyndication()
    objectCache.remove(getTestSyndicationOne().getId().toString()); // evict Syn from object cache
    transaction.commit();
    session.close();

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setSessionFactory(sessionFactory);
    synService.setOtmSession(session);
    synService.setObjectCache(objectCache);
    Syndication synReturned = synService.asynchronousUpdateSyndication(
        "fakeArticleIdOne", "testTargetOne", Syndication.STATUS_FAILURE, errorMessage);

    verify(session);

    assertEquals(synReturned.getId(), getTestSyndicationOne().getId());
    assertEquals(synReturned.getArticleId(), getTestSyndicationOne().getArticleId());
    assertEquals(synReturned.getStatus(), Syndication.STATUS_FAILURE); // Updated
    assertEquals(synReturned.getTarget(), getTestSyndicationOne().getTarget());
    assertEquals(synReturned.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
    assertEquals(roundTime(synReturned.getStatusTimestamp()), roundTime(new Date())); // Updated
    assertEquals(synReturned.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
    assertEquals(synReturned.getErrorMessage(), errorMessage); // Updated
  }

  /**
   * Test the syndicate() method.  Simulates the successful creation of a message and the pushing of
   * that message to the plos-queue functionality.
   */
  @Test
  public void testSyndicate() throws Exception {
    Article article = new Article();
    try {
      article.setId(new URI("fakeArticleIdOne"));
    } catch (URISyntaxException e) {
      throw new TestException("Could not set Article.id = fakeArticleId" +
          " for test Article returned by method getTestArticleWithSyndications()");
    }
    // The Article needs to know the name of the archive file that will be syndicated.
    article.setArchiveName("fakeArchiveFileName.zip");
    Syndication syndication = getTestSyndicationOne();

    IMocksControl ctrl = createStrictControl();
    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);
    MessageService messageService = ctrl.createMock(MessageService.class);

    // from method: syndicate
    expect(session.get(eq(Article.class), eq("fakeArticleIdOne"))).andReturn(article);
    // from method: syndicate
    messageService.sendSyndicationMessage(
        syndication.getTarget(), article.getId().toString(), article.getArchiveName());
    // from method: querySyndication(session, articleId, syndicationTarget, status, errorMessage)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", syndication.getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndication);
    // from method: updateSyndication
    expect(session.saveOrUpdate(syndication)).andReturn("");
    // from method: syndicate
    expect(session.saveOrUpdate(syndication)).andReturn("");

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.setAmbraConfiguration(configuration); // Set from XML file
    synService.setMessageService(messageService);
    synService.syndicate("fakeArticleIdOne", getTestSyndicationOne().getTarget());

    verify(session);

    assertEquals(syndication.getId(), getTestSyndicationOne().getId());
    assertEquals(syndication.getArticleId(), getTestSyndicationOne().getArticleId());
    assertEquals(syndication.getStatus(), Syndication.STATUS_IN_PROGRESS); // Updated
    assertEquals(syndication.getTarget(), getTestSyndicationOne().getTarget());
    assertEquals(syndication.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount() + 1); // Updated
    assertEquals(roundTime(syndication.getStatusTimestamp()), roundTime(new Date())); // Updated
    assertEquals(syndication.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
    assertEquals(syndication.getErrorMessage(), getTestSyndicationOne().getErrorMessage());
  }

  /**
   * Test the syndicate() method.  Simulates the creation of a message and a failure when trying to
   * push that message to the plos-queue functionality.
   * This Exception is thrown from method messageService.sendSyndicationMessage() because the
   * variable "queue" is null, but trying to "expect" the call to
   * "messageService.sendSyndicationMessage()" is evidently wrong.
   */
  @Test
  public void testSyndicateWhereQueueThrowsException() throws Exception {
    Article article = new Article();
    try {
      article.setId(new URI("fakeArticleIdOne"));
    } catch (URISyntaxException e) {
      throw new TestException("Could not set Article.id = fakeArticleId" +
          " for test Article returned by method getTestArticleWithSyndications()");
    }
    // The Article needs to know the name of the archive file that will be syndicated.
    article.setArchiveName("fakeArchiveFileName.zip");
    Syndication syndication = getTestSyndicationOne();

    IMocksControl ctrl = createStrictControl();
    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);
    
    // from method: syndicate
    expect(session.get(eq(Article.class), eq("fakeArticleIdOne"))).andReturn(article);
    // from method: querySyndication(session, articleId, syndicationTarget, status, errorMessage)
    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
        .andReturn(query);
    expect(query.setParameter("id", syndication.getId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(syndication);
    // from method: updateSyndication
    expect(session.saveOrUpdate(syndication)).andReturn("");

    replay(session);

    MessageServiceImpl messageService2 = new MessageServiceImpl();
    messageService2.setAmbraConfiguration(configuration);
    
    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setMessageService(messageService2);
    synService.setOtmSession(session);
    synService.setAmbraConfiguration(configuration); // Set from XML file
    synService.syndicate("fakeArticleIdOne", getTestSyndicationOne().getTarget());

    verify(session);

    assertEquals(syndication.getId(), getTestSyndicationOne().getId());
    assertEquals(syndication.getArticleId(), getTestSyndicationOne().getArticleId());
    assertEquals(syndication.getStatus(), Syndication.STATUS_FAILURE); // Updated
    assertEquals(syndication.getTarget(), getTestSyndicationOne().getTarget());
    assertEquals(syndication.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
    assertEquals(roundTime(syndication.getStatusTimestamp()), roundTime(new Date())); // Updated
    assertEquals(syndication.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
    assertEquals(syndication.getErrorMessage(), "testTargetOne queue not configured");
  }

  @Test
  public void testQuerySyndication() throws URISyntaxException {
    IMocksControl ctrl = createStrictControl();

    Session session = ctrl.createMock(Session.class);
    Query query = ctrl.createMock(Query.class);
    Results results = ctrl.createMock(Results.class);

    // from method: querySyndication(String articleId)
    expect(session.createQuery("select syn from Syndication syn where syn.articleId = :articleId;"))
        .andReturn(query);
    expect(query.setParameter("articleId", getTestSyndicationOne().getArticleId())).andReturn(query);
    expect(query.execute()).andReturn(results);
    expect(results.next()).andReturn(true);
    expect(results.get(0)).andReturn(getTestSyndicationOne());
    expect(results.next()).andReturn(false);

    replay(session);

    SyndicationServiceImpl synService = new SyndicationServiceImpl();
    synService.setOtmSession(session);
    synService.querySyndication(getTestSyndicationOne().getArticleId().toString());

    verify(session);
    // No assertions, since we do not actually get anything back from the datastore.
  }
}