/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ws.pap.UserPreference;
import org.topazproject.ws.pap.UserProfile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class to roll up access for a user into a PLoS ONE appplication specific object.
 * @author Stephen Cheng
 */

public class PlosOneUser {

  private static final Log log = LogFactory.getLog(PlosOneUser.class);

  /* Topaz user ID */
  private String userId;

  /* authID for the user in this app */
  private String authId;

  private UserProfile userProfile;

  private Map<String, String[]> userPrefs;

  // Constants for application specific User Preferences
  private static final String ALERTS_EMAIL_ADDRESS = "alertsEmailAddress";
  private static final String ALERTS_CATEGORIES = "alertsJournals";

  /**
   * Initializes a new PLoS ONE user
   */
  public PlosOneUser() {
    userProfile = new UserProfile();
    userPrefs = new HashMap<String, String[]>();
  }

  /**
   * Initializes a new PLoS ONE user and sets the authentication ID
   *
   * @param authId
   *          authentication ID of new user
   */
  public PlosOneUser(String authId) {
    this();
    this.authId = authId;
  }

  /**
   * Method to check to see if this was a migrated PLoS user or not.
   *
   * @return Returns true if the user is initialized (i.e. has chosen a username)
   */
  public boolean isInitialized() {
    if (userProfile == null) {
      return false;
    }
    return (userProfile.getDisplayName() != null);
  }

  /**
   * Returns the userPrefs for the web services to use. Should never need to be used by the main
   * parts of the application.
   *
   * @return Returns the userPrefs.
   */
  public UserPreference[] getUserPrefs() {
    int numElements = userPrefs.size();
    UserPreference[] tempPrefs = new UserPreference[numElements];
    UserPreference onePref;
    Set<Entry<String, String[]>> prefsSet = userPrefs.entrySet();
    Iterator<Entry<String, String[]>> iter = prefsSet.iterator();
    Entry<String, String[]> oneEntry;
    int i = 0;
    while (iter.hasNext()) {
      oneEntry = iter.next();
      onePref = new UserPreference();
      onePref.setName(oneEntry.getKey());
      onePref.setValues(oneEntry.getValue());
      tempPrefs[i] = onePref;
      i++;
    }
    return tempPrefs;
  }

  /**
   * Sets the userPrefs for the back end web services. Should never need to be used by the main
   * parts of the application.
   *
   * @param inUserPrefs
   *          The userPrefs to set.
   */
  public void setUserPrefs(UserPreference[] inUserPrefs) {
    if (inUserPrefs == null || inUserPrefs.length == 0) {
      this.userPrefs = null;
    } else {
      this.userPrefs = new HashMap<String, String[]>();
      // int numElements = inUserPrefs.length;
      for (UserPreference onePref : inUserPrefs) {
        this.userPrefs.put(onePref.getName(), onePref.getValues());
      }
    }
  }

  /**
   * @return Returns the userProfile.
   */
  public UserProfile getUserProfile() {
    return userProfile;
  }

