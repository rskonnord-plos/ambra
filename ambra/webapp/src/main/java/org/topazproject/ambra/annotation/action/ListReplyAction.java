/* $HeadURL::                                                                            $
 * $Id:ListReplyAction.java 722 2006-10-02 16:42:45Z viru $
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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.annotation.service.WebReply;
import org.topazproject.ambra.article.action.CreateCitation;
import org.topazproject.ambra.article.service.CitationInfo;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.util.ArticleXMLUtils;
import org.topazproject.ambra.util.CitationUtils;
import org.xml.sax.SAXException;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.thoughtworks.xstream.XStream;

/**
 * Action class to get a list of replies to annotations.
 */
@SuppressWarnings("serial")
public class ListReplyAction extends AnnotationActionSupport {

  private String root;
  private String inReplyTo;
  private WebReply[] replies;
  private WebAnnotation baseAnnotation;
  private Article article;
  private ArticleXMLUtils citationService;
  private FetchArticleService fetchArticleService;
  private Cache articleAnnotationCache;
  private String citation;

  private static final Log log = LogFactory.getLog(ListReplyAction.class);

  @Override
  public String execute() throws Exception {
    try {
      replies = getAnnotationService().listReplies(root, inReplyTo);
    } catch (final ApplicationException e) {
      log.error("ListReplyAction.execute() failed for root: " + root, e);
      addActionError("Reply fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * List all the replies for a given root and inRelyTo in a threaded tree
   * structure.
   * 
   * @return webwork status for the call
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String listAllReplies() throws Exception {
    try {
      // Allow a single 'root' param to be accepted. If 'inReplyTo' is null or
      // empty string, set to root value.
      // This results in that single annotation being displayed.
      if ((inReplyTo == null) || inReplyTo.length() == 0) {
        inReplyTo = root;
      }
      if (log.isDebugEnabled()) {
        log.debug("listing all Replies for root: " + root);
      }
      baseAnnotation = getAnnotationService().getAnnotation(root);
      replies = getAnnotationService().listAllReplies(root, inReplyTo);
      final String articleId = baseAnnotation.getAnnotates();
      article = fetchArticleService.getArticleInfo(articleId);

      // construct citation string
      // we're only showing annotation citations for formal corrections
      if(baseAnnotation.isFormalCorrection()) {
        // lock @ Article level
        final Object lock = (FetchArticleService.ARTICLE_LOCK + articleId).intern();
        CitationInfo result = articleAnnotationCache.get(
                CreateCitation.CITATION_KEY + articleId, -1,
                new Cache.SynchronizedLookup<CitationInfo, ApplicationException>(lock) {
                  public CitationInfo lookup() throws ApplicationException {

                    XStream xstream = new XStream();
                    try {
                      return (CitationInfo) xstream.fromXML(
                              citationService.getTransformedArticle(articleId));
                    } catch (IOException ie) {
                      throw new ApplicationException(ie);
                    } catch (NoSuchArticleIdException nsaie) {
                      throw new ApplicationException(nsaie);
                    } catch (ParserConfigurationException pce) {
                      throw new ApplicationException(pce);
                    } catch (SAXException se) {
                      throw new ApplicationException(se);
                    }
            }
        });
        citation = CitationUtils.generateArticleCorrectionCitationString(result, baseAnnotation);
      }
    } catch (ApplicationException ae) {
      citation = null;
      log.error("Could not list all replies for root: " + root, ae);
      addActionError("Reply fetching failed with error message: " + ae.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  /**
   * @return The constructed annotation citation string.
   */
  public String getCitation() {
    return citation;
  }

  public void setRoot(final String root) {
    this.root = root;
  }

  public void setInReplyTo(final String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public WebReply[] getReplies() {
    return replies;
  }

  @RequiredStringValidator(message = "root is required")
  public String getRoot() {
    return root;
  }

  public String getInReplyTo() {
    return inReplyTo;
  }

  /**
   * @return Returns the baseAnnotation.
   */
  public WebAnnotation getBaseAnnotation() {
    return baseAnnotation;
  }

  /**
   * @param baseAnnotation The baseAnnotation to set.
   */
  public void setBaseAnnotation(WebAnnotation baseAnnotation) {
    this.baseAnnotation = baseAnnotation;
  }

  /**
   * @param fetchArticleService The fetchArticleService to set.
   */
  public void setFetchArticleService(FetchArticleService s) {
    this.fetchArticleService = s;
  }

  /**
   * @return Returns the articleInfo.
   */
  public Article getArticleInfo() {
    return article;
  }

  /**
   * @param articleInfo The articleInfo to set.
   */
  public void setArticleInfo(Article articleInfo) {
    this.article = articleInfo;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }

  /**
   * @param citationService The citationService to set.
   */
  @Required
  public void setCitationService(ArticleXMLUtils citationService) {
    this.citationService = citationService;
  }
}
