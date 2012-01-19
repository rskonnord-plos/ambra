/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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
package org.topazproject.ambra.article.action;

import java.util.Date;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.SmallBlobService;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.util.FileUtils;
import org.topazproject.ambra.service.XMLService;
import org.topazproject.ambra.struts2.Span;
import org.topazproject.otm.RdfUtil;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fetch the object for a given uri.
 * Returns either inputStream or inputByteArray. If returning inputStream, contentLength
 * needs to be passed too.
 *
 * All transactions will be spanning over to results.
 */
@Span(@Transactional(readOnly = true))
public class FetchObjectAction extends BaseActionSupport {
  private static final Logger    log             = LoggerFactory.getLogger(FetchObjectAction.class);
  private static final String SMALL_BLOB_SIZE = "ambra.cache.smallBlobSize";

  private ArticleOtmService articleOtmService;
  private SmallBlobService  smallBlobService;

  private String            uri;
  private String            representation;
  private String            contentDisposition;
  private String            contentType;
  private byte[]            inputByteArray;
  private InputStream       inputStream;
  private Long              contentLength;
  private Date              lastModified;

  /**
   * Return the object for a given uri and representation
   * @return webwork status code
   * @throws Exception Exception
   */
  public String execute() throws Exception {
    if (StringUtils.isEmpty(representation)) {
      addActionMessage("No representation specified");
      return ERROR;
    }

    RdfUtil.validateUri(uri, "uri=<" + uri + ">");

    ObjectInfo objectInfo = articleOtmService.getObjectInfo(uri);
    if (null == objectInfo) {
      addActionMessage("No object found for uri: " + uri);
      return ERROR;
    }

    Representation rep = objectInfo.getRepresentation(representation);
    if (null == rep) {
      addActionMessage("No such representation '" + representation + "' for uri: " + uri);
      return ERROR;
    }

    handleBlob(rep);

    return SUCCESS;
  }

  /**
   * If size of Representation object is less than what is specified in small blob size config
   * parameter, blob will be fetched as byte array and stored in cache.
   * @param rep Representation object.
   * @throws Exception
   */
  private void handleBlob(final Representation rep) throws Exception {
    setResponseParams(rep);
    contentLength = rep.getSize();

    long smallBlobSizeBytes = configuration.getLong(SMALL_BLOB_SIZE, 0l) * 1024l;

    if (rep.getSize() <= smallBlobSizeBytes) {
      inputByteArray = smallBlobService.getSmallBlob(rep);

    } else {
      // Large blob. Do not cache.
      inputStream = rep.getBody().getInputStream();
    }
  }

  /**
   * Fetch the only representation of the object. If the object does not exist or
   * it has no representations then an error is returned; if there are more than 1
   * representations a random representation is chosen.
   *
   * @return webwork status code
   * @throws Exception Exception
   */
  public String fetchSingleRepresentation() throws Exception {
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
    if (representations.size() > 1)
      log.warn("Found " + representations.size() + " representations for '" + uri +
               "' where only one was expected");

    handleBlob(representations.iterator().next());

    return SUCCESS;
  }

  private void setResponseParams(Representation rep) throws IOException {
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
   * Returns the InputStream of the Fedora object
   * @return the InputStream of the Fedora object.
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  public Long getContentLength() {
    return contentLength;
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

  /**
   * Spring setter method to inject articleOtmService
   * @param articleOtmService articleOtmService
   */
  @Required
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * Spring setter method to inject small objects cache
   * @param smallBlobService Small blob service
   */
  @Required
  public void setSmallBlobService(SmallBlobService smallBlobService) {
    this.smallBlobService = smallBlobService;
  }
}
