/*
 * $HeadURL$
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

package org.topazproject.ambra.admin.service;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.assertEquals;
import org.testng.TestException;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.cache.MockCache;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.CacheManager;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.query.Results;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createStrictControl;
import org.easymock.classextension.IMocksControl;
import static org.easymock.EasyMock.expectLastCall;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.LinkedHashSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Array;

/**
 * The getMostRecentSyndications() method is not being tested because it does not do anything which
 * can be easily or effectively unit tested without a datastore against which to run the query.
 *
 * @author Scott Sterling
 */
public class SyndicationServiceImplTest {

  private Configuration configuration;

  /**
   * Access a fake config file so that the complex behaviors of Configuration do not have to
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
  private Article getTestArticleWithSyndications() throws URISyntaxException {
    Article article = new Article();
//    try {
//      article.setId(new URI("fakeArticleId"));
//    } catch (URISyntaxException e) {
//      throw new TestException("Could not set Article.id = fakeArticleId" +
//          " for test Article returned by method getTestArticleWithSyndications()");
//    }
//    article.setSyndications(new HashSet<Syndication>());
//    article.getSyndications().add(getTestSyndicationOne());
//    article.getSyndications().add(getTestSyndicationTwo());
    return article;
  }

  /**
   * Get new object every time to ensure that tests do not overwrite the contents
   * @return a <b>new</b> test object
   */
  private Syndication getTestSyndicationOne() throws URISyntaxException {
    return new Syndication("fakeArticleId", "testStatusOne", "testTargetOne", 1,
        new Date(1L), new Date(11L), "testErrorMessageOne");
  }

