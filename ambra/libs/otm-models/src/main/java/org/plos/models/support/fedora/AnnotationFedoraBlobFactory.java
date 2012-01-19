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
package org.plos.models.support.fedora;

import org.topazproject.fedora.otm.DefaultFedoraBlobFactory;
import org.topazproject.fedora.otm.FedoraBlob;
import org.topazproject.fedora.otm.FedoraConnection;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * A factory to create fedora blobs for Annotation and Reply.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationFedoraBlobFactory extends DefaultFedoraBlobFactory {
  /*
   * inherited javadoc
   */
  public String[] getSupportedUriPrefixes() {
    return new String[] { "info:fedora/" };
  }

  /*
   * inherited javadoc
   */
  public FedoraBlob createBlob(ClassMetadata cm, String id, Object instance, FedoraConnection con)
                        throws OtmException {
    return new AnnotationFedoraBlob(cm, id, getPid(cm, id, instance, con), getDsId(cm, id, instance, con));
  }

  /*
   * inherited javadoc
   */
  protected String getPid(ClassMetadata cm, String id, Object instance, FedoraConnection con) {
    return super.getPid(cm, id, instance, con);
  }

  /*
   * inherited javadoc
   */
  protected String getDsId(ClassMetadata cm, String id, Object instance, FedoraConnection con) {
    return "BODY";
  }

  /*
   * inherited javadoc
   */
  protected String getPidNs(ClassMetadata cm, FedoraConnection con) {
    return super.getPidNs(cm, con);
  }
}
