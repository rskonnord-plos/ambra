/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
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

import org.ambraproject.views.CitationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.util.UriUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Action to create a citation.  Does not care what the output format is.
 *
 * @author Stephen Cheng
 *
 */
@SuppressWarnings("serial")
public class CreateCitation extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(CreateCitation.class);

  private ArticleService articleService;     // OTM service Spring injected.
  private String articleURI;
  private String doi;
  private CitationView citation;

  /**
   * Get Citation object from database
   */
  @Override
  @Transactional(readOnly = true)
  public String execute () throws Exception {

    try {
      UriUtil.validateUri(articleURI, "articleUri=<" + articleURI + ">");
    } catch (Exception ex) {
      return ERROR;
    }

    try {
      Article article = articleService.getArticle(articleURI, getAuthId());
      doi = article.getDoi();
      citation =  CitationView.builder()
              .setDoi(article.getDoi())
              .seteLocationId(article.geteLocationId())
              .setUrl(article.getUrl())
              .setTitle(article.getTitle())
              .setJournal(article.getJournal())
              .setVolume(article.getVolume())
              .setIssue(article.getIssue())
              .setSummary(article.getDescription())
              .setPublisherName(article.getPublisherName())
              .setPublishedDate(article.getDate())
              .setAuthorList(article.getAuthors())
              .setCollaborativeAuthors(article.getCollaborativeAuthors())
              .build();

    } catch (NoSuchArticleIdException ex) {
      return ERROR;
    }
    
    return SUCCESS;
  }

  /**
   * @param articleService ArticleService Spring Injected
   */
  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
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
   * @return return the article citation info
   */
  public CitationView getCitation() {
    return citation;
  }

  public String getDoi() {
    return doi;
  }
}
