/*
 * Copyright (c) 2006-2013 by Public Library of Science
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

package org.ambraproject.service.search;

import org.ambraproject.ApplicationException;
import org.ambraproject.views.SearchHit;
import org.ambraproject.views.SearchResultSinglePage;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;

/**
 * Search service interface.
 *
 * @author Dragisa Krsmanovic
 */
public interface SearchService {

  SearchResultSinglePage simpleSearch(SearchParameters searchParameters) throws ApplicationException;

  SearchResultSinglePage advancedSearch(SearchParameters searchParameters) throws ApplicationException;

  SearchResultSinglePage findAnArticleSearch(SearchParameters searchParameters) throws ApplicationException;

  SearchResultSinglePage getFilterData(SearchParameters searchParameters) throws ApplicationException;

  /**
   * Returns a list of all subject categories associated with all papers ever published
   * for the given journal.
   *
   * @param journal name of the journal in question
   * @return List of category strings, slash-delimited to indicate hierarchy.  Example:
   *     "/Biology and life sciences/Plant science/Plant anatomy/Flowers"
   */
  List<String> getAllSubjects(String journal) throws ApplicationException;

  /**
   * Returns a list of the top level subject categories associated with all journals
   *
   * @return A map of subject areas and a count of their frequency
   * @throws ApplicationException
   */
  SortedMap<String, Long> getTopSubjects() throws ApplicationException;

  /**
   * Returns articles list that are published between the last search time and the current search time for saved search
   * alerts.
   *
   * @param sParams The search params
   * @param lastSearchTime the begin time of the search
   * @param currentSearchTime the end time of the search
   * @param resultLimit the maximum number of records to return
   *
   * @return a list of search results
   *
   * @throws ApplicationException
   */
  List<SearchHit> savedSearchAlerts(SearchParameters sParams, Date lastSearchTime, Date currentSearchTime, int resultLimit) throws ApplicationException;

  /**
   * The map of sorts that are valid for this provider
   * @return
   */
  List getSorts();

  /**
   * The valid page sizes for this provider
   * @return
   */
  List getPageSizes();

  /**
   * Retrieve an article abstract from the search engine.
   *
   * @param articleDoi
   * @return the article abstract, with HTML if possible, or an empty (non-null) string if the article has none
   * @throws ApplicationException if article abstracts (including empty values) are missing or invalid
   */
  String fetchAbstractText(String articleDoi) throws ApplicationException;

}
