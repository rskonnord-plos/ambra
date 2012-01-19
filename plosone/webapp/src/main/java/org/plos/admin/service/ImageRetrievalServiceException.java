/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

/**
 * This is the exception used by the ImageRetrievalService to indicate the
 * failure of an operation.
 *
 * @author jonnie
 */
public class ImageRetrievalServiceException extends Exception {

  /**
   * Constructs an ImageRetrievalServiceException.
   * 
   * @param message textual indication of the cause of failure
   * @param cause   the Throwable object received by the caller which forced
   *                the creator to throw this exception
   */
  public ImageRetrievalServiceException(final String message,final Throwable cause) {
    super(message,cause);
  }

  /**
   * Constructs an ImageRetrievalServiceException.
   *
   * @param cause the Throwable object received by the caller which forced
   *              the creator to throw this exception
   */
  public ImageRetrievalServiceException(final Throwable cause) {
    super(cause);
  }
}
