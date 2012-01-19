/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service.password;

/**
 * Password service exception
 */
public class PasswordServiceException extends Exception {
  public PasswordServiceException() {
  }

  public PasswordServiceException(final String message) {
    super(message);
  }

  /**
   * @param message message
   * @param exception exception
   * @see Exception#Exception(String, Throwable)
   */
  public PasswordServiceException(final String message, final Exception exception) {
    super(message, exception);
  }
}
