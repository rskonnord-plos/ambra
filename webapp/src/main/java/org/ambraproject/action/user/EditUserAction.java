package org.ambraproject.action.user;

import org.ambraproject.Constants;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserProfile;
import org.ambraproject.views.SavedSearchView;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Joe Osowski 04/12/2013
 * @author Alex Kudlick 10/23/12
 */
public class EditUserAction extends UserActionSupport {

  private List<SavedSearchView> savedSearches;

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
        journalSubjectFilters.put(curJournal, subjectFilters);

        //Append the weekly alert for the view
        weeklyAlerts.add(curJournal);
      }
    }

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

          if(filterSpecified.get(journal) != null && filterSpecified.get(journal).equals("subjects")) {
            String[] subjects = journalSubjectFilters.get(journal);

            profile = userService.setFilteredWeeklySearchAlert(profile.getID(), subjects, journal);

            //The weekly alert is actually a savedSearch, remove from list
            weeklyAlerts.remove(journal);
          } else {
            profile = userService.removedFilteredWeeklySearchAlert(profile.getID(), journal);
          }
        }
      }

      profile = userService.setAlerts(authId, monthlyAlerts, weeklyAlerts);

      session.put(Constants.AMBRA_USER_KEY, profile);
    }

    return retrieveAlerts();
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

  public Collection<SavedSearchView> getUserSearchAlerts() throws Exception {
    return savedSearches;
  }
}
