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
 * To manage the interactions with the persistence service, including database persistence.
 */
public interface PersistenceService {
  /**
   * Sets the UserDAO
   * @param userDAO UserDAO
   */
  void setUserDAO(final UserDAO userDAO);

  /**
   * @return UserDAO
   */
  UserDAO getUserDAO();
}
