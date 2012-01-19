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

import org.apache.struts2.ServletActionContext;

import static org.plos.Constants.PLOS_ONE_USER_KEY;
import org.plos.models.UserAccount;
import org.plos.models.UserPreferences;
import org.plos.models.UserProfile;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to roll up access for a user into a PLoS ONE appplication specific object.
 * @author Stephen Cheng
 */

public class PlosOneUser {
  private static final Log log = LogFactory.getLog(PlosOneUser.class);
  // Constants for application specific User Preferences
  private static final String ALERTS_CATEGORIES = "alertsJournals";

  /** the current user-id */
  private String      userId;

  /** authID for the user in this app */
  private String      authId;

  /** authID for the user in this app */
  private UserProfile userProfile;

  /** the user-preferences as a map */
  private Map<String, String[]> userPrefs;


  /**
   * Returns the current user. Valid only in a request context.
   */
  public static PlosOneUser getCurrentUser() {
    if (ServletActionContext.getRequest() == null)
      return null;
    if (ServletActionContext.getRequest().getSession() == null)
      return null;
    return (PlosOneUser) ServletActionContext.getRequest().getSession()
                                                              .getAttribute(PLOS_ONE_USER_KEY);
  }

  /**
   * Initializes a new PLoS ONE user
   *
   * @param ua the user-account
   */
  public PlosOneUser(UserAccount ua, String appId, UsersPEP pep) {
    this.userId   = ua.getId().toString();
    this.authId   = ua.getAuthIds().iterator().next().getValue();

    userProfile = ua.getProfile();
    if (userProfile == null)
      userProfile = new UserProfile();
    else
      filterProfile(userProfile, ua.getId(), pep);

    UserPreferences p;
    try {
      pep.checkAccess(pep.GET_PREFERENCES, ua.getId());
      p = ua.getPreferences(appId);
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("get-preferences was disallowed on '" + ua.getId() + "'", se);
      p = null;
    }
    if (p != null)
      userPrefs = p.getPrefsAsMap();
    else
      userPrefs = new HashMap<String, String[]>();
  }

  /**
   * Initializes a new PLoS ONE user and sets the authentication ID
   *
   * @param authId authentication ID of new user
   */
  public PlosOneUser(String authId) {
    this.userId = null;
    this.authId = authId;

    userProfile = new UserProfile();
    userPrefs   = new HashMap<String, String[]>();
  }

  /**
   * Initializes a new PLoS ONE user from another one.
   *
   * @param pou the other user-object
   */
  protected PlosOneUser(PlosOneUser pou) {
    userId      = pou.userId;
    authId      = pou.authId;
    userProfile = pou.userProfile;
    userPrefs   = pou.userPrefs;
  }

