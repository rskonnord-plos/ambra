/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.action;

import org.plos.ApplicationException;
import org.plos.user.PlosOneUser;

/**
 * User Profile Action to be used by the admin to update the profile of any member user
 * (distinct from the one that might be called by member user themselves to edit their profile)
 */
public class AdminUserProfileAction extends UserProfileAction {
  private boolean userIsHalfCreated = false;

  @Override
  protected PlosOneUser getPlosOneUserToUse() throws ApplicationException {
    final PlosOneUser plosOneUser = getUserService().getUserByTopazId(getTopazId());

    //To be used to fix the partial created user profile for a user
    if (null == plosOneUser.getUserProfile()) {
      assignUserProfileToPlosUser(plosOneUser);
      setDisplayNameRequired(false);
      userIsHalfCreated = true;
    }

    return plosOneUser;
  }

  @Override
  public String executeRetrieveUserProfile() throws Exception {
    final String status = super.executeRetrieveUserProfile();
    if (userIsHalfCreated) {
      setEmail(fetchUserEmailAddress());
    }
    return status;
  }

  @Override
  protected String getUserIdToFetchEmailAddressFor() throws ApplicationException {
    return getUserService().getAuthenticationId(getTopazId());
  }

  /** Using by freemarker to indicate that the user profile is being edited by an admin */
  public boolean getIsEditedByAdmin() {
    return true;
  }
}
