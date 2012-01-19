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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * Represents a blob stored in Fedora (similar concept as java.io.File).
 *
 * @author Pradeep Krishnan
 */
public interface FedoraBlob {
  /**
   * Gets the blob identifier.
   *
   * @return the blob identifier URI.
   */
  public String getBlobId();

  /**
   * Gets the Fedora PID of this Blob.
   *
   * @return the blob PID
   */
  public String getPid();

  /**
   * Gets the Fedora Datastream id of this Blob.
   *
   * @return the blob Datastream id
   */
  public String getDsId();

  /**
   * Gets the blob class metadata.
   *
   * @return the ClassMetadata.
   */
  public ClassMetadata getClassMetadata();

  /**
   * Ingest the contents into Fedora.
   *
   * @param blob the blob contents
   * @param con the Fedora connection handle
   *
   * @throws OtmException on an error
   */
  public void ingest(byte[] blob, FedoraConnection con)
              throws OtmException;

  /**
   * Purge this Blob from Fedora.
   *
   * @param con the Fedora connection handle
   *
   * @throws OtmException on an error
   */
  public void purge(FedoraConnection con) throws OtmException;

  /**
   * Gets the blob content (if it exists) from Fedora.
   *
   * @param con the Fedora connection handle
   *
   * @return the blob content or null if the blob does not exist
   *
   * @throws OtmException on an error
   */
  public byte[] get(FedoraConnection con) throws OtmException;
}
