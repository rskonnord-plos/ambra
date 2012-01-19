/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;

import org.topazproject.authentication.ProtectedService;

/**
 * Fedora uploader client.
 *
 * @author Pradeep Krishnan
 */
public class Uploader {
  private static MultiThreadedHttpConnectionManager connectionManager =
    new MultiThreadedHttpConnectionManager();

  //
  private HttpClient       client;
  private ProtectedService service;

  static {
    // xxx: tune this
    connectionManager.setMaxConnectionsPerHost(100);
    connectionManager.setMaxTotalConnections(100);
  }

  /**
   * Creates a new Uploader object.
   *
   * @param service The uploader service configuration
   */
  public Uploader(ProtectedService service) {
    this.service   = service;
    client         = new HttpClient(connectionManager);

    if (service.requiresUserNamePassword()) {
      client.getParams().setAuthenticationPreemptive(true);

      Credentials defaultcreds =
        new UsernamePasswordCredentials(service.getUserName(), service.getPassword());

      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }

  /**
   * Uploads a file to fedora.
   *
   * @param file the file to upload
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(File file) throws IOException {
    return upload(new FilePart("file", file));
  }

  /**
   * Uploads a byte array to fedora.
   *
   * @param bytes the bytes to upload.
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(byte[] bytes) throws IOException {
    return upload(new FilePart("file", new ByteArrayPartSource("byte-array", bytes)));
  }

  /**
   * Uploads the contents of a fixed length input stream to fedora.
   *
   * @param in the input stream to upload.
   * @param length the length of the input stream.
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(final InputStream in, final long length)
                throws IOException {
    final int markLength = 20000; // Auth errors will probably be reported before this

    return upload(new FilePart("file",
                               new PartSource() {
        private boolean marked = false;

        public InputStream createInputStream() throws IOException {
          if (marked)
            in.reset();

          in.mark(markLength);
          marked = true;

          return in;
        }

        public String getFileName() {
          return "fixed-length-input-stream";
        }

        public long getLength() {
          return length;
        }
      }) {
        public boolean isRepeatable() {
          return in.markSupported() && super.isRepeatable();
        }
      });
  }

  /**
   * Uploads the contents of an input stream. Copies to a local file before upload. Use only if any
   * of the other upload methods cannot be used.
   *
   * @param in the input stream to upload
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(InputStream in) throws IOException {
    FileOutputStream out      = null;
    File             tempFile = null;

    try {
      byte[] buf = new byte[4096];
      int    len;

      tempFile   = File.createTempFile("fedora-upload-", null);
      out        = new FileOutputStream(tempFile);

      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }

      out.close();
      out = null;

      return upload(tempFile);
    } finally {
      try {
        if (in != null)
          in.close();
      } catch (Throwable t) {
      }

      try {
        if (out != null)
          out.close();
      } catch (Throwable t) {
      }

      try {
        tempFile.delete();
      } catch (Throwable t) {
      }
    }
  }

  private String upload(Part part) throws IOException {
    MultipartPostMethod post = new MultipartPostMethod(service.getServiceUri());
    post.addPart(part);

    try {
      int resultCode = client.executeMethod(post);

      // renew credentials if auth failure
      if ((resultCode == 401) && part.isRepeatable() && service.renew()) {
        if (service.requiresUserNamePassword()) {
          Credentials defaultcreds =
            new UsernamePasswordCredentials(service.getUserName(), service.getPassword());

          client.getState().setCredentials(AuthScope.ANY, defaultcreds);
        }

        post.releaseConnection();
        post = new MultipartPostMethod(service.getServiceUri());
        post.addPart(part);
        resultCode = client.executeMethod(post);
      }

      if (resultCode != 201)
        throw new IOException(HttpStatus.getStatusText(resultCode) + ":"
                              + replaceNewlines(post.getResponseBodyAsString(), " "));

      return replaceNewlines(post.getResponseBodyAsString(), "");
    } finally {
      post.releaseConnection();
    }
  }

  private static String replaceNewlines(String in, String replaceWith) {
    return in.replaceAll("\r", replaceWith).replaceAll("\n", replaceWith);
  }
}
