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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Search a user based on a criteria
 */
public class SearchUserAction extends UserActionSupport {
  private String authId;
  private String emailAddress;
  private String[] topazUserIdList;

  private static final Log log = LogFactory.getLog(SearchUserAction.class);

  /**
   * Find user with a given auth id
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeFindUserByAuthId() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with AuthID: " + authId);
      }
      final String topazUserId = getUserService().lookUpUserByAuthId(authId);
      if (null == topazUserId) {
        throw new ApplicationException("No user found with the authid:" + authId);
      }
      topazUserIdList = new String[]{topazUserId};
    } catch (final ApplicationException ex) {
      addFieldError("authId", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }

  /**
   * Find user with a given email address
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeFindUserByEmailAddress() throws Exception {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Finding user with email: " + emailAddress);
      }
      final String topazUserId = getUserService().lookUpUserByEmailAddress(emailAddress);
      if (null == topazUserId) {
        throw new ApplicationException("No user found with the email address:" + emailAddress);
      }
      topazUserIdList = new String[]{topazUserId};
    } catch (final ApplicationException ex) {
      addFieldError("emailAddress", ex.getMessage());
      return INPUT;
    }

    return SUCCESS;
  }


  /**
   * Getter for authId.
   * @return Value of authId.
   */
  public String getAuthId() {
    return authId;
  }

  /**
   * Setter for authId.
   * @param authId Value to set for authId.
   */
  public void setAuthId(final String authId) {
    this.authId = authId;
  }

  /**
   * Getter for emailAddress.
   * @return Value of emailAddress.
   */
  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * Setter for emailAddress.
   * @param emailAddress Value to set for emailAddress.
   */
  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Getter for topazUserIdList.
   * @return Value of topazUserIdList.
   */
  public String[] getTopazUserIdList() {
    return topazUserIdList;
  }
}
