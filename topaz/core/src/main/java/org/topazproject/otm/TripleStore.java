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
import java.util.List;
import java.util.Map;

import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.query.GenericQuery;
import org.topazproject.otm.query.Results;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
 */
public interface TripleStore extends Store {
  /**
   * Persists an object in the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param o the object
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void insert(ClassMetadata cm, String id, T o, Connection con) throws OtmException;

  /**
   * Persists parts of an object in the triple store.
   *
   * @param cm the class metadata for the object
   * @param fields the fields that are to be selectively persisted
   * @param id the id/subject-uri for the object
   * @param o the object
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void insert(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException;

  /**
   * Removes an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param o the object
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void delete(ClassMetadata cm, String id, T o, Connection con) throws OtmException;

  /**
   * Removes parts of an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param fields the fields that are to be selectively deleted
   * @param id the id/subject-uri for the object
   * @param o the object
   * @param con the connection to use
   *
   * @throws OtmException on an error
   */
  public <T> void delete(ClassMetadata cm, Collection<RdfMapper> fields, String id, T o,
                         Connection con) throws OtmException;

  /**
   * Signals that the triple store should flush any buffered operations to the underlying store.
   * This is useful for implementations that want to collect inserts and deletes and send them
   * in one go for efficiency.
   *
   * @param con the connection to use
   * @throws OtmException on an error
   */
  public void flush(Connection con) throws OtmException;

  /**
   * Gets an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param con the connection to use
   * @param filters the filters to use, or null if there are none
   * @param filterObj whether the object itself needs filtering (if false only the fields are
   *                  filtered)
   *
   * @return the result
   *
   * @throws OtmException on an error
   */
  public Result get(ClassMetadata cm, String id, Connection con,
                   List<Filter> filters, boolean filterObj) throws OtmException;

  /**
   * Lists objects matching the given Criteria.
   *
   * @param criteria the criteria
   * @param con the connection to use
   *
   * @return list of result objects
   *
   * @throws OtmException on an error
   */
  public List list(Criteria criteria, Connection con)
                          throws OtmException;

  /**
   * Execute an OQL query.
   *
   * @param query   the preparsed query
   * @param filters the list of filters to apply
   * @param con the connection to use
   * @return the query results
   * @throws OtmException on an error
   */
  public Results doQuery(GenericQuery query, Collection<Filter> filters, Connection con)
      throws OtmException;

  /**
   * Execute a native query.
   *
   * @param query the native query string
   * @param con the connection to use
   * @return the query results
   * @throws OtmException on an error
   */
  public Results doNativeQuery(String query, Connection con) throws OtmException;

  /**
   * Execute a native update.
   *
   * @param command the native command(s) to execute
   * @param con the connection to use
   * @throws OtmException on an error
   */
  public void doNativeUpdate(String command, Connection con) throws OtmException;

  /**
   * Creates a new graph in the persistence store.
   *
   * @param conf the configuration
   * @param con a connection to the triplestore to send the request over
   *
   * @throws OtmException on an error
   */
  public void createGraph(GraphConfig conf, Connection con) throws OtmException;

  /**
   * Drops a graph in the persistence store, deleting all triples.
   *
   * @param conf the configuration
   * @param con a connection to the triplestore to send the request over
   *
   * @throws OtmException on an error
   */
  public void dropGraph(GraphConfig conf, Connection con) throws OtmException;

  /**
   * Gets a store specific criterion builder.
   *
   * @param func the function name
   *
   * @return the criterion builder
   *
   * @throws OtmException on an error
   */
  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException;

  /**
   * Sets a store specific criterion builder.
   *
   * @param func the function name
   * @param builder the builder
   *
   * @throws OtmException on an error
   */
  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException;

  /**
   * Gets an rdf:list collection property values.
   *
   * @param id the subject id
   * @param pUri the predicate uri that points to the rdf:list node
   * @param mUri the graph uri
   * @param con the connection
   *
   * @return the collection values in order
   *
   * @throws OtmException on an error
   */
  public List<String> getRdfList(String id, String pUri, String mUri, Connection isc)
                          throws OtmException;

  /**
   * Gets an rdf:bag container property values.
   *
   * @param id the subject id
   * @param pUri the predicate uri that points to the rdf:list node
   * @param mUri the graph uri
   * @param con the connection
   *
   * @return the container values in order
   *
   * @throws OtmException on an error
   */
  public List<String> getRdfBag(String id, String pUri, String mUri, Connection isc)
                          throws OtmException;

  public interface Result {
    public Map<String, List<String>> getFValues();

    public Map<String, List<String>> getRValues();

  }
}
