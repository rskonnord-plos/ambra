/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.service.user;

import com.google.gson.Gson;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.SavedSearchType;
import org.ambraproject.models.UserLogin;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserSearch;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.permission.PermissionsService;
import org.ambraproject.service.search.SearchParameters;
import org.ambraproject.util.TextUtils;
import org.ambraproject.views.SavedSearchView;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.transaction.annotation.Transactional;
import org.ambraproject.configuration.ConfigurationStore;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to roll up web services that a user needs in Ambra. Rest of application should generally
 * use AmbraUser to
 *
 * @author Stephen Cheng
 * @author Joe Osowski
 */
public class UserServiceImpl extends HibernateServiceImpl implements UserService {
  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
  private static final String ALERTS_CATEGORIES_CATEGORY = "ambra.userAlerts.categories.category";
  private static final String ALERTS_WEEKLY = "ambra.userAlerts.weekly";
  private static final String ALERTS_MONTHLY = "ambra.userAlerts.monthly";
  private static final String SUBJECT_FILTER = "ambra.userAlerts.subjectFilter";

  private PermissionsService permissionsService;
  private Configuration configuration;
  private boolean advancedLogging = false;

  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public UserProfile login(final String authId, final UserLogin loginInfo) {
    log.debug("logging in user with auth id {}", authId);
    UserProfile user = getUserByAuthId(authId);
    if (user != null && this.advancedLogging) {
      loginInfo.setUserProfileID(user.getID());
      hibernateTemplate.save(loginInfo);
    }
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfile getUserByAuthId(String authId) {
    log.debug("Attempting to find user with authID: {}", authId);
    try {
      return (UserProfile) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(UserProfile.class)
              .add(Restrictions.eq("authId", authId))
          , 0, 1).get(0);
    } catch (IndexOutOfBoundsException e) {
      log.warn("Didn't find user for authID: {}", authId);
      return null;
    }
  }

  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public UserProfile updateProfile(final UserProfile userProfile) throws NoSuchUserException {
    //get the user by auth id
    UserProfile existingUser = getUserByAuthId(userProfile.getAuthId());
    if (existingUser == null) {
      throw new NoSuchUserException();
    }
    log.debug("Found a user with authID: {}, updating profile", userProfile.getAuthId());
    copyFields(userProfile, existingUser);
    hibernateTemplate.update(existingUser);
    return existingUser;
  }

  @Override
  @Transactional
  public UserProfile setAlerts(String userAuthId, List<String> monthlyAlerts, List<String> weeklyAlerts) {
    UserProfile user = getUserByAuthId(userAuthId);

    log.debug("updating alerts for user: {}; Montly alerts: {}; weekly alerts: {}",
        new Object[]{user.getDisplayName(), StringUtils.join(monthlyAlerts, ","), StringUtils.join(weeklyAlerts, ",")});
    List<String> allAlerts;

    if (monthlyAlerts != null && weeklyAlerts != null) {
      allAlerts = new ArrayList<String>(monthlyAlerts.size() + weeklyAlerts.size());
      allAlerts.addAll(getAlertsList(monthlyAlerts, UserProfile.MONTHLY_ALERT_SUFFIX));
      allAlerts.addAll(getAlertsList(weeklyAlerts, UserProfile.WEEKLY_ALERT_SUFFIX));
    } else if (monthlyAlerts != null) {
      allAlerts = new ArrayList<String>(monthlyAlerts.size());
      allAlerts.addAll(getAlertsList(monthlyAlerts, UserProfile.MONTHLY_ALERT_SUFFIX));
    } else if (weeklyAlerts != null) {
      allAlerts = new ArrayList<String>(weeklyAlerts.size());
      allAlerts.addAll(getAlertsList(weeklyAlerts, UserProfile.WEEKLY_ALERT_SUFFIX));
    } else {
      allAlerts = new ArrayList<String>(0);
    }
    user.setAlertsList(allAlerts);
    hibernateTemplate.update(user);
    return user;
  }

