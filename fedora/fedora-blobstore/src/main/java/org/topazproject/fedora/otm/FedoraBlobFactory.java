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
 * A factory to create fedora blobs.
 *
 * @author Pradeep Krishnan
 */
public interface FedoraBlobFactory {
  /**
   * Gets the blob uri prefixes this factory knows about. The list is consulted by the {@link
   * FedoraBlobStore#addBlobFactory} and is not expected to change until after an ivocation of
   * {@link FedoraBlobStore#removeBlobFactory}.
   *
   * @return array of uri prefixes
   */
  public String[] getSupportedUriPrefixes();

  /**
   * Creates a Blob object with the given ClassMetadata and id. Implementations may cache and
   * re-use FedoraBlob instances.
   *
   * @param cm The ClassMetadata of the blob
   * @param id The blod identifier URI
   *
   * @return the Blob object with the given
   *
   * @throws OtmException on an error
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id)
                        throws OtmException;

  /**
   * Generates a blob id for the given ClassMetadata
   *
   * @param cm The ClassMetadata of the blob
   * @param con Connection handle to Fedora.
   *
   * @return a newly generated id
   *
   * @throws OtmException on an error
   */
  public String generateId(ClassMetadata cm, FedoraConnection con)
                    throws OtmException;
}
