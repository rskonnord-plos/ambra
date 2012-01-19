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
 * To be used when a user who has been already verified requests a verification again
 */
public class UserAlreadyVerifiedException extends Exception {
  /**
   * Constructor with loginName
   * @param loginName loginName
   */
  public UserAlreadyVerifiedException(final String loginName) {
    super(loginName);
  }
}
