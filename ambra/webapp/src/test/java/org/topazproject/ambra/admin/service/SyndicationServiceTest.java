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

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Syndication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

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
 * @author Alex Kudlick
 */

@Test(singleThreaded = true)
public class SyndicationServiceTest extends BaseTest {

  @Autowired
  protected SyndicationService syndicationService;

  @DataProvider(name = "storedSyndications")
  public Object[][] getStoredSyndications() {
    String articleId = "id:test-article-1";
    Article article = new Article();
    article.setId(URI.create(articleId));

    Syndication pmc = new Syndication();
    pmc.setArticleId(article.getId());
    pmc.setTarget("PMC");
    pmc.setStatusTimestamp(new Date());
    pmc.setSubmitTimestamp(new Date());
    pmc.setSubmissionCount(1);
    pmc.setStatus(Syndication.STATUS_IN_PROGRESS);
    pmc.setErrorMessage("hello");
    pmc.setId(URI.create(Syndication.makeSyndicationId(articleId, "PMC")));
    dummyDataStore.store(pmc);

    Syndication ebsco = new Syndication();
    ebsco.setArticleId(article.getId());
    ebsco.setTarget("EBSCO");
    ebsco.setStatusTimestamp(new Date());
    ebsco.setSubmitTimestamp(new Date());
    ebsco.setSubmissionCount(10);
    ebsco.setStatus(Syndication.STATUS_FAILURE);
    ebsco.setErrorMessage("ERROR ERROR");
    ebsco.setId(URI.create(Syndication.makeSyndicationId(articleId, "EBSCO")));
    dummyDataStore.store(ebsco);

    return new Object[][]{
        {articleId, "PMC", pmc},
        {articleId, "EBSCO", ebsco}
    };
  }

  @Test(dataProvider = "storedSyndications")
  public void testGetSyndication(String articleId, String target, Syndication original) throws URISyntaxException {
    Syndication syndication = syndicationService.getSyndication(articleId, target);
    assertNotNull(syndication, "returned null syndication");
    compareSyndications(syndication, original);

  }

  /**
   * Test the updateSyndication() method by setting a Syndication's <code>status</code>
   * to the "failure" constant.
   */
  @Test(dataProvider = "storedSyndications",
      dependsOnMethods = {"testGetSyndication","testUpdateSyndicationUpdateToPending"}, alwaysRun = true)
  public void testUpdateSyndicationUpdateToFailure(String articleId, String target, Syndication original) throws URISyntaxException {
    long testStart = new Date().getTime();
    String errorMessage = "a new error message";

    Syndication syndication = syndicationService.updateSyndication(articleId, target,
        Syndication.STATUS_FAILURE, errorMessage);

    assertNotNull(syndication, "returned null syndication");
    assertEquals(syndication.getStatus(), Syndication.STATUS_FAILURE, "Syndication didn't get status updated");
    assertEquals(syndication.getErrorMessage(), errorMessage, "syndication didn't get error message updated");
    assertTrue(syndication.getStatusTimestamp().getTime() >= testStart,"syndication didn't get status timestamp updated");

    //Check that none of the other properties got messed with
    assertEquals(syndication.getId(), original.getId(), "syndication had id changed");
    assertEquals(syndication.getArticleId(), original.getArticleId(), "syndication had article id changed");
    assertEquals(syndication.getSubmissionCount(), original.getSubmissionCount(), "syndication had submission count changed");
    assertEquals(syndication.getTarget(), original.getTarget(), "syndication had target changed");
    assertMatchingDates(syndication.getSubmitTimestamp(), original.getSubmitTimestamp());
  }

  /**
   * Test the updateSyndication() method by setting a Syndication's <code>status</code>
   * to the "pending" constant.  This update should fail, so the resulting Syndication is the same.
   */
  @Test(dataProvider = "storedSyndications",dependsOnMethods = {"testGetSyndication"},alwaysRun = true)
  public void testUpdateSyndicationUpdateToPending(String articleId, String target, Syndication original) throws URISyntaxException {
    Syndication syndication = syndicationService.updateSyndication(articleId,target, Syndication.STATUS_PENDING, null);
    compareSyndications(syndication,original); //nothing should've changed
  }

