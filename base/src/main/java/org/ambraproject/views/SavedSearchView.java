/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.views;

import com.google.gson.Gson;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.service.search.SearchParameters;

import java.util.Date;
import java.util.List;

/**
 * A view class for saved searches
 *
 * @author Joe Osowski
 */
public class SavedSearchView {
  private Long savedSearchId;
  private SearchParameters searchParameters;
  private String searchName;
  private boolean weekly;
  private boolean monthly;
  private Date lastWeeklySearchTime;
  private Date lastMonthlySearchTime;

  public SavedSearchView(SavedSearch savedSearch) {
    this.savedSearchId = savedSearch.getID();
    this.searchName = savedSearch.getSearchName();
    this.weekly = savedSearch.getWeekly();
    this.monthly = savedSearch.getMonthly();
    this.lastMonthlySearchTime = savedSearch.getLastMonthlySearchTime();
    this.lastWeeklySearchTime = savedSearch.getLastWeeklySearchTime();

    Gson gson = new Gson();
    this.searchParameters = gson.fromJson(savedSearch.getSearchQuery().getSearchParams(), SearchParameters.class);
  }

  public SavedSearchView(Long savedSearchId, String searchName, String searchParams) {
    this.savedSearchId = savedSearchId;
    this.searchName = searchName;

    Gson gson = new Gson();
    this.searchParameters = gson.fromJson(searchParams, SearchParameters.class);
  }
  /**
   * get the savedSearchId
   * @return savedSearchId
   */
  public Long getSavedSearchId() {
    return savedSearchId;
  }

  /**
   * Return a de-serialized version of the search parameters
   *
   * @return the SearchParameters object
   */
  public SearchParameters getSearchParameters() {
    return searchParameters;
  }

  /**
   * Get the stored search name
   *
   * @return searchName
   */
  public String getSearchName() {
    return searchName;
  }

  /**
   * Is this search meant to run weekly?
   *
   * @return boolean
   */
  public boolean getWeekly() {
    return weekly;
  }

  /**
   * Is this search meant to run monthly?
   *
   * @return monthly
   */
  public boolean getMonthly() {
    return monthly;
  }

  public Date getLastWeeklySearchTime() {
    return lastWeeklySearchTime;
  }

  public Date getLastMonthlySearchTime() {
    return lastMonthlySearchTime;
  }
}
