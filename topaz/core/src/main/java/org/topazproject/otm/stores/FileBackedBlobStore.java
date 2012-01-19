/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.otm.stores;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
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
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.topazproject.otm.AbstractBlob;
import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.Blob;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * A common base class for BlobStores that keep a local txn scoped copy
 * in the file-system. This is not required for a proper txn supported
 * BlobStores (like the Akubra project).
 *
 * @author Pradeep Krishnan
 *
 */
public abstract class FileBackedBlobStore implements BlobStore {
  protected static char[] hex = "0123456789abcdef".toCharArray();
  protected File root;
  private int nextId = 1;
  private List<File> txns = new ArrayList<File>();
  private final ResourceLock<Connection> storeLock = new ResourceLock<Connection>();

  /**
   * Construct a FileBackedBlobStore instance.
   *
   * @param root the root of the file-system scratch area.
   *
   * @throws OtmException on an error
   */
  protected FileBackedBlobStore(File root) throws OtmException {
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
    this.root = root;
  }

  /**
   * Allocate a txn specific directory root.
   *
   * @return the root of the file-system scratch area for this txn
   *
   * @throws OtmException on an error
   */
  protected File allocateTxn() {
    synchronized (txns) {
      return (txns.size() > 0) ? txns.remove(0) : new File(root, "txn" + nextId++);
    }
  }

  /**
   * Return back the txn directory.
   *
   * @param txn the txn temp directory
   */
  protected void returnTxn(File txn) {
    synchronized (txns) {
      txns.add(txn);
    }
  }

  public void flush(Connection con) throws OtmException {
  }

  public Blob getBlob(ClassMetadata cm, String id, Object blob, Connection con) throws OtmException {
    FileBackedBlobStoreConnection ebsc = (FileBackedBlobStoreConnection) con;
    return ebsc.get(cm, id, blob);
  }

  public ResourceLock<Connection> getStoreLock() {
    return storeLock;
  }

  /**
   * A convenient base class for file-backed blob-store implementations.
   *
   * @author Pradeep Krishnan
   *
   */
  public static abstract class FileBackedBlobStoreConnection extends AbstractConnection {
    protected final Log             log     = LogFactory.getLog(getClass());
    protected final FileBackedBlobStore store;
    protected File                  txn;
    protected Map<String, FileBackedBlob> blobs = new HashMap<String, FileBackedBlob>();

    /**
     * Construct a FileBackedBlobStoreConnection object.
     *
     * @param store the blob-store
     * @param sess the session for this connection
     *
     * @throws OtmException on an error
     */
    public FileBackedBlobStoreConnection(FileBackedBlobStore store, Session sess) throws OtmException {
      super(sess);
      this.store = store;
      this.txn = store.allocateTxn();

      enlistResource(newXAResource());
    }

    /**
     * Normalize the given id URI.
     *
     * @param id the URI to normalize
     *
     * @return the normalized URI
     *
     * @throws OtmException on an error
     */
    protected String normalize(String id) throws OtmException {
      try {
        URI uri = new URI(id);

        return uri.normalize().toString();
      } catch (Exception e) {
        throw new OtmException("Blob id must be a valid URI. id = " + id, e);
      }
    }

    /**
     * Convert the id to a File.
     *
     * @param baseDir the base directory
     * @param normalizedId the id
     * @param suffix the suffix for the file
     *
     * @return the File corresponding to this id
     *
     * @throws OtmException on an error
     */
    protected File toFile(File baseDir, String normalizedId, String suffix) throws OtmException {
      try {
        byte[] input = normalizedId.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(input);
        assert digest.length == 20; // 160-bit digest

        File f = baseDir;
        int idx = 0;

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

            f = new File(f, sb.append(suffix).toString());
          }
        }

