/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.Flag;

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
