/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.ambra.rating.action;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.action.BaseSessionAwareActionSupport;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.models.Article;

/**
 * AbstractRatingAction - Common base class to rating related actions.
 *
 * @author jkirton
 */
@SuppressWarnings("serial")
public abstract class AbstractRatingAction extends BaseSessionAwareActionSupport {
  protected static final Log log = LogFactory.getLog(AbstractRatingAction.class);

  protected ArticleOtmService articleOtmService;

  /**
   * @param articleOtmService the ArticleOtmService to set
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * Determines if an article is a research type article.
   *
   * @param articleURI The URI of the article.
   * @return true/false
   * @throws NoSuchArticleIdException When no article resolvable for the articleURI. 
   * @throws ApplicationException When no article type is resolvable for the article.
   */
  protected final boolean isResearchArticle(String articleURI)
    throws ApplicationException, NoSuchArticleIdException {
    // resolve article type and supported properties
    Article artInfo = articleOtmService.getArticle(URI.create(articleURI));
    ArticleType articleType = ArticleType.getDefaultArticleType();
    for (URI artTypeUri : artInfo.getArticleType()) {
      if (ArticleType.getKnownArticleTypeForURI(artTypeUri) != null) {
        articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
        break;
      }
    }
    if (articleType == null)
      throw new ApplicationException("Unable to resolve article type for: " + articleURI);
    return ArticleType.isResearchArticle(articleType);
  }
}
