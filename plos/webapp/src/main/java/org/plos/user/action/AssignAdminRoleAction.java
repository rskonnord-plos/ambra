/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.Constants;

/**
 * Creates a new admin user in Topaz. User must be logged in already.
 */
public class AssignAdminRoleAction extends UserActionSupport {
  private static final Log log = LogFactory.getLog(AssignAdminRoleAction.class);
  private String topazId;

  /**
   * Assign the given topazId an admin role.
   * @return status code from webwork
   * @throws Exception Exception
   */
  public String execute() throws Exception {
    return assignAdminRole(topazId);
  }

  private String assignAdminRole(final String topazId) throws ApplicationException {
    getUserService().setRole(topazId, Constants.ADMIN_ROLE);

    final String successMessage = "Topaz ID: " + topazId + " successfully assigned the role: " + Constants.ADMIN_ROLE;
    addActionMessage(successMessage);
    log.debug(successMessage);

    return SUCCESS;
  }

  /**
   * Getter for topazId
   * @return Value for property topazId.
   */
  @RequiredStringValidator(message = "Topaz id is required.")
  public String getTopazId() {
    return topazId;
  }

  /**
   * Setter for topazId
   * @param topazId Value to set for topazId.
   */
  public void setTopazId(final String topazId) {
    this.topazId = topazId;
  }
}
