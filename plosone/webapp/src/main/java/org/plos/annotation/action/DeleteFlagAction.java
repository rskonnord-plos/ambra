/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;

/**
 * Action to delete a flag
 */
public class DeleteFlagAction extends AnnotationActionSupport {
  private String flagId;

  private static final Log log = LogFactory.getLog(DeleteFlagAction.class);

  /**
   * Flag deletion action.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      getAnnotationService().deleteFlag(flagId);
    } catch (final ApplicationException e) {
      log.error("Could not delete flag: " + flagId, e);
      addActionError("Flag deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Flag deleted with id:" + flagId);
    return SUCCESS;
  }

  /**
   * Set the flag Id.
   * @param flagId flagId
   */
  public void setFlagId(final String flagId) {
    this.flagId = flagId;
  }

  /**
   * @return the flag id
   */
  @RequiredStringValidator(message="You must specify the id of the flag that you want to delete")
  public String getFlagId() {
    return flagId;
  }
}
