/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.plos.article.action;

import java.util.Date;
import java.util.Set;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.models.ObjectInfo;
import org.plos.models.Representation;
import org.plos.util.FileUtils;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Fetch the object for a given uri
 */
public class FetchObjectAction extends BaseActionSupport {
  private ArticleOtmService articleOtmService;
  private String uri;
  private String representation;

  private String contentDisposition;
  private static final Log log = LogFactory.getLog(FetchObjectAction.class);
  private String contentType;
  private byte[] inputByteArray;
  private Date lastModified;

  /**
   * Return the object for a given uri and representation
   * @return webwork status code
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    if (StringUtils.isEmpty(representation)) {
      addFieldError("representation", "Object representation is required");
      return INPUT;
    }

    final ObjectInfo objectInfo = articleOtmService.getObjectInfo(uri);

    if (null == objectInfo) {
      addActionMessage("No object found for uri: " + uri);
      return ERROR;
    }

    Representation rep = objectInfo.getRepresentation(representation);

    if (null == rep) {
      addActionMessage("No such representation '" + representation + "' for uri: " + uri);
      return ERROR;
    }

    setOutputStreamAndAttributes(rep);
    return SUCCESS;
  }

  /**
   * Return the first representation of the uri
   * @return webwork status code
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String fetchFirstObject() throws Exception {
    final ObjectInfo objectInfo = articleOtmService.getObjectInfo(uri);

    if (null == objectInfo) {
      addActionMessage("No object found for uri: " + uri);
      return ERROR;
    }

    final Set<Representation> representations = objectInfo.getRepresentations();
    if ((representations == null) || representations.isEmpty()) {
      addActionMessage("No representations found for uri: " + uri);
      return ERROR;
    }

    setOutputStreamAndAttributes(representations.iterator().next());
    return SUCCESS;
  }

  private void setOutputStreamAndAttributes(final Representation rep) throws IOException {
    inputByteArray = rep.getBody();

    contentType = rep.getContentType();
    if (contentType == null)
      contentType = "application/octet-stream";

    lastModified = rep.getLastModified();
    if (lastModified == null)
      lastModified = rep.getObject().getDublinCore().getDate();
    if (lastModified == null)
      log.warn("Missing modification date for " + uri);

    contentDisposition = getContentDisposition(getFileExtension(contentType));
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
   * Returns the byte[] of the Fedora object so it is not necessary to read from it using an InputStream
   * @return the byte[] representation of the Fedora object. 
   */
  public byte[] getInputByteArray() {
    return inputByteArray;
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

  /**
   * @return Return the last modified time for the object
   */
  public Date getLastModified() {
    return lastModified;
  }
}
