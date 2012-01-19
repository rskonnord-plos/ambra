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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.Constants;

/**
 * Creates a new admin user in Topaz. User must be logged in already.
 */
public class AssignAdminRoleAction extends UserActionSupport {
  private static final Logger log = LoggerFactory.getLogger(AssignAdminRoleAction.class);
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

    final String successMessage = "Topaz ID: " + topazId + " successfully assigned the role: " +
                                  Constants.ADMIN_ROLE;
    addActionMessage(successMessage);
    if (log.isDebugEnabled()) { log.debug(successMessage); }

    return SUCCESS;
  }

  /**
   * Struts setter for topazId.
   * 
   * @param topazId Value to set for topazId.
   */
  @RequiredStringValidator(message = "Id is required.")
  public void setTopazId(final String topazId) {
    this.topazId = topazId;
  }

  /**
   * Struts getter for topazId.
   * 
   * @return Topaz Id.
   */
  public String getTopazId() {
    return topazId;
  }
}
