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

package org.topazproject.ambra.user.service;

import com.sun.xacml.PDP;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.Constants;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.user.UserProfileGrant;
import org.topazproject.ambra.user.UsersPEP;
import org.topazproject.ambra.xacml.AbstractSimplePEP;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.spring.OtmTransactionManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Class to roll up web services that a user needs in Ambra. Rest of application should generally
 * use AmbraUser to
 *
 * @author Stephen Cheng
 *
 */
public class  TopazUserService implements UserService {
  private static final Logger      log = LoggerFactory.getLogger(TopazUserService.class);

  private static final String[] allUserProfileFieldGrants = getAllUserProfileFieldGrants();
  private static final DefaultTransactionDefinition txnDef = new DefaultTransactionDefinition();

  private static final String[] ALL_PRINCIPALS =
    new String[] { Constants.Permission.ALL_PRINCIPALS };

  private static final String USER_LOCK = "UserCache-Lock-";
  private static final String USER_KEY = "UserCache-User-";

  private Session               session;
  private OtmTransactionManager txManager;

  private UsersPEP pep;

  private PermissionsService permissionsService;
  private Cache userCache;

  private String applicationId;
  private String emailAddressUrl;

  /**
   * Constructor
   */
  public TopazUserService() {
  }

  @Required
  public void setUsersPdp(PDP pdp) {
    pep = new UsersPEP(pdp);
  }

  /**
   * Create a new user account and associate a single authentication id with it.
   *
   * @param authId the user's authentication id from CAS
   * @return the user's internal id
   * @throws org.topazproject.ambra.ApplicationException if the <var>authId</var> is a duplicate
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createUser(final String authId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.CREATE_USER, AbstractSimplePEP.ANY_RESOURCE);

    // create account
    UserAccount ua = new UserAccount();
    ua.getAuthIds().add(new AuthenticationId(authId));
    session.saveOrUpdate(ua);

    // check for duplicate. Tx hack alert!!!
    txManager.doCommit(
        (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus());
    txManager.doBegin(txManager.doGetTransaction(), txnDef);

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();
    try {
      if (!r.next() || !r.next())
        return ua.getId().toString();
    } finally {
      r.close();
    }

    session.delete(ua);
    txManager.doCommit(
        (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus());
    txManager.doBegin(txManager.doGetTransaction(), txnDef);

    throw new ApplicationException("AuthId '" + authId + "' is already in use");
  }

  /**
   * Deletes the given user from Topaz. Visible for testing only.
   *
   * @param userId
   *          the Topaz User ID
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteUser(final String userId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.DELETE_USER, URI.create(userId));

    UserAccount ua = session.get(UserAccount.class, userId);
    if (ua != null)
      session.delete(ua);
  }

  /**
   * Get the account info for the given user.
   * @param topazUserId the Topaz User ID
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @return UserAccount
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
   * @param userId topazUserId
   * @return username username
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public String getUsernameById(final String userId) throws ApplicationException {

    final Object lock = (USER_LOCK + userId).intern();

    return userCache.get(USER_KEY + userId, -1,
                               new Cache.SynchronizedLookup<String, ApplicationException>(lock) {
      @SuppressWarnings("synthetic-access")
      @Override
      public String lookup() throws ApplicationException {
        return getDisplayName(userId);
      }
    });
  }

  private String getDisplayName(final String topazUserId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.GET_DISP_NAME, URI.create(topazUserId));

    Results r = session.
        createQuery("select ua.profile.displayName from UserAccount ua where ua = :id;").
        setParameter("id", topazUserId).execute();
    try {
      if (!r.next())
        throw new ApplicationException("No user-account with id '" + topazUserId + "' found");
      return r.getString(0);
    } finally {
      r.close();
    }
  }

  /**
   * Gets the user specified by the Topaz userID passed in
   *
   * @param userId Topaz User ID
   * @return user associated with the topazUserId
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public AmbraUser getUserById(final String userId) throws ApplicationException {
    return getUserWithProfileLoaded(userId);
  }

  /**
   * Get the AmbraUser with only the profile loaded. For getting the preferences also use
   * getUserById
   *
   * @param topazUserId topazUserId
   * @return AmbraUser
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public AmbraUser getUserWithProfileLoaded(final String topazUserId)
      throws ApplicationException {
    pep.checkAccessAE(UsersPEP.LOOKUP_USER, URI.create(topazUserId));

    UserAccount ua = getUserAccount(topazUserId);
    return new AmbraUser(ua, applicationId, pep);
  }

  /**
   * Gets the us  er specified by the authentication ID (CAS ID currently)
   *
   * @param authId authentication ID
   * @return the user associated with the authID
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public AmbraUser getUserByAuthId(final String authId) throws ApplicationException {
    UserAccount ua = getUserAccountByAuthId(authId);
    if (ua == null)
      return null;

    return new AmbraUser(ua, applicationId, pep);
  }

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   *
   * @param authId authentication ID
   * @return the user associated with the authID
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public UserAccount getUserAccountByAuthId(String authId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.LOOKUP_USER, URI.create("account:" + authId));

    Results r = session.
        createQuery("select ua from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();

    try {
      return r.next() ? (UserAccount) r.get(0) : null;
    } finally {
      r.close();
    }
  }

  /**
   * Sets the state of the user account
   *
   * @param userId Topaz User ID
   * @param state  new state of the user
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setState(final String userId, int state) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.SET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId);
    ua.setState(state);
  }

  /**
   * Gets the current state of the user
   *
   * @param userId Topaz userID
   * @return current state of the user
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public int getState(final String userId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.GET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId);
    return ua.getState();
  }

  /**
   * Retrieves first authentication ID for a given Topaz userID.
   *
   * @param userId Topaz userID
   * @return first authentiation ID associated with the Topaz userID
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public String getAuthenticationId(final String userId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.GET_AUTH_IDS, URI.create(userId));

    UserAccount ua = getUserAccount(userId);
    return ua.getAuthIds().iterator().next().getValue();
  }

  /**
   * Returns the Topaz userID the authentiation ID passed in is associated with
   *
   * @param authId authentication ID you are looking up
   * @return Topaz userID for a given authentication ID
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public String lookUpUserByAuthId(final String authId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.LOOKUP_USER, URI.create("account:" + authId));

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
        setParameter("id", authId).execute();
    try {
      if (!r.next())
        return null;
      return r.getString(0);
    } finally {
      r.close();
    }
  }

  /**
   * Returns the Topaz userID the account ID passed in is associated with
   *
   * @param accountId account ID you are looking up
   * @return Topaz userID for a given account ID
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public String lookUpUserByAccountId(final String accountId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.LOOKUP_USER, URI.create("account:" + accountId));

    UserAccount account = session.get(UserAccount.class, accountId);

    if (account != null)
      return account.getId().toString();
    else
      return null;
  }

  /**
   * Lookup the topaz id of the user with a given email address
   * @param emailAddress emailAddress
   * @return topaz id of the user
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public String lookUpUserByEmailAddress(final String emailAddress) throws ApplicationException {
    return lookUpUserByProfile("email", URI.create("mailto:" + emailAddress));
  }


  /**
   * Lookup the topaz id of the user with a given name
   * @param name user display name
   * @return topaz id of the user
   * @throws org.topazproject.ambra.ApplicationException on access-check failure
   */
  @Transactional(readOnly = true)
  public String lookUpUserByDisplayName(final String name) throws ApplicationException {

    pep.checkAccessAE(UsersPEP.LOOKUP_USER, URI.create("account:" + name));

    return lookUpUserByProfile("displayName", name);

  }

