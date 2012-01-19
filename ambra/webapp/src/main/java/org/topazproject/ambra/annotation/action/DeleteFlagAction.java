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
package org.topazproject.ambra.annotation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.ApplicationException;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

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
