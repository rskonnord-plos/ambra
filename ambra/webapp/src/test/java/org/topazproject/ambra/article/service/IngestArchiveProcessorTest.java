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

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.article.ArchiveProcessException;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.ObjectInfo;
import org.w3c.dom.Document;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipFile;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick Date: 6/7/11
 *         <p/>
 *         org.topazproject.ambra.article.service
 */
public class IngestArchiveProcessorTest extends BaseTest {

  @Autowired
  protected IngestArchiveProcessor ingestArchiveProcessor;

  @Test(dataProvider = "sampleArticle", dataProviderClass = SampleArticleData.class)
  public void testProcessArticle(ZipFile archive, Article expectedArticle) throws Exception {
    Document articleXml = ingestArchiveProcessor.extractArticleXml(archive);
    Article result = ingestArchiveProcessor.processArticle(archive, articleXml);
    compareArticles(result,expectedArticle);
    String archiveName = archive.getName().contains(File.separator)
        ? archive.getName().substring(archive.getName().lastIndexOf(File.separator) + 1)
        : archive.getName();
    assertEquals(result.getArchiveName(),archiveName,"Article didn't have archive name set correctly");
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleSecondaryObjects")
  public void testParseWithSecondaryObjects(ZipFile archive, List<ObjectInfo> expectedSecondaryObjects) throws ArchiveProcessException {
    Document articleXml = ingestArchiveProcessor.extractArticleXml(archive);
    Article result = ingestArchiveProcessor.processArticle(archive, articleXml);
    assertNotNull(result, "Returned null article");
    assertNotNull(result.getParts(), "returned null secondary objects");
    assertEquals(result.getParts().size(), expectedSecondaryObjects.size(), "Returned incorrect number of secondary objects");
    for (int i = 0; i < result.getParts().size(); i++) {
      compareSecondaryObjects(result.getParts().get(i), expectedSecondaryObjects.get(i));
    }
    String archiveName = archive.getName().contains(File.separator)
        ? archive.getName().substring(archive.getName().lastIndexOf(File.separator) + 1)
        : archive.getName();
    assertEquals(result.getArchiveName(),archiveName,"Article didn't have archive name set correctly");
  }

}
