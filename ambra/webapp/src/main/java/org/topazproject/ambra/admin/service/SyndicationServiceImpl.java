/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.admin.service;

import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.queue.MessageService;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Date;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manage the syndication process, including creating and updating Syndication objects, as
 * well as pushing syndication messages to a message queue.
 *
 * @author Scott Sterling
 */
public class SyndicationServiceImpl implements SyndicationService {
  private static final Logger log = LoggerFactory.getLogger(SyndicationServiceImpl.class);

  private SessionFactory sessionFactory;

  private Session session;
  private Configuration configuration;
  private MessageService messageService;

  private URI articleTypeDoNotCreateSyndication;

  private Cache objectCache;

  /**
   * Setter method for Session which will be used for all interactions with the datastore.
   * Injected through Spring.
   * <p/>
   * All Syndication status values are drawn from the appropriately named constants in the
   * Syndication class.
   *
   * @param session OTM session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set OTM session factory. Used to create a Session when invoking method outside of a web request.
   * The SessionFactory is used in the method <code>asynchronousUpdateSyndication</code>.
   * @param sessionFactory OTM session factory
   */
  @Required
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Set the Service which will be used to push syndication messages to the message queue
   * Injected through Spring.
   *
   * @param messageService The Service through which syndication messages
   *   will be pushed to the message queue
   */
  @Required
  public void setMessageService(MessageService messageService) {
    this.messageService = messageService;
  }

  /**
   * Set object cache.
   * We need to manually evict syndication objects from the cache when a message arrives through
   * the method <code>asynchronousUpdateSyndication</code>.
   * @param objectCache Object cache
   */
  @Required
  public void setObjectCache(Cache objectCache) {
    this.objectCache = objectCache;
  }

  /**
   * TODO: The article type(s) which will NOT be syndicated should be managed in a config list which
   * TODO:   defines which types should NOT be syndicated for EACH syndication target, probably
   * TODO:   contained in the config property: ambra.services.syndications.syndication.
   * TODO:   For instance, images do NOT go to PMC, but DO go to CrossRef.
   *
   * @param articleTypes All the article types of the Article for which Syndication
   *   objects are being created
   * @return Whether to create a Syndication object for this Article and this syndication target
   */
  private Boolean isCreateSyndicationForArticleType(Set<URI> articleTypes) {
    if (articleTypeDoNotCreateSyndication == null) {
      try {
        articleTypeDoNotCreateSyndication = new URI("http://rdf.plos.org/RDF/articleType/Issue%20Image");
      } catch (URISyntaxException e) {
        log.warn("Failure to create URI from String: http://rdf.plos.org/RDF/articleType/Issue%20Image");
        return true;
      }
    }
    return !(articleTypes != null && articleTypes.contains(articleTypeDoNotCreateSyndication));
  }

