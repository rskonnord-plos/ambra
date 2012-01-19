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

import org.plos.user.PlosOneUser;
import org.plos.ApplicationException;

/**
 * User Alerts Action that is called by the admin to update a user's alerts preferences
 * (distinct from the one that might be called by a member user to edit preferences)
 */
public class AdminUserAlertsAction extends UserAlertsAction {
  private String topazId;

  @Override
  protected PlosOneUser getPlosOneUserToUse() throws ApplicationException {
    return getUserService().getUserByTopazId(topazId);
  }

  /**
   * Setter for topazId.
   * @param topazId Value to set for topazId.
   */
  public void setTopazId(final String topazId) {
    this.topazId = topazId;
  }

  /**
   * Getter for topazId.
   * @return Value of topazId.
   */
  public String getTopazId() {
    return topazId;
  }

  /** Using by freemarker to indicate that the user profile is being edited by an admin */
  public boolean getIsEditedByAdmin() {
    return true;
  }
}
