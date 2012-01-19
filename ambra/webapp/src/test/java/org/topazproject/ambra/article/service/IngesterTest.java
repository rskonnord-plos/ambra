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

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.ObjectInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipFile;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Test for implementation of ingester.  Working directory for the test should be set to the ambra webapp home
 * directory
 *
 * @author Alex Kudlick Date: 5/11/11
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
public class IngesterTest extends BaseTest {

  @Autowired
  protected Ingester ingester;

  @Qualifier("ingestedDocumentDir")
  @Autowired
  protected String ingestedDocumentDirectory;

  //Article persistence service is just used to check that ingested articles were stored to the db
  @Autowired
  protected ArticlePersistenceService articlePersistenceService;

  @BeforeMethod
  public void createIngestedDir() throws IOException {
    FileUtils.deleteDirectory(new File(ingestedDocumentDirectory));
    new File(ingestedDocumentDirectory).mkdirs();
  }

  @AfterMethod
  public void deleteIngestedDir() throws IOException {
    FileUtils.deleteDirectory(new File(ingestedDocumentDirectory));
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle")
  public void testIngest(ZipFile ingestArchive, Article expectedArticle) throws Exception {
    Article ingestedArticle = ingester.ingest(ingestArchive, false);
    compareArticles(ingestedArticle, expectedArticle);
    String archiveName = ingestArchive.getName().contains(File.separator)
        ? ingestArchive.getName().substring(ingestArchive.getName().lastIndexOf(File.separator) + 1)
        : ingestArchive.getName();
    assertEquals(ingestedArticle.getArchiveName(), archiveName, "Article didn't have archive name set correctly");

    Article storedArticle = null;
    try {
      storedArticle = articlePersistenceService.getArticle(ingestedArticle.getId(), DEFAULT_ADMIN_AUTHID);
    } catch (NoSuchArticleIdException e) {
      fail("article wasn't stored to the db");
    }
    compareArticles(storedArticle, expectedArticle);
    assertEquals(storedArticle.getArchiveName(), archiveName, "Article didn't have archive name set correctly");
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle",
      dependsOnMethods = "testIngest", expectedExceptions = DuplicateArticleIdException.class)
  public void testDuplicateArticleException(ZipFile ingestArchive, Article notUsed) throws DuplicateArticleIdException, IngestException {
    ingester.ingest(ingestArchive, false);
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle",
      dependsOnMethods = {"testIngest", "testDuplicateArticleException"})
  public void testIngestWithForce(ZipFile ingestArchive, Article expectedArticle) throws DuplicateArticleIdException, IngestException {
    ingester.ingest(ingestArchive, true);
    Article storedArticle = null;
    try {
      storedArticle = articlePersistenceService.getArticle(expectedArticle.getId(), DEFAULT_ADMIN_AUTHID);
    } catch (NoSuchArticleIdException e) {
      fail("article wasn't stored to the db");
    }
    compareArticles(storedArticle, expectedArticle);
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleSecondaryObjects")
  public void testIngestWithSecondaryObjects(ZipFile archive, List<ObjectInfo> expectedParts) throws DuplicateArticleIdException, IngestException, NoSuchArticleIdException {
    final Article article = ingester.ingest(archive, false);
    List<ObjectInfo> actualParts = article.getParts();
    assertNotNull(actualParts, "Returned article with null list of parts");
    assertEquals(actualParts.size(), expectedParts.size(), "Returned article with incorrect number of parts");
    for (int i = 0; i < expectedParts.size(); i++) {
      ObjectInfo actual = actualParts.get(i);
      ObjectInfo expected = expectedParts.get(i);
      compareSecondaryObjects(actual, expected);
    }
    List<ObjectInfo> storedParts = articlePersistenceService.getArticle(article.getId(), DEFAULT_ADMIN_AUTHID).getParts();
    assertNotNull(storedParts, "Returned article with null list of parts");
    assertEquals(storedParts.size(), expectedParts.size(), "Returned article with incorrect number of parts");
    for (int i = 0; i < expectedParts.size(); i++) {
      ObjectInfo actual = storedParts.get(i);
      ObjectInfo expected = expectedParts.get(i);
      compareSecondaryObjects(actual, expected);
    }
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleSecondaryObjects",
      dependsOnMethods = "testIngestWithSecondaryObjects")
  public void testIngestWithSecondaryObjectsAndForce(ZipFile archive, List<ObjectInfo> notUsed) throws DuplicateArticleIdException, IngestException {
    ingester.ingest(archive, true);
  }

  /**
   * Regression test for the bug described <a href="https://developer.plos.org/jira/browse/NHOPE-222">here</a>.  Ingest
   * an article, then add images to the zip, and try to reingest.
   *
   * @param originalArchive
   * @param alteredArchive
   * @param expectedPartsUris
   * @throws Exception
   */
  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "alteredZip",
      groups = "regressionTest")
  public void testForceIngestAfterZipHasChanged(ZipFile originalArchive, ZipFile alteredArchive, List<URI> expectedPartsUris) throws Exception {
    Article article = ingester.ingest(originalArchive, false); //get the original article in the db
    try {
      article = ingester.ingest(alteredArchive, true);//force ingestion
    } catch (Exception e) {
      fail("There was an error ingesting the altered archive", e);
    }
    article = articlePersistenceService.getArticle(article.getId(), DEFAULT_ADMIN_AUTHID);
    assertEquals(article.getParts().size(), expectedPartsUris.size(), "stored incorrect number of parts");
    //get the parts' ids into a list to compare them
    List<URI> actualPartsUris = new ArrayList<URI>(expectedPartsUris.size());
    for (ObjectInfo part : article.getParts()) {
      assertNotNull(part, "parts didn't get stored to the db");
      actualPartsUris.add(part.getId());
    }
    assertEquals(actualPartsUris.toArray(), expectedPartsUris.toArray(), "stored incorrect part ids");

    try {
      ingester.ingest(originalArchive, true); //re-ingest the original archive
    } catch (Exception e) {
      fail("There was an error reingesting the original archive", e);
    }
  }
}
