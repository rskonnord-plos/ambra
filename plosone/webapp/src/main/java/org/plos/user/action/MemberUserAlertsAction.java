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
import org.plos.Constants;

import java.util.Map;

/**
 * User Alerts Action that is called by the member user to update their alerts preferences
 * (distinct from the one that might be called by admin to edit a user's preferences)
 */
public class MemberUserAlertsAction extends UserAlertsAction {
  @Override
  protected PlosOneUser getPlosOneUserToUse() {
    final Map<String, Object> sessionMap = getSessionMap();
    return (PlosOneUser) sessionMap.get(Constants.PLOS_ONE_USER_KEY);
  }
}
