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
import org.topazproject.otm.Session;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * An id generator that allocates ids from Fedora.
 *
 * @author Pradeep Krishnan
 */
public class FedoraIdGenerator implements IdentifierGenerator {
  private String prefix;

  /*
   * inherited javadoc
   */
  public String generate(ClassMetadata cm, Session sess) throws OtmException {
    if (!(sess.getBlobStoreCon() instanceof FedoraConnection))
      throw new OtmException(getClass().getName() + " requires a connection using "
          + FedoraConnection.class);
    FedoraConnection con = (FedoraConnection) sess.getBlobStoreCon();
    return con.getBlobStore().generateId(cm, prefix, con);
  }

  /*
   * inherited javadoc
   */
  public void setUriPrefix(String prefix) {
    this.prefix = prefix;
  }
}
