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

package org.topazproject.ambra.admin.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.IngestException;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.Zip;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.RelatedArticle;
import org.topazproject.xml.transform.cache.CachedSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public class DocumentManagementService {
  private static final Logger log = LoggerFactory.getLogger(DocumentManagementService.class);
  private ArticleOtmService articleOtmService;
  private FetchArticleService fetchArticleService;
  private String documentDirectory;
  private String ingestedDocumentDirectory;
  private String documentPrefix;
  private CrossRefPosterService crossRefPosterService;
  private File xslTemplate;
  private JournalService journalService;
  private SyndicationService syndicationService;
  private String plosDoiUrl;
  private String plosEmail;
  private boolean sendToXref;
  private Cache browseCache;

  private List<OnPublishListener> onPublishListeners;
  private List<OnDeleteListener> onDeleteListeners;

  private FileTypeMap fileTypeMap = new FileTypeMap() {
    private final Map<String, String> extensions = new HashMap<String, String>();

    {
      extensions.put(".zip",     "application/zip");
      extensions.put(".tar",     "application/x-tar");
      extensions.put(".tar.gz",  "application/x-tar-gz");
      extensions.put(".tgz",     "application/x-tar-gz");
      extensions.put(".tar.bz",  "application/x-tar-bz");
      extensions.put(".tar.bz2", "application/x-tar-bz");
      extensions.put(".tbz2",    "application/x-tar-bz");
      extensions.put(".tbz",     "application/x-tar-bz");
      extensions.put(".tb2",     "application/x-tar-bz");
    }

    @Override
    public String getContentType(File file) {
      return getContentType(file.getName());
    }

    @Override
    public String getContentType(String name) {
      name = name.toLowerCase();
      for (Map.Entry<String, String> stringStringEntry : extensions.entrySet())
        if (name.endsWith(stringStringEntry.getKey()))
          return stringStringEntry.getValue();
      return "application/octet-stream";
    }
  };

  /**
   * Set the article web service
   *
   * @param articleOtmService articleOtmService
   */
  @Required
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  @Required
  public void setDocumentDirectory(final String documentDirectory) {
    this.documentDirectory = documentDirectory;
  }

  @Required
  public void setDocumentPrefix(final String documentPrefix) {
    this.documentPrefix = documentPrefix;
  }

  public String getDocumentDirectory() {
    return documentDirectory;
  }

  @Required
  public void setIngestedDocumentDirectory(final String ingestedDocumentDirectory) {
    this.ingestedDocumentDirectory = ingestedDocumentDirectory;
  }

  @Required
  public void setCrossRefPosterService(final CrossRefPosterService crossRefPosterService) {
    this.crossRefPosterService = crossRefPosterService;
  }

  public void setOnPublishListeners(List<OnPublishListener> onPublishListeners) {
    this.onPublishListeners = onPublishListeners;
  }

  public void setOnDeleteListeners(List<OnDeleteListener> onDeleteListeners) {
    this.onDeleteListeners = onDeleteListeners;
  }

  @Required
  public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @return the local or remote file or url as a java.io.File
   * @throws URISyntaxException when URL is malformed
   */
  private File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      // access it as a local file resource
      return new File(org.topazproject.ambra.util.FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Deletes an article from Topaz and flushes only the Related Articles from BrowseCache.
   * Useful for deleting a recently ingested article that hasn't been published
   *
   * @param objectURI URI of the article to delete
   * @throws Exception if id is invalid or Sending of delete message failed.
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void delete(String objectURI) throws Exception {
    URI id = URI.create(objectURI);
    // Remove all Related Articles from BrowseCache so there will be no links to deleted article.
    Article article = articleOtmService.getArticle(id);
    if (article != null // No guarantee that the article exists at this point.
        && article.getRelatedArticles() != null && article.getRelatedArticles().size() > 0) {
      for (RelatedArticle relatedArticle : article.getRelatedArticles()) {
        browseCache.remove(BrowseService.ARTICLE_KEY + relatedArticle.getArticle());
      }
    }

    articleOtmService.delete(objectURI);
    for (Journal j : journalService.getAllJournals()) {
      List<URI> col = j.getSimpleCollection();
      if (col != null)
        while (col.contains(id))
          col.remove(id);
    }

    invokeOnDeleteListeners(objectURI);

  }

  /**
   * Revert the data out of the ingested queue
   *
   * @param uri the article uri
   *
   * @throws IOException on an error
   */
  public void revertIngestedQueue(String uri) throws IOException {
    // delete any crossref submission file
    File queueDir        = new File(documentDirectory);
    File ingestedDir     = new File(ingestedDocumentDirectory);
    File ingestedXmlFile = new File(ingestedDir, uri.replaceAll("[:/.]", "_") + ".xml");

    if (log.isDebugEnabled())
      log.debug("Deleting '" + ingestedXmlFile + "'");

    try {
      FileUtils.forceDelete(ingestedXmlFile);
    } catch (FileNotFoundException fnfe) {
      log.info("'" + ingestedXmlFile + "' does not exist - cannot delete: ", fnfe);
    }

    // move zip back to ingestion queue
    if (!queueDir.equals(ingestedDir)) {
      // strip 'info:doi/10.1371/journal.'
      String fname = uri.substring(documentPrefix.length()) + ".zip";
      File fromFile = new File(ingestedDir, fname);
      File toFile   = new File(queueDir,    fname);

      try {
        if (log.isDebugEnabled())
          log.debug("Moving '" + fromFile + "' to '" + toFile + "'");
        FileUtils.moveFile(fromFile, toFile);
      } catch (FileNotFoundException fnfe) {
        log.info("Could not move '" + fromFile + "' to '" + toFile + "': ", fnfe);
      }
    }
  }

  /**
   * Deletes articles from Topaz and flushes the servlet image cache and article cache
   *
   * @param objectURIs  URIs of the articles to delete
   * @return a list of messages describing what was successful and what failed
   */
  @Transactional(rollbackFor = { Throwable.class })
  public List<String> delete(String[] objectURIs) {
    List<String> msgs = new ArrayList<String>();
    for (String objectURI : objectURIs) {
      try {
        delete(objectURI);
        msgs.add("Deleted: " + objectURI);
        if (log.isInfoEnabled())
          log.info("Deleted article: " + objectURI);
      } catch (Exception e) {
        log.error("Could not delete article: " + objectURI, e);
        msgs.add("Error deleting: " + objectURI + " - " + e);
      }
    }

    return msgs;
  }

  /**
   * Execute the ingest in transaction context. If successful create the Transformed CrossRef xml
   * file and deposit that in the Directory as well.
   *
   * @param ingester the ingester to execute
   * @param force if true then don't check whether this article already exists but just
   *              save this new article.
   * @return the ingested article
   * @throws IngestException on an error in ingest
   * @throws DuplicateArticleIdException if the article exists and force is false
   * @throws IOException on any other error
   * @throws URISyntaxException if article ID is malformed
   */
  @Transactional(rollbackFor = { Throwable.class })
  public Article ingest(Ingester ingester, boolean force)
      throws IngestException, DuplicateArticleIdException, URISyntaxException, IOException {

    Article article = articleOtmService.ingest(ingester, force);

    try {
      generateCrossRefInfoDoc(article);
    } catch (TransformerException e) {
      throw new IngestException("Failed to generate cross-ref", e);
    } catch (SAXException e) {
      throw new IngestException("Failed to generate cross-ref", e);
    }

    if (log.isInfoEnabled()) {
      log.info("Generated Xref: " + article.getId() + " ingested from '"
          + ingester.getZip().getName() + "'");
    }

    // Create one Syndication object for each of the possible
    // syndication targets, then put those objects into a Set in this Article object.
    syndicationService.createSyndications(article.getId().toString());

    return article;
  }

  public Ingester createIngester(File file) throws IOException {
    FileDataSource fd = new FileDataSource(file);
    fd.setFileTypeMap(fileTypeMap);
    return new Ingester(fd, browseCache);
  }

  /**
   * @return List of filenames of files in uploadable directory on server
   */
  public List<String> getUploadableFiles() {
    List<String> documents = new ArrayList<String>();
    File dir = new File(documentDirectory);
    if (dir.isDirectory()) {
      for (String name : dir.list()) {
        if (Zip.StreamZip.isArchive(fileTypeMap.getContentType(name)))
          documents.add(name);
      }
    }

    Collections.sort(documents);
    return documents;
  }

  /**
   * Move the file to the ingested directory and generate cross-ref.
   *
   * @param file the file to move
   * @param article the associated article
   *
   * @throws IOException on an error
   */
  public void generateIngestedData(File file, Article article)
    throws IOException {
    FileUtils.deleteQuietly(new File(ingestedDocumentDirectory, file.getName()));
    FileUtils.moveFileToDirectory(file, new File(ingestedDocumentDirectory), true);
    if (log.isInfoEnabled()) {
      log.info("Relocated: " + file + ":" + article.getId());
    }
  }

  /**
   * @return A list of articles in ST_DISABLED
   * @throws ApplicationException on an error
   */
  public Map<String, Article> getPublishableArticles() throws ApplicationException {
    try {
      Map<String, Article> articles = new LinkedHashMap<String, Article>();
      List<String> dois =
          fetchArticleService.getArticleIds(null, null, new int[] { Article.STATE_DISABLED });
      Collections.sort(dois);

      for(String uri : dois) {
        Article a = articleOtmService.getArticle(new URI(uri));
        articles.put(uri, a);
      }

      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private void generateCrossRefInfoDoc(Article article)
      throws IOException, TransformerException, SAXException {
    Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(xslTemplate));
    t.setParameter("plosDoiUrl", plosDoiUrl);
    t.setParameter("plosEmail", plosEmail);

    InputSource artXml = new InputSource(article.getRepresentation("XML")
                                                  .getBody().getInputStream());
    artXml.setSystemId(article.getId().toString());

    File target_xml =
        new File(ingestedDocumentDirectory, uriToFilename(article.getId().toString()) + ".xml");

    t.transform(new CachedSource(artXml), new StreamResult(target_xml));
  }

  /**
   * @param uri URI
   * @return a string usable as a distinct filename - ':', '/' and '.' -&gt; '_'
   */
  private String uriToFilename(String uri) {
    return uri.replace(':', '_').replace('/', '_').replace('.', '_');
  }

  /**
   * @param uris  uris to be published. Send CrossRef xml file to CrossRef - if it is _received_ ok
   *              then set article stat to active
   * @return a list of messages describing what was successful and what failed
   */
  @Transactional(rollbackFor = { Throwable.class })
  public List<String> publish(String[] uris) {
    final List<String> msgs = new ArrayList<String>();

    // publish articles
    for (String article : uris) {
      try {
        // send to cross-ref
        if (sendToXref) {
          File xref = new File(ingestedDocumentDirectory, uriToFilename(article) + ".xml");
          if (!xref.exists())
            throw new IOException("Cannot find CrossRef xml: " + uriToFilename(article) + ".xml");

          try {
            int stat = crossRefPosterService.post(xref);
            if (200 != stat)
              throw new Exception("CrossRef status returned " + stat);
          } catch (HttpException he) {
            log.error ("Could not connect to CrossRef", he);
            throw new Exception("Could not connect to CrossRef. " + he, he);
          } catch (IOException ioe) {
            log.error ("Could not connect to CrossRef", ioe);
            throw new Exception("Could not connect to CrossRef. " + ioe, ioe);
          }
        }

        // mark article as active
        articleOtmService.setState(article, Article.STATE_ACTIVE);
        invokeOnPublishListeners(article);

        msgs.add("Published: " + article);
        log.info("Published article: '" + article + "'");
      } catch (Exception e) {
        log.error("Could not publish article: '" + article + "'", e);
        msgs.add("Error publishing: '" + article + "' - " + e.toString());
      }
    }
    return msgs;
  }

  /**
   * Invokes all objects that are registered to listen to article publish event.
   *
   * @param articleId Article ID
   * @throws Exception If listener method failed
   */
  private void invokeOnPublishListeners(String articleId) throws Exception {
    if (onPublishListeners != null) {
      for (OnPublishListener listener : onPublishListeners) {
        listener.articlePublished(articleId);
      }
    }
  }

  /**
   * Invokes all objects that are registered to listen to article delete event.
   *
   * @param articleId Article ID
   * @throws Exception If listener method failed
   */
  private void invokeOnDeleteListeners(String articleId) throws Exception {
    if (onDeleteListeners != null) {
      for (OnDeleteListener listener : onDeleteListeners) {
        listener.articleDeleted(articleId);
      }
    }
  }


  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Sets the service allows Articles to be syndicated to external organizations.
   *
   * @param  syndicationService Service which allows Articles to be syndicated to external
   *   organizations
   */
  @Required
  public void setSyndicationService(SyndicationService syndicationService) {
    this.syndicationService = syndicationService;
  }

  /**
   * @param plosDoiUrl The plosDxUrl to set.
   */
  @Required
  public void setPlosDoiUrl(String plosDoiUrl) {
    this.plosDoiUrl = plosDoiUrl;
  }

  /**
   * @param sendToXref The sendToXref to set.
   */
  @Required
  public void setSendToXref(boolean sendToXref) {
    this.sendToXref = sendToXref;
  }

  /**
   * @param plosEmail The plosEmail to set.
   */
  @Required
  public void setPlosEmail(String plosEmail) {
    this.plosEmail = plosEmail;
  }

  /**
   * Articles will be removed from Browse Cache so that they can be re-queried.
   * This is necessary for the reciprocal Related Article links to be displayed correctly.
   *
   * @param browseCache The Browse Cache which contains the articles to be removed
   */
  @Required
  public void setBrowseCache(Cache browseCache) {
    this.browseCache = browseCache;
  }
}
