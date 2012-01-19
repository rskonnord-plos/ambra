/* $HeadURL::                                                                             $
 * $Id::                                                         $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.topazproject.ambra.annotation.action;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.Commentary;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.models.Article;


import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action class to get a list of all commentary for an article and the threads associated
 * with each base comment.
 *
 * @author Stephen Cheng
 * @author jkirton
 * @author Alex Worden
 */
@SuppressWarnings("serial")
public class GetCommentaryAction extends AnnotationActionSupport {

  protected final Log log = LogFactory.getLog(this.getClass());

  protected String target;
  private Commentary[] commentary;
  private Article article;
  private FetchArticleService fetchArticleService;

  /**
   * Provides a list of comments for the target.
   * @return Array of {@link WebAnnotation}s representing the target's comments
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public final String listComments() throws Exception {
    return list(false);
  }

  /**
   * Provides a list of corrections for the target.
   * @return Array of {@link WebAnnotation}s representing the target's corrections
   * @throws ApplicationException
   */
  @Transactional(readOnly = true)
  public final String listCorrections() throws Exception {
    return list(true);
  }

  /**
   * Pulls either all corrections or all non-correction comments for the given target.
   * @param isCorrections Pull corrections?
   * @return status
   * @throws Exception
   */
  private String list(boolean isCorrections) throws Exception {
    try {
      article = fetchArticleService.getArticleInfo(target);
      WebAnnotation[] annotations = isCorrections? getAnnotationService().listCorrections(target) : getAnnotationService().listComments(target);
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
      if(log.isErrorEnabled()) log.error("Could not get " + (isCorrections ? "corrections" : "comments") + " for articleID: " + target, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  @Transactional(readOnly = true)
  public final String getArticleMetaInfo () throws Exception {
    article = fetchArticleService.getArticleInfo(target);
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
   * @param articleOtmService The articleOtmService to set.
   */
  public final void setFetchArticleService(FetchArticleService service) {
    this.fetchArticleService = service;
  }

  /**
   * @return Returns the articleInfo.
   */
  public final Article getArticleInfo() {
    return article;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  public final void setArticleInfo(Article articleInfo) {
    this.article = articleInfo;
  }

  /**
   * @return The commentary array
   */
  public final Commentary[] getCommentary() {
    return commentary;
  }

}
