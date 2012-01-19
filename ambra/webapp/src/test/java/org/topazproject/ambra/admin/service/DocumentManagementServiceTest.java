/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.IngestException;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.Zip;
import org.topazproject.otm.Blob;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

import static org.testng.Assert.*;

import org.easymock.classextension.IMocksControl;

import static org.easymock.classextension.EasyMock.*;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Dragisa Krsmanovic
 */
public class DocumentManagementServiceTest {


  @BeforeTest
  public void setUp() {
    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    System.setProperty("javax.xml.transform.Transformer", "net.sf.saxon.Controller");
  }

  @Test
  public void testIngest() throws IngestException, DuplicateArticleIdException, IOException,
      URISyntaxException {

    IMocksControl ctl = createStrictControl();
    Zip zip = ctl.createMock(Zip.class);
    Ingester ingester = ctl.createMock(Ingester.class);
    ArticleOtmService articleOtmService = ctl.createMock(ArticleOtmService.class);
    SyndicationService syndicationService = ctl.createMock(SyndicationServiceImpl.class);
    Article article = new Article();
    String articleId = "info:doi/1234.678/abcd";
    String articleFilename = "info_doi_1234_678_abcd.xml";
    String docDirectory = "./";
    article.setId(URI.create(articleId));

    Set<Representation> representations = new HashSet<Representation>();
    Representation representation = new Representation();
    representations.add(representation);
    representation.setId("info:doi/1234.678/representation");

    Blob blob = ctl.createMock(Blob.class);
    representation.setBody(blob);
    representation.setName("XML");
    article.setRepresentations(representations);


    expect(articleOtmService.ingest(ingester, false)).andReturn(article);
    expect(blob.getInputStream()).andReturn(getClass().getResourceAsStream("/article/article1.xml"));
    // methods in log call
    expect(ingester.getZip()).andReturn(zip).times(0, 1);
    expect(zip.getName()).andReturn("zip name").times(0, 1);

    //  TODO: ??? get an actual LIst of Syndications that have real values ???
    expect(syndicationService.createSyndications(articleId))
        .andReturn(new LinkedList<SyndicationService.SyndicationDTO>());

    ctl.replay();
    DocumentManagementService service = new DocumentManagementService();
    service.setArticleOtmService(articleOtmService);
    service.setSyndicationService(syndicationService);
    service.setXslTemplate("/crossref.xsl");
    service.setPlosDoiUrl("http://www.plos.org/test-doi-resolver");
    service.setPlosEmail("test@plos.org");
    service.setDocumentDirectory(docDirectory);

    try {
      assertEquals(service.ingest(ingester, false), article);
      File doiCrossRef = new File(docDirectory, articleFilename);
      assertTrue(doiCrossRef.exists());
      assertTrue(doiCrossRef.length() > 0);
    } finally {
      File doiCrossRef = new File(docDirectory, articleFilename);
      if (doiCrossRef.exists()) {
        doiCrossRef.delete();
      }
    }
    ctl.verify();
  }

  @Test
  public void testPublish() throws Exception {

    String[] articleIds = {"article1", "article2"};
    String docDirectory = "./";

    File xrefFile1 = new File("article1.xml");
    xrefFile1.createNewFile();
    File xrefFile2 = new File("article2.xml");
    xrefFile2.createNewFile();

    List<String> expectedMessages = new ArrayList<String>();
    for (String articleId : articleIds) {
      expectedMessages.add("Published: " + articleId);
    }

    IMocksControl ctl = createStrictControl();

    CrossRefPosterService mockCrossRefPosterService = ctl.createMock(CrossRefPosterService.class);
    ArticleOtmService mockArticleOtmService = ctl.createMock(ArticleOtmService.class);
    OnPublishListener listener1 = ctl.createMock(OnPublishListener.class);
    OnPublishListener listener2 = ctl.createMock(OnPublishListener.class);

    List<OnPublishListener> listeners = new ArrayList<OnPublishListener>();
    listeners.add(listener1);
    listeners.add(listener2);

    expect(mockCrossRefPosterService.post(xrefFile1)).andReturn(200);
    mockArticleOtmService.setState(articleIds[0], Article.STATE_ACTIVE);
    expectLastCall();
    listener1.articlePublished(articleIds[0]);
    expectLastCall();
    listener2.articlePublished(articleIds[0]);
    expectLastCall();

    expect(mockCrossRefPosterService.post(xrefFile2)).andReturn(200);
    mockArticleOtmService.setState(articleIds[1], Article.STATE_ACTIVE);
    expectLastCall();
    listener1.articlePublished(articleIds[1]);
    expectLastCall();
    listener2.articlePublished(articleIds[1]);
    expectLastCall();

    ctl.replay();
    DocumentManagementService service = new DocumentManagementService();
    service.setArticleOtmService(mockArticleOtmService);
    service.setCrossRefPosterService(mockCrossRefPosterService);
    service.setDocumentDirectory(docDirectory);
    service.setSendToXref(true);
    service.setOnPublishListeners(listeners);

    try {
      List<String> msg = service.publish(articleIds);
      assertEquals(msg, expectedMessages);
    } finally {
      xrefFile1.delete();
      xrefFile2.delete();
    }
    ctl.verify();
  }


