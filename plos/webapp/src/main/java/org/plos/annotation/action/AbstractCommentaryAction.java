/* $$HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.1/plos/webapp/src/main/#$$
 * $$Id: GetCommentaryAction.java 4629 2008-02-04 06:07:05Z alex $$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.action;

import java.net.URI;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.Commentary;
import org.plos.annotation.service.WebAnnotation;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.models.Article;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action class to get a list of all commentary for an article and the threads associated
 * with each base comment.
 * 
 * <p>Concrete classes are responsible for providing the desired list of annotations 
 * which is a function of the use case.
 * 
 * @author Stephen Cheng
 * @author jkirton
 */
public abstract class AbstractCommentaryAction extends AnnotationActionSupport {
  
  protected final Log log = LogFactory.getLog(this.getClass());

  private String target;
  private Commentary[] commentary;
  private Article articleInfo;
  private ArticleOtmService articleOtmService;
  
  /**
   * @return Simple presentation worthy description 
   * of this implementation's use case.
   */
  protected abstract String useCaseDescriptor();
  
  /**
   * Provides the use case specific array of {@link WebAnnotation}s 
   * for which commentary is provided.
   * @return Array of {@link WebAnnotation}s
   * @throws ApplicationException
   */
  protected abstract WebAnnotation[] getAnnotations() throws ApplicationException;

  /**
   * Get all commentary for an article and sort them in reverse chronological order.
   * 
   * @return status
   * @throws Exception
   */
  @Override
  public final String execute() throws Exception {
    final String useCaseDsc = useCaseDescriptor();
    try {
      if (log.isDebugEnabled()){
        log.debug("retrieving " + useCaseDsc + " for article id: " + target);
      }
      articleInfo = getArticleOtmService().getArticle(new URI(target));
      WebAnnotation[] annotations = getAnnotations();
      commentary = new Commentary[annotations.length];
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
          commentary[i] = com;
        }
        Arrays.sort(commentary, commentary[0]);
      }
    } catch (final ApplicationException e) {
      log.error("Could not get " + useCaseDsc + " for articleID: " + target, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  public final String getArticleMetaInfo () throws Exception {
    articleInfo = getArticleOtmService().getArticle(new URI(target));
    return SUCCESS;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public final void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @return the target of the annotation
   */
  @RequiredStringValidator(message="You must specify the target that you want to list the annotations for")
  public final String getTarget() {
    return target;
  }

  /**
   * @return Returns the articleOtmService.
   */
  protected final ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * @param articleOtmService The articleOtmService to set.
   */
  public final void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the articleInfo.
   */
  public final Article getArticleInfo() {
    return articleInfo;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  public final void setArticleInfo(Article articleInfo) {
    this.articleInfo = articleInfo;
  }

  /**
   * @return The commentary array
   */
  public final Commentary[] getCommentary() {
    return commentary;
  }

}
