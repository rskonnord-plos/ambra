/* $$HeadURL:: http://gandalf.topazproject.org/svn/branches/0.8.2.2/plos/webapp/src/main/java/org/plos/admin#$$                                                                        $
 * $$Id ImageRetrievalService.java 2007-05-31 $$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class performs the operations of conversion and resizing of images.
 * The final image format is assumed to be png. The desired image sizes are
 * passed to the public methods.
 *
 * @author jonnie 
 */
public class ImageRetrievalService {
  private static int BUFFER_SIZE = 8192;

  public ImageRetrievalService() {
  }

  /**
   * This method is used to copy image data from an input stream to an output stream.
   * @param in  the input stream to read from
   * @param out the output stream to write to
   * @throws ImageRetrievalServiceException
   */
  public void transferImage(final InputStream in,final OutputStream out)
        throws ImageRetrievalServiceException {
    try {
      int bytesReadTotal = 0;
      int length = BUFFER_SIZE;
      byte[] buffer = new byte[length];
      int bytesRead;

      do {
        bytesRead = 0;
        int offset = 0;
        int remainingBytes = (length - offset);

        while (bytesRead != -1) {
          offset = offset + bytesRead;
          remainingBytes = remainingBytes - bytesRead;
          assert(remainingBytes + offset == length);
          bytesReadTotal = bytesReadTotal + bytesRead;

          if (remainingBytes <= 0) {
            assert(remainingBytes == 0);
            break;
          }

          bytesRead = in.read(buffer,offset,remainingBytes);
        }

        remainingBytes = offset;
        offset = 0;
        out.write(buffer,offset,remainingBytes);
      } while (bytesRead != -1);
    } catch (IOException e) {
      throw new ImageRetrievalServiceException("",e);
    }
  }
}
