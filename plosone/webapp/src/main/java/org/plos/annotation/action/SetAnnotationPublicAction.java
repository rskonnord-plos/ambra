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
 * Set the annotation as public.
 */
public class SetAnnotationPublicAction extends AnnotationActionSupport {
  private String targetId;
  private static final Log log = LogFactory.getLog(SetAnnotationPublicAction.class);

  /**
   * Set an annotation as public/private
   * @return Webwork status code
   * @throws Exception Exception
   */
  public String executeSetAnnotationPublic() throws Exception {
    try {
      getAnnotationService().setAnnotationPublic(targetId);
    } catch (final ApplicationException e) {
      log.error(e, e);
      addActionError("Annotation creation failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Getter for targetId.
   * @return Value of targetId.
   */
  @RequiredStringValidator(message = "Annotation id is a required field")
  public String getTargetId() {
    return targetId;
  }

  /**
   * Setter for targetId.
   * @param targetId Value to set for targetId.
   */
  public void setTargetId(final String targetId) {
    this.targetId = targetId;
  }
}