  private String lookUpUserByProfile(final String field, final Object value)
      throws ApplicationException {
    pep.checkAccessAE(UsersPEP.FIND_USERS_BY_PROF, AbstractSimplePEP.ANY_RESOURCE);

    Results r = session.
        createQuery("select ua.id from UserAccount ua where ua.profile." + field + " = :v;").
        setParameter("v", value).execute();
    try {
      if (!r.next())
        return null;
      return r.getString(0);
    } finally {
      r.close();
    }
  }

  /**
   * Takes in an Ambra user and write the profile to the store
   *
   * @param inUser write profile of this user to the store
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @throws org.topazproject.ambra.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setProfile(final AmbraUser inUser)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    if (inUser != null) {
      setProfile(inUser, false);
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Takes in an Ambra user and write the profile to the store
   *
   * @param inUser write profile of this user to the store
   * @param privateFields fields marked private by the user
   * @param userNameIsRequired whether userNameIsRequired
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @throws org.topazproject.ambra.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setProfile(final AmbraUser inUser, final String[] privateFields,
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
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @throws org.topazproject.ambra.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  public void setProfile(final AmbraUser inUser, final boolean userNameIsRequired)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    pep.checkAccessAE(UsersPEP.SET_PROFILE, URI.create(inUser.getUserId()));

    if (userNameIsRequired) {
      final String userId = lookUpUserByProfile("displayName", inUser.getDisplayName());
      if ((null != userId) && !userId.equals(inUser.getUserId())) {
        throw new DisplayNameAlreadyExistsException();
      }
    }

    UserAccount ua = getUserAccount(inUser.getUserId());
    UserProfile old = ua.getProfile();
    UserProfile nu = inUser.getUserProfile();
    // if we are swapping out a profile with another with the same id, evict the old
    if ((old != null) && (nu != null) && (old != nu) && old.getId().equals(nu.getId())) {
      if (log.isDebugEnabled())
        log.debug("Evicting old profile (" + old + ") with id " + old.getId());
      session.evict(old);
    }
    if (nu != null) {
      ua.setProfile(nu);
      session.saveOrUpdate(ua); // force it since 'nu' may have been evicted
      session.flush();
      session.evict(nu);
      if (log.isDebugEnabled())
        log.debug("Evicting nu profile (" + nu + ") with id " + nu.getId());
    }

    userCache.remove(USER_KEY + inUser.getUserId());
  }

  /**
   * Get the UserProfileGrants for the profile fields that are public
   *
   * @param topazId topazId
   * @return the collection of UserProfileGrant
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public Collection<UserProfileGrant> getProfileFieldsThatArePublic(String topazId)
      throws ApplicationException {
    try {
      final String[] publicGrants =
          permissionsService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

      final Collection<String> result = new ArrayList<String>(publicGrants.length);
      for (final String publicGrant : publicGrants) {
        if (ArrayUtils.contains(allUserProfileFieldGrants, publicGrant)) {
          result.add(publicGrant);
        }
      }

      return UserProfileGrant.getProfileGrantsForGrants(result.toArray(new String[result.size()]));
    } catch (Exception ex) {
      throw new ApplicationException("Unable to fetch the access rights on the profile fields", ex);
    }
  }

  /**
   * Get the UserProfileGrants for the profile fields that are private
   *
   * @param topazId topazId
   * @return the collection of UserProfileGrant
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(readOnly = true)
  public Collection<UserProfileGrant> getProfileFieldsThatArePrivate(String topazId)
      throws ApplicationException {
    try {
      final String[] publicGrants =
          permissionsService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

      final Collection<String> result = new ArrayList<String>();
      CollectionUtils.addAll(result, getAllUserProfileFieldGrants());
      for (final String publicGrant : publicGrants) {
        result.remove(publicGrant);
      }

      return UserProfileGrant.getProfileGrantsForGrants(result.toArray(new String[result.size()]));

    } catch (Exception ex) {
      throw new ApplicationException("Unable to fetch the access rights on the profile fields", ex);
    }
  }

  /**
   * Set the selected profileGrantEnums on the given topaz profile to be private for the user making
   * this call.  All the other fields will be set as public.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
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
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setProfileFieldsPublic(String topazId, UserProfileGrant[] profileGrantEnums)
      throws ApplicationException {
    final String[] grants = getGrants(profileGrantEnums);
    setProfileFieldsPublic(topazId, grants);
  }

  private void setProfileFieldsPublic(final String topazId, final String[] grants)
      throws ApplicationException {
    if (grants.length > 0) {
      try {
        final String[] publicGrants = permissionsService.listGrants(topazId, ALL_PRINCIPALS[0]);
        if (log.isDebugEnabled()) {
          log.debug("TopazId:" + topazId);
          log.debug("Cancelling grants: " + Arrays.toString(publicGrants));
          log.debug("Adding grants:" + Arrays.toString(grants));
        }
        //Cancel all previous grants first
        permissionsService.cancelGrants(topazId, publicGrants, ALL_PRINCIPALS);
        //Now add the grants as requested
        permissionsService.grant(topazId, grants, ALL_PRINCIPALS);
      } catch (Exception e) {
        throw new ApplicationException("Failed to set the userProfilePermission on the profile fields", e);
      }
    }
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
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setPreferences(final AmbraUser inUser) throws ApplicationException {
    if (inUser == null)
      throw new ApplicationException("User is null");

    pep.checkAccessAE(UsersPEP.SET_PREFERENCES, URI.create(inUser.getUserId()));

    UserAccount ua = getUserAccount(inUser.getUserId());

    UserPreferences p = ua.getPreferences(applicationId);
    if (p == null) {
      p = new UserPreferences();
      p.setAppId(applicationId);
      ua.getPreferences().add(p);
    }
    inUser.getUserPrefs(p);

  }

  /**
   * @see org.topazproject.ambra.user.service.TopazUserService#setRole(String, String[])
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setRole(final String topazId, final String roleId) throws ApplicationException {
    setRole(topazId, new String[] { roleId });
  }

  /**
   * Set the roles for the user.
   *
   * @param topazId the user's id
   * @param roleIds the new roles
   * @throws org.topazproject.ambra.ApplicationException if the user doesn't exist
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setRole(final String topazId, final String[] roleIds) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.SET_ROLES, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId);
    ua.getRoles().clear();
    for (String r : roleIds)
      ua.getRoles().add(new UserRole(r));
  }

  /**
   * Get the roles for the user.
   * @param topazId topazId
   * @return roles
   * @throws org.topazproject.ambra.ApplicationException
   */
  @Transactional(readOnly = true)
  public String[] getRole(final String topazId) throws ApplicationException {
    pep.checkAccessAE(UsersPEP.GET_ROLES, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId);

    String[] res = new String[ua.getRoles().size()];
    int idx = 0;
    for (UserRole ur : ua.getRoles())
      res[idx++] = ur.getRole();

    return res;
  }

  /**
   * Checks the action guard.
   * @return boolean
   */
  @Transactional(readOnly = true)
  public boolean allowAdminAction() {
    try {
      pep.checkAccess(UsersPEP.ADMIN_GUARD, AbstractSimplePEP.ANY_RESOURCE);
      return true;
    } catch (SecurityException e) {
      return false;
    }
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
  @Required
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
  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * @param userCache The User cache to use.
   */
  @Required
  public void setUserCache(Cache userCache) {
    this.userCache = userCache;
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
}