  private static void filterProfile(UserProfile prof, URI owner, UsersPEP pep) {
    if (prof.getDisplayName() != null && !checkAccess(owner, pep.GET_DISP_NAME, pep))
      prof.setDisplayName(null);
    if (prof.getRealName() != null && !checkAccess(owner, pep.GET_REAL_NAME, pep))
      prof.setRealName(null);
    if (prof.getGivenNames() != null && !checkAccess(owner, pep.GET_GIVEN_NAMES, pep))
      prof.setGivenNames(null);
    if (prof.getSurnames() != null && !checkAccess(owner, pep.GET_SURNAMES, pep))
      prof.setSurnames(null);
    if (prof.getTitle() != null && !checkAccess(owner, pep.GET_TITLE, pep))
      prof.setTitle(null);
    if (prof.getGender() != null && !checkAccess(owner, pep.GET_GENDER, pep))
      prof.setGender(null);
    if (prof.getPositionType() != null && !checkAccess(owner, pep.GET_POSITION_TYPE, pep))
      prof.setPositionType(null);
    if (prof.getOrganizationName() != null && !checkAccess(owner, pep.GET_ORGANIZATION_NAME, pep))
      prof.setOrganizationName(null);
    if (prof.getOrganizationType() != null && !checkAccess(owner, pep.GET_ORGANIZATION_TYPE, pep))
      prof.setOrganizationType(null);
    if (prof.getPostalAddress() != null && !checkAccess(owner, pep.GET_POSTAL_ADDRESS, pep))
      prof.setPostalAddress(null);
    if (prof.getCity() != null && !checkAccess(owner, pep.GET_CITY, pep))
      prof.setCity(null);
    if (prof.getCountry() != null && !checkAccess(owner, pep.GET_COUNTRY, pep))
      prof.setCountry(null);
    if (prof.getEmail() != null && !checkAccess(owner, pep.GET_EMAIL, pep))
      prof.setEmail(null);
    if (prof.getHomePage() != null && !checkAccess(owner, pep.GET_HOME_PAGE, pep))
      prof.setHomePage(null);
    if (prof.getWeblog() != null && !checkAccess(owner, pep.GET_WEBLOG, pep))
      prof.setWeblog(null);
    if (prof.getBiography() != null && !checkAccess(owner, pep.GET_BIOGRAPHY, pep))
      prof.setBiography(null);
    if (prof.getInterests() != null && !checkAccess(owner, pep.GET_INTERESTS, pep))
      prof.setInterests(null);
    if (prof.getPublications() != null && !checkAccess(owner, pep.GET_PUBLICATIONS, pep))
      prof.setPublications(null);
    if (prof.getBiographyText() != null && !checkAccess(owner, pep.GET_BIOGRAPHY_TEXT, pep))
      prof.setBiographyText(null);
    if (prof.getInterestsText() != null && !checkAccess(owner, pep.GET_INTERESTS_TEXT, pep))
      prof.setInterestsText(null);
    if (prof.getResearchAreasText() != null && !checkAccess(owner, pep.GET_RESEARCH_AREAS_TEXT, pep))
      prof.setResearchAreasText(null);
  }

  private static boolean checkAccess(URI owner, String perm, UsersPEP pep) {
    try {
      pep.checkAccess(perm, owner);
      return true;
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("access '" + perm + "' to '" + owner + "'s profile denied", se);
      return false;
    }
  }

  /**
   * Method to check to see if this was a migrated PLoS user or not.
   *
   * @return Returns true if the user is initialized (i.e. has chosen a username)
   */
  public boolean isInitialized() {
    return (userProfile.getDisplayName() != null);
  }

  /**
   * @return Returns the user profile.
   */
  public UserProfile getUserProfile() {
    return userProfile;
  }

  /**
   * @param p the user preferences to fill in.
   */
  public void getUserPrefs(UserPreferences p) {
    p.setPrefsFromMap(userPrefs);
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
    return getNonNull(userProfile.getDisplayName());
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
    return getNonNull(userProfile.getEmailAsString());
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    userProfile.setEmailFromString(email);
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
    userProfile.setHomePage(homePage != null ? URI.create(homePage) : null);
  }

  /**
   * @return Returns the interests.
   */
  public String[] getInterests() {
    Set<URI> i = userProfile.getInterests();

    String[] res = new String[i.size()];
    int idx = 0;
    for (URI uri : i)
      res[idx++] = uri.toString();

    return res;
  }

  /**
   * @param interests
   *          The interests to set.
   */
  public void setInterests(String[] interests) {
    Set<URI> i = userProfile.getInterests();
    i.clear();
    for (String interest : interests) {
      if (interest != null)
        i.add(URI.create(interest));
    }
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
    userProfile.setPublications(publications != null ? URI.create(publications) : null);
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
    userProfile.setWeblog(weblog != null ? URI.create(weblog) : null);
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
    return userPrefs.get(PlosOneUser.ALERTS_CATEGORIES);
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

  private String getNonNull(final URI value) {
    return null == value ? StringUtils.EMPTY : value.toString();
  }
}
