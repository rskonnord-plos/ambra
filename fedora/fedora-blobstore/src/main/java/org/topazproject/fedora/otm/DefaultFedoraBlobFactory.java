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

import org.apache.axis.types.NonNegativeInteger;

import org.topazproject.fedora.client.RepositoryInfo;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * A default factory to create fedora blobs. The default implementation handles "info:fedora/"
 * URI's of the form "info:fedora/PID" where PID is the Fedora PID of the blob. Also it uses the
 * Datastream id of "BODY" and the default PID namespace configured on the Fedora server.
 *
 * @author Pradeep Krishnan
 */
public class DefaultFedoraBlobFactory implements FedoraBlobFactory {
  private String pidNs;

  /*
   * inherited javadoc
   */
  public String[] getSupportedUriPrefixes() {
    return new String[] { "info:fedora/" };
  }

  /*
   * inherited javadoc
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id, Object blob, FedoraConnection con)
                        throws OtmException {
    return new DefaultFedoraBlob(cm, id, getPid(cm, id, blob, con), getDsId(cm, id, blob, con));
  }

  /**
   * Get a blob's PID given its ClassMetadata and id. The default implementation assumes that
   * the blobId contains the PID (ie. blobId endsWith PID).
   *
   * @param cm the ClassMetadata of the blob
   * @param id the id for the blob
   * @param blob the blob instance
   * @param con the connection handle to Fedora
   *
   * @return the Fedora PID
   */
  protected String getPid(ClassMetadata cm, String id, Object blob, FedoraConnection con) {
    return id.substring(id.lastIndexOf('/') + 1);
  }

  /**
   * Get a blob's Datastream id given its ClassMetadata and id. The default implementation
   * assumes a fixed datastream id of 'BODY'.
   *
   * @param cm the ClassMetadata of the blob
   * @param id the id for the blob
   * @param blob the blob instance
   * @param con the connection handle to Fedora
   *
   * @return the Datastream Id
   */
  protected String getDsId(ClassMetadata cm, String id, Object blob, FedoraConnection con) {
    return "BODY";
  }

  /*
   * inherited javadoc
   */
  public String generateId(ClassMetadata cm, FedoraConnection con)
                    throws OtmException {
    String ns = getPidNs(cm, con);

    try {
      return con.getAPIM().getNextPID(new NonNegativeInteger("1"), ns)[0];
    } catch (Exception e) {
      throw new OtmException("Id generation failed", e);
    }
  }

  /**
   * Gets the PID namespace for the given ClassMetadata. The default is to use the default
   * namespace configured on the Fedora server.
   *
   * @param cm the ClassMetadata of the blob
   * @param con the Fedora connection handle
   *
   * @return the PID namespace
   *
   * @throws OtmException on an error
   */
  protected String getPidNs(ClassMetadata cm, FedoraConnection con)
                     throws OtmException {
    try {
      if (pidNs == null) {
        RepositoryInfo ri = con.getAPIA().describeRepository();
        pidNs = ri.getRepositoryPIDNamespace();
      }

      return pidNs;
    } catch (Exception e) {
      throw new OtmException("Failed to determine the default PID namespace from Fedora", e);
    }
  }
}
