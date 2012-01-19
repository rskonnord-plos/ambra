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

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.service.BrowseService;
import org.plos.article.service.FetchArticleService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.journal.JournalService;
import org.plos.model.article.ArticleInfo;
import org.plos.model.article.ArticleType;
import org.plos.models.Article;
import org.plos.models.Journal;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Fetch article action.
 */
@SuppressWarnings("serial")
public class FetchArticleAction extends BaseActionSupport {
  private String articleURI;
  private String annotationId = "";

  private final ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private BrowseService browseService;
  private FetchArticleService fetchArticleService;
  private JournalService journalService;
  private Set<Journal> journalList;
  private Session session;
  private Article articleInfo;
  private ArticleType articleType;
  private ArticleInfo articleInfoX;
  private String transformedArticle;
  private final String articleTypeHeading = "Research Article"; // displayed article type (assigned default)
  private AnnotationService annotationService;
  private int numDiscussions = 0;
  private int numMinorCorrections = 0;
  private int numFormalCorrections = 0;
  /**
   * Represents the number of notes that are not corrections from  a UI stand point
   */
  private int numComments = 0;

  
  @Override
  public String execute() throws Exception {
    try {
      setTransformedArticle(fetchArticleService.getURIAsHTML(articleURI));

      WebAnnotation[] articleAnnotations = annotationService.listAnnotations(articleURI);
      for (WebAnnotation a : articleAnnotations) {
        if (a.getContext() == null) {
          numDiscussions ++;
        } else {
          if (a.isMinorCorrection()) {
            numMinorCorrections++;
          } else if (a.isFormalCorrection()) {
            numFormalCorrections++;
          } else {
            numComments++;
          }
        }
      }
      Article artInfo = fetchArticleService.getArticleInfo(articleURI); 
      
      setArticleInfo(artInfo);
      
      articleType = ArticleType.getDefaultArticleType();
      for (URI artTypeUri : artInfo.getArticleType()) {
        if (ArticleType.getKnownArticleTypeForURI(artTypeUri)!= null) {
          articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
          break;
        }
      }
      
      TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            
            // get the alternate ArticleInfo, e.g. contains RelatedArticles
            articleInfoX = browseService.getArticleInfo(URI.create(articleURI));
            
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
   * A struts Action method used to display the annotated article. 
   * The transformedArticle field is populated with the annotated articleURI content. 
   * 
   * @return Annotated Article XML String
   */
  public String displayAnnotatedArticle() {
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
   * @return Returns the numComments.
   */
  public int getNumComments() {
    return numComments;
  }

  /**
   * @return Returns the numMinorCorrections.
   */
  public int getNumMinorCorrections() {
    return numMinorCorrections;
  }

  /**
   * @return Returns the numMinorCorrections.
   */
  public int getNumFormalCorrections() {
    return numFormalCorrections;
  }

  /**
   * @return Returns the calculated number of notes.
   */
  public int getNumNotes() {
    return numComments + numMinorCorrections + numFormalCorrections;
  }

  /**
   * @return Returns the total number of corrections.
   */
  public int getNumCorrections() {
    return numMinorCorrections + numFormalCorrections;
  }

  /**
   * Return the ArticleInfo from the Browse cache.
   * 
   * TODO: convert all usages of "articleInfo" (ObjectInfo) to use the Browse cache version of
   * ArticleInfo.  Note that for all templates to use ArticleInfo, it will have to
   * be enhanced.  articleInfo & articleInfoX are both present, for now, to support:
   *   - existing templates/services w/o a large conversion
   *   - access to RelatedArticles
   * 
   * @return Returns the articleInfoX.
   */
  public ArticleInfo getArticleInfoX() {
    return articleInfoX;
  }

  /**
   * @return Returns the articleInfo.
   */
  public Article getArticleInfo() {
    return articleInfo;
  }

  /**
   * The article type displayed by article_content.ftl as per #693
   * @return
   */
  public String getArticleTypeHeading() {
  	return articleTypeHeading;
  }
  
  /**
   * @param articleInfo The articleInfo to set.
   */
  private void setArticleInfo(Article articleInfo) {
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
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * @param session The session to set.
   */
  public void setOtmSession(Session session) {
    this.session = session;
  }


  public ArticleType getArticleType() {
    return articleType;
  }
}
