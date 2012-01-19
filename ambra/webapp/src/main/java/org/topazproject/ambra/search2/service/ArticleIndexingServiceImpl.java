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

import org.apache.camel.Handler;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.admin.service.OnCrossPubListener;
import org.topazproject.ambra.admin.service.OnDeleteListener;
import org.topazproject.ambra.admin.service.OnPublishListener;
import org.topazproject.ambra.article.service.ArticleDocumentService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.queue.MessageSender;
import org.topazproject.ambra.queue.Routes;
import org.topazproject.otm.Blob;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Service class that handles article search indexing. It is plugged in as OnPublishListener into
 * DocumentManagementService.
 *
 * @author Dragisa Krsmanovic
 */
public class ArticleIndexingServiceImpl implements OnPublishListener, OnDeleteListener, OnCrossPubListener, ArticleIndexingService {

  private static final Logger log = LoggerFactory.getLogger(ArticleIndexingServiceImpl.class);

  protected static final int VERY_LONG_TX_TIMEOUT = 7200; // 2hrs

  private ArticleDocumentService articleDocumentService;
  private MessageSender messageSender;
  private SessionFactory sessionFactory;
  private Session otmSession;
  private String indexingQueue;
  private String deleteQueue;

  @Required
  public void setArticleDocumentService(ArticleDocumentService articleDocumentService) {
    this.articleDocumentService = articleDocumentService;
  }

  @Required
  public void setMessageSender(MessageSender messageSender) {
    this.messageSender = messageSender;
  }

  @Required
  public void setAmbraConfiguration(Configuration ambraConfiguration) {
    indexingQueue = ambraConfiguration.getString("ambra.services.search.articleIndexingQueue", null);
    log.info("Article indexing queue set to " + indexingQueue);
    deleteQueue = ambraConfiguration.getString("ambra.services.search.articleDeleteQueue", null);
    log.info("Article delete queue set to " + deleteQueue);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Required
  public void setOtmSession(Session otmSession) {
    this.otmSession = otmSession;
  }

  /**
   * Method that is fired on article publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that publish operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the published article
   * @throws Exception if message send fails
   */
  @Transactional(readOnly = true)
  public void articlePublished(String articleId) throws Exception {
    if (indexingQueue != null) {
      log.info("Indexing published article " + articleId);
      indexOneArticle(articleId);
    } else {
      log.warn("Article indexing queue not set. Article " + articleId + " will not be indexed.");
    }
  }

  /**
   * Method that is fired on article delete operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that delete operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the deleted article
   * @throws Exception if message send fails
   */
  public void articleDeleted(String articleId) throws Exception {
    if (deleteQueue != null) {
      log.info("Deleting article " + articleId + " from search index.");
      messageSender.sendMessage(Routes.SEARCH_DELETE, articleId);
    } else {
      log.warn("Article index delete queue not set. Article " + articleId + " will not be deleted from search index.");
    }
  }

  /**
   * Method that is fired on article cross publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that cross publish operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the cross published article
   * @throws Exception if message send fails
   */
  @Transactional(readOnly = true)
  public void articleCrossPublished(String articleId) throws Exception {
    if (indexingQueue != null) {
      log.info("Indexing cross published article " + articleId);
      indexOneArticle(articleId);
    } else {
      log.warn("Article indexing queue not set. Article " + articleId + " will not be re-indexed.");
    }
  }

  public void startIndexingAllArticles() throws Exception {
     // Message content is unimportant here
    messageSender.sendMessage(Routes.SEARCH_INDEXALL, "start");
  }

  /**
   * Index one article. Disables filters so can be applied in any journal context.
   *
   * @param articleId Article ID
   * @throws Exception If operation fails
   */
  @Transactional(readOnly = true)
  public void indexArticle(String articleId) throws Exception {

    if (indexingQueue == null) {
      throw new ApplicationException("Article indexing queue not set. Article " + articleId + " will not be re-indexed.");
    }

    Set<String> filters = disableFilters(otmSession);

    Document doc;
    try {
      doc = articleDocumentService.getFullDocument(articleId);
    } finally {
      enableFilters(filters, otmSession);
    }

    if (doc == null) {
      log.error("Search indexing failed for " + articleId + ". Returned document is NULL.");
      return;
    }

    messageSender.sendMessage(indexingQueue, doc);
  }

  /**
   * Same as indexArticle() except that it doesn't disable filters.
   *
   * @param articleId Article ID
   * @throws Exception If operation fails
   */
  private void indexOneArticle(String articleId) throws Exception {

    Document doc = articleDocumentService.getFullDocument(articleId);

    if (doc == null) {
      log.error("Search indexing failed for " + articleId + ". Returned document is NULL.");
      return;
    }

    messageSender.sendMessage(Routes.SEARCH_INDEX, doc);
  }

  /**
   * Send all articles for re-indexing.
   * <p/>
   * Queries to fetch all articles and to get all cross-published articles are separated to
   * speed up the process.
   * <p/>
   * This is Apache Camel handler. It is invoked asynchronously after user submits a message to SEDA
   * queue.
   *
   * @return Email message body
   * @throws Exception
   * @see org.topazproject.ambra.queue.Routes
   */
  @Handler
  public String indexAllArticles() throws Exception {


    if (indexingQueue != null) {

      Session session = null;
      Transaction transaction = null;
      try {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction(true, VERY_LONG_TX_TIMEOUT);

        Set<String> filters = disableFilters(session);

        try {
          long timestamp = System.currentTimeMillis();
          int count = indexAll(session, articleDocumentService, messageSender, indexingQueue);
          String message = "Finished indexing " + count + " articles in " + (System.currentTimeMillis() - timestamp) / 1000l + " sec";
          log.info(message);
          return message;

        } finally {
          enableFilters(filters, session);
        }

      } finally {
        try {
          if (transaction != null) {
            transaction.rollback();
          }
        } catch (Throwable t) {
          log.warn("Error in rollback", t);
        }
        try {
          if (session != null) {
            session.close();
          }
        } catch (Throwable t) {
          log.warn("Error closing session", t);
        }
      }
    } else {
      throw new ApplicationException("Indexing queue not defined");
    }
  }

  /**
   * Keep this method static to ensure that it's not using instance fields.
   *
   * @param session OTM session
   * @param articleDocumentService ArticleDocumentService
   * @param messageSender MessageSender
   * @param indexingQueue IndexingQueue
   * @return Number of articles indexed
   * @throws Exception If operation fails
   */
  private static int indexAll(
      Session session,
      ArticleDocumentService articleDocumentService,
      MessageSender messageSender,
      String indexingQueue) throws Exception {

    int totalIndexed = 0;

    // Sorted map to make sure they are in the same order for each test run
    SortedMap<URI, Set<String>> allArticles = new TreeMap<URI, Set<String>>();
    Map<URI, Blob> articleXmls = new HashMap<URI, Blob>();
    loadAllArticles(session, allArticles, articleXmls);
    Map<String, JournalFields> journals = getJournals(session);
    loadAllCrossPublishedArticles(session, allArticles);

    for (Map.Entry<URI, Set<String>> articleEntry : allArticles.entrySet()) {

      Document doc = articleDocumentService.getDocument(articleXmls.get(articleEntry.getKey()));

      Element additionalInfoElement = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "ambra");

      Element journalsElement = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "journals");

      for (String eIssn : articleEntry.getValue()) {
        JournalFields jrnlFields = journals.get(eIssn);
        journalsElement.appendChild(
            createJournalElement(doc, eIssn, jrnlFields.getKey(), jrnlFields.getName()));
      }
      additionalInfoElement.appendChild(journalsElement);
      doc.getDocumentElement().appendChild(additionalInfoElement);

      messageSender.sendMessage(indexingQueue, doc);
      totalIndexed++;
    }

