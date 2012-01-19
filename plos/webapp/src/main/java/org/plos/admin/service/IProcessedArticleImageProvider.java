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

import org.plos.article.util.ImageProcessingException;
import org.plos.article.util.ImageSetConfig;

/**
 * IProcessedArticleImageProvider - Inidicates the ability to provide processed
 * article images.
 * @author jkirton
 */
public interface IProcessedArticleImageProvider {

  /**
   * Provides an article image given a URL.
   * @param url The image url
   * @param imageSetConfig The {@link ImageSetConfig} instance to employ.
   * @param mimeType The "quasi", natively used, processeed article image
   *        mime-type that serves to identify a single processed image.
   *        <p>
   *        E.g.: PNG[_{S|M|L}]
   * @return ProcessedImageDataSource Containing the image data and the
   *         processed image mime-type String.
   * @throws ImageProcessingException When an image processing related error
   *         occurrs.
   */
  ProcessedImageDataSource getProcessedArticleImage(URL url, ImageSetConfig imageSetConfig,
      String mimeType) throws ImageProcessingException;
}
