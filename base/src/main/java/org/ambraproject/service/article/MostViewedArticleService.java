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

package org.ambraproject.service.article;

import org.ambraproject.service.search.SolrException;
import org.ambraproject.util.Pair;
import org.ambraproject.views.article.HomePageArticleInfo;

import java.net.URI;
import java.util.List;

/**
 * @author Alex Kudlick Date: 4/19/11
 *         <p/>
 *         org.ambraproject.article.service
 */
public interface MostViewedArticleService {
  /**
   * Get the most viewed Articles from Solr
   *
   * @param journal - the journal to get articles from
   * @param limit   - The number of articles to return
   * @param numDays - the number of days over which to count the views. If null; the default is over all time
   * @return - a list of dois and titles of the most viewed articles, in order
   */
  //TODO: make this a list of articles instead of pairs
  //This is *bad practice* because it mixes the view (i.e. "we just want to see the title and doi") with
  //the model (i.e. the "most viewed articles") ... but ArticleService is also bad
  public List<Pair<String, String>> getMostViewedArticles(String journal, int limit, Integer numDays) throws SolrException;

  /**
   * Get the most viewed Articles with pagination from Solr.
   *
   * @param journal - the journal to get articles from
   * @param offset - the offset index of the first article of the page to fetch
   * @param limit - the number of articles in the page to fetch
   * @param numDays - (optional) maximum number of days for the articles.
   * @return - a list of article info that is used to display on the home page.
   * @throws SolrException
   */
  public List<HomePageArticleInfo> getMostViewedArticleInfo(String journal, int offset, int limit, Integer numDays) throws SolrException;

  /**
   * Get the most recent Articles with pagination from Solr.
   *
   * @param journal - the journal to get articles from
   * @param offset - the offset index of the first article of the page to fetch
   * @param limit - the number of articles in the page to fetch
   * @param articleTypes - the list of possible article types to fetch.
   * @return - a list of article info that is used to display on the home page.
   * @throws SolrException
   */
  public List<HomePageArticleInfo> getRecentArticleInfo(String journal, int offset, int limit, List<URI> articleTypes) throws SolrException;

  /**
   * Get the articles for "In the News" from database.
   * @param listCode - the list code of the set of articles, e.g., "plosone_news"
   * @return - a list of article info that is used to display on the home page.
   */
  public List<HomePageArticleInfo> getNewsArticleInfo(String listCode);

}
