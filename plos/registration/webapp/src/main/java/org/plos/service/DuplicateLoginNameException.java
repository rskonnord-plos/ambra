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

import org.plos.ApplicationException;

/**
 * Indicates that multiple users have been found with the same loginName. 
 * 
 */
public class DuplicateLoginNameException extends ApplicationException {

  /**
   * @param loginName loginName for which exception occured
   */
  public DuplicateLoginNameException(final String loginName) {
    super(loginName);
  }
}