    return totalIndexed;
  }

  private static Element createJournalElement(Document doc, String eIssn, String key, String name) {
    Element journalElement = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "journal");

    Element eIssnNode = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "eIssn");
    eIssnNode.appendChild(doc.createTextNode(eIssn));
    journalElement.appendChild(eIssnNode);

    Element keyNode = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "key");
    keyNode.appendChild(doc.createTextNode(key));
    journalElement.appendChild(keyNode);

    Element nameNode = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "name");
    nameNode.appendChild(doc.createTextNode(name));
    journalElement.appendChild(nameNode);
    return journalElement;
  }

  private static void loadAllCrossPublishedArticles(Session session, SortedMap<URI, Set<String>> allArticles) {
    Query query = session.createQuery("select j.eIssn eIssn, j.simpleCollection articleId from Journal j;");
    Results results = query.execute();

    while (results.next()) {
      Set<String> articleJournals = allArticles.get(results.getURI(1));
      if (articleJournals != null) {
        articleJournals.add(results.getString(0));
      }
    }
  }

  private static void loadAllArticles(Session session, SortedMap<URI, Set<String>> allArticles, Map<URI, Blob> articleXmls) {
    Query q = session.createQuery(
        "select a.id articleId, a.eIssn eIssn, rep " +
            "from Article a " +
            "where a.state = '" + Article.STATE_ACTIVE + "'^^<xsd:int> " +
            "and rep := a.representations " +
            "and rep.name = 'XML';");
    Results r = q.execute();
    while (r.next()) {
      URI articleId = r.getURI(0);
      Representation representation = (Representation) r.get(2);
      if (representation.getBody() != null) {
        Set<String> articleJournals = new HashSet<String>();
        articleJournals.add(r.getString(1));
        allArticles.put(articleId, articleJournals);
        articleXmls.put(articleId, representation.getBody());
      } else {
        log.warn(articleId.toString() + " has null representation blob for XML.");
      }
    }
  }

  /**
   * Create map of all journals by eIssn.
   *
   * @param session OTM Session
   * @return Map where key is eIssn and value is object containing key and name.
   */
  private static Map<String, JournalFields> getJournals(Session session) {
    Map<String, JournalFields> journals = new HashMap<String, JournalFields>();
    Query journalsQuery = session.createQuery("select j.eIssn eIssn, j.key key, j.dublinCore.title name from Journal j;");
    Results journalsResults = journalsQuery.execute();

    while (journalsResults.next()) {
      journals.put(journalsResults.getString(0),
                   new JournalFields(journalsResults.getString(1), journalsResults.getString(2)));
    }
    return journals;
  }

  private static Set<String> disableFilters(Session session) {
    Set<String> filters = session.listFilters();
    for (String filter : filters) {
      session.disableFilter(filter);
    }
    return filters;
  }

  private static void enableFilters(Set<String> filters, Session session) {
    for (String filter : filters) {
      session.enableFilter(filter);
    }
  }

  /**
   * Private class to hold journal details needed to form additional info XML.
   */
  private static class JournalFields implements Serializable {

    private static final long serialVersionUID = 5576062239245504730L;
    
    private String key;
    private String name;

    private JournalFields(String key, String name) {
      this.key = key;
      this.name = name;
    }

    public String getKey() {
      return key;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "JournalFields{" +
          "key='" + key + '\'' +
          ", name='" + name + '\'' +
          '}';
    }
  }

}
