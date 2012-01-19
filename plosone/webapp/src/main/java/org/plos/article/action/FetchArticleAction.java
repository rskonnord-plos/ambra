/* $HeadURL::                                                                            $
 * $Id:FetchArticleAction.java 722 2006-10-02 16:42:45Z viru $
 */
package org.plos.article.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.FetchArticleService;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.service.AnnotationService;
import org.topazproject.common.NoSuchIdException;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Fetch article action. 
 */
public class FetchArticleAction extends BaseActionSupport {
  private String articleURI;
  private static final int INITIAL_TRANSFORMED_FILE_SIZE = 100000;
  private String annotationId = "";
  
  private ArrayList<String> messages = new ArrayList<String>();
  private static final Log log = LogFactory.getLog(FetchArticleAction.class);
  private FetchArticleService fetchArticleService;
  private String transformedArticle;
  private AnnotationService annotationService;
  private int numDiscussions = 0;
  private int numAnnotations = 0;
  
  public String execute() throws Exception {
    try {
      //final StringWriter stringWriter = new StringWriter(INITIAL_TRANSFORMED_FILE_SIZE);
      //fetchArticleService.getURIAsHTML(articleURI, );

      setTransformedArticle(fetchArticleService.getURIAsHTML(articleURI));
      //log.debug("transformedArticle: " + transformedArticle);
      Annotation[] articleAnnotations = annotationService.listAnnotations(articleURI);
      for (Annotation a : articleAnnotations) {
        if (a.getContext() == null) {
          numDiscussions ++;
        } else {
          numAnnotations ++;
        }
      }
      
    } catch (NoSuchIdException e) {
      messages.add("No article found for id: " + articleURI);
      log.info("Could not find article: "+ articleURI, e);
      return ERROR;
    } catch (RemoteException e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    } catch (IOException e) {
      messages.add(e.getMessage());
      log.error("Error retrieving article: " + articleURI, e);
      return ERROR;
    }

    return SUCCESS;
  }

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
}
