/* $HeadURL::                                                                            $
 * $Id$
 *
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
