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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mulgara.connection.Connection;
import org.mulgara.itql.TqlInterpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.operation.Command;

/** 
 * A mulgara client using TqlInterpreter directly.
 *
 * @author Ronald Tschalär
 * @see org.mulgara.itql.TqlInterpreter
 */
abstract class TIClient implements ItqlClient {
  private static final Log  log = LogFactory.getLog(TIClient.class);

  /** the underlying connection instance to use */
  protected final Connection con;

  /** the underlying tql-interpreter instance to use */
  protected final TqlInterpreter ti;

  private Exception lastErr = null;

  /**
   * Create a new instance using the given connection.
   *
   * @param con the connection to use
   */
  protected TIClient(Connection con) {
    if (con == null)
      throw new NullPointerException("connection may not be null");

    this.con = con;
    this.ti  = new TqlInterpreter();
  }

  public List<org.topazproject.mulgara.itql.Answer> doQuery(String itql)
      throws IOException, AnswerException {
    try {
      if (log.isDebugEnabled())
        log.debug("sending query '" + itql + "'");

      List<org.topazproject.mulgara.itql.Answer> res =
          new ArrayList<org.topazproject.mulgara.itql.Answer>();

      for (Command c : ti.parseCommands(itql)) {
        Object o = c.execute(con);

        if (o instanceof Answer)
          res.add(new AnswerAnswer((Answer) o));
        else if (o instanceof String)
          res.add(new AnswerAnswer((String) o));
        else
          throw new Error("Unexpected object received in result to query '" + itql + "': class=" +
                          o.getClass().getName());
      }

      return res;
    } catch (AnswerException ae) {
      lastErr = ae;
      throw ae;
    } catch (Exception e) {
      lastErr = e;
      throw (IOException) new IOException("Error running query '" + itql + "'").initCause(e);
    }
  }

  public void doUpdate(String itql) throws IOException {
    try {
      if (log.isDebugEnabled())
        log.debug("sending update '" + itql + "'");

      for (Command c : ti.parseCommands(itql))
        c.execute(con);
    } catch (Exception e) {
      lastErr = e;
      throw (IOException) new IOException("Error running update '" + itql + "'").initCause(e);
    }
  }

  public void beginTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending beginTransaction '" + txnName + "'");

    try {
      con.setAutoCommit(false);
    } catch (QueryException qe) {
      lastErr = qe;
      throw (IOException) new IOException("Error beginning tx '" + txnName + "'").initCause(qe);
    }
  }

  public void commitTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending commit '" + txnName + "'");

    try {
      con.setAutoCommit(true);
    } catch (QueryException qe) {
      lastErr = qe;
      throw (IOException) new IOException("Error committing tx '" + txnName + "'").initCause(qe);
    }
  }

  public void rollbackTxn(String txnName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("sending rollback '" + txnName + "'");

    boolean rbOk = false;
    try {
      con.getSession().rollback();
      rbOk = true;
    } catch (QueryException qe) {
      lastErr = qe;
      throw (IOException) new IOException("Error rolling back tx '" + txnName + "'").initCause(qe);
    } finally {
      try {
        con.setAutoCommit(true);
      } catch (Exception e) {
        if (rbOk)
          throw (IOException) new IOException("Error setting auto-commit after rolling back tx '" +
                                              txnName + "'").initCause(e);
        else
          log.error("Error setting auto-commit after rolling back tx '" + txnName + "'", e);
      }
    }
  }

  public XAResource getXAResource() throws IOException {
    try {
      return con.getSession().getXAResource();
    } catch (QueryException qe) {
      lastErr = qe;
      throw (IOException) new IOException("Error getting xa-resource").initCause(qe);
    }
  }

  public XAResource getReadOnlyXAResource() throws IOException {
    try {
      return con.getSession().getReadOnlyXAResource();
    } catch (QueryException qe) {
      lastErr = qe;
      throw (IOException) new IOException("Error getting read-only xa-resource").initCause(qe);
    }
  }

  public void setAliases(Map<String, String> aliases) {
    HashMap a = new HashMap();
    for (Map.Entry<String, String> e : aliases.entrySet())
      a.put(e.getKey(), URI.create(e.getValue()));
    ti.setAliasMap(a);
  }

  public Map<String, String> getAliases() {
    Map<String, String> a = new HashMap<String, String>();
    for (Map.Entry<String, URI> e : ((Map<String, URI>) ti.getAliasMap()).entrySet())
      a.put(e.getKey(), e.getValue().toString());
    return a;
  }

  public Exception getLastError() {
    return lastErr;
  }

  public void clearLastError() {
    lastErr = null;
  }

  public void close() {
    if (!con.getAutoCommit()) {
      try {
        rollbackTxn("");
      } catch (Exception e) {
        log.warn("Error rolling back connection for close", e);
      }
    }

    try {
      con.close();
    } catch (QueryException qe) {
      log.warn("Error closing connection", qe);
    }
  }
}
