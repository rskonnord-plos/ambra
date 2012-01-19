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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.otm.Blob;
import org.topazproject.otm.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * @author Dragisa Krsmanovic
 */
public class ArticleDocumentServiceImpl implements ArticleDocumentService {

  private static final Logger log = LoggerFactory.getLogger(ArticleDocumentServiceImpl.class);

  private Session otmSession;
  private DocumentBuilderFactory documentBuilderFactory;
  private JournalService journalService;

  /**
   * Set OTM Session.
   *
   * @param otmSession Topaz OTM Session object
   */
  @Required
  public void setOtmSession(Session otmSession) {
    this.otmSession = otmSession;
  }

  /**
   * Set XML document builder factory. It will be used to parse Article XML.
   * @param documentBuilderFactory DocumentBuilderFactory
   */
  @Required
  public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
  }

  /**
   * Set journal service.
   *
   * @param journalService Journal service
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  public Document getDocument(Blob blob) throws Exception {
    InputSource xmlInputSource = new InputSource(blob
        .getInputStream());

    DocumentBuilder documentBuilder;
    synchronized (documentBuilderFactory) {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    return documentBuilder.parse(xmlInputSource);
  }

  /**
   * @param articleId Article ID
   * @return Article XML + additional info
   * @throws IOException If reading XML document failed
   */
  @Transactional(readOnly = true)
  public Document getFullDocument(String articleId) throws Exception {

    Article article = otmSession.get(Article.class, articleId);

    if (article == null) {
      throw new NoSuchArticleIdException(articleId, " not found");
    }

    Blob blob = article.getRepresentation("XML").getBody();
    Document doc = getDocument(blob);

    appendJournals(URI.create(articleId), doc);

    return doc;
  }

  private void appendJournals(URI articleId, Document doc) {
    Set<Journal> journals = journalService.getJournalsForObject(articleId);

    Element additionalInfoElement = doc.createElementNS(XML_NAMESPACE, "ambra");
    Element journalsElement = doc.createElementNS(XML_NAMESPACE, "journals");

    doc.getDocumentElement().appendChild(additionalInfoElement);
    additionalInfoElement.appendChild(journalsElement);

    for (Journal journal : journals) {
      Element journalElement = doc.createElementNS(XML_NAMESPACE, "journal");

      Element eIssn = doc.createElementNS(XML_NAMESPACE, "eIssn");
      eIssn.appendChild(doc.createTextNode(journal.geteIssn()));
      journalElement.appendChild(eIssn);

      Element key = doc.createElementNS(XML_NAMESPACE, "key");
      key.appendChild(doc.createTextNode(journal.getKey()));
      journalElement.appendChild(key);

      Element name = doc.createElementNS(XML_NAMESPACE, "name");
      name.appendChild(doc.createTextNode(journal.getDublinCore().getTitle()));
      journalElement.appendChild(name);

      journalsElement.appendChild(journalElement);
    }
  }

}
