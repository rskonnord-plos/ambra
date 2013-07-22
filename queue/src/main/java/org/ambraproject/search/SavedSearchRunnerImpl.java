/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.search;

import org.ambraproject.ApplicationException;
import org.ambraproject.service.search.SolrSearchService;
import org.ambraproject.views.SavedSearchHit;
import org.ambraproject.views.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @inheritDoc
 */
public class SavedSearchRunnerImpl implements SavedSearchRunner {

  private SolrSearchService searchService;
  private int resultLimit;

  private static final Logger log = LoggerFactory.getLogger(SavedSearchRunnerImpl.class);

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("unchecked")
  public SavedSearchJob runSavedSearch(SavedSearchJob searchJob) throws ApplicationException {
    log.debug("Received thread Name: {}", Thread.currentThread().getName());
    log.debug("Running Saved Search for the search query ID : {}, {}" ,
      searchJob.getSavedSearchQueryID(), searchJob.getFrequency());

    if(searchJob.getStartDate() == null) {
      if(searchJob.getFrequency().equals("WEEKLY")) {
        //7 days into the past
        Calendar date = Calendar.getInstance();

        //We really should just start only using Calendar objects
        //But until that day... remove all time parts to avoid UTC / PST problems
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        date.add(Calendar.DAY_OF_MONTH, -7);

        searchJob.setStartDate(date.getTime());
      } else {
        //30 days into the past
        Calendar date = Calendar.getInstance();

        //We really should just start only using Calendar objects
        //But until that day... remove all time parts to avoid UTC / PST problems
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        date.add(Calendar.MONTH, -1);

        searchJob.setStartDate(date.getTime());
      }
    }

    if(searchJob.getEndDate() == null) {
      searchJob.setEndDate(Calendar.getInstance().getTime());
    }

    List<SearchHit> results = searchService.savedSearchAlerts(searchJob.getSearchParams(),
      searchJob.getStartDate(), searchJob.getEndDate(), resultLimit);
    List<SavedSearchHit> finalHitList = new ArrayList<SavedSearchHit>();

    log.debug("Search hits : {}", results.size());

    if(results.size() > 0) {
      for(SearchHit hit :results){
        finalHitList.add(SavedSearchHit.builder()
          .setUri(hit.getUri())
          .setTitle(hit.getTitle())
          .setCreator(hit.getCreator())
          .setSubjects(hit.getSubjects())
          .setSubjectsPolyhierarchy(hit.getSubjectsPolyhierarchy())
          .build());
      }
    }

    searchJob.setSearchHitList(finalHitList);

    return searchJob;
  }

  @Required
  public void setSearchService(SolrSearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setResultLimit(int resultLimit) {
    this.resultLimit = resultLimit;
  }
}
