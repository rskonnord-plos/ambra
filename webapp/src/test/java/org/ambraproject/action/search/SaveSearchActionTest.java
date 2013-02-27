/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
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

package org.ambraproject.action.search;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import org.ambraproject.Constants;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchParams;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.testng.Assert.assertEquals;

/**
 * Test the SaveSearch Action
 *
 * @author Joe Osowski
 */
public class SaveSearchActionTest extends AmbraWebTest {

  @Autowired
  protected SaveSearchAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  private void assertNoSearchName() {
    UserProfile user = new UserProfile();
    user.setEmail("noSearchemail@testAddAlerts.org");
    user.setAuthId("authIdForTestNoSearch");
    user.setDisplayName("displayNameForTestNoSearch");

    dummyDataStore.store(user);

    ActionContext.getContext().getSession().put(Constants.AUTH_KEY, user.getAuthId());
    ActionContext.getContext().getSession().put(Constants.AMBRA_USER_KEY, user);

    action.setQuery("Test");
    action.setSearchName(null);

    String result = action.saveSearch();

    assertEquals(result, Action.INPUT, "Didn't halt with invalid INPUT");

    final Collection<String> actionErrors = action.getActionErrors();
    assertEquals(actionErrors.size(), 1, "Expected exactly one action error from invalid input");
  }


  private void assertDuplicateSearchName() {
    UserProfile user = new UserProfile();
    user.setEmail("email@testAddAlerts.org");
    user.setAuthId("authIdForTestSearchName");
    user.setDisplayName("displayNameForTestSearchName");

    SavedSearchParams params = new SavedSearchParams("search string", "hash");
    dummyDataStore.store(params);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setSearchName("TestWithSearchName");
    savedSearch.setSearchParams(params);
    savedSearch.setLastWeeklySearchTime(new Date());
    savedSearch.setLastMonthlySearchTime(new Date());
    savedSearch.setMonthly(false);
    savedSearch.setWeekly(true);

    user.setSavedSearches(Arrays.asList(savedSearch));

    dummyDataStore.store(user);

    ActionContext.getContext().getSession().put(Constants.AUTH_KEY, user.getAuthId());
    ActionContext.getContext().getSession().put(Constants.AMBRA_USER_KEY, user);

    action.setQuery("TestSearch");
    action.setSearchName("TestWithSearchName");


    String result = action.saveSearch();

    //assertEquals(result, Action.SUCCESS, "Search did not save");
    //assertEquals(result, Action.INPUT, "Search List is empty");

   // result = action.saveSearch();

    assertEquals(result, Action.INPUT, "Search did not report error");

    final Collection<String> actionErrors = action.getActionErrors();
    assertEquals(actionErrors.size(), 1, "Expected exactly one action error from invalid input");
  }

  @Test
  private void assertNoLogin() {
    String result = action.saveSearch();

    assertEquals(result, Action.ERROR, "Didn't halt with ERROR");
  }
}
