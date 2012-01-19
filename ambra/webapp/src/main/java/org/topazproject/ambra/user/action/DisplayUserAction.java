/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
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


package org.topazproject.ambra.user.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.user.UserProfileGrant;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple class to display a user based on a TopazId
 * 
 * @author Stephen Cheng
 * 
 */
public class DisplayUserAction extends UserActionSupport {

  private static final Log log = LogFactory.getLog(DisplayUserAction.class);

  private AmbraUser pou;
  private String userId;
  private Collection<String> privateFields;

  /**
   * Returns the user based on the userId passed in.
   * 
   * @return webwork status string
   */
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("retrieving user profile for: " + userId);
    }
    pou = new AmbraUserDecorator(getUserService().getUserWithProfileLoaded(userId));
    return SUCCESS;
  }

  /**
   * Returns the user profile fields names that have private visibility.
   *
   * @return webwork status string
   * @throws Exception Exception when it fails
   */
  @Transactional(readOnly = true)
  public String fetchUserProfileWithPrivateVisibility() throws Exception {
    final Collection<UserProfileGrant> privateGrants = getUserService().getProfileFieldsThatArePrivate(userId);

    privateFields = new ArrayList<String>(privateGrants.size());
    for (final UserProfileGrant userProfileGrant : privateGrants) {
      privateFields.add(userProfileGrant.getFieldName());
    }
    return SUCCESS;
  }

  /**
   * @return Returns the pou.
   */
  public AmbraUser getPou() {
    return pou;
  }

  /**
   * @param pou
   *          The pou to set.
   */
  public void setPou(AmbraUser pou) {
    this.pou = pou;
  }

  /**
   * @return Returns the userId.
   */
  @RequiredStringValidator(message = "Topaz id is required.")
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId
   *          The userId to set.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return the field names that are private.
   */
  public Collection<String> getPrivateFields() {
    return privateFields;
  }

}
