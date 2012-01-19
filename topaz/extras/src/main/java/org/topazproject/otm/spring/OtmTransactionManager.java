/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.spring;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * A Spring transaction-manager that uses OTM transactions. Due to the current limitations on
 * Mulgara transactions, and hence current limitations on OTM transactions, this transaction
 * manager does not support suspending transactions or nested transactions (i.e. you can't use
 * propagation REQUIRES_NEW, NOT_SUPPORTED, or NESTED); however it does support re-using existing
 * transactions (i.e. you can use propagation REQUIRED, SUPPORTS, MANDATORY, and NEVER).
 *
 * <p>This transaction-manager has limited thread-safety: multiple threads may use the same
 * instance as long as there's only one thread per underlying session. "Underlying" here refers to
 * the fact that spring may inject a proxy for the Session object which actually delegates to one
 * of several real Session objects (this commonly happens when the transaction-manager has a longer
 * scope, such as singleton scope, than the Session objects).
 *
 * <p>Note that {@link #doGetTransaction doGetTransaction}, {@link #doBegin doBegin},
 * {@link #doCommit doCommit}, and {@link #doRollback doRollback} should really all be
 * <var>protected</var>, but are <var>public</var> instead to allow for the necessary tx stop/start
 * hacks in the publishing app. Once Mulgara supports multiple parallel transactions we can/should
 * change this back.
 *
 * @author Ronald Tschalär
 */
public class OtmTransactionManager extends AbstractPlatformTransactionManager {
  private Session session;

  /** 
   * Create a new otm-transaction-manager instance. 
   */
  public OtmTransactionManager() {
    setRollbackOnCommitFailure(false);
  }

  @Override
  public Object doGetTransaction() {
    return new TransactionObject(session);
  }

  @Override
  public void doBegin(Object transaction, TransactionDefinition definition)
      throws TransactionException {
    try {
      Transaction tx = ((TransactionObject) transaction).getSession().
                            beginTransaction(definition.isReadOnly(), definition.getTimeout());
    } catch (OtmException oe) {
      throw new TransactionSystemException("error beginning transaction", oe);
    }
  }

  @Override
  public void doCommit(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();

    Transaction tx = txObj.getSession().getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.commit();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error committing transaction", oe);
    }
  }

  @Override
  public void doRollback(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();

    Transaction tx = txObj.getSession().getTransaction();
    if (tx == null)
      throw new TransactionUsageException("no transaction active");

    try {
      tx.rollback();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error rolling back transaction", oe);
    }
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) {
    return ((TransactionObject) transaction).getSession().getTransaction() != null;
  }

  @Override
  protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
    TransactionObject txObj = (TransactionObject) status.getTransaction();
    try {
      txObj.setRollbackOnly();
    } catch (OtmException oe) {
      throw new TransactionSystemException("error setting rollback-only", oe);
    }
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Implement SmartTransactionObject so spring can do proper rollback-only handling.
   */
  private class TransactionObject implements SmartTransactionObject {
    private final Session session;

    public TransactionObject(Session session) {
      this.session = session;
    }

    Session getSession() {
      return session;
    }

    void setRollbackOnly() {
      session.getTransaction().setRollbackOnly();
    }

    public boolean isRollbackOnly() {
      return session.getTransaction().isRollbackOnly();
    }
  }
}
