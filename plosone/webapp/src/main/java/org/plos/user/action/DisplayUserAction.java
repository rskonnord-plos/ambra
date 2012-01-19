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

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.user.PlosOneUser;
import org.plos.user.UserProfileGrant;

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

  private PlosOneUser pou;
  private String userId;
  private Collection<String> privateFields;

  /**
   * Returns the user based on the userId passed in.
   * 
   * @return webwork status string
   */
  public String execute() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("retrieving user profile for: " + userId);
    }
    pou = new PlosOneUserDecorator(getUserService().getUserWithProfileLoaded(userId));
    return SUCCESS;
  }

  /**
   * Returns the user profile fields names that have private visibility.
   *
   * @return webwork status string
   * @throws Exception Exception when it fails
   */
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
  public PlosOneUser getPou() {
    return pou;
  }

  /**
   * @param pou
   *          The pou to set.
   */
  public void setPou(PlosOneUser pou) {
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
