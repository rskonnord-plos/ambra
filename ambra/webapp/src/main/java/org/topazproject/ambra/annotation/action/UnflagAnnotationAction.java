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

/**
 * Mark an annotation as unflagged.
 */
public class UnflagAnnotationAction extends AnnotationActionSupport {
  private String targetId;

  private static final Log log = LogFactory.getLog(UnflagAnnotationAction.class);

  /**
   * Unflag the Annotation.
   * @return status
   * @throws Exception Exception
   */
  public String unflagAnnotation() throws Exception {
    try {
      getAnnotationService().unflagAnnotation(targetId);
    } catch (final ApplicationException e) {
      log.error("Could not unflag annotation: " + targetId, e);
      addActionError("Annotation unflagging failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Annotation unflagged with id:" + targetId);
    return SUCCESS;
  }

  /**
   * Unflag the Reply.
   * @return status
   * @throws Exception Exception
   */
  public String unflagReply() throws Exception {
    try {
      getAnnotationService().unflagReply(targetId);
    } catch (final ApplicationException e) {
      log.error("Could not unflag reply: " + targetId, e);
      addActionError("Reply unflagging failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Reply unflagged with id:" + targetId);
    return SUCCESS;
  }

  /**
   * Set the annotation Id.
   * @param targetId targetId
   */
  public void setTargetId(final String targetId) {
    this.targetId = targetId;
  }

  /**
   * @return the annotation id
   */
  @RequiredStringValidator(message = "You must specify the id of the annotation/reply that you want to unflag")
  public String getTargetId() {
    return targetId;
  }
}
