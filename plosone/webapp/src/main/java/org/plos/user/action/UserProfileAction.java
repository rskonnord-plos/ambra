/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.user.action;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import static org.plos.Constants.Length;
import static org.plos.Constants.ReturnCode.NEW_PROFILE;
import static org.plos.Constants.ReturnCode.UPDATE_PROFILE;
import static org.plos.Constants.SINGLE_SIGNON_EMAIL_KEY;
import org.plos.user.PlosOneUser;
import org.plos.user.UserAccountsInterceptor;
import org.plos.user.UserProfileGrant;
import org.plos.user.service.DisplayNameAlreadyExistsException;
import org.plos.util.FileUtils;
import org.plos.util.ProfanityCheckingService;
import org.plos.util.TextUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Creates a new user in Topaz and sets come Profile properties.  User must be logged in via CAS.
 * 
 * @author Stephen Cheng
 * 
 */
public abstract class UserProfileAction extends UserActionSupport {

  private String displayName, email, realName, topazId;
  private String authId;

  private static final Log log = LogFactory.getLog(UserProfileAction.class);
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
  private String nameVisibility = PUBLIC;
  private String extendedVisibility;
  private String orgVisibility;
  private ProfanityCheckingService profanityCheckingService;

  public static final String PRIVATE = "private";
  public static final String PUBLIC = "public";
  private static final Map<String, String[]> visibilityMapping = new HashMap<String, String[]>();

  private static final String GIVEN_NAMES = UserProfileGrant.GIVENNAMES.getFieldName();
  private static final String REAL_NAME = UserProfileGrant.REAL_NAME.getFieldName();
  private static final String POSTAL_ADDRESS = UserProfileGrant.POSTAL_ADDRESS.getFieldName();
  private static final String ORGANIZATION_TYPE = UserProfileGrant.ORGANIZATION_TYPE.getFieldName();
  private static final String ORGANIZATION_NAME = UserProfileGrant.ORGANIZATION_NAME.getFieldName();
  private static final String TITLE = UserProfileGrant.TITLE.getFieldName();
  private static final String POSITION_TYPE = UserProfileGrant.POSITION_TYPE.getFieldName();
  private static final String DISPLAY_NAME = UserProfileGrant.DISPLAY_NAME.getFieldName();
  private static final String SURNAMES = UserProfileGrant.SURNAMES.getFieldName();
  private static final String CITY = UserProfileGrant.CITY.getFieldName();
  private static final String COUNTRY = UserProfileGrant.COUNTRY.getFieldName();
  private static final String BIOGRAPHY_TEXT = UserProfileGrant.BIOGRAPHY_TEXT.getFieldName();
  private static final String INTERESTS_TEXT = UserProfileGrant.INTERESTS_TEXT.getFieldName();
  private static final String RESEARCH_AREAS_TEXT = UserProfileGrant.RESEARCH_AREAS_TEXT.getFieldName();
  private static final String HOME_PAGE = "homePage";
  private static final String WEBLOG = "weblog";

  private static final String NAME_GROUP = "name";
  private static final String EXTENDED_GROUP = "extended";
  private static final String ORG_GROUP = "org";
  private static final String HTTP_PREFIX = "http://";
  private static final Pattern spacePattern = Pattern.compile("[\\p{L}\\p{N}\\p{Pc}\\p{Pd}]*");
  private boolean isDisplayNameSet = true;
  private boolean displayNameRequired = true;

  static {
    visibilityMapping.put(NAME_GROUP,
                          new String[]{
                                  GIVEN_NAMES,
                                  DISPLAY_NAME,
                                  SURNAMES,
                                  CITY,
                                  COUNTRY});
    visibilityMapping.put(EXTENDED_GROUP,
                          new String[]{
                                  POSTAL_ADDRESS});
    visibilityMapping.put(ORG_GROUP,
                          new String[]{
                                  ORGANIZATION_TYPE,
                                  ORGANIZATION_NAME,
                                  TITLE,
                                  POSITION_TYPE});
  }

  private static final Map fieldToUINameMapping = new HashMap<String, String>();
  private PlosOneUser newUser;

