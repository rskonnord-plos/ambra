/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plos.annotation.service;

import org.springframework.beans.factory.annotation.Required;

/**
 * Base class for Annotaion and Reply web service wrappers
 */
public abstract class BaseAnnotationService {
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
