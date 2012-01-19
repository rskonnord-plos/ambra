
/* $HeadURL::                                                                            $
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

package org.topazproject.ambra.article.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.topazproject.ambra.article.BrowseResult;
import org.topazproject.ambra.article.action.TOCArticleGroup;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.Years;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.search2.SearchHit;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author Alex Worden, stevec
 */
public interface BrowseService {

  String ARTICLE_KEY         = "Article-";

  /**
   * Get the dates of all articles with a <code>state</code> of <code>ACTIVE</code>
   * (meaning the articles have been published).
   * The outer map is a map of years, the next inner map a map
   * of months, and finally the innermost is a list of days.
   * <br/>
   *
   * @return the article dates.
   */
  public Years getArticleDates();

  /**
   * Get articles in the given category. One "page" of articles will be returned, i.e. articles
   * pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less than a pageSize articles
   * may be returned, either because it's the end of the list or because some articles are not
   * accessible.
   *
   * @param catName  the category for which to return the articles
   * @param pageNum  the page-number for which to return articles; 0-based
   * @param pageSize the number of articles per page
   * @param numArt   (output) the total number of articles in the given category
   * @return the articles.
   */
  public List<SearchHit> getArticlesByCategory(final String catName, final int pageNum, final int pageSize,
                                                 long[] numArt);

  /**
   * Get articles in the given date range, from newest to oldest, of the given article type(s).
   * One "page" of articles will be returned,
   * i.e. articles pageNum * pageSize .. (pageNum + 1) * pageSize - 1 .
   * Note that less than a pageSize articles may be returned, either because it's the end
   * of the list or because some articles are not accessible.
   * <p/>
   * Note: this method assumes the dates are truly just dates, i.e. no hours, minutes, etc.
   * <p/>
   * If the <code>articleTypes</code> parameter is null or empty,
   * then all types of articles are returned.
   * <p/>
   * This method should never return null.
   *
   * @param startDate    the earliest date for which to return articles (inclusive)
   * @param endDate      the latest date for which to return articles (exclusive)
   * @param articleTypes The URIs indicating the types of articles which will be returned,
   *                       or null for all types
   * @param pageNum      the page-number for which to return articles; 0-based
   * @param pageSize     the number of articles per page, or -1 for all articles
   * @param numArt       (output) the total number of articles in the given category
   * @return the articles.
   */
  public List<SearchHit> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                             final List<URI> articleTypes, final int pageNum,
                                             final int pageSize, long[] numArt);
  /**
   * Get a list of article-counts for each category.
   *
   * @return the category infos.
   */
  public SortedMap<String, Long> getArticlesByCategory();

  /**
   * Get Issue information.
   *
   * @param doi DOI of Issue.
   * @return the Issue information.
   */
  public IssueInfo getIssueInfo(final URI doi);

  /**
   * Return the ID of the latest issue from the latest volume.
   * If no issue exists in the latest volume, then look at the previous volume and so on.
   * The Current Issue for each Journal should be configured via the admin console.
   * This method is a reasonable way to get the most recent Issue if Current Issue was not set.
   *
   * @param journal The journal in which to seek the most recent Issue
   * @return The most recent Issue from the most recent Volume, or null if there are no Issues
   */
  public URI getLatestIssueFromLatestVolume(Journal journal);

  /**
   * Returns the list of ArticleInfos contained in this Issue. The list will contain only
   * ArticleInfos for Articles that the current user has permission to view.
   *
   * @param issueDOI Issue ID
   * @return List of ArticleInfo objects.
   */
  public List<ArticleInfo> getArticleInfosForIssue(final URI issueDOI);
  /**
   * Get a VolumeInfo for the given id. This only works if the volume is in the current journal.
   *
   * @param id Volume ID
   * @return VolumeInfo
   */
  public VolumeInfo getVolumeInfo(URI id);

  /**
   * Returns a list of VolumeInfos for the given Journal. VolumeInfos are sorted in reverse order
   * to reflect most common usage. Uses the pull-through cache.
   *
   * @param journal To find VolumeInfos for.
   * @return VolumeInfos for journal in reverse order.
   */
  public List<VolumeInfo> getVolumeInfosForJournal(final Journal journal);

  public ArticleInfo getArticleInfo(final URI id);

  /**
   * Given a list of Article Groups with correctly ordered articles
   * create a CSV string of article URIs. The URIs will be in the
   * order that they show up on the TOC.
   *
   * @param  articleGroups the list of TOCArticleGroup to process.
   * @return a string of a comma separated list of article URIs
   */
  public String articleGrpListToCSV( List<TOCArticleGroup> articleGroups);
  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(URI issueURI);

  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(Issue issue);
  /**
   *
   */
  public List<TOCArticleGroup> buildArticleGroups(Issue issue, List<TOCArticleGroup> articleGroups);
  /**
   * Get ordered list of articles. Either from articleList or from
   * simpleCollection if articleList is empty.
   * @param issue
   * @return List of article URI's
   */
  public List<URI> getArticleList(Issue issue);

  /**
   * ArticleInfo objects store lists of FormalCorrection id's because ArticleInfo is
   * object that is not cached in the ObjectCache and it cannot hold referencews to OTM entities
   * because it will cause it to fail serialization.
   * We need to fetch FormalCorrection objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all FormalCorrection objects contained in articleGroups
   */
  public Map<URI, FormalCorrection> getCorrectionMap(List<TOCArticleGroup> articleGroups);
  /**
   * ArticleInfo objects store lists of Retraction id's because ArticleInfo is
   * object that is not cached in the ObjectCache and it cannot hold referencews to OTM entities
   * because it will cause it to fail serialization.
   * We need to fetch Retraction objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all Retraction objects contained in articleGroups
   */
  public Map<URI, Retraction> getRetractionMap(List<TOCArticleGroup> articleGroups);

  /**
   * Returns a list of articles for a given category
   * @param category category
   * @param pageNum page number for which to return articles; 0-based
   * @param pageSize number of articles per page
   * @return articles
   */
  public BrowseResult getArticlesByCategoryViaSolr(final String category, final int pageNum, final int pageSize);

  /**
   * Checks to see if solr is up or not
   *
   * @throws SolrServerException
   * @throws IOException
   */
  public void pingSolr() throws SolrServerException, IOException;
}