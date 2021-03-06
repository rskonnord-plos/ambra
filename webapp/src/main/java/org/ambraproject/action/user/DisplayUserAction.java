/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.user;

import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class to display a user based on an id
 * 
 * @author Stephen Cheng
 * 
 */
public class DisplayUserAction extends UserActionSupport {
  private static final Logger log = LoggerFactory.getLogger(DisplayUserAction.class);

  private Long userId;

  /**
   * Returns the user based on the userId passed in.
   * 
   * @return webwork status string
   */
  @Override
  public String execute() throws Exception {
    UserProfile user;

    if (userId != null) {
      user = userService.getUser(userId);
    } else {
      return INPUT;
    }

    if(user == null) {
      return INPUT;
    }

    final String authId = this.getAuthId();

    // check if the user wants to show private fields
    boolean showPrivateFields = user.getOrganizationVisibility();
    if (!showPrivateFields) {

      //if they said no, still show them to admins and that same user
      try {
        permissionsService.checkPermission(Permission.MANAGE_USERS, authId);
        showPrivateFields = true;
      } catch(SecurityException ex) {
        log.debug("User does not have MANAGE_USERS permission");
        showPrivateFields = user.getAuthId().equals(authId);
      }
    }

    setFieldsFromProfile(userService.getProfileForDisplay(user, showPrivateFields));
    return SUCCESS;
  }

  /**
   * @param userId
   *          The userId to set.
   */
  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
