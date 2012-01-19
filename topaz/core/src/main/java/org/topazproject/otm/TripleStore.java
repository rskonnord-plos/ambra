/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.util.Collection;
import java.util.List;

import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.query.GenericQueryImpl;
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
   * Gets an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param instance the instance to be refreshed or null 
   * @param con the connection to use
   * @param filters the filters to use, or null if there are none
   * @param filterObj whether the object itself needs filtering (if false only the fields are
   *                  filtered)
   *
   * @return the result instance
   *
   * @throws OtmException on an error
   */
  public Object get(ClassMetadata cm, String id, Object  instance, Connection con,
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
  public Results doQuery(GenericQueryImpl query, Collection<Filter> filters, Connection con)
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
   * Creates a new model/graph in the persistence store.
   *
   * @param conf the configuration
   *
   * @throws OtmException on an error
   */
  public void createModel(ModelConfig conf) throws OtmException;

  /**
   * Drops a model/graph in the persistence store, deleting all triples.
   *
   * @param conf the configuration
   *
   * @throws OtmException on an error
   */
  public void dropModel(ModelConfig conf) throws OtmException;

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
}
