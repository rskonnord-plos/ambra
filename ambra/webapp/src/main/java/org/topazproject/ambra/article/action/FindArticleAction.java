/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.crossref.CrossRefArticle;
import org.topazproject.ambra.crossref.CrossRefLookupService;
import org.topazproject.ambra.pubget.PubGetLookupService;

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


  private String author;
  private String title;

  private String crossRefUrl;
  private String pubGetUrl;

  private CrossRefLookupService crossRefLookupService;
  private PubGetLookupService pubGetLookupService;

  @Override
  public String execute() throws Exception {

    List<CrossRefArticle> results = crossRefLookupService.findArticles(title, author);

    if (results.isEmpty()) {
      log.debug("No articles found on CrossRef");
      crossRefUrl = configuration.getString("ambra.services.crossref.guestquery.url");
    } else {
      CrossRefArticle article = results.get(0);
      if (log.isDebugEnabled()) {
        log.debug("Found article " + article);
      }
      crossRefUrl = "http://dx.doi.org/" + article.getDoi();

      pubGetUrl = pubGetLookupService.getPDFLink(article.getDoi());
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

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCrossRefUrl() {
    return crossRefUrl;
  }

  public String getPubGetUrl() {
    return pubGetUrl;
  }
}
