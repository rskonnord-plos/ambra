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
import org.topazproject.otm.OtmException;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Date;

/**
 * Manage the syndication process, including creating and updating Syndication objects, as
 * well as pushing syndication messages to a message queue.
 *
 * @author Scott Sterling
 */
public interface SyndicationService {

  /**
   * Get the list of Syndication objects for this <code>articleId</code>.
   * If there are no Syndications for this articleId, then return an empty List.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The List of Syndications for this <code>articleId</code>.
   *   If there are no Syndications for this articleId, then return an empty List
   * @throws URISyntaxException if articleId is malformed.
   */
  public List<SyndicationDTO> querySyndication(String articleId) throws URISyntaxException;
    
  /**
   * If a Syndication object exists for this <code>articleId</code> and
   * <code>syndicationTarget</code>, then return that object.  Otherwise, create a new
   * Syndication object using these two parameters.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @return The existent Syndication (if one exists for this <code>articleId</code> and
   *   <code>syndicationTarget</code>), else create one (using these two parameters) and return it
   * @throws URISyntaxException if articleId is malformed.
   */
  public Syndication getSyndication(String articleId, String syndicationTarget)
                                   throws URISyntaxException;

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
   * @return Syndication objects for provided articleId and target.
   * @throws URISyntaxException if articleId is malformed.
   */
  public Syndication asynchronousUpdateSyndication(String articleId, String syndicationTarget, String status,
                     String errorMessage) throws URISyntaxException;

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
  public Syndication updateSyndication(String articleId, String syndicationTarget, String status,
                     String errorMessage) throws URISyntaxException;

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
   * @throws URISyntaxException if articleId is malformed.
   */
  public List<SyndicationDTO> createSyndications(String articleId) throws URISyntaxException;

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
  public List<SyndicationDTO> getFailedAndInProgressSyndications(
         String journalKey) throws OtmException;

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
  public Syndication syndicate(String articleId, String syndicationTarget) throws URISyntaxException;

  /**
   * This inner class is meant to be used in the display layer.
   */
  public static class SyndicationDTO implements Serializable {
    private Syndication syn;

    public SyndicationDTO(Syndication syn) {
      this.syn = new Syndication(syn);
    }

    public URI getArticleURI() {
      return syn != null ? syn.getArticleId() : null;
    }

    public String getArticleId() {
      return syn != null ? syn.getArticleId().toString() : null;
    }

    public URI getSyndicationURI() {
      return syn != null ? syn.getId(): null;
    }

    public String getSyndicationId() {
      return syn != null ? syn.getId().toString() : null;
    }

    public String getStatus() {
      return syn != null ? syn.getStatus() : null;
    }

    public String getTarget() {
      return syn != null ? syn.getTarget() : null;
    }

    public int getSubmissionCount() {
      return syn != null ? syn.getSubmissionCount() : 0;
    }

    public Date getStatusTimestamp() {
      return syn != null ? syn.getStatusTimestamp() : null;
    }

    public Date getSubmitTimestamp() {
      return syn != null ? syn.getSubmitTimestamp() : null;
    }

    public Syndication getSyndication() {
      return syn;
    }

    public String getErrorMessage() {
      return syn.getErrorMessage();
    }

    public Boolean complete() {
      return (syn.getStatus().equals(Syndication.STATUS_SUCCESS) ||
              syn.getStatus().equals(Syndication.STATUS_FAILURE));
    }

    public Boolean isPending() {
        return syn.getStatus().equals(Syndication.STATUS_PENDING);
    }

    public Boolean isInProgress() {
       return syn.getStatus().equals(Syndication.STATUS_IN_PROGRESS);
    }

    public Boolean isSuccess() {
      return syn.getStatus().equals(Syndication.STATUS_SUCCESS);
    }

    public Boolean isFailed() {
      return syn.getStatus().equals(Syndication.STATUS_FAILURE);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      SyndicationDTO that = (SyndicationDTO) o;

      if (!syn.equals(that.syn))
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      return syn.hashCode();
    }
  }
}