  /**
   * @param userProfile
   *          The userProfile to set.
   */
  public void setUserProfile(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  /**
   * @return Returns the authId.
   */
  public String getAuthId() {
    return authId;
  }

  /**
   * @param authId
   *          The authId to set.
   */
  public void setAuthId(String authId) {
    this.authId = authId;
  }

  /**
   * @return Returns the biography.
   */
  public String getBiography() {
    return getNonNull(userProfile.getBiography());
  }

  /**
   * @param biography
   *          The biography to set.
   */
  public void setBiography(String biography) {
    userProfile.setBiography(biography);
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    if (userProfile != null){
      return getNonNull(userProfile.getDisplayName());
    }
    return ("");
  }

  /**
   * @param displayName
   *          The displayName to set.
   */
  public void setDisplayName(String displayName) {
    userProfile.setDisplayName(displayName);
  }

  /**
   * @return Returns the email.
   */
  public String getEmail() {
    return getNonNull(userProfile.getEmail());
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    userProfile.setEmail(email);
  }

  /**
   * @return Returns the gender.
   */
  public String getGender() {
    return getNonNull(userProfile.getGender());
  }

  /**
   * @param gender
   *          The gender to set.
   */
  public void setGender(String gender) {
    userProfile.setGender(gender);
  }

  /**
   * @return Returns the homePage.
   */
  public String getHomePage() {
    return getNonNull(userProfile.getHomePage());
  }

  /**
   * @param homePage
   *          The homePage to set.
   */
  public void setHomePage(String homePage) {
    userProfile.setHomePage(homePage);
  }

  /**
   * @return Returns the interests.
   */
  public String[] getInterests() {
    return userProfile.getInterests();
  }

  /**
   * @param interests
   *          The interests to set.
   */
  public void setInterests(String[] interests) {
    userProfile.setInterests(interests);
  }

  /**
   * @return Returns the publications.
   */
  public String getPublications() {
    return getNonNull(userProfile.getPublications());
  }

  /**
   * @param publications
   *          The publications to set.
   */
  public void setPublications(String publications) {
    userProfile.setPublications(publications);
  }

  /**
   * @return Returns the realName.
   */
  public String getRealName() {
    return getNonNull(userProfile.getRealName());
  }

  /**
   * @param realName
   *          The realName to set.
   */
  public void setRealName(String realName) {
    userProfile.setRealName(realName);
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return getNonNull(userProfile.getTitle());
  }

  /**
   * @param title
   *          The title to set.
   */
  public void setTitle(String title) {
    userProfile.setTitle(title);
  }

  /**
   * @return Returns the userId.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId
   *          The userId to set.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return Returns the weblog.
   */
  public String getWeblog() {
    return getNonNull(userProfile.getWeblog());
  }

  /**
   * @param weblog
   *          The weblog to set.
   */
  public void setWeblog(String weblog) {
    userProfile.setWeblog(weblog);
  }

  /**
   *
   * @return Returns the city.
   */
  public String getCity() {
    return getNonNull(userProfile.getCity());
  }

  /**
   *
   * @param city
   *          The city to set.
   */
  public void setCity(String city) {
    userProfile.setCity(city);
  }

  /**
   *
   * @return Returns the country.
   */
  public String getCountry() {
    return getNonNull(userProfile.getCountry());
  }

  /**
   *
   * @param country
   *          The country to set.
   */
  public void setCountry(String country) {
    userProfile.setCountry(country);
  }

  /**
   *
   * @return array of categories user is subscribed to
   */
  public String[] getAlerts() {
    return null == userPrefs ? null : userPrefs.get(PlosOneUser.ALERTS_CATEGORIES);
  }

  /**
   *
   * @param inAlerts
   *          the array of alert categories to set
   */
  public void setAlerts(String[] inAlerts) {
    userPrefs.put(PlosOneUser.ALERTS_CATEGORIES, inAlerts);
  }

  /**
   *
   * @return email address alerts are being sent to
   */
  public String getAlertsEmailAddress() {
    final String[] emailAddresses = userPrefs.get(PlosOneUser.ALERTS_EMAIL_ADDRESS);
    if (null == emailAddresses) {
      return null;
    }
    return emailAddresses[0];
  }

  /**
   *
   * @param inEmail
   *          the email to set
   */
  public void setAlertsEmailAddress(String inEmail) {
    userPrefs.put(PlosOneUser.ALERTS_EMAIL_ADDRESS, new String[] { inEmail });
  }

  /**
   * @return givennames givenNames
   */
  public String getGivenNames() {
    return getNonNull(userProfile.getGivenNames());
  }

  /**
   * @param givenNames givenNames
   */
  public void setGivenNames(final String givenNames) {
    userProfile.setGivenNames(givenNames);
  }

  /**
   * @return positionType positionType
   */
  public String getPositionType() {
    return getNonNull(userProfile.getPositionType());
  }

  /**
   * @param surnames surnames
   */
  public void setSurnames(final String surnames) {
    userProfile.setSurnames(surnames);
  }

  /**
   * @return surnames surnames
   */
  public String getSurnames() {
    return getNonNull(userProfile.getSurnames());
  }

  /**
   * @param positionType positionType
   */
  public void setPositionType(final String positionType) {
    userProfile.setPositionType(positionType);
  }

  /**
   * @return organizationType organizationType
   */
  public String getOrganizationType() {
    return getNonNull(userProfile.getOrganizationType());
  }

  /**
   * @param organizationType organizationType
   */
  public void setOrganizationType(final String organizationType) {
    userProfile.setOrganizationType(organizationType);
  }

  /**
   * @return organizationName organizationName
   */
  public String getOrganizationName() {
    return getNonNull(userProfile.getOrganizationName());
  }

  /**
   * @param organizationName organizationName
   */
  public void setOrganizationName(final String organizationName) {
    userProfile.setOrganizationName(organizationName);
  }

  /**
   * @return postalAddress postalAddress
   */
  public String getPostalAddress() {
    return getNonNull(userProfile.getPostalAddress());
  }

  /**
   * @param postalAddress postalAddress
   */
  public void setPostalAddress(final String postalAddress) {
    userProfile.setPostalAddress(postalAddress);
  }

  /**
   * @return biographyText biographyText
   */
  public String getBiographyText() {
    return getNonNull(userProfile.getBiographyText());
  }

  /**
   * @param biographyText biographyText
   */
  public void setBiographyText(final String biographyText) {
    userProfile.setBiographyText(biographyText);
  }

  /**
   * @return interestsText interestsText
   */
  public String getInterestsText() {
    return getNonNull(userProfile.getInterestsText());
  }

  /**
   * @param interestsText interestsText
   */
  public void setInterestsText(final String interestsText) {
    userProfile.setInterestsText(interestsText);
  }

  /**
   * @return researchAreasText researchAreasText
   */
  public String getResearchAreasText() {
    return getNonNull(userProfile.getResearchAreasText());
  }

  /**
   * @param researchAreasText researchAreasText
   */
  public void setResearchAreasText(final String researchAreasText) {
    userProfile.setResearchAreasText(researchAreasText);
  }

  private String getNonNull(final String value) {
    return null == value ? StringUtils.EMPTY : value;
  }

}