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

import net.sf.ehcache.Ehcache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.Constants;
import org.plos.models.AuthenticationId;
import org.plos.models.UserAccount;
import org.plos.models.UserPreference;
import org.plos.models.UserPreferences;
import org.plos.models.UserRole;
import org.plos.permission.service.PermissionWebService;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;
import org.plos.user.UsersPEP;
import org.plos.web.UserContext;
import org.topazproject.otm.util.TransactionHelper;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  private static final int      LD_NONE     = 0;
  private static final int      LD_AUTH_IDS = 1 << 0;
  private static final int      LD_ROLES    = 1 << 1;
  private static final int      LD_PREFS    = 1 << 2;
  private static final int      LD_PROFILE  = 1 << 3;
  private static final int      LD_ALL      = 0xFFFFFFFF;

  private static final String USER_LOCK = "UserCache-Lock-";
  private static final String USER_KEY  = "UserCache-User-";

  private Session session;

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
   * @param authId
   *          the user's authentication id from CAS
   * @return the user's internal id
   * @throws ApplicationException
   *           if an error occured
   */
  public String createUser(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.CREATE_USER, pep.ANY_RESOURCE);

    // create account
    final UserAccount ua =
        TransactionHelper.doInTx(session, new TransactionHelper.Action<UserAccount>() {
      public UserAccount run(Transaction tx) {
        UserAccount ua = new UserAccount();
        ua.getAuthIds().add(new AuthenticationId(authId));
        tx.getSession().saveOrUpdate(ua);
        return ua;
      }
    });

    // check for duplicate
    TransactionHelper.doInTxE(session, new TransactionHelper.ActionE<Void, ApplicationException>() {
      public Void run(Transaction tx) throws ApplicationException {
        Results r = tx.getSession().
            createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
            setParameter("id", authId).execute();
        if (!r.next() || !r.next())
          return null;

        tx.getSession().delete(ua);
        tx.commit();

        throw new ApplicationException("AuthId '" + authId + "' is already in use");
      }
    });

    return ua.getId().toString();
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

    TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        UserAccount ua = tx.getSession().get(UserAccount.class, topazUserId);
        if (ua != null)
          tx.getSession().delete(ua);
        return null;
      }
    });
  }

  /**
   * Get the account info for the given user.
   * @param topazUserId the Topaz User ID
   * @param loadList    the list of associations to eagerly load
   * @throws ApplicationException ApplicationException
   */
  private UserAccount getUserAccount(final String topazUserId, final int loadList)
      throws ApplicationException {
    return TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<UserAccount, ApplicationException>() {
      public UserAccount run(Transaction tx) throws ApplicationException {
        UserAccount ua = tx.getSession().get(UserAccount.class, topazUserId);
        if (ua == null)
          throw new ApplicationException("No user-account with id '" + topazUserId + "' found");

        /* Ugh! This is ugly, and fragile in case the model ever changes...
         * Since there's no eager loading (yet) in OTM, we touch (force a load of)
         * all the stuff we'll need now while we're still in a transaction.
         */
        if ((loadList & LD_AUTH_IDS) != 0 && ua.getAuthIds() != null)
          for (AuthenticationId ai : ua.getAuthIds()) ;

        if ((loadList & LD_ROLES) != 0 && ua.getRoles() != null)
          for (UserRole r : ua.getRoles()) ;

        if ((loadList & LD_PREFS) != 0 && ua.getPreferences() != null) {
          for (UserPreferences pl : ua.getPreferences()) {
            if (pl.getPrefs() != null)
              for (UserPreference p : pl.getPrefs()) ;
          }
        }

        if ((loadList & LD_PROFILE) != 0 && ua.getProfile() != null &&
            ua.getProfile().getInterests() != null)
          for (URI i : ua.getProfile().getInterests()) ;

        return ua;
      }
    });
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

    Object result = CacheAdminHelper.getFromCache(userCache, USER_KEY + topazUserId, -1, lock,
                                                  "userName",
                                                  new CacheAdminHelper.EhcacheUpdater<Object>() {
        public Object lookup() {
          try {
            String displayName = getDisplayName(topazUserId);
            return displayName;
          } catch (ApplicationException ae) {
            return ae;
          }
        }
    });

    if (result instanceof ApplicationException) {
      throw (ApplicationException) result;
    }
    
    return (String) result;
  }

  private String getDisplayName(final String topazUserId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_DISP_NAME, URI.create(topazUserId));
    return TransactionHelper.doInTxE(session,
                                     new TransactionHelper.ActionE<String, ApplicationException>() {
      public String run(Transaction tx) throws ApplicationException {
        Results r = tx.getSession().
            createQuery("select ua.profile.displayName from UserAccount ua where ua = :id;").
            setParameter("id", topazUserId).execute();
        if (!r.next())
          throw new ApplicationException("No user-account with id '" + topazUserId + "' found");
        return r.getString(0);
      }
    });
  }

  /**
   * Gets the user specified by the Topaz userID passed in
   *
   * @param topazUserId
   *          Topaz User ID
   * @return user associated with the topazUserId
   * @throws ApplicationException ApplicationException
   */
  public PlosOneUser getUserByTopazId(final String topazUserId) throws ApplicationException {
    return getUserWithProfileLoaded(topazUserId);
  }

  /**
   * Get the PlosOneUser with only the profile loaded,
   * for getting the preferences also use {@link UserService#getUserByTopazId UserService.getUserByTopazId}
   * @param topazUserId topazUserId
   * @return PlosOneUser
   * @throws ApplicationException ApplicationException
   */
  public PlosOneUser getUserWithProfileLoaded(final String topazUserId) throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create(topazUserId));

    return new PlosOneUser(getUserAccount(topazUserId, LD_ALL), applicationId, pep);
  }

  /**
   * Gets the user specified by the authentication ID (CAS ID currently)
   *
   * @param authId
   *          authentication ID
   * @return the user associated with the authID
   * @throws ApplicationException ApplicationException
   */
  public PlosOneUser getUserByAuthId(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create("account:" + authId));

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<PlosOneUser>() {
      public PlosOneUser run(Transaction tx) {
        Results r = tx.getSession().
            createQuery("select ua from UserAccount ua where ua.authIds.value = :id;").
            setParameter("id", authId).execute();
        if (!r.next())
          return null;
        return new PlosOneUser((UserAccount) r.get(0), applicationId, pep);
      }
    });
  }

  /**
   * Sets the state of the user account
   *
   * @param userId
   *          Topaz User ID
   * @param state
   *          new state of the user
   * @throws ApplicationException ApplicationException
   */
  public void setState(final String userId, int state) throws ApplicationException {
    pep.checkAccessAE(pep.SET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId, LD_NONE);
    ua.setState(state);
    flush(ua);
  }

  /**
   * Gets the current state of the user
   *
   * @param userId
   *          Topaz userID
   * @return current state of the user
   * @throws ApplicationException ApplicationException
   */
  public int getState(final String userId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_STATE, URI.create(userId));

    UserAccount ua = getUserAccount(userId, LD_NONE);
    return ua.getState();
  }

  /**
   * Retrieves all authentication IDs for a given Topaz userID
   *
   * @param topazId
   *          Topaz userID
   * @return first authentiation ID associated with the Topaz userID
   * @throws ApplicationException ApplicationException
   */
  public String getAuthenticationId(final String topazId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_AUTH_IDS, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId, LD_AUTH_IDS);
    return ua.getAuthIds().iterator().next().getValue();
  }

  /**
   * Returns the Topaz userID the authentiation ID passed in is associated with
   *
   * @param authId
   *          authentication ID you are looking up
   * @return Topaz userID for a given authentication ID
   * @throws ApplicationException ApplicationException
   */
  public String lookUpUserByAuthId(final String authId) throws ApplicationException {
    pep.checkAccessAE(pep.LOOKUP_USER, URI.create("account:" + authId));

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<String>() {
      public String run(Transaction tx) {
        Results r = tx.getSession().
            createQuery("select ua.id from UserAccount ua where ua.authIds.value = :id;").
            setParameter("id", authId).execute();
        if (!r.next())
          return null;
        return r.getString(0);
      }
    });
  }

  /**
   * Lookup the topaz id of the user with a given email address
   * @param emailAddress emailAddress
   * @return topaz id of the user
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String lookUpUserByEmailAddress(final String emailAddress) throws ApplicationException {
    return lookUpUserByProfile("email", URI.create("mailto:" + emailAddress));
  }

  private String lookUpUserByProfile(final String field, final Object value)
      throws ApplicationException {
    pep.checkAccessAE(pep.FIND_USERS_BY_PROF, pep.ANY_RESOURCE);

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<String>() {
      public String run(Transaction tx) {
        Results r = tx.getSession().
            createQuery("select ua.id from UserAccount ua where ua.profile." + field + " = :v;").
            setParameter("v", value).execute();
        if (!r.next())
          return null;
        return r.getString(0);
      }
    });
  }

  /**
   * Takes in a PLoS ONE user and write the profile to the store
   *
   * @param inUser
   *          write profile of this user to the store
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.plos.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  public void setProfile(final PlosOneUser inUser) throws ApplicationException, DisplayNameAlreadyExistsException {
    if (inUser != null) {
      setProfile(inUser, false);
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Takes in a PLoS ONE user and write the profile to the store
   *
   * @param inUser
   *          write profile of this user to the store
   * @param privateFields fields marked private by the user
   * @param userNameIsRequired whether userNameIsRequired
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.plos.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
   */
  public void setProfile(final PlosOneUser inUser, final String[] privateFields,
                         final boolean userNameIsRequired)
      throws ApplicationException, DisplayNameAlreadyExistsException {
    if (inUser != null) {
      setProfile(inUser, userNameIsRequired);
      final Collection<UserProfileGrant> profileGrantsList = UserProfileGrant.getProfileGrantsForFields(privateFields);
      final UserProfileGrant[] profileGrants = profileGrantsList.toArray(new UserProfileGrant[profileGrantsList.size()]);
      setProfileFieldsPrivate(inUser.getUserId(), profileGrants);
    } else {
      throw new ApplicationException("User is null");
    }
  }

  /**
   * Write the specified user profile and associates it with the specified user ID
   *
   * @param topazUserId
   *          Topaz User ID
   * @param profile
   *          profile to be written
   * @param userNameIsRequired whether userNameIsRequired
   * @throws org.plos.ApplicationException ApplicationException
   * @throws org.plos.user.service.DisplayNameAlreadyExistsException DisplayNameAlreadyExistsException
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

    UserAccount ua = getUserAccount(inUser.getUserId(), LD_PROFILE);
    ua.setProfile(inUser.getUserProfile());
    flush(ua);

    userCache.remove(USER_KEY + inUser.getUserId());
  }

  /**
   * Get the UserProfileGrants for the profile fields that are public
   * @param topazId topazId
   * @throws ApplicationException ApplicationException
   * @return the collection of UserProfileGrant
   */
  public Collection<UserProfileGrant> getProfileFieldsThatArePublic(final String topazId) throws ApplicationException {
    try {
      final String[] publicGrants = permissionWebService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

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
   * @param topazId topazId
   * @throws ApplicationException ApplicationException
   * @return the collection of UserProfileGrant
   */
  public Collection<UserProfileGrant> getProfileFieldsThatArePrivate(final String topazId) throws ApplicationException {
    try {
      final String[] publicGrants = permissionWebService.listGrants(topazId, Constants.Permission.ALL_PRINCIPALS);

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
   * Set the selected profileGrantEnums on the given topaz profile to be private for the user making this call.
   * All the other fields will be set as public.
   *
   * @param topazId topazId
   * @param profileGrantEnums profileGrantEnums
   * @throws ApplicationException ApplicationException
   */
  public void setProfileFieldsPrivate(final String topazId, final UserProfileGrant[] profileGrantEnums) throws ApplicationException {
    UserProfileGrant[] allUserProfileGrants = UserProfileGrant.values();

    for (final UserProfileGrant profileGrantEnum : profileGrantEnums) {
      allUserProfileGrants = (UserProfileGrant[]) ArrayUtils.removeElement(allUserProfileGrants, profileGrantEnum);
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
  public void setProfileFieldsPublic(final String topazId, final UserProfileGrant[] profileGrantEnums) throws ApplicationException {
    final String[] grants = getGrants(profileGrantEnums);
    setProfileFieldsPublic(topazId, grants);
  }

  private void setProfileFieldsPublic(final String topazId, final String[] grants) throws ApplicationException {
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

    UserAccount ua = getUserAccount(inUser.getUserId(), LD_PREFS);

    UserPreferences p = ua.getPreferences(applicationId);
    if (p == null) {
      p = new UserPreferences();
      p.setAppId(applicationId);
      ua.getPreferences().add(p);
    }
    inUser.getUserPrefs(p);

    flush(ua);
  }

  /**
   * Writes out the given user's to the store.
   *
   * @param ua
   *          User account whose data should be written
   * @throws ApplicationException ApplicationException
   */
  private void flush(final UserAccount ua) throws ApplicationException {
    if (ua == null)
      throw new ApplicationException("User is null");

    TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
      public Void run(Transaction tx) {
        tx.getSession().saveOrUpdate(ua);
        return null;
      }
    });
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

    UserAccount ua = getUserAccount(topazId, LD_ROLES);
    ua.getRoles().clear();
    for (String r : roleIds)
      ua.getRoles().add(new UserRole(r));
    flush(ua);
  }

  /**
   * Get the roles for the user.
   * @param topazId topazId
   * @see org.plos.user.service.UserRoleWebService#getRoles(String)
   */
  public String[] getRole(final String topazId) throws ApplicationException {
    pep.checkAccessAE(pep.GET_ROLES, URI.create(topazId));

    UserAccount ua = getUserAccount(topazId, LD_ROLES);

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
