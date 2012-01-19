/* 
 * $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedoragsearch.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.DuplicateArticleIdException;
import org.topazproject.ws.article.NoSuchArticleIdException;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.ArticleClientFactory;

import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;


public class SearchTest extends TestCase {
  private Article service;
  private FgsOperations ops;
  private static final Log log = LogFactory.getLog(SearchTest.class);
  
  protected static final String ARTICLES_SVC_URL =
    "http://localhost:9997/ws-articles/services/ArticleServicePort";
  protected static final String SEARCH_SVC_URL =
    "http://localhost:9092/ws-search-webapp/services/FedoraGenericSearchServicePort";

  protected static final String[][] TEST_ARTICLES = {
    { "info:doi/10.1371/journal.pbio.0020294", "/pbio.0020294.zip" },
//    { "info:doi/10.1371/journal.pbio.0020042", "/pbio.0020042.zip" },
//    { "info:doi/10.1371/journal.pbio.0020317", "/pbio.0020317.zip" },
//    { "info:doi/10.1371/journal.pbio.0020382", "/pbio.0020382.zip" },
  };

  protected void setUp()
      throws MalformedURLException, ServiceException, RemoteException, DuplicateArticleIdException,
             IngestException {
    log.info("setUp");

    // Create search service
    ops = new FgsOperationsServiceLocator().getOperations(new URL(SEARCH_SVC_URL));
    
    // Create articles service
    service = ArticleClientFactory.create(ARTICLES_SVC_URL);
    log.info("ingesting article(s)");
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      ingestArticle(TEST_ARTICLES[i][0], TEST_ARTICLES[i][1]);
  }
  
  protected void tearDown() throws RemoteException {
    log.info("tearDown");

    // Delete the articles we created
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      deleteArticle(TEST_ARTICLES[i][0]);
  }
  
  protected void ingestArticle(String uri, String resource)
      throws RemoteException, DuplicateArticleIdException, IngestException {
    log.info("ingesting article " + uri + " : " + resource);
    deleteArticle(uri);
    URL article = getClass().getResource(resource);
    try {
      assertEquals("Wrong uri returned,", uri, service.ingest(new DataHandler(article)));
      log.info("ingested article " + uri);
    } catch (DuplicateArticleIdException daie) {
      log.info("already ingested article " + uri);
    }
  }

  protected void deleteArticle(String uri) throws RemoteException {
    try {
      service.delete(uri);
      log.info("deleted article " + uri);
//    } catch (NoSuchArticleIdException nsaie) {
    } catch (Exception nsaie) {
      // so what
      //log.debug(nsie);
    }
  }

  public void testMe() throws Exception {
    boolean success = true;

    success &= tstResults();
    
    assertTrue(success);
  }

  private boolean tstResults() throws Exception {
    String results = ops.gfindObjects("transgenic", 0, 10, 3, 50, "TopazIndex", "copyXml");
    log.info("Search results:\n" + results);

    return true;
  }
}
