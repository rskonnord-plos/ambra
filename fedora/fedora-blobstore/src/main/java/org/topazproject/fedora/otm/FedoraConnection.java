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
package org.topazproject.fedora.otm;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.fedora.client.Datastream;
import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;
import org.topazproject.fedora.otm.FedoraBlob.INGEST_OP;

import org.topazproject.otm.AbstractBlob;
import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.Blob;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Blob.ChangeState;

/**
 * Acts as the client to Fedora and manages the transactional aspects of user operations.
 *
 * @author Pradeep Krishnan
 */
public class FedoraConnection extends AbstractConnection {
  private static final Log    log    = LogFactory.getLog(FedoraConnection.class);
  private FedoraAPIM              apim;
  private FedoraAPIA              apia;
  private Uploader                upld;
  private final FedoraBlobStore   bs;
  private Map<String, Blob> blobs = new HashMap<String, Blob>();
  private Map<String, FedoraState> states = new HashMap<String, FedoraState>();
  private ArrayList<FedoraState> ingested = new ArrayList<FedoraState>();

  /**
   * Creates a new FedoraConnection object.
   *
   * @param bs   the FedoraBlobStore that this connects to
   * @param sess the session this connection is attached to
   * @throws OtmException on an error
   */
  public FedoraConnection(FedoraBlobStore bs, Session sess) throws OtmException {
    super(sess);
    this.bs = bs;
    enlistResource(newXAResource());
  }

  /**
   * Get the underlying fedora blob-store.
   *
   * @return the blob-store, or null if this connection has been closed
   */
  FedoraBlobStore getBlobStore() {
    return bs;
  }

    /**
   * Gets the API-A stub used by this connection.
   *
   * @return the API-A stub
   */
  public FedoraAPIA getAPIA() {
    if (apia == null)
      apia = getBlobStore().createAPIA();

    return apia;
  }

  /**
   * Gets the API-M stub used by this connection.
   *
   * @return the API-M stub
   */
  public FedoraAPIM getAPIM() {
    if (apim == null)
      apim = getBlobStore().createAPIM();

    return apim;
  }

  /**
   * Gets the Fedora uploader used by this connection.
   *
   * @return the Fedora uploader
   */
  public Uploader getUploader() {
    if (upld == null)
      upld = getBlobStore().createUploader();

    return upld;
  }

  /**
   * Create a URL to access the given Datastream.
   *
   * @param pid the Fedora PID
   * @param dsId the Fedora dsId
   *
   * @return the URL
   *
   * @throws OtmException on an error
   */
  public URL getDatastreamLocation(String pid, String dsId)
                            throws OtmException {
    try {
      return getBlobStore().getFedoraBaseUri().resolve("get/" + pid + "/" + dsId).toURL();
    } catch (Exception e) {
      throw new OtmException("Failed to build a data-stream access URL", e);
    }
  }

  public Blob getBlob(ClassMetadata cm, String id, Object instance) throws OtmException {
    Blob blob = blobs.get(id);
    if (blob == null) {
      FedoraBlobStore bs = getBlobStore();
      FedoraBlobFactory bf   = bs.mostSpecificBlobFactory(id);

      if (bf == null)
        throw new OtmException("Can't find a blob factory for " + id);

      FedoraState o = new FedoraState(bf.createBlob(cm, id, instance, this));
      FedoraState fstate = states.get(o.getKey());
      if (fstate == null)
        states.put(o.getKey(), fstate = o);

      blob = newBlob(id, fstate);
      blobs.put(id, blob);
    }
    return blob;
  }


