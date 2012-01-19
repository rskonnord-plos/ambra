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

import java.util.Collection;

import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * An abstraction to represent text-search indexes stores. This is currently only a partial,
 * incomplete abstraction (in particular it is missing query support) and should be considered
 * internal, experimental, and subject to change.
 *
 * @author Ronald Tschalär
 */
public interface SearchStore extends Store {
  /**
   * Indexes parts of an object in the text-search store.
   *
   * @param cm     the class metadata for the object
   * @param fields the fields that are to be selectively indexed
   * @param id     the id/subject-uri for the object
   * @param o      the object
   * @param con    the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void index(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                        Connection con) throws OtmException;

  /**
   * Removes parts of an object from the text-search indexes store.
   *
   * @param cm     the class metadata for the object
   * @param fields the fields that are to be selectively deleted
   * @param id     the id/subject-uri for the object
   * @param o      the object
   * @param con    the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void remove(ClassMetadata cm, Collection<SearchableDefinition> fields, String id, T o,
                         Connection con) throws OtmException;

  /**
   * Removes the single blob field of the object from the text-search indexes store.
   *
   * @param cm     the class metadata for the object
   * @param field  the field that is to be selectively deleted
   * @param id     the id/subject-uri for the object
   * @param o      the object
   * @param con    the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void remove(ClassMetadata cm, SearchableDefinition field, String id, T o,
                         Connection con) throws OtmException;

  /**
   * Signals that the search store should flush any buffered operations to the underlying store.
   * This is useful for implementations that want to collect inserts and deletes and send them
   * in one go for efficiency.
   *
   * @param con the connection to use
   * @throws OtmException on an error
   */
  public void flush(Connection con) throws OtmException;
}
