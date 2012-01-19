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



/**
 * Change the password action.
 */
public class NoOpAction extends BaseAction {
  public String execute() throws Exception {
    return SUCCESS;
  }

}
