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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstraction to represent blobs.
 *
 * @author Pradeep Krishnan
 *
 * @see BlobStore#getBlob
 */
public interface Blob {
  /**
   * Represents the modifications to the blob object.
   *
   * @author Pradeep Krishnan
   *
   * @see Blob#create()
   * @see Blob#delete()
   * @see AbstractBlob#writing(OutputStream)
   */
  public static enum ChangeState {NONE, CREATED, WRITTEN, DELETED}

  /**
   * Returns the normalized id of this blob.
   *
   * @return the normalized id.
   *
   * @see java.net.URI#normalize
   */
  public String getId();

  /**
   * Gets the change state of this blob.
   *
   * @return the change state
   */
  public ChangeState getChangeState();

  /**
   * Gets the change state of this blob since the previous call.
   * This is used mainly for the {@link Session#flush()} to see
   * what changed since the last flush. This avoids the work
   * for search indexing etc. when things haven't really changed.
   *
   * @return the change state
   */
  public ChangeState mark();

  /**
   * Gets a stream suitable for reading from this blob within the current transaction. A new stream
   * is returned each time this method is called, and the stream is positioned at the beginning of
   * the data.
   *
   * @return the newly created InputStream
   *
   * @throws OtmException when an InputStream cannot be returned and is quite likely to occur when
   *           the test {@link #exists} fails.
   */
  InputStream getInputStream() throws OtmException;

  /**
   * Gets a stream suitable for writing to this blob. Note that a new stream is returned each time
   * this method is called, and the stream is positioned at the beginning of the blob.
   *
   * @return the newly created OutputStream
   *
   * @throws OtmException when an OutputStream cannot be returned
   */
  OutputStream getOutputStream() throws OtmException;

  /**
   * A utility method to read the contents of this blob. It is equivalent to calling
   * {@link #getInputStream} and reading the stream fully.
   *
   * @return the blob contents
   *
   * @throws OtmException on an error
   */
  byte[] readAll() throws OtmException;

  /**
   * A utility method to write/replace the contents of this blob. It is equivalent to calling
   * {@link #getOutputStream} and writing the contents fully.
   *
   * @param b the blob contents
   *
   * @throws OtmException on an error
   */
  void writeAll(byte[] b) throws OtmException;

  /**
   * A test to see if this blob exists.
   *
   * @return <code>true</code> if and only if the blob denoted by this id exists;
   *         <code>false</code> otherwise
   *
   * @throws OtmException on an error
   */
  public boolean exists() throws OtmException;

  /**
   * Creates a new, empty blob named by this id if and only if a blob with this id does not yet
   * exist.
   *
   * @return <code>true</code> if the named blob does not exist and was successfully created;
   *         <code>false</code> if the named blob already exists
   *
   * @throws OtmException If an I/O error occurred
   *
   */
  public boolean create() throws OtmException;

  /**
   * Deletes the blob denoted by this id.
   *
   * @return <code>true</code> if and only if the blob is successfully deleted; <code>false</code>
   *         otherwise
   *
   * @throws OtmException If an I/O error occurred
   */
  public boolean delete() throws OtmException;
}
