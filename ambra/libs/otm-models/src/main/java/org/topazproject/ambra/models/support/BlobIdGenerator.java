/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.models.support;

import org.topazproject.fedora.otm.FedoraBlobStore;
import org.topazproject.fedora.otm.FedoraIdGenerator;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.id.GUIDGenerator;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * An id generator that uses fedora's id generator if we are using fedora as the blob-store.
 * Otherwise switches to the normal GUIDGenerator.
 *
 * @author Pradeep Krishnan
 */
public class BlobIdGenerator implements IdentifierGenerator {
  private final FedoraIdGenerator fedoraIdGen = new FedoraIdGenerator();
  private final GUIDGenerator     guidGen     = new GUIDGenerator();

  /*
   * inherited javadoc
   */
  public String generate(ClassMetadata cm, Session sess)
                  throws OtmException {
    if (sess.getSessionFactory().getBlobStore() instanceof FedoraBlobStore)
      return fedoraIdGen.generate(cm, sess);

    return guidGen.generate(cm, sess);
  }

  /*
   * inherited javadoc
   */
  public void setUriPrefix(String uriPrefix) {
    fedoraIdGen.setUriPrefix(uriPrefix);
    guidGen.setUriPrefix(uriPrefix);
  }
}
