/* $HeadURL::                                                                                               $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.rating.service;

/**
 * This is the exception thrown by the RatingsService to indicate the failure of an operation.
 *
 * @author jonnie
 */
public class RatingsServiceException extends Exception {

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   *
   * @param message a textual indication of the cause of failure
   * @param cause   the Throwable object received by the caller which forced
   *                the creator to throw this exception.
   */
  public RatingsServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   *
   * @param cause the Throwable object received by the caller which forced
   *              the creator to throw this exception.
   **/
  public RatingsServiceException(final Throwable cause) {
    super(cause);
  }

  /**
   * Creates a RatingsServiceException to be thrown to the environment.
   * WARNING: Does not chain
   *
   * @param message a textual indication of the cause of failure
   */
  public RatingsServiceException(final String message) {
    super(message);
  }
}
