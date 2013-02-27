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

package org.ambraproject.models;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 *  Unit Test Class to test the savedsearch.
 */
public class SavedSearchTest extends BaseHibernateTest {

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testSavedSearchWithNullUserProfileId() {
    hibernateTemplate.save(new SavedSearch());
  }

  @Test
  public void testSavedSearch() {
    long testStart = new Date().getTime();
    final UserProfile user = new UserProfile("savedSearchAuthId", "savedSearchEmail", "savedSearchDisplayName");

    SavedSearchQuery query = new SavedSearchQuery("search string", "search hash");
    hibernateTemplate.save(query);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setSearchName("search name");
    savedSearch.setSearchQuery(query);
    savedSearch.setLastWeeklySearchTime(new Date());
    savedSearch.setLastMonthlySearchTime(new Date());
    savedSearch.setMonthly(false);
    savedSearch.setWeekly(true);

    user.setSavedSearches(Arrays.asList(savedSearch));
    hibernateTemplate.save(user);

    SavedSearch storedSearch = hibernateTemplate.get(SavedSearch.class, savedSearch.getID());
    assertEquals(storedSearch.getSearchQuery(), savedSearch.getSearchQuery());
    assertTrue(storedSearch.getLastModified().getTime() >= testStart,
        "savedsearch didn't get last submit timestamp set correctly");
    assertEquals(storedSearch.getMonthly(), false);
    assertEquals(storedSearch.getWeekly(), true);
    assertEquals(storedSearch.getSearchName(), "search name");
    assertEquals(storedSearch.getSearchQuery().getSearchParams(), "search string");
    assertEquals(storedSearch.getSearchQuery().getHash(), "search hash");

    Calendar lastSearchTime = Calendar.getInstance();
    lastSearchTime.add(Calendar.YEAR, 1);
    savedSearch.setLastWeeklySearchTime(lastSearchTime.getTime());
    savedSearch.setLastMonthlySearchTime(lastSearchTime.getTime());
    hibernateTemplate.update(user);

    storedSearch = hibernateTemplate.get(SavedSearch.class, savedSearch.getID());
    assertTrue(Calendar.getInstance().getTime().getTime() < storedSearch.getLastWeeklySearchTime().getTime(),
        "didn't update last search time");
    assertTrue(Calendar.getInstance().getTime().getTime() < storedSearch.getLastMonthlySearchTime().getTime(),
        "didn't update last search time");

    List<SavedSearch> storedSavedSearchList = hibernateTemplate.execute(new HibernateCallback<List<SavedSearch>>() {
      @Override
      public List<SavedSearch> doInHibernate(Session session) throws HibernateException, SQLException {
        //load up all the saved searches(they're lazy)
        List<SavedSearch> savedSearches = ((UserProfile) session.get(UserProfile.class, user.getID())).getSavedSearches();
        for (Iterator<SavedSearch> i = savedSearches.iterator(); i.hasNext(); ) {
          i.next();
        }
        return savedSearches;
      }
    });
    assertEquals(storedSavedSearchList, Arrays.asList(storedSearch), "user didn't have correct saved search list");
  }

}
