/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.ws.article.ObjectInfo;
import org.topazproject.ws.article.RepresentationInfo;

/**
 * Wrapper around topaz's ObjectInfo.
 */
public class SecondaryObject {
  private final ObjectInfo objectInfo;
  private String repSmall;
  private String repMedium;
  private String repLarge;
  private String transformedDescription;
  private String transformedCaptionTitle;
  private String plainCaptionTitle;

  private static final Log log = LogFactory.getLog(SecondaryObject.class);
  
  public SecondaryObject(final ObjectInfo objectInfo, 
                         final String repSmall, final String repMedium, final String repLarge)  {
    this.objectInfo = objectInfo;
    this.repSmall = repSmall;
    this.repMedium = repMedium;
    this.repLarge = repLarge;

  }
  
  /**
   * @see org.topazproject.ws.article.ObjectInfo#getContextElement()
   * 
   * @return the context element of this object
   */
  public String getContextElement() {
    return objectInfo.getContextElement();
  }
  
  /**
   * @see org.topazproject.ws.article.ObjectInfo#getUri()
   */
  public String getUri() {
    return objectInfo.getUri();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getTitle()
   */
  public String getTitle() {
    
    return (objectInfo.getTitle() == null) ? "" : objectInfo.getTitle();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getDescription()
   */
  public String getDescription() {
    return (objectInfo.getDescription() == null) ? "" : objectInfo.getDescription();
  }

  /**
   * @see org.topazproject.ws.article.ObjectInfo#getRepresentations()
   */
  public RepresentationInfo[] getRepresentations() {
    return objectInfo.getRepresentations();
  }

  /**
   * @return the thumbnail representation for the images
   */
  public String getRepSmall() {
    return repSmall;
  }

  /**
   * @return the representation for medium size image
   */
  public String getRepMedium() {
    return repMedium;
  }

  /**
   * @return the representation for maximum size image
   */
  public String getRepLarge() {
    return repLarge;
  }

  /**
   * @return Returns the plainTitle.
   */
  public String getPlainCaptionTitle() {
    return (plainCaptionTitle == null) ? "" : plainCaptionTitle;
  }

  /**
   * @return Returns the transformedDescription.
   */
  public String getTransformedDescription() {
    return (transformedDescription == null) ? "" : transformedDescription;
  }

  /**
   * @return Returns the transformedTitle.
   */
  public String getTransformedCaptionTitle() {
    return (transformedCaptionTitle == null) ? "" : transformedCaptionTitle;
  }

  /**
   * @param plainTitle The plainTitle to set.
   */
  public void setPlainCaptionTitle(String plainTitle) {
    this.plainCaptionTitle = plainTitle;
  }

  /**
   * @param transformedDescription The transformedDescription to set.
   */
  public void setTransformedDescription(String transformedDescription) {
    this.transformedDescription = transformedDescription;
  }

  /**
   * @param transformedTitle The transformedTitle to set.
   */
  public void setTransformedCaptionTitle(String transformedTitle) {
    this.transformedCaptionTitle = transformedTitle;
  }

}
