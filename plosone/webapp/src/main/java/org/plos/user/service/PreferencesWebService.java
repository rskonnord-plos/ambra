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
import org.topazproject.ws.pap.Preferences;
import org.topazproject.ws.pap.PreferencesClientFactory;
import org.topazproject.ws.pap.UserPreference;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Simple wrapper class around Topaz preference web services 
 * 
 * @author Stephen Chneg
 * 
 */
public class PreferencesWebService extends BaseConfigurableService {
  private Preferences preferencesService;

  /**
   * Creates a preferences web service
   * 
   * @throws IOException
   * @throws URISyntaxException
   * @throws ServiceException
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    preferencesService = PreferencesClientFactory.create(protectedService);
  }

  /**
   * Retrieve user preferences for given Topaz user ID.
   * 
   * @param appId Application ID
   * @param topazUserId Topaz user ID
   * @return array of UserPreferences
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public UserPreference[] getPreferences(final String appId, final String topazUserId)
      throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return preferencesService.getPreferences(appId, topazUserId);
  }

  
  /**
   * Write user preferences to Topaz store
   * 
   * @param appId application ID
   * @param topazUserId Topaz user ID
   * @param prefs array of User Preferences to set
   * @throws NoSuchIdException
   * @throws RemoteException
   */
  public void setPreferences(final String appId, final String topazUserId, UserPreference[] prefs)
      throws NoSuchIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    preferencesService.setPreferences(appId, topazUserId, prefs);
  }
}
