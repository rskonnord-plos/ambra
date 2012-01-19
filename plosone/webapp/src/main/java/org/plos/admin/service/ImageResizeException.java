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
 * @author stevec
 *
 */
public class ImageResizeException extends Exception {
  private String articleURI;
  private String imageURI;
  
  public ImageResizeException (final String inArticleURI) {
    this.articleURI = inArticleURI;
    //this.imageURI = inImageURI;
  }

  /**
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * @return Returns the imageURI.
   */
  public String getImageURI() {
    return imageURI;
  }

  /**
   * @param imageURI The imageURI to set.
   */
  public void setImageURI(String imageURI) {
    this.imageURI = imageURI;
  }
  
  
}