  /**
   * Ingest the contents into Fedora.
   *
   * @param blob the blob contents
   * @param ref the uploaded content reference
   *
   * @throws OtmException on an error
   */
  private boolean ingest(FedoraBlob blob, String ref) throws OtmException {
    String[]   newPid = new String[] { blob.getPid() };
    String[]   newDs  = new String[] { blob.getDsId() };

    try {
      INGEST_OP op = blob.getFirstIngestOp();
      int maxIter = 3;
      while (op != null && maxIter-- > 0) {
        switch (op) {
          case AddObj:
            op = addObject(blob, ref, newPid);
            break;

          case AddDs:
            op = addDatastream(blob, ref, newDs);
            break;

          case ModDs:
            op = modifyDatastream(blob, ref);
            break;

          default:
            throw new Error("Internal error: unexpected op " + op);
        }
      }

      if (op != null)
        throw new OtmException("Loop detected: failed to ingest " + blob + ", op=" + op);

    } catch (Exception e) {
      if (e instanceof OtmException)
        throw (OtmException) e;

      throw new OtmException("Write to Fedora failed", e);
    }

    if (!blob.getPid().equals(newPid[0]))
      throw new OtmException("PID mismatch in ingest. Expecting '" + blob.getPid() + "', got '" + newPid[0]
                             + "'");

    if (!blob.getDsId().equals(newDs[0]))
      throw new OtmException("DS-ID mismatch in add-DS. Expecting '" + blob.getDsId() + "', got '" + newDs[0]
                             + "'");

    if (log.isDebugEnabled())
      log.debug("Ingested " + blob);

    return true;
  }

  private INGEST_OP addObject(FedoraBlob blob, String ref, String[] newPid) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Ingesting " + blob);

      newPid[0] = apim.ingest(blob.getFoxml(ref), "foxml1.0", "created");
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("ingest failed: ", e);

