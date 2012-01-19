/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.util.ArticleUtil;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.models.ObjectInfo;

/**
 * This holds the information returned about a representation of an object.
 *
 * @author Jeff Suttor
 */
public class RepresentationInfo {
  /** The name of the representation. */
  private String name;
  /** The mime-type of the representation content. */
  private String contentType;
  /** The size, in bytes, of the representation content. */
  private long   size;
  /** The URL at which the representation can be retrieved. */
  private String url;

  private static final Log log = LogFactory.getLog(RepresentationInfo.class);

  public static RepresentationInfo[] parseObjectInfo(ObjectInfo objectInfo) {

    ArrayList<RepresentationInfo> returnRepresentationInfo = new ArrayList();

    Map<String, List<String>> data = objectInfo.getData();
    Set<String> formats = objectInfo.getRepresentations();
    Iterator it = formats.iterator();
    while(it.hasNext()) {
      String format = (String) it.next();

      // build keys into predicate Map
      String contentTypeKey = "<topaz:" + format + "-contentType>";
      String objectSizeKey = "<topaz:" + format + "-objectSize>";

      String contentType = null;
      List<String> contentTypeList = data.get(contentTypeKey);
      if (contentTypeList != null) {
        if (contentTypeList.size() == 1) {
          contentType = contentTypeList.get(0);
        } else {
          // TODO: data integ issue?
          if (log.isWarnEnabled()) {
            log.warn("found " + contentTypeList.size() + " " + contentTypeKey);
          }
        }
      } else {
        // TODO: what kind of error?
        if (log.isWarnEnabled()) {
          log.warn("missing: " + contentTypeKey);
        }
      }

      long objectSize = 0L;
      List<String> objectSizeList = data.get(objectSizeKey);
      if (objectSizeList != null) {
        if (objectSizeList.size() == 1) {
          objectSize = Long.parseLong(objectSizeList.get(0));
        } else {
          // TODO: data integ issue?
          if (log.isWarnEnabled()) {
            log.warn("found " + objectSizeList.size() + " " + objectSizeKey);
          }
        }
      } else {
        // TODO: what kind of error?
        if (log.isWarnEnabled()) {
          log.warn("missing: " + objectSizeKey);
        }
      }

      String url = null;
      try {
        url = (new ArticleUtil()).getObjectURL(objectInfo.getId().toString(), format);
      } catch (MalformedURLException ex) {
        // TODO: data integ issue
      } catch (NoSuchObjectIdException ex) {
        // TODO: data integ issue
      } catch (ServiceException ex) {
        // TODO: data integ issue
      } catch (RemoteException ex) {
        // TODO: data integ issue
      }

      RepresentationInfo representationInfo = new RepresentationInfo();
      representationInfo.setContentType(contentType);
      representationInfo.setName(format);
      representationInfo.setSize(objectSize);
      representationInfo.setURL(url);

      returnRepresentationInfo.add(representationInfo);
    }
      return returnRepresentationInfo.toArray(new RepresentationInfo[returnRepresentationInfo.size()]);
  }

  /**
   * Get the name of the representation.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the representation.
   *
   * @param name the name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the mime-type of the content of the representation.
   *
   * @return the content-type.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the mime-type of the content of the representation.
   *
   * @param contentType the content-type.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Get the size of the representation.
   *
   * @return the size, in bytes, or -1 if unknown.
   */
  public long getSize() {
    return size;
  }

  /**
   * Set the size of the representation.
   *
   * @param size the size, in bytes.
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Get the URL where this representation can be downloaded from.
   *
   * @return the URL.
   */
  public String getURL() {
    return url;
  }

  /**
   * Set the URL where this representation can be downloaded from.
   *
   * @param url the URL.
   */
  public void setURL(String url) {
    this.url = url;
  }
}
