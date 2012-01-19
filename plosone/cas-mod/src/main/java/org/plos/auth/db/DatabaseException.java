/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/cas-mod/src/main/java/org#$
 * $Id: DatabaseException.java 649 2006-09-20 21:49:15Z viru $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.auth.db;

/**
 * Database error.
 */
public class DatabaseException extends Exception {
  public DatabaseException(final String message, final Throwable throwable) {
    super(message, throwable);
  }

  public DatabaseException(final String message) {
    super(message);
  }
}
