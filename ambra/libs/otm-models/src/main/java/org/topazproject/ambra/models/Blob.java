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
package org.topazproject.ambra.models;

import java.io.Serializable;

import org.topazproject.otm.annotations.Entity;

/**
 * Base class for blobs loaded from the BlobStore.
 *
 * @author Pradeep Krishnan
 */
@Entity()
public abstract class Blob implements Serializable {
  private static final long serialVersionUID = -783693796375733488L;

  private org.topazproject.otm.Blob body;

  /**
   * Creates a new Blob object.
   */
  public Blob() {
  }

  /**
   * Creates a new Blob object.
   *
   * @param contentType the content type
   */
  public Blob(String contentType) {
    // FIXME: contentType
  }

  /**
   * Get the body blob. Available only when attached to OTM.
   *
   * @return body as a Blob
   */
  public org.topazproject.otm.Blob getBody() {
    return body;
  }

  /**
   * Set the body. Used by OTM.
   *
   * @param body the value to set.
   */
  @org.topazproject.otm.annotations.Blob
  public void setBody(org.topazproject.otm.Blob body) {
    this.body = body;
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