  @DataProvider(name = "articleNoSyndications")
  public Object[][] getArticleWithNoSyndications() {
    Article article = new Article();
    String articleId = "id:article-no-syndications";
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    return new Object[][]{
        {articleId, new String[]{"FOO","BAR"}}
    };
  }
  /**
   * Test the createSyndication() method for an Article which has no associated Syndication objects.
   */
  @Test(dataProvider = "articleNoSyndications")
  public void testCreateSyndicationsNoExistingSyndications(String articleId, String[] expectedTargets) throws URISyntaxException {
    long testStart = new Date().getTime();
    List<SyndicationService.SyndicationDTO> syndications
        = syndicationService.createSyndications(articleId);
    assertNotNull(syndications, "returned null list of syndications");
    assertEquals(syndications.size(), expectedTargets.length, "returned incorrect number of syndications");
    for (int i = 0; i < syndications.size(); i++) {
      SyndicationService.SyndicationDTO syndicationDTO = syndications.get(i);
      assertNotNull(syndicationDTO, "returned null syndication");
      assertEquals(syndicationDTO.getArticleId(), articleId, "returned syndication with incorrect target");
      assertEquals(syndicationDTO.getTarget(), expectedTargets[i], "returned syndication with incorrect target");
      assertTrue(syndicationDTO.getStatusTimestamp().getTime() >= testStart, "syndication didn't get status timestamp updated");
      assertEquals(syndicationDTO.getStatus(), Syndication.STATUS_PENDING, "syndication didn't get correct status");
    }

  }

  @DataProvider(name = "articleWithSyndications")
  public Object[][] getArticleWithSyndications() {
    Article article = new Article();
    String articleId = "id:article-with-syndications";
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    Syndication syndication = new Syndication();
    syndication.setArticleId(article.getId());
    syndication.setStatus(Syndication.STATUS_IN_PROGRESS);
    syndication.setStatusTimestamp(new Date());
    syndication.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO")));
    syndication.setTarget("FOO");
    dummyDataStore.store(syndication);


    return new Object[][]{
        {articleId, new String[]{"FOO","BAR"}, new Syndication[] {syndication}}
    };
  }
  /**
   * Test the createSyndication() method for an Article which already has some associated
   * Syndication objects. Expects the existing syndications to be for targets that appear before the new ones
   */
  @Test(dataProvider = "articleWithSyndications")
  public void testCreateSyndicationsWithExistingSyndications(String articleId, String[] expectedTargets, Syndication[] existingSyndications) throws URISyntaxException {
    long testStart = new Date().getTime();
    List<SyndicationService.SyndicationDTO> syndications
        = syndicationService.createSyndications(articleId);
    assertNotNull(syndications, "returned null list of syndications");
    assertEquals(syndications.size(), expectedTargets.length, "returned incorrect number of syndications");

    for (int i = 0; i < existingSyndications.length; i++) {
      compareSyndications(syndications.get(i),existingSyndications[i]);
    }

    //new syndications
    for (int i = existingSyndications.length; i < syndications.size(); i++) {
      SyndicationService.SyndicationDTO syndicationDTO = syndications.get(i);
      assertNotNull(syndicationDTO, "returned null syndication");
      assertEquals(syndicationDTO.getArticleId(), articleId, "returned syndication with incorrect target");
      assertEquals(syndicationDTO.getTarget(), expectedTargets[i], "returned syndication with incorrect target");
      assertTrue(syndicationDTO.getStatusTimestamp().getTime() >= testStart, "syndication didn't get status timestamp updated");
      assertEquals(syndicationDTO.getStatus(), Syndication.STATUS_PENDING, "syndication didn't get correct status");
    }
  }

  @DataProvider(name = "failedAndInProgressSyndications")
  public Object[][] getFailedAndInProgressSyndications() {
    String journalKey = "PLoSBio";
    Journal journal = new Journal();
    journal.setKey(journalKey);
    journal.seteIssn("111-120-56");
    dummyDataStore.store(journal);

    Article article = new Article();
    article.seteIssn(journal.geteIssn());
    String articleId = "id:test-article-for-failed-and-in-progress";
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    Syndication failed = new Syndication();
    failed.setStatus(Syndication.STATUS_FAILURE);
    failed.setArticleId(article.getId());
    failed.setSubmissionCount(1);
    failed.setStatusTimestamp(new Date());
    failed.setSubmitTimestamp(new Date());
    failed.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO1")));
    failed.setTarget("FOO1");
    dummyDataStore.store(failed);

    Syndication inProgress = new Syndication();
    inProgress.setStatus(Syndication.STATUS_IN_PROGRESS);
    inProgress.setArticleId(article.getId());
    inProgress.setSubmissionCount(1);
    inProgress.setStatusTimestamp(new Date());
    inProgress.setSubmitTimestamp(new Date());
    inProgress.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO2")));
    inProgress.setTarget("FOO2");
    dummyDataStore.store(inProgress);

    Syndication successful = new Syndication();
    successful.setStatus(Syndication.STATUS_SUCCESS);
    successful.setSubmissionCount(10);
    successful.setStatusTimestamp(new Date());
    successful.setSubmitTimestamp(new Date());
    successful.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO3")));
    successful.setTarget("FOO3");
    dummyDataStore.store(successful);

    Calendar oldDate = Calendar.getInstance();
    oldDate.add(Calendar.YEAR, -1);
    Syndication old = new Syndication();
    old.setStatus(Syndication.STATUS_FAILURE);
    old.setSubmissionCount(10);
    old.setStatusTimestamp(oldDate.getTime());
    old.setSubmitTimestamp(oldDate.getTime());
    old.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO4")));
    old.setTarget("FOO4");
    dummyDataStore.store(old);

    return new Object[][]{
        {journalKey, new Syndication[] {failed, inProgress}}
    };
  }

