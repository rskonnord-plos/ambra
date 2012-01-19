/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.permission.service;

import org.plos.service.BaseConfigurableService;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.permissions.Permissions;
import org.topazproject.ws.permissions.PermissionsClientFactory;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper on the topaz permission web service.
 */
public class PermissionWebService extends BaseConfigurableService {
  private Permissions permissionsService;
  private String currentPrincipal;

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = getProtectedService();
    permissionsService = PermissionsClientFactory.create(permissionProtectedService);
  }

  /**
   * @param resource resource
   * @return a list of grants for the current user/principal
   * @throws RemoteException
   */
  public String[] listGrants(final String resource) throws RemoteException {
    return listGrants(resource, getCurrentPrincipal());
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listGrants(String, String)
   *
   * @param resource resource
   * @param principal principal
   * @return a list of grants
   * @throws java.rmi.RemoteException
   */
  public String[] listGrants(final String resource, final String principal) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return permissionsService.listGrants(resource, principal);
  }

  /**
   * @return the current user's principle
   */
  public String getCurrentPrincipal() {
    return currentPrincipal;
  }

  /**
   * Set the current principle.
   * @param currentPrincipal currentPrincipal
   */
  public void setCurrentPrincipal(final String currentPrincipal) {
    this.currentPrincipal = currentPrincipal;
  }

  /**
   * Grants permissions.
   * @see org.topazproject.ws.permissions.Permissions#grant(String, String[], String[])
   * @param resource resource
   * @param permissions permissions
   * @param principals principals
   * @throws RemoteException
   */
  public void grant(String resource, String[] permissions, String[] principals)
          throws RemoteException
  {
    ensureInitGetsCalledWithUsersSessionAttributes();
    permissionsService.grant(resource, permissions, principals);
  }

  /**
   * Grant a permission to the currentPrincipal
   * @param resource resource
   * @param permissions permissions
   * @throws java.rmi.RemoteException
   */
  public void grant(final String resource, final String[] permissions) throws RemoteException {
    grant(resource, permissions, currentPrincipal);
  }

  private void grant(final String resource, final String[] permissions, final String principal)
          throws RemoteException
  {
    grant(resource, permissions, new String[]{principal});
  }

  /**
   * Revoke permissions.
   * @param resource resource
   * @param permissions permissions
   * @param principals principals
   * @throws RemoteException
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException
  {
    ensureInitGetsCalledWithUsersSessionAttributes();
    permissionsService.revoke(resource, permissions, principals);
  }

  /**
   * Revoke permission for the currentPrincipal
   * @param resource resource
   * @param permissions permissions
   * @throws RemoteException
   */
  public void revoke(final String resource, final String[] permissions) throws RemoteException {
    revoke(resource, permissions, currentPrincipal);
  }

  private void revoke(final String resource, final String[] permissions, final String principal)
          throws RemoteException
  {
    revoke(resource, permissions, new String[]{principal});
  }

  /**
   * Cancel grants.
   * @param resource resource
   * @param grants grants
   * @param principals principals
   * @throws RemoteException
   * @see org.topazproject.ws.permissions.Permissions#cancelGrants(String, String[], String[])
   */
  public void cancelGrants(final String resource, final String[] grants, final String[] principals) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    permissionsService.cancelGrants(resource, grants, principals);
  }

  /**
   * Cancel revokations
   * @param resource resource
   * @param grants grants
   * @param principals principals
   * @throws RemoteException
   * @see org.topazproject.ws.permissions.Permissions#cancelRevokes(String, String[], String[])
   */
  public void cancelRevokes(final String resource, final String[] grants, final String[] principals) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    permissionsService.cancelRevokes(resource, grants, principals);
  }

  /**
   * List revokes.
   * @param resource resource
   * @param principal principal
   * @return a list of revokations for this principal
   * @throws RemoteException
   * @see org.topazproject.ws.permissions.Permissions#listRevokes(String, String)
   */

  public String[] listRevokes(final String resource, final String principal) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return permissionsService.listRevokes(resource, principal);
  }
}
