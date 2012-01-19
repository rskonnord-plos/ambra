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

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

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
