/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

package org.topazproject.ambra.models;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

import java.util.Date;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Data about the process of submitting one Article to one Syndication Target.
 * For instance: The editorial team submits a newly-published Article to PubMed Central.
 * <p/>
 * Syndication is, essentially, the duplication of an Article (in whole or in part)
 * into a remote repository.
 * <p/>
 * Objects of this type are used to track progression of the syndication process and, when
 * that process is complete, are retained in the datastore to indicate success or failure.
 * 
 * @author Scott Sterling
 */
@Entity(types = {"plos:Syndication"}, graph = "ri")
@UriPrefix("plos:syndication/")
public class Syndication implements Serializable {
  private static final long serialVersionUID = -1774305201327263084L;

  /**
   * This Article has been published, but has not yet been submitted to this syndication target.
   */
  public static final String STATUS_PENDING = "PENDING";
  /**
   * This Article has been submitted to this syndication target, but the process is not yet complete.
   */
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  /**
   * This Article has been successfully submitted to this syndication target.
   */
  public static final String STATUS_SUCCESS = "SUCCESS";
  /**
   * This Article was submitted to this syndication target, but the process failed.
   * The reason for this failure should be written into the <i>errorMessage</i> variable.
   */
  public static final String STATUS_FAILURE = "FAILURE";

  private static final String IFS="/";

  private URI    id; // Unique ID for mangement in the datastore.  Format is articleId:::target
  private URI    articleId; // Unique ID for the article that this Syndication is associated to.
  private String status; // The current status of the syndication submission.
  private String target; // The organization to which this Article was syndicated.
  private int    submissionCount = 0; // Number of times this Article was submitted to this target.
  private Date   statusTimestamp; // When the Status was most recently changed to its current value.
  private Date   submitTimestamp; // When the Article was initially submitted to syndication.
  private String errorMessage; // If there is a failure, put the reason for that failure here.

    /**
     * Create a unique identifier for for a syndication based upon
     * the articleId and  target name.
     * 
     * @param articleId
     * @param target
     * @return  syndication Id string.
     */
  public static String makeSyndicationId(String articleId, String target) {
    StringBuilder id = new StringBuilder();
    return id.append(articleId).append(IFS).append(target).toString();
  }

  /**
   * 
   */
  public Syndication() {
  }

  /*
   * Contructor to make a new copy of the object
   *
   * @param syn the syndication object to copy.
   */
  public Syndication(Syndication syn) {
    this.id = syn.id;
    this.articleId = syn.articleId;
    this.status = syn.status;
    this.target =  syn.target;
    this.submissionCount =  syn.submissionCount;
    this.statusTimestamp =  syn.statusTimestamp;
    this.submitTimestamp = syn.submitTimestamp;
    this.errorMessage = syn.errorMessage;
  }
    
  /**
   * Basic constructor which sets all the variables.
   *
   * @param articleId The ID of the Article to which this Syndication is attached.
   *   Not stored; used only in composing the ID for this Syndication object
   * @param status The current status of the syndication submission
   * @param target The organization to which this Article was syndicated.
   *   Used in composing the ID for this Syndication object
   * @param submissionCount The number of times this Article was submitted to this target
   * @param statusTimestamp When the Status was most recently changed to it's current value
   * @param submitTimestamp When the Article was initially submitted to syndication
   * @param errorMessage If there is a failure, put the reason for that failure here
   * @throws java.net.URISyntaxException
   */
  public Syndication(String articleId, String status, String target, int submissionCount,
                     Date statusTimestamp, Date submitTimestamp, String errorMessage)
                     throws URISyntaxException {
    String newId = makeSyndicationId(articleId, target);
    this.id = new URI(newId);
    this.articleId = new URI(articleId);
    this.status = status;
    this.target = target;
    this.submissionCount = submissionCount;
    this.statusTimestamp = statusTimestamp;
    this.submitTimestamp = submitTimestamp;
    this.errorMessage = errorMessage;
  }

  /**
   * Simplified constructor which creates a Syndication object indicating that this
   * Article has not yet been submitted to this syndication Target.
   * For instance, immediately after an Article has been published, but before the Article has
   * been submitted for syndication.
   *
   * @param articleId The ID of the Article to which this Syndication is attached.
   *   Not stored; used only in composing the ID for this Syndication object
   * @param target The organization to which this Article will be syndicated
   * @throws java.net.URISyntaxException
   */
  public Syndication(String articleId, String target) throws URISyntaxException {
    String newId = makeSyndicationId(articleId, target);
    this.id = new URI(newId);
    this.articleId = new URI(articleId);
    this.status = STATUS_PENDING;
    this.target = target;
    this.submissionCount = 0; // The Article has not yet been submitted
    this.statusTimestamp = new Date();
    this.submitTimestamp = null; // The Article has not yet been submitted
    this.errorMessage = null; // No submission, so no chance for a failure
  }

  
  /**
   * Get the unique ID used to manage this object in the datastore.  Constructors for this class
   * create an ID with the format <code>articleId:::target</code>
   *
   * @return The unique ID for this object, usually in the format <code>articleId:::target</code>
   */
  public URI getId() {
    return id;
  }

