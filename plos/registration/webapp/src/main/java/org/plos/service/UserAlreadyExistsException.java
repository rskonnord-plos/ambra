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
 * User already exists exception if the same user is being created again. 
 */
public class UserAlreadyExistsException extends Exception {
  /**
   * Constructor with loginName.
   * @param loginName name that already exists.
   */
  public UserAlreadyExistsException(final String loginName) {
    super(loginName);
  }
}
