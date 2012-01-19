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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.service.BaseConfigurableService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper around Topaz User Accounts web service
 * 
 * @author Stephen Cheng
 * 
 */
public class UserWebService extends BaseConfigurableService {

  private UserAccounts userService;

  private String applicationId;

  private static final Log log = LogFactory.getLog(UserWebService.class);
  /**
   * Creates web service
   * 
   * @throws IOException
   * @throws URISyntaxException
   * @throws ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    userService = UserAccountsClientFactory.create(protectedService);
  }

  /**
   * Create a new user account and associate a single authentication id with it.
   * 
   * @param authId
   *          the user's authentication id
   * @return the user's internal id
   * @throws RemoteException
   *           if some other error occured
   * @throws DuplicateAuthIdException
   *           if the authId already exists
   */
  public String createUser(final String authId) throws DuplicateAuthIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    log.debug("lookUpUserByAuthId:" + authId);
    return userService.createUser(authId);
  }

  /**
   * Deletes given user ID from system. Does not delete any resources with the user, only the user
   * itself.
   * 
   * @param topazUserId
   *          Topaz User ID to delete
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void deleteUser(final String topazUserId) throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    userService.deleteUser(topazUserId);
  }

  /**
   * Sets the state of a given user account
   * 
   * @param topazUserId
   *          Topaz User ID
   * @param state
   *          new state of user
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void setState(final String topazUserId, int state) throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    userService.setState(topazUserId, state);
  }

  /**
   * Gets the current state of the user
   * 
   * @param topazUserId
   *          Topaz User ID to set
   * @return current state of user
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public int getState(final String topazUserId) throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return userService.getState(topazUserId);
  }

  /**
   * Gets the authentication IDs for a given Topaz user ID
   * 
   * @param topazUserId
   *          Topaz user ID
   * @return array of authentication ids
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public String[] getAuthenticationIds(final String topazUserId) throws NoSuchIdException,
      RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return userService.getAuthenticationIds(topazUserId);
  }

  /**
   * Returns the Topaz User ID the authId is associated with
   * 
   * @param authId
   *          authentication ID to lookup
   * @return Topaz User ID
   * @throws RemoteException
   */
  public String lookUpUserByAuthId(final String authId) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    log.debug("lookUpUserByAuthId:" + authId);
    return userService.lookUpUserByAuthId(authId);
  }

  /**
   * @return Returns the applicationId.
   */
  public String getApplicationId() {
    return applicationId;
  }

  /**
   * @param applicationId
   *          The applicationId to set.
   */
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }
}
