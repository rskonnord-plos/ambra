/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.service;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Ehcache;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.models.AuthenticationId;
import org.plos.models.UserAccount;
import org.plos.models.UserPreferences;
import org.plos.models.UserRole;
import org.plos.permission.service.PermissionWebService;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;
import org.plos.user.UsersPEP;
import org.plos.web.UserContext;

import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.spring.OtmTransactionManager;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.plos.util.CacheAdminHelper;

/**
 * Class to roll up web services that a user needs in PLoS ONE. Rest of application should generally
 * use PlosOneUser to
 *
 * @author Stephen Cheng
 *
 */
public class UserService {
  private static final Log      log = LogFactory.getLog(UserService.class);
  private static final String[] allUserProfileFieldGrants = getAllUserProfileFieldGrants();

  private Session               session;
  private OtmTransactionManager txManager;

  private static final String USER_LOCK = "UserCache-Lock-";
  private static final String USER_KEY  = "UserCache-User-";

  private final UsersPEP pep;

  private PermissionWebService permissionWebService;
  private Ehcache userCache;

  private String applicationId;
  private String emailAddressUrl;

  private final String[] ALL_PRINCIPALS = new String[] { Constants.Permission.ALL_PRINCIPALS };
  private Collection<String> weeklyCategories;
  private Collection<String> monthlyCategories;
  private Map<String, String> categoryNames;
  private UserContext userContext;

  public UserService() throws IOException {
    pep = new UsersPEP();
  }

  /**
   * Create a new user account and associate a single authentication id with it.
   *
   * @param authId the user's authentication id from CAS
   * @return the user's internal id
   * @throws ApplicationException if the <var>authId</var> is a duplicate
   */
  public String createUser(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.CREATE_USER, pep.ANY_RESOURCE);

    // create account
    UserAccount ua = new UserAccount();
    ua.getAuthIds().add(new AuthenticationId(authId));
    session.saveOrUpdate(ua);

    // check for duplicate. Tx hack alert!!!
    txManager.doCommit(
        (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus());
    txManager.doBegin(txManager.doGetTransaction(), null);

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();
    if (!r.next() || !r.next())
      return ua.getId().toString();

    session.delete(ua);
    txManager.doCommit(
        (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus());
    txManager.doBegin(txManager.doGetTransaction(), null);

    throw new ApplicationException("AuthId '" + authId + "' is already in use");
  }

  /**
   * Deletes the given user from Topaz. Visible for testing only.
   *
   * @param topazUserId
   *          the Topaz User ID
   * @throws ApplicationException ApplicationException
   */
  public void deleteUser(final String topazUserId) throws ApplicationException {
    pep.checkAccessAE(pep.DELETE_USER, URI.create(topazUserId));

    UserAccount ua = session.get(UserAccount.class, topazUserId);
    if (ua != null)
      session.delete(ua);
  }

  /**
   * Get the account info for the given user.
   * @param topazUserId the Topaz User ID
   * @throws ApplicationException ApplicationException
   */
  private UserAccount getUserAccount(final String topazUserId) throws ApplicationException {
    UserAccount ua = session.get(UserAccount.class, topazUserId);
    if (ua == null)
      throw new ApplicationException("No user-account with id '" + topazUserId + "' found");
    return ua;
  }

  /**
   * Returns the username for a user given a Topaz UserId.  Because usernames cannot be changed and
   * can always be viewed by anyone, we use a simple cache here.
   *
   * @param topazUserId topazUserId
   * @return username username
   * @throws ApplicationException ApplicationException
   */
  public String getUsernameByTopazId(final String topazUserId) throws ApplicationException {

    final Object lock = (USER_LOCK + topazUserId).intern();

    return CacheAdminHelper.getFromCacheE(userCache, USER_KEY + topazUserId, -1, lock, "userName",
                               new CacheAdminHelper.EhcacheUpdaterE<String, ApplicationException>() {
      public String lookup() throws ApplicationException {
        return getDisplayName(topazUserId);
      }
    });
  }

  private String getDisplayName(final String topazUserId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_DISP_NAME, URI.create(topazUserId));

    Results r = session.
        createQuery("select ua.profile.displayName from UserAccount ua where ua = :id;").
        setParameter("id", topazUserId).execute();
    if (!r.next())
      throw new ApplicationException("No user-account with id '" + topazUserId + "' found");
    return r.getString(0);
  }

