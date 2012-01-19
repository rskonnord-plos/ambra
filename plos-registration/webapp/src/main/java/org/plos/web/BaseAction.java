/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 * 
 */
package org.plos.web;

import com.opensymphony.xwork2.ActionSupport;
import org.plos.OtherConstants;



/**
 * @author stevec
 *
 */
public class BaseAction extends ActionSupport {
  private OtherConstants otherConstants;

  /**
   * @return Returns the otherConstants.
   */
  public OtherConstants getOtherConstants() {
    return otherConstants;
  }

  /**
   * @param otherConstants The otherConstants to set.
   */
  public void setOtherConstants(OtherConstants otherConstants) {
    otherConstants = otherConstants;
  }
  
}
