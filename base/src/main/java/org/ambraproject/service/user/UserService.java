/*
 * Copyright (c) 2007-2014 by Public Library of Science
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
package org.ambraproject.service.user;

import org.ambraproject.models.ArticleView;
import org.ambraproject.models.UserLogin;
import org.ambraproject.models.UserOrcid;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.search.SearchParameters;
import org.ambraproject.util.Pair;
import org.ambraproject.views.OrcidAuthorization;
import org.ambraproject.views.SavedSearchView;

import java.util.List;

/**
 * Class to roll up web services that a user needs in Ambra. Rest of application should generally
 * use AmbraUser to
 *
 * @author Stephen Cheng
 */
public interface UserService {

  /**
   * Login the user for the auth id with the given login info.  Return an AmbraUser object for display/caching purposes.
   *
   * @param authId    the auth id of the user being logged in
   * @param loginInfo detached UserLogin object holding login information (browser, ip, etc.)
   * @return the user object
   */
  public UserProfile login(final String authId, final UserLogin loginInfo);

  /**
   * Get the user specified by the given id
   * @param userId the id of the given user
   * @return the user specified by the given id
   */
  public UserProfile getUser(Long userId);

  /**
   * Save the current user's ORCiD information
   *
   * @param userProfileId the userProfileId of the account to update
   * @param orcidAuthorization the orcidAuthorization set of values to update with
   */
  public void saveUserOrcid(Long userProfileId, OrcidAuthorization orcidAuthorization) throws DuplicateOrcidException;

  /**
   * Remove any record of the user's ORCiD from our system
   *
   * @param userProfileId the userProfileId of the account to update
   */
  public void removeUserOrcid(Long userProfileId);

  /**
   * Fetch the user's orcid data (or null if it does not exist)
   *
   * @param userProfileId the userProfileId of the account to fetch
   */
  public UserOrcid getUserOrcid(Long userProfileId);

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   *
   * @param authId authentication ID
   * @return the user associated with the authID
   */
  public UserProfile getUserByAuthId(String authId);

  /**
   * Update the stored user object with the same authId as the provided argument.
   *
   * Does not overwrite a user's roles, and if some are provided they will be ignored
   * 
   *
   *
   * @param userProfile a detached {@link org.ambraproject.models.UserProfile} instance with values to save
   * @return the saved user object
   * @throws DuplicateUserException if no user matches the auth id in the profile object provided
   */
  public UserProfile updateProfile(UserProfile userProfile) throws NoSuchUserException;

  /**
   * Save the given alerts for the user specified by the given id.
   *
   * @param userAuthId    the auth id of the user to set the alerts on
   * @param monthlyAlerts a list of the monthly alerts
   * @param weeklyAlerts  a list of the weekly alerts
   * @return the user object
   */
  public UserProfile setAlerts(String userAuthId, List<String> monthlyAlerts, List<String> weeklyAlerts);

  /**
   * save the given search alerts for the user specified by the given id
   * @param userAuthId  the auth id of the user to set the alerts on
   * @param monthlyAlerts a list of the monthly alerts
   * @param weeklyAlerts  a list of the weekly alerts
   * @param deleteAlerts  a list of the delete alerts
   */
  public UserProfile setSavedSearchAlerts(String userAuthId, List<String> monthlyAlerts, List<String> weeklyAlerts, List<String> deleteAlerts);

  /**
   * Create a weekly filtered savedSearch alert for the passed in journal
   *
   * @param userProfileId the auth id of the user to set the alerts on
   * @param subjects the subjects to filter on
   * @param journal the journal to filter on
   *
   * @return the userProfile
   */
  public UserProfile setFilteredWeeklySearchAlert(Long userProfileId, String[] subjects, String journal);

  /**
   * Removed a weekly filtered savedSearch alert for the passed in journal and userProfile
   *
   * @param userProfileId the userProfileID
   * @param journal the journal entry
   *
   * @return
   */
  public UserProfile removedFilteredWeeklySearchAlert(Long userProfileId, String journal);

  /**
   * Remove the saved search
   *
   * @param savedSearchId the savedSearchId to remove
   */
  public void deleteSavedSearch(Long userProfileId, Long savedSearchId);

