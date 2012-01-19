/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;

import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.AbstractStore;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * A simple File backed BlobStore that supports transactions and
 * ensures repeatable reads and resource level locks for writes.
 *
 * @author Pradeep Krishnan
 */
public class SimpleBlobStore extends AbstractStore implements BlobStore {
  private static final Log log = LogFactory.getLog(ItqlStore.class);
  private File              root;
  private File              store;
  private static int        nextId = 1;
  private Map<String, Lock> locks  = new HashMap<String, Lock>();
  private List<File>        txns   = new ArrayList<File>();

  /**
   * Creates a new SimpleBlobStore object.
   *
   * @throws OtmException on an error
   */
  public SimpleBlobStore() throws OtmException {
    this(new File(new File(System.getProperty("usr.dir")), "blobstore"));
  }

  /**
   * Creates a new SimpleBlobStore object.
   *
   * @param root the root directory for blobstore
   *
   * @throws OtmException DOCUMENT ME!
   */
  public SimpleBlobStore(String root) throws OtmException {
    this(new File(root));
  }

/**
   * Creates a new SimpleBlobStore object.
   *
   * @param root 
   */
  public SimpleBlobStore(File root) throws OtmException {
    try {
      if (!root.exists())
        root.mkdirs();

      if (!root.exists())
        throw new IOException("Failed to create " + root);

      if (!root.isDirectory())
        throw new IOException("Not a directory " + root);
    } catch (Throwable t) {
      throw new OtmException("Invalid root directory " + root, t);
    }

    this.root    = root;
    this.store   = new File(root, "blobs");
  }

  /*
   * inherited javadoc
   */
  public Connection openConnection(Session sess, boolean readOnly) throws OtmException {
    int txnId;

    synchronized (txns) {
      if (txns.size() > 0)
        return new SimpleBlobStoreConnection(this, store, txns.remove(0), sess);

      txnId = nextId++;
    }

    File txn = new File(root, "txn" + txnId);

    return new SimpleBlobStoreConnection(this, store, txn, sess);
  }

  /**
   * Return back the txn directory.
   *
   * @param txn the txn temp directory
   */
  void returnTxn(File txn) {
    synchronized (txns) {
      txns.add(txn);
    }
  }

  /**
   * Gets the lock to use for an id.
   *
   * @param id the blob id
   *
   * @return the lock to use
   *
   * @throws OtmException on an error
   */
  Lock getLock(String id) throws OtmException {
    synchronized (locks) {
      Lock l = locks.get(id);

      if (l == null)
        locks.put(id, l = new Lock());

      return l;
    }
  }

  /*
   * inherited javadoc
   */
  public void insert(ClassMetadata cm, String id, byte[] blob, Connection con)
              throws OtmException {
    SimpleBlobStoreConnection ebsc = (SimpleBlobStoreConnection) con;
    ebsc.insert(id, blob);
  }

  /*
   * inherited javadoc
   */
  public void delete(ClassMetadata cm, String id, Connection con)
              throws OtmException {
    SimpleBlobStoreConnection ebsc = (SimpleBlobStoreConnection) con;
    ebsc.delete(id);
  }

  /*
   * inherited javadoc
   */
  public byte[] get(ClassMetadata cm, String id, Connection con)
             throws OtmException {
    SimpleBlobStoreConnection ebsc = (SimpleBlobStoreConnection) con;

    return ebsc.get(id);
  }

  public static class SimpleBlobStoreConnection extends AbstractConnection {
    private static Object             deadLockCheck = new Object();
    private static char[]             hex           =
      new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private final File                store;
    private File                      txn;
    private final SimpleBlobStore     bs;
    private SimpleBlobStoreConnection blockedOn;
    private Map<String, File>         undoI;
    private Map<String, File>         undoD;
    private Map<String, File>         insertMap     = new HashMap<String, File>();
    private Map<String, File>         deleteMap     = new HashMap<String, File>();
    private Map<String, File>         readMap       = new HashMap<String, File>();
    private Set<Lock>                 locks         = new HashSet<Lock>();

    public SimpleBlobStoreConnection(SimpleBlobStore bs, File store, File txn, Session sess)
           throws OtmException {
      super(sess);
      this.bs      = bs;
      this.store   = store;
      this.txn     = txn;

      enlistResource(newXAResource());
    }

    private String normalize(String id) throws OtmException {
      try {
        URI uri = new URI(id);

        return uri.normalize().toString();
      } catch (Exception e) {
        throw new OtmException("Blob id must be a valid URI. id = " + id, e);
      }
    }