  /**
   * If a Syndication object exists for this <code>articleId</code> and
   * <code>syndicationTarget</code>, then return that object.  Otherwise, create a new
   * Syndication object using these two parameters.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @return The existent Syndication (if one exists for this <code>articleId</code> and
   *   <code>syndicationTarget</code>), else create one (using these two parameters) and return it
   */
  public Syndication getSyndication(String articleId, String syndicationTarget)
                                   throws URISyntaxException {
    Syndication syn = querySyndication(articleId, syndicationTarget, session);
    return syn != null ? syn :  new Syndication(articleId, syndicationTarget);
  }

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  If no such Syndication exists, then create it.
   * <ul>
   *   <li>For any <code>status</code> other than <i>pending</i> (e.g., failure, success, etc)
   *     the Syndication object will be updated or created with the given values</li>
   *   <li>If the <code>status</code> is <i>pending</i> (i.e., the syndication process has not yet
   *     been intitated) <strong>and</strong> no Syndication object exists for this
   *     <code>articleId</code> and <code>syndicationTarget</code>, then a <b>new</b>
   *     Syndication object will be created and returned</li>
   *   <li>If the <code>status</code> is <i>pending</i> <strong>and</strong> a Syndication object
   *     already exists for this <code>articleId</code> and <code>syndicationTarget</code>,
   *     then no Syndication object will be created or updated.
   *     The existing Syndication object will be returned</li>
   * </ul>
   * Whenever the status is updated, the <i>Syndication.statusTimestamp</i> property
   *   will be updated to the current date/time.
   *
   * @param session OTM session
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication.
   *   A null in this field will <strong>not</strong> update the errorMessage of this Syndication
   * @return The Syndication that matches the <code>articleId</code> and
   *   <code>syndicationTarget</code> parameters
   * @throws URISyntaxException if articleId is malformed
   */
  private static Syndication updateSyndication(Session session, String articleId,
                            String syndicationTarget, String status,
                            String errorMessage) throws URISyntaxException {
    Syndication syn = querySyndication(articleId, syndicationTarget, session);
    if (syn == null) {  //  No existing Syndication, so create one.
      syn = new Syndication(articleId, syndicationTarget);
      syn.setStatus(status);
      syn.setStatusTimestamp(new Date());
      syn.setErrorMessage(errorMessage);
    } else {
      //  A Syndication already exists, so update it if Status is NOT "pending".
      if (Syndication.STATUS_PENDING.equals(status)) {
        log.info("Attempted to update Syndication object to a status of PENDING.  No update was "
          + "performed. ArticleId = " + articleId + " and syndicationTarget = " + syndicationTarget);
      } else {
        syn.setStatus(status);
        syn.setStatusTimestamp(new Date());
        if (errorMessage != null) {
          syn.setErrorMessage(errorMessage);
        }
      }
    }

    session.saveOrUpdate(syn);
    return syn;
  }

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  Will create a new "pending" Syndication.
   * <p/>
   * ATTENTION: this method is designed to be invoked outside of "request" scope.
   * It manually creates it's own OTM session.  Calling this method when a Session has already been
   * loaded from Spring (e.g., calling this method through one of the Action classes)
   * results in a conflict between the existing Session object and the Session object that this
   * method tries to create.  In practice, this results in the application "hanging".
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
   */
  public Syndication asynchronousUpdateSyndication(String articleId, String syndicationTarget,
                     String status, String errorMessage) throws URISyntaxException {

    Syndication syndication = null;
    Session session = null;
    Transaction transaction = null;

    try {
      /*
      No interceptors will be fired. Many interceptors have @Transactional on them and will cause error when
      invoked outside of request scope.
      */
      session = sessionFactory.openSession();
      transaction = session.beginTransaction();
      syndication = updateSyndication(session, articleId, syndicationTarget, status, errorMessage);
      // Have to manually evict object from cache since invalidators are not firing
      objectCache.remove(syndication.getId().toString());
      transaction.commit();
      transaction = null;
    } finally {
      try {
        if (transaction != null)
          transaction.rollback();
      } catch (Throwable t) {
        log.warn("Error in rollback", t);
      }
      try {
        if (session != null)
          session.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }

    return syndication;
  }

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  If no such Syndication exists, then create it.
   * <ul>
   *   <li>For any <code>status</code> other than <i>pending</i> (e.g., failure, success, etc)
   *     the Syndication object will be updated or created with the given values</li>
   *   <li>If the <code>status</code> is <i>pending</i> (i.e., the syndication process has not yet
   *     been intitated) <strong>and</strong> no Syndication object exists for this
   *     <code>articleId</code> and <code>syndicationTarget</code>, then a <b>new</b>
   *     Syndication object will be created and returned</li>
   *   <li>If the <code>status</code> is <i>pending</i> <strong>and</strong> a Syndication object
   *     already exists for this <code>articleId</code> and <code>syndicationTarget</code>,
   *     then no Syndication object will be created or updated.
   *     The existing Syndication object will be returned</li>
   * </ul>
   * Whenever the status is updated, the <i>Syndication.statusTimestamp</i> property
   *   will be updated to the current date/time.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication.
   *   A null in this field will <strong>not</strong> update the errorMessage of this Syndication
   * @return The Syndication that matches the <code>articleId</code> and
   *   <code>syndicationTarget</code> parameters
   * @throws URISyntaxException if articleId is malformed
   */
  @Transactional(rollbackFor = {Throwable.class})
  public Syndication updateSyndication(String articleId, String syndicationTarget, String status,
                                       String errorMessage) throws URISyntaxException {

    return updateSyndication(session, articleId, syndicationTarget, status, errorMessage);
  }

  /**
   * For the Article indicated by <code>articleId</code>, create a new Syndication object for each
   * possible syndication target which does not already have a Syndication object.
   * Return the complete list of Syndication objects for this Article.
   * <p/>
   * If a Syndication object for a given syndication target already exists for this Article,
   * then the datastore will not be updated for that Syndication object.
   * This silent failure mode is useful during the re-ingestion of any
   * Article which was previously published and syndicated.
   * <p/>
   * This process is accomplished by looping over all possible targets and calling
   * "updateSyndication()" with status "pending" for each one.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The complete list of Syndication objects for this Article
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public List<SyndicationDTO> createSyndications(String articleId) throws URISyntaxException {
    List<HierarchicalConfiguration> allSyndicationTargets = ((HierarchicalConfiguration)
        configuration).configurationsAt("ambra.services.syndications.syndication");
    if (allSyndicationTargets == null || allSyndicationTargets.size() < 1) { // Should never happen.
      log.warn("There are no Syndication Targets defined in the property: " +
          "ambra.services.syndications.syndication so no Syndication objects were created for " +
          "the article with ID = " + articleId);
      return querySyndication(articleId);
    }

    //  Loop over all possible Syndication Targets, creating one Syndication for each target.
    for (HierarchicalConfiguration synTarget : allSyndicationTargets) {
      String target = synTarget.getString("[@target]");

      if (isCreateSyndicationForArticleType(
          (session.get(Article.class, articleId).getArticleType()))) {
        updateSyndication(session, articleId, target, Syndication.STATUS_PENDING, null);
      }
    }
    // The updateSyndication method does "session.saveOrUpdate()" for each Syndication, so all
    // possible Syndications should now exist for this Article.
    return querySyndication(articleId);
  }

  /**
   * Get Syndications (from the current journal) that each have a <code>status</code> of either
   * <i>failed</i> or <i>in progress</i> and a <code>statusTimestamp</code> within the past number
   * of days defined by the configuration property
   * <code>ambra.virtualJournals.JOURNAL_KEY.syndications.display.numDaysInPast</code>,
   * where <i>JOURNAL_KEY</i> is the <code>journalKey</code> parameter.  By default, a
   * <i>failed</i> or <i>in progress</i> Syndication can be up to 30 days old and still appear in
   * this list.
   *
   * @param journalKey Indicates which journal configuration is to be used when determining
   *   how many days in the past the oldest Syndications can be.  This property is passed in because
   *   the Action class (which calls this method) has easy access to this value, while this Service
   *   class does not
   * @return Syndications which have a <code>status</code> of either <i>failed</i> or
   *   <i>in progress</i> and a <i>statusTimestamp</i> up to a certain number of days in the past.
   *   The SyndicationDTO class encapsulates the Syndication class and adds additional properties
   * @throws OtmException Thrown by any problems encountered during the query
   */
  @Transactional
  public List<SyndicationDTO> getFailedAndInProgressSyndications(
         String journalKey) throws OtmException {
    Integer numDaysInPast = configuration.getInteger(
        "ambra.virtualJournals." + journalKey + ".syndications.display.numDaysInPast", 30);

    // The most recent midnight.  No need to futz about with exact dates.
    Calendar start = Calendar.getInstance();
    start.set(Calendar.HOUR, 0);
    start.set(Calendar.MINUTE, 0);
    start.set(Calendar.SECOND, 0);
    start.set(Calendar.MILLISECOND, 0);

    Calendar end = (Calendar) start.clone(); // The most recent midnight (last night)

    start.add(Calendar.DATE, -(numDaysInPast));
    end.add(Calendar.DATE, 1); // Include everything that happened today.

    // Article is included in the query so that the journal filter will be invoked,
    //   guaranteeing that only Syndications for Articles in the current journal will be returned.
    StringBuilder qry = new StringBuilder();
    qry.append("select syn, timestamp, articleId from Syndication syn, Article a ")
        .append("where timestamp := syn.statusTimestamp and articleId := a.id ")
        .append("and syn.articleId = articleId ")
        .append("and ( syn.status = '").append(Syndication.STATUS_IN_PROGRESS).append("'")
        .append("  or syn.status = '").append(Syndication.STATUS_FAILURE).append("' ) ")
        .append("and gt(timestamp, :sd) and lt(timestamp, :ed) ")
        .append("order by articleId asc;");

    Results r = session.createQuery(qry.toString())
        .setParameter("sd", start).setParameter("ed", end).execute();

    List<SyndicationDTO> syndications = new LinkedList<SyndicationDTO>();
    while (r.next()) {
      syndications.add(new SyndicationDTO((Syndication)r.get(0)));
    }

    return syndications;
  }

  /**
   * Get the list of Syndication objects for this <code>articleId</code>.
   * If there are no Syndications for this articleId, then return an empty List.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The List of Syndications for this <code>articleId</code>.
   *   If there are no Syndications for this articleId, then return an empty List
   */
  @Transactional(readOnly = true)
  public List<SyndicationDTO> querySyndication(String articleId) throws URISyntaxException {
    URI articleURI = new URI(articleId);
    Results r = session.createQuery("select syn from Syndication syn where syn.articleId = :articleId;")
        .setParameter("articleId", articleURI)
        .execute();

    List<SyndicationDTO> result = new LinkedList<SyndicationDTO>();
    while(r.next()) {
      SyndicationDTO syn = new SyndicationDTO((Syndication)r.get(0));
      result.add(syn);
    }
    return result;
  }

  /**
   * Get the Syndication object for this <code>articleId</code> and <code>syndicationTarget</code>,
   * if it exists, else return null;
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param session OTM session
   * @return The existent Syndication, if one exists for this <code>articleId</code> and
   *   <code>syndicationTarget</code>, else null
   * @throws URISyntaxException If articleId is malformed
   */
  private static Syndication querySyndication(String articleId, String syndicationTarget,
                                              Session session) throws URISyntaxException {
    //  Compose a new Syndication object so that the ID will be created in the usual way
    URI synURI = new URI(Syndication.makeSyndicationId(articleId, syndicationTarget));

    Results r = session.createQuery("select syn from Syndication syn where syn.id = :id;")
        .setParameter("id", synURI)
        .execute();

    return r.next() ? (Syndication)r.get(0) : null;
  }

  /**
   * Setter method for configuration. Injected through Spring.
   * <p/>
   * Response queues are obtained from configuration file.
   * Beans that consume response queue are named &lt;target_lowercase&gt;ResponseConsumer and should
   * already be defined in Spring context.
   * Example: for PMC, the consumer bean is named <i>pmcResponseConsumer</i>.
   * <p/>
   * In addition to the normal route, two routes for testing are configured for each target:
   * <ul>
   *    <li>seda:test&lt;target&gt;Ok - loopback route that always returns success.
   *      Example: to simulate a successful queue submission for PMC, send a message to the
   *      queue named <i>seda:testPMCOk</i></li>
   *    <li>seda:test&lt;target&gt;Fail - loopback route that always returns failure.
   *      Example: to simulate a failed queue submission for PMC, send a message to the
   *      queue named <i>seda:testPMCFail</i></li>
   * </ul>
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Send a message to the message queue indicating that the Article identified by
   * <code>articleId</code> should be syndicated to the syndication target identified by
   * <code>syndicationTarget</code>.
   * <p/>
   * If the Article does not exist or does not know the name of its archive file,
   * then this method throws an exception and the corresponding Syndication object is not updated.
   * <p/>
   * If the message is successfully pushed to the message queue, then the corresponding Syndication
   * object will have its status set to "in progress".  If the message cannot be pushed to the
   * message queue, then the corresponding Syndication object will have its status set to
   * "failure".
   *
   * @param articleId The ID for the Article which will be syndicated to the
   *   <code>syndicationTarget</code>
   * @param syndicationTarget The syndication target to which will be sent the Article designated by
   *   <code>articleId</code>
   * @return The Syndication object which matches the <code>articleId</code> and
   *   <code>syndicationTarget</code> parameters.  Contains the latest status information.
   * @throws URISyntaxException if <code>articleId</code> cannot be translated into URI
   */
  @Transactional(rollbackFor = { Throwable.class })
  public Syndication syndicate(String articleId, String syndicationTarget) throws URISyntaxException {
    try {
      Article article = session.get(Article.class, articleId);
      if (article == null) {
        throw new Exception("The article with ID = " + articleId + " has been deleted."
            + " Please reingest this article before attempting to syndicate2 it.");
      }

      if (article.getArchiveName() == null || article.getArchiveName().trim().equals("")) {
        throw new Exception("The article with ID = " + articleId
            + " does not have an archive file associated with it.");
      }

      //  Send message.
      messageService.sendSyndicationMessage(syndicationTarget, articleId, article.getArchiveName());

      Syndication syndication = updateSyndication(articleId, syndicationTarget,
          Syndication.STATUS_IN_PROGRESS, null);
      syndication.setSubmissionCount(syndication.getSubmissionCount() + 1);
      session.saveOrUpdate(syndication);

      log.info("Successfully sent a Message to plos-queue for Syndication: "
          + syndication.toString() + " and articleId = " + articleId);

      return syndication;
    } catch (Exception e) {
      log.warn("Error syndicating articleId " + articleId + " to " + syndicationTarget, e);
      return updateSyndication(articleId, syndicationTarget,
          Syndication.STATUS_FAILURE, e.getMessage());
    }
  }
}