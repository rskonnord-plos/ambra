/* $HeadURL:: http://ambraproject.org/svn/ambra/ambra/branches/aggregation-refactor/weba#$
 * $Id: FetchArticleAction.java 10952 2012-05-01 00:41:39Z josowski $
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
package org.ambraproject.action.article;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

/**
 * This class fetches the information from the service tier for an artcle
 *
 */
public class FetchArticleAction extends BaseSessionAwareActionSupport {
  private static final Logger log = LoggerFactory.getLogger(FetchArticleAction.class);
  private final ArrayList<String> messages = new ArrayList<String>();

  private String articleURI;
  private ArticleInfo articleInfoX;
  private ArticleService articleService;

  /**
   * Fetch common data the article HTML text
   *
   * @return "success" on succes, "error" on error
   */
  @Transactional
  public String fetchArticleInfo() throws NoSuchArticleIdException {

    try {
      UriUtil.validateUri(articleURI, "articleURI=<" + articleURI + ">");
    } catch (Exception e) {
      throw new NoSuchArticleIdException(articleURI, e.getMessage(), e);
    }

    articleInfoX = articleService.getArticleInfo(articleURI, getAuthId());

    return SUCCESS;
  }

  /**
   * Set the fetch article service
   *
   * @param articleService articleService
   */
  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
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
   *
   * @param articleURI articleURI
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Return the ArticleInfo from the Browse cache.
   * <p/>
   *
   * @return Returns the articleInfoX.
   */
  public ArticleInfo getArticleInfoX() {
    return articleInfoX;
  }
}
