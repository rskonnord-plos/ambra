/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/cas-mod/src/main/java/org#$
 * $Id: UserService.java 649 2006-09-20 21:49:15Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.service;

import org.plos.auth.db.DatabaseException;
import org.plos.auth.db.DatabaseContext;

import java.sql.SQLException;

/**
 * Used to fetch the various properties, like guid, for a given user.
 */
public class UserService {
  private final DatabaseContext context;
  private final String usernameToGuidSql;
  private final String guidToUsernameSql;

  public UserService(final DatabaseContext context, final String usernameToGuidSql, final String guidToUsernameSql) {
    this.usernameToGuidSql = usernameToGuidSql;
    this.guidToUsernameSql = guidToUsernameSql;
    this.context = context;
  }

  /**
   * Given a loginname it will return the guid for it from the database
   * @param loginname loginname
   * @return the guid for the loginname
   * @throws DatabaseException DatabaseException
   */
  public String getGuid(final String loginname) throws DatabaseException {
    try {
      return context.getSingleStringValueFromDb(usernameToGuidSql, loginname);
    } catch (SQLException e) {
      throw new DatabaseException("Unable to get loginame from db", e);
    }
  }

  /**
   * Given a guid it will return the username for it from the database
   * @param guid guid
   * @return the guid for the guid
   * @throws DatabaseException DatabaseException
   */
  public String getEmailAddress(final String guid) throws DatabaseException {
    try {
      return context.getSingleStringValueFromDb(guidToUsernameSql, guid);
    } catch (SQLException e) {
      throw new DatabaseException("Unable to get email address from db", e);
    }
  }
}
