/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.annotation.service.Annotation;
import org.plos.annotation.Commentary;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.models.ObjectInfo;

import java.util.Arrays;

/**
 * Action class to get a list of all commentary for an article and the threads associated
 * with each base comment.
 * 
 * @author Stephen Cheng
 */
public class GetCommentaryAction extends AnnotationActionSupport {
  private String target;
  private Annotation[] annotations;
  private Commentary[] allCommentary;
  private ObjectInfo articleInfo;
  private ArticleOtmService articleOtmService;
  
  private static final Log log = LogFactory.getLog(ListAnnotationAction.class);

  /**
   * Get all commentary for an article and sort them in reverse chronological order.
   * 
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      if (log.isDebugEnabled()){
        log.debug("retrieving all commentary for article id: " + target);
      }
      articleInfo = getArticleOtmService().getObjectInfo(target);
      annotations = getAnnotationService().listAnnotations(target);
      allCommentary = new Commentary[annotations.length];
      Commentary com = null;
      if (annotations.length > 0) {
        for (int i = 0; i < annotations.length; i++) {
          com = new Commentary();
          com.setAnnotation(annotations[i]);
          try {
            getAnnotationService().listAllReplies(annotations[i].getId(), annotations[i].getId(), com);
          } catch (ApplicationException ae) {
            Throwable t = ae.getCause();
            if ((t instanceof NoSuchArticleIdException) || (t instanceof java.lang.SecurityException)) {
              // don't error if that id is gone or if you can't list the replies
              com.setNumReplies(0);
              com.setReplies(null);
            } else {
              throw ae;
            }
          }
          allCommentary[i] = com;
        }
        Arrays.sort(allCommentary, allCommentary[0]);  
      }
    } catch (final ApplicationException e) {
      log.error("Could not get all commentary for articleID: " + target, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }
  
  public String getArticleMetaInfo () throws Exception {
    articleInfo = getArticleOtmService().getObjectInfo(target);
    return SUCCESS;
  }
    
    
  

  /**
   * @return a list of annotations
   */
  private Annotation[] getAnnotations() {
    return annotations;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @return the target of the annotation
   */
  @RequiredStringValidator(message="You must specify the target that you want to list the annotations for")
  public String getTarget() {
    return target;
  }

  /**
   * @return Returns the articleOtmService.
   */
  protected ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * @param articleOtmService The articleOtmService to set.
   */
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the allCommentary.
   */
  public Commentary[] getAllCommentary() {
    return allCommentary;
  }

  /**
   * @param allCommentary The allCommentary to set.
   */
  public void setAllCommentary(Commentary[] allCommentary) {
    this.allCommentary = allCommentary;
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
  public void setArticleInfo(ObjectInfo articleInfo) {
    this.articleInfo = articleInfo;
  }

}
