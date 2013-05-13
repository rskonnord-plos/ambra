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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.journal.JournalService;
import org.ambraproject.service.user.UserAlert;
import org.ambraproject.util.ProfanityCheckingService;
import org.ambraproject.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.service.user.UserService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base class for user actions in order to have a userService object accessible
 * 
 * @author Stephen Cheng
 * 
 */
public class UserActionSupport extends BaseSessionAwareActionSupport {
  private static final Logger log = LoggerFactory.getLogger(UserActionSupport.class);

  private static final String GIVEN_NAMES = "givenNames";
  private static final String REAL_NAME = "realName";
  private static final String POSTAL_ADDRESS = "postalAddress";
  private static final String ORGANIZATION_TYPE = "organizationType";
  private static final String ORGANIZATION_NAME = "organizationName";
  private static final String TITLE = "title";
  private static final String POSITION_TYPE = "positionType";
  private static final String DISPLAY_NAME = "displayName";
  private static final String SURNAMES = "surnames";
  private static final String CITY = "city";
  private static final String COUNTRY = "country";
  private static final String BIOGRAPHY_TEXT = "biographyText";
  private static final String INTERESTS_TEXT = "interestsText";
  private static final String RESEARCH_AREAS_TEXT = "researchAreasText";
  private static final String HOME_PAGE = "homePage";
  private static final String WEBLOG = "weblog";
  private static final String HTTP_PREFIX = "http://";

  protected UserService userService;
  protected JournalService journalService;
  private String email;
  private String displayName;
  private String givenNames;
  private String surnames;
  private String positionType;
  private String organizationType;
  private String organizationName;
  private String postalAddress;
  private String biographyText;
  private String interestsText;
  private String researchAreasText;
  private String homePage;
  private String weblog;
  private String city;
  private String country;
  private String title;
  private boolean organizationVisibility;
  //users don't actually edit this value, but we need to pass it to the freemarker in a hidden input to get it back on the save action
  //TODO: Confirm this is needed, I don't think it is
  private String alertsJournals;
  protected List<String> monthlyAlerts = new ArrayList<String>();
  protected List<String> weeklyAlerts = new ArrayList<String>();
  protected List<String> deleteAlerts = new ArrayList<String>();

  //Used for setting of filtered journal alerts
  protected Map<String, String[]> journalSubjectFilters;
  protected Map<String, String> filterSpecified;

  //Need to hide the username text box field on the edit Profile page. Should display the text box only on create profile page.
  protected boolean showDisplayName = true;
  private ProfanityCheckingService profanityCheckingService;

  /**
   * @param userService the userService to use
   */
  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * @param journalService the journalService to use
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  protected boolean validateProfileInput() {
    boolean isValid = true;
    if (givenNames == null || StringUtils.isBlank(givenNames)) {
      addFieldError(GIVEN_NAMES, "Given name cannot be empty");
      isValid = false;
    }
    if (surnames == null || StringUtils.isBlank(surnames)) {
      addFieldError(SURNAMES, "Surname cannot be empty");
      isValid = false;
    }

    if (!isValidUrl(homePage)) {
      addFieldError(HOME_PAGE, "Home page URL is not valid");
      isValid = false;
    }
    if (!isValidUrl(weblog)) {
      addFieldError(WEBLOG, "Weblog URL is not valid");
      isValid = false;
    }

    isValid = checkProfanity() && isValid;

    return isValid;
  }

  protected boolean validateAlertInput() {
    boolean isValid = true;

    if(filterSpecified != null) {
      for(String journal : filterSpecified.keySet()) {
        //If the user has selected a filtered search result, check
        //that they have selected at least one filter and no more then 12
        if(filterSpecified.get(journal).equals("subjects")) {
          if(journalSubjectFilters == null || journalSubjectFilters.get(journal) == null ||
            journalSubjectFilters.get(journal).length == 0) {
            addActionError("You must selected at least one subject to filter on");
            isValid = false;
          } else {
            if(journalSubjectFilters.get(journal).length > 12) {
              addActionError("You can not select more then 12 subjects to filter on");
              isValid = false;
            }
          }
        }
      }
    }

    return isValid;
  }



  private boolean checkProfanity() {
    boolean isValid = true;
    final String[][] fieldsToValidate = new String[][]{
        {DISPLAY_NAME, displayName},
        {TITLE, title},
        {SURNAMES, surnames},
        {GIVEN_NAMES, givenNames},
        {POSITION_TYPE, positionType},
        {ORGANIZATION_NAME, organizationName},
        {ORGANIZATION_TYPE, organizationType},
        {POSTAL_ADDRESS, postalAddress},
        {BIOGRAPHY_TEXT, biographyText},
        {INTERESTS_TEXT, interestsText},
        {RESEARCH_AREAS_TEXT, researchAreasText},
        {HOME_PAGE, homePage},
        {WEBLOG, weblog},
        {CITY, city},
        {COUNTRY, country},
    };
    for (final String[] field : fieldsToValidate) {
      final String fieldName = field[0];
      final String fieldValue = field[1];
      final List<String> profaneWords = profanityCheckingService.validate(fieldValue);
      if (profaneWords.size() > 0) {
        addProfaneMessages(profaneWords, fieldName, fieldName);
        isValid = false;
      }
    }
    return isValid;
  }

  private boolean isValidUrl(final String url) {
    //allow null or or empty value for urls
    return StringUtils.isEmpty(url) || TextUtils.verifyUrl(url) || TextUtils.verifyUrl(HTTP_PREFIX + url);
  }

