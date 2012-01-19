/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** 
 * This defines the user-account service. It allows for the management of the core user accounts.
 * The interal topaz user-id is created and assigned here; it is this id that must be passed to
 * various other services whenever a userId is required. The id should be treated as an opaque
 * string.
 * 
 * <P>Each account may have any number of authentication id's associated with it. These are used to
 * find the account associated with a given authentication id (token). The ids are treated as opaque
 * strings by this service.
 *
 * @author Ronald Tschalär
 */
public interface UserAccounts extends Remote {
  /**
   * Permissions associated with the user-accounts service.
   */
  public static interface Permissions {
    /** The action that represents a user account creation operation in XACML policies. */
    public static final String CREATE_USER = "userAccounts:createUser";

    /** The action that represents a delete user account operation in XACML policies. */
    public static final String DELETE_USER = "userAccounts:deleteUser";

    /** The action that represents a get-state operation in XACML policies. */
    public static final String GET_STATE = "userAccounts:getState";

    /** The action that represents a set-state operation in XACML policies. */
    public static final String SET_STATE = "userAccounts:setState";

    /** The action that represents a get-authentication-ids operation in XACML policies. */
    public static final String GET_AUTH_IDS = "userAccounts:getAuthIds";

    /** The action that represents a set-authentication-ids operation in XACML policies. */
    public static final String SET_AUTH_IDS = "userAccounts:setAuthIds";

    /** The action that represents a look-up-user operation in XACML policies. */
    public static final String LOOKUP_USER = "userAccounts:lookUpUser";
  }

  /** the state indicating the user account is active: {@value} */
  public static final int ACNT_ACTIVE    = 0;
  /** the state indicating the user account is suspened: {@value} */
  public static final int ACNT_SUSPENDED = 1;

  /** 
   * Create a new user account and associate a single authentication id with it.
   * 
   * @param authId  the user's authentication id
   * @return the user's internal id
   * @throws DuplicateAuthIdException if the <var>authId</var> is already in use
   * @throws RemoteException if some other error occured
   */
  public String createUser(String authId) throws DuplicateAuthIdException, RemoteException;

  /** 
   * Delete a user's account. Note that the application should ensure that all other information
   * related to this account (e.g. profile and preferences) have been deleted first - this method
   * will not do that.
   * 
   * @param userId  the user's internal id
   * @throws NoSuchUserIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public void deleteUser(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Set the state of a user's account. Note that while constants for two states have been
   * pre-defined, applications may (re)define both the number and the semantics of the states;
   * i.e.  no semantics are attached to the state values, except that a new account is assigned
   * state 0. However, all applications (including things like XACML policies) must agree on the
   * valid states and their semantics.
   * 
   * @param userId  the user's internal id
   * @param state   the new state to put the account in
   * @throws NoSuchUserIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public void setState(String userId, int state) throws NoSuchUserIdException, RemoteException;

  /** 
   * Get the state of a user's account.
   * 
   * @param userId  the user's internal id
   * @return the current state of account
   * @throws NoSuchUserIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   * @see #setState
   */
  public int getState(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Get the list of currently known authentication id's for the specified user account.
   * 
   * @param userId  the user account's id
   * @return the list of authentication id's; this may be empty. Note that the order of the entries
   *         will be arbitrary.
   * @throws NoSuchUserIdException if the user account does not exist
   * @throws RemoteException if some other error occured
   */
  public String[] getAuthenticationIds(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Set the list of currently known authentication id's for the specified user account. The list
   * may be empty, which will probably have the effect of disabling logins to the account.
   * 
   * @param userId  the user account's id
   * @param authIds the list of authentication id's; this may be empty. Note that the order will not
   *                be preserved.
   * @throws NoSuchUserIdException if the user account does not exist
   * @throws DuplicateAuthIdException if any of the <var>authIds</var> are already in use
   * @throws RemoteException if some other error occured
   */
  public void setAuthenticationIds(String userId, String[] authIds)
      throws NoSuchUserIdException, DuplicateAuthIdException, RemoteException;

  /** 
   * Look up a user-id given an authentication id.
   * 
   * @param authId the user's authentication id
   * @return the user's internal id, or null if not found
   * @throws RemoteException if some lookup error occured
   */
  public String lookUpUserByAuthId(String authId) throws RemoteException;
}
