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

import javax.transaction.xa.XAResource;

/**
 * A connection impl that stores the current session.
 *
 * @author Pradeep Krishnan
  */
public abstract class AbstractConnection implements Connection {
  private final Session sess;

  /** 
   * Create a new connection. 
   * 
   * @param sess the session this connection belongs to
   */
  protected AbstractConnection(Session sess) {
    this.sess = sess;
  }

  /** 
   * Get the owning session. 
   * 
   * @return the session
   */
  public Session getSession() {
    return sess;
  }

  /** 
   * Enlist the given xa-resource with the session's transaction-manager.
   * 
   * @param xaRes the xa-resource to enlist
   * @throws OtmException if an error occurred enlisting <var>xaRes</var>
   */
  protected void enlistResource(XAResource xaRes) throws OtmException {
    try {
      sess.getSessionFactory().getTransactionManager().getTransaction().enlistResource(xaRes);
    } catch (Exception e) {
      throw new OtmException("Error enlisting resource", e);
    }
  }
}
