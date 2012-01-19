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

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.commons.lang.StringUtils;
import org.plos.filestore.FSIDMapper;
import org.plos.filestore.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticlePersistenceService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.article.service.NoSuchObjectIdException;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.service.XMLService;
import org.topazproject.ambra.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Set;

/**
 * Fetch the object is primarily used to fetch assets associated with a particular DOI. These assets (tif's pdf's,
 * supplementary data etc) are either used in the rendered html version of the article or supplied to the user for
 * downloading.
 * <p/>
 * All transactions will be spanning over to results.
 */
@Transactional(readOnly = true)
public class FetchObjectAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(FetchObjectAction.class);

  private ArticlePersistenceService articlePersistenceService;
  private FileStoreService fileStoreService;
  private XMLService xmlService;

  private boolean fullDOI = false;
  private String uri;
  private String representation;
  private String contentDisposition;
  private String contentType;
  private String xReproxyList;
  private String reproxyCacheSettings;
  private InputStream inputStream;
  private Long contentLength;
  private Date lastModified;

  /**
   * Return the object for a given uri and representation
   *
   * @return webwork status code
   * @throws Exception Exception
   */
  @Transactional
  public String fetchObjectAction() {
    try {
      if (StringUtils.isEmpty(representation)) {
        addActionMessage("No representation specified");
        return ERROR;
      }

      Representation rep = null;
      ObjectInfo objectInfo;

      // PDF's and XML article representaions are linked to the article
      // everything else is through ObjectInfo.
      if (representation.equalsIgnoreCase("PDF") || representation.equalsIgnoreCase("XML")) {
        objectInfo = articlePersistenceService.getArticle(URI.create(uri), getAuthId());
        rep = objectInfo.getRepresentation(representation);
      } else {
        objectInfo = articlePersistenceService.getObjectInfo(uri, getAuthId());
        rep = objectInfo.getRepresentation(representation);
      }

      if (null == rep) {
        addActionMessage("No such representation '" + representation + "' for uri: " + uri);
        return ERROR;
      }

      setResponseParams(rep, objectInfo);

      // Using the uri (doi) and representation (the file type)
      // construct a fsid to get the Inputstream
      String fsid = FSIDMapper.doiTofsid(uri, representation);

      // If x-reproxy is available and they are not requesting a transformed
      // document then get the redirect urls.
      Boolean needTransformedXML = (fullDOI && "XML".equals(representation));
      if (fileStoreService.hasXReproxy() && !needTransformedXML) {
        StringBuilder str = new StringBuilder();
        URL[] urls =  fileStoreService.getRedirectURL(fsid);
        for (URL url : urls) {
          str.append(url).append(" ");
        }
        xReproxyList = str.toString();
        reproxyCacheSettings = fileStoreService.getReproxyCacheSettings();
      } else {
        // x-reproxy not available so return the xml document stream
        // or a transformed document if requested.
        InputStream fsStream = fileStoreService.getFileInStream(fsid);
        inputStream = needTransformedXML ? xmlService.getTransformedInputStream(fsStream) : fsStream;
      }

    } catch (NoSuchArticleIdException e) {
      log.info("Article not found: " + uri, e);
      return ERROR;
    } catch (NoSuchObjectIdException e) {
      log.info("Object not found: " + uri, e);
      return ERROR;
    } catch (Exception e) {
      log.error("Error retrieving object: " + uri, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Fetch the only representation of the object. If the object does not exist or it has no representations then an
   * error is returned; if there are more than 1 representations a random representation is chosen.
   * <p/>
   * WTO: From the WTF department - Look into who uses this and why. Seems really stupid.
   *
   * @return webwork status code
   * @throws Exception Exception
   */
  public String fetchSingleRepresentation() {
    try {
      final ObjectInfo objectInfo = articlePersistenceService.getObjectInfo(uri, getAuthId());

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

      Representation rep = representations.iterator().next();
      setResponseParams(rep, objectInfo);

      // Using the uri (doi) and representation (the file type)
      // construct the fsid to get the Inputstream.
      String fsid = FSIDMapper.doiTofsid(uri, rep.getName());

      if (fileStoreService.hasXReproxy()) {
         StringBuilder str = new StringBuilder();
         URL[] urls =  fileStoreService.getRedirectURL(fsid);
         for (URL url : urls) {
           str.append(url).append(" ");
         }
         xReproxyList = str.toString();
         reproxyCacheSettings = fileStoreService.getReproxyCacheSettings();
       } else {
         // x-reproxy not available so return the xml document stream
         inputStream = fileStoreService.getFileInStream(fsid);
       }

    } catch (NoSuchObjectIdException e) {
      log.info("Object not found: " + uri, e);
      return ERROR;
    } catch (Exception e) {
      log.error("Error retrieving object: " + uri, e);
      return ERROR;
    }
    return SUCCESS;
  }

  /*
   * Set up the various feilds that will be needed by the results handler
   * to properly form a response.
   */
  private void setResponseParams(Representation rep, ObjectInfo objectInfo) throws IOException {

    contentType = rep.getContentType();
    contentLength = rep.getSize();
    if (contentType == null)
      contentType = "application/octet-stream";

    lastModified = rep.getLastModified();
    if (lastModified == null)
      lastModified = objectInfo.getDublinCore().getDate();

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
   * Returns the InputStream of the File store
   *
   * @return
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
   * @return Return the list of x-reproxy url's that are available for this request.
   */
  public String getXReproxyList() {
    return xReproxyList;
  }

  /**
   * @return Return the reproxy cache settings.
   */
  public String getReproxyCacheSettings() {
    return this.reproxyCacheSettings;
  }

  /**
   * Set wether we should run the XSL transformation for the full DOI or not.
   *
   * @return
   */
  public boolean isFullDOI() {
    return fullDOI;
  }

  /**
   * Wether we should run the XSL transformation for the full DOI or not.
   *
   * @param fullDOI
   */
  public void setFullDOI(boolean fullDOI) {
    this.fullDOI = fullDOI;
  }

  /**
   * Set the XMLService
   *
   * @param xmlService
   */
  @Required
  public void setXmlService(XMLService xmlService) {
    this.xmlService = xmlService;
  }

  /**
   * Set the FileStoreService
   *
   * @param fileStoreService
   */
  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  /**
   * Spring setter method to inject articlePersistenceService
   *
   * @param articlePersistenceService articlePersistenceService
   */
  @Required
  public void setArticlePersistenceService(final ArticlePersistenceService articlePersistenceService) {
    this.articlePersistenceService = articlePersistenceService;
  }
}
