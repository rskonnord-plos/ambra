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
   * @param blob The blob instance
   * @param con the connection handle to Fedora
   *
   * @return the Blob object with the given
   *
   * @throws OtmException on an error
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id, Object blob, FedoraConnection con)
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
