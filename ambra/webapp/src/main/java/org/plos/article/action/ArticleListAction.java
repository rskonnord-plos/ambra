/* $HeadURL::                                                                            $
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

package org.plos.article.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.FetchArticleService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Fetch as list of available articles.
 */
public class ArticleListAction extends BaseActionSupport {
  private ArrayList<String> messages = new ArrayList<String>();
  private FetchArticleService fetchArticleService;
  private Collection<String> articles;
  private String startDate;
  private String endDate;

  private static final Log log = LogFactory.getLog(ArticleListAction.class);

  public String execute() throws Exception {
    articles = fetchArticleService.getArticleIds(startDate, endDate, null);
    return SUCCESS;
  }

  /**
   * @return the list of available articles
   */
  public Collection<String> getArticles() {
    return articles;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }

  /** Set the fetch article service
   * @param fetchArticleService fetchArticleService
   */
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * @return end date
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Set end date
   * @param endDate endDate
   */
  public void setEndDate(final String endDate) {
    this.endDate = endDate;
  }

  /**
   * @return start date
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Set start date
   * @param startDate startDate
   */
  public void setStartDate(final String startDate) {
    this.startDate = startDate;
  }
}