  /**
   * Set the unique ID used to manage this object in the datastore.  Constructors for this class
   * create an ID with the format <code>articleId:::target</code>
   * so it is recommended that all Syndication IDs follow this format.
   *
   * @param id The unique ID for this object, recommended to be in the format
   *   <code>articleId:::target</code>
   */
  @Id
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get the unique ID for the Article that was syndicated with this Syndication object.
   *
   * @return The unique ID for the Article associated to this Syndication object
   */
  public URI getArticleId() {
    return articleId;
  }

  /**
   * Set the unique ID for the Article that was syndicated with this Syndication object.
   *
   * @param articleId The unique ID for the Article associated to this Syndication object
   */
  @Predicate
  public void setArticleId(URI articleId) {
    this.articleId = articleId;
  }

  /**
   * Get the current state of this syndication submission.  Should always be one of the
   * <code>STATUS_</code> constants in this class.
   *
   * @return The current state of this syndication submission
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the current state of this syndication submission.  Should always be one of the
   * <code>STATUS_</code> constants in this class.
   *
   * @param status The current state of this syndication submission
   */
  @Predicate
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Get the organization to which this Article was submitted for syndication.
   *
   * @return The organization to which this Article was submitted for syndication
   */
  public String getTarget() {
    return target;
  }

  /**
   * Set the organization to which this Article was submitted for syndication.
   *
   * @param target The organization to which this Article was submitted for syndication
   */
  @Predicate
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Get the number of times this Article was submitted to this syndication Target.
   * Each end-to-end submission counts as one "attempt".
   * Multiple programatic attempts to FTP an Article (as part of one submission attempt)
   * do <b>not</b> count multiple times.
   *
   * @return The number of times this Article was submitted to this syndication Target
   */
  public int getSubmissionCount() {
    return submissionCount;
  }

  /**
   * Set the number of times this Article was submitted to this syndication Target
   * Each end-to-end submission counts as one "attempt".
   *
   * @param submissionCount Number of times this Article was submitted to this syndication Target
   */
  @Predicate
  public void setSubmissionCount(int submissionCount) {
    this.submissionCount = submissionCount;
  }

  /**
   * Get when the Status variable was most recently set.
   *
   * @return When the Status variable was most recently set
   */
  public Date getStatusTimestamp() {
    return statusTimestamp;
  }

  /**
   * Set when the Status variable was most recently set.
   *
   * @param statusTimestamp When the Status variable was most recently set
   */
  @Predicate(dataType = "xsd:dateTime")
  public void setStatusTimestamp(Date statusTimestamp) {
    this.statusTimestamp = statusTimestamp;
  }

  /**
   * Get when this Article was first submitted to this syndication Target.
   *
   * @return When this Article was first submitted to this syndication Target
   */
  public Date getSubmitTimestamp() {
    return submitTimestamp;
  }

  /**
   * Set when this Article was first submitted to this syndication Target.
   *
   * @param submitTimestamp When this Article was first submitted to this syndication Target
   */
  @Predicate(dataType = "xsd:dateTime")
  public void setSubmitTimestamp(Date submitTimestamp) {
    this.submitTimestamp = submitTimestamp;
  }

  /**
   * Get the reason for syndication failure, if the syndication process failed.
   * If the syndication process succeeded the first time, then this method will be return a null.
   * If the syndication process failed one or more times, but succeeded on a subsequent submission,
   * then this method will return the error which caused the most recent failure.
   *
   * @return The reason for the most recent syndication failure
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Set the reason for syndication failure, if the syndication process failed.
   *
   * @param errorMessage The reason for the most recent syndication failure
   */
  @Predicate
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Only uses the <code>target</code> variable.
   *
   * @return The hashCode based only on the <code>target</code> variable
   */
  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Syndication{" +
        "id=" + id +
        ", articleId=" + articleId +
        ", status='" + status + '\'' +
        ", target='" + target + '\'' +
        ", submissionCount=" + submissionCount +
        ", statusTimestamp=" + statusTimestamp +
        ", submitTimestamp=" + submitTimestamp +
        ", errorMessage='" + errorMessage + '\'' +
        '}';
  }
}
