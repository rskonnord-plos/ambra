/* $$HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/webapp/src/main/#$$
 * $$Id: ImageRetrievalServiceException.java 5139 2008-03-21 23:17:26Z jkirton $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.util;


/**
 * This is the exception used by the ImageRetrievalService to indicate the
 * failure of an operation.
 *
 * @author jonnie
 */
public class ImageRetrievalServiceException extends ImageProcessingException {
  private static final long serialVersionUID = -3175658452639416889L;

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