    private File toFile(File baseDir, String normalizedId)
                 throws OtmException {
      try {
        byte[] input  = normalizedId.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(input);
        assert digest.length == 20; // 160-bit digest

        File f   = baseDir;
        int  idx = 0;

        for (int i = 0; i < 4; i++) {
          StringBuilder sb = new StringBuilder();

          for (int j = 0; j < 5; j++) {
            byte b = digest[idx++];
            sb.append(hex[(b >>> 4) & 0x0F]);
            sb.append(hex[b & 0x0F]);
          }

          if (i < 3)
            f = new File(f, sb.toString());
          else {
            f.mkdirs();

            if (!f.exists())
              throw new IOException("Failed to create directory " + f);

            f = new File(f, sb.append(".dat").toString());
          }
        }

        return f;
      } catch (Exception e) {
        throw new OtmException("Failed to map id to a file under " + baseDir, e);
      }
    }

    public void insert(String id, byte[] blob) throws OtmException {
      if (txn == null)
        throw new OtmException("Attempt to use a connection that is closed");

      id = normalize(id);

      Lock l = bs.getLock(id);
      l.acquire(this);
      locks.add(l);

      File f = toFile(store, id);

      if (f.exists() && !deleteMap.containsKey(id))
        throw new OtmException("A blob already exists with id " + id);

      f = toFile(txn, id);

      OutputStream o = null;

      try {
        o = new BufferedOutputStream(new FileOutputStream(f));
        o.write(blob);
        o.close();
        o = null;
      } catch (Exception e) {
        try {
          if (o != null)
            o.close();
        } catch (Throwable t) {
          log.warn("Failed to close file for blob " + id, t);
        }

        throw new OtmException("Failed to write blob for " + id, e);
      }

      deleteMap.remove(id);
      insertMap.put(id, f);
      readMap.remove(id);
    }

    public void delete(String id) throws OtmException {
      if (txn == null)
        throw new OtmException("Attempt to use a connection that is closed");

      id = normalize(id);

      Lock l = bs.getLock(id);
      l.acquire(this);
      locks.add(l);

      toFile(txn, id); // Catch any path creation errors early
      File o = toFile(store, id);

      // XXX: Treats deletion of non-existing files as 
      insertMap.remove(id);
      deleteMap.put(id, o);
      readMap.remove(id);
    }

    public byte[] get(String id) throws OtmException {
      if (txn == null)
        throw new OtmException("Attempt to use a connection that is closed");

      id = normalize(id);

      if (deleteMap.get(id) != null)
        return null;

      Lock         l   = null;
      InputStream  in  = null;
      OutputStream out = null;

      try {
        File copy = null;
        File f    = insertMap.get(id);

        if (f == null)
          f = readMap.get(id);

        if (f == null) {
          f   = toFile(store, id);

          // Acquire the lock while we do a copy
          l   = bs.getLock(id);
          l.acquire(this);

          if (!f.exists())
            return null;

          copy = toFile(txn, id);
        }

        in = new BufferedInputStream(new FileInputStream(f));

        byte[] blob = new byte[(int) f.length()];
        in.read(blob);

        if (copy != null) {
          out = new BufferedOutputStream(new FileOutputStream(copy));
          out.write(blob);
          out.close();
          out = null;
          readMap.put(id, copy);
        }

        return blob;
      } catch (Exception e) {
        throw new OtmException("Failed to read blob: id = " + id, e);
      } finally {
        try {
          if (in != null)
            in.close();
        } catch (Throwable t) {
          log.warn("Failed to close file for blob: id = "  + id, t);
        }

        try {
          if (out != null)
            out.close();
        } catch (Throwable t) {
          log.warn("Failed to close copy of blob: id = "  + id, t);
        }

        if (l != null)
          l.release();
      }
    }

    private XAResource newXAResource() {
      return new XAResource() {
        public void    commit(Xid xid, boolean onePhase) throws XAException { doCommit(); }
        public void    rollback(Xid xid)                 throws XAException { doRollback(); }
        public int     prepare(Xid xid)                  throws XAException { doPrepare(); return XA_OK; }

        public void    start(Xid xid, int flags)         { }
        public void    end(Xid xid, int flags)           { }
        public Xid[]   recover(int flag)                 { return new Xid[0]; }
        public void    forget(Xid xid)                   { }
        public boolean isSameRM(XAResource xares)        { return xares == this; }
        public int     getTransactionTimeout()           { return 0; }
        public boolean setTransactionTimeout(int secs)   { return false; }
      };
    }