  @Test(dataProvider = "failedAndInProgressSyndications")
  public void testGetFailedAndInProgressSyndications(String journalKey, Syndication[] expectedSyndications) throws URISyntaxException {
    List<SyndicationService.SyndicationDTO> list = syndicationService.getFailedAndInProgressSyndications(journalKey);
    assertNotNull(list, "returned null syndication list");
    assertEquals(list.size(), expectedSyndications.length, "returned incorrect number of syndications");
    for (SyndicationService.SyndicationDTO syndicationDTO : list) {
      boolean foundMatch = false;
      for (Syndication expected : expectedSyndications) {
        if (expected.getTarget().equals(syndicationDTO.getTarget())) {
          foundMatch = true;
          compareSyndications(syndicationDTO, expected);
          break;
        }
      }
      assertTrue(foundMatch, "returned an unexpected syndication with target: " + syndicationDTO.getTarget());
    }
  }

  @DataProvider(name = "storedSyndicationForAsynchronous")
  public Object[][] getStoredSyndicationsForAsynchronousTests() {
    String articleId = "id:article-for-asynchronous";
    Article article = new Article();
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    Syndication syndication = new Syndication();
    syndication.setTarget("FOO");
    syndication.setArticleId(article.getId());
    syndication.setStatus(Syndication.STATUS_IN_PROGRESS);
    syndication.setSubmissionCount(1);
    syndication.setSubmitTimestamp(new Date());
    syndication.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO")));
    dummyDataStore.store(syndication);

    return new Object[][]{
        {articleId, "FOO", syndication}
    };
  }

  /**
   * Test the asynchronousUpdateSyndication() method by setting a Syndication's <code>status</code>
   * to the "failure" constant.
   */
  @Test(dataProvider = "storedSyndicationForAsynchronous")
  public void testAsynchronousUpdateSyndicationToFailure(String articleId, String target, Syndication original) throws Exception {
    long testStart = new Date().getTime();
    String errorMessage = "a new error message for asynchronous";

    Syndication syndication = syndicationService.updateSyndication(articleId, target,
        Syndication.STATUS_FAILURE, errorMessage);

    assertNotNull(syndication, "returned null syndication");
    assertEquals(syndication.getStatus(), Syndication.STATUS_FAILURE, "Syndication didn't get status updated");
    assertEquals(syndication.getErrorMessage(), errorMessage, "syndication didn't get error message updated");
    assertTrue(syndication.getStatusTimestamp().getTime() >= testStart, "syndication didn't get status timestamp updated");

    //Check that none of the other properties got messed with
    assertEquals(syndication.getId(), original.getId(), "syndication had id changed");
    assertEquals(syndication.getArticleId(), original.getArticleId(), "syndication had article id changed");
    assertEquals(syndication.getSubmissionCount(), original.getSubmissionCount(), "syndication had submission count changed");
    assertEquals(syndication.getTarget(), original.getTarget(), "syndication had target changed");
    assertMatchingDates(syndication.getSubmitTimestamp(), original.getSubmitTimestamp());
  }

  @DataProvider(name = "articleForSyndication")
  public Object[][] getArticleForSyndication() {
    Article article = new Article();
    String articleId = "id:article-for-syndication";
    article.setArchiveName("archive.zip");
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    Syndication previousSyndication = new Syndication();
    previousSyndication.setStatus(Syndication.STATUS_IN_PROGRESS);
    previousSyndication.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO")));
    previousSyndication.setSubmissionCount(1);
    previousSyndication.setArticleId(article.getId());
    previousSyndication.setTarget("FOO");
    dummyDataStore.store(previousSyndication);


    return new Object[][]{
        {articleId, "FOO", 1},
        {articleId, "BAR", 0}
    };
  }

