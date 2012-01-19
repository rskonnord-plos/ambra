/* $$HeadURL::                                                                                               $$                                                                        $
 * $$Id ImageStorageService.java 2007-06-06 $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * This Stores an image from a given URL in a buffer and provides the images as
 * a byte array.
 *
 * @author jonnie
 */
public class ImageStorageService {
  private static final int DEFAULT_BUFFER_SIZE = 33554432;

  private final ByteArrayOutputStream buffer;

  public ImageStorageService() {
    this(DEFAULT_BUFFER_SIZE);
  }

  /**
   * Constructs the service and allocates memory storage of numberOfBytes in
   * length
   *
   * @param numberOfBytes the number of bytes to be allocated
   */
  public ImageStorageService(final int numberOfBytes) {
    buffer = new ByteArrayOutputStream(numberOfBytes);
  }

  /**
   * Retrieves the contents of the URL and stores them into memory for later
   * retrieval by clients.
   * 
   * @param url the url from which the content is to be obtained.
   * @throws ImageStorageServiceException
   */
  public void captureImage(final URL url) throws ImageStorageServiceException {
    try {
      final InputStream in = url.openStream();
      IOUtils.copyLarge(in, buffer);
    } catch (IOException e) {
      throw new ImageStorageServiceException("", e);
    }
  }

  /**
   * Provides the image in the form of a bytes array.
   *
   * @return the image as a byte array
   */
  public byte[] getBytes() {
    return buffer.toByteArray();
  }
}