    private void doPrepare() throws XAException {
      undoI = new HashMap<String, File>();
      try {
        rename(insertMap, store, undoI);
      } catch (OtmException oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      }
      insertMap = null;
    }

    private void doCommit() throws XAException {
      try {
        undoD = new HashMap<String, File>();
        rename(deleteMap, txn, undoD);
        undoD   = null;
        undoI   = null;
      } catch (OtmException oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      } finally {
        close();
      }
    }

    private void doRollback() {
      close();
    }

    public void close() {
      // undo deletes
      if (undoD != null) {
        rename(undoD, store);
        undoD = null;
      }

      // undo inserts
      if (undoI != null) {
        delete(undoI);
        undoI = null;
      }

      // clear up space used by insert temporaries
      if (insertMap != null) {
        delete(insertMap);
        insertMap = null;
      }

      // clear up space used for repeatable reads
      if (readMap != null) {
        delete(readMap);
        readMap = null;
      }

      // XXX: deleted files will exist in the txn area
      deleteMap = null;

      if (locks != null) {
        for (Lock l : locks)
          l.release();

        locks = null;
      }

      if (txn != null)
        bs.returnTxn(txn);

      txn = null;
    }

    private Map<String, File> rename(Map<String, File> map, File dest, Map<String, File> undo)
                              throws OtmException {
      OtmException error = null;

      for (String id : map.keySet()) {
        try {
          File f = toFile(dest, id);

          if (f.exists())
            f.delete();

          File s = map.get(id);

          if (s.exists()) {
            s.renameTo(f);

            if (!f.exists())
              throw new OtmException("Failed to create " + f);

            if (undo != null)
              undo.put(id, f);
          }
        } catch (Throwable t) {
          OtmException e;

          if (t instanceof OtmException)
            e = (OtmException) t;
          else
            e = new OtmException("Failed to create file for " + id, t);

          if (undo != null)
            throw e;

          if (error == null)
            error = e;
        }
      }

      if (error != null)
        throw error;

      return undo;
    }

    private void rename(Map<String, File> map, File dest) {
      try {
        rename(map, dest, null);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Ignoring the error while renaming. Some or all files in " 
              + map.values() + " may not have been renamed", t);
      }
    }

    private void delete(Map<String, File> map) {
      for (String id : map.keySet()) {
        try {
          map.get(id).delete();
        } catch (Throwable t) {
          if (map.get(id).exists())
            log.warn("Failed to delete file " + map.get(id), t);
        }
      }
    }

    private void setBlockedOn(SimpleBlobStoreConnection con)
                       throws OtmException {
      synchronized (deadLockCheck) {
        for (SimpleBlobStoreConnection c = con; c != null; c = c.blockedOn)
          if (c == this)
            throw new OtmException("Deadlock detected. (A waits for B; B waits for A etc.)");

        blockedOn = con;
      }
    }

    /**
     * Suspend the thread association on locks held by this connection. 
     * This doesn't mean locks are released. It is just that the thread 
     * based dead-lock detection can now be bypassed. This makes the 
     * SimpleBlobStore usable in a thread-pool situation such as 
     * when exposed via RMI or SOAP to client apps. 
     */
    public void suspend() {
      for (Lock lock : locks)
        lock.thread = null;
    }

    /**
     * Resume the thread association on locks held by this connection. 
     * Locks are now attached to the current thread. See {@link #suspend}.
     */
    public void resume() {
      Thread thread = Thread.currentThread();
      for (Lock lock : locks)
        lock.thread = thread;
    }
  }

  private static class Lock {
    SimpleBlobStoreConnection owner  = null;
    Thread                    thread = null;

    public synchronized void acquire(SimpleBlobStoreConnection con)
                              throws OtmException {
      if (owner == con)
        return;

      while (owner != null) {
        if (thread == Thread.currentThread())
          throw new OtmException("Deadlock detected. Transactions from the same thread"
                                 + " can't compete for the same resource.");

        con.setBlockedOn(owner);

        try {
          wait();
        } catch (InterruptedException e) {
          throw new OtmException("Interrupted while waiting to obtain a lock", e);
        } finally {
          con.setBlockedOn(null);
        }
      }

      owner    = con;
      thread   = Thread.currentThread();
    }

    public synchronized void release() {
      owner    = null;
      thread   = null;
      notify();
    }
  }
}
