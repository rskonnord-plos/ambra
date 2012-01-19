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
import org.topazproject.ambra.models.Article;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

/**
   * Manage the syndication process.
   * <p/>
   * Syndication is, typically, the duplication of an Article in a remote repository.
   *
   * @author Scott Sterling
   */
  public interface SyndicationService {

  /**
   * Get the set of Syndication objects for this <code>articleId</code> ,
   * if it exists, else return null;
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The set of Syndications (if one exists for this <code>articleDoi</code> else null
   */
  public List<SyndicationDTO> querySyndication(String articleId) throws URISyntaxException;
    
  /**
   * Get a Syndication object for this <code>articleId</code> and <code>syndicationTarget</code>
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @return A Syndication object for this <code>articleId</code> and <code>syndicationTarget</code>
   */
  public Syndication getSyndication(String articleId, String syndicationTarget)
                                   throws URISyntaxException;

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  Will create a new "pending" Syndication.
   *
   * This method is only for asynchronouse callbacks. Not to be called in actions. It manually creates
   * it's session and transaction.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
   */
  public Syndication asynchronousUpdateSyndication(String articleId, String syndicationTarget, String status,
                     String errorMessage) throws URISyntaxException;

  /**
   * Update the Syndication object specified by the <code>articleId</code>
   * and <code>syndicationTarget</code> parameters.  Will create a new "pending" Syndication.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @param syndicationTarget The organization to which this Article was (or will be) syndicated
   * @param status The current status of this syndication (e.g., pending, failure, success, etc)
   * @param errorMessage Any failure during the process of updating this Syndication
   */
  public Syndication updateSyndication(String articleId, String syndicationTarget, String status,
                     String errorMessage) throws URISyntaxException;

  /**
   * For the Article indicated by <code>articleId</code>, ensure that there is one Syndication
   * object for each of the possible syndication targets.
   *
   * @param articleId The unique identifier for the Article which was (or is to be) syndicated
   * @return The complete list of Syndication objects for this Article
   */
  public List<SyndicationDTO> createSyndications(String articleId) throws URISyntaxException;

  /**
   * Get Syndications (from the current journal) that each have a <code>status</code> of either
   * <i>failed</i> or <i>in progress</i> and a <code>statusTimestamp</code> within the past number
   * of days defined by the configuration property
   * <code>ambra.virtualJournals.JOURNAL_KEY.syndications.display.numDaysInPast</code>,
   * where <i>JOURNAL_KEY</i> is the <code>journalKey</code> parameter.
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
  public List<SyndicationDTO> getFailedAndInProgressSyndications(
         String journalKey) throws OtmException;

  /**
   * Trigger the process which will syndicate this Article to this syndication target.
   *
   * @param articleId The Article which will be syndicated
   * @param syndicationTarget The target to which this Article will be syndicated
   * @return The Syndication object matching this Article and this syndication target
   * @throws org.topazproject.ambra.ApplicationException While attempting to send a message
   */
  public Syndication syndicate(String articleId, String syndicationTarget) throws Exception;

  /**
   * This inner class is meant to be used in the display layer, particluarly in cases where
   * Syndications from diverse Articles are aggregated together without those Article objects.
   * Typically, a Syndication collection is managed by its Article.
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
      return syn != null ? syn.getSubmissionCount() : null;
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