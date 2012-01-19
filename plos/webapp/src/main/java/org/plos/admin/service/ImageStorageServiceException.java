/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

/**
 * This is the exception thrown by the ImageStorageService to indicate the
 * failure of an operation.
 *
 * @author jonnie
 */
public class ImageStorageServiceException extends Exception {

  /**
   * Creates an ImageStorageServiceException to be thrown to the environment
   *
   * @param message a textual indication of the cause of failure
   * @param cause   the Throwable object received by the caller which forced
   *                the creator to throw this exception.
   */
  public ImageStorageServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  /**
   * Creates an ImageStorageServiceException to be thrown to the environment
   *
   * @param cause the Throwable object received by the caller which forced
   *              the creator to throw this exception.
   **/
  public ImageStorageServiceException(final Throwable cause) {
    super(cause);
  }
}
