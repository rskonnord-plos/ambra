/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos;

/**
 *
 */
 public class ApplicationException extends RuntimeException {
  public ApplicationException() {
  }

  public ApplicationException(final String message) {
    super(message);
  }

  /**
   * Constructor with message and exception.
   * @param message message
   * @param ex exception
   */
  public ApplicationException(final String message, final Exception ex) {
    super(message, ex);
  }
}
