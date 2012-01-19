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

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topazproject.otm.mapping.Mapper;

/**
 * An abstraction to represent triple stores.
 *
 * @author Pradeep Krishnan
  */
public interface TripleStore {
  /**
   * Opens a connection to the store.
   *
   * @return the connection
   *
   * @throws OtmException on an error
   */
  public Connection openConnection() throws OtmException;

  /**
   * Closes a previously opened connection.
   *
   * @param con the connection
   *
   * @throws OtmException error
   */
  public void closeConnection(Connection con) throws OtmException;

  /**
   * Persists an object in the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param o the object
   * @param txn the transaction context
   *
   * @throws OtmException on an error
   */
  public void insert(ClassMetadata cm, String id, Object o, Transaction txn)
              throws OtmException;

  /**
   * Removes an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param txn the transaction context
   *
   * @throws OtmException on an error
   */
  public void delete(ClassMetadata cm, String id, Transaction txn)
              throws OtmException;

  /**
   * Gets an object from the triple store.
   *
   * @param cm the class metadata for the object
   * @param id the id/subject-uri for the object
   * @param txn the transaction context
   *
   * @return the result that may contain a partially created object
   *
   * @throws OtmException on an error
   */
  public ResultObject get(ClassMetadata cm, String id, Transaction txn)
                   throws OtmException;

  /**
   * Lists objects matching the given Criteria.
   *
   * @param criteria the criteria
   * @param txn the transaction context
   *
   * @return list of results containing partially created objects
   *
   * @throws OtmException on an error
   */
  public List<ResultObject> list(Criteria criteria, Transaction txn)
                          throws OtmException;

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

  public static class ResultObject {
    public Object                          o;
    public String                          id;
    public Map<Mapper, List<String>>       unresolvedAssocs = new HashMap<Mapper, List<String>>();
    public Map<Mapper, List<ResultObject>> resolvedAssocs   =
      new HashMap<Mapper, List<ResultObject>>();

    public ResultObject(Object o, String id) {
      this.o    = o;
      this.id   = id;
    }
  }
}
