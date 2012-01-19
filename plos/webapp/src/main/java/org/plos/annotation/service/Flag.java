/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.plos.ApplicationException;
import org.plos.annotation.FlagUtil;

/**
 * Used to flag an Annotaion or a Reply.
 */
public class Flag {
  private final WebAnnotation annotation;

  public Flag(final WebAnnotation annotation) {
    this.annotation = annotation;
  }

  public String getAnnotates() {
    return annotation.getAnnotates();
  }

  public String getCreator() {
    return annotation.getCreator();
  }

  public int getState() {
    return annotation.getState();
  }

  public String getId() {
    return annotation.getId();
  }

  public String getCreated() {
    return annotation.getCreated();
  }

  public String getComment() throws ApplicationException {
    return FlagUtil.getComment(getOriginalComment());
  }

  public String getReasonCode() throws ApplicationException {
    return FlagUtil.getReasonCode(getOriginalComment());
  }

  private String getOriginalComment() throws ApplicationException {
    return annotation.getOriginalBodyContent();
  }

  public boolean isDeleted() {
    return annotation.isDeleted();
  }
}
