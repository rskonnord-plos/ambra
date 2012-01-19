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
package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fedora uploader client.
 *
 * @author Pradeep Krishnan
 */
public class Uploader {
  private static final MultiThreadedHttpConnectionManager connectionManager =
    new MultiThreadedHttpConnectionManager();
  private static final Log             log     = LogFactory.getLog(Uploader.class);

  private HttpClient       client;
  private String           serviceUri;

  static {
    // XXX: tune this
    connectionManager.getParams().setDefaultMaxConnectionsPerHost(100);
    connectionManager.getParams().setMaxTotalConnections(100);
  }

  private static final byte[] FILE_NAME_BYTES = EncodingUtil.getAsciiBytes("; filename=");

  /**
   * Creates a new Uploader object.
   *
   * @param serviceUri The uploader service uri
   * @param uname   The uploader user name or null
   * @param passwd  The uploader service passwd
   */
  public Uploader(String serviceUri, String uname, String passwd) {
    this.serviceUri   = serviceUri;
    client            = new HttpClient(connectionManager);

    if (uname != null) {
      client.getParams().setAuthenticationPreemptive(true);

      Credentials defaultcreds =
        new UsernamePasswordCredentials(uname, passwd);

      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }

  /**
   * Download from an uploaded URI.
   *
   * @param uploaded the URI that was returned by an upload
   * @return the inputstream to read
   * @throws IOException on an error
   */
  public InputStream download(String uploaded) throws IOException {
     GetMethod get = new GetMethod(serviceUri + "?" + uploaded);

     int resultCode = client.executeMethod(get);

     if (resultCode != 200)
       throw new IOException(HttpStatus.getStatusText(resultCode) + ":"
                             + replaceNewlines(get.getResponseBodyAsString(), " "));

     return get.getResponseBodyAsStream();
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
    InputStream in = new FileInputStream(file);
    try {
      return upload(in, file.length());
    } finally {
      try {
        in.close();
      } catch (Throwable t) {
        log.warn("Failed to close input stream for " + file, t);
      }
    }
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
    return upload(new ByteArrayInputStream(bytes), bytes.length);
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
  public String upload(InputStream in, long length)
                throws IOException {
    UploadResult result = new UploadResult();
    OutputStream out = getOutputStream(result.getListener(), length);

    try {
      byte[] buf = new byte[4096];
      int    len;

      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      try {
        if (out != null)
          out.close();
      } catch (Throwable t) {
        log.warn("Failed to close output stream", t);
      }
    }

    return result.getResult();
  }

  /**
   * Uploads the contents of an input stream of unknown length.
   *
   * @param in the input stream to upload
   *
   * @return Returns a uri that can be used in setting up a data-stream for a fedora object.
   *
   * @throws IOException on an error
   */
  public String upload(InputStream in) throws IOException {
    return upload(in, -1);
  }

  /**
   * Gets an output stream to allow writes directly to Fedora.
   *
   * @param listener the completion listener (called by the uploader-thread)
   * @param length the optional length of the stream. -1 if not known. Note that
   *        the length is ignored currently and instead a chunked transfer encoding is
   *        what is used now. This is because the commons-http-client library used
   *        is horribly inefficient when it comes to Content-Length header computation
   *        for MultiPart messages. There is no reason why the Fedora upload-servlet
   *        should be using MultiPart - but it does and so we use chunked transfer
   *        encoding always.
   *
   * @return the output stream to use
   * @throws IOException on an error
   */
  public OutputStream getOutputStream(final UploadListener listener, long length) throws IOException {
    final CountingSynchronousQueue<Object> exchange = new CountingSynchronousQueue<Object>();
    final Part part = new Part() {

      @Override
      public String getCharSet() {
        return FilePart.DEFAULT_CHARSET;
      }

      @Override
      public String getContentType() {
        return FilePart.DEFAULT_CONTENT_TYPE;
      }

      @Override
      public String getName() {
        return "file";
      }

      @Override
      public String getTransferEncoding() {
        return FilePart.DEFAULT_TRANSFER_ENCODING;
      }

      @Override
      protected long lengthOfData() throws IOException {
        return -1;
      }

      @Override
      protected void sendData(OutputStream out) throws IOException {
        try {
          // Pass the stream to the app to write
          exchange.put(out);
          // Wait for the app to finish writing
          exchange.take();
        } catch (InterruptedException e) {
          throw (IOException) new IOException("Interrupted while waiting for write to complete")
                                             .initCause(e);
        }
      }

      @Override
      protected void sendDispositionHeader(OutputStream out)
      throws IOException {
          super.sendDispositionHeader(out);
          String filename = "dummy";
          if (filename != null) {
              out.write(FILE_NAME_BYTES);
              out.write(QUOTE_BYTES);
              out.write(EncodingUtil.getAsciiBytes(filename));
              out.write(QUOTE_BYTES);
          }
      }

    };

    // Launch the upload(part) from another thread
    Thread t = new Thread("Upload-To-Fedora") {
      @Override
      public void run() {
        try {
          doRun();
        } catch (RuntimeException e) {
          log.error("Uncaught run-time exception", e);
        } catch (Error e) {
          log.error("Uncaught error", e);
        }
      }
      private void doRun() {
        String ref = null;
        try {
          // catch all exceptions and report to the calling thread
          ref = upload(part);
        } catch (Throwable e) {
          if (log.isDebugEnabled())
            log.debug("Error in upload: ", e);

          if (exchange.getPutCount() != 0)
            listener.uploadComplete(e);
          else {
            try {
              exchange.put(e);
            } catch (InterruptedException e1) {
              log.error("Interrupted while notifying upload-error.", e1);
            }
          }
        }
        if (ref != null)
          listener.uploadComplete(ref);
      }
    };

    t.setDaemon(true);
    t.start();

    // Wait for the out-put stream to be available
    Object o;
    try {
      o = exchange.take();
    } catch (InterruptedException e) {
      throw (IOException) new IOException("Interrupted while waiting for output stream")
      .initCause(e);
    }

    if (o instanceof RuntimeException)
      throw new RuntimeException("Error reported by Uploader-thread", (RuntimeException)o);

    if (o instanceof IOException)
      throw (IOException) new IOException("Error reported by Uploader-thread")
                                .initCause((Throwable)o);

    if (o instanceof Throwable)
      throw new Error("Error reported by Uploader-thread", (Throwable)o);

    if (!(o instanceof OutputStream))
      throw new Error("unexpected type: " + o.getClass());

    // Return a filtered stream that tracks close() to signal a write-complete to the upload-thread
    return new FilterOutputStream((OutputStream)o) {

      @Override
      public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
      }

      @Override
      public void close() throws IOException {
        try {
          exchange.put(out);
        } catch (InterruptedException e) {
          throw (IOException) new IOException("Interrupted while signalling write complete")
          .initCause(e);
        }
      }
    };
  }

  private String upload(Part part) throws IOException {
    PostMethod post = new PostMethod(serviceUri);
    post.setRequestEntity(new MultipartRequestEntity(new Part[]{part},post.getParams()));

    try {
      int resultCode = client.executeMethod(post);

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

  /**
   * A listener for upload status.
   *
   * @author Pradeep Krishnan
   *
   */
  public static interface UploadListener {
    /**
     * Notification of a successful upload with the reference uri.
     *
     * @param reference upload reference uri
     */
    public void uploadComplete(String reference);
    /**
     * Notification of a failed upload with the error.
     *
     * @param err the error that was reported
     */
    public void uploadComplete(Throwable err);

  }

  /**
   * A convenient result class that can listen for upload complete events
   * and get the result of the upload.
   *
   * @author Pradeep Krishnan
   */
  public static class UploadResult {
    private final SynchronousQueue<Object> exchange = new SynchronousQueue<Object>();

    /**
     * Get the result of the uploader. Blocks until the upload is complete and
     * a result is available.
     *
     * @return the result of upload
     * @throws IOException on an error
     */
    public String getResult() throws IOException {
      Object o;
      try {
        o = exchange.take();
      } catch (InterruptedException e) {
        throw (IOException) new IOException("Interrupted while waiting for write to complete")
        .initCause(e);
      }

      return parseResult(o);
    }

    public static String parseResult(Object o) throws IOException {
      if (o instanceof RuntimeException)
        throw new RuntimeException("Error reported by Uploader-thread", (RuntimeException)o);

      if (o instanceof IOException)
        throw (IOException) new IOException("Error reported by Uploader-thread")
                                .initCause((Throwable)o);

      if (o instanceof Throwable)
        throw new Error("Error reported by Uploader-thread", (Throwable)o);

      if (!(o instanceof String))
        throw new Error("unexpected type: " + o.getClass());

      return (String) o;
    }

    /**
     * The listener to use for {@link Uploader#getOutputStream}.
     *
     * @return the listener
     */
    public UploadListener getListener() {
      return new UploadListener() {
        public void uploadComplete(String reference) {
          try {
            exchange.put(reference);
          } catch (InterruptedException e) {
            throw new Error("Interrupted during notify", e);
          }
        }

        public void uploadComplete(Throwable err) {
          try {
            exchange.put(err);
          } catch (InterruptedException e) {
            throw new Error("Interrupted during notify", err);
          }
        }
      };
    }
  }

  @SuppressWarnings("serial")
  private static class CountingSynchronousQueue<T> extends SynchronousQueue<T> {
    private int putCount = 0;

    public void put(T o) throws InterruptedException {
      if (o != null)
        putCount++;
      super.put(o);
    }

    public int getPutCount() {
      return putCount;
    }
  }
}
