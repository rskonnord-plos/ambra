/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.topazproject.ws.users.NoSuchUserIdException;

/** 
 * This defines the preferences service. Preferences are stored as a list of name/values pairs.
 * The names must be unique; it is up to the application to define the valid names and associated
 * semantics.
 * 
 * @author Ronald Tschalär
 */
public interface Preferences extends Remote {
  /**
   * Permissions associated with the preferences service.
   */
  public static interface Permissions {
    /** The action that represents a write operation in XACML policies. */
    public static final String SET_PREFERENCES = "preferences:setPreferences";

    /** The action that represents a read operation in XACML policies. */
    public static final String GET_PREFERENCES = "preferences:getPreferences";
  }

  /** 
   * Get a user's preferences.
   * 
   * @param appId  the application id; may be null
   * @param userId the user's internal id
   * @return the user's preferences, or null if none exist for this user. Note that the order of the
   *         entries will be arbitrary.
   * @throws NoSuchUserIdException the user does not exist
   * @throws RemoteException if some error occured accessing the preferences
   */
  public UserPreference[] getPreferences(String appId, String userId)
      throws NoSuchUserIdException, RemoteException;

  /** 
   * Set a user's preferences. This completely overrides any previous settings.
   * 
   * @param appId  the application id; may only be null if <var>prefs</var> is also null, which
   *               causes all settings for the given user under all app-ids to be erased.
   * @param userId the user's internal id
   * @param prefs  the user's preferences; may be null in which case all settings are erased. Note
   *               that the order will not be preserved.
   * @throws NoSuchUserIdException the user does not exist
   * @throws RemoteException if some error occured accessing the preferences
   */
  public void setPreferences(String appId, String userId, UserPreference[] prefs)
      throws NoSuchUserIdException, RemoteException;
}
