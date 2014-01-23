/*
 * Copyright (c) 2006-2010 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.action.user;

import org.ambraproject.Constants;
import org.ambraproject.models.Journal;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserOrcid;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.orcid.OrcidService;
import org.ambraproject.service.user.UserAlert;
import org.ambraproject.views.SavedSearchView;
import org.apache.http.HttpHeaders;
import org.apache.struts2.interceptor.ServletRequestAware;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Joe Osowski 04/12/2013
 * @author Alex Kudlick 10/23/12
 */
public class EditUserAction extends UserActionSupport implements ServletRequestAware {
  private List<SavedSearchView> savedSearches;
  protected OrcidService orcidService;

  protected String orcid;
  private String clientID;
  private String scope;
  private String orcidAuth;
  private String redirectURL;
  private HttpServletRequest request;

  //tabID maps to tabs defined in user.js
  private String tabID = "profile";

  @Override
  public String execute() throws Exception {
    String authId = getUserAuthId();

    UserProfile userProfile = userService.getUserByAuthId(authId);
    setFieldsFromProfile(userProfile);

    monthlyAlerts = userProfile.getMonthlyAlerts();
    weeklyAlerts = userProfile.getWeeklyAlerts();

    showDisplayName = false;

    List<SavedSearchView> searches = userService.getSavedSearches(userProfile.getID());

    savedSearches = new ArrayList<SavedSearchView>();
    journalSubjectFilters = new HashMap<String, String[]>();

    for(SavedSearchView search : searches) {
      if(search.getSearchType() == SavedSearchType.USER_DEFINED) {
        savedSearches.add(search);
      } else {
        //Only two types are supported the above and "SavedSearchType.JOURNAL_ALERT"
        //The second should be grouped by journal for the view
        String[] journals = search.getSearchParameters().getFilterJournals();

        if(journals == null || journals.length != 1) {
          throw new RuntimeException("Journal not specified for filter or specified multiple times.");
        }

        String curJournal = journals[0];
        String[] subjectFilters = search.getSearchParameters().getFilterSubjectsDisjunction();

        //Assume one search alert per journal
        //Kludge: Deparse the journal.  This will only work for journals where the only difference between the values
        //are case changes
        journalSubjectFilters.put(curJournal.toLowerCase(), subjectFilters);

        //Append the weekly alert for the view
        //Kludge: Deparse the journal.  This will only work for journals where the only difference between the values
        //are case changes
        weeklyAlerts.add(curJournal.toLowerCase());
      }
    }

    UserOrcid userOrcid = userService.getUserOrcid(userProfile.getID());

    if(userOrcid != null) {
      orcid = userOrcid.getOrcid();
    }

    clientID = orcidService.getClientID();
    scope = orcidService.getScope();
    orcidAuth = configuration.getString(OrcidService.ORCID_AUTHORIZATION_URL);
    //TODO: Better way to handle this?  Config?
    redirectURL = "http://" + request.getHeader(HttpHeaders.HOST) + "/user/secure/profile/orcid/confirm";

    return SUCCESS;
  }

  public String retrieveAlerts() throws Exception {

    tabID = "journalAlerts";

    return execute();
  }

  public String retrieveSearchAlerts() throws Exception {

    tabID = "savedSearchAlerts";

    return execute();
  }

  public String saveUser() throws Exception {
    boolean valid = validateProfileInput();

    if (!valid) {
      //Fill in values for the front end
      execute();

      return INPUT;
    }

    UserProfile profile = createProfileFromFields();

    //Make sure to set the auth id so the user service can see if this account already exists
    String userAuthId = getUserAuthId();
    profile.setAuthId(userAuthId);

    UserProfile savedProfile = userService.updateProfile(profile);

    session.put(Constants.AMBRA_USER_KEY, savedProfile);

    return execute();
  }

  /**
   * Save the alerts.
   *
   * @return webwork status
   * @throws Exception Exception
   */
  public String saveAlerts() throws Exception {
    final String authId = getUserAuthId();

    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }

    if(validateAlertInput()) {
      UserProfile profile = userService.getUserByAuthId(authId);

      if(this.filterSpecified != null) {
        for(String journal : filterSpecified.keySet()) {
          String journalKey = translateJournalKey(journal);

          //If the journal is not checked for weekly, just remove the alert
          boolean weeklyChecked = false;
          for(String s : weeklyAlerts) {
            if(journal.equals(s)) {
              weeklyChecked = true;
            }
          }

          if(!weeklyChecked) {
            userService.removedFilteredWeeklySearchAlert(profile.getID(), journalKey);
            break;
          }

          if(filterSpecified.get(journal) != null && filterSpecified.get(journal).equals("subjects")) {
            String[] subjects = journalSubjectFilters.get(journal);

            userService.setFilteredWeeklySearchAlert(profile.getID(), subjects, journalKey);

            //The weekly alert is actually a savedSearch, remove from list
            weeklyAlerts.remove(journal);
          } else {
            userService.removedFilteredWeeklySearchAlert(profile.getID(), journalKey);
          }
        }
      } else {
        //If there is no filterSpecified make sure there are no filtered search alerts
        for(UserAlert s : this.getUserAlerts()) {
          userService.removedFilteredWeeklySearchAlert(profile.getID(), s.getKey());
        }
      }

      profile = userService.setAlerts(authId, monthlyAlerts, weeklyAlerts);

      session.put(Constants.AMBRA_USER_KEY, profile);
    }

    return retrieveAlerts();
  }

  /**
   *
   * Kind of kludge, but we seemingly have two ways of representing journalkeys in the ambra.xml
   * This only works as plosone is the same as "PLoSONE".  Not all journal names use this same convention.
   * These terms need to be corrected!  However, any changes made to these keys will cause user search
   * Alerts to be lost.  A data migration will have to be performed
   *
   * @param journal
   *
   * @return
   */
  private String translateJournalKey(String journal) {
    Set<Journal> journals = journalService.getAllJournals();
    for(Journal j  : journals) {
      if(j.getJournalKey().toLowerCase().equals(journal)) {
        return j.getJournalKey();
      }
    }

    throw new RuntimeException("Can not find journal of name:" + journal);
  }

  /**
   * save the user search alerts
   * @return webwork status
   * @throws Exception
   */
  public String saveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();

    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }

    UserProfile profile = userService.setSavedSearchAlerts(authId, monthlyAlerts, weeklyAlerts, deleteAlerts);
    session.put(Constants.AMBRA_USER_KEY, profile);

    return retrieveSearchAlerts();
  }

  private String getUserAuthId() {
    return (String) session.get(Constants.AUTH_KEY);
  }

  public String getTabID() {
    return tabID;
  }

  public String getOrcid() {
    return orcid;
  }

  public String getClientID() {
    return clientID;
  }

  public String getScope() {
    return scope;
  }

  public String getOrcidAuth() {
    return orcidAuth;
  }

  public String getRedirectURL() {
    return redirectURL;
  }

  public Collection<SavedSearchView> getUserSearchAlerts() throws Exception {
    return savedSearches;
  }

  public void setOrcidService(OrcidService orcidService) {
    this.orcidService = orcidService;
  }

  public void setServletRequest(HttpServletRequest request) { this.request = request; }
}
