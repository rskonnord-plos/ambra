/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.admin.service;


/**
 * ProcessedPngImageDataSource
 * @author jkirton
 */
public final class ProcessedPngImageDataSource extends ProcessedImageDataSource {
  
  private final String processedImageMimeType;

  /**
   * Constructor
   * @param content
   * @param processedImageMimeType
   */
  public ProcessedPngImageDataSource(byte[] content, String processedImageMimeType) {
    super(content, "image/png");
    assert processedImageMimeType != null : "The processed image mime-type must be specified";
    this.processedImageMimeType = processedImageMimeType;
  }

  @Override
  public String getProcessedImageMimeType() {
    return processedImageMimeType;
  }
  
  public String getName() {
    return "png";
  }
}