        return f;
      } catch (Exception e) {
        throw new OtmException("Failed to map id to a file under " + baseDir, e);
      }
    }

    /**
     * Get the Blob object for the given id. Actual creation is done by {@link #doGetBlob}.
     *
     * @param cm the metadata of the containing entity
     * @param id the blob id
     * @param instance the containing entity instance
     *
     * @return the Blob object
     *
     * @throws OtmException on an error
     */
    public FileBackedBlob get(ClassMetadata cm, String id, Object instance) throws OtmException {
      if (txn == null)
        throw new OtmException("Attempt to use a connection that is closed");

      id = normalize(id);

      FileBackedBlob blob = blobs.get(id);

      if (blob != null)
        return blob;

      File work = toFile(txn, id, ".dat");

      if (work.exists())
        work.delete();

      if (work.exists())
        throw new Error("Failed to delete file:" + work);

      blob = doGetBlob(cm, id, instance, work);

      blobs.put(id, blob);

      return blob;
    }

    /**
     * Hook into sub-classes for creation of new Blob instances.
     *
     * @param cm the metadata of the containing entity
     * @param id the blob id
     * @param instance the containing entity instance
     * @param work the scratch file
     *
     * @return the Blob object
     *
     * @throws OtmException on an error
     */
    protected abstract FileBackedBlob doGetBlob(ClassMetadata cm, String id, Object instance, File work) throws OtmException;

    private XAResource newXAResource() {
      return new XAResource() {
        public void commit(Xid xid, boolean onePhase) throws XAException {
          if (onePhase) {
            try {
              onePhase = !doPrepare();
            } catch (XAException xae) {
              doRollback();
              throw xae;
            }
          }
          if (onePhase)
            close();
          else
            doCommit();
        }

        public void rollback(Xid xid) throws XAException {
          doRollback();
        }

        public int prepare(Xid xid) throws XAException {
          // assumption: doPrepare only throws RMERR, which means this resource stays in S2
          if (doPrepare()) {
            return XA_OK;
          } else {
            close();
            return XA_RDONLY;
          }
        }

        public void start(Xid xid, int flags) {
        }

        public void end(Xid xid, int flags) {
        }

        public Xid[] recover(int flag) {
          return new Xid[0];
        }

        public void forget(Xid xid) {
        }

        public boolean isSameRM(XAResource xares) {
          return xares == this;
        }

        public int getTransactionTimeout() {
          return 0;
        }

        public boolean setTransactionTimeout(int secs) {
          return false;
        }
      };
    }

    protected boolean doPrepare() throws XAException {
      try {
        if (log.isTraceEnabled())
          log.trace("doPrepare starting ...");
        int mods = 0;
        // Note: we keep the lock acquired thru the commit phase.
        store.getStoreLock().acquireWrite(this);
        for (FileBackedBlob blob : blobs.values())
          if (blob.prepare())
            mods++;
        if (log.isDebugEnabled())
          log.debug("doPrepare found " + mods + " change(s) to commit");
        return (mods > 0);
      } catch (Exception oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      }
    }

    protected void doCommit() throws XAException {
      if (log.isTraceEnabled())
        log.trace("doCommit starting ...");
      boolean abort = true;
      try {
        // Note: lock acquired in doPrepare().
        for (FileBackedBlob blob : blobs.values())
          blob.commit();
        abort = false;
      } catch (Exception oe) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(oe);
      } finally {
        cleanup(abort);
      }
    }

    protected void doRollback() throws XAException {
      if (log.isTraceEnabled())
        log.trace("doRollback starting ...");
      try {
        try {
          store.getStoreLock().acquireWrite(this);
        } catch (InterruptedException e) {
          log.warn("Interrupted while acquiring lock for roll-back. Proceeding without lock", e);
        }
        cleanup(true);
      } catch (Exception e) {
        throw (XAException) new XAException(XAException.XAER_RMERR).initCause(e);
      }
    }

    protected void cleanup(boolean abort) {
      if (log.isTraceEnabled())
        log.trace("cleanup(abort=" + abort + ") starting ...");
      try {
        for (FileBackedBlob blob : blobs.values())
          blob.cleanup(abort);

        blobs.clear();

        if (txn != null)
          store.returnTxn(txn);

        txn = null;
      } finally {
        int cnt = store.getStoreLock().writeCount(this);
        while (cnt-- > 0)
          store.getStoreLock().releaseWrite(this);
      }
    }

    public void close() {
      if (log.isTraceEnabled())
        log.trace("close starting ...");
      try {
        if (txn != null)
          doRollback();
      } catch (XAException e) {
        log.warn("Failed to close connection", e);
      }
    }

    /**
     * A Blob instance that is file backed. All operations in txn scope is
     * done on a local File. Sub-classes only implement copying to and
     * from the store to the txn scratch directory.
     *
     * @author Pradeep Krishnan
     */
    public abstract class FileBackedBlob extends AbstractBlob {
      protected Boolean     exists = null;
      protected ChangeState state  = ChangeState.NONE;
      protected ChangeState undo   = ChangeState.NONE;
      protected ChangeState marked = ChangeState.NONE;
      protected final File  tmp;
      protected final File  bak;

      /**
       * Creates an instance of FileBackedBlob.
       *
       * @param id the blob id
       * @param tmp the txn tmp file
       * @param bak the back-up file to use before modifying the value in store
       */
      public FileBackedBlob(String id, File tmp, File bak) {
        super(id);
        this.tmp = tmp;
        this.bak = bak;
      }

      public ChangeState getChangeState() {
        return state;
      }

      public ChangeState mark() {
        if (log.isDebugEnabled())
          log.debug("Marking... previous-mark = " + marked + ", state = " + state + ", id = " + getId());
        ChangeState prev = marked;
        marked = ChangeState.NONE;
        return prev;
      }

      @Override
      public InputStream doGetInputStream() throws OtmException {
        if (!exists())
          throw new OtmException("Blob " + getId() + " does not exist. Write to it first to create.");

        if (log.isTraceEnabled())
          log.trace("Creating inputstream for " + getId() + " from " + tmp);

        try {
          return new FileInputStream(tmp);
        } catch (IOException e) {
          throw new OtmException("Failed to open file: " + tmp + " for " + getId(), e);
        }
      }

      @Override
      public OutputStream doGetOutputStream() throws OtmException {
        if (state == ChangeState.DELETED)
          throw new OtmException("Blob " + getId() + " is deleted. Cannot write to it.");

        create();

        if (log.isTraceEnabled())
          log.trace("Creating outputstream for " + getId() + " from " + tmp);

        try {
          return new FileOutputStream(tmp);
        } catch (IOException e) {
          throw new OtmException("Failed to open file: " + tmp + " for " + getId(), e);
        }
      }

      @Override
      public void closed(Closeable stream) {
        if (log.isTraceEnabled())
          log.trace("Closing stream for " + getId() + " on " + tmp);
        super.closed(stream);
      }

      @Override
      public void writing(OutputStream out) {
        switch (state) {
          case CREATED:
          case NONE:
            if (log.isTraceEnabled())
              log.trace("Write detected on " + tmp + " for " + getId());
            state = marked = ChangeState.WRITTEN;
            break;
          case DELETED:
            // All streams should be closed on delete.
            log.error("Writing to deleted Blob file: " + tmp + " for " + getId());
        }
      }

      public boolean create() throws OtmException {
        switch (state) {
          case DELETED:
          case NONE:
            if (log.isTraceEnabled())
              log.trace("Creating " + tmp + " for " + getId());
            try {
              boolean ret = tmp.createNewFile();
              state = marked = ChangeState.CREATED;
              exists = Boolean.TRUE;
              return ret;
            } catch (IOException e) {
              throw new OtmException("Failed to create file: " + tmp + " for " + getId(), e);
            }
        }
        return false;
      }

      public boolean delete() throws OtmException {
        switch (state) {
          case CREATED:
          case NONE:
          case WRITTEN:
            for (Closeable stream : streams) {
              try {
                stream.close();
              } catch (IOException e) {
                log.warn("Failed to close a stream : " + tmp + " for " + getId(), e);
              }
            }
            streams.clear();
            if (log.isTraceEnabled())
              log.trace("Deleting " + tmp + " for " + getId());
            boolean ret = tmp.delete();
            state = marked = ChangeState.DELETED;
            exists = Boolean.FALSE;
            return ret;
        }
        return false;
      }

      public boolean exists() throws OtmException {
        if (exists != null)
          return exists;

        if (log.isTraceEnabled())
          log.trace("performing exists() check on " + getId());

        exists = copyFromStore(tmp, false);

        if (log.isTraceEnabled())
          log.trace("exists() check result: " + exists);

        return exists;
      }

      /**
       * Implement in sub-classes to actually create an instance of this blob in store.
       *
       * @return true only if a new instance was created.
       *
       * @throws OtmException on an error
       */
      protected abstract boolean createInStore() throws OtmException;

      /**
       * Implement in sub-classes to copy from the store to the txn scratch file.
       *
       * @param to the outputs stream to copy to
       * @param asBackup reason for the copy
       *
       * @return true only if the blob was copied over
       *
       * @throws OtmException on an error
       */
      protected abstract boolean copyFromStore(OutputStream to, boolean asBackup) throws OtmException;

      /**
       * Implement in sub-classes to copy/move from the txn scratch file to the blob-store.
       *
       * @param from the scratch file
       *
       * @return true only if the blob was copied/moved
       *
       * @throws OtmException on an error
       */
      protected abstract boolean moveToStore(File from) throws OtmException;

      /**
       * Implement in sub-classes to delete/purge this object.
       *
       * @return true only if the blob was deleted
       *
       * @throws OtmException on an error
       */
      protected abstract boolean deleteFromStore() throws OtmException;

      /**
       * Copy from store to the given file. Calls {@link #copyFromStore(OutputStream, boolean)}.
       *
       * @param file the file to copy to
       *
       * @param asBackup to indicate what this copy is for
       *
       * @return true only if the blob was copied over
       */
      protected final boolean copyFromStore(File file, boolean asBackup) {
        OutputStream out = null;
        try {
          out = new FileOutputStream(file);
        } catch (IOException e) {
          throw new OtmException("Failed to open file " + file + " for write.", e);
        }

        try {
          return copyFromStore(out, false);
        } finally {
          closeAll(out);
        }
      }

      /**
       * Prepare for commit. Backup things etc.
       *
       * @return true only if there was something that needed committing
       *
       * @throws OtmException on an error
       */
      protected boolean prepare() throws OtmException {
        if (log.isTraceEnabled())
          log.trace("Preparing to commit " + getId());

        close();
        switch (state) {
          case DELETED:
          case WRITTEN:
            backup();
        }

        return state != ChangeState.NONE;
      }

      /**
       * Commit changes.
       *
       * @throws OtmException on an error
       */
      protected void commit() throws OtmException {
        boolean success = true;
        String operation = "no-op";
        switch (state) {
          case DELETED:
            operation = "delete";
            success = deleteFromStore();
            break;
          case CREATED:
            operation = "create";
            success = createInStore();
            break;
          case WRITTEN:
            operation = "save";
            success = moveToStore(tmp);
            break;
        }

        if (!success)
          throw new OtmException("Commit(" + operation + ") failed for " + getId());
        else {
          if (log.isDebugEnabled())
            log.debug("Committed(" + operation + ") on " + getId());
          exists = null;
          undo = state;
          state = marked = ChangeState.NONE;
        }
      }

      /**
       * Clean-up since the txn is now complete.
       *
       * @param abort if true, restore from the backup
       */
      protected void cleanup(boolean abort) {
        if (abort && ((undo == ChangeState.DELETED) || (undo == ChangeState.WRITTEN)))
          restore();

        undo = ChangeState.NONE;
        exists = null;
        tmp.delete();
      }

      private void backup() throws OtmException {
        if (bak.exists()) {
          if (log.isDebugEnabled())
            log.debug("Deleting old backup for " + getId());
          bak.delete();
          if (bak.exists())
            throw new OtmException("Failed to delete old backup at " + bak);
        }

        copyFromStore(bak, true);
      }

      private void restore() {
        if (!bak.exists())
          return;

        if (log.isDebugEnabled())
          log.debug("Restoring " + bak + " for id " + getId());

        try {
          if (!moveToStore(bak))
            log.error("Failed to restore from backup " + bak + " for id " + getId());
          else
            log.warn("Restored from backup " + bak + " for id " + getId());
        } catch (Exception e) {
          log.error("Failed to restore from backup " + bak + " for id " + getId(), e);
        }
      }
    }
  }
}