  /**
   * Test the syndicate() method.  Simulates the successful creation of a message and the pushing of
   * that message to the plos-queue functionality.
   */
  @Test(dataProvider = "articleForSyndication")
  public void testSyndicate(String articleId, String target, int previousSubmissionCount) throws Exception {
    long testStart = new Date().getTime();
    Syndication syndication = syndicationService.syndicate(articleId, target);
    assertNotNull(syndication, "returned null syndication");
    assertEquals(syndication.getArticleId(), URI.create(articleId), "syndication had incorrect article id");
    assertEquals(syndication.getTarget(), target, "syndication had incorrect target");
    assertEquals(syndication.getSubmissionCount(), previousSubmissionCount + 1, "syndication didn't get submission count updated");
    assertNotNull(syndication.getSubmitTimestamp(), "syndication had null submit timestamp");
    assertTrue(syndication.getSubmitTimestamp().getTime() >= testStart, "syndication didn't get submit timestamp updated");
    assertEquals(syndication.getStatus(), Syndication.STATUS_IN_PROGRESS, "syndication didn't get status set correctly");
  }
  @DataProvider(name = "articleWithSyndicationsToQuery")
  public Object[][] getArticleWithSyndicationsToQuery() {
    String articleId = "id:article-with-syndications-to-query";
    Article article = new Article();
    article.setId(URI.create(articleId));
    dummyDataStore.store(article);

    List<String> expectedTargets = new ArrayList<String>(2);
    Syndication syndication1 = new Syndication();
    syndication1.setArticleId(article.getId());
    syndication1.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO10000")));
    syndication1.setTarget("FOO10000");
    dummyDataStore.store(syndication1);
    expectedTargets.add(syndication1.getTarget());

    Syndication syndication2 = new Syndication();
    syndication2.setArticleId(article.getId());
    syndication2.setId(URI.create(Syndication.makeSyndicationId(articleId, "FOO10001")));
    syndication2.setTarget("FOO10001");
    dummyDataStore.store(syndication2);
    expectedTargets.add(syndication2.getTarget());


    return new Object[][]{
        {articleId, expectedTargets}
    };
  }

  @Test(dataProvider = "articleWithSyndicationsToQuery")
  public void testQuerySyndication(String articleId, List<String> expectedTargets) throws URISyntaxException {
    List<SyndicationService.SyndicationDTO> syndicationDTOs = syndicationService.querySyndication(articleId);
    assertNotNull(syndicationDTOs, "returned null list of syndications");
    assertEquals(syndicationDTOs.size(), expectedTargets.size(), "returned incorrect number of syndications");
    for (SyndicationService.SyndicationDTO syndicationDTO : syndicationDTOs) {
      assertNotNull(syndicationDTO, "returned null syndication");
      assertEquals(syndicationDTO.getArticleId(), articleId, "syndication had incorrect article id");
      assertTrue(expectedTargets.contains(syndicationDTO.getTarget()),
          "returned syndication for unexpected target: " + syndicationDTO.getTarget());
    }
  }

  private void compareSyndications(Syndication actual, Syndication expected) {
    assertEquals(actual.getId(), expected.getId(), "syndication had incorrect id");
    assertEquals(actual.getArticleId(), expected.getArticleId(), "syndication had incorrect article id");
    assertEquals(actual.getStatus(), expected.getStatus(), "syndication had incorrect status");
    assertEquals(actual.getErrorMessage(), expected.getErrorMessage(), "syndication had incorrect error message");
    assertEquals(actual.getSubmissionCount(), expected.getSubmissionCount(), "syndication had incorrect submission count");
    assertEquals(actual.getTarget(), expected.getTarget(), "syndication had incorrect target");
    assertMatchingDates(actual.getStatusTimestamp(), expected.getStatusTimestamp());
    assertMatchingDates(actual.getSubmitTimestamp(), expected.getSubmitTimestamp());
  }
  private void compareSyndications(SyndicationService.SyndicationDTO actual, Syndication expected) {
    assertEquals(actual.getSyndicationId(), expected.getId().toString(), "syndication had incorrect id");
    assertEquals(actual.getArticleId(), expected.getArticleId().toString(), "syndication had incorrect article id");
    assertEquals(actual.getStatus(), expected.getStatus(), "syndication had incorrect status");
    assertEquals(actual.getErrorMessage(), expected.getErrorMessage(), "syndication had incorrect error message");
    assertEquals(actual.getSubmissionCount(), expected.getSubmissionCount(), "syndication had incorrect submission count");
    assertEquals(actual.getTarget(), expected.getTarget(), "syndication had incorrect target");
    assertMatchingDates(actual.getStatusTimestamp(), expected.getStatusTimestamp());
    assertMatchingDates(actual.getSubmitTimestamp(), expected.getSubmitTimestamp());
  }

}
