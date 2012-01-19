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

import org.plos.ApplicationException;
import org.plos.util.FileUtils;

import java.io.IOException;

/**
 * This is a worker class that would have/or be supplied the logic to retrieve functionality to fetch the values when requested for.
 * Fetch extra annotation properties on demand/lazily.
 * This will also cache the values already fetched.
 */
public class AnnotationLazyLoader {
  private String bodyContent;
  private final String bodyUrl;

  /**
   * @param bodyUrl bodyUrl
   */
  public AnnotationLazyLoader(final String bodyUrl) {
    this.bodyUrl = bodyUrl;
  }

  /**
   * @return the body of the annotation
   * @throws ApplicationException
   */
  public String getBody() throws ApplicationException {
    if (null == bodyContent) {
      bodyContent = getBodyContent(bodyUrl);
    }
    return bodyContent;
  }

  /**
   * Fetch the body content from the given url
   * @param bodyUrl bodyUrl
   * @return the body
   * @throws ApplicationException
   */
  protected static String getBodyContent(final String bodyUrl) throws ApplicationException {
    try {
      return FileUtils.getTextFromUrl(bodyUrl);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

}

