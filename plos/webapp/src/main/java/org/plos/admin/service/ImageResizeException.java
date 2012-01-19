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

import java.net.URI;
/**
 * This is the exception used by the ImageResizeService to indicate the
 * failure of an operation.
 *
 * @author stevec
 */
public class ImageResizeException extends Exception {
  private URI articleURI;

  public ImageResizeException (final Throwable cause) {
    super(cause);
  }

  public ImageResizeException (final URI inArticleURI, final Throwable cause) {
    super(cause);
    this.articleURI = inArticleURI;
  }

  public ImageResizeException (final URI inArticleURI) {
    this.articleURI = inArticleURI;
    //this.imageURI = inImageURI;
  }

  public ImageResizeException (String message) {
    super(message);
  }

  public ImageResizeException (String message, Throwable cause) {
    super (message, cause);
  }

  /**
   * @return Returns the articleURI.
   */
  public URI getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(URI articleURI) {
    this.articleURI = articleURI;
  }

}