  /**
   * Update the saved search with new preferences
   *
   * @param savedSearchId the savedSearchID
   * @param weekly weekly value
   * @param monthly monthly value
   */
  public void updateSavedSearch(Long savedSearchId, boolean weekly, boolean monthly);

  /**
   * Get all the saved searches for a given user
   *
   * @param userProfileId the userProfileId of the user
   *
   * @return a list of saved search views
   */
  public List<SavedSearchView> getSavedSearches(Long userProfileId);

  /**
   * Save the search to the user's profile
   *
   * @param userProfileId the user to create the search for
   * @param searchParameters the search parameters to use
   * @param name the name of the search
   * @param weekly the weekly value
   * @param monthly the monthly value
   */
  public void saveSearch(Long userProfileId,
                         SearchParameters searchParameters,
                         String name,
                         boolean weekly,
                         boolean monthly);

  /**
   * Return a copy of the given user profile object with private fields set to null, if applicable, and all html escaped in string fields
   *
   * @param userProfile the user object to copy
   * @param showPrivateFields if true, show fields the user has marked as private
   * @return a copy of the given user profile
   */
  public UserProfile getProfileForDisplay(UserProfile userProfile, boolean showPrivateFields);

  /**
   * Return user alert data data which is specified in the configuration. <br>
   * <p/>
   * Config FORMAT EXAMPLE:<br>
   * <p/>
   * <pre>
   * &lt;userAlerts&gt;
   *   &lt;categories&gt;
   *     &lt;category key=&quot;biology&quot;&gt;PLoS Biology&lt;/category&gt;
   *     &lt;category key=&quot;computational_biology&quot;&gt;PLoS Computational Biology&lt;/category&gt;
   *     &lt;category key=&quot;clinical_trials&quot;&gt;PLoS Hub for Clinical Trials&lt;/category&gt;
   *     &lt;category key=&quot;genetics&quot;&gt;PLoS Genetics&lt;/category&gt;
   *     &lt;category key=&quot;medicine&quot;&gt;PLoS Medicine&lt;/category&gt;
   *     &lt;category key=&quot;pathogens&quot;&gt;PLoS Pathogens&lt;/category&gt;
   *     &lt;category key=&quot;plosntds&quot;&gt;PLoS Neglected Tropical Diseases&lt;/category&gt;
   *     &lt;category key=&quot;plosone&quot;&gt;PLoS ONE&lt;/category&gt;
   *     &lt;/categories&gt;
   *     &lt;monthly&gt;biology, clinical_trials, computational_biology, genetics, medicine, pathogens, plosntds&lt;/monthly&gt;
   *     &lt;weekly&gt;biology, clinical_trials, computational_biology, genetics, medicine, pathogens, plosntds, plosone&lt;/weekly&gt;
   * &lt;/userAlerts&gt;
   * </pre>
   *
   * @return All available user alerts
   */
  public List<UserAlert> getAvailableAlerts();

  /**
   * Record an article view by the given user
   * @param userId the id of the user
   * @param articleId the id of the article
   * @param type the type of view (e.g. Article view, XML download, etc.)
   * @return the id of the article view that was stored
   */
  public Long recordArticleView(Long userId, Long articleId, ArticleView.Type type);

  /**
   * Record a search performed by the given user
   * @param userProfileID the id of the user
   * @param searchTerms the search terms entered
   * @param searchParams all other parameters serialized
   * @return the id of the log entry created
   */
  public Long recordUserSearch(Long userProfileID, String searchTerms, String searchParams);

  /**
   * Whether the logged in user has the specified category in her journal alerts.
   * Return false if user is not logged in.
   *
   * @param category - the category string
   * @return two value - first is a boolean which is true if the category is saved
   * as a journal alert search for the logged in user, and second is an integer with
   * the number of subject categories the user has already subscribed to in this journal.
   */
  public Pair<Boolean,Integer> getJournalAlertAndSubjectCount(Long userId, String journal, String category);

  /**
   * Return a list of subjects for which this user has a journal alert set up.
   *
   * @param userId
   * @param journal
   * @return  all available list of subject for users
   */
  public List<String> getJournalAlertSubjects(Long userId, String journal);
}