  /**
   * Gets the user specified by the Topaz userID passed in
   *
   * @param topazUserId Topaz User ID
   * @return user associated with the topazUserId
   * @throws ApplicationException on access-check failure
   */
  public PlosOneUser getUserByTopazId(final String topazUserId) throws ApplicationException {
    return getUserWithProfileLoaded(topazUserId);
  }

  /**
   * Get the PlosOneUser with only the profile loaded. For getting the preferences also use
   * {@link UserService#getUserByTopazId UserService.getUserByTopazId}
   *
   * @param topazUserId topazUserId
   * @return PlosOneUser
   * @throws ApplicationException on access-check failure
   */
  public PlosOneUser getUserWithProfileLoaded(final String topazUserId)
      throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create(topazUserId));

    return new PlosOneUser(getUserAccount(topazUserId), applicationId, pep);
  }

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   *
   * @param authId authentication ID
   * @return the user associated with the authID
   * @throws ApplicationException on access-check failure
   */
  public PlosOneUser getUserByAuthId(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create("account:" + authId));

    Results r = session.
        createQuery("select ua from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();
    if (!r.next())
      return null;
    return new PlosOneUser((UserAccount) r.get(0), applicationId, pep);
  }

  /**
   * Sets the state of the user account
   *
   * @param userId Topaz User ID
   * @param state  new state of the user
   * @throws ApplicationException ApplicationException
   */
  public void setState(final String userId, int state) throws ApplicationException {
    pep.checkAccessAE(pep.SET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId);
    ua.setState(state);
    session.saveOrUpdate(ua);
  }

  /**
   * Gets the current state of the user
   *
   * @param userId Topaz userID
   * @return current state of the user
   * @throws ApplicationException ApplicationException
   */
  public int getState(final String userId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId);
    return ua.getState();
  }

  /**
   * Retrieves first authentication ID for a given Topaz userID.
   *
   * @param topazId Topaz userID
   * @return first authentiation ID associated with the Topaz userID
   * @throws ApplicationException ApplicationException
   */
  public String getAuthenticationId(final String topazId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_AUTH_IDS, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId);
    return ua.getAuthIds().iterator().next().getValue();
  }

  /**
   * Returns the Topaz userID the authentiation ID passed in is associated with
   *
   * @param authId authentication ID you are looking up
   * @return Topaz userID for a given authentication ID
   * @throws ApplicationException on access-check failure
   */
  public String lookUpUserByAuthId(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create("account:" + authId));

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();
    if (!r.next())
      return null;
    return r.getString(0);
  }

  /**
   * Lookup the topaz id of the user with a given email address
   * @param emailAddress emailAddress
   * @return topaz id of the user
   * @throws ApplicationException on access-check failure
   */
  public String lookUpUserByEmailAddress(final String emailAddress) throws ApplicationException {
    return lookUpUserByProfile("email", URI.create("mailto:" + emailAddress));
  }

  private String lookUpUserByProfile(final String field, final Object value)
      throws ApplicationException {
    pep.checkAccessAE(pep.FIND_USERS_BY_PROF, pep.ANY_RESOURCE);

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.profile." + field + " = :v;").
        setParameter("v", value).execute();
    if (!r.next())
      return null;
    return r.getString(0);
  }

  /**
   * Takes in a PLoS ONE user and write the profile to the store
   *
   * @param inUser write profile of this user to the store
   * @throws ApplicationException ApplicationException
   * @throws DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  public void setProfile(final PlosOneUser inUser)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    if (inUser != null) {
      setProfile(inUser, false);
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Takes in a PLoS ONE user and write the profile to the store
   *
   * @param inUser write profile of this user to the store
   * @param privateFields fields marked private by the user
   * @param userNameIsRequired whether userNameIsRequired
   * @throws ApplicationException ApplicationException
   * @throws DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  public void setProfile(final PlosOneUser inUser, final String[] privateFields,
                         final boolean userNameIsRequired)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    if (inUser != null) {
      setProfile(inUser, userNameIsRequired);
      final Collection<UserProfileGrant> profileGrantsList =
          UserProfileGrant.getProfileGrantsForFields(privateFields);
      final UserProfileGrant[] profileGrants =
          profileGrantsList.toArray(new UserProfileGrant[profileGrantsList.size()]);
      setProfileFieldsPrivate(inUser.getUserId(), profileGrants);
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Write the specified user profile and associates it with the specified user ID
   *
   * @param inUser  topaz user object with the profile
   * @param userNameIsRequired whether a username in the profile is required
   * @throws ApplicationException ApplicationException
   * @throws DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  protected void setProfile(final PlosOneUser inUser, final boolean userNameIsRequired)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    pep.checkAccessAE(pep.SET_PROFILE, URI.create(inUser.getUserId()));

    if (userNameIsRequired) {
      final String userId = lookUpUserByProfile("displayName", inUser.getDisplayName());
      if ((null != userId) && !userId.equals(inUser.getUserId())) {
        throw new DisplayNameAlreadyExistsException();
      }
    }

    UserAccount ua = getUserAccount(inUser.getUserId());
    ua.setProfile(inUser.getUserProfile());
    session.saveOrUpdate(ua);

    userCache.remove(USER_KEY + inUser.getUserId());
  }

  /**
   * Get the UserProfileGrants for the profile fields that are public
   *
   * @param topazId topazId
   * @return the collection of UserProfileGrant
   * @throws ApplicationException ApplicationException
   */
  public Collection<UserProfileGrant> getProfileFieldsThatArePublic(String topazId)
      throws ApplicationException {
    try {
      final String[] publicGrants =
          permissionWebService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

      final Collection<String> result = new ArrayList<String>(publicGrants.length);
      for (final String publicGrant : publicGrants) {
        if (ArrayUtils.contains(allUserProfileFieldGrants, publicGrant)) {
          result.add(publicGrant);
        }
      }

      return UserProfileGrant.getProfileGrantsForGrants(result.toArray(new String[result.size()]));

    } catch (RemoteException ex) {
      throw new ApplicationException("Unable to fetch the access rights on the profile fields", ex);
    }
  }

  /**
   * Get the UserProfileGrants for the profile fields that are private
   *
   * @param topazId topazId
   * @return the collection of UserProfileGrant
   * @throws ApplicationException ApplicationException
   */
  public Collection<UserProfileGrant> getProfileFieldsThatArePrivate(String topazId)
      throws ApplicationException {
    try {
      final String[] publicGrants =
          permissionWebService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

      final Collection<String> result = new ArrayList<String>();
      CollectionUtils.addAll(result, getAllUserProfileFieldGrants());
      for (final String publicGrant : publicGrants) {
        result.remove(publicGrant);
      }

      return UserProfileGrant.getProfileGrantsForGrants(result.toArray(new String[result.size()]));

    } catch (RemoteException ex) {
      throw new ApplicationException("Unable to fetch the access rights on the profile fields", ex);
    }
  }

  /**
   * Set the selected profileGrantEnums on the given topaz profile to be private for the user making
   * this call.  All the other fields will be set as public.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws ApplicationException ApplicationException
   */
  public void setProfileFieldsPrivate(String topazId, UserProfileGrant[] profileGrantEnums)
      throws ApplicationException {
    UserProfileGrant[] allUserProfileGrants = UserProfileGrant.values();

    for (final UserProfileGrant profileGrantEnum : profileGrantEnums) {
      allUserProfileGrants =
          (UserProfileGrant[]) ArrayUtils.removeElement(allUserProfileGrants, profileGrantEnum);
    }

    setProfileFieldsPublic(topazId, allUserProfileGrants);
  }

  /**
   * Set the selected profileGrantEnums on the given topaz profile for all the users as public.
   * All the other fields will be set as private.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws ApplicationException ApplicationException
   */
  public void setProfileFieldsPublic(String topazId, UserProfileGrant[] profileGrantEnums)
      throws ApplicationException {
    final String[] grants = getGrants(profileGrantEnums);
    setProfileFieldsPublic(topazId, grants);
  }

  private void setProfileFieldsPublic(final String topazId, final String[] grants)
      throws ApplicationException {
    if (grants.length > 0) {
      try {
        final String[] publicGrants = permissionWebService.listGrants(topazId, ALL_PRINCIPALS[0]);
        if (log.isDebugEnabled()) {
          log.debug("TopazId:" + topazId);
          log.debug("Cancelling grants:" + publicGrants);
          log.debug("Adding grants:" + ArrayUtils.toString(grants));
        }
        //Cancel all previous grants first
        permissionWebService.cancelGrants(topazId, publicGrants, ALL_PRINCIPALS);
        //Now add the grants as requested
        permissionWebService.grant(topazId, grants, ALL_PRINCIPALS);
      } catch (RemoteException e) {
        throw new ApplicationException("Failed to set the userProfilePermission on the profile fields", e);
      }
    }
  }

  private ArrayList<String> getAllUserProfileGrants() {
    return new ArrayList<String>(Arrays.asList(allUserProfileFieldGrants));
  }

  private String[] getGrants(final UserProfileGrant[] grantEnums) {
    final List<String> grantsList = new ArrayList<String>(grantEnums.length);
    for (final UserProfileGrant grantEnum : grantEnums) {
      grantsList.add(grantEnum.getGrant());
    }

    return grantsList.toArray(new String[grantsList.size()]);
  }

  private static String[] getAllUserProfileFieldGrants() {
    final UserProfileGrant[] profileGrantEnums = UserProfileGrant.values();
    final String[] allGrants =  new String[profileGrantEnums.length];
    int i = 0;
    for (final UserProfileGrant allProfileGrantEnum : profileGrantEnums) {
      allGrants[i++] = allProfileGrantEnum.getGrant();
    }

    return allGrants;
  }

  /**
   * Writes the preferences for the given user to the store
   *
   * @param inUser
   *          User whose preferences should be written
   * @throws ApplicationException ApplicationException
   */
  public void setPreferences(final PlosOneUser inUser) throws ApplicationException {
    if (inUser == null)
      throw new ApplicationException("User is null");

    pep.checkAccessAE(pep.SET_PREFERENCES, URI.create(inUser.getUserId()));

    UserAccount ua = getUserAccount(inUser.getUserId());

    UserPreferences p = ua.getPreferences(applicationId);
    if (p == null) {
      p = new UserPreferences();
      p.setAppId(applicationId);
      ua.getPreferences().add(p);
    }
    inUser.getUserPrefs(p);

    session.saveOrUpdate(ua);
  }

  /**
   * @see org.plos.user.service.UserService#setRole(String, String[])
   */
  public void setRole(final String topazId, final String roleId) throws ApplicationException {
    setRole(topazId, new String[] { roleId });
  }

  /**
   * Set the roles for the user.
   *
   * @param topazId the user's id
   * @param roleIds the new roles
   * @throws ApplicationException if the user doesn't exist
   */
  public void setRole(final String topazId, final String[] roleIds) throws ApplicationException {
    pep.checkAccessAE(pep.SET_ROLES, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId);
    ua.getRoles().clear();
    for (String r : roleIds)
      ua.getRoles().add(new UserRole(r));
    session.saveOrUpdate(ua);
  }

  /**
   * Get the roles for the user.
   * @param topazId topazId
   */
  public String[] getRole(final String topazId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_ROLES, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId);

    String[] res = new String[ua.getRoles().size()];
    int idx = 0;
    for (UserRole ur : ua.getRoles())
      res[idx++] = ur.getRole();

    return res;
  }

  /**
   * @return Returns the appId.
   */
  public String getApplicationId() {
    return applicationId;
  }

  /**
   * @param appId
   *          The appId to set.
   */
  public void setApplicationId(String appId) {
    this.applicationId = appId;
  }

  /**
   * @return the url from which the email address of the given guid can be fetched
   */
  public String getEmailAddressUrl() {
    return emailAddressUrl;
  }

  /**
   * @param emailAddressUrl
   *          The url from which the email address of the given guid can be fetched
   */
  public void setEmailAddressUrl(final String emailAddressUrl) {
    this.emailAddressUrl = emailAddressUrl;
  }

  /**
   * Getter for property 'permissionWebService'.
   *
   * @return Value for property 'permissionWebService'.
   */
  public PermissionWebService getPermissionWebService() {
    return permissionWebService;
  }

  /**
   * Setter for property 'permissionWebService'.
   *
   * @param permissionWebService Value to set for property 'permissionWebService'.
   */
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionWebService = permissionWebService;
  }

  /**
   * @return all the weekly categories
   * TODO: should these not really be in a database
   */
  public Collection<String> getWeeklyCategories() {
    return weeklyCategories;
  }

  /**
   * Set the weekly categories.
   * @param weeklyCategories weeklyCategories
   */
  public void setWeeklyCategories(final Collection<String> weeklyCategories) {
    this.weeklyCategories = weeklyCategories;
  }

  /**
   * @return all the monthly categories
   * TODO: should these not really be in a database
   */
  public Collection<String> getMonthlyCategories() {
    return monthlyCategories;
  }

  /**
   * Set the monthly categories.
   * @param monthlyCategories monthlyCategories
   */
  public void setMonthlyCategories(final Collection<String> monthlyCategories) {
    this.monthlyCategories = monthlyCategories;
  }

  /**
   * @param userCache The User cache to use.
   */
  @Required
  public void setUserCache(Ehcache userCache) {
    this.userCache = userCache;
  }

  /**
   * Set the categories and their presentation names.
   * @param categoryNames categoryNames
   */
  public void setCategoryNames(final Map<String, String> categoryNames) {
    this.categoryNames = categoryNames;
  }

  /**
   * @return the map of category and their presentation names
   */
  public Map<String, String> getCategoryNames() {
    return categoryNames;
  }

  /**
   * @return the Category beans
   */
  public Collection<CategoryBean> getCategoryBeans() {
    final Collection<CategoryBean> result = new ArrayList<CategoryBean>(categoryNames.size());
    final Set<Map.Entry<String, String>> categoryNamesSet = categoryNames.entrySet();

    for (final Map.Entry<String, String> category : categoryNamesSet) {
      final String key = category.getKey();
      boolean weeklyCategoryKey = false;
      boolean monthlyCategoryKey = false;
      if (weeklyCategories.contains(key)) {
        weeklyCategoryKey = true;
      }
      if (monthlyCategories.contains(key)) {
        monthlyCategoryKey = true;
      }
      result.add(
              new CategoryBean(key, category.getValue(), weeklyCategoryKey, monthlyCategoryKey));
    }

    return result;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /** 
   * Set the OTM transaction manager. Called by spring's bean wiring. 
   * 
   * @param txManager the otm transaction manager
   */
  @Required
  public void setTxManager(OtmTransactionManager txManager) {
    this.txManager = txManager;
  }

  /**
   * Set the user's context which can be used to obtain user's session values/attributes
   * @param userContext userContext
   */
  public void setUserContext(final UserContext userContext) {
    this.userContext = userContext;
  }

  /**
   * @return get user context
   */
  public UserContext getUserContext() {
    return userContext;
  }
}
