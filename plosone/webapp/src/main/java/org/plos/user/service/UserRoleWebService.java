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

import org.plos.service.BaseConfigurableService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserRoles;
import org.topazproject.ws.users.UserRolesClientFactory;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper around Topaz User Roles web service
 */
public class UserRoleWebService extends BaseConfigurableService {
  private UserRoles userRoles;

  /**
   * Creates web service.
   *
   * @throws java.io.IOException
   * @throws java.net.URISyntaxException
   * @throws javax.xml.rpc.ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    userRoles = UserRolesClientFactory.create(protectedService);
  }

  /**
   * Set the roles for the user
   * @param topazId a pre-existing user account id.
   * @param roleIds an array of role id literals to set for the user.
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.users.NoSuchUserIdException NoSuchUserIdException
   */
  public void setRole(final String topazId, final String[] roleIds) throws RemoteException, NoSuchUserIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    userRoles.setRoles(topazId, roleIds);
  }

  /**
   * Returns the roles associated to the topazId
   *
   * @param topazId a pre-existing user account id.
   * @return user roles
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.users.NoSuchUserIdException NoSuchUserIdException
   */
  public String[] getRoles(final String topazId) throws NoSuchUserIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return userRoles.getRoles(topazId);
  }
}
