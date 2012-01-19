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
 * This defines the user-roles service. It allows for the management of the security roles assigned
 * to users.
 * 
 * @author Ronald Tschalär
 */
public interface UserRoles extends Remote {
  /**
   * Permissions associated with user-roles service.
   */
  public static interface Permissions {
    /** The action that represents a get-roles operation in XACML policies. */
    public static final String GET_ROLES = "userRoles:getRoles";

    /** The action that represents a set-roles operation in XACML policies. */
    public static final String SET_ROLES = "userRoles:setRoles";

    /** The action that represents a list-users-in-role operation in XACML policies. */
    public static final String LIST_USERS_IN_ROLE = "userRoles:listUsersInRole";
  }

  /** 
   * Set the list of currently assigned security roles for the specified user.
   * 
   * @param userId  the user's id
   * @param roles   the list of roles to assign this user; may be null. Note that the order will not
   *                be preserved.
   * @throws RemoteException if some other error occured
   */
  public void setRoles(String userId, String[] roles) throws NoSuchUserIdException, RemoteException;

  /** 
   * Get the list of currently assigned security roles for the specified user.
   * 
   * @param userId  the user's id
   * @return the list of roles; this may be null. Note that the order of the entries will be
   *         arbitrary.
   * @throws NoSuchUserIdException if the user does not exist
   * @throws RemoteException if some other error occured
   */
  public String[] getRoles(String userId) throws NoSuchUserIdException, RemoteException;

  /** 
   * Get the list of users who are currently assigned the specified security role.
   * 
   * @param role  the role
   * @return the list of user ids; this may be null. Note that the order of the entries will be
   *         arbitrary.
   * @throws RemoteException if some error occured
   */
  public String[] listUsersInRole(String role) throws RemoteException;
}
