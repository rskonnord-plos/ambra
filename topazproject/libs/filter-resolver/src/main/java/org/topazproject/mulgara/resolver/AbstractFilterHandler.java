/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver;

import java.net.URI;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/** 
 * Superclass for filter handlers. All methods are no-ops.
 * 
 * @author Ronald Tschalär
 */
public abstract class AbstractFilterHandler implements FilterHandler {
  public void modelCreated(URI filterModel, URI realModel) throws ResolverException {
  }

  public void modelRemoved(URI filterModel, URI realModel) throws ResolverException {
  }

  public void modelModified(URI filterModel, URI realModel, Statements stmts, boolean occurs,
                            ResolverSession resolverSession) throws ResolverException {
  }

  public XAResource getXAResource() {
    return null;
  }

  public void close() {
  }

  /** 
   * Simple XAResource that does nothing.
   */
  protected static class DummyXAResource implements XAResource {
    private int txTimeout = 10;

    public void start(Xid xid, int flags) {
    }

    public void end(Xid xid, int flags) {
    }

    public int prepare(Xid xid) {
      return XA_OK;
    }

    public void commit(Xid xid, boolean onePhase) {
    }

    public void rollback(Xid xid) {
    }

    public int getTransactionTimeout() {
      return txTimeout;
    }

    public boolean setTransactionTimeout(int transactionTimeout) {
      txTimeout = transactionTimeout;
      return true;
    }

    public Xid[] recover(int flag) {
      return new Xid[0];
    }

    public void forget(Xid xid) {
    }

    public boolean isSameRM(XAResource xaResource) {
      return xaResource == this;
    }
  }
}
