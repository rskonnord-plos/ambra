/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.NonNegativeInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.service.WSTopazContext;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.fedora.client.FedoraAPIM;

import org.topazproject.mulgara.itql.ItqlHelper;

/**
 * A helper class to interface with Fedora.
 */
public class FedoraHelper {
  private static final Log log = LogFactory.getLog(FedoraHelper.class);

  //
  private static final String FOXML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<foxml:digitalObject xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\">"
    + "<foxml:objectProperties>"
    + "<foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"${LABEL}\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"${CONTENTMODEL}\"/>"
    + "</foxml:objectProperties>"
    + "<foxml:datastream CONTROL_GROUP=\"M\" ID=\"BODY\" STATE=\"A\">"
    + "<foxml:datastreamVersion ID=\"BODY1.0\" MIMETYPE=\"${CONTENTTYPE}\" LABEL=\"${LABEL}\">"
    + "<foxml:contentLocation REF=\"${CONTENT}\" TYPE=\"URL\"/></foxml:datastreamVersion>"
    + "</foxml:datastream></foxml:digitalObject>";

  //
  private final WSTopazContext ctx = new WSTopazContext(FedoraHelper.class.getName());


  /**
   * Creates a body URI by uploading the content to fedora.
   *
   * @param contentType the contentType
   * @param content the content
   * @param contentModel the fedora content model
   * @param label the label for the content
   *
   * @return the body URI
   *
   * @throws RemoteException if an error in upload
   * @throws Error on some other error
   */
  public String createBody(String contentType, byte[] content, String contentModel, String label)
                    throws RemoteException {
    try {
      String ref = ctx.getFedoraUploader().upload(content);

      Map    values = new HashMap();
      values.put("CONTENTTYPE", xmlAttrEscape(contentType));
      values.put("CONTENT", xmlAttrEscape(ref));
      values.put("CONTENTMODEL", xmlAttrEscape(contentModel));
      values.put("LABEL", xmlAttrEscape(label));

      String foxml = ItqlHelper.bindValues(FOXML, values);

      return pid2URI(ctx.getFedoraAPIM().ingest(foxml.getBytes("UTF-8"), "foxml1.0", "created"));
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    } catch (IOException e) {
      throw new RemoteException("Upload failed", e);
    }
  }

  private static final String xmlAttrEscape(String val) {
    /* AttValue ::= '"' ([^<&"] | Reference)* '"'
     *              |  "'" ([^<&'] | Reference)* "'"
     */
    return val.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
  }

  /**
   * Converts a fedora PID to a fedora URI.
   *
   * @param pid the pid to convert
   *
   * @return Returns the fedora pid
   */
  public static String pid2URI(String pid) {
    return "info:fedora/" + pid;
  }

  /**
   * Converts a fedora URI to a fedora PID
   *
   * @param uri the fedora URI
   *
   * @return Returns the fedora PID
   */
  public static String uri2PID(String uri) {
    return uri.substring(12);
  }

  /**
   * Converts a fedora URI to a fedora REST URL.
   *
   * @param uri the fedora URI
   *
   * @return Returns the REST API-A URL for this URI
   */
  public String getBodyURL(String uri) {
    if (!uri.startsWith("info:fedora"))
      return uri;

    String path = "/fedora/get/" + uri2PID(uri) + "/BODY";

    return ctx.getFedoraBaseUri().resolve(path).toString();
  }

  /**
   * Get the next unique PID to use.
   *
   * @param pidNs the namespace to allocate from
   *
   * @return Returns the next id.
   *
   * @throws RemoteException on an erro
   */
  public String getNextId(String pidNs) throws RemoteException {
    // xxx: cache a bunch of ids
    return ctx.getFedoraAPIM().getNextPID(new NonNegativeInteger("1"), pidNs)[0];
  }

  /**
   * Purge a list of objects.
   *
   * @param purgeList the list of PIDs to purge
   */
  public void purgeObjects(String[] purgeList) {
    try {
      ctx.activate();
      FedoraAPIM apim = ctx.getFedoraAPIM();

      for (int i = 0; i < purgeList.length; i++) {
        if (log.isDebugEnabled())
          log.debug("purging " + purgeList[i]);

        // xxx: error says "fedora does not support forced removal yet".  
        // But don't suppose this really is a forced removal. So try with false. 
        //apim.purgeObject(purgeList[i], "deleted", true);
        apim.purgeObject(purgeList[i], "deleted", false);
      }

      if (log.isDebugEnabled())
        log.debug("purged " + purgeList.length + " fedora objects");
    } catch (Throwable t) {
      // No point reporting this back to the caller since the Annotation is deleted.
      // Admin needs to manually purge these later
      log.error("Failed to purge one or more fedora pids in " + Arrays.asList(purgeList), t);
    } finally {
      ctx.passivate();
    }
  }

  public TopazContext getContext() {
    return ctx;
  }
}