  static {
    fieldToUINameMapping.put(DISPLAY_NAME, "Username");
    fieldToUINameMapping.put(GIVEN_NAMES, "First/Given Name");
    fieldToUINameMapping.put(SURNAMES, "Last/Family Name");
    fieldToUINameMapping.put(CITY, "City");
    fieldToUINameMapping.put(COUNTRY, "Country");
    fieldToUINameMapping.put(POSTAL_ADDRESS, "Address");
    fieldToUINameMapping.put(ORGANIZATION_TYPE, "Organization Type");
    fieldToUINameMapping.put(ORGANIZATION_NAME, "Organization Name");
    fieldToUINameMapping.put(TITLE, "Title");
    fieldToUINameMapping.put(POSITION_TYPE, "Position Type");
    fieldToUINameMapping.put(BIOGRAPHY_TEXT, "About Me");
    fieldToUINameMapping.put(RESEARCH_AREAS_TEXT, "Research Areas");
    fieldToUINameMapping.put(INTERESTS_TEXT, "Interests");
    fieldToUINameMapping.put(HOME_PAGE, "Home page");
    fieldToUINameMapping.put(WEBLOG, "Weblog");
  }

  /**
   * Will take the CAS ID and create a user in Topaz associated with that auth ID. If auth ID
   * already exists, it will not create another user. Email and Username are required and the
   * profile will be updated.
   * 
   * @return status code for webwork
   */
  public String executeSaveUser() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserToUse();
    //if new user then capture the displayname or if display name is blank (when user has been migrated)
    if ((null == plosOneUser) || (displayNameRequired && StringUtils.isBlank(plosOneUser.getDisplayName()))) {
        isDisplayNameSet = false;
    }

    if (!validates()) {
      email = fetchUserEmailAddress();

      if (log.isDebugEnabled()) {
        log.debug("Topaz ID: " + topazId + " with authID: " + authId + " did not validate");
      }
      return INPUT;
    }

    authId = getUserIdToFetchEmailAddressFor();

    topazId = getUserService().lookUpUserByAuthId(authId);
    if (topazId == null) {
      topazId = getUserService().createUser(authId);

      // update the user-id in the session if we just created an account for ourselves
      Map<String, Object> session = getSessionMap();
      if (authId != null && authId.equals(session.get(UserAccountsInterceptor.AUTH_KEY)))
        session.put(UserAccountsInterceptor.USER_KEY, topazId);
    }

    newUser = createPlosOneUser();
    //if new or migrated user then capture the displayname
    if (!isDisplayNameSet) {
      newUser.setDisplayName(this.displayName);
    }

    if (log.isDebugEnabled()) {
      log.debug("Topaz ID: " + topazId + " with authID: " + authId);
    }

    try {
      //If a field is not among the private fields, it is saved as public
      getUserService().setProfile(newUser, getPrivateFields(), displayNameRequired);
    } catch (DisplayNameAlreadyExistsException ex) {
      email = fetchUserEmailAddress();
      newUser.setDisplayName(StringUtils.EMPTY);  //Empty out the display name from the newUser object
      isDisplayNameSet = false;
      addErrorForField(DISPLAY_NAME, "is already in use. Please select a different username");
      return INPUT;
    }

