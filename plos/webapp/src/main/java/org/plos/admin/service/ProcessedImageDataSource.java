package org.plos.admin.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * ProcessedImageDataSource
 * @author jkirton
 */
public abstract class ProcessedImageDataSource implements DataSource {
  private final byte[] src;

  private final String ct;

  /**
   * Constructor
   * @param content
   * @param contType
   */
  public ProcessedImageDataSource(byte[] content, String contType) {
    src = content;
    ct = contType;
  }
  
  /**
   * @return A natively used String that identifies the type of procesed
   *         image.
   */
  public abstract String getProcessedImageMimeType();

  public final InputStream getInputStream() /* throws IOException */ {
    return new ByteArrayInputStream(src);
  }

  public final OutputStream getOutputStream() throws IOException {
    throw new IOException("Not supported");
  }

  public final String getContentType() {
    return ct;
  }
}