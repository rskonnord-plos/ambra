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
import org.topazproject.ambra.service.AmbraMailer;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Service class that handles article search indexing. It is plugged in as OnPublishListener into
 * DocumentManagementService.
 *
 * @author Dragisa Krsmanovic
 */
public class ArticleIndexingServiceImpl implements OnPublishListener, OnDeleteListener, OnCrossPubListener, ArticleIndexingService {

  private static final Logger log = LoggerFactory.getLogger(ArticleIndexingServiceImpl.class);

  protected static final int DEFAULT_VERY_LONG_TX_TIMEOUT = 1800; // in seconds, 30 mins
  protected static final int DEFAULT_INCREMENT_LIMIT_SIZE = 200;

  private AmbraMailer mailer;
  private ArticleDocumentService articleDocumentService;
  private MessageSender messageSender;
  private SessionFactory sessionFactory;
  private Session otmSession;
  private String indexingQueue;
  private String deleteQueue;
  private int incrementLimitSize;
  private int batchTimeInterval;

  @Required
  public void setArticleDocumentService(ArticleDocumentService articleDocumentService) {
    this.articleDocumentService = articleDocumentService;
  }

  @Required
  public void setAmbraMailer(AmbraMailer m) {
    this.mailer = m;
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

    incrementLimitSize = ambraConfiguration.getInt("ambra.services.search.incrementLimitSize",
      DEFAULT_INCREMENT_LIMIT_SIZE);
    batchTimeInterval = ambraConfiguration.getInt("ambra.services.search.batchTimeInterval",
      DEFAULT_VERY_LONG_TX_TIMEOUT);
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
      indexArticle(articleId);
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
      try {
        session = sessionFactory.openSession();

        Set<String> filters = disableFilters(session);

        try {
          long timestamp = System.currentTimeMillis();
          Result result = indexAll(session, articleDocumentService, messageSender, indexingQueue, mailer,
            this.batchTimeInterval, this.incrementLimitSize);
          StringBuilder message = new StringBuilder();
          message.append("Finished indexing ")
              .append(Integer.toString(result.total))
              .append(" articles in ")
              .append(Long.toString((System.currentTimeMillis() - timestamp) / 1000l))
              .append(" sec.");
          log.info(message.toString());

          if (result.failed > 0) {
            log.warn("Failed indexing " + result.failed + " articles");
            message.append("\nFailed indexing ")
                .append(Integer.toString(result.failed))
                .append(" articles.");
          }

          if (result.partialUpdate) {
            message.append("\nThere was an error while trying to index all the articles.  Only a subset of articles " +
              "have been reindexed.  Try reindexing all the articles again later.");
          }

          return message.toString();

        } finally {
          enableFilters(filters, session);
        }

      } finally {
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
   * @param mailer ambra mailer
   * @param batchTimeInterval the length of a transaction in seconds
   * @param incrementLimitSize batch size for processing articles
   * @return Number of articles indexed
   * @throws Exception If operation fails
   */
  private static Result indexAll(
      Session session,
      ArticleDocumentService articleDocumentService,
      MessageSender messageSender,
      String indexingQueue, AmbraMailer mailer, int batchTimeInterval, int incrementLimitSize) throws Exception {

    boolean bContinue = true;

    Transaction transaction = null;
    Map<String, JournalFields> journals = null;
    HashMap<URI, Set<String>> allArticleJournals = null;
    try {
      // true => readonly transaction
      transaction = session.beginTransaction(true, batchTimeInterval);
      // get all journals by eIssn
      journals = getJournals(session);
      // get information about cross-published articles
      allArticleJournals = loadAllCrossPublishedArticles(session);
    } catch (Exception e) {
      // if we encounter problem here, don't continue
      bContinue = false;
      log.error("Error while getting journal information", e);
    } finally {
      try {
        transaction.rollback();
      } catch (Exception e) {
         log.warn("Error in rollback", e);
      }
    }

    List<URI> failedArticles = new ArrayList<URI>();
    int totalIndexed = 0;
    int totalFailed = 0;
    boolean partialUpdate = false;
    int offset = 0;

    while (bContinue) {
      try {
        // readonly transaction
        transaction = session.beginTransaction(true, batchTimeInterval);

        // get the list of articles
        Query q = session.createQuery(
            "select a.id articleId, a.eIssn eIssn, rep " +
                "from Article a " +
                "where a.state = '" + Article.STATE_ACTIVE + "'^^<xsd:int> " +
                "and rep := a.representations " +
                "and rep.name = 'XML' " +
                "limit " + incrementLimitSize + " offset " + offset + ";");
        Results r = q.execute();

        while (r.next()) {
          URI articleId = null;
          Representation representation = null;
          try {
            articleId = r.getURI(0);
            representation = (Representation) r.get(2);
          } catch (Exception ex) {
            if(articleId == null) {
              log.error("Couldn't get article URI.", ex);
            } else {
              failedArticles.add(articleId);
              log.error("Failed to get representation " + ex.getMessage(), ex);
            }
          }

          if (representation != null && representation.getBody() != null) {
            // get the article xml and add the necessary information to the xml
            try {
              Document doc = articleDocumentService.getDocument(representation.getBody());

              Element additionalInfoElement = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "ambra");
              Element journalsElement = doc.createElementNS(ArticleDocumentService.XML_NAMESPACE, "journals");

              Set<String> articleJournals =  allArticleJournals.get(articleId);
              if (articleJournals == null) {
                articleJournals = new HashSet<String>();
              }
              articleJournals.add(r.getString(1));

              for (String eIssn : articleJournals) {
                JournalFields jrnlFields = journals.get(eIssn);
                journalsElement.appendChild(
                    createJournalElement(doc, eIssn, jrnlFields.getKey(), jrnlFields.getName()));
              }
              additionalInfoElement.appendChild(journalsElement);
              doc.getDocumentElement().appendChild(additionalInfoElement);

              // send the article xml to plos-queue to be indexed
              messageSender.sendMessage(indexingQueue, doc);
              totalIndexed++;
            } catch (Exception e) {
              log.error("Error indexing article " + articleId.toString(), e);
              totalFailed++;
            }
          } else {
            log.warn(articleId.toString() + " has null representation blob for XML.");
          }
        } // end while loop tql result

        transaction.rollback();

        offset = offset + incrementLimitSize;
        log.info("Offset " + offset);

        if (offset > (totalIndexed + totalFailed)) {
          // we have processed all the articles, exit the while loop
          bContinue = false;
        }
      } catch (Exception e) {
        bContinue = false;
        log.error("Error while gathering a list of articles", e);

        StringBuilder message = new StringBuilder("Error while gathering a list of articles. \n");
        message.append(e.getMessage());
        mailer.sendError(message.toString());

        partialUpdate = true;
      }
    } // end of while

    if(failedArticles.size() > 0) {
      StringBuilder message = new StringBuilder("Error getting XML for articles:\n");

      for(URI article : failedArticles) {
        message.append(article.toString());
        message.append("\n");
      }

      mailer.sendError(message.toString());
    }

    return new Result(totalIndexed, totalFailed, partialUpdate);
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

  /**
   * Get information about cross-published articles 
   * @param session OTM Session
   * @return Map where key is articleId and value is set of journals the article was cross-published on
   */
  private static HashMap<URI, Set<String>> loadAllCrossPublishedArticles(Session session) {
    HashMap<URI, Set<String>> allArticleJournals = new HashMap<URI, Set<String>>();
    
    Query query = session.createQuery("select j.eIssn eIssn, j.simpleCollection articleId from Journal j;");
    Results results = query.execute();
    
    while (results.next()) {
      Set<String> articleJournals = allArticleJournals.get(results.getURI(1));
      if (articleJournals == null) {
        articleJournals = new HashSet<String>();
      }
      articleJournals.add(results.getString(0));
      allArticleJournals.put(results.getURI(1), articleJournals);
    }

    return allArticleJournals;
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

  /**
   * Transfer object for 3 values
   */
  private static class Result {
    public final int total;
    public final int failed;
    public final boolean partialUpdate;

    private Result(int total, int failed, boolean partialUpdate) {
      this.total = total;
      this.failed = failed;
      this.partialUpdate = partialUpdate;
    }
  }

}
