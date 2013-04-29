/* $HeadURL::                                                                            $
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
package org.ambraproject.action.user;

import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserAlert;
import org.ambraproject.views.SavedSearchView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Remove this class. Only admin uses it now. Admin should be refactored to directly extend {@link UserActionSupport}
 */
@Deprecated
public abstract class UserAlertsAction extends UserActionSupport {
  private static final Logger log = LoggerFactory.getLogger(UserAlertsAction.class);

  private String displayName;
  protected List<String> monthlyAlerts = new ArrayList<String>();
  protected List<String> weeklyAlerts = new ArrayList<String>();
  protected List<String> deleteAlerts = new ArrayList<String>();
  private List<SavedSearchView> savedSearches;

  /**
   * Subclasses must override this to provide the id of the user being edited
   *
   * @return the id of the user to edit
   */
  protected abstract String getUserAuthId();

  public Collection<UserAlert> getUserAlerts() {
    return userService.getAvailableAlerts();
  }

  /**
   * Save the alerts.
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(rollbackFor = {Throwable.class})
  public String saveAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    userService.setAlerts(authId, monthlyAlerts, weeklyAlerts);
    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String retrieveAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }

    final UserProfile user = userService.getUserByAuthId(authId);

    monthlyAlerts = user.getMonthlyAlerts();
    weeklyAlerts = user.getWeeklyAlerts();
    displayName = user.getDisplayName();

    return SUCCESS;
  }

  public Collection<SavedSearchView> getUserSearchAlerts() throws Exception{
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    final UserProfile user = userService.getUserByAuthId(authId);
    return userService.getSavedSearches(user.getID());
  }

  /**
   * save the user search alerts
   * @return webwork status
   * @throws Exception
   */
  @Transactional(rollbackFor = {Throwable.class})
  public String saveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    userService.setSavedSearchAlerts(authId, monthlyAlerts, weeklyAlerts, deleteAlerts);
    return SUCCESS;
  }

  @Transactional(readOnly = true)
  public String retrieveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    final UserProfile user = userService.getUserByAuthId(authId);
    savedSearches = userService.getSavedSearches(user.getID());

    return SUCCESS;
  }

  /**
   * @return categories that have monthly alerts
   */
  public List<String> getMonthlyAlerts() {
    return monthlyAlerts;
  }

  /**
   * Set the categories that have monthly alerts
   *
   * @param monthlyAlerts monthlyAlerts
   */
  public void setMonthlyAlerts(final String[] monthlyAlerts) {
    this.monthlyAlerts = new LinkedList<String>(edu.emory.mathcs.backport.java.util.Arrays.asList(monthlyAlerts));
  }

  /**
   * @return weekly alert categories
   */
  public List<String> getWeeklyAlerts() {
    return weeklyAlerts;
  }

  /**
   * Set weekly alert categories
   *
   * @param weeklyAlerts weeklyAlerts
   */
  public void setWeeklyAlerts(String[] weeklyAlerts) {
    this.weeklyAlerts = new LinkedList<String>(edu.emory.mathcs.backport.java.util.Arrays.asList(weeklyAlerts));
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<String> getDeleteAlerts() {
    return deleteAlerts;
  }

  public void setDeleteAlerts(String[] deleteAlerts) {
    this.deleteAlerts = new LinkedList<String>(edu.emory.mathcs.backport.java.util.Arrays.asList(deleteAlerts));
  }
}
