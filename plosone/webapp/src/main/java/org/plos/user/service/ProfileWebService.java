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
import org.topazproject.common.NoSuchIdException;
import org.topazproject.ws.pap.Profiles;
import org.topazproject.ws.pap.ProfilesClientFactory;
import org.topazproject.ws.pap.UserProfile;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper class for Topaz profile web service
 * 
 * @author Stephen Cheng
 * 
 */
public class ProfileWebService extends BaseConfigurableService {

  private Profiles profileService;

  /**
   * Creates the profiles web service
   * 
   * @throws IOException
   * @throws URISyntaxException
   * @throws ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    profileService = ProfilesClientFactory.create(protectedService);
  }

  /**
   * Retrieves UserProfile for a given Topaz UserId
   * 
   * @param topazUserId Topaz User ID
   * @return profile of given user
   * @throws NoSuchIdException NoSuchIdException
   * @throws RemoteException RemoteException
   */
  public UserProfile getProfile(final String topazUserId) throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return profileService.getProfile(topazUserId);
  }

  /**
   * Store UserProfile for a given Topaz UserID
   * 
   * @param topazUserId Topaz User ID
   * @param profile Profile to store
   * @throws NoSuchIdException NoSuchIdException
   * @throws RemoteException RemoteException
   */
  public void setProfile(final String topazUserId, final UserProfile profile) throws NoSuchIdException,
      RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    profileService.setProfile(topazUserId, profile);
  }

  /**
   * Find the user with this display name ignoring the case
   * @param displayName displayName
   * @throws RemoteException RemoteException
   * @return the userId with the display name
   */
  public String getUserWithDisplayName(final String displayName) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    final UserProfile userTemplate = new UserProfile();
    userTemplate.setDisplayName(displayName);
    final String[] userIds = profileService.findUsersByProfile(new UserProfile[]{userTemplate}, new boolean[]{true});
    if ((null != userIds) && (userIds.length > 0)) {
      return userIds[0];
    }
    return null;
  }
}
