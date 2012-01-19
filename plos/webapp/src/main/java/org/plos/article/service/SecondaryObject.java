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

import java.io.Serializable;

import org.plos.models.ObjectInfo;

/**
 * Wrapper around topaz's ObjectInfo.
 */
public class SecondaryObject implements Serializable {

  private final ObjectInfo objectInfo;
  private String repSmall;
  private String repMedium;
  private String repLarge;
  private String transformedDescription;
  private String transformedCaptionTitle;
  private String plainCaptionTitle;

  static final long serialVersionUID = 7439718780407844715L;

  public SecondaryObject(final ObjectInfo objectInfo,
                         final String repSmall, final String repMedium, final String repLarge)  {
    this.objectInfo = objectInfo;
    this.repSmall = repSmall;
    this.repMedium = repMedium;
    this.repLarge = repLarge;

  }

  /**
   * @see ObjectInfo#getContextElement()
   * 
   * @return the context element of this object
   */
  public String getContextElement() {
    return objectInfo.getContextElement();
  }

  /**
   * @see ObjectInfo#getId()
   */
  public String getUri() {
    return objectInfo.getId().toString();
  }

  /**
   * @see org.plos.models.DublinCore#getTitle()
   */
  public String getTitle() {
    String title = objectInfo.getDublinCore().getTitle();
    return (title == null) ? "" : title;
  }

  /**
   * @see org.plos.models.DublinCore#getDescription()
   */
  public String getDescription() {
    String description = objectInfo.getDublinCore().getDescription();
    return (description == null) ? "" : description;
  }

  /**
   * @see ObjectInfo#getRepresentations()
   */
  public RepresentationInfo[] getRepresentations() {
    return RepresentationInfo.parseObjectInfo(objectInfo);
  }

  /**
   * @see ObjectInfo#getDoi()
   */
  public String getDoi() {
    // TODO: doi: munging not really resolved
    return null;  // objectInfo.getDoi();
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
