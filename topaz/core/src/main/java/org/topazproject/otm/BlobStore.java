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
   * Persists an object in the blob store.
   *
   * @param cm the class metadata for the object
   * @param id the id for the blob
   * @param blob the blob to store
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public void insert(ClassMetadata cm, String id, Object blob, Connection con) throws OtmException;

  /**
   * Removes an object from the blob store.
   *
   * @param cm the class metadata for the object
   * @param id the id for the blob
   * @param blob the blob to delete
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public void delete(ClassMetadata cm, String id, Object blob, Connection con) throws OtmException;

  /**
   * Signals that the blob store should flush any buffered operations to the underlying store.
   * This is useful for implementations that want to collect inserts and deletes and send them
   * in one go for efficiency.
   *
   * @param con the connection to use
   * @throws OtmException on an error
   */
  public void flush(Connection con) throws OtmException;

  /**
   * Gets a blob from the blob store.
   *
   * @param cm the class metadata for the object
   * @param id the id for the blob
   * @param blob the blob to refresh or null
   * @param con the connection to use
   *
   * @return the blob or null if it does not exist
   *
   * @throws OtmException on an error
   */
  public Object get(ClassMetadata cm, String id, Object blob, Connection con) throws OtmException;

}
