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

/**
 * An abstraction to represent blob stores.
 *
 * @author Pradeep Krishnan
 */
public interface BlobStore extends Store {
  /**
   * Signals that the blob store should flush any buffered operations to the underlying store. This
   * is useful for implementations that want to collect inserts and deletes and send them in one go
   * for efficiency.
   *
   * @param con the connection to use
   * @throws OtmException on an error
   */
  public void flush(Connection con) throws OtmException;

  /**
   * Gets a Blob object with the given id. This will always return a Blob, regardless of whether
   * it exists or not.
   *
   * @param cm  the metadata of the containing entity
   * @param id the id of the containing entity (same as blob)
   * @param inst the instane of the containing entity or null
   * @param con the connection to use
   * @return a Blob object
   *
   * @throws OtmException on an error
   */
  public Blob getBlob(ClassMetadata cm, String id, Object inst, Connection con) throws OtmException;
}
