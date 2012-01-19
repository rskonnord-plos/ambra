/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

/**
 * Skeleton action class to provide a getter/setter for the toGo parameter.  Here simply
 * for future use in case we want to do something on a redirect.
 * 
 * @author Stephen Cheng
 *
 */
public class RedirectAction extends BaseActionSupport {
  private String goTo;

  /**
   * This execute method always returns SUCCESS
   * 
   */
  public String execute() throws Exception {
    return SUCCESS;
  }

  /**
   * @return Returns the goTo.
   */
  public String getGoTo() {
    return goTo;
  }

  /**
   * @param goTo The goTo to set.
   */
  public void setGoTo(String goTo) {
    this.goTo = goTo;
  }
}