  protected void setFieldsFromProfile(UserProfile ambraUser) {
    email = ambraUser.getEmail();
    displayName = ambraUser.getDisplayName();
    givenNames = ambraUser.getGivenNames();
    surnames = ambraUser.getSurname();
    title = ambraUser.getTitle();
    positionType = ambraUser.getPositionType();
    organizationType = ambraUser.getOrganizationType();
    organizationName = ambraUser.getOrganizationName();
    postalAddress = ambraUser.getPostalAddress();
    biographyText = ambraUser.getBiography();
    interestsText = ambraUser.getInterests();
    researchAreasText = ambraUser.getResearchAreas();
    homePage = ambraUser.getHomePage();
    weblog = ambraUser.getWeblog();
    city = ambraUser.getCity();
    country = ambraUser.getCountry();
    alertsJournals = ambraUser.getAlertsJournals();
    organizationVisibility = ambraUser.getOrganizationVisibility();
  }

  protected UserProfile createProfileFromFields() throws Exception {
    UserProfile userProfile = new UserProfile();
    userProfile.setDisplayName(this.displayName);
    userProfile.setEmail(this.email);
    userProfile.setTitle(this.title);
    userProfile.setSurname(this.surnames);
    userProfile.setGivenNames(this.givenNames);
    userProfile.setRealName(this.givenNames + " " + this.surnames);
    userProfile.setPositionType(this.positionType);
    userProfile.setOrganizationType(this.organizationType);
    userProfile.setOrganizationName(this.organizationName);
    userProfile.setPostalAddress(this.postalAddress);
    userProfile.setBiography(this.biographyText);
    userProfile.setInterests(this.interestsText);
    userProfile.setResearchAreas(this.researchAreasText);
    userProfile.setAlertsJournals(this.alertsJournals);

    final String homePageUrl = StringUtils.stripToNull(makeValidUrl(homePage));
    userProfile.setHomePage(homePageUrl);

    final String weblogUrl = StringUtils.stripToNull(makeValidUrl(weblog));
    userProfile.setWeblog(weblogUrl);

    userProfile.setCity(this.city);
    userProfile.setCountry(this.country);
    userProfile.setOrganizationVisibility(this.organizationVisibility);

    return userProfile;
  }

  private String makeValidUrl(final String url) throws MalformedURLException {
    final String newUrl = StringUtils.stripToEmpty(url);
    if (StringUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase(HTTP_PREFIX)) {
      return StringUtils.EMPTY;
    }
    return TextUtils.makeValidUrl(newUrl);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public void setGivenNames(String givenNames) {
    this.givenNames = givenNames;
  }

  public String getSurnames() {
    return surnames;
  }

  public void setSurnames(String surnames) {
    this.surnames = surnames;
  }

  public String getPositionType() {
    return positionType;
  }

  public void setPositionType(String positionType) {
    this.positionType = positionType;
  }

  public String getOrganizationType() {
    return organizationType;
  }

  public void setOrganizationType(String organizationType) {
    this.organizationType = organizationType;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  public String getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  public String getBiographyText() {
    return biographyText;
  }

  public void setBiographyText(String biographyText) {
    this.biographyText = biographyText;
  }

  public String getInterestsText() {
    return interestsText;
  }

  public void setInterestsText(String interestsText) {
    this.interestsText = interestsText;
  }

  public String getResearchAreasText() {
    return researchAreasText;
  }

  public void setResearchAreasText(String researchAreasText) {
    this.researchAreasText = researchAreasText;
  }

  public String getHomePage() {
    return homePage;
  }

  public void setHomePage(String homePage) {
    this.homePage = homePage;
  }

  public String getWeblog() {
    return weblog;
  }

  public void setWeblog(String weblog) {
    this.weblog = weblog;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean getOrganizationVisibility() {
    return organizationVisibility;
  }

  public void setOrganizationVisibility(boolean organizationVisibility) {
    this.organizationVisibility = organizationVisibility;
  }

  public Collection<UserAlert> getUserAlerts() {
    return userService.getAvailableAlerts();
  }

  @Required
  public void setProfanityCheckingService(ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  public String getAlertsJournals() {
    return alertsJournals;
  }

  public void setAlertsJournals(String alertsJournals) {
    this.alertsJournals = alertsJournals;
  }

  public boolean isShowDisplayName() {
    return showDisplayName;
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
    if(monthlyAlerts != null) {
      this.monthlyAlerts = new LinkedList<String>(Arrays.asList(monthlyAlerts));
    } else {
      this.monthlyAlerts = null;
    }
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
    //Use LinkedList as it supports removing items
    if(weeklyAlerts != null) {
      this.weeklyAlerts = new LinkedList<String>(Arrays.asList(weeklyAlerts));
    } else {
      this.weeklyAlerts = null;
    }
  }

  public List<String> getDeleteAlerts() {
    return deleteAlerts;
  }

  public void setDeleteAlerts(String[] deleteAlerts) {
    //Use LinkedList as it supports removing items
    if(deleteAlerts != null) {
      this.deleteAlerts = new LinkedList<String>(Arrays.asList(deleteAlerts));
    } else {
      this.deleteAlerts = null;
    }
  }

  public Map getJournalSubjectFilters()
  {
    return journalSubjectFilters;
  }

  public void setJournalSubjectFilters(Map<String, String[]> journalSubjectFilters)
  {
    this.journalSubjectFilters = journalSubjectFilters;
  }

  public Map getFilterSpecified()
  {
    return this.filterSpecified;
  }

  public void setFilterSpecified(Map<String, String> filterSpecified)
  {
    this.filterSpecified = filterSpecified;
  }
}
