/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.action;

import org.ambraproject.service.article.MostViewedArticleService;
import org.ambraproject.service.search.SolrException;
import org.ambraproject.views.article.HomePageArticleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author akudlick
 */

public class MostViewedAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(MostViewedAction.class);

  private MostViewedArticleService mostViewedArticleService;

  private List<HomePageArticleInfo> mostViewedArticles;
  private String mostViewedComment;

  /**
   * This execute method always returns SUCCESS
   */
  @Override
  public String execute() {
    if (mostViewedEnabled()) {
      initMostViewed();
    } else {
      mostViewedArticles = new ArrayList<HomePageArticleInfo>();
    }
    return SUCCESS;
  }

  private boolean mostViewedEnabled() {
    return configuration.containsKey("ambra.virtualJournals." + getCurrentJournal() + ".mostViewedArticles.limit") && mostViewedArticleService != null;
  }

  private void initMostViewed() {
    String mostViewedKey = "ambra.virtualJournals." + getCurrentJournal() + ".mostViewedArticles";
    if (configuration.containsKey(mostViewedKey + ".message")) {
      mostViewedComment = configuration.getString(mostViewedKey + ".message");
    }
    try {
      int limit = configuration.getInt(mostViewedKey + ".limit");
      Integer days;
      try {
        days = configuration.getInt(mostViewedKey + ".timeFrame");
      } catch (Exception e) {
        days = null;
      }

      mostViewedArticles = mostViewedArticleService.getMostViewedArticles(getCurrentJournal(), limit, days);
    } catch (SolrException e) {
      log.error("Error querying solr for most viewed articles; returning empty list", e);
      mostViewedArticles = new LinkedList<HomePageArticleInfo>();
    }

  }

  public void setMostViewedArticleService(MostViewedArticleService mostViewedArticleService) {
    this.mostViewedArticleService = mostViewedArticleService;
  }

  public List<HomePageArticleInfo> getMostViewedArticles() {
    return mostViewedArticles;
  }

  public String getMostViewedComment() {
    return mostViewedComment;
  }
}
