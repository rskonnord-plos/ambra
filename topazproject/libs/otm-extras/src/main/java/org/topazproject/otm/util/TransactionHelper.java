/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.util;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper to execute code inside of an OTM transaction. This should probably get replaced
 * by a spring TransactionManager.
 *
 * <p>Example usage:
 * <pre>
 *    Foo f =
 *      TransactionHelper.doInTx(session, new TransactionHelper.Action<Foo>() {
 *        public Foo run(Transaction tx) {
 *          List l = tx.getSession().createCriteria(Foo.class)...list();
 *          return (Foo) l(0);
 *        }
 *      });
 * </pre>
 *
 * @author Ronald Tschalär
 */
public class TransactionHelper {
  private static Log log = LogFactory.getLog(TransactionHelper.class);

  /** 
   * Not meant to be instantiated. 
   */
  private TransactionHelper() {
  }

  /** 
   * Run the given action within a transaction.
   * 
   * @param s      the otm session to use    
   * @param action the action to run
   * @return the value returned by the action
   */
  public static <T> T doInTx(Session s, Action<T> action) {
    Transaction tx = null;
    try {
      tx = s.beginTransaction();
      T res = action.run(tx);
      tx.commit();
      return res;
    } catch (Throwable t) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
      if (t instanceof Error)
        throw (Error) t;
      throw (RuntimeException) t;
    }
  }

  /** 
   * Run the given action within a transaction.
   * 
   * @param s      the otm session to use    
   * @param action the action to run
   * @return the value returned by the action
   */
  public static <T, E extends Throwable> T doInTxE(Session s, ActionE<T, E> action) throws E {
    Transaction tx = null;
    try {
      tx = s.beginTransaction();
      T res = action.run(tx);
      tx.commit();
      return res;
    } catch (Throwable t) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException oe) {
        log.warn("rollback failed", oe);
      }
      if (t instanceof Error)
        throw (Error) t;
      if (t instanceof RuntimeException)
        throw (RuntimeException) t;
      throw (E) t;
    }
  }

  /**
   * The interface actions must implement.
   */
  public static interface Action<T> {
    /** 
     * This is run within the context of a transaction.
     * 
     * @param tx the current transaction
     * @return anything you want
     */
    T run(Transaction tx);
  }

  /**
   * The interface actions which throw an exception must implement.
   */
  public static interface ActionE<T, E extends Throwable> {
    /** 
     * This is run within the context of a transaction.
     * 
     * @param tx the current transaction
     * @return anything you want
     */
    T run(Transaction tx) throws E;
  }
}