      if (isObjectExistsException(e))
        new_op = INGEST_OP.AddDs;
      else
        throw e;
    }

    return new_op;
  }

  private INGEST_OP addDatastream(FedoraBlob blob, String ref, String[] newDs) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Adding data-stream '" + blob.getDsId() + "' for '" + blob.getPid() + "'");

      newDs[0] = apim.addDatastream(blob.getPid(), blob.getDsId(), new String[0], blob.getDatastreamLabel(), false,
                                    blob.getContentType(), null, ref, "M", "A", "created");
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("add datastream failed: ", e);

      if (isNoSuchObjectException(e))
        new_op = INGEST_OP.AddObj;
      else if (isDatastreamExistsException(e))
        new_op = INGEST_OP.ModDs;
      else
        throw e;
    }

    return new_op;
  }

  private INGEST_OP modifyDatastream(FedoraBlob blob, String ref) throws Exception {
    INGEST_OP new_op;

    try {
      if (log.isDebugEnabled())
        log.debug("Modifying data-stream(by reference) for " + blob);

      apim.modifyDatastreamByReference(blob.getPid(), blob.getDsId(), new String[0], blob.getDatastreamLabel(), false,
                                       blob.getContentType(), null, ref, "A", "updated", false);
      new_op = null;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("ds-modify failed: ", e);

      if (isNoSuchObjectException(e))
        new_op = INGEST_OP.AddObj;
      else if (isNoSuchDatastreamException(e))
        new_op = INGEST_OP.AddDs;
      else
        throw e;
    }

    return new_op;
   }


  /* WARNING: this is fedora version specific! */
  private static boolean isNoSuchObjectException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isNoSuchDatastreamException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("java.lang.Exception: Uncaught exception from Fedora Server");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isObjectExistsException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.ObjectExistsException");
  }

  /* WARNING: this is fedora version specific! */
  private static boolean isDatastreamExistsException(Exception e) {
    return e instanceof RemoteException &&
           e.getMessage().startsWith("fedora.server.errors.GeneralException: A datastream already exists");
  }

  /**
   * Gets the data-stream meta object from Fedora.
   *
   * @param blob the fedora blob
   *
   * @return the data-stream meta object
   *
   * @throws OtmException on an error
   */
  protected Datastream getDatastream(FedoraBlob blob) throws OtmException {
    try {
      return getAPIM().getDatastream(blob.getPid(), blob.getDsId(), null);
    } catch (Exception e) {
      if (isNoSuchObjectException(e) || isNoSuchDatastreamException(e))
        return null;

      throw new OtmException("Error while getting the data-stream for " + blob, e);
    }
  }

  /**
   * Checks if the Fedora object itself can be purged. In general an object can be purged if
   * the remaining data-streams on it are not significant. Override it in sub-classes to handle
   * app specific processing.
   *
   * @param blob the fedora blob
   *
   * @return true if it can be purged, false if only the datastream should be purged, or null
   *         if nothing should be done (e.g. because the object doesn't exist in the first place)
   *
   * @throws OtmException on an error
   */
  protected Boolean canPurgeObject(FedoraBlob blob) throws OtmException {
    try {
      if (blob.hasSingleDs())
        return true;
      return blob.canPurgeObject(getAPIM().getDatastreams(blob.getPid(), null, null));
    } catch (Exception e) {
      if (isNoSuchObjectException(e)) {
        if (log.isDebugEnabled())
          log.debug("Object with PID " + blob.getPid() + " does not exist in blob-store", e);
        return null;
      }

      throw new OtmException("Error in obtaining the list of data-streams on " + blob.getPid(), e);
    }
  }


  /**
   * Purge this Blob from Fedora.
   *
   * @param blob the blob to purge
   *
   * @throws OtmException on an error
   */
  private boolean purge(FedoraBlob blob) throws OtmException {
    FedoraAPIM apim = getAPIM();

    try {
      Boolean canPurgeObject = canPurgeObject(blob);
      if (canPurgeObject == null) {
        if (log.isDebugEnabled())
          log.debug("Not purging " + blob);
        return false;
      }

      if (canPurgeObject) {
        if (log.isDebugEnabled())
          log.debug("Purging Object " + blob.getPid());

        apim.purgeObject(blob.getPid(), "deleted", false);
      } else {
        if (log.isDebugEnabled())
          log.debug("Purging Datastream " + blob);

        apim.purgeDatastream(blob.getPid(), blob.getDsId(), null, "deleted", false);
      }

      return true;
    } catch (Exception e) {
      if (!isNoSuchObjectException(e))
        throw new OtmException("Purge failed", e);

      if (log.isDebugEnabled())
        log.debug("Datastream " + blob + " does not exist in blob-store", e);

      return false;
    }
  }

  /**
   * Gets the blob's InputStream from Fedora.
   *
   * @param blob the blob
   * @param original to get the original instead of the uploaded
   *
   * @return the InputStream or null
   *
   * @throws OtmException on an error
   */
  private InputStream getBlobStream(FedoraState fs, boolean original) throws OtmException {
    String ref = original ? null : fs.getUploadedRef();
    if (ref != null) {
      try {
        return getUploader().download(ref);
      } catch (IOException e) {
        throw new OtmException("Error while downloading from " + ref + " for " + fs);
      }
    }

    Datastream stream = getDatastream(fs);

    if (stream == null)
      return null;

    URL  location = getDatastreamLocation(fs.getPid(), fs.getDsId());

    try {
      return location.openStream();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Error while opening a stream to read from " + fs
                  + ". According to Fedora the stream must exist - but most likeley was"
                  + " purged recently. Treating this as if it was purged and does not exist.", e);

      return null;
    }
  }
  protected boolean doPrepare() throws XAException {
    try {
      if (log.isTraceEnabled())
        log.trace("doPrepare starting ...");
      int mods = 0;
      // Note: since the server is not txn based, no point doing any local locks.
      for (FedoraState fs : states.values()) {
        switch(fs.getState()) {
          case WRITTEN:
            if (log.isDebugEnabled())
              log.debug("Ingesting: " + fs);
            ingest(fs, fs.getUploadedRef());
            ingested.add(fs);
          case DELETED:
            mods++;
        }
      }
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

    if (log.isDebugEnabled() && !ingested.isEmpty())
      log.debug("Ingest committed for " + ingested.size() + " blobs");

    ingested.clear();

    for (FedoraState fs : states.values()) {
      switch(fs.getState()) {
        case DELETED:
          try {
            if (log.isDebugEnabled())
              log.debug("Purging: " + fs);
            purge(fs);
          } catch (Exception e) {
            log.error("Failed to purge: " + fs);
          }
          break;
      }
    }
  }

  protected void doRollback() throws XAException {
    if (log.isTraceEnabled())
      log.trace("doRollback");

    for (FedoraState fs : ingested) {
      try {
        String ref = fs.getBackupRef();
        if (ref != null)
          ingest(fs, ref);
        else
          purge(fs);
      } catch (Exception e) {
        log.error("Undo of ingest failed for: " + fs);
      }
    }

    close();
  }


  public void close() {
    if (log.isTraceEnabled())
      log.trace("close");
  }

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

  private Blob newBlob(String id, final FedoraState fstate) {
    return new AbstractBlob(id) {

      @Override
      protected InputStream doGetInputStream() throws OtmException {
        InputStream in =  getBlobStream(fstate, false);
        if (in == null)
          throw new OtmException(this +  " does not exist. Write to it first to create.");

        if (log.isTraceEnabled())
          log.trace("Creating inputstream for " + this);

        return in;
      }

      @Override
      protected OutputStream doGetOutputStream() throws OtmException {
        InputStream in = (fstate.getBackupRef() != null) ? null : getBlobStream(fstate, true);
        OutputStream out = null;
        try {
          if (in != null) {
            try {
              copy(in, out = getUploadStream(true));
            } catch (IOException e) {
              throw new OtmException("Failed to backup original content for " + this);
            }
          }
        } finally {
          closeAll(in, out);
        }

        create();

        if (log.isTraceEnabled())
          log.trace("Creating outputstream for " + this);

        return getUploadStream(false);
      }

      private OutputStream getUploadStream(final boolean backup) {
        final Uploader.UploadResult result = new Uploader.UploadResult();
        OutputStream out;
        try {
          out = getUploader().getOutputStream(result.getListener(), -1);
        } catch (IOException e) {
          throw new OtmException("Error while creating output stream for " + this, e);
        }

        return new FilterOutputStream(out) {
          @Override
          public void close() throws IOException {
            super.close();
            String ref = result.getResult();
            if (backup)
              fstate.setBackupRef(ref);
            else
              fstate.setUploadedRef(ref);
            if (log.isDebugEnabled()) {
              if (!backup)
                log.debug("Uploaded content for " + this + " into " + ref);
              else
                log.debug("Backed up original content for " + this + " into " + ref);
            }
          }

          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
          }
        };
      }

      @Override
      public void closed(Closeable stream) {
        if (log.isTraceEnabled())
          log.trace("Closing stream for " + this);
        super.closed(stream);
      }

      @Override
      public void writing(OutputStream out) {
        switch (fstate.getState()) {
          case CREATED:
          case NONE:
            if (log.isTraceEnabled())
              log.trace("Write detected on " + this);
            fstate.setState(ChangeState.WRITTEN);
            break;
          case DELETED:
            // All streams should be closed on delete.
            log.error("Writing to deleted Blob " + this);
        }
      }

      public boolean create() throws OtmException {
        switch (fstate.getState()) {
          case DELETED:
          case NONE:
            boolean ret = !exists();
            if (log.isTraceEnabled())
              log.trace("Create requested for " + this);
            fstate.setState(ChangeState.CREATED);
            fstate.setExists(Boolean.TRUE);
            return ret;
        }
        return false;
      }

      public boolean delete() throws OtmException {
        switch (fstate.getState()) {
          case CREATED:
          case NONE:
          case WRITTEN:
            for (Closeable stream : streams) {
              try {
                stream.close();
              } catch (IOException e) {
                log.warn("Failed to close a stream for " + this, e);
              }
            }
            streams.clear();
            boolean ret = exists();
            if (log.isTraceEnabled())
              log.trace("Delete requested for " + this);
            fstate.setState(ChangeState.DELETED);
            fstate.setExists(Boolean.FALSE);
            return ret;
        }
        return false;
      }

      public boolean exists() throws OtmException {
        if (fstate.getExists() != null)
          return fstate.getExists();

        if (log.isTraceEnabled())
          log.trace("performing exists() check on " + this);

        InputStream in = getBlobStream(fstate, true);
        fstate.setExists(in != null);
        try {
          if (in != null)
            in.close();
        } catch (IOException e) {
          if (log.isDebugEnabled())
            log.debug("Failed to close stream used in exists() check", e);
        }

        if (log.isTraceEnabled())
          log.trace("exists() check result: " + fstate.getExists());

        return fstate.getExists();
      }

      public ChangeState getChangeState() {
        return fstate.getState();
      }

      public ChangeState mark() {
        ChangeState prev = fstate.mark();
        if (log.isDebugEnabled())
          log.debug("Marking... previous-mark = " + prev
              + ", state = " + fstate.getState() + ", " + this);
        return prev;
      }

      public String toString() {
        return "BlobId = " + getId() + ", " + fstate;
      }
    };
  }

  public static class FedoraState implements FedoraBlob {
    private final FedoraBlob  fb;
    private Boolean     exists = null;
    private ChangeState state = ChangeState.NONE;
    private ChangeState marked = ChangeState.NONE;
    private String upld = null;
    private String bak = null;
    private final String key;

    public FedoraState(FedoraBlob fb) {
      this.fb = fb;
      key = "PID = " + getPid() + ", DS = " + getDsId();
    }

    public String getKey() {
      return key;
    }

    public String toString() {
      return key;
    }

    /**
     * @param upld the reference uri for the uploaded content
     */
    public void setUploadedRef(String upld) {
      this.upld = upld;
    }

    /**
     * @return the reference uri for the uploaded content
     */
    public String getUploadedRef() {
      return upld;
    }

    /**
     * @param bak the reference uri for the backed up content
     */
    public void setBackupRef(String bak) {
      this.bak = bak;
    }

    /**
     * @return the reference uri for the backed up content
     */
    public String getBackupRef() {
      return bak;
    }

    /**
     * @param val the new state value
     */
    public void setState(ChangeState val) {
      state = marked = val;
    }

    /**
     * @return the current state value
     */
    public ChangeState getState() {
      return state;
    }

    /**
     * Reset the state value to NONE and return the new state since the
     * previous call.
     *
     * @return the new state since the previous call
     */
    public ChangeState mark() {
      ChangeState prev = marked;
      marked = ChangeState.NONE;
      return prev;
    }

    /**
     * @param exists the exists flag to set
     */
    public void setExists(Boolean exists) {
      this.exists = exists;
    }

    /**
     * @return the exists flag
     */
    public Boolean getExists() {
      return exists;
    }

    public boolean canPurgeObject(Datastream[] ds) throws OtmException {
      return fb.canPurgeObject(ds);
    }

    public ClassMetadata getClassMetadata() {
      return fb.getClassMetadata();
    }

    public String getContentModel() {
      return fb.getContentModel();
    }

    public String getContentType() {
      return fb.getContentType();
    }

    public String getDatastreamLabel() {
      return fb.getDatastreamLabel();
    }

    public String getDsId() {
      return fb.getDsId();
    }

    public INGEST_OP getFirstIngestOp() {
      return fb.getFirstIngestOp();
    }

    public byte[] getFoxml(String ref) {
      return fb.getFoxml(ref);
    }

    public String getPid() {
      return fb.getPid();
    }

    public boolean hasSingleDs() {
      return fb.hasSingleDs();
    }
  }
}
