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

import org.topazproject.fedora.client.Datastream;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;

/**
 * Represents a blob stored in Fedora (similar concept as java.io.File).
 *
 * @author Pradeep Krishnan
 */
public interface FedoraBlob {
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
   * Gets the datastream label to use in the FOXML. eg. "Annotation content".
   *
   * @return the label to use
   */
  public String getDatastreamLabel();

  /**
   * Gets the content model to use in the FOXML. eg. "Annotation".
   *
   * @return the content model
   */
  public String getContentModel();

  /**
   * Gets the contentType for use in the FOXML. eg. "application/octet-stream".
   *
   * @return the content type
   */
  public String getContentType();

  /**
   * Get the first operation to try when ingesting blob. Subclasses that know something of
   * their use can override this to reduce the number of calls made to fedora. E.g. if some class
   * never has it's contents updated and only ever has one datastream per object, then it would
   * return <var>AddObj</var> here.
   *
   * <p>By default this returns <var>AddDs</var>.
   *
   * @return the first operation to try
   */
  public INGEST_OP getFirstIngestOp();

  enum INGEST_OP { AddObj, AddDs, ModDs }

  /**
   * Generate the FOXML used to create a new Fedora Object with the Blob DataStream.
   *
   * @param ref the location where blob content is uploaded to (returned by Fedora on upload)
   *
   * @return the FOXML ready for ingesting
   */
  public byte[] getFoxml(String ref);

  /**
   * Allows sub-classes to indicate that there is a one-to-one correspondence
   * between blob-id and fedora-pid. This will govern how purges are done for example.
   *
   * @return true to assert that fedora-pid and blob-id has a one-to-one correspondence
   */
  public boolean hasSingleDs();

  /**
   * Checks if the data-streams on this object are significant enough so as not to purge
   * this. In general, if the only data-stream remaining is the "DC", "RELS-EXT", or current
   * datastream, then the whole object can be purged; otherwise only the datastream is purged.
   * Override it in sub-classes to handle app specific processing.
   *
   * @param ds the current list of data-streams
   *
   * @return true if it can be purged, false if only the datastream should be purged, or null
   *         if nothing should be done (e.g. because the object doesn't exist in the first place)
   *
   * @throws OtmException on an error
   */
  public boolean canPurgeObject(Datastream[] ds) throws OtmException;

}

