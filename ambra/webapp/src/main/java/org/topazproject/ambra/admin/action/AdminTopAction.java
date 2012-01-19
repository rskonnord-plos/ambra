/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc. http://topazproject.org
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

package org.topazproject.ambra.admin.action;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.models.Article;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.File;

@SuppressWarnings("serial")
public class AdminTopAction extends BaseAdminActionSupport {

  private static final Log log = LogFactory.getLog(AdminTopAction.class);

  private Collection<String> uploadableFiles;
  private Collection<String> publishableFiles;

  private DocumentManagementService documentManagementService;
  private Session session;


  // Fields used for delete
  private String article;

  // Fields used foor ingest
  private String[] filesToIngest;
  private boolean  force = false;

  // Fields used for publishArchives
  private String[] articlesToPublish;
  private String[] articlesInVirtualJournals;
  private String[] articlesToDelete;


  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {

    // create a faux journal object for template
    if (setCommonFields())
      return ERROR;
    return SUCCESS;
  }

  /**
   * Struts action method
   * @return Struts result
   * @throws Exception when error occures
   */
  public String delete() throws Exception {
    if (article != null)
      article = article.trim();

    try {
      RdfUtil.validateUri(article, "Article Uri");
      documentManagementService.delete(article);
      addActionMessage("Successfully deleted article: " + article);

      try {
        documentManagementService.revertIngestedQueue(article);
      } catch (Exception ioe) {
        log.warn("Error cleaning up spool directories for '" + article +
            "' - manual cleanup required", ioe);
      }
    } catch (Exception e) {
      addActionError("Failed to successfully delete article: " + article + ". <br>" + e);
      log.error("Failed to successfully delete article: " + article, e);
    }

    // create a faux journal object for template
    if (setCommonFields())
      return ERROR;
    return SUCCESS;
  }

  /**
   * Struts action method
   * @return Struts result
   */
  public String ingest() {
    if (filesToIngest != null) {
      for (String filename : filesToIngest) {
        filename = filename.trim();
        try {
          File file = new File(documentManagementService.getDocumentDirectory(), filename);
          log.info("Creating ingester for " + file);
          Ingester ingester = documentManagementService.createIngester(file);
          log.info("Preparing ingester for " + file);
          ingester.prepare(configuration);
          log.info("Starting ingest for " + file);
          Article article = documentManagementService.ingest(ingester, force);
          log.info("Finished ingest for " + file);
          addActionMessage("Ingested: " + filename);

          session.evict(article);  // purely for performance. Subsequent flush()es will be faster.
          documentManagementService.generateIngestedData(file, article);
        } catch (DuplicateArticleIdException de) {
          addActionError("Error ingesting: " + filename + " - " + getMessages(de));
          log.error("Error ingesting article: " + filename , de);
        } catch (Exception e) {
          addActionError("Error ingesting: " + filename + " - " + getMessages(e));
          log.error("Error ingesting article: " + filename, e);
        }
      }
    }

    // create a faux journal object for template
    if (setCommonFields())
      return ERROR;
    return SUCCESS;
  }

  private static String getMessages(Throwable t) {
    StringBuilder msg = new StringBuilder();
    while (t != null) {
      msg.append(t.toString());
      t = t.getCause();
      if (t != null)
        msg.append("<br/>\n");
    }
    return msg.toString();
  }

  @Transactional(rollbackFor = { Throwable.class })
  public String publishArchives() {
    try {
      deleteArticles();
      publishArticles();
    } catch (Exception e) {
      addActionError("Exception: " + e);
      log.error(e);
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ERROR;
    }

    // create a faux journal object for template
    if (setCommonFields())
      return ERROR;
    return SUCCESS;
  }

  /**
   * Publishes articles from the admin console.
   */
  private void publishArticles() {
    if (articlesToPublish == null)
      return;

    Map<String, Set<String>> vjMap = new HashMap<String, Set<String>>();
    if (articlesInVirtualJournals != null) {
      for (String articleInVirtualJournal : articlesInVirtualJournals) {
        // form builds checkbox value as "article" + "::" + "virtualJournal"
        String[] parts = articleInVirtualJournal.split("::");
        Set<String> vjList = vjMap.get(parts[0]);
        if (vjList == null)
          vjMap.put(parts[0], vjList = new HashSet<String>());
        vjList.add(parts[1]);
      }
    }

    List<String> msgs = documentManagementService.publish(articlesToPublish, vjMap);
    for (String msg : msgs)
      addActionMessage(msg);
  }

  /**
   * Deletes the checked articles from the admin console.
   */
  private void deleteArticles() {
    if (articlesToDelete == null)
      return;

    List<String> msgs = documentManagementService.delete(articlesToDelete);
    for (String msg : msgs)
      addActionMessage(msg);

    for (String article : articlesToDelete) {
      try {
        documentManagementService.revertIngestedQueue(article);
      } catch (Exception ioe) {
        log.warn("Error cleaning up spool directories for '" + article +
                 "' - manual cleanup required", ioe);
        addActionMessage("Failed to move " + article + " back to ingestion queue: " + ioe);
      }
    }
  }

  /**
   *
   * @param articles array of articles to publish
   */
  public void setArticlesToPublish(String[] articles) {
    articlesToPublish = articles;
  }

  /**
   *
   * @param articlesInVirtualJournals array of ${virtualJournal} + "::" + ${article} to publish.
   */
  public void setArticlesInVirtualJournals(String[] articlesInVirtualJournals) {
    this.articlesInVirtualJournals = articlesInVirtualJournals;
  }

  /**
   *
   * @param articles array of articles to delete
   */
  public void setArticlesToDelete(String[] articles) {
    articlesToDelete= articles;
  }

  private boolean setCommonFields() {
    // create a faux journal object for template
    initJournal();
    // catch all Exceptions to keep Admin console active (vs. Site Error)
    try {
      uploadableFiles = documentManagementService.getUploadableFiles();
      publishableFiles = documentManagementService.getPublishableFiles();
    } catch (Exception e) {
      log.error("Admin console Exception", e);
      addActionError("Exception: " + e);
      return true;
    }
    return false;
  }

  /**
   * Sets the DocumentManagementService.
   *
   * @param  documentManagementService The document management service
   */
  @Required
  public void setDocumentManagementService(DocumentManagementService documentManagementService) {
    this.documentManagementService = documentManagementService;
  }

  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
    * Gets the collection of uploadable files
    *
    * @return List of file names
    */
  public Collection<String> getUploadableFiles() {
    return uploadableFiles;
  }

  /**
    * Gets the list of ingested articles that are not published yet.
    *
    * @return List of articles
    */
  public Collection<String> getPublishableFiles() {
    return publishableFiles;
  }

  /**
   * Form field setter
   * @param a article id
   */
  public void setArticle(String a) {
    article = a;
  }

  /**
   * Form field setter
   * @param files list of files
   */
  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

  /**
   * Form field setter
   * @param flag true or false
   */
  public void setForce(boolean flag) {
    force = flag;
  }
}