    isDisplayNameSet = true;
    return SUCCESS;
  }

  /**
   * Getter for newUser.
   * @return Value of newUser.
   */
  protected PlosOneUser getSavedPlosOneUser() {
    return newUser;
  }

  private String makeValidUrl(final String url) throws MalformedURLException {
    final String newUrl = StringUtils.stripToEmpty(url);
    if (StringUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase(HTTP_PREFIX)) {
      return StringUtils.EMPTY;
    }
    return TextUtils.makeValidUrl(newUrl);
  }

  public String executeRetrieveUserProfile() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserToUse();
    assignUserFields (plosOneUser);

    final Collection<UserProfileGrant> grants = getUserService().getProfileFieldsThatArePrivate(topazId);
    setVisibility(grants);

    return SUCCESS;
  }

  private void assignUserFields (PlosOneUser plosOneUser){
    authId = plosOneUser.getAuthId();
    topazId = plosOneUser.getUserId();
    email = plosOneUser.getEmail();
    displayName = plosOneUser.getDisplayName();
    realName = plosOneUser.getRealName();
    givenNames = plosOneUser.getGivenNames();
    surnames = plosOneUser.getSurnames();
    title = plosOneUser.getTitle();
    positionType = plosOneUser.getPositionType();
    organizationType = plosOneUser.getOrganizationType();
    organizationName = plosOneUser.getOrganizationName();
    postalAddress = plosOneUser.getPostalAddress();
    biographyText = plosOneUser.getBiographyText();
    interestsText = plosOneUser.getInterestsText();
    researchAreasText = plosOneUser.getResearchAreasText();
    homePage = plosOneUser.getHomePage();
    weblog = plosOneUser.getWeblog();
    city = plosOneUser.getCity();
    country = plosOneUser.getCountry();
  }


  /**
   * Prepopulate the user profile data as available
   * @return return code for webwork
   * @throws Exception Exception
   */
  public String prePopulateUserDetails() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserToUse();

    isDisplayNameSet = false;
    //If the user has no topaz id
    if (null == plosOneUser) {
      email = fetchUserEmailAddress();
      log.debug("new profile with email: " + email);
      return NEW_PROFILE;
    } else if (StringUtils.isBlank(plosOneUser.getDisplayName())) {
      // if the user has no display name, possibly getting migrated from an old system
      try {
        log.debug("this is an existing user with email: " + plosOneUser.getEmail());

        assignUserFields (plosOneUser);
      } catch(NullPointerException  ex) {
        //fetching email in the case where profile creation failed and so email did not get saved.
        //Will not be needed when all the user accounts with a profile have a email set up
        //This is to display the email address to the user on the profile page, not required for saving
        email = fetchUserEmailAddress();
        if (log.isDebugEnabled()) {
          log.debug("Profile was not found, so creating one for user with email:" + email);
        }
      }
      return UPDATE_PROFILE;
    }
    //else just forward the user to a success page, which might be the home page.
    isDisplayNameSet = true;
    return SUCCESS;
  }

  private PlosOneUser createPlosOneUser() throws Exception {
    PlosOneUser plosOneUser = getPlosOneUserToUse();
    if (null == plosOneUser || StringUtils.isEmpty(plosOneUser.getEmail())) {
      if (plosOneUser == null) {
        plosOneUser = new PlosOneUser(this.authId);
      }
      //Set the email address if the email address did not get saved during profile creation
      plosOneUser.setEmail(fetchUserEmailAddress());
    }

    plosOneUser.setUserId(this.topazId);
    plosOneUser.setRealName(this.realName);
    plosOneUser.setTitle(this.title);
    plosOneUser.setSurnames(this.surnames);
    plosOneUser.setGivenNames(this.givenNames);
    plosOneUser.setPositionType(this.positionType);
    plosOneUser.setOrganizationType(this.organizationType);
    plosOneUser.setOrganizationName(this.organizationName);
    plosOneUser.setPostalAddress(this.postalAddress);
    plosOneUser.setBiographyText(this.biographyText);
    plosOneUser.setInterestsText(this.interestsText);
    plosOneUser.setResearchAreasText(this.researchAreasText);
    final String homePageUrl = StringUtils.stripToNull(makeValidUrl(homePage));
    plosOneUser.setHomePage(homePageUrl);
    final String weblogUrl = StringUtils.stripToNull(makeValidUrl(weblog));
    plosOneUser.setWeblog(weblogUrl);
    plosOneUser.setCity(this.city);
    plosOneUser.setCountry(this.country);
    return plosOneUser;
  }

  /**
   * Provides a way to get the PlosOneUser to edit
   * @return the PlosOneUser to edit
   * @throws org.plos.ApplicationException ApplicationException
   */
  protected abstract PlosOneUser getPlosOneUserToUse() throws ApplicationException;

  public String getEmail() {
    return email;
  }

  /**
   * @param email
   *          The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return Returns the realName.
   */
  public String getRealName() {
    return realName;
  }

  /**
   * @param realName
   *          The firstName to set.
   */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  private boolean validates() {
    boolean isValid = true;

    //Don't validate displayName if the user already has a display name
    if (!isDisplayNameSet && displayNameRequired) {
      if (spacePattern.matcher(displayName).matches()) {
        final int usernameLength = displayName.length();
        if (usernameLength < Integer.parseInt(Length.DISPLAY_NAME_MIN)
              || usernameLength > Integer.parseInt(Length.DISPLAY_NAME_MAX)) {
          addErrorForField(DISPLAY_NAME, "must be between " + Length.DISPLAY_NAME_MIN + " and " + Length.DISPLAY_NAME_MAX + " characters");
          isValid = false;
        }
      } else {
        addErrorForField(DISPLAY_NAME, "may only contain letters, numbers, dashes, and underscores");
        isValid = false;
      }
    }
    if (StringUtils.isBlank(givenNames)) {
      addErrorForField(GIVEN_NAMES, "cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(surnames)) {
      addErrorForField(SURNAMES, "cannot be empty");
      isValid = false;
    }
    //TODO: does everyone live in a city?
    if (StringUtils.isBlank(city)) {
      addErrorForField(CITY, "cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(country)) {
      addErrorForField(COUNTRY, "cannot be empty");
      isValid = false;
    }
    if (isInvalidUrl(homePage)) {
      addErrorForField(HOME_PAGE, "URL is not valid");
      isValid = false;
    }
    if (isInvalidUrl(weblog)) {
      addErrorForField(WEBLOG, "URL is not valid");
      isValid = false;
    }

    if (profanityCheckFailed()) {
      isValid = false;
    }

    return isValid;
  }

  private void addErrorForField(final String fieldName, final String messageSuffix) {
    addFieldError(fieldName, fieldToUINameMapping.get(fieldName) +  " " + messageSuffix);
  }

  private boolean isInvalidUrl(final String url) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }
    return !TextUtils.verifyUrl(url) && !TextUtils.verifyUrl(HTTP_PREFIX + url);
  }

  private boolean profanityCheckFailed() {
    boolean isProfane = false;
    final String[][] fieldsToValidate = getFieldMappings();

    for (final String[] field : fieldsToValidate) {
      if (isProfane(field[0], field[1])) {
        isProfane = true;
      }
    }

    return isProfane;
  }

  private String[][] getFieldMappings() {
    final String[][] toValidateArray = new String[][]{
            {DISPLAY_NAME, displayName},
            {REAL_NAME, realName},
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
    return toValidateArray;
  }

  private boolean isProfane(final String fieldName, final String fieldValue) {
    final List<String> profaneWords = profanityCheckingService.validate(fieldValue);
    if (profaneWords.size() > 0 ) {
      addProfaneMessages(profaneWords, fieldName, (String) fieldToUINameMapping.get(fieldName));
      return true;
    }
    return false;
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName
   *          The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return Returns the topazId.
   */
  public String getInternalId() {
    return topazId;
  }

  /**
   * @param internalId
   *          The topazId to set.
   */
  public void setInternalId(String internalId) {
    this.topazId = internalId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise
   * 
   * @return Returns the authId.
   */
  protected String getAuthId() {
    return authId;
  }

  /**
   * Here mainly for unit tests. Should not need to be used otherwise. Action picks it up from
   * session automatically.
   * 
   * @param authId
   *          The authId to set.
   */
  protected void setAuthId(String authId) {
    this.authId = authId;
  }


  /**
   * Getter for property 'biographyText'.
   *
   * @return Value for property 'biographyText'.
   */
  public String getBiographyText() {
    return biographyText;
  }

  /**
   * Setter for property 'biographyText'.
   *
   * @param biographyText Value to set for property 'biographyText'.
   */
  public void setBiographyText(final String biographyText) {
    this.biographyText = biographyText;
  }

  /**
   * Getter for property 'city'.
   *
   * @return Value for property 'city'.
   */
  public String getCity() {
    return city;
  }

  /**
   * Setter for property 'city'.
   *
   * @param city Value to set for property 'city'.
   */
  public void setCity(final String city) {
    this.city = city;
  }

  /**
   * Getter for property 'country'.
   *
   * @return Value for property 'country'.
   */
  public String getCountry() {
    return country;
  }

  /**
   * Setter for property 'country'.
   *
   * @param country Value to set for property 'country'.
   */
  public void setCountry(final String country) {
    this.country = country;
  }

  /**
   * Getter for property 'givennames'.
   *
   * @return Value for property 'givennames'.
   */
  public String getGivenNames() {
    return givenNames;
  }

  /**
   * Setter for property 'givenNames'.
   *
   * @param givenNames Value to set for property 'givenNames'.
   */
  public void setGivenNames(final String givenNames) {
    this.givenNames = givenNames;
  }

  /**
   * Getter for property 'interestsText'.
   *
   * @return Value for property 'interestsText'.
   */
  public String getInterestsText() {
    return interestsText;
  }

  /**
   * Setter for property 'interestsText'.
   *
   * @param interestsText Value to set for property 'interestsText'.
   */
  public void setInterestsText(final String interestsText) {
    this.interestsText = interestsText;
  }

  /**
   * Getter for property 'organizationType'.
   *
   * @return Value for property 'organizationType'.
   */
  public String getOrganizationType() {
    return organizationType;
  }

  /**
   * Setter for property 'organizationType'.
   *
   * @param organizationType Value to set for property 'organizationType'.
   */
  public void setOrganizationType(final String organizationType) {
    this.organizationType = organizationType;
  }

  /**
   * Getter for property 'positionType'.
   *
   * @return Value for property 'positionType'.
   */
  public String getPositionType() {
    return positionType;
  }

  /**
   * Setter for property 'positionType'.
   *
   * @param positionType Value to set for property 'positionType'.
   */
  public void setPositionType(final String positionType) {
    this.positionType = positionType;
  }

  /**
   * Getter for property 'postalAddress'.
   *
   * @return Value for property 'postalAddress'.
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * Setter for property 'postalAddress'.
   *
   * @param postalAddress Value to set for property 'postalAddress'.
   */
  public void setPostalAddress(final String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * Getter for property 'researchAreasText'.
   *
   * @return Value for property 'researchAreasText'.
   */
  public String getResearchAreasText() {
    return researchAreasText;
  }

  /**
   * Setter for property 'researchAreasText'.
   *
   * @param researchAreasText Value to set for property 'researchAreasText'.
   */
  public void setResearchAreasText(final String researchAreasText) {
    this.researchAreasText = researchAreasText;
  }

  /**
   * Getter for property 'topazId'.
   *
   * @return Value for property 'topazId'.
   */
  public String getTopazId() {
    return topazId;
  }

  /**
   * Setter for property 'topazId'.
   *
   * @param topazId Value to set for property 'topazId'.
   */
  public void setTopazId(final String topazId) {
    this.topazId = topazId;
  }

  private void setVisibility(final Collection<UserProfileGrant> grants) {
    final String[] privateFields = new String[grants.size()];
    int i = 0;
    for (final UserProfileGrant grant : grants) {
      privateFields[i++] = grant.getFieldName();
    }

    nameVisibility = setFieldVisibility(privateFields, GIVEN_NAMES);
    extendedVisibility = setFieldVisibility(privateFields, POSTAL_ADDRESS);
    orgVisibility = setFieldVisibility(privateFields, ORGANIZATION_TYPE);
  }

  private String setFieldVisibility(final String[] privateFields, final String fieldName) {
    return ArrayUtils.contains(privateFields, fieldName) ? PRIVATE : PUBLIC;
  }

  /**
   * Get the private fields
   * @return the private fields
   */
  private String[] getPrivateFields() {
    final Collection<String> privateFieldsList = new ArrayList<String>();
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(nameVisibility, NAME_GROUP));
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(extendedVisibility, EXTENDED_GROUP));
    CollectionUtils.addAll(privateFieldsList, getRespectiveFields(orgVisibility, ORG_GROUP));

    //Add email as a private field.
    CollectionUtils.addAll(privateFieldsList, new String[]{UserProfileGrant.EMAIL.getFieldName()});

    return privateFieldsList.toArray(new String[privateFieldsList.size()]);
  }

  private String[] getRespectiveFields(final String fieldVisibilityToCheck, final String key) {
    if(StringUtils.stripToEmpty(fieldVisibilityToCheck).equalsIgnoreCase(PRIVATE)) {
      return visibilityMapping.get(key);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * Getter for property 'organizationName'.
   * @return Value for property 'organizationName'.
   */
  public String getOrganizationName() {
    return organizationName;
  }

  /**
   * Setter for property 'organizationName'.
   * @param organizationName Value to set for property 'organizationName'.
   */
  public void setOrganizationName(final String organizationName) {
    this.organizationName = organizationName;
  }

  /**
   * Getter for property 'surnames'.
   * @return Value for property 'surnames'.
   */
  public String getSurnames() {
    return surnames;
  }

  /**
   * Setter for property 'surnames'.
   * @param surnames Value to set for property 'surnames'.
   */
  public void setSurnames(final String surnames) {
    this.surnames = surnames;
  }

  /**
   * Getter for title.
   * @return Value of title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Setter for title.
   * @param title Value to set for title.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Getter for extendedVisibility.
   * @return Value of extendedVisibility.
   */
  public String getExtendedVisibility() {
    return getVisibility(extendedVisibility);
  }

  /**
   * Setter for extendedVisibility.
   * @param extendedVisibility Value to set for extendedVisibility.
   */
  public void setExtendedVisibility(final String extendedVisibility) {
    this.extendedVisibility = extendedVisibility;
  }

  /**
   * Getter for nameVisibility.
   * @return Value of nameVisibility.
   */
  public String getNameVisibility() {
    return getVisibility(nameVisibility);
  }

  /**
   * Setter for nameVisibility.
   * @param nameVisibility Value to set for nameVisibility.
   */
  private void setNameVisibility(final String nameVisibility) {
    this.nameVisibility = nameVisibility;
  }

  /**
   * Getter for orgVisibility.
   * @return Value of orgVisibility.
   */
  public String getOrgVisibility() {
    return getVisibility(orgVisibility);
  }

  /**
   * Setter for orgVisibility.
   * @param orgVisibility Value to set for orgVisibility.
   */
  public void setOrgVisibility(final String orgVisibility) {
    this.orgVisibility = orgVisibility;
  }

  private String getVisibility(final String visibility) {
    return StringUtils.isBlank(visibility) ? PUBLIC : visibility;
  }

  /**
   * Getter for homePage.
   * @return Value of homePage.
   */
  public String getHomePage() {
    return homePage;
  }

  /**
   * Setter for homePage.
   * @param homePage Value to set for homePage.
   */
  public void setHomePage(final String homePage) {
    this.homePage = homePage.trim();
  }

  /**
   * Getter for weblog.
   * @return Value of weblog.
   */
  public String getWeblog() {
    return weblog;
  }

  /**
   * Setter for weblog.
   * @param weblog Value to set for weblog.
   */
  public void setWeblog(final String weblog) {
    this.weblog = weblog.trim();
  }

  /**
   * Getter for profanityCheckingService.
   * @return Value of profanityCheckingService.
   */
  public ProfanityCheckingService getProfanityCheckingService() {
    return profanityCheckingService;
  }

  /**
   * Setter for profanityCheckingService.
   * @param profanityCheckingService Value to set for profanityCheckingService.
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  /**
   * Getter for isDisplayNameSet.
   * @return Value of isDisplayNameSet.
   */
  protected void setIsDisplayNameSet(final boolean inIsDisplayNameSet) {
    isDisplayNameSet = inIsDisplayNameSet;
  }

  /**
   * Getter for isDisplayNameSet.
   * @return Value of isDisplayNameSet.
   */
  public boolean getIsDisplayNameSet() {
    return isDisplayNameSet;
  }

  /**
   * Get the user id(guid) of the user for which we want to get the email address for.
   * @return user id
   */
  protected abstract String getUserIdToFetchEmailAddressFor() throws ApplicationException;

  protected String fetchUserEmailAddress() throws ApplicationException {
    String presetEmail = (String) getSessionMap().get(SINGLE_SIGNON_EMAIL_KEY);
    if (presetEmail != null)
      return presetEmail;

    final String emailAddressUrl = getEmailAddressUrl();
    final String userId = getUserIdToFetchEmailAddressFor();
    final String url = emailAddressUrl + userId;
    try {
      return FileUtils.getTextFromUrl(url);
    } catch (IOException ex) {
      final String errorMessage = "Failed to fetch the email address using the url:" + url;
      log.error(errorMessage, ex);
      throw new ApplicationException(errorMessage, ex);
    }
  }

  private String getEmailAddressUrl() {
    return getUserService().getEmailAddressUrl();
  }

  /**
   * Setter for displayNameRequired. To be used only if the display name is not required to be set,
   * for example when the administrator is creating the account for a user.
   * @param displayNameRequired Value to set for displayNameRequired.
   */
  public void setDisplayNameRequired(final boolean displayNameRequired) {
    this.displayNameRequired = displayNameRequired;
  }
}
