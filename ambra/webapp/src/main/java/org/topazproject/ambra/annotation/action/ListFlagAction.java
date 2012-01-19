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

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.service.Flag;

/**
 * Action class to get a list of flags for a given uri.
 */
public class ListFlagAction extends AnnotationActionSupport {
  private String target;
  private Flag[] flags;

  private static final Log log = LogFactory.getLog(ListFlagAction.class);

  /**
   * List flags.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
    try {
      flags = getAnnotationService().listFlags(target);
    } catch (final ApplicationException e) {
      log.error("Could not list flags for target: " + target, e);
      addActionError("Flag fetch failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * @return a list of flags
   */
  public Flag[] getFlags() {
    return flags;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @return the target of the annotation
   */
  @RequiredStringValidator(message="You must specify the target that you want to list the flags for")
  public String getTarget() {
    return target;
  }

}
