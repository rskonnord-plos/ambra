/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A convenient base class for Blob that provides tracked streams. Streams are tracked to detect
 * writes. An implementation must provide its own {@link #writing(OutputStream)} method to listen
 * for updates.
 * <p>
 * For tracking to work, this class implements both the {@link #getInputStream()} and
 * {@link #getOutputStream()} methods and delegates to {{@link #doGetInputStream()} and
 * {@link #doGetOutputStream()} for the actual stream creation.
 * <p>
 * This class also implements {@link #readAll()} and {@link #writeAll(byte[])} methods and
 * also has a {@link #close()} method to close all open streams.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractBlob implements Blob {
  protected final Log             log     = LogFactory.getLog(getClass());
  protected boolean            closed     = false;
  protected Collection<Closeable> streams = new ArrayList<Closeable>();
  private final String id;

  /**
   * Creates an AbstratcBlob instance.
   *
   * @param id the normalized id of this blob
   */
  protected AbstractBlob(String id) {
    this.id = id;
  }

  /**
   * Gets the 'real' InputStream.
   *
   * @return the newly created OutputStream
   *
   * @throws OtmException on an error
   */
  protected abstract InputStream doGetInputStream() throws OtmException;

  /**
   * Gets the 'real' OutputStream.
   *
   * @return the newly created OutputStream
   *
   * @throws OtmException on an error
   */
  protected abstract OutputStream doGetOutputStream() throws OtmException;

  /**
   * Notification that the application is writing to the stream.
   *
   * @param out the stream that is being written to
   */
  protected abstract void writing(OutputStream out);

  /**
   * Notification that the application has closed a stream.
   *
   * @param stream the stream that is closed
   */
  protected void closed(Closeable stream) {
    streams.remove(stream);
  }

  /**
   * Copies an input stream to an output stream.
   *
   * @param in the input stream
   * @param out the output stream
   *
   * @throws IOException on an error
   */
  protected void copy(InputStream in, OutputStream out) throws IOException {
    byte[] b = new byte[4096];
    for (int len; (len = in.read(b)) != -1; )
      out.write(b, 0, len);
  }
  /**
   * Closes all the streams 'silently'. Catches and logs exceptions instead of throwing.
   *
   * @param streams array of streams
   */
  protected void closeAll(Closeable ... streams) {
    for (Closeable c : streams) {
      try {
        if (c != null)
          c.close();
      } catch (IOException e) {
        log.warn("Failed to close stream for " + getId(), e);
      }
    }
  }

  public String getId() {
    return id;
  }

  public final InputStream getInputStream() throws OtmException {
    if (closed)
      throw new OtmException("Blob is closed");
    InputStream in = doGetInputStream();
    streams.add(in);
    return new TrackedInputStream(in);
  }

  public final OutputStream getOutputStream() throws OtmException {
    if (closed)
      throw new OtmException("Blob is closed");
    OutputStream out = doGetOutputStream();
    streams.add(out);
    return new TrackedOutputStream(out);
  }

  public byte[] readAll() throws OtmException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    try {
      copy(getInputStream(), out);
    } catch (IOException e) {
      throw new OtmException("Read failed on " + getId(), e);
    }
    return out.toByteArray();
  }

  public void writeAll(byte[] b) throws OtmException {
    OutputStream out = getOutputStream();
    try {
      out.write(b);
      out.close();
    } catch (IOException e) {
      throw new OtmException("Write failed on " + getId(), e);
    }
  }

  public void close() throws OtmException {
    closeAll(streams.toArray(new Closeable[streams.size()]));
    streams.clear();
    closed = true;
  }

  private class TrackedInputStream extends FilterInputStream {
    public TrackedInputStream(InputStream in) {
      super(in);
    }

    @Override
    public void close() throws IOException {
      super.close();
      closed(super.in);
    }
  }

  private class TrackedOutputStream extends FilterOutputStream {
    public TrackedOutputStream(OutputStream out) {
      super(out);
    }

    public void close() throws IOException {
      super.close();
      closed(super.out);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      writing(super.out);
      super.out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
      writing(super.out);
      super.out.write(b);
    }
  }
}
