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
package org.topazproject.ambra.models.support.fedora;

import org.topazproject.fedora.otm.DefaultFedoraBlob;

import org.topazproject.otm.ClassMetadata;

/**
 * A FedoraBlob implementation for Representation.
 *
 * @author Pradeep Krishnan
 */
public class RepresentationFedoraBlob extends DefaultFedoraBlob {
  private String           contentType;
  private String           contentModel;

  /**
   * Creates a new RepresentaionFedoraBlob object.
   *
   * @param cm the class metadata of this blob
   * @param pid the Fedora PID of this blob
   * @param dsId the Datastream id of this blob
   * @param ct the content type
   * @param cModel content model (in Fedora's domain)
   */
  public RepresentationFedoraBlob(ClassMetadata cm, String pid, String dsId, String ct, String cModel) {
    super(cm, pid, dsId);

    this.contentType  = ct;
    this.contentModel = cModel;
  }

  /**
   * Gets the contentType for use in the FOXML.
   *
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Gets the content model to use in the FOXML.
   *
   * @return the content model
   */
  public String getContentModel() {
    return contentModel;
  }

  /**
   * Gets the datastream label to use in the FOXML.
   *
   * @return the label to use
   */
  public String getDatastreamLabel() {
    return getDsId() + " Representation";
  }
}
