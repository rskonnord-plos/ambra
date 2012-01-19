/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service.impl;

import org.plos.service.PersistenceService;
import org.plos.service.UserDAO;

/**
 * Plos implementation of the Persistence Service.
 */
public class PlosPersistenceService implements PersistenceService {
  private UserDAO userDAO;

  /**
   * @see PersistenceService#setUserDAO(UserDAO)
   */
  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * @see org.plos.service.PersistenceService#getUserDAO() 
   */
  public UserDAO getUserDAO() {
    return userDAO;
  }
}