  @Override
  @Transactional
  public UserProfile setSavedSearchAlerts(String userAuthId, List<String> monthlyAlerts, List<String> weeklyAlerts, List<String> deleteAlerts) {
    UserProfile user = getUserByAuthId(userAuthId);

    log.debug("updating alerts for user: {}; Montly alerts: {}; weekly alerts: {}; delete alerts: {}",
        new Object[]{user.getDisplayName(), StringUtils.join(monthlyAlerts, ","), StringUtils.join(weeklyAlerts, ","), StringUtils.join(deleteAlerts, ",")});
    List<SavedSearchView> searches = getSavedSearches(user.getID());

    Set<String> weeklyItems = new HashSet<String>(weeklyAlerts);
    Set<String> monthlyItems = new HashSet<String>(monthlyAlerts);
    Set<String> deleteItems = new HashSet<String>(deleteAlerts);

    for (SavedSearchView savedSearch: searches) {
      String idstr = String.valueOf(savedSearch.getSavedSearchId());
      boolean delete = deleteItems.contains(idstr);
      if (delete) {
        deleteSavedSearch(user.getID(), savedSearch.getSavedSearchId());
      }
      else {
        boolean weekly = weeklyItems.contains(idstr);
        boolean monthly = monthlyItems.contains(idstr);
        if (weekly != savedSearch.getWeekly() || monthly != savedSearch.getMonthly()) {
          updateSavedSearch(savedSearch.getSavedSearchId(), weekly, monthly);
        }
      }
    }
    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(rollbackFor = {Throwable.class})
  public UserProfile setFilteredWeeklySearchAlert(Long userProfileId, String[] subjects, String journal)
  {
    SearchParameters searchParameters = new SearchParameters();

    searchParameters.setFilterJournals(new String[] { journal });
    searchParameters.setFilterSubjectsDisjunction(subjects);

    //We store the saved search here as JSON instead of serializing the object cuz JSON rocks
    SavedSearchQuery query = saveSearchQuery(searchParameters);

    UserProfile user = getUser(userProfileId);
    SavedSearch newSearch = null;

    //See if a record exists already, we only allow one weekly alert of type JOURNAL_ALERT per journal
    //We key off of the title as it is not user facing
    for(SavedSearch savedSearch : user.getSavedSearches()) {
      if(savedSearch.getSearchType() == SavedSearchType.JOURNAL_ALERT
        && savedSearch.getWeekly()
        && savedSearch.getSearchName().equals(journal)) {
        newSearch = savedSearch;
      }
    }

    if(newSearch == null) {
      newSearch = new SavedSearch(journal, query);
      newSearch.setSearchType(SavedSearchType.JOURNAL_ALERT);
      newSearch.setWeekly(true);
      newSearch.setMonthly(false);
      user.getSavedSearches().add(newSearch);
    } else {
      newSearch.setSearchQuery(query);
    }

    hibernateTemplate.save(user);

    return user;
  }


  /**
   * {@inheritDoc}
   */
  @Transactional(rollbackFor = {Throwable.class})
  @SuppressWarnings("unchecked")
  public void saveSearch(Long userProfileId,
                         SearchParameters searchParameters,
                         String name,
                         boolean weekly,
                         boolean monthly) {


    UserProfile user = hibernateTemplate.get(UserProfile.class, userProfileId);

    SavedSearchQuery query = saveSearchQuery(searchParameters);

    SavedSearch savedSearch = new SavedSearch(name, query);
    savedSearch.setSearchType(SavedSearchType.USER_DEFINED);
    savedSearch.setWeekly(weekly);
    savedSearch.setMonthly(monthly);

    user.getSavedSearches().add(savedSearch);

    hibernateTemplate.save(user);
  }

  /**
   * Check to see if a matching savedSearch exists already with the passed in parameters
   * if so, reuses that record
   *
   * @param searchParameters
   *
   * @return the savedQuery object
   */
  private SavedSearchQuery saveSearchQuery(SearchParameters searchParameters) {
    Gson gson = new Gson();

    //We store the saved search here as JSON instead of serializing the object.
    String searchParametersString = gson.toJson(searchParameters);
    String queryHash = TextUtils.createHash(searchParametersString);
    SavedSearchQuery query;

    //Check to see if a matching savedSearch exists already.
    List<SavedSearchQuery> queryList =
      hibernateTemplate.findByCriteria(DetachedCriteria.forClass(SavedSearchQuery.class)
        .add(Restrictions.eq("hash", queryHash))
        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

    if(queryList.size() == 0) {
      //It does exist, lets not create a new record
      query = new SavedSearchQuery(searchParametersString, queryHash);
      hibernateTemplate.save(query);
    } else {
      query = queryList.get(0);
    }

    return query;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(rollbackFor = {Throwable.class})
  public List<SavedSearchView> getSavedSearches(Long userProfileId) {
    UserProfile userProfile = (UserProfile) DataAccessUtils.uniqueResult(
      hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(UserProfile.class)
          .add(Restrictions.eq("ID", userProfileId))
          .setFetchMode("savedSearches", FetchMode.JOIN)
          .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      ));

    List<SavedSearch> searches = userProfile.getSavedSearches();
    List<SavedSearchView> searchViews = new ArrayList<SavedSearchView>(searches.size());

    for(SavedSearch savedSearch : searches) {
      searchViews.add(new SavedSearchView(savedSearch));
    }

    return searchViews;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(rollbackFor = {Throwable.class})
  public void deleteSavedSearch(Long userProfileId, Long savedSearchId) {

    UserProfile userProfile = (UserProfile) DataAccessUtils.uniqueResult(
        hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(UserProfile.class)
                .add(Restrictions.eq("ID", userProfileId))
                .setFetchMode("savedSearches", FetchMode.JOIN)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        ));
    List<SavedSearch> savedSearches = userProfile.getSavedSearches();
    for (Iterator<SavedSearch> it=savedSearches.iterator(); it.hasNext(); ) {
      SavedSearch savedSearch = it.next();
      if (savedSearch.getID().equals(savedSearchId)) {
        it.remove();
      }
    }
    hibernateTemplate.update(userProfile);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(rollbackFor = {Throwable.class})
  public void updateSavedSearch(Long savedSearchId, boolean weekly, boolean monthly) {
    SavedSearch savedSearch = hibernateTemplate.get(SavedSearch.class, savedSearchId);

    savedSearch.setMonthly(monthly);
    savedSearch.setWeekly(weekly);

    hibernateTemplate.update(savedSearch);
  }

  /**
   * return a list of alerts strings with the given suffix added, if they don't already have it
   *
   * @param alerts the list of alerts
   * @param suffix the alerts suffix
   * @return a list of alerts strings with the given suffix added, if they don't already have it
   */
  private List<String> getAlertsList(List<String> alerts, String suffix) {
    List<String> result = new ArrayList<String>(alerts.size());
    for (String alert : alerts) {
      if (alert.endsWith(suffix)) {
        result.add(alert);
      } else {
        result.add(alert + suffix);
      }
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfile getUser(Long userId) {
    if (userId != null) {
      log.debug("Looking up user with id: {}", userId);
      return (UserProfile) hibernateTemplate.get(UserProfile.class, userId);
    } else {
      throw new IllegalArgumentException("Null userId");
    }
  }

  @Override
  public UserProfile getProfileForDisplay(UserProfile userProfile, boolean showPrivateFields) {
    UserProfile display = new UserProfile();
    copyFields(userProfile, display);
    if (!showPrivateFields) {
      log.debug("Removing private fields for display on user: {}", userProfile.getDisplayName());
      display.setOrganizationName(null);
      display.setOrganizationType(null);
      display.setPostalAddress(null);
      display.setPositionType(null);
    }

    //escape html in all string fields
    BeanWrapper wrapper = new BeanWrapperImpl(display);
    for (PropertyDescriptor property : wrapper.getPropertyDescriptors()) {
      if (String.class.isAssignableFrom(property.getPropertyType())) {
        String name = property.getName();
        wrapper.setPropertyValue(name, TextUtils.escapeHtml((String) wrapper.getPropertyValue(name)));
      }
    }


    return display;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserAlert> getAvailableAlerts() {
    List<UserAlert> alerts = new ArrayList<UserAlert>();

    final Map<String, String> categoryNames = new HashMap<String, String>();

    HierarchicalConfiguration hc = (HierarchicalConfiguration) configuration;
    List<HierarchicalConfiguration> categories = hc.configurationsAt(ALERTS_CATEGORIES_CATEGORY);
    for (HierarchicalConfiguration c : categories) {
      String key = c.getString("[@key]");
      String value = c.getString("");
      categoryNames.put(key, value);
    }

    final String[] weeklyCategories = hc.getStringArray(ALERTS_WEEKLY);
    final String[] monthlyCategories = hc.getStringArray(ALERTS_MONTHLY);
    final String[] subjectFilters = hc.getStringArray(SUBJECT_FILTER);

    final Set<Map.Entry<String, String>> categoryNamesSet = categoryNames.entrySet();

    for (final Map.Entry<String, String> category : categoryNamesSet) {
      final String key = category.getKey();
      boolean weeklyCategoryKey = false;
      boolean monthlyCategoryKey = false;
      boolean subjectFilter = false;
      if (ArrayUtils.contains(weeklyCategories, key)) {
        weeklyCategoryKey = true;
      }
      if (ArrayUtils.contains(monthlyCategories, key)) {
        monthlyCategoryKey = true;
      }
      if (ArrayUtils.contains(subjectFilters, key)) {
        subjectFilter = true;
      }
      alerts.add(new UserAlert(key, category.getValue(), weeklyCategoryKey, monthlyCategoryKey, subjectFilter));
    }
    return alerts;
  }

  /**
   * Copy fields for updating or display. Does <b>not</b> copy some fields:
   * <ul>
   * <li>ID: never overwrite IDs on hibernate objects</li>
   * <li>userAccountUri: these don't come down from display layer, so we don't want to overwrite with null</li>
   * <li>userProfileUri: these don't come down from display layer, so we don't want to overwrite with null</li>
   * <li>roles: don't want to overwrite a user's roles when updating their profile</li>
   * </ul>
   *
   * @param source
   * @param destination
   */
  private void copyFields(UserProfile source, UserProfile destination) {
    destination.setAuthId(source.getAuthId());
    destination.setRealName(source.getRealName());
    destination.setGivenNames(source.getGivenNames());
    destination.setSurname(source.getSurname());
    destination.setTitle(source.getTitle());
    destination.setGender(source.getGender());
    destination.setEmail(source.getEmail());
    destination.setHomePage(source.getHomePage());
    destination.setWeblog(source.getWeblog());
    destination.setPublications(source.getPublications());
    destination.setDisplayName(source.getDisplayName());
    destination.setSuffix(source.getSuffix());
    destination.setPositionType(source.getPositionType());
    destination.setOrganizationName(source.getOrganizationName());
    destination.setOrganizationType(source.getOrganizationType());
    destination.setPostalAddress(source.getPostalAddress());
    destination.setCity(source.getCity());
    destination.setCountry(source.getCountry());
    destination.setBiography(source.getBiography());
    destination.setInterests(source.getInterests());
    destination.setResearchAreas(source.getResearchAreas());
    destination.setOrganizationVisibility(source.getOrganizationVisibility());
    destination.setAlertsJournals(source.getAlertsJournals());
  }

  @Override
  @Transactional
  public Long recordArticleView(Long userId, Long articleId, ArticleView.Type type) {
    if (this.advancedLogging) {
      return (Long) hibernateTemplate.save(new ArticleView(userId, articleId, type));
    } else {
      return 0L;
    }
  }

  @Override
  @Transactional
  public Long recordUserSearch(Long userProfileID, String searchTerms, String searchParams) {
    if (this.advancedLogging) {
      return (Long) hibernateTemplate.save(new UserSearch(userProfileID, searchTerms, searchParams));
    } else {
      return 0L;
    }
  }

  /**
   * Getter for property 'permissionsService'.
   *
   * @return Value for property 'permissionsService'.
   */
  public PermissionsService getPermissionsService() {
    return permissionsService;
  }

  /**
   * Setter for property 'permissionsService'.
   *
   * @param permissionsService Value to set for property 'permissionsService'.
   */
  @Required
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;

    Object val = configuration.getProperty(ConfigurationStore.ADVANCED_USAGE_LOGGING);
    if (val != null && val.equals("true")) {
      advancedLogging = true;
    }
  }
}
