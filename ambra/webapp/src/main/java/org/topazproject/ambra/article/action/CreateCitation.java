/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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

package org.topazproject.ambra.article.action;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.otm.RdfUtil;

/**
 * Action to create a citation.  Does not care what the output format is.
 *
 * @author Stephen Cheng
 *
 */
@SuppressWarnings("serial")
public class CreateCitation extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(CreateCitation.class);

  public static final String CITATION_KEY = "ArticleAnnotationCache-Citation-";

  private ArticleOtmService articleOtmService;     // OTM service Spring injected.

  private String articleURI;
  private String title;
  private Citation citation;

  /**
   * Get Citation object from database
   */
  @Override
  @Transactional(readOnly = true)
  public String execute () throws Exception {

    RdfUtil.validateUri(articleURI, "articleUri=<" + articleURI + ">");

    DublinCore articleDB = articleOtmService.getArticle(URI.create(articleURI)).getDublinCore();

    citation = articleDB.getBibliographicCitation();
    title = articleDB.getTitle();

    citation.getAuthors();
    
    return SUCCESS;
  }

  /**
   * @param articleOtmService ArticleOtmService Spring Injected
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * @return Returns the citation.
   */
  public Citation getCitation() {
    return citation;
  }

  /**
   * @return Returns the article title
   */
  public String getTitle() {
    return title;
  }
}