  @Test
  public void testDelete() throws Exception {

    String[] articleIds = {"article1", "article2"};
    String docDirectory = "./";

    List<String> expectedMessages = new ArrayList<String>();
    for (String articleId : articleIds) {
      expectedMessages.add("Deleted: " + articleId);
    }

    IMocksControl ctl = createStrictControl();

    ArticleOtmService mockArticleOtmService = ctl.createMock(ArticleOtmService.class);
    JournalService mockJournalService = ctl.createMock(JournalService.class);
    Cache browseCache = ctl.createMock(Cache.class);
    OnDeleteListener listener1 = ctl.createMock(OnDeleteListener.class);
    OnDeleteListener listener2 = ctl.createMock(OnDeleteListener.class);


    List<OnDeleteListener> listeners = new ArrayList<OnDeleteListener>();
    listeners.add(listener1);
    listeners.add(listener2);

    List<URI> uris1 = getUris(articleIds);
    List<URI> uris2 = getUris(articleIds);

    List<URI> expectedUris = getUris(new String[]{});

    Journal journal1 = new Journal();
    journal1.setId(URI.create("journal1"));
    journal1.setSimpleCollection(uris1);
    Journal journal2 = new Journal();
    journal2.setId(URI.create("journal2"));
    journal2.setSimpleCollection(uris2);
    Set<Journal> journals = new HashSet<Journal>();
    journals.add(journal1);
    journals.add(journal2);

    expect(mockArticleOtmService.getArticle(new URI(articleIds[0]))).andReturn(new Article());
    mockArticleOtmService.delete(articleIds[0]);
    expectLastCall();
    expect(mockJournalService.getAllJournals()).andReturn(journals);
    listener1.articleDeleted(articleIds[0]);
    expectLastCall();
    listener2.articleDeleted(articleIds[0]);
    expectLastCall();

    expect(mockArticleOtmService.getArticle(new URI(articleIds[1]))).andReturn(new Article());
    mockArticleOtmService.delete(articleIds[1]);
    expectLastCall();
    expect(mockJournalService.getAllJournals()).andReturn(journals);
    listener1.articleDeleted(articleIds[1]);
    expectLastCall();
    listener2.articleDeleted(articleIds[1]);
    expectLastCall();

    ctl.replay();
    DocumentManagementService service = new DocumentManagementService();
    service.setArticleOtmService(mockArticleOtmService);
    service.setJournalService(mockJournalService);
    service.setDocumentDirectory(docDirectory);
    service.setSendToXref(true);
    service.setOnDeleteListeners(listeners);
    service.setBrowseCache(browseCache);

    List<String> msg = service.delete(articleIds);
    assertEquals(msg, expectedMessages);
    assertEquals(journal1.getSimpleCollection(), expectedUris);
    assertEquals(journal2.getSimpleCollection(), expectedUris);
    ctl.verify();
  }

  private List<URI> getUris(String[] articleIds) {
    List<URI> uris = new ArrayList<URI>();

    for (String articleId : articleIds) {
      uris.add(URI.create(articleId));
    }
    uris.add(URI.create("article3"));
    return uris;
  }


}
