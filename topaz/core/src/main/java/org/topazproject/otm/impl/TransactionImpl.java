/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

import javax.transaction.Status;
import javax.transaction.Transaction;

import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implement local Transaction as a wrapper around a jta transaction.
 *
 * @author Pradeep Krishnan
 */
public class TransactionImpl implements org.topazproject.otm.Transaction {
  private static final Log  log = LogFactory.getLog(TransactionImpl.class);

  private final Session     session;
  private final Transaction jtaTxn;

  public int ctr = 0;           // temporary hack for problems with mulgara's blank-nodes

  /**
   * Creates a new Transaction object.
   *
   * @param session the owning session
   * @param jtaTxn  the underlying jta transaction
   */
  public TransactionImpl(Session session, Transaction jtaTxn) {
    this.session = session;
    this.jtaTxn  = jtaTxn;

    if (log.isDebugEnabled())
      log.debug("Started local transaction " + this + " for session " + session);
  }

  public Session getSession() {
    return session;
  }

  public void setRollbackOnly() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Setting rollback-only on transaction " + this);

    try {
      jtaTxn.setRollbackOnly();
    } catch (Exception e) {
      throw new OtmException("Error setting rollback-only", e);
    }
  }

  public boolean isRollbackOnly() throws OtmException {
    try {
      return (jtaTxn.getStatus() == Status.STATUS_MARKED_ROLLBACK);
    } catch (Exception e) {
      throw new OtmException("Error getting rollback-only status", e);
    }
  }

  public void commit() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Committing transaction " + this);

    try {
      jtaTxn.commit();
    } catch (Exception e) {
      throw new OtmException("Error committing transaction", e);
    }
  }

  public void rollback() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Rolling back transaction " + this);

    try {
      jtaTxn.rollback();
    } catch (Exception e) {
      throw new OtmException("Error rolling back transaction", e);
    }
  }
}
