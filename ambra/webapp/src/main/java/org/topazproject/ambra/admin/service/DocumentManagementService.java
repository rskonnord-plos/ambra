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

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.IngestException;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.models.Article;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public interface DocumentManagementService {

  /**
   * Deletes an article from Topaz and flushes only the Related Articles from BrowseCache.
   * Useful for deleting a recently ingested article that hasn't been published
   *
   * @param objectURI URI of the article to delete
   * @throws Exception if id is invalid or Sending of delete message failed.
   */
  public void delete(String objectURI) throws Exception;

  /**
   * Revert the data out of the ingested queue
   *
   * @param uri the article uri
   *
   * @throws IOException on an error
   */
  public void revertIngestedQueue(String uri) throws IOException;
  /**
   * Deletes articles from Topaz and flushes the servlet image cache and article cache
   *
   * @param objectURIs  URIs of the articles to delete
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> delete(String[] objectURIs);

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
  public Article ingest(Ingester ingester, boolean force)
      throws IngestException, DuplicateArticleIdException, URISyntaxException, IOException;

  public Ingester createIngester(File file) throws IOException;

  /**
   * @return List of filenames of files in uploadable directory on server
   */
  public List<String> getUploadableFiles() ;

  /**
   * Get a list of files from the auto ingest queue
   * @return List of filenames of files in auto ingestable directory on server
   */
  public List<String> getAutoIngestFiles();
  /**
   * Move the file to the ingested directory and generate cross-ref.
   *
   * @param file the file to move
   * @param article the associated article
   *
   * @throws IOException on an error
   */
  public void generateIngestedData(File file, Article article)
    throws IOException;
  /**
   * Move the file to the ingested directory and generate cross-ref.
   *
   * @param file the file to move
   *
   */
  public void moveAutoToIngestDirectory(File file);
  /**
   * Returns a list of publishable articles sorted by doi in ascending order
   * @return A list of articles in ST_DISABLED
   * @throws ApplicationException on an error
   */
  public Map<String, Article> getPublishableArticles() throws ApplicationException;

  /**
   * Returns a list of publishable articles sorted by pub date
   *
   * @param dateAscending true - sort pub date in ascending order, false - descending
   * @return list of publishable articles sorted by pub date
   * @throws ApplicationException
   */
  public Map<String, Article> getPublishableArticles(boolean dateAscending) throws ApplicationException;

  /**
   * @param uris  uris to be published. Send CrossRef xml file to CrossRef - if it is _received_ ok
   *              then set article stat to active
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> publish(String[] uris);

  public String getDocumentDirectory();

  public String getDocumentAutoDirectory();
}
