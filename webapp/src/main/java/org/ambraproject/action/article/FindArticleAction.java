/*
 * $HeadURL$
 * $Id$
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

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.crossref.CrossRefLookupService;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.models.CitedArticleAuthor;
import org.ambraproject.service.pubget.PubGetLookupService;
import org.ambraproject.views.CitedArticleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Display online resources for the references in an article.
 * </p>
 * Lookup is performed on <a href="http://www.crossref.org">CrossRef</a>
 *
 * @author Dragisa Krsmanovic
 */
public class FindArticleAction extends BaseActionSupport {

  private static final Logger log = LoggerFactory.getLogger(FindArticleAction.class);

  private long citedArticleID;

  private String crossRefUrl;
  private String pubGetUrl;
  private String title;
  private String author;
  private String originalDOI;

  private CrossRefLookupService crossRefLookupService;
  private PubGetLookupService pubGetLookupService;
  private ArticleService articleService;

  @Override
  public String execute() throws Exception {

    // We first look for the DOI in the DB.  If it's not there, we query CrossRef, and if we get back a DOI,
    // store it in the DB for next time.
    CitedArticleView citedArticleView = articleService.getCitedArticle(citedArticleID);
    CitedArticle citedArticle = citedArticleView.getCitedArticle();

    title = citedArticle.getTitle() == null ? "" : citedArticle.getTitle();

    String doi = citedArticle.getDoi();
    originalDOI = citedArticleView.getArticleDoi();

    if (doi == null || doi.isEmpty()) {
      doi = articleService.refreshArticleCitation(citedArticle);
    }

    if (doi != null && !doi.isEmpty()) {
      crossRefUrl = "http://dx.doi.org/" + doi;

      // We never cache or store these PDF links, because they change frequently.
      pubGetUrl = pubGetLookupService.getPDFLink(doi);
    } else {
      crossRefUrl = configuration.getString("ambra.services.crossref.guestquery.url");
    }

    return SUCCESS;
  }

  @Required
  public void setCrossRefLookupService(CrossRefLookupService crossRefLookupService) {
    this.crossRefLookupService = crossRefLookupService;
  }

  @Required
  public void setPubGetLookupService(PubGetLookupService pubGetLookupService) {
    this.pubGetLookupService = pubGetLookupService;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @RequiredFieldValidator(message = "citedArticleID is a required field")
  public long getCitedArticleID() {
    return citedArticleID;
  }

  public void setCitedArticleID(long citedArticleID) {
    this.citedArticleID = citedArticleID;
  }

  public String getOriginalDOI() {
    return originalDOI;
  }

  public String getCrossRefUrl() {
    return crossRefUrl;
  }

  public String getPubGetUrl() {
    return pubGetUrl;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }
}
