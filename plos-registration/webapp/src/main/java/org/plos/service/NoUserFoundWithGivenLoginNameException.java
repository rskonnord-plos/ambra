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
 * No user found with given login name exception.
 */
public class NoUserFoundWithGivenLoginNameException extends Exception {
  /**
   * Constructor with loginName
   * @param loginName name of the user
   */
  public NoUserFoundWithGivenLoginNameException(final String loginName) {
    super(loginName);
  }
}
