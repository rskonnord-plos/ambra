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

import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;

import org.topazproject.fedora.client.Datastream;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;

/**
 * A FedoraBlob implementation where blobs are stored as data-streams associated with an object.
 *
 * @author Pradeep Krishnan
 */
public class DefaultFedoraBlob implements FedoraBlob {
  private static final String FOXML  =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<foxml:digitalObject PID=\"${PID}\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\">"
    + "<foxml:objectProperties>"
    + "<foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"${LABEL}\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"${CONTENTMODEL}\"/>"
    + "</foxml:objectProperties>"
    + "<foxml:datastream CONTROL_GROUP=\"M\" ID=\"${DS}\" STATE=\"A\">"
    + "<foxml:datastreamVersion ID=\"${DS}1.0\" MIMETYPE=\"${CONTENTTYPE}\" LABEL=\"${LABEL}\">"
    + "<foxml:contentLocation REF=\"${CONTENT}\" TYPE=\"URL\"/></foxml:datastreamVersion>"
    + "</foxml:datastream></foxml:digitalObject>";
  private final ClassMetadata    cm;
  private final String           pid;
  private final String           dsId;

  /**
   * Creates a new DefaultFedoraBlob object.
   * @param cm the class metadata of this blob
   * @param pid the Fedora PID of this blob
   * @param dsId the Datastream id of this blob
   */
  public DefaultFedoraBlob(ClassMetadata cm, String pid, String dsId) {
    this.cm                          = cm;
    this.pid                         = pid;
    this.dsId                        = dsId;
  }

  public final ClassMetadata getClassMetadata() {
    return cm;
  }

  public final String getPid() {
    return pid;
  }

  public final String getDsId() {
    return dsId;
  }

  public INGEST_OP getFirstIngestOp() {
    return INGEST_OP.AddDs;
  }

  public String getContentType() {
    return "application/octet-stream";
  }

  public String getContentModel() {
    return "Blob";
  }

  public String getDatastreamLabel() {
    return "Blob content";
  }

  public byte[] getFoxml(String ref) {
    Map<String, String> values = new HashMap<String, String>();
    values.put("CONTENTTYPE", xmlAttrEscape(getContentType()));
    values.put("CONTENT", xmlAttrEscape(ref));
    values.put("CONTENTMODEL", xmlAttrEscape(getContentModel()));
    values.put("LABEL", xmlAttrEscape(getDatastreamLabel()));
    values.put("PID", xmlAttrEscape(getPid()));
    values.put("DS", xmlAttrEscape(getDsId()));

    String foxml = RdfUtil.bindValues(FOXML, values);

    try {
      return foxml.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }

  /**
   * Escape the values used as XML attributes.
   *
   * @param val the value to escape
   *
   * @return the escaped value
   */
  protected static final String xmlAttrEscape(String val) {
    /* AttValue ::= '"' ([^<&"] | Reference)* '"'
     *              |  "'" ([^<&'] | Reference)* "'"
     */
    return val.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;")
               .replaceAll("'", "&apos;");
  }

  public boolean hasSingleDs() {
    return false;
  }

  public boolean canPurgeObject(Datastream[] ds) throws OtmException {
    if (ds == null)
      return true;

    for (Datastream d : ds) {
      if (!d.getID().equals("DC") && !d.getID().equals("RELS-EXT") && !d.getID().equals(dsId))
        return false;
    }

    return true;
  }
}
