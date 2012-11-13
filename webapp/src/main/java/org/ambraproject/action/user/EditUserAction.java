package org.ambraproject.action.user;

import org.ambraproject.Constants;
import org.ambraproject.models.UserProfile;
import org.ambraproject.views.SavedSearchView;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Alex Kudlick 10/23/12
 */
public class EditUserAction extends UserActionSupport {

  private List<SavedSearchView> savedSearches;

  @Override
  public String execute() throws Exception {
    String authId = getUserAuthId();
    UserProfile userProfile = userService.getUserByAuthId(authId);
    setFieldsFromProfile(userProfile);
    showDisplayName = false;
    final List<String> monthlyAlertsList = userProfile.getMonthlyAlerts();
    final List<String> weeklyAlertsList = userProfile.getWeeklyAlerts();

    monthlyAlerts = monthlyAlertsList.toArray(new String[monthlyAlertsList.size()]);
    weeklyAlerts = weeklyAlertsList.toArray(new String[weeklyAlertsList.size()]);

    return SUCCESS;
  }

  @SuppressWarnings("unchecked")
  public String saveUser() throws Exception {
    boolean valid = validateProfileInput();
    if (!valid) {
      return INPUT;
    }

    UserProfile profile = createProfileFromFields();
    //Make sure to set the auth id so the user service can see if this account already exists
    String userAuthId = getUserAuthId();
    profile.setAuthId(userAuthId);
    UserProfile savedProfile = userService.updateProfile(profile);
    session.put(Constants.AMBRA_USER_KEY, savedProfile);
    setFieldsFromProfile(savedProfile);
    return SUCCESS;
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
    UserProfile profile = userService.setAlerts(authId, Arrays.asList(monthlyAlerts), Arrays.asList(weeklyAlerts));
    session.put(Constants.AMBRA_USER_KEY, profile);
    setFieldsFromProfile(profile);
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
  public String saveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    userService.setSavedSearchAlerts(authId, Arrays.asList(monthlyAlerts), Arrays.asList(weeklyAlerts), Arrays.asList(deleteAlerts));
    return SUCCESS;
  }

  public String retrieveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    final UserProfile user = userService.getUserByAuthId(authId);
    savedSearches = userService.getSavedSearches(user.getID());

    return SUCCESS;
  }

  private String getUserAuthId() {
    return (String) session.get(Constants.AUTH_KEY);
  }


}
