/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
 * hacks in ambra. Once Mulgara supports multiple parallel transactions we can/should change this
 * back.
 *
 * @author Ronald Tschalär
 */
public class OtmTransactionManager extends AbstractPlatformTransactionManager {
  private Session session;
  private boolean clearSessionOnRB = false;
  private boolean skipFlushForRoTx = false;

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
      Session s = ((TransactionObject) transaction).getSession();
      s.beginTransaction(definition.isReadOnly(), determineTimeout(definition));

      if (definition.isReadOnly() && skipFlushForRoTx) {
        ((TransactionObject) transaction).savedFlushMode = s.getFlushMode();
        s.setFlushMode(Session.FlushMode.commit);
      }
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
    } finally {
      if (txObj.savedFlushMode != null)
        txObj.getSession().setFlushMode(txObj.savedFlushMode);
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
    } finally {
      if (clearSessionOnRB)
        txObj.getSession().clear();
      if (txObj.savedFlushMode != null)
        txObj.getSession().setFlushMode(txObj.savedFlushMode);
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
   * Set the clear-session-on-rollback flag. Because after a rollback the state of the objects in
   * the session does not match the state of the database, it's usually prudent to clear the
   * session after a rollback to prevent the application from continuing with stale objects.
   *
   * <p>Changing this flag will affect the current transaction, if there is one.
   *
   * @param clearSessionOnRB true if the session should be <code>clear()</code>'d on transaction
   *                         rollback
   */
  public void setClearSessionOnRollback(boolean clearSessionOnRB) {
    this.clearSessionOnRB = clearSessionOnRB;
  }

  /**
   * Set the skip-flush-on-readonly-transaction flag. If there are a large number of objects in
   * the session, a {@link Session#flush flush()} can take significant time. Since no modifications
   * should be done in a read-only transaction, this check can be skipped when in one. However,
   * disabling this check does mean that inadvertant modifications will be silently dropped (as
   * opposed to having an exception thrown). For this reason this flag currently sets the
   * flush-mode to <var>commit</var>, i.e. it just skips the flushes done before queries.
   *
   * <p>Changing this flag does not affect any current transaction, only new ones.
   *
   * @param clearSessionOnRB true if Session.flush's should skipped in read-only transactions
   */
  public void setSkipFlushOnReadonlyTx(boolean skipFlushForRoTx) {
    this.skipFlushForRoTx = skipFlushForRoTx;
  }

  /**
   * Implement SmartTransactionObject so spring can do proper rollback-only handling.
   */
  private class TransactionObject implements SmartTransactionObject {
    private final Session session;
    public Session.FlushMode savedFlushMode = null;

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
