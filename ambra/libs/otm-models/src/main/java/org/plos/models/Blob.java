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
package org.plos.models;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.topazproject.otm.annotations.Entity;

/**
 * Base class for blobs loaded from the BlobStore.
 *
 * @author Pradeep Krishnan
 */
@Entity()
public abstract class Blob implements Serializable {
  @org.topazproject.otm.annotations.Blob
  private byte[] body;

  /**
   * Creates a new Blob object.
   */
  public Blob() {
  }

  /**
   * Creates a new Blob object.
   *
   * @param contentType the content type
   * @param body the blob
   */
  public Blob(String contentType, byte[] body) {
    setBody(body);
  }

  /**
   * Get body.
   *
   * @return body as byte[].
   */
  public byte[] getBody() {
    return body;
  }

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  public void setBody(byte[] body) {
    this.body = body;
  }

  /**
   * Get body as a string.
   *
   * @return body as a string
   *
   * @throws UnsupportedEncodingException DOCUMENT ME!
   */
  public String getText() throws UnsupportedEncodingException {
    return new String(getBody(), "UTF-8");
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public abstract String getId();

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public abstract void setId(String id);
}
