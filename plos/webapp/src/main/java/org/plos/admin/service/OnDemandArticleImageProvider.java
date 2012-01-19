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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.util.ImageProcessingException;
import org.plos.article.util.ImageResizeException;
import org.plos.article.util.ImageResizeService;
import org.plos.article.util.ImageSetConfig;

/**
 * OnDemandArticleImageProvider - Performs image processing on demand.
 * @author jkirton
 */
final class OnDemandArticleImageProvider implements IProcessedArticleImageProvider {
  static final Log log = LogFactory.getLog(OnDemandArticleImageProvider.class);

  public ProcessedImageDataSource getProcessedArticleImage(URL url, ImageSetConfig imageSetConfig,
      String mimeType) throws ImageProcessingException {
    ImageStorageService iss = new ImageStorageService();
    iss.captureImage(url);
    final byte[] originalBytes = iss.getBytes();
    ImageResizeService irs = new ImageResizeService(imageSetConfig);
    byte[] bytes;
    if ("PNG_S".equals(mimeType)) {
      bytes = irs.getSmallScaledImage(originalBytes);
    }
    else if ("PNG_M".equals(mimeType)) {
      bytes = irs.getMediumScaledImage(originalBytes);
    }
    else if ("PNG_L".equals(mimeType) || "PNG".equals(mimeType)) {
      bytes = irs.getLargeScaledImage(originalBytes);
    }
    else {
      throw new ImageResizeException("Unhandled mime-type: " + mimeType);
    }
    return new ProcessedPngImageDataSource(bytes, mimeType);
  }
  
  @Override
  public String toString() {
    return getClass().getName();
  }
}
