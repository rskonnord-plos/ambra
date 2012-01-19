/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util;

import java.net.URI;

/**
 * ImageProcessingException - Common base class for image processing related exceptions.
 * @author jkirton
 */
public class ImageProcessingException extends Exception {

  private static final long serialVersionUID = 1989216981562835187L;

  private URI articleURI;

  public ImageProcessingException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructor
   * @param message
   */
  public ImageProcessingException(String message) {
    super(message);
  }

  /**
   * Constructor
   * @param message
   * @param cause
   */
  public ImageProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor
   * @param message
   * @param cause
   * @param articleURI
   */
  public ImageProcessingException(String message, Throwable cause, URI articleURI) {
    super(message, cause);
    this.articleURI = articleURI;
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
