/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.otm;

import java.net.URL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.otm.AbstractConnection;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * Acts as the client to Fedora and manages the transactional aspects of user operations.
 *
 * @author Pradeep Krishnan
 */
public class FedoraConnection extends AbstractConnection {
  private static final Log        log        = LogFactory.getLog(FedoraConnection.class);
  private static final Map<Session, FedoraConnection> conMap =
                    Collections.synchronizedMap(new WeakHashMap<Session, FedoraConnection>());

  private FedoraBlobStore         bs;
  private Map<String, FedoraBlob> undoI;
  private Map<String, FedoraBlob> undoD;
  private Map<String, FedoraBlob> insertMap  = new HashMap<String, FedoraBlob>();
  private Map<String, FedoraBlob> deleteMap  = new HashMap<String, FedoraBlob>();
  private Map<String, byte[]>     contentMap = new HashMap<String, byte[]>();
  private FedoraAPIM              apim;
  private FedoraAPIA              apia;
  private Uploader                upld;

  /** 
   * Get the fedora-connection for the given session. 
   * 
   * @param sess the session for which to look up the connection
   * @return the fedora-connection, or null if none is active
   */
  static FedoraConnection getCon(Session sess) {
    return conMap.get(sess);
  }

  /**
   * Creates a new FedoraConnection object.
   *
   * @param bs   the FedoraBlobStore that this connects to
   * @param sess the session this connection is attached to
   */
  public FedoraConnection(FedoraBlobStore bs, Session sess) throws OtmException {
    super(sess);
    this.bs = bs;
    conMap.put(sess, this);
    enlistResource(new FedoraXAResource());
  }

  /** 
   * Get the underlying fedora blob-store. 
   * 
   * @return the blob-store, or null if this connection has been closed
   */
  FedoraBlobStore getBlobStore() {
    return bs;
  }

