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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.topazproject.fedora.client.APIAStubFactory;
import org.topazproject.fedora.client.APIMStubFactory;
import org.topazproject.fedora.client.FedoraAPIA;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.otm.AbstractStore;
import org.topazproject.otm.BlobStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * Uses Fedora as a BlobStore. The current version of fedora is not
 * transactional. Therefore the transactional aspects of this store
 * is only "best-effort" and may not behave well in a cluster. Use
 * it only in cases where the higher level app logic can work 
 * without having locking/exclusive-access to the blobs.
 *
 * @author Pradeep Krishnan
 */
public class FedoraBlobStore extends AbstractStore implements BlobStore {
  private final URI                                  fedoraBaseUri;
  private final String                               apimUri;
  private final String                               apiaUri;
  private final String                               uploadUri;
  private final String                               userName;
  private final String                               password;
  private final SortedMap<String, FedoraBlobFactory> blobFactories =
    new TreeMap<String, FedoraBlobFactory>(Collections.reverseOrder());

  /**
   * Creates a new FedoraBlobStore object.
   *
   * @param apimUri the API-M SOAP service URI
   * @param userName API-M userid
   * @param password API-M password
   *
   * @throws OtmException on an error
   */
  public FedoraBlobStore(String apimUri, String userName, String password)
                  throws OtmException {
    this.apimUri    = apimUri;
    this.userName   = userName;
    this.password   = password;

    URI uri;

    try {
      uri           = new URI(apimUri);
    } catch (URISyntaxException e) {
      throw new OtmException("Invalid fedora API-M uri", e);
    }

    String path = uri.getPath();

    if (!path.endsWith("/services/management"))
      throw new OtmException("Invalid fedora API-M uri" + uri);

    path = path.substring(0, path.length() - 19); // get rid of "services/management"

    try {
      fedoraBaseUri = new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), path, null, null);
    } catch (URISyntaxException use) {
      throw new Error(use); // Can't happen
    }

    uploadUri   = fedoraBaseUri.resolve("management/upload").toString();
    apiaUri     = fedoraBaseUri.resolve("services/access").toString();
  }

  /*
   * inherited javadoc
   */
  public Connection openConnection(Session sess, boolean readOnly) throws OtmException {
    return new FedoraConnection(this, sess);
  }

  /*
   * inherited javadoc
   */
  public void insert(ClassMetadata cm, String id, byte[] blob, Connection con)
              throws OtmException {
    FedoraConnection fc = (FedoraConnection) con;
    fc.insert(cm, id, blob);
  }

  /*
   * inherited javadoc
   */
  public void delete(ClassMetadata cm, String id, Connection con)
              throws OtmException {
    FedoraConnection fc = (FedoraConnection) con;
    fc.delete(cm, id);
  }

  /*
   * inherited javadoc
   */
  public byte[] get(ClassMetadata cm, String id, Connection con)
             throws OtmException {
    FedoraConnection fc = (FedoraConnection) con;

    return fc.get(cm, id);
  }

  /**
   * Adds a FedoraBlobFactory that can create blobs with the given uri prefix.
   *
   * @param bf the blob factory
   *
   * @throws OtmException on an error
   */
  public void addBlobFactory(FedoraBlobFactory bf) throws OtmException {
    for (String prefix : bf.getSupportedUriPrefixes())
      blobFactories.put(prefix, bf);
  }

  /**
   * Adds a FedoraBlobFactory that can create blobs with the given uri prefix.
   *
   * @param bf the blob factory
   *
   * @throws OtmException on an error
   */
  public void removeBlobFactory(FedoraBlobFactory bf) throws OtmException {
    for (String prefix : bf.getSupportedUriPrefixes())
      blobFactories.remove(prefix);
  }

  /**
   * Finds the blob factory that has the best match for the given uri prefix,
   *
   * @param id the blob id
   *
   * @return the blob factory
   */
  public FedoraBlobFactory mostSpecificBlobFactory(String id) {
    FedoraBlobFactory bf = blobFactories.get(id);

    if (bf != null)
      return bf;

    // reverse sorted map. So most specific first
    for (String prefix : blobFactories.keySet())
      if (id.startsWith(prefix))
        return blobFactories.get(prefix);

    return null;
  }

  /**
   * Converts the blob-id to a blob.
   *
   * @param cm The ClassMetadata of the blob
   * @param blobId The blob id
   *
   * @return the FedoraBlob object created by the 'most specific' factory
   *
   * @throws OtmException on an error
   */
  public FedoraBlob toBlob(ClassMetadata cm, String blobId)
                    throws OtmException {
    FedoraBlobFactory bf   = mostSpecificBlobFactory(blobId);

    if (bf == null)
      throw new OtmException("Can't find a blob factory for " + blobId);

    return bf.createBlob(cm, blobId);
  }

  /**
   * Generates a new blob id. (Typically assigned by Fedora).
   *
   * @param cm the ClassMetadata of the blob
   * @param prefix the prefix for the id
   * @param con the connection to use
   *
   * @return new blob-od gemerated by the most specific factory
   *
   * @throws OtmException on an error
   */
  public String generateId(ClassMetadata cm, String prefix, Connection con)
                    throws OtmException {
    FedoraBlobFactory bf   = mostSpecificBlobFactory(prefix);

    if (bf == null)
      throw new OtmException("Can't find a blob factory for " + prefix);

    return prefix + bf.generateId(cm, (FedoraConnection) con);
  }

  /**
   * Get fedoraBaseUri.
   *
   * @return fedoraBaseUri as URI.
   */
  URI getFedoraBaseUri() {
    return fedoraBaseUri;
  }

  /**
   * Creates a new API-A stub.
   *
   * @return the API-A stub
   *
   * @throws OtmException on an error
   */
  FedoraAPIA createAPIA() throws OtmException {
    try {
      return APIAStubFactory.create(apiaUri);
    } catch (Exception e) {
      throw new OtmException("Failed to create API-A stub", e);
    }
  }

  /**
   * Creates a new API-M stub
   *
   * @return the API-M stub
   *
   * @throws OtmException on an error
   */
  FedoraAPIM createAPIM() throws OtmException {
    try {
      return APIMStubFactory.create(apimUri, userName, password);
    } catch (Exception e) {
      throw new OtmException("Failed to create API-M stub", e);
    }
  }

  /**
   * Create a new uploader.
   *
   * @return the uploader
   *
   * @throws OtmException on an error
   */
  Uploader createUploader() throws OtmException {
    try {
      return new Uploader(uploadUri, userName, password);
    } catch (Exception e) {
      throw new OtmException("Failed to create a Fedora Uploader", e);
    }
  }
}
