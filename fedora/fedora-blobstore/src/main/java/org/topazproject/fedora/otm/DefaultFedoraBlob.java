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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.fedora.client.Datastream;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedora.client.Uploader;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;

/**
 * A FedoraBlob implementation. The default implementation assumes a one-to-one correspondence
 * between a PID and blobId.
 *
 * @author Pradeep Krishnan
 */
public class DefaultFedoraBlob implements FedoraBlob {
  private static final Log    log    = LogFactory.getLog(DefaultFedoraBlob.class);
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
  private final ClassMetadata cm;
  private final String        blobId;
  private final String        pid;
  private final String        dsId;

  /**
   * Creates a new DefaultFedoraBlob object.
   *
   * @param cm the class metadata of this blob
   * @param blobId the blob identifier URI
   * @param pid the Fedora PID of this blob
   * @param dsId the Datastream id of this blob
   */
  public DefaultFedoraBlob(ClassMetadata cm, String blobId, String pid, String dsId) {
    this.cm                          = cm;
    this.blobId                      = blobId;
    this.pid                         = pid;
    this.dsId                        = dsId;
  }

  /*
   * inherited javadoc
   */
  public final String getBlobId() {
    return blobId;
  }

  /*
   * inherited javadoc
   */
  public final ClassMetadata getClassMetadata() {
    return cm;
  }

  /*
   * inherited javadoc
   */
  public final String getPid() {
    return pid;
  }

  /*
   * inherited javadoc
   */
  public final String getDsId() {
    return dsId;
  }

  /*
   * inherited javadoc
   */
  public void ingest(byte[] blob, FedoraConnection con)
              throws OtmException {
    Uploader   upld   = con.getUploader();
    FedoraAPIM apim   = con.getAPIM();
    String     newPid = pid;

    try {
      String ref = upld.upload(blob);

      if (exists(con))
        apim.modifyDatastreamByReference(pid, dsId, null, getDatastreamLabel(), false,
                                         getContentType(), null, ref, "A", "updated", false);
      else
        newPid = apim.ingest(getFoxml(ref), "foxml1.0", "created");
    } catch (Exception e) {
      throw new OtmException("Ingest failed", e);
    }

    if (!pid.equals(newPid))
      throw new OtmException("PID mismatch in ingest. Expecting '" + pid + "', got '" + newPid
                             + "'");

    if (log.isDebugEnabled())
      log.debug("Ingested " + blobId + " as " + pid + "/" + dsId);
  }

  /**
   * Generate the FOXML used to create a new Fedora Object with the Blob DataStream.
   *
   * @param ref the location where blob content is uploaded to (returned by Fedora on upload)
   *
   * @return the FOXML ready for ingesting
   */
  protected byte[] getFoxml(String ref) {
    Map values = new HashMap();
    values.put("CONTENTTYPE", xmlAttrEscape(getContentType()));
    values.put("CONTENT", xmlAttrEscape(ref));
    values.put("CONTENTMODEL", xmlAttrEscape(getContentModel()));
    values.put("LABEL", xmlAttrEscape(getDatastreamLabel()));
    values.put("PID", xmlAttrEscape(pid));
    values.put("DS", xmlAttrEscape(dsId));

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

  /**
   * Checks the existence of this blob in Fedora.
   *
   * @param con the Fedora APIM stub to use
   *
   * @return true if it exists, false otherwise
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected boolean exists(FedoraConnection con) throws OtmException {
    if (getDatastream(con) == null)
      return false;

    // XXX: Bug in Fedora. Check if the object really exists
    URL location = con.getDatastreamLocation(pid, dsId);

    try {
      location.openStream().close();

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the data-stream meta object from Fedora.
   *
   * @param con the Fedora APIM stub to use
   *
   * @return the data-stream meta object
   *
   * @throws OtmException on an error
   */
  protected Datastream getDatastream(FedoraConnection con)
                              throws OtmException {
    try {
      return con.getAPIM().getDatastream(pid, dsId, null);
    } catch (Exception e) {
      if ((e.getMessage().indexOf("Server.userException") < 0)
           && (e.getMessage().indexOf("no path in db registry for") < 0))
        throw new OtmException("Error while getting the data-stream to " + pid + "/" + dsId, e);

      return null;
    }
  }

  /*
   * inherited javadoc
   */
  public void purge(FedoraConnection con) throws OtmException {
    FedoraAPIM apim = con.getAPIM();

    try {
      if (exists(con)) {
        apim.purgeObject(pid, "deleted", false);

        if (log.isDebugEnabled())
          log.debug("Purged " + blobId + " at " + pid + "/" + dsId);
      }
    } catch (Exception e) {
      throw new OtmException("Purge failed", e);
    }
  }

  /*
   * inherited javadoc
   */
  public byte[] get(FedoraConnection con) throws OtmException {
    Datastream stream = getDatastream(con);

    if (stream == null)
      return null;

    InputStream in       = null;
    URL         location = null;

    try {
      location   = con.getDatastreamLocation(pid, dsId);
      in         = location.openStream();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Error while opening a stream to read from " + pid + "/" + dsId
                  + ". According to Fedora the stream must exist - but most likeley was"
                  + " purged recently. Treating this as if it was purged and does not exist.", e);

      return null;
    }

    try {
      byte[] buf;

      if (stream.getSize() != 0) {
        buf   = new byte[(int) stream.getSize()];
        in    = new BufferedInputStream(in);
        in.read(buf);
      } else {
        // XXX: Seems like size==0 should be interpreted as unknown size
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        buf = new byte[4096];

        int c;

        while ((c = in.read(buf)) >= 0)
          out.write(buf, 0, c);

        buf = out.toByteArray();
      }

      if (log.isDebugEnabled())
        log.debug("Got " + buf.length + " bytes from " + location);

      return buf;
    } catch (Exception e) {
      throw new OtmException("Get failed on " + blobId, e);
    } finally {
      try {
        if (in != null)
          in.close();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to close connection to " + location, t);
      }
    }
  }

  /**
   * Gets the contentType for use in the FOXML. Defaults to "application/octet-stream".
   *
   * @return the content type
   */
  protected String getContentType() {
    return "application/octet-stream";
  }

  /**
   * Gets the content model to use in the FOXML. Defaults to "Blob".
   *
   * @return the content model
   */
  protected String getContentModel() {
    return "Blob";
  }

  /**
   * Gets the datastream label to use in the FOXML. Defaults to "Blob content".
   *
   * @return the label to use
   */
  protected String getDatastreamLabel() {
    return "Blob content";
  }
}
