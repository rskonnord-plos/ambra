/* $HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/webapp/src/main/#$
 * $Id: ImageResizeException.java 5139 2008-03-21 23:17:26Z jkirton $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.util;


/**
 * This is the exception used by the ImageResizeService to indicate the
 * failure of an operation.
 *
 * @author stevec, jkirton
 */
public class ImageResizeException extends ImageProcessingException {
  private static final long serialVersionUID = 2256237883612015143L;

  /**
   * Constructor
   * @param message
   * @param cause
   */
  public ImageResizeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor
   * @param message
   */
  public ImageResizeException(String message) {
    super(message);
  }

  /**
   * Constructor
   * @param cause
   */
  public ImageResizeException(Throwable cause) {
    super(cause);
  }
}