  /**
   * Get new object every time to ensure that tests do not overwrite the contents
   * @return a <b>new</b> test object
   */
  private Syndication getTestSyndicationTwo() throws URISyntaxException {
    return new Syndication("fakeArticleId", "testStatusTwo", "testTargetTwo", 2,
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
   * Test the getSyndication() method.
   */
  @Test
  public void testGetSyndication() throws URISyntaxException {
//    IMocksControl ctrl = createStrictControl();
//
//    Session session = ctrl.createMock(Session.class);
//    Results results = ctrl.createMock(Results.class);
//    Query query = ctrl.createMock(Query.class);
//    CacheManager cacheManager = ctrl.createMock(CacheManager.class);
//
//    MockCache cache = new MockCache();
//    cache.setCacheManager(cacheManager);
//    cacheManager.registerListener(isA(AbstractObjectListener.class));
//    expectLastCall().times(0,1);
//
//    // Test Syndication One
//    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
//        .andReturn(query);
//    expect(query.setParameter("id", getTestSyndicationOne().getId())).andReturn(query);
//    expect(query.execute()).andReturn(results);
//    expect(results.next()).andReturn(true);
//    expect(results.get(0)).andReturn(getTestSyndicationOne());
//
//    // Test Syndication Two
//    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
//        .andReturn(query);
//    expect(query.setParameter("id", getTestSyndicationTwo().getId())).andReturn(query);
//    expect(query.execute()).andReturn(results);
//    expect(results.next()).andReturn(true);
//    expect(results.get(0)).andReturn(getTestSyndicationTwo());
//
//    replay(session);
//
//    SyndicationServiceImpl synService = new SyndicationServiceImpl();
//    synService.setOtmSession(session);
//    Syndication synOne = synService.getSyndication(
//        getTestArticleWithSyndications().getId().toString(), "testTargetOne");
//    Syndication synTwo = synService.getSyndication(
//        getTestArticleWithSyndications().getId().toString(), "testTargetTwo");
//
//    verify(session);
//
//    assertEquals(synOne.getStatus(), getTestSyndicationOne().getStatus());
//    assertEquals(synOne.getTarget(), getTestSyndicationOne().getTarget());
//    assertEquals(synOne.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
//    assertEquals(synOne.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
//    assertEquals(synOne.getStatusTimestamp(), getTestSyndicationOne().getStatusTimestamp());
//    assertEquals(synOne.getErrorMessage(), getTestSyndicationOne().getErrorMessage());
//
//    assertEquals(synTwo.getStatus(), getTestSyndicationTwo().getStatus());
//    assertEquals(synTwo.getTarget(), getTestSyndicationTwo().getTarget());
//    assertEquals(synTwo.getSubmissionCount(), getTestSyndicationTwo().getSubmissionCount());
//    assertEquals(synTwo.getSubmitTimestamp(), getTestSyndicationTwo().getSubmitTimestamp());
//    assertEquals(synTwo.getStatusTimestamp(), getTestSyndicationTwo().getStatusTimestamp());
//    assertEquals(synTwo.getErrorMessage(), getTestSyndicationTwo().getErrorMessage());
  }

  /**
   * Test the updateSyndications() method by setting a Syndication's <code>status</code>
   * to the "failure" constant.
   *
   * TODO: add test for the OTHER "updateSyndication" method!
   */
  @Test
  public void testUpdateSyndicationUpdateToFailure() throws URISyntaxException {
//    Article article = getTestArticleWithSyndications();
//    String articleId = getTestArticleWithSyndications().getId().toString();
//    String syndicationTarget = getTestSyndicationOne().getTarget();
//    String status = Syndication.STATUS_FAILURE;
//    String errorMessage = "fake error message for testUpdateSyndicationUpdateToFailure().";
//
//    IMocksControl ctrl = createStrictControl();
//    SessionFactory sessionFactory = ctrl.createMock(SessionFactory.class);
//    Session session = ctrl.createMock(Session.class);
//    Results results = ctrl.createMock(Results.class);
//    Query query = ctrl.createMock(Query.class);
//    CacheManager cacheManager = ctrl.createMock(CacheManager.class);
//
//    MockCache cache = new MockCache();
//    cache.setCacheManager(cacheManager);
//    cacheManager.registerListener(isA(AbstractObjectListener.class));
//    expectLastCall().times(0,1);
//
//
//    expect(sessionFactory.openSession()).andReturn(session);
//    expect(session.get(eq(Article.class), eq(articleId))).andReturn(article); // from querySyndication.
//    // from method: querySyndication
//    expect(session.createQuery("select syn from Syndication syn where syn.id = :id;"))
//        .andReturn(query);
//    expect(query.setParameter("id", getTestSyndicationOne().getId())).andReturn(query);
//    expect(query.execute()).andReturn(results);
//    expect(results.next()).andReturn(true);
//    expect(results.get(0)).andReturn(getTestSyndicationOne());
//
//
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    replay(session);
//
//    SyndicationServiceImpl synService = new SyndicationServiceImpl();
//    synService.setOtmSession(session);
//    synService.setSessionFactory(sessionFactory);
//    Syndication synReturned = synService.updateSyndication(
//        articleId, syndicationTarget, status, errorMessage);
//
//    verify(session);
//
//    assertEquals(synReturned.getStatus(), status); // Updated
//    assertEquals(synReturned.getTarget(), getTestSyndicationOne().getTarget());
//    assertEquals(synReturned.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
//    assertEquals(synReturned.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
//    assertEquals(roundTime(synReturned.getStatusTimestamp()), roundTime(new Date())); // Updated
//    assertEquals(synReturned.getErrorMessage(), errorMessage);
  }

  /**
   * Test the updateSyndications() method by setting a Syndication's <code>status</code>
   * to the "pending" constant.  This update will fail.
   *
   * TODO: add test for the OTHER "updateSyndication" method!
   */
  @Test
  public void testUpdateSyndicationUpdateToPending() throws URISyntaxException {
//    Article article = getTestArticleWithSyndications();
//    String articleId = getTestArticleWithSyndications().getId().toString();
//    String syndicationTarget = getTestSyndicationOne().getTarget();
//
//    //  Set the Status to PENDING for the first Syndication
//    Iterator<Syndication> synIterator = article.getSyndications().iterator();
//    while (synIterator.hasNext()) {
//      Syndication syn = synIterator.next();
//      if (syn.getTarget().equals(getTestSyndicationOne().getTarget())) {
//        syn.setStatus(Syndication.STATUS_PENDING);
//        break;
//      }
//    }
//
//    Session session = createMock(Session.class);
//    expect(session.get(eq(Article.class), eq(articleId))).andReturn(article)
//        .times(2);  //  from querySyndication and updateSyndication.
//    replay(session);
//
//    SyndicationServiceImpl synService = new SyndicationServiceImpl();
//    synService.setOtmSession(session);
//    Syndication synReturned = synService.updateSyndication( // Try and fail to update the status.
//        articleId, syndicationTarget, Syndication.STATUS_PENDING, null);
//    Syndication synTwo = synService.getSyndication(articleId, "testTargetTwo");
//
//    verify(session);
//
//    //  No changes.
//    assertEquals(synReturned.getStatus(), Syndication.STATUS_PENDING); // Previously set to "pending".
//    assertEquals(synReturned.getTarget(), getTestSyndicationOne().getTarget());
//    assertEquals(synReturned.getSubmissionCount(), getTestSyndicationOne().getSubmissionCount());
//    assertEquals(synReturned.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
//    assertEquals(synReturned.getStatusTimestamp(), getTestSyndicationOne().getStatusTimestamp());
//    assertEquals(synReturned.getErrorMessage(), getTestSyndicationOne().getErrorMessage());
//
//    //  No changes.
//    assertEquals(synTwo.getStatus(), getTestSyndicationTwo().getStatus());
//    assertEquals(synTwo.getTarget(), getTestSyndicationTwo().getTarget());
//    assertEquals(synTwo.getSubmissionCount(), getTestSyndicationTwo().getSubmissionCount());
//    assertEquals(synTwo.getSubmitTimestamp(), getTestSyndicationTwo().getSubmitTimestamp());
//    assertEquals(synTwo.getStatusTimestamp(), getTestSyndicationTwo().getStatusTimestamp());
//    assertEquals(synTwo.getErrorMessage(), getTestSyndicationTwo().getErrorMessage());
  }

  /**
   * Test the createSyndication() method for an Article which has no attached Syndication objects.
   *
   * TODO: Add test to ensure that Articles of type "issue image" do NOT get a Syndication created.
   */
  @Test
  public void testCreateSyndicationsNoExistingSyndications() throws URISyntaxException {
//    Article article = new Article();
//    try {
//      article.setId(new URI("fakeArticleId"));
//    } catch (URISyntaxException e) {
//      throw new TestException("Could not set Article.id = fakeArticleId" +
//          " for test Article returned by method getTestArticleWithSyndications()");
//    }
//
//    Session session = createMock(Session.class);
//
//    // Target PMC
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    // Target FOO
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    // Target BAR
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    // Extra call at start of loop (where?) .
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article);
//
//    replay(session);
//
//    SyndicationServiceImpl synService = new SyndicationServiceImpl();
//    synService.setOtmSession(session);
//    synService.setAmbraConfiguration(configuration); // Set from XML file
//    Set<Syndication> syndications = synService.createSyndications(article.getId().toString());
//    Object[] synArray = syndications.toArray();
//
//    verify(session);
//
//    //  Make sure the Syndications were created as expected.  Order given by XML config file.
//
//    // First Syndication should be for target PMC.
//    Syndication synOne = (Syndication)synArray[0];
//    assertEquals(synOne.getStatus(), Syndication.STATUS_PENDING);
//    assertEquals(synOne.getTarget(), "PMC");
//    assertEquals(synOne.getSubmissionCount(), 0);
//    assertEquals(synOne.getSubmitTimestamp(), null); // Has not been submitted yet.
//    assertEquals(roundTime(synOne.getStatusTimestamp()), roundTime(new Date()));
//    assertEquals(synOne.getErrorMessage(), null);
//
//    // Second Syndication should be for target BAR.
//    Syndication synTwo = (Syndication)synArray[1];
//    assertEquals(synTwo.getStatus(), Syndication.STATUS_PENDING);
//    assertEquals(synTwo.getTarget(), "BAR");
//    assertEquals(synTwo.getSubmissionCount(), 0);
//    assertEquals(synTwo.getSubmitTimestamp(), null); // Has not been submitted yet.
//    assertEquals(roundTime(synTwo.getStatusTimestamp()), roundTime(new Date()));
//    assertEquals(synTwo.getErrorMessage(), null);
//
//    // Third Syndication should be for target FOO.
//    Syndication synThree = (Syndication)synArray[2];
//    assertEquals(synThree.getStatus(), Syndication.STATUS_PENDING);
//    assertEquals(synThree.getTarget(), "FOO");
//    assertEquals(synThree.getSubmissionCount(), 0);
//    assertEquals(synThree.getSubmitTimestamp(), null); // Has not been submitted yet.
//    assertEquals(roundTime(synThree.getStatusTimestamp()), roundTime(new Date()));
//    assertEquals(synThree.getErrorMessage(), null);
  }

  /**
   * Test the createSyndication() method for an Article which already has some
   * attached Syndication objects.
   *
   * TODO: Add test to ensure that Articles of type "issue image" do NOT get a Syndication created.
   */
  @Test
  public void testCreateSyndicationsWithExistingSyndications() throws URISyntaxException {
//    //TODO: fix this test method.
//    Article article = getTestArticleWithSyndications();
//    Object[] synArray = article.getSyndications().toArray();
//    ((Syndication)synArray[0]).setStatus(Syndication.STATUS_SUCCESS);
//    ((Syndication)synArray[0]).setSubmissionCount(100);
//    ((Syndication)synArray[0]).setErrorMessage(null);
//    ((Syndication)synArray[0]).setTarget("PMC");
//
//    ((Syndication)synArray[1]).setStatus(Syndication.STATUS_FAILURE);
//    ((Syndication)synArray[1]).setSubmissionCount(222);
//    ((Syndication)synArray[1]).setErrorMessage("Failure message for target: testErrorMessageTwo");
//
//    for (Syndication syn : article.getSyndications() ) {
//      System.out.println();
//      System.out.println("before: " + syn.toString());
//      System.out.println();
//    }
//
//    Session session = createMock(Session.class);
//
//    // Target PMC
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    // No "session.saveOrUpdate()" because PMC is not updated.
//
//    // Target FOO
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    // Target BAR
//    expect(session.get(eq(Article.class), eq(article.getId().toString()))).andReturn(article)
//        .times(3);
//    expect(session.saveOrUpdate(isA(Syndication.class))).andReturn(""); // "commit" Syndication object to datastore.
//
//    replay(session);
//
//    SyndicationServiceImpl synService = new SyndicationServiceImpl();
//    synService.setOtmSession(session);
//    synService.setAmbraConfiguration(configuration); // Set from XML file
//    Set<Syndication> syndications = synService.createSyndications(article.getId().toString());
//
//    for (Syndication syn : syndications) {
//      System.out.println();
//      System.out.println("After: " + syn.toString());
//      System.out.println();
//    }
//    synArray = syndications.toArray();
//
//    verify(session);
//
//    //  Make sure the Syndications were created as expected.  Order given by XML config file.
//
//    // First Syndication should be for target PMC.  Pre-existing Syndication object.  Not Updated.
//    Syndication synOne = (Syndication)synArray[0];
//    assertEquals(synOne.getStatus(), Syndication.STATUS_SUCCESS);
//    assertEquals(synOne.getTarget(), "PMC");
//    assertEquals(synOne.getSubmissionCount(), 100);
//    assertEquals(synOne.getSubmitTimestamp(), getTestSyndicationOne().getSubmitTimestamp());
//    assertEquals(synOne.getStatusTimestamp(), getTestSyndicationOne().getStatusTimestamp());
//    assertEquals(synOne.getErrorMessage(), null);
//
//    // Second Syndication should be for target BAR.
//    Syndication synTwo = (Syndication)synArray[1];
//    assertEquals(synTwo.getStatus(), Syndication.STATUS_PENDING);
//    assertEquals(synTwo.getTarget(), "BAR");
//    assertEquals(synTwo.getSubmissionCount(), 0);
//    assertEquals(synTwo.getSubmitTimestamp(), null); // Has not been submitted yet.
//    assertEquals(roundTime(synTwo.getStatusTimestamp()), roundTime(new Date()));
//    assertEquals(synTwo.getErrorMessage(), null);
//
//    // Third Syndication should be for target FOO.
//    Syndication synThree = (Syndication)synArray[2];
//    assertEquals(synThree.getStatus(), Syndication.STATUS_PENDING);
//    assertEquals(synThree.getTarget(), "FOO");
//    assertEquals(synThree.getSubmissionCount(), 0);
//    assertEquals(synThree.getSubmitTimestamp(), null); // Has not been submitted yet.
//    assertEquals(roundTime(synThree.getStatusTimestamp()), roundTime(new Date()));
//    assertEquals(synThree.getErrorMessage(), null);
//
//    // Fourth Syndication should be for target testTargetTwo.  Pre-existing Syndication object.  Not Updated.
//    Syndication synFour = (Syndication)synArray[3];
//    assertEquals(synFour.getStatus(), Syndication.STATUS_FAILURE);
//    assertEquals(synFour.getTarget(), "testTargetTwo");
//    assertEquals(synFour.getSubmissionCount(), 222);
//    assertEquals(synFour.getSubmitTimestamp(), getTestSyndicationTwo().getSubmitTimestamp());
//    assertEquals(synFour.getStatusTimestamp(), getTestSyndicationTwo().getStatusTimestamp());
//    assertEquals(synFour.getErrorMessage(), "Failure message for target: testErrorMessageTwo");
//
  }

  /**
   * Test the syndicate() method.  Successfully creates a message and pushes it to the plos-queue
   * functionality.
   */
  @Test
  public void testSyndicateSuccess() {
    //TODO: fix this test method.
  }

  /**
   * Test the syndicate() method.  Fails to push a message to the plos-queue functionality.
   */
  @Test
  public void testSyndicateFailure() {
    //TODO: fix this test method.
  }
}
