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

package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchParams;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.SavedSearchRetriever;
import org.ambraproject.util.TextUtils;
import org.ambraproject.views.SavedSearchView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Created with IntelliJ IDEA. User: stumu Date: 10/3/12 Time: 10:32 AM To change this template use File | Settings |
 * File Templates.
 */
@ContextConfiguration
public class SavedSearchRetrieverTest extends BaseTest {

  @Autowired
  protected SavedSearchRetriever savedSearchRetriever;


  @Test
  public void testSavedSearch() {

    Calendar searchTime = Calendar.getInstance();
    searchTime.set(Calendar.YEAR, 2008);
    searchTime.set(Calendar.MONTH, 5);
    searchTime.set(Calendar.DAY_OF_MONTH, 15);

    for (int i = 1; i <= 3; i++) {
      UserProfile user = new UserProfile("savedSearch1-" + i, i + "savedSearch11@example.org", "savedSearch1" + i);

      String query = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      SavedSearchParams params = new SavedSearchParams(query, TextUtils.createHash(query));
      SavedSearch savedSearch1 = new SavedSearch("weekly-" + i, params);
      savedSearch1.setWeekly(true);
      savedSearch1.setMonthly(false);
      savedSearch1.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch1.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchParams(query, TextUtils.createHash(query));
      SavedSearch savedSearch2 = new SavedSearch("monthly-" + i, params);
      savedSearch2.setWeekly(false);
      savedSearch2.setMonthly(true);
      savedSearch2.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch2.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchParams(query, TextUtils.createHash(query));
      SavedSearch savedSearch3 = new SavedSearch("both-" + i, params);
      savedSearch3.setMonthly(true);
      savedSearch3.setWeekly(true);
      savedSearch3.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch3.setLastMonthlySearchTime(searchTime.getTime());

      query = "{\"query\":\"\",\"unformattedQuery\":\"everything:debug\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":\"\",\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
      params = new SavedSearchParams(query, TextUtils.createHash(query));
      SavedSearch savedSearch4 = new SavedSearch("both-" + i, params);
      savedSearch4.setMonthly(true);
      savedSearch4.setWeekly(true);
      savedSearch4.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch4.setLastMonthlySearchTime(searchTime.getTime());

      user.setSavedSearches(Arrays.asList(savedSearch1, savedSearch2, savedSearch3,savedSearch4));
      dummyDataStore.store(user);
    }



    List<SavedSearchView> savedSearchViews = savedSearchRetriever.retrieveSearchAlerts(SavedSearchRetriever.AlertType.WEEKLY);

    assertNotNull(savedSearchViews, "saved search views List is empty for weekly search");
    assertEquals(savedSearchViews.size(), 18, "returned incorrect number of results");


    savedSearchViews = savedSearchRetriever.retrieveSearchAlerts(SavedSearchRetriever.AlertType.MONTHLY);

    assertNotNull(savedSearchViews, "saved search views List is empty for monthly search");
    assertEquals(savedSearchViews.size(), 18, "returned incorrect number of results");

  }


}
