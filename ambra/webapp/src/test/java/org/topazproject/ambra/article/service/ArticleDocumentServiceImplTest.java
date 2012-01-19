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

package org.topazproject.ambra.article.service;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.MultiLevelElementNameAndTextQualifier;
import org.easymock.classextension.IMocksControl;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Representation;
import org.topazproject.otm.Blob;
import org.topazproject.otm.Session;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.classextension.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author Dragisa Krsmanovic
 */
public class ArticleDocumentServiceImplTest {

  private DocumentBuilderFactory documentBuilderfactory;

  @BeforeTest
  public void setUp() throws ParserConfigurationException {
    this.documentBuilderfactory = DocumentBuilderFactory
        .newInstance("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", getClass().getClassLoader());
    this.documentBuilderfactory.setNamespaceAware(true);
    this.documentBuilderfactory.setValidating(false);
    this.documentBuilderfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    XMLUnit.setControlDocumentBuilderFactory(documentBuilderfactory);
    XMLUnit.setTestDocumentBuilderFactory(documentBuilderfactory);
    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
    XMLUnit.setTransformerFactory("net.sf.saxon.TransformerFactoryImpl");
    XMLUnit.setXSLTVersion("2.0");
    XMLUnit.setIgnoreAttributeOrder(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreWhitespace(true);
  }

  @Test(timeOut = 6000)
  public void testGetDocument() throws Exception {

    ClassLoader classLoader = getClass().getClassLoader();

    IMocksControl ctrl = createControl();

    Session mockSession = ctrl.createMock(Session.class);
    Blob mockBlob = ctrl.createMock(Blob.class);

    ArticleDocumentServiceImpl service = new ArticleDocumentServiceImpl();
    service.setDocumentBuilderFactory(documentBuilderfactory);
    service.setOtmSession(mockSession);

    expect(mockBlob.getInputStream()).andReturn(
        classLoader.getResourceAsStream("article/article1.xml"));

    ctrl.replay();

    Document result = service.getDocument(mockBlob);

    assertNotNull(result, "Null Document returned");

    ctrl.verify();

    Document expected = XMLUnit.buildControlDocument(new InputSource(
        classLoader.getResourceAsStream("article/article1.xml")));

    Diff diff = new Diff(expected, result);
    assertTrue(diff.identical(), diff.toString());
  }

  @Test(timeOut = 6000)
  public void testGetFullDocument() throws Exception {

    String articleId = "someId";
    URI articleUri = URI.create(articleId);
    ClassLoader classLoader = getClass().getClassLoader();

    IMocksControl ctrl = createControl();

    Session mockSession = ctrl.createMock(Session.class);
    Article mockArticle = ctrl.createMock(Article.class);
    Representation mockRepresentation = ctrl.createMock(Representation.class);
    Blob mockBlob = ctrl.createMock(Blob.class);
    JournalService mockJournalService = ctrl.createMock(JournalService.class);

    ArticleDocumentServiceImpl service = new ArticleDocumentServiceImpl();
    service.setDocumentBuilderFactory(documentBuilderfactory);
    service.setOtmSession(mockSession);
    service.setJournalService(mockJournalService);

    expect(mockSession.get(Article.class, articleId)).andReturn(mockArticle);
    expect(mockArticle.getRepresentation("XML")).andReturn(mockRepresentation);
    expect(mockRepresentation.getBody()).andReturn(mockBlob);
    expect(mockBlob.getInputStream()).andReturn(
        classLoader.getResourceAsStream("article/article1.xml"));

    Set<Journal> journals = new HashSet<Journal>();

    journals.add(createJournal("journal1id", "eissn1", "journal1", "Journal One"));
    journals.add(createJournal("journal2id", "eissn2", "journal2", "Journal Two"));

    expect(mockJournalService.getJournalsForObject(articleUri)).andReturn(journals);

    ctrl.replay();

    Document result = service.getFullDocument(articleId);

    assertNotNull(result, "Null Document returned");

    ctrl.verify();

    Document expected = XMLUnit.buildControlDocument(new InputSource(
        classLoader.getResourceAsStream("article/article1plusAdditionalInfo.xml")));

    Diff diff = new Diff(expected, result);
    diff.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(2));  // ignore order of elements
    assertTrue(diff.similar(), diff.toString());
  }

  private Journal createJournal(String id, String eIssn, String key, String name) {
    Journal journal = new Journal();
    journal.setId(URI.create(id));
    journal.setKey(key);
    journal.seteIssn(eIssn);
    journal.setDublinCore(new DublinCore());
    journal.getDublinCore().setTitle(name);
    return journal;
  }

}
