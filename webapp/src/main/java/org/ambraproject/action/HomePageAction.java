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

import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.ArticleServiceSearchParameters;
import org.ambraproject.service.article.BrowseParameters;
import org.ambraproject.service.article.BrowseService;
import org.ambraproject.views.BrowseResult;
import org.ambraproject.views.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author stevec
 */
@SuppressWarnings("serial")
public class HomePageAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(HomePageAction.class);

  private ArticleService articleService;
  private BrowseService browseService;
  private SortedMap<String, Long> categoryInfos;

  private ArrayList<SearchHit> recentArticles;
  private int numDaysInPast;
  private int numArticlesToShow;

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

      //end date is most recent midnight
      //milliseconds in a day: 86400000
      Date endDate = new Date();
      endDate.setTime(endDate.getTime() - endDate.getTime() % 86400000L);
      Date startDate = new Date();
      startDate.setTime(endDate.getTime() - Long.valueOf(numDaysInPast) * 86400000L);

      //  First query.  Just get the articles from "numDaysInPast" ago.
      recentArticles = (ArrayList<SearchHit>) articleService.getArticleURIsTitlesByDate(startDate, endDate);

      // If not enough, then query for articles before ${numDaysInPast} to make up the difference.
      if (recentArticles.size() < numArticlesToShow) {
        int dayCount = 0;

        while (recentArticles.size() < numArticlesToShow && dayCount < 30) {

          endDate = (Date) startDate.clone();
          endDate.setTime(endDate.getTime() - 1000L); //avoid overlap here
          //3 days = 86400000L * 3
          startDate.setTime(startDate.getTime() - 3 * 86400000L);
          recentArticles.addAll(articleService.getArticleURIsTitlesByDate(startDate, endDate));
          dayCount += 3;

        }

      }

      //pare down the actual number of recent articles to match ${numberArticlesToShow}
      int howManyIsTooMany = recentArticles.size() - numArticlesToShow;
      Random randy = new Random((new Date()).getTime());  // Seed: time = "now".

      while (howManyIsTooMany >= 0) {
        // Remove one random article from "recentArticles" and add it to "recentArticlesTemp".
        recentArticles.remove(randy.nextInt(recentArticles.size()));
        howManyIsTooMany--;
      }

      //shuffle the recent articles
      Collections.shuffle(recentArticles);
    }

  /**
   * This execute method always returns SUCCESS
   */
  @Override
  public String execute() {
    String journal = getCurrentJournal();

    // HACK: the PLOS ONE homepage displays all top-level categories.  With the
    // old taxonomy, we got these results from solr.  However, the new taxonomy
    // has many fewer top-level categories, so it's a lot simpler just to
    // hard code them here.  Still, this should probably be moved somewhere else
    // and/or be done more elegantly.
    if ("PLoSONE".equals(journal)) {
      Map<String, Long> topLevelCategories = new HashMap<String, Long>(10);
      topLevelCategories.put("Physical sciences", 1L);
      topLevelCategories.put("Earth sciences", 1L);
      topLevelCategories.put("Computer and information sciences", 1L);
      topLevelCategories.put("Environmental sciences and ecology", 1L);
      topLevelCategories.put("Social sciences", 1L);
      topLevelCategories.put("Science policy", 1L);
      topLevelCategories.put("Research and analysis methods", 1L);
      topLevelCategories.put("Medicine and health sciences", 1L);
      topLevelCategories.put("Engineering and technology", 1L);
      topLevelCategories.put("Biology and life sciences", 1L);
      categoryInfos = new TreeMap<String, Long>(topLevelCategories);
    } else {
      categoryInfos = browseService.getSubjectsForJournal(journal);
    }
    initRecentArticles();
    return SUCCESS;
  }

  /**
   * @return Returns category and number of articles for each category.
   * Categories are sorted by name.
   */
  public SortedMap<String,Long> getCategoryInfos() {
    return categoryInfos;
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
   * @param articleService the ArticleService to set
   */
  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
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

}
