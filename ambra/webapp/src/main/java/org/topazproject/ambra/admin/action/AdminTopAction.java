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

package org.topazproject.ambra.admin.action;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.admin.service.SyndicationService;
import org.topazproject.ambra.admin.service.SyndicationService.SyndicationDTO;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.service.AmbraMailer;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URISyntaxException;

@SuppressWarnings("serial")
public class AdminTopAction extends BaseAdminActionSupport {

  private static final Logger log = LoggerFactory.getLogger(AdminTopAction.class);
  private static Boolean sync = false;
  private Collection<String> autoIngestFiles;
  private Collection<String> uploadableFiles;
  private Map<String, Article> publishableArticles;
  private Map<String, List<SyndicationDTO>> syndicationMap;
  private List<SyndicationDTO> syndications;

  private DocumentManagementService documentManagementService;
  private Session session;
  private SyndicationService syndicationService;

  // Fields used for delete
  private String article;
  private String action;

  // Fields used for ingest
  private String[] filesToIngest;
  private Boolean  force = false;

  // Fields used for processArticles
  private String[] articles;
  private String[] syndicates;

  // Used only for resyndicating articles that previously failed their syndications.
  private String[] resyndicates;
  private Boolean isFailedSyndications = false;

  private AmbraMailer ambraMailer;

  // fields used for sorting publishable articles
  private List<Article> orderedPublishableArticles;
  private String orderBy;


  /**
   * Main entry point to AdminTopAction.
   *
   * @return SUCCESS/ERROR
   * @throws Exception
   */
  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {

    // create a faux journal object for template
    if (!setCommonFields())
      return ERROR;

    return SUCCESS;
  }

  /**
   * Struts action method
   *
   * @return Struts result
   * @throws Exception when error occures
   */
@Transactional(rollbackFor = { Throwable.class })
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
    if (!setCommonFields())
      return ERROR;

