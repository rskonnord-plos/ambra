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
 * Password does not match the loginName exception.
 */
public class PasswordInvalidException extends Exception {
  /**
   * Constructor with loginName and password.
   * @param loginName loginName
   * @param password password
   */
  public PasswordInvalidException(final String loginName, final String password) {
    super("loginName:"+ loginName + ",password:"+ password);
  }
}
