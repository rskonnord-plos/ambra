/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.otm.impl.btm;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.internal.XAResourceHolderState;
import bitronix.tm.resource.common.XAResourceProducer;
import bitronix.tm.resource.common.XAResourceHolder;
import bitronix.tm.resource.common.XAStatefulHolder;
import bitronix.tm.resource.common.ResourceBean;
import bitronix.tm.resource.common.AbstractXAResourceHolder;
import bitronix.tm.resource.ResourceRegistrar;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.naming.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Date;
import java.util.List;

/**
 * @author Dragisa Krsmanovic
 */
public class TransactionManagerHelper {

  private static BitronixTransactionManager defTxnMgr;
  private static Object                     txnMgrCleaner;

  /**
   * Creates Bitronix implementation of JTA transaction manager
   *
   * @return JTA transaction manager
   */
  public static synchronized TransactionManager getTransactionManager() {
    if (defTxnMgr == null) {
      defTxnMgr = TransactionManagerServices.getTransactionManager();
      txnMgrCleaner = new Object() {
        protected void finalize() {
          defTxnMgr.shutdown();
        }
      };
      SimpleXAResourceProducer.create();
    }

    return defTxnMgr;
  }

  /**
   * @author Ronald Tschalär
   */
  private static class SimpleXAResourceProducer implements XAResourceProducer {

    private static XAResourceProducer xaResourceProducer;

    private final Map<XAResource, WeakReference<XAResourceHolder>> xaresHolders =
                                    new WeakHashMap<XAResource, WeakReference<XAResourceHolder>>();

    private SimpleXAResourceProducer() {
    }

    public void init() {
    }

    public void close() {
    }

    public String getUniqueName() {
      return "OTM-Simple-Resource-Producer";
    }

    public XAResourceHolderState startRecovery() {
      return createResHolder(new RecoveryXAResource()).getXAResourceHolderState();
    }

    public void endRecovery() {
    }

    public Reference getReference() {
      return null;
    }

    public XAStatefulHolder createPooledConnection(Object xaFactory, ResourceBean bean) {
      return null;
    }

    public XAResourceHolder findXAResourceHolder(final XAResource xaResource) {
      WeakReference<XAResourceHolder> resHolderRef = xaresHolders.get(xaResource);
      XAResourceHolder resHolder = (resHolderRef != null) ? resHolderRef.get() : null;

      if (resHolder == null) {
        resHolder = createResHolder(xaResource);
        xaresHolders.put(xaResource, new WeakReference<XAResourceHolder>(resHolder));
      }

      return resHolder;
    }

    private static XAResourceHolder createResHolder(XAResource xaResource) {
      ResourceBean rb = new ResourceBean() { };
      rb.setUniqueName(xaResource.getClass().getName() + System.identityHashCode(xaResource));
      rb.setApplyTransactionTimeout(true);

      XAResourceHolder resHolder = new SimpleXAResourceHolder(xaResource);
      resHolder.setXAResourceHolderState(new XAResourceHolderState(resHolder, rb));

      return resHolder;
    }

    private static class SimpleXAResourceHolder extends AbstractXAResourceHolder {
      private final XAResource xares;

      SimpleXAResourceHolder(XAResource xares) {
        this.xares = xares;
      }

      public void close() {
      }

      public Object getConnectionHandle() {
        return null;
      }

      public Date getLastReleaseDate() {
        return null;
      }

      public List getXAResourceHolders() {
        return null;
      }

      public XAResource getXAResource() {
        return xares;
      }
    }

    private static class RecoveryXAResource implements XAResource {
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

      public Xid[] recover(int flag) {
        // recovery not supported (yet)
        return null;
      }

      public void forget(Xid xid) {
      }

      public int getTransactionTimeout() {
        return 10;
      }

      public boolean setTransactionTimeout(int transactionTimeout) {
        return false;
      }

      public boolean isSameRM(XAResource xaResource) {
        return xaResource == this;
      }
    }

    /**
     * Create implementation of XAResourceProducer
     * @return implementation of XAResourceProducer
     */
    public static synchronized XAResourceProducer create() {
      if (xaResourceProducer == null) {
        xaResourceProducer = new SimpleXAResourceProducer();
        ResourceRegistrar.register(xaResourceProducer);
      }
      return xaResourceProducer;
    }

  }
}
