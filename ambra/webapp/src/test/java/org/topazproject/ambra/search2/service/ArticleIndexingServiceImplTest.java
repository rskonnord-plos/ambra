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

package org.topazproject.ambra.search2.service;

import static org.easymock.EasyMock.*;

import org.apache.commons.configuration.Configuration;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.MultiLevelElementNameAndTextQualifier;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.ArticleDocumentService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.queue.MessageSender;
import org.topazproject.ambra.queue.Routes;
import org.topazproject.otm.Blob;
import org.topazproject.otm.Filter;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dragisa Krsmanovic
 */
public class ArticleIndexingServiceImplTest {

  DocumentBuilder documentBuilder;
  private Set<String> filters;

  @BeforeTest
  public void setUp() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderfactory = DocumentBuilderFactory
        .newInstance("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", getClass().getClassLoader());
    documentBuilderfactory.setNamespaceAware(true);
    documentBuilderfactory.setValidating(false);
    documentBuilderfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    documentBuilder = documentBuilderfactory.newDocumentBuilder();
    XMLUnit.setControlDocumentBuilderFactory(documentBuilderfactory);
    XMLUnit.setTestDocumentBuilderFactory(documentBuilderfactory);
    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
    XMLUnit.setTransformerFactory("net.sf.saxon.TransformerFactoryImpl");
    XMLUnit.setXSLTVersion("2.0");
    XMLUnit.setIgnoreAttributeOrder(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreWhitespace(true);

    filters = new HashSet<String>();
    filters.add("filter1");
    filters.add("filter2");

  }

  private Document createEmptyArticle() {
    Document doc = documentBuilder.newDocument();
    doc.appendChild(doc.createElement("article"));
    return doc;
  }


