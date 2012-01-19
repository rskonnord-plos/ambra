/* $HeadURL::                                                                            $
 * $Id$
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
