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

package org.ambraproject.action;

import org.ambraproject.service.article.BrowseParameters;
import org.ambraproject.service.article.BrowseService;
import org.ambraproject.service.article.MostViewedArticleService;
import org.ambraproject.service.search.SolrException;
import org.ambraproject.views.BrowseResult;
import org.ambraproject.views.SearchHit;
import org.ambraproject.views.article.HomePageArticleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author stevec
 */
@SuppressWarnings("serial")
public class HomePageAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(HomePageAction.class);

  private BrowseService browseService;
  private MostViewedArticleService mostViewedArticleService;

  private ArrayList<SearchHit> recentArticles;
  private List<HomePageArticleInfo> recentArticleInfo;
  private int numDaysInPast;
  private int numArticlesToShow;

  private List<HomePageArticleInfo> mostViewedArticleInfo;
  private String mostViewedComment;
  // the action returns from mostViewedStartIndex, and sets mostViewedNextIndex = mostViewedStartIndex + mostViewedLimit
  private int mostViewedNextIndex = 0;
  private int mostViewedStartIndex = 0;

  private int recentNextIndex = 0;
  private int recentStartIndex = 0;

  /**
   * This execute method always returns SUCCESS
   */
  @Override
  public String execute() {
    if ("PLoSONE".equals(getCurrentJournal())) {
      recentStartIndex = 0;
      executeRecent();

      if (mostViewedEnabled()) {
        mostViewedStartIndex = 0;
        executeMostViewed();
      } else {
        mostViewedArticleInfo = new ArrayList<HomePageArticleInfo>();
        mostViewedNextIndex = 0;
      }
      executeMostViewed();
    }
    else {
      initRecentArticles();
    }
    return SUCCESS;
  }

  public String executeRecent() {
    initRecentArticleInfo();
    return SUCCESS;
  }

  public String executeMostViewed() {
    initMostViewed();
    return SUCCESS;
  }

  /**
   * Get the URIs for the Article Types which can be displayed on the <i>Recent Articles</i> tab
   * on the home page.  If there is no list of acceptable Article Type URIs,
   * then an empty List is returned.
   * This method should never return null.
   * <p/>
   * As an example, this XML is used to create a List of two Strings for the PLoS One Journal:
   * <pre>
   *   &lt;PLoSONE&gt;
   *     &lt;recentArticles&gt;
   *       &lt;numDaysInPast&gt;7&lt;/numDaysInPast&gt;
   *       &lt;numArticlesToShow&gt;5&lt;/numArticlesToShow&gt;
   *       &lt;typeUriArticlesToShow&gt;
   *         &lt;articleTypeUri&gt;http://rdf.plos.org/RDF/articleType/Research%20Article&lt;/articleTypeUri&gt;
   *         &lt;articleTypeUri&gt;http://rdf.plos.org/RDF/articleType/History/Profile&lt;/articleTypeUri&gt;
   *       &lt;/typeUriArticlesToShow&gt;
   *     &lt;/recentArticles&gt;
   *   &lt;/PLoSONE&gt;
   * </pre>
   * The logic in this method was adapted from the ArticleType.configureArticleTypes(Configuration)
   * method.
   *
   * @param basePath The location (including Journal Name) of the properties which will be used to
   *   populate the returned Set.  An example is <i>ambra.virtualJournals.PLoSONE.recentArticles</i>
   * @return The URIs for the Article Types which will be displayed on the "Recent Articles" tab on
   *   the home page
   */
  private List<URI> getArticleTypesToShow(String basePath) {
    String baseString = basePath + ".typeUriArticlesToShow";
    List<URI> typeUriArticlesToShow;

    /*
     * Iterate through the defined article types.  This is ugly since the index needs to be given
     * in xpath format to access the element, so we calculate a base string like:
     *   ambra.virtualJournals.PLoSONE.recentArticles.typeUriArticlesToShow.articleTypeUri(x)
     * and check if that element is non-null.
     */
    typeUriArticlesToShow = new LinkedList<URI>();
    int count = 0;
    String articleTypeUri;
    while (true) {
      articleTypeUri = configuration.getString(baseString + ".articleTypeUri(" + count + ")");
      if (articleTypeUri != null && articleTypeUri.trim().length() > 0) {
        typeUriArticlesToShow.add(URI.create(articleTypeUri));
      } else {
        break;
      }
      count++;
    }
    return typeUriArticlesToShow;
  }

  /**
     * Populate the <b>recentArticles</b> (global) variable with random recent articles of
     * appropriate Article Type(s).
     * <ul>
     *   <li>The number of articles set into the <b>recentArticles</b>
     *     (global) variable determined by the
     *     <i>ambra.virtualJournals.CURRENT_JOURNAL_NAME.recentArticles.numArticlesToShow</i>
     *     configuration property
     *   </li>
     *   <li>The type of articles set into the <b>recentArticles</b> variable is determined by
     *     the list in the
     *   <i>ambra.virtualJournals.CURRENT_JOURNAL_NAME.recentArticles.typeUriListArticlesToShow</i>
     *     configuration property.
     *     If this property is not defined, then <b>all</b> types of articles are shown
     *   </li>
     *   <li>The initial definition of "recent" is the number of days (before today) indicated by
     *     the <i>ambra.virtualJournals.CURRENT_JOURNAL_NAME.recentArticles.numDaysInPast</i>
     *     configuration property.
     *     If not enough articles of the appropriate type are found in that span of time,
     *       then a new query is made for a somewhat longer duration.
     *   </li>
     * </ul>
     * The CURRENT_JOURNAL_NAME is acquired from the {@link BaseActionSupport#getCurrentJournal()}
     */
  private void initRecentArticles() {
      String journalKey = getCurrentJournal();
      String rootKey = "ambra.virtualJournals." + journalKey + ".recentArticles";

      List<URI> typeUriArticlesToShow = getArticleTypesToShow(rootKey);

      numDaysInPast = configuration.getInteger(rootKey + ".numDaysInPast", 7);
      numArticlesToShow = configuration.getInteger(rootKey + ".numArticlesToShow", 5);

      //  This is the most recent midnight.  No need to futz about with exact dates.
      Calendar startDate = Calendar.getInstance();
      startDate.set(Calendar.HOUR_OF_DAY, 0);
      startDate.set(Calendar.MINUTE, 0);
      startDate.set(Calendar.SECOND, 0);
      startDate.set(Calendar.MILLISECOND, 0);

      //  First query.  Just get the articles from "numDaysInPast" ago.
      Calendar endDate = (Calendar) startDate.clone();
      startDate.add(Calendar.DATE, -(numDaysInPast) + 1);

      BrowseParameters params = new BrowseParameters();
      params.setStartDate(startDate);
      params.setEndDate(endDate);
      params.setArticleTypes(typeUriArticlesToShow);
      params.setPageNum(0);
      params.setPageSize(numArticlesToShow * 100);
      params.setJournalKey(this.getCurrentJournal());

      BrowseResult results = browseService.getArticlesByDate(params);

      //Create a clone here so we're not modifying the object that is actually in the cache
      recentArticles = (ArrayList<SearchHit>)results.getArticles().clone();

      //  If not enough, then query for articles before "numDaysInPast" to make up the difference.
      if (recentArticles.size() < numArticlesToShow) {
        endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.SECOND, -1); // So no overlap with the first query.
        startDate.add(Calendar.DATE, -(numDaysInPast) - 1); // One extra day to play it safe.

        params = new BrowseParameters();
        params.setStartDate(startDate);
        params.setEndDate(endDate);
        params.setArticleTypes(typeUriArticlesToShow);
        params.setPageNum(0);
        params.setPageSize(numArticlesToShow - recentArticles.size());
        params.setJournalKey(this.getCurrentJournal());

        recentArticles.addAll(browseService.getArticlesByDate(params).getArticles());
      }

      // Now choose a random selection of numArticlesToShow articles from the article pool.
      // Even if we do not have enough articles, this will still randomize their order.
      if (recentArticles.size() > 0) {
        Random randomNumberGenerator = new Random((new Date()).getTime());  // Seed: time = "now".
        ArrayList<SearchHit> recentArticlesTemp = new ArrayList<SearchHit>();
        while (recentArticlesTemp.size() < numArticlesToShow && recentArticles.size() > 0) {
          // Remove one random article from "recentArticles" and add it to "recentArticlesTemp".
          int randomNumber = randomNumberGenerator.nextInt(recentArticles.size());
          recentArticlesTemp.add(recentArticles.get(randomNumber));
          recentArticles.remove(randomNumber);
        }
        recentArticles = recentArticlesTemp;
      }
  }

  private void initRecentArticleInfo() {
    String recentKey = "ambra.virtualJournals." + getCurrentJournal() + ".recentArticles";

    try {
      String limitKey = (recentStartIndex == 0 ? ".limitFirstPage" :  ".limit");
      int limit = configuration.getInt(recentKey + limitKey);
      List<URI> typeUriArticlesToShow = getArticleTypesToShow(recentKey);

      recentArticleInfo = mostViewedArticleService.getRecentArticleInfo(getCurrentJournal(), recentStartIndex, limit, typeUriArticlesToShow);
      recentNextIndex = recentStartIndex + recentArticleInfo.size();
    } catch (SolrException e) {
      log.error("Error querying solr for most viewed articles; returning empty list", e);
      recentArticleInfo = new LinkedList<HomePageArticleInfo>();
      recentNextIndex = recentStartIndex;
    }
  }

  private boolean mostViewedEnabled() {
    return configuration.containsKey("ambra.virtualJournals." + getCurrentJournal() + ".mostViewedArticles.limit") && mostViewedArticleService != null;
  }

  /**
   * Populate the <b>mostViewedArticleInfo</b> (global) variable with articles
   *
   */
  private void initMostViewed() {
    String mostViewedKey = "ambra.virtualJournals." + getCurrentJournal() + ".mostViewedArticles";
    if (configuration.containsKey(mostViewedKey + ".message")) {
      mostViewedComment = configuration.getString(mostViewedKey + ".message");
    }
    try {
      String limitKey = (mostViewedStartIndex == 0 ? ".limitFirstPage" :  ".limit");
      int limit = configuration.getInt(mostViewedKey + limitKey);
      Integer days;
      try {
        days = configuration.getInt(mostViewedKey + ".timeFrame");
      } catch (Exception e) {
        days = null;
      }

      mostViewedArticleInfo = mostViewedArticleService.getMostViewedArticleInfo(getCurrentJournal(), mostViewedStartIndex, limit, days);
      mostViewedNextIndex = mostViewedStartIndex + mostViewedArticleInfo.size();
    } catch (SolrException e) {
      log.error("Error querying solr for most viewed articles; returning empty list", e);
      mostViewedArticleInfo = new LinkedList<HomePageArticleInfo>();
      mostViewedNextIndex = mostViewedStartIndex;
    }

  }

  public void setMostViewedArticleService(MostViewedArticleService mostViewedArticleService) {
    this.mostViewedArticleService = mostViewedArticleService;
  }

  public List<HomePageArticleInfo> getMostViewedArticleInfo() {
    return mostViewedArticleInfo;
  }

  public String getMostViewedComment() {
    return mostViewedComment;
  }

  /**
   * Retrieves the most recently published articles in the last 7 days
   *
   * @return array of SearchHit objects
   */
  public List<SearchHit> getRecentArticles() {
    return recentArticles;
  }

  /**
   * Retrieves the most recently published articles in the last 7 days
   *
   * @return array of SearchHit objects
   */
  public List<HomePageArticleInfo> getRecentArticleInfo() {
    return recentArticleInfo;
  }

  /**
   * Returns an array of numValues ints which are randomly selected between 0 (inclusive) and
   * maxValue(exclusive). If maxValue is less than numValues, will return maxValue items. Guarantees
   * uniqueness of values.
   *
   * @param numValues Length of the array
   * @param maxValue Maximum value of each element of the array
   * @return array of random ints
   */
  public int[] randomNumbers(int numValues, int maxValue) {
    if (numValues > maxValue) {
      numValues = maxValue;
    }

    Random rng = new Random(System.currentTimeMillis());
    Set<Integer> intValues = new HashSet<Integer>();
    while (intValues.size() < numValues) {
      Integer oneNum = rng.nextInt(maxValue);
      if (!intValues.contains(oneNum)) {
        intValues.add(oneNum);
      }
    }

    Iterator<Integer> iter = intValues.iterator();
    int[] returnArray = new int[intValues.size()];
    for (int i = 0; iter.hasNext(); i++) {
      returnArray[i] = iter.next();
    }

    return returnArray;
  }

  /**
   * @param browseService The browseService to set.
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  public int getNumDaysInPast() {
    return numDaysInPast;
  }

  public int getNumArticlesToShow() {
    return numArticlesToShow;
  }

  public int getMostViewedNextIndex() {
    return mostViewedNextIndex;
  }

  public void setMostViewedNextIndex(int mostViewedNextIndex) {
    this.mostViewedNextIndex = mostViewedNextIndex;
  }

  public int getMostViewedStartIndex() {
    return mostViewedStartIndex;
  }

  public void setMostViewedStartIndex(int mostViewedStartIndex) {
    this.mostViewedStartIndex = mostViewedStartIndex;
  }

  public int getRecentNextIndex() {
    return recentNextIndex;
  }

  public void setRecentNextIndex(int recentNextIndex) {
    this.recentNextIndex = recentNextIndex;
  }

  public int getRecentStartIndex() {
    return recentStartIndex;
  }

  public void setRecentStartIndex(int recentStartIndex) {
    this.recentStartIndex = recentStartIndex;
  }
}