  @Test
  public void testArticlePublished() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);
    ArticleDocumentService mockArticleDocumentService = ctrl.createMock(ArticleDocumentService.class);
    MessageSender mockSender = ctrl.createMock(MessageSender.class);

    Document doc = createEmptyArticle();

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn("indexQueue");
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    expect(mockArticleDocumentService.getFullDocument(articleId)).andReturn(doc);

    mockSender.sendMessage(Routes.SEARCH_INDEX, doc);
    expectLastCall();

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);
    indexer.setArticleDocumentService(mockArticleDocumentService);
    indexer.setMessageSender(mockSender);

    indexer.articlePublished(articleId);

    ctrl.verify();
  }

  @Test
  public void testNoIndexingQueueConfigured() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn(null);
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn(null);

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);

    indexer.articlePublished(articleId);

    ctrl.verify();
  }

  @Test
  public void testArticleDeleted() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);
    MessageSender mockSender = ctrl.createMock(MessageSender.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn("indexQueue");
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    mockSender.sendMessage(Routes.SEARCH_DELETE, articleId);
    expectLastCall();

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);
    indexer.setMessageSender(mockSender);

    indexer.articleDeleted(articleId);

    ctrl.verify();

  }

  @Test
  public void testNoDeleteQueueConfigured() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn(null);
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn(null);

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);

    indexer.articleDeleted(articleId);

    ctrl.verify();

  }

  @Test
  public void testArticleCrossPublished() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);
    ArticleDocumentService mockArticleDocumentService = ctrl.createMock(ArticleDocumentService.class);
    MessageSender mockSender = ctrl.createMock(MessageSender.class);
    Session mockSession = ctrl.createMock(Session.class);

    Document doc = createEmptyArticle();

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn("indexQueue");
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    expectDisableFilters(mockSession);

    expect(mockArticleDocumentService.getFullDocument(articleId)).andReturn(doc);

    expectEnableFilters(ctrl, mockSession);

    mockSender.sendMessage("indexQueue", doc);
    expectLastCall();

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);
    indexer.setArticleDocumentService(mockArticleDocumentService);
    indexer.setMessageSender(mockSender);
    indexer.setOtmSession(mockSession);
    indexer.articleCrossPublished(articleId);

    ctrl.verify();
  }

  @Test
  public void testNoCrossPublishIndexingQueueConfigured() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn(null);
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn(null);

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);

    indexer.articleCrossPublished(articleId);

    ctrl.verify();
  }

  @Test
  public void testIndexAllArticles() throws Exception {
    String[] articles = {"article1", "article2"};
    String[] articleEIssn = {"eissn1", "eissn2"};

    String[] journalEissn = {"eissn1", "eissn2", "eissn3"};
    String[] journalKeys = {"journalOne", "journalTwo", "journalThree"};
    String[] journalNames = {"Journal One", "Journal Two", "Journal Three"};

    String[][] xpubArticles = {
        {"article1"},
        {"article2"},
        {"article1"}
    };

    String[] additionalInfo = {
        "<ambra xmlns=\"http://www.ambraproject.org/article/additionalInfo\"><journals>" +
            "<journal><eIssn>eissn1</eIssn><key>journalOne</key><name>Journal One</name></journal>" +
            "<journal><eIssn>eissn3</eIssn><key>journalThree</key><name>Journal Three</name></journal>" +
        "</journals></ambra>",
        "<ambra xmlns=\"http://www.ambraproject.org/article/additionalInfo\"><journals>" +
            "<journal><eIssn>eissn2</eIssn><key>journalTwo</key><name>Journal Two</name></journal>" +
        "</journals></ambra>"
    };

    Document[] messages = {
        XMLUnit.buildControlDocument("<article>" + additionalInfo[0] + "</article>"),
        XMLUnit.buildControlDocument("<article>" + additionalInfo[1] + "</article>")
    };

    IMocksControl ctrl = EasyMock.createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);
    ArticleDocumentService mockArticleDocumentService = ctrl.createMock(ArticleDocumentService.class);
    MessageSender mockSender = ctrl.createMock(MessageSender.class);
    SessionFactory mockSessionFactory = ctrl.createMock(SessionFactory.class);
    Session mockSession = ctrl.createMock(Session.class);
    Transaction mockTransaction = ctrl.createMock(Transaction.class);

    expect(mockSessionFactory.openSession()).andReturn(mockSession);
    expect(mockSession.beginTransaction(true, ArticleIndexingServiceImpl.VERY_LONG_TX_TIMEOUT))
        .andReturn(mockTransaction);

    Query mockArticleQuery = ctrl.createMock(Query.class);
    org.topazproject.otm.query.Results mockArticleResults = ctrl.createMock(Results.class);

    Query mockJournalQuery = ctrl.createMock(Query.class);
    Results mockJournalResults = ctrl.createMock(Results.class);

    Query mockXPubQuery = ctrl.createMock(Query.class);
    Results mockXPubResults = ctrl.createMock(Results.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn("indexQueue");
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    expectDisableFilters(mockSession);

    expect(mockSession.createQuery("select a.id articleId, a.eIssn eIssn, rep " +
            "from Article a " +
            "where a.state = '" + Article.STATE_ACTIVE + "'^^<xsd:int> " +
            "and rep := a.representations " +
            "and rep.name = 'XML';"))
        .andReturn(mockArticleQuery);
    expect(mockArticleQuery.execute()).andReturn(mockArticleResults);
    for (int i = 0; i < articles.length; i++) {
      String article = articles[i];
      String eIssn = articleEIssn[i];
      expect(mockArticleResults.next()).andReturn(true);
      URI uri = URI.create(article);
      expect(mockArticleResults.getURI(0)).andReturn(uri);
      Representation representation = new Representation();
      representation.setBody(ctrl.createMock(Blob.class));
      expect(mockArticleResults.get(2)).andReturn(representation);
      expect(mockArticleResults.getString(1)).andReturn(eIssn);
    }
    expect(mockArticleResults.next()).andReturn(false);

    expect(mockSession.createQuery("select j.eIssn eIssn, j.key key, j.dublinCore.title name from Journal j;"))
        .andReturn(mockJournalQuery);
    expect(mockJournalQuery.execute()).andReturn(mockJournalResults);
    for (int i = 0; i < journalEissn.length; i++) {
      String eIssn = journalEissn[i];
      String key = journalKeys[i];
      String name = journalNames[i];
      expect(mockJournalResults.next()).andReturn(true);
      expect(mockJournalResults.getString(0)).andReturn(eIssn);
      expect(mockJournalResults.getString(1)).andReturn(key);
      expect(mockJournalResults.getString(2)).andReturn(name);
    }
    expect(mockJournalResults.next()).andReturn(false);

    expect(mockSession.createQuery("select j.eIssn eIssn, j.simpleCollection articleId from Journal j;"))
        .andReturn(mockXPubQuery);
    expect(mockXPubQuery.execute()).andReturn(mockXPubResults);
    for (int i = 0; i < journalEissn.length; i++) {
      String eIssn = journalEissn[i];
      String[] articleXpub = xpubArticles[i];
      for (String articleId : articleXpub) {
        expect(mockXPubResults.next()).andReturn(true);
        expect(mockXPubResults.getString(0)).andReturn(eIssn);
        expect(mockXPubResults.getURI(1)).andReturn(URI.create(articleId));
      }
    }
    expect(mockXPubResults.next()).andReturn(false);

    Capture<Document> captureMessage = new Capture<Document>(CaptureType.ALL);

    for (String article : articles) {

      expect(mockArticleDocumentService.getDocument(isA(Blob.class)))
          .andReturn(createEmptyArticle());

      mockSender.sendMessage(eq("indexQueue"), capture(captureMessage));
      expectLastCall();
    }

    expectEnableFilters(ctrl, mockSession);

    mockTransaction.rollback();
    expectLastCall();

    ctrl.replay();

    ArticleIndexingServiceImpl service = new ArticleIndexingServiceImpl();
    service.setAmbraConfiguration(mockConfiguration);
    service.setArticleDocumentService(mockArticleDocumentService);
    service.setMessageSender(mockSender);
    service.setSessionFactory(mockSessionFactory);

    String message = service.indexAllArticles();

    assertTrue(message.startsWith("Finished indexing 2 articles in "), "Wrong count of articles");

    ctrl.verify();


    // verify that additional info is constructed correctly
    for (int i = 0; i < articles.length; i++) {
      Document sentMessage = captureMessage.getValues().get(i);
      URI receivedArticleId = URI.create(articles[i]);

      boolean matched = false;
      for (int j = 0; j < articles.length; j++) {
        if (receivedArticleId.equals(URI.create(articles[j]))) {
          Diff diff = new Diff(messages[j], sentMessage);
          diff.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(2));  // ignore order of elements
          assertTrue(diff.similar(), diff.toString());
          matched = true;
          break;
        }
      }
      assertTrue(matched, "Additional info for " + articles[i] + " not captured");

    }

  }

  private void expectEnableFilters(IMocksControl ctrl, Session mockSession) {
    expect(mockSession.enableFilter(or(eq("filter1"), eq("filter2"))))
        .andReturn(ctrl.createMock(Filter.class))
        .times(2);
  }

  private void expectDisableFilters(Session mockSession) {
    expect(mockSession.listFilters()).andReturn(filters);

    mockSession.disableFilter(or(eq("filter1"), eq("filter2")));
    expectLastCall().times(2);
  }

  @Test(expectedExceptions = {ApplicationException.class})
  public void testIndexAllNoQueueSet() throws Exception {
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn(null);
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    ctrl.replay();

    ArticleIndexingServiceImpl service = new ArticleIndexingServiceImpl();
    service.setAmbraConfiguration(mockConfiguration);

    service.indexAllArticles();

    ctrl.verify();

  }

  @Test
  public void testIndexArticle() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);
    ArticleDocumentService mockArticleDocumentService = ctrl.createMock(ArticleDocumentService.class);
    MessageSender mockSender = ctrl.createMock(MessageSender.class);
    Session mockSession = ctrl.createMock(Session.class);

    Document doc = createEmptyArticle();

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn("indexQueue");
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    expectDisableFilters(mockSession);

    expect(mockArticleDocumentService.getFullDocument(articleId)).andReturn(doc);

    expectEnableFilters(ctrl, mockSession);

    mockSender.sendMessage("indexQueue", doc);
    expectLastCall();

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);
    indexer.setArticleDocumentService(mockArticleDocumentService);
    indexer.setMessageSender(mockSender);
    indexer.setOtmSession(mockSession);

    indexer.indexArticle(articleId);

    ctrl.verify();
  }


  @Test(expectedExceptions = {ApplicationException.class})
  public void testIndexArticleNoQueueSet() throws Exception {
    String articleId = "someId";
    IMocksControl ctrl = createControl();
    Configuration mockConfiguration = ctrl.createMock(Configuration.class);

    expect(mockConfiguration.getString("ambra.services.search.articleIndexingQueue", null))
        .andReturn(null);
    expect(mockConfiguration.getString("ambra.services.search.articleDeleteQueue", null))
        .andReturn("deleteQueue");

    ctrl.replay();

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setAmbraConfiguration(mockConfiguration);

    indexer.indexArticle(articleId);

    ctrl.verify();
  }

  @Test
  public void testStartIndexingAllArticles() throws Exception {

    MessageSender mockSender = createMock(MessageSender.class);

    mockSender.sendMessage(eq(Routes.SEARCH_INDEXALL), EasyMock.<String>anyObject());
    expectLastCall();

    replay(mockSender);

    ArticleIndexingServiceImpl indexer = new ArticleIndexingServiceImpl();
    indexer.setMessageSender(mockSender);

    indexer.startIndexingAllArticles();

    verify(mockSender);
  }

}
