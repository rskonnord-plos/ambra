/*
 * $HeadURL$
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
package org.ambraproject.action.search;

import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserService;
import org.ambraproject.views.SavedSearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Save a user's search
 *
 * @author Joe Osowski
 */
public class SaveSearchAction extends BaseSearchAction {
  private static final Logger log = LoggerFactory.getLogger(SaveSearchAction.class);
  private UserService userService;
  private String searchName;
  private boolean monthly;
  private boolean weekly;

  /**
   * Save the user's search
   * @return
   */
  @Transactional(rollbackFor = { Throwable.class })
  @SuppressWarnings("unchecked")
  public String saveSearch() {
    final UserProfile user = getCurrentUser();

    if (user == null) {
      log.info("User is null for saving search");
      addActionError("You must be logged in");

      return ERROR;
    }

    //Make sure the user is authenticated, is this redundant?
    this.permissionsService.checkLogin(user.getAuthId());

    if (searchName == null || searchName.length() == 0) {
      addActionError("Search name must be specified.");
      return INPUT;
    }

    List<SavedSearchView> savedSearchViewList = userService.getSavedSearches(this.getCurrentUser().getID());

    for(SavedSearchView savedSearchView : savedSearchViewList) {
      if(savedSearchView.getSearchName().equals(this.searchName)) {
        addActionError("Search name must be unique.");
        return INPUT;
      }
    }

    userService.saveSearch(user.getID(), getSearchParameters(), searchName,
      this.weekly, this.monthly);

    return SUCCESS;
  }

  /**
   * Setter method for struts
   *
   * @param searchName
   */
  public void setSearchName(String searchName) {
    this.searchName = searchName;
  }

  /**
   * Setter method for struts
   *
   * @param monthly
   */
  public void setMonthly(boolean monthly) {
    this.monthly = monthly;
  }

  /**
   * Setter method for struts
   *
   * @param weekly
   */
  public void setWeekly(boolean weekly) {
    this.weekly = weekly;
  }

  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}