  /*
   * inherited javadoc
   */
  public void insert(ClassMetadata cm, String id, byte[] blob)
              throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    insertMap.put(id, bs.toBlob(cm, id));
    deleteMap.remove(id);
    contentMap.put(id, blob);
  }

  /*
   * inherited javadoc
   */
  public void delete(ClassMetadata cm, String id) throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    deleteMap.put(id, bs.toBlob(cm, id));
    insertMap.remove(id);
  }

  /*
   * inherited javadoc
   */
  public byte[] get(ClassMetadata cm, String id) throws OtmException {
    if (bs == null)
      throw new OtmException("Attempt to use a connection that is closed");

    if (deleteMap.get(id) != null)
      return null;

    byte[] blob = contentMap.get(id);

    if (blob == null) {
      blob = bs.toBlob(cm, id).get(this);

      if (blob != null)
        contentMap.put(id, blob);
    }

    return blob;
  }

  /**
   * Gets the API-A stub used by this connection.
   *
   * @return the API-A stub
   */
  public FedoraAPIA getAPIA() {
    if (apia == null)
      apia = bs.createAPIA();

    return apia;
  }

  /**
   * Gets the API-M stub used by this connection.
   *
   * @return the API-M stub
   */
  public FedoraAPIM getAPIM() {
    if (apim == null)
      apim = bs.createAPIM();

    return apim;
  }

  /**
   * Gets the Fedora uploader used by this connection.
   *
   * @return the Fedora uploader
   */
  public Uploader getUploader() {
    if (upld == null)
      upld = bs.createUploader();

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
      return bs.getFedoraBaseUri().resolve("get/" + pid + "/" + dsId).toURL();
    } catch (Exception e) {
      throw new OtmException("Failed to build a data-stream access URL", e);
    }
  }

  /**
   * A smple xa-resource wrapper around this connection's transaction operations. There's no
   * recovery support, and this assumes only a single tx per instance.
   */
  private class FedoraXAResource implements XAResource {
    public void start(Xid xid, int flags) { }
    public void end(Xid xid, int flags)   { }

    public int prepare(Xid xid) throws XAException {
      // assumption: doPrepare only throws RMERR, which means this resource stays in S2
      if (FedoraConnection.this.doPrepare()) {
        return XA_OK;
      } else {
        FedoraConnection.this.close();
        return XA_RDONLY;
      }
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
      if (onePhase) {
        try {
          FedoraConnection.this.doPrepare();
        } catch (XAException xae) {
          FedoraConnection.this.doRollback();
          throw xae;
        }
      }

      FedoraConnection.this.doCommit();
    }

    public void rollback(Xid xid) {
      FedoraConnection.this.doRollback();
    }

    public Xid[]   recover(int flag)                 { return new Xid[0]; }
    public void    forget(Xid xid)                   { }
    public boolean isSameRM(XAResource xares)        { return xares == this; }
    public int     getTransactionTimeout()           { return 0; }
    public boolean setTransactionTimeout(int secs)   { return false; }
  }

  private boolean doPrepare() throws XAException {
    boolean haveMods = !insertMap.isEmpty() || !deleteMap.isEmpty();
    undoI = new HashMap<String, FedoraBlob>();
    try {
      ingest(insertMap, undoI);
    } catch (OtmException oe) {
      log.error("Error preparing", oe);
      throw new XAException(XAException.XAER_RMERR);
    }
    insertMap = null;
    return haveMods;
  }

  private void doCommit() throws XAException {
    try {
      undoD = new HashMap<String, FedoraBlob>();
      purge(deleteMap, undoD);
      undoD   = null;
      undoI   = null;
    } catch (OtmException oe) {
      log.error("Error committing", oe);
      throw new XAException(XAException.XAER_RMERR);
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
      if (undoD.size() > 0)
        log.warn("Undoing purges for " + undoD.keySet());

      ingest(undoD);
      undoD = null;
    }

    // undo inserts
    if (undoI != null) {
      if (undoI.size() > 0)
        log.warn("Undoing ingests for " + undoI.keySet());

      purge(undoI);
      undoI = null;
    }

    insertMap    = null;
    contentMap   = null;
    deleteMap    = null;
    bs           = null;
    conMap.values().remove(this);
  }

  private Map<String, FedoraBlob> ingest(Map<String, FedoraBlob> map, Map<String, FedoraBlob> undo)
                                  throws OtmException {
    OtmException error = null;

    for (String id : map.keySet()) {
      try {
        FedoraBlob blob = map.get(id);
        blob.ingest(contentMap.get(id), this);

        if (undo != null)
          undo.put(id, blob);
      } catch (Throwable t) {
        OtmException e;

        if (t instanceof OtmException)
          e = (OtmException) t;
        else
          e = new OtmException("Failed to ingest blob for " + id, t);

        if (undo != null)
          throw e;
        else
          log.warn("Failed to ingest blob for " + id, t);

        if (error == null)
          error = e;
      }
    }

    if (error != null)
      throw error;

    return undo;
  }

  private Map<String, FedoraBlob> purge(Map<String, FedoraBlob> map, Map<String, FedoraBlob> undo)
                                 throws OtmException {
    OtmException error = null;

    for (String id : map.keySet()) {
      try {
        FedoraBlob blob = map.get(id);
        blob.purge(this);

        if (undo != null)
          undo.put(id, blob);
      } catch (Throwable t) {
        OtmException e;

        if (t instanceof OtmException)
          e = (OtmException) t;
        else
          e = new OtmException("Failed to purge blob for " + id, t);

        if (undo != null)
          throw e;
        else
          log.warn("Failed to purge blob for " + id, t);

        if (error == null)
          error = e;
      }
    }

    if (error != null)
      throw error;

    return undo;
  }

  private void ingest(Map<String, FedoraBlob> map) {
    try {
      ingest(map, null);
    } catch (Throwable t) {
      log.error("Error undoing deletes - fedora-store will be in an inconsistent state", t);
    }
  }

  private void purge(Map<String, FedoraBlob> map) {
    try {
      purge(map, null);
    } catch (Throwable t) {
      log.error("Error undoing inserts - fedora-store will be in an inconsistent state", t);
    }
  }
}
