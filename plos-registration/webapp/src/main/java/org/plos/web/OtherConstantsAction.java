/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.web;

import com.opensymphony.xwork.ActionSupport;
import org.plos.OtherConstants;

/**
 * An easy way to access any constants from the OtherConstants 
 */
public class OtherConstantsAction extends ActionSupport {
  private OtherConstants otherConstants;
  /**
   * @return webwork status
   * @throws Exception
   */
  public String execute() throws Exception {
    return SUCCESS;
  }

  /**
   * Getter for otherConstants.
   * @param key key of the object
   * @return Value for otherConstants.
   */
  public Object get(final String key) {
    return otherConstants.getValue(key);
  }

  /**
   * Setter for property otherConstants.
   * @param otherConstants Value to otherConstants.
   */
  public void setOtherConstants(final OtherConstants otherConstants) {
    this.otherConstants = otherConstants;
  }
}
