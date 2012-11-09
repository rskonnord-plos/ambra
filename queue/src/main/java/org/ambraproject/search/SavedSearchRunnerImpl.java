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

package org.ambraproject.search;

import org.ambraproject.ApplicationException;
import org.ambraproject.service.search.SolrSearchService;
import org.ambraproject.views.SavedSearchHit;
import org.ambraproject.views.SavedSearchView;
import org.ambraproject.views.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: stumu Date: 9/26/12 Time: 2:00 PM To change this template use File | Settings |
 * File Templates.
 */
public class SavedSearchRunnerImpl implements SavedSearchRunner {

  private SolrSearchService searchService;
  private static final Logger log = LoggerFactory.getLogger(SavedSearchRunnerImpl.class);

  @Override
  public SavedSearchView runSavedSearch(SavedSearchView savedSearchView) throws ApplicationException {

     List<SearchHit> results = null;
     Date currentTime = Calendar.getInstance().getTime();
     log.debug("Running Saved Search for the search name :  {}" , savedSearchView.getSearchName());
     results = searchService.savedSearchAlerts(savedSearchView.getSearchParameters(), savedSearchView.getLastSearchTime(), currentTime);

     SavedSearchHit finalHit;
     List<SavedSearchHit> finalHitList = new ArrayList<SavedSearchHit>();
     if(results.size()>0){
      for(SearchHit hit :results){
        finalHit = new SavedSearchHit(hit.getUri(), hit.getTitle(),hit.getCreator());
        finalHitList.add(finalHit);
      }
     }

     savedSearchView.setSearchHitList(finalHitList.size()==0?null:finalHitList);
     savedSearchView.setCurrentTime(currentTime);

     return savedSearchView;
  }

  public void setSearchService(SolrSearchService searchService) {
    this.searchService = searchService;
  }

}
