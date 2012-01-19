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
 * Manage the syndication process.
 * <p/>
 * Syndication is, typically, the duplication of an Article in a remote repository.
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
   * Setter method for Session which will be used for all interaction with the datastore.
   * Injected through Spring.
   *
   * @param session OTM session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set OTM session factory. It is used to create session when invoking method outside of web request.
   * @param sessionFactory OTM session factory
   */
  @Required
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Setter method for MessageSender which will be used to push messages to the Camel queueing
   * system. Injected through Spring.
   *
   * @param messageService The interface through which messages will be pushed to the queueing system
   */
  @Required
  public void setMessageService(MessageService messageService) {
    this.messageService = messageService;
  }

  /**
   * Set object cache. We need to manually evict syndication object from cache when message arrives.
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
   * @param articleTypes All the article types for the article that is having its syndication
   *   objects being created
   * @return Whether to create a Syndication object for this Article and the syndication target
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
   * Syndication object using the <code>syndicationTarget</code> parameter.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @return The existent Syndication (if one exists for this <code>articleId</code> and
   *   <code>syndicationTarget</code>), else create one with <code>syndicationTarget</code>
   *   and return it
   */
  public Syndication getSyndication(String articleId, String syndicationTarget)
                                   throws URISyntaxException {
    Syndication syn = querySyndication(articleId, syndicationTarget, session);
    return syn != null ? syn :  new Syndication(articleId, syndicationTarget);
  }

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.
   * <ul>
   *   <li>For any <code>status</code> other than <i>pending</i> (e.g., failure, success, etc)
   *     the Syndication object for this <code>articleId</code> and <code>syndicationTarget</code>
   *     will be updated (if it exists) or created (if not) with the given values</li>
   *   <li>If the <code>status</code> is <i>pending</i> (i.e., the syndication process has not yet
   *     been intitated) and no Syndication object exists for this <code>articleId</code>
   *     and <code>syndicationTarget</code>, then a <b>new</b> Syndication object will be created
   *     and returned</li>
   *   <li>If the <code>status</code> is <i>pending</i> and a Syndication object already exists
   *     for this <code>articleId</code> and <code>syndicationTarget</code>, then no Syndication
   *     object will be created or updated.  The existing Syndication object will be returned</li>
   * </ul>
   * The <i>Syndication.statusTimestamp</i> property will be updated to the current date/time.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
   * @return The Syndication that matches the <code>articleId</code> and
   *   <code>syndicationTarget</code> parameters
   * @throws URISyntaxException
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
        syn.setErrorMessage(errorMessage);
      }
    }

    session.saveOrUpdate(syn);
    session.flush();
    return syn;
  }

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  Will create a new "pending" Syndication.
   * <p/>
   * This method gets the <code>article</code> object from the <code>session</code> (using the
   * <code>articleId</code> parameter, of course) and then calls the other
   * <code>updateSyndication</code> method.
   *
   * ATTENTION: this method is designed to be invoked outside of "request" scope. It manually creates it's
   * own OTM session.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
   */
  public Syndication asynchronousUpdateSyndication(String articleId, String syndicationTarget, String status,
                     String errorMessage) throws URISyntaxException {

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
   * and <code>syndicationTarget</code> parameters.  Will create a new "pending" Syndication.
   * <p/>
   * This method gets the <code>article</code> object from the <code>session</code> (using the
   * <code>articleId</code> parameter, of course) and then calls the other
   * <code>updateSyndication</code> method.
   *
   * ATTENTION: this method is designed to be invoked outside of "request" scope. It manually creates it's
   * own OTM session.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
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
   *   The SyndicationDTO class encapsulates the Syndication class and adds additional
   *   properties
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
    qry.append("select syn, timestamp from Syndication syn, Article a ")
        .append("where timestamp := syn.statusTimestamp and syn.articleId = a.id ")
        .append("and gt(timestamp, :sd) and lt(timestamp, :ed) ")
        .append("order by timestamp desc;");

    Results r = session.createQuery(qry.toString())
        .setParameter("sd", start).setParameter("ed", end).execute();

    List<SyndicationDTO> syndications = new LinkedList<SyndicationDTO>();
    while (r.next()) {
      Syndication s = (Syndication)r.get(0);
      if (s != null &&
         (s.getStatus().equals(Syndication.STATUS_FAILURE) ||
        s.getStatus().equals(Syndication.STATUS_IN_PROGRESS)))
      syndications.add(new SyndicationDTO(s));
    }

    return syndications;
  }

  /**
   * Trigger the process which will syndicate this Article to this syndication target.
   * If the syndication process is successfully begun, this method sets the
   * <code>Syndication.status</code> property (of the Syndication object matching this Article
   * and this syndication target) to the "in progress" constant.
   * <p/>
   * If the syndication process cannot be started, this method sets <code>status</code>
   * to the "failure" constant.
   * Since the <code>status</code> is set, the <code>statusTimestamp</code> will also be set.
   * <p/>
   * Always increment <code>syndication.submissionCount</code>.
   * <p/>
   * If an Exception is thrown, then the Syndication object is <strong>not</strong> updated.
   *
   * @param articleId The Article which will be syndicated
   * @param syndicationTarget The target to which this Article will be syndicated
   * @return The Syndication object matching this Article and this syndication target with updated
   *   <code>submissionCount</code>, <code>status</code>, and <code>statusTimestamp</code>
   * @throws Exception While attempting to push a message to MessageService
   * @throws URISyntaxException While turning the <code>articleId</code> into a URI
   */
  @Transactional(rollbackFor = { Throwable.class })
  public Syndication syndicate(String articleId, String syndicationTarget) throws Exception {
    Article article = session.get(Article.class, articleId);
    if (article == null) {
      throw new Exception("The article with ID = " + articleId + " has been deleted."
          + " Please reingest this article before attempting to syndicate it.");
    }

    if (article.getArchiveName() == null || article.getArchiveName().trim().equals("")) {
      throw new Exception("The article with ID = " + articleId + " does not have an archive file associated with it.");
    }

    Syndication syndication = getSyndication(articleId, syndicationTarget);

    syndication.setSubmissionCount(syndication.getSubmissionCount() + 1);

    // Send the message.  Always sets syndication.status and syndication.statusTimestamp.
    try {
      // Status and timeStamp may be changed by the call to messageService.sendSyndicationMessage()
      syndication.setStatus(Syndication.STATUS_IN_PROGRESS);
      syndication.setStatusTimestamp(new Date());

      //  Send message.
      messageService.sendSyndicationMessage(
          syndication.getTarget(), articleId, article.getArchiveName());

      log.info("Successfully sent a Message to plos-queue for Syndication: "
          + syndication.toString() + " and articleId = " + articleId);

      session.saveOrUpdate(syndication);
      session.flush();
      return syndication;

    } catch (Exception e) {
      log.warn("Failed to syndicate article " + articleId + " to target " + syndicationTarget
          + " because " + e.getMessage());
      throw e;
    }
  }

  /**
   * Get the list of Syndication objects for this <code>articleId</code>.
   * If there are no Syndications with this articleId, then return an empty List.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The List of Syndications (if one exists for this <code>articleDoi</code> else null
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
   * @return The existent Syndication (if one exists for this <code>articleDoi</code> and
   *   <code>syndicationTarget</code>), else null
   * @throws URISyntaxException If articleId is malformed
   */
  private static Syndication querySyndication(String articleId, String syndicationTarget, Session session)
      throws URISyntaxException {
    //  Compose a new Syndication object so that the ID will be created in the usual way
    URI synURI = new URI(Syndication.makeSyndicationId(articleId, syndicationTarget));

    Results r = session.createQuery("select syn from Syndication syn where syn.id = :id;")
        .setParameter("id", synURI)
        .execute();

    return r.next() ? (Syndication)r.get(0) : null;
  }

  /**
   * <p>
   * Setter method for configuration. Injected through Spring.
   * </p>
   * <p>
   * Response queues are obtained from configuration file.
   * Beans that consume response queue are named <target_lowercase>ResponseConsumer and should already
   * be defined in Spring context. For examle for PMC, consumer bean is named "pmcResponseConsumer".
   * </p>
   * <p>
   * In addition to normal route, two routes for testing are configuret for each target:
   * <ol>
   *    <li>direct:test<target>Ok - loopback route that always returs success.</li>
   *    <li>direct:test<target>Fail - loopback route that always returns failuer.</li>
   * </ol>
   * </p>
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
