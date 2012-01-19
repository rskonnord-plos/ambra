/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.service;
/**
 * User has not been verified exception. May happen if the user never verified his email address
 */
public class UserNotVerifiedException extends Exception {
  public UserNotVerifiedException(final String loginName) {
    super(loginName);
  }
}