    return SUCCESS;
  }

  /**
   * Ingest the files made available in the ingestion-queue.
   *
   * @return SUCCESS/ERROR
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
    if (!setCommonFields())
      return ERROR;

    return SUCCESS;
  }

  /**
   * Syncing on this boolean will prevent auto ingest re-entrance.
   * Returns true if boolean sync was acquired.
   *
   * @return true/false
   */
  private synchronized boolean acquire() {
    if (sync == false) {
      sync = true;
      return true;
    }
    return false;
  }

  /**
   * Syncing on this boolean will prevent auto ingest re-entrance.
   * Release the boolean to allow another call. Prevent situation
   * where there is an acquare without a corresponding release.
   *
   * ie if an exception skip over the release.
   *
   */
  private void release() {
    sync = false;
  }
  /**
   * Ingest the files made available in the auto-ingestion-queue.
   *
   * @return SUCCESS/ERROR
   */
  public String auto_ingest() {
    StringBuilder successList = new StringBuilder();
    StringBuilder failedList = new StringBuilder();
    StringBuilder fullList = new StringBuilder();
    List<String> filesToAutoIngest = documentManagementService.getAutoIngestFiles();

    if (acquire()) {
      for (String filename : filesToAutoIngest) {
        String fname = filename.trim();
        File file = new File(documentManagementService.getDocumentAutoDirectory(), fname);
        try {
          fullList.append(fname).append("\n");
          log.info("Creating auto-ingester for " + file);
          Ingester ingester = documentManagementService.createIngester(file);
          log.info("Preparing auto-ingester for " + file);
          ingester.prepare(configuration);
          log.info("Starting ingest for " + file);
          Article article = documentManagementService.ingest(ingester, false);
          log.info("Finished auto ingest for " + file);

          session.evict(article);  // purely for performance. Subsequent flush()es will be faster.
          documentManagementService.generateIngestedData(file, article);
          successList.append(fname).append("\n");
        } catch (DuplicateArticleIdException de) {
          documentManagementService.moveAutoToIngestDirectory(file);
          failedList.append(fname).append("\n");
          log.error("Error auto ingesting article: " + fname , de);
        } catch (Exception e) {
          documentManagementService.moveAutoToIngestDirectory(file);
          failedList.append(fname).append("\n");
          log.error("Error auto-ingesting article: " + fname, e);
        }
      }
      // release() must ALWAYS be called
      // AVOID exceptions before
      release();
      Map<String, Object> mapFields = new HashMap<String, Object>();
      mapFields.put("successList", successList);
      mapFields.put("failedList", failedList);
      mapFields.put("fullList", fullList);
      ambraMailer.sendIngestNotify(mapFields);
    }
      
    // create a faux journal object for template
    if (!setCommonFields())
      return ERROR;

    return SUCCESS;
  }

  /**
   * Extract the message string from an exception object.
   *
   * @param t The throwable containing the message that will be extracted.
   * @return message string
   */
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

  /**
   * Process submitted article list for either deletion of publication and syndication.
   *
   * @return SUCCESS/ERROR
   */
  public String processArticles() {
    if("Delete".equals(action)) {
      Boolean result = deleteArticles();
      if (!setCommonFields())
        return ERROR;

      return result?SUCCESS:ERROR;
    }

    if("Publish and Syndicate".equals(action)) {
      //If publish succeeds but syndication fails, we'll report and log the error and keep going
      //But if pub fails, we won't want to syndicate.  Hence the follow line
      Boolean result = publishArticles() && syndicateArticles(syndicates);

      if (!setCommonFields())
        return ERROR;

      return result?SUCCESS:ERROR;
    }

    // sort the publishable articles by published date in ascending order
    if ("Sort by Pub Date Asc".equals(action)) {
      orderBy = "pubdate asc";
      if (!setCommonFields())
        return ERROR;

      return SUCCESS;
    }

    // sort the publishable articles by published date in descending order
    if ("Sort by Pub Date Desc".equals(action)) {
      orderBy = "pubdate desc";
      if (!setCommonFields())
        return ERROR;

      return SUCCESS;
    }

    // sort the publishable articles by doi in ascending order
    if ("Sort by DOI Asc".equals(action)) {
      orderBy = "doi asc";

      if (!setCommonFields())
        return ERROR;

      return SUCCESS;
    }

    // sort the publishable articles by doi in descending order
    if ("Sort by DOI Desc".equals(action)) {
      orderBy = "doi desc";

      if (!setCommonFields())
        return ERROR;

      return SUCCESS;
    }

    setCommonFields();
    addActionError("Invalid action received: " + action);

    return ERROR;
  }

  /**
   * Submit for syndication all the FAILED Syndications that were displayed on this page.
   *
   * @return SUCCESS/ERROR
   */
  public String resyndicateFailedArticles() {
    Boolean result = syndicateArticles(resyndicates);
    if (!setCommonFields())
      return ERROR;

    return result?SUCCESS:ERROR;
  }

  /**
   * Queue for syndication the set of user selected articles.
   *
   * @return  true if syndication was successfully queued.
   */
  private boolean syndicateArticles(String[] synArray) {
    if (synArray == null)
      return true;

    Boolean result = true;

    for(String t : synArray) {
      //Create syndication task for this article and these DOIS:
      try {
        String[] values = t.split("::");

        if(values.length != 2) {
          addActionMessage("Can not parse received value:" + t);
        }

        String doi = URLDecoder.decode(values[0], "UTF-8");
        String target = values[1];

        Syndication syndication = syndicationService.syndicate(doi, target);

        if (Syndication.STATUS_FAILURE.equals(syndication.getStatus())) {
          addActionError("Exception: " + syndication.getErrorMessage());
          log.error("Error syndicating articles", syndication.getErrorMessage());
          result = false;
        } else {
          addActionMessage("Syndicated: " + doi + " to " + target);
        }

      } catch(URISyntaxException ex) {
        addActionMessage("Received invalid doi of:" + t.split("::")[0]);
        result = false;
      } catch(UnsupportedEncodingException ex) {
        addActionMessage("EncodingException:" + ex.getMessage());
        log.error("EncodingException", ex);
        result = false;
      }
    }

    return result;
  }

  /**
   * Publish the a set of user selected articles that
   * have already been ingested.
   *
   * @return true if all the articles are published.
   */
  private boolean publishArticles() {
    if (articles == null) {
      addActionError("No articles selected to publish.");
      return false;
    }

    try {
      List<String> msgs = documentManagementService.publish(articles);
      for (String msg : msgs)
        addActionMessage(msg);

      return true;
    } catch (Exception e) {
       addActionError("Exception: " + e);
       log.error("Error publishing archives", e);
       TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
       return false;
    }
  }

 /**
  * Delete arcticle with DOIs specified by the user.
  *
  * @return  true if article successfully deleted.
  */
  private Boolean deleteArticles() {
    if (articles == null) {
      addActionError("No articles selected to delete.");
      return false;
    }

    List<String> msgs = documentManagementService.delete(articles);
    for (String msg : msgs)
      addActionMessage(msg);

    for (String article : articles) {
      try {
        documentManagementService.revertIngestedQueue(article);
      } catch (Exception ioe) {
        log.warn("Error cleaning up spool directories for '" + article +
                 "' - manual cleanup required", ioe);
        addActionMessage("Failed to move " + article + " back to ingestion queue: " + ioe);
      }
    }

    return true;
  }

  /**
   *
   * @param s array of articles to process
   */
  public void setArticles(String[] s) {
    articles = s;
  }

  /**
   * All the individual actions handled by adminTopAction need to
   * provide a common set on information for the ftl to display.
   *
   * @return  true if there was no error when setting the fields
   */
  private boolean setCommonFields() {
    // create a faux journal object for template.  Ensures correct display of page.
    initJournal();

    // catch all Exceptions to keep Admin console active (vs. Site Error)
    try {
      uploadableFiles = documentManagementService.getUploadableFiles();
      autoIngestFiles = documentManagementService.getAutoIngestFiles();

      if (orderBy == null) {
        orderBy = "doi asc";
      }

      // the publishableArticles is ordered by doi in ascending order
      if (orderBy.equals("doi asc") || orderBy.equals("doi desc")) {
        publishableArticles = documentManagementService.getPublishableArticles();
      } else if (orderBy.equals("pubdate asc")) {
        publishableArticles = documentManagementService.getPublishableArticles(true);
      } else if (orderBy.equals("pubdate desc")) {
        publishableArticles = documentManagementService.getPublishableArticles(false);
      }

      orderedPublishableArticles = new ArrayList(publishableArticles.values());

      if (orderBy.equals("doi desc")) {
        Collections.reverse(orderedPublishableArticles);
      } 

      // get the recent article syndication activity for display
      syndications = syndicationService.getFailedAndInProgressSyndications(getCurrentJournal());
      // check whether any of the recent article syndications have a FAILED status. Default to false.
      for (SyndicationDTO syn : syndications) {
        isFailedSyndications = syn.isFailed();
        if (isFailedSyndications)
          break;
      }

      syndicationMap = new HashMap<String, List<SyndicationDTO>>();
      //Map syndications to the publishable articles
      Set<String> keys = publishableArticles.keySet();
      for (String k : keys)
        syndicationMap.put(k, syndicationService.querySyndication(k));

    } catch (Exception e) {
      log.error("Admin console Exception", e);
      addActionError("Exception: " + e);
      return false;
    }
    return true;
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

  @Required
  public SyndicationService getSyndicationService() {
    return syndicationService;
  }

  /**
   * Sets service used to syndicate these articles to external organizations
   *
   * @param  syndicationService The service used to syndicate these articles to
   *   external organizations
   */
  @Required
  public void setSyndicationService(SyndicationService syndicationService) {
    this.syndicationService = syndicationService;
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
    * Gets the collection of AutoIngestFiles files
    *
    * @return List of file names
    */
  public Collection<String> getAutoIngestFiles() {
    return autoIngestFiles;
  }

  /**
   * Get the auto ingest sync state
   * @return a true if auto ingest is in progress.
   **/
  public Boolean getAutoIngesting() {
    return sync;
  }

  /**
    * Gets all of ingested articles that are not published yet, keyed off of their Article IDs.
    *
    * @return Map of articles keyed off of their Article IDs
    */
  public Map<String, Article> getPublishableArticles() {
    return publishableArticles;
  }

  /**
    * Gets all of ingested articles that are not published yet, keyed off of their Article IDs.
    *
    * @return Map of articles keyed off of their Article IDs
    */
  public Map<String, List<SyndicationDTO>> getPublishableSyndications() {
    return syndicationMap;
  }
  /**
   * Form field setter
   * @param a article id
   */
  public void setArticle(String a) {
    article = a;
  }

  /**
   * Form field setter for action
   * The action parameter is the value of the button pressed on the front
   * End, and determines what actions to take
   * @param s the current action
   */
  public void setAction(String s) {
    action = s;
  }

  /**
   * Form field setter
   * @return the current action
   */
  public String getAction() {
    return action;
  }

  /**
   * Form field setter
   * @param files list of files
   */
  public void setFilesToIngest(String[] files) {
    filesToIngest = files;
  }

  /**
   * Set the array of article/syndication options for syndicating articles when those articles
   * are published
   * @param s
   */
  public void setSyndicates(String[] s) {
    syndicates = s;
  }

  /**
   * Set the array of article/syndication options for syndicating articles that have previously
   * had their syndications fail.
   * @param r
   */
  public void setResyndicates(String[] r) {
    resyndicates = r;
  }

  /**
   * Whether there is at least one FAILED syndication in the list returned by the
   * <code>getSyndications()</code> method.
   *
   * @return true if there are any FAILED syndications that will be shown on the page
   */
  public Boolean getIsFailedSyndications() {
    return isFailedSyndications;
  }

  /**
   * Form field setter
   * @param flag true or false
   */
  public void setForce(boolean flag) {
    force = flag;
  }

  /**
   * Get the most recent syndication activity
   * @return a list of syndications
   **/
  public List<SyndicationDTO> getSyndications() {
    return syndications;
  }

  /**
   * Sets the list of syndications to display
   * @param syndications a list of syndications
   */
  public void setSyndications(List<SyndicationDTO> syndications) {
    this.syndications = syndications;
  }

  /**
   * Setter for ambraMailer.
   * @param ambraMailer Value to set for ambraMailer.
   */
  public void setAmbraMailer(AmbraMailer ambraMailer) {
    this.ambraMailer = ambraMailer;
  }

  /**
   * Getter for ordered publishable articles
   * @return ordered publishable articles
   */
  public List<Article> getOrderedPublishableArticles() {
    return orderedPublishableArticles;
  }

  /**
   * Getter for order by publishable articles
   * @return order by publishable articles
   */
  public String getOrderByPublishableArticles() {
    return orderBy;
  }
}