/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.RepresentationInfo;
import org.plos.models.ObjectInfo;
import org.plos.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Fetch the object for a given uri
 */
public class FetchObjectAction extends BaseActionSupport {
  private ArticleOtmService articleOtmService;
  private String uri;
  private String representation;

  private InputStream inputStream;
  private String contentDisposition;
  private static final Log log = LogFactory.getLog(FetchObjectAction.class);
  private String contentType;

  /**
   * Return the object for a given uri and representation
   * @return webwork status code
   * @throws Exception Exception
   */
  public String execute() throws Exception {
    if (StringUtils.isEmpty(representation)) {
      addFieldError("representation", "Object representation is required");
      return INPUT;
    }

    final String objectURL = articleOtmService.getObjectURL(uri, representation);
    setOutputStreamAndAttributes(objectURL);
    return SUCCESS;
  }

  /**
   * Return the first representation of the uri
   * @return webwork status code
   * @throws Exception Exception
   */
  public String fetchFirstObject() throws Exception {
    final ObjectInfo objectInfo = articleOtmService.getObjectInfo(uri);

    if (null == objectInfo) return ERROR;

    final RepresentationInfo[] representations = RepresentationInfo.parseObjectInfo(objectInfo);
    if (representations.length == 0) {
      addActionMessage("No representations found");
      log.error("No representation found for the uri:" + uri);
      return ERROR;
    }

    final RepresentationInfo firstRep = representations[0];
    final String objectUrl = firstRep.getURL();
    setOutputStreamAndAttributes(objectUrl);
    return SUCCESS;
  }

  private void setOutputStreamAndAttributes(final String objectUrl) throws IOException {
    final URLConnection urlConnection = new URL(objectUrl).openConnection();
    inputStream = urlConnection.getInputStream();
    contentType = urlConnection.getContentType();
    final String fileExt = getFileExtension(contentType);
    contentDisposition = getContentDisposition(fileExt);
  }

  private String getContentDisposition(final String fileExt) {
    return "filename=\"" + FileUtils.getFileName(uri) + "." + fileExt + "\"";
  }

  private String getFileExtension(final String contentType) throws IOException {
    return FileUtils.getDefaultFileExtByMimeType(contentType);
  }

  /**
   * Set articleOtmService
   * @param articleOtmService articleOtmService
   */
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  @RequiredStringValidator(message = "Object URI is required.")
  public String getUri() {
    return uri;
  }

  /**
   * @param uri set uri
   */
  public void setUri(final String uri) {
    this.uri = uri;
  }

  /**
   * @return the representation of the object
   */
  public String getRepresentation() {
    return representation;
  }

  /**
   * @param representation set the representation of the article ex: XML, PDF, etc
   */
  public void setRepresentation(final String representation) {
    this.representation = representation;
  }

  /**
   * @return get the input stream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * @return the filename that the file will be saved as of the client browser
   */
  public String getContentDisposition() {
    return contentDisposition;
  }

  /**
   * @return Return the content type for the object
   */
  public String getContentType() {
    return contentType;
  }
}
