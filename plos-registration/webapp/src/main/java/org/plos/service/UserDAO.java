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

import org.plos.registration.User;

/**
 * Contract for all User DAO's.
 */
 public interface UserDAO {

  /**
   * Save or update the user
   * @param user User
   */
  void saveOrUpdate(final User user);

  /**
   * Delete user
   * @param user User
   */
  void delete(final User user);


  /**
   * Find user with a given login name.
   * @param loginName
   * @return User
   */
  User findUserWithLoginName(final String loginName);

}

