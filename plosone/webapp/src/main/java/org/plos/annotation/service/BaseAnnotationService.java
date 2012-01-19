/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import org.plos.service.BaseConfigurableService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base class for Annotaion and Reply web service wrappers
 */
public abstract class BaseAnnotationService extends BaseConfigurableService {
  private String defaultType;
  private String encodingCharset = "UTF-8";
  private String applicationId;
  private boolean isAnonymous;

  /**
   * Set the default annotation type.
   * @param defaultType defaultType
   */
  public void setDefaultType(final String defaultType) {
    this.defaultType = defaultType;
  }

  /**
   * Set the id of the application
   * @param applicationId applicationId
   */
  @Required
  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * @return the encoding charset
   */
  public String getEncodingCharset() {
    return encodingCharset;
  }

  /**
   * @param encodingCharset charset for encoding the data to be persisting in
   */
  public void setEncodingCharset(final String encodingCharset) {
    this.encodingCharset = encodingCharset;
  }

  /**
   * Set whether the user isAnonymous.
   * @param isAnonymous true if user isAnonymous
   */
  public void setAnonymous(final boolean isAnonymous) {
    this.isAnonymous = isAnonymous;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  /**
   * @return the default type for the annotation or reply
   */
  public String getDefaultType() {
    return defaultType;
  }

  /**
   * @return the application id
   */
  public String getApplicationId() {
    return applicationId;
  }

  protected String getContentType(final String mimeType) {
    return mimeType + ";charset=" + getEncodingCharset();
  }
}
