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

package org.ambraproject.queue;

import org.ambraproject.action.BaseTest;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.SavedSearchJob;
import org.ambraproject.search.SavedSearchRetriever;
import org.ambraproject.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for the SavedSearchRetriever class
 *
 * @author stumu
 * @author Joe Osowski
 */
@ContextConfiguration
public class SavedSearchRetrieverTest extends BaseTest {

  @Autowired
  protected SavedSearchRetriever savedSearchRetriever;

  @Test
  public void testSavedSearch() {
    cleanup();

    Calendar searchTime = Calendar.getInstance();
    searchTime.set(Calendar.YEAR, 2008);
    searchTime.set(Calendar.MONTH, 5);
    searchTime.set(Calendar.DAY_OF_MONTH, 15);

    String query1 = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query2 = "{\"query\":\"\",\"unformattedQuery\":\"everything:testing\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSOne\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";
    String query3 = "{\"query\":\"\",\"unformattedQuery\":\"everything:debug\",\"volume\":\"\",\"eLocationId\":\"\",\"id\":\"\",\"filterSubjects\":[],\"filterKeyword\":\"\",\"filterArticleType\":[],\"filterJournals\":[\"PLoSMedicine\"],\"sort\":\"Relevance\",\"startPage\":0,\"pageSize\":10}";

    SavedSearchQuery ssq1 = new SavedSearchQuery(query1, TextUtils.createHash(query1));
    dummyDataStore.store(ssq1);

    SavedSearchQuery ssq2 = new SavedSearchQuery(query2, TextUtils.createHash(query2));
    dummyDataStore.store(ssq2);

    SavedSearchQuery ssq3 = new SavedSearchQuery(query3, TextUtils.createHash(query3));
    dummyDataStore.store(ssq3);

    for (int i = 0; i < 3; i++) {
      UserProfile user = new UserProfile("savedSearch1-" + i, i + "savedSearch11@example.org", "savedSearch1" + i);

      SavedSearch savedSearch1 = new SavedSearch("weekly-" + i, ssq1);
      savedSearch1.setWeekly(true);
      savedSearch1.setMonthly(false);
      savedSearch1.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch1.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch1.setLastMonthlySearchTime(searchTime.getTime());

      SavedSearch savedSearch2 = new SavedSearch("monthly-" + i, ssq2);
      savedSearch2.setWeekly(false);
      savedSearch2.setMonthly(true);
      savedSearch2.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch2.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch2.setLastMonthlySearchTime(searchTime.getTime());

      //Use same query another alert
      //Kind of silly to have the same alert defined twice, but
      //just checking the use case
      SavedSearch savedSearch3 = new SavedSearch("both-" + i, ssq2);
      savedSearch3.setWeekly(true);
      savedSearch3.setMonthly(true);
      savedSearch3.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch3.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch3.setLastMonthlySearchTime(searchTime.getTime());

      SavedSearch savedSearch4 = new SavedSearch("both-" + i, ssq3);
      savedSearch4.setWeekly(true);
      savedSearch4.setMonthly(false);
      savedSearch4.setSearchType(SavedSearchType.USER_DEFINED);
      savedSearch4.setLastWeeklySearchTime(searchTime.getTime());
      savedSearch4.setLastMonthlySearchTime(searchTime.getTime());

      user.setSavedSearches(Arrays.asList(savedSearch1, savedSearch2, savedSearch3, savedSearch4));

      dummyDataStore.store(user);
    }

    //Confirm unique jobs,
    //Though there are many profiles and saved searches there are only a
    //few distinct search queries, confirm this list is only three elements
    List<SavedSearchJob> savedSearchJobs = savedSearchRetriever.retrieveSearchAlerts(SavedSearchRetriever.AlertType.WEEKLY,
      null, null);

    assertNotNull(savedSearchJobs, "saved search views List is empty for weekly search");
    assertEquals(savedSearchJobs.size(), 3, "returned incorrect number of results");

    savedSearchJobs = savedSearchRetriever.retrieveSearchAlerts(SavedSearchRetriever.AlertType.MONTHLY,
      null, null);

    assertNotNull(savedSearchJobs, "saved search views List is empty for monthly search");
    assertEquals(savedSearchJobs.size(), 1, "returned incorrect number of results");

    Date then = new Date(1000);
    Date now = new Date();

    savedSearchJobs = savedSearchRetriever.retrieveSearchAlerts(SavedSearchRetriever.AlertType.MONTHLY,
      then, now);

    assertNotNull(savedSearchJobs, "saved search views List is empty for monthly search");
    assertEquals(savedSearchJobs.size(), 1, "returned incorrect number of results");

    assertEquals(savedSearchJobs.get(0).getStartDate(), then, "Start date specified, but not correct.");
    assertEquals(savedSearchJobs.get(0).getEndDate(), now, "End date specified, but not correct.");
  }

  @AfterClass
  public void cleanup() {
    logger.debug("Cleaning up data");

    dummyDataStore.deleteAll(SavedSearch.class);
    dummyDataStore.deleteAll(UserProfile.class);
    dummyDataStore.deleteAll(SavedSearchQuery.class);

    restoreDefaultUsers();
  }
}
