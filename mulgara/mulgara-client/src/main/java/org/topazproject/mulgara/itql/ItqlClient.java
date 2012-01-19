/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.itql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAResource;

/** 
 * The interface for a minimal mulgara client. This is basically a subset of ItqlInterpreterBean.
 * Use {@link ItqlClientFactory ItqlClientFactory} to create instances.
 *
 * @author Ronald Tschalär
 */
public interface ItqlClient {
  /** 
   * Run one or more iTQL queries. 
   * 
   * @param itql the iTQL queries to run
   * @return the answer
   * @throws IOException if an exception was encountered while processing the queries
   */
  public List<Answer> doQuery(String itql) throws IOException, AnswerException;

  /** 
   * Run one or more iTQL update commands (or any commands that do not produce output).
   * 
   * @param itql the iTQL statement to execute
   * @throws IOException if an exception was encountered while processing the queries
   */
  public void doUpdate(String itql) throws IOException;

  /** 
   * Begin a transaction. If not invoked, iTQL commands will run in auto-commit mode.
   * One of {@link #commitTxn commitTxn()} or {@link #rollbackTxn rollbackTxn()} must be
   * invoked to end the transaction.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws IOException if an exception occurred starting the transaction
   */
  public void beginTxn(String txnName) throws IOException;

  /** 
   * Commit a transaction. May only be invoked after a {@link #beginTxn beginTxn()}.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws IOException if an exception occurred committing the transaction
   */
  public void commitTxn(String txnName) throws IOException;

  /** 
   * Roll back a transaction. May only be invoked after a {@link #beginTxn beginTxn()}.
   * 
   * @param txnName  a name to associate with the transaction; used for logging only
   * @throws IOException if an exception occurred rolling back the transaction
   */
  public void rollbackTxn(String txnName) throws IOException;

  /** 
   * Get the xa-resource for this connection. 
   * 
   * @return the xa-resource
   * @throws IOException if an exception occurred getting the xa-resource
   */
  public XAResource getXAResource() throws IOException;

  /** 
   * Get the read-only xa-resource for this connection. 
   * 
   * @return the xa-resource
   * @throws IOException if an exception occurred getting the xa-resource
   */
  public XAResource getReadOnlyXAResource() throws IOException;

  /** 
   * Set the aliases to use. 
   * 
   * @param aliases the aliases to use
   */
  public void setAliases(Map<String, String> aliases);

  /** 
   * Get the current list of aliases. 
   * 
   * @return the aliases
   */
  public Map<String, String> getAliases();

  /**
   * Returns the last error from a mulgara operation.
   * 
   * @return the error or null
   */
  public Exception getLastError();

  /**
   * Clear the error status from a mulgara operation.
   */
  public void clearLastError();

  /** 
   * Close the session. 
   */
  public void close();
}
