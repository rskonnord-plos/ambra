/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.FetchArticleService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.AnnotationService;
import org.plos.journal.JournalService;
import org.plos.models.Journal;
import org.plos.models.ObjectInfo;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Fetch article action.
 */
public class FetchArticleAction extends BaseActionSupport {
  private String articleURI;
  private String annotationId = "";

  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private FetchArticleService fetchArticleService;
  private JournalService journalService;
  private Set<Journal> journalList;
  private Session session;
  private ObjectInfo articleInfo;
  private String transformedArticle;
  private AnnotationService annotationService;
  private int numDiscussions = 0;
  private int numAnnotations = 0;

  
  public String execute() throws Exception {
    try {
      setTransformedArticle(fetchArticleService.getURIAsHTML(articleURI));

      Annotation[] articleAnnotations = annotationService.listAnnotations(articleURI);
      for (Annotation a : articleAnnotations) {
        if (a.getContext() == null) {
          numDiscussions ++;
        } else {
          numAnnotations ++;
        }
      }
      setArticleInfo(fetchArticleService.getArticleInfo(articleURI));
      
      TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            journalList = journalService.getJournalsForObject(URI.create(articleURI));
            return null;
          }
        });
    } catch (NoSuchArticleIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: "+ articleURI, e);
      return ERROR;
    } catch (RemoteException e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }


  /**
   * Returns just the annotated article XML
   *
   * @return Annotated Article XML String
   */
  public String getAnnotatedArticle() {
    try {
      setTransformedArticle(fetchArticleService.getAnnotatedContent(articleURI));
    } catch (Exception e) {
      log.error ("Could not get annotated article:" + articleURI, e);
      return ERROR;
    }
    return SUCCESS;
  }


  /**
   * @return transformed output
   */
  public String getTransformedArticle() {
    return transformedArticle;
  }

  private void setTransformedArticle(final String transformedArticle) {
    this.transformedArticle = transformedArticle;
  }

  /** Set the fetch article service
   * @param fetchArticleService fetchArticleService
   */
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * @return articleURI
   */
  @RequiredStringValidator(message = "Article URI is required.")
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Set articleURI to fetch the article for.
   * @param articleURI articleURI
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }

  /**
   * @return Returns the annotationId.
   */
  public String getAnnotationId() {
    return annotationId;
  }

  /**
   * @param annotationId The annotationId to set.
   */
  public void setAnnotationId(String annotationId) {
    this.annotationId = annotationId;
  }

  /**
   * @return Returns the annotationService.
   */
  public AnnotationService getAnnotationService() {
    return annotationService;
  }

  /**
   * @param annotationService The annotationService to set.
   */
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @return Returns the numDiscussions.
   */
  public int getNumDiscussions() {
    return numDiscussions;
  }

  /**
   * @return Returns the numAnnotations.
   */
  public int getNumAnnotations() {
    return numAnnotations;
  }

  /**
   * @return Returns the articleInfo.
   */
  public ObjectInfo getArticleInfo() {
    return articleInfo;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  private void setArticleInfo(ObjectInfo articleInfo) {
    this.articleInfo = articleInfo;
  }


  /**
   * @param journalService The journalService to set.
   */
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }


  /**
   * @return Returns the journalList.
   */
  public Set<Journal> getJournalList() {
    return journalList;
  }

  /**
   * @param session The session to set.
   */
  public void setOtmSession(Session session) {
    this.session = session;
  }
}
