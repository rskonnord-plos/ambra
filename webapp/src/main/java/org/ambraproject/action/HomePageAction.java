/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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
import org.ambraproject.service.journal.JournalService;
import org.ambraproject.ApplicationException;
import org.ambraproject.service.search.SearchService;
import org.ambraproject.views.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.net.URI;
import java.util.List;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author stevec
 */
@SuppressWarnings("serial")
public class HomePageAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(HomePageAction.class);

  private ArticleService articleService;
  private JournalService journalService;
  private SearchService searchService;
  private SortedMap<String, Long> categoryInfos;

  private List<SearchHit> recentArticles;
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
      String journal_eIssn = journalService.getJournal(journalKey).geteIssn();
      String rootKey = "ambra.virtualJournals." + journalKey + ".recentArticles";
      List<URI> typeUriArticlesToShow = getArticleTypesToShow(rootKey);

      numDaysInPast = configuration.getInteger(rootKey + ".numDaysInPast", 7);
      numArticlesToShow = configuration.getInteger(rootKey + ".numArticlesToShow", 5);
      recentArticles = articleService.getRandomRecentArticles(journal_eIssn, typeUriArticlesToShow, numDaysInPast, numArticlesToShow);
    }

  /**
   * This execute method always returns SUCCESS
   */
  @Override
  public String execute() {
    try {
      categoryInfos = searchService.getTopSubjects();
    } catch(ApplicationException ex) {
      log.error("Failed to query search service", ex);
      categoryInfos = new TreeMap<String, Long>();
    }

    initRecentArticles();

    return SUCCESS;
  }

  /**
   * @return Returns category and number of articles for each category.
   *
   * Categories are listed for all journals and sorted by name
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
   * @param articleService the ArticleService to set
   */
  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  /*
  * @param journalService the JournalService to use
  */
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param searchService The searchService to set.
   */
  @Required
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public int getNumArticlesToShow() {
    return numArticlesToShow;
  }

}
