/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Value object that holds the result of a single search item
 */
public class SearchHit {
  private final String hitNumber;
  private final String hitScore;
  private final String pid;
  private final String title;
  private final String highlight;
  private final String type;
  private final String state;
  private final Date createdDate;
  private final Date lastModifiedDate;
  private final String contentModel;
  private final String description;
  private final String publisher;
  private final String repositoryName;
  private Date date;
  private String creator;
  public static final String MULTIPLE_VALUE_DELIMITER = "@@";

  /**
   * Create a search hit with the values set
   */
  public SearchHit(final String hitNumber, final String hitScore, final String pid, final String title, final String highlight, final String type, final String state, final String creator, final Date date, final Date createdDate, final Date lastModifiedDate, final String contentModel, final String description, final String publisher, final String repositoryName) {
    this.date = date;
    this.hitNumber = hitNumber;
    this.hitScore = hitScore;
    this.pid = StringUtils.split(pid, MULTIPLE_VALUE_DELIMITER)[0];
    this.title = StringUtils.split(title, MULTIPLE_VALUE_DELIMITER)[0];
    this.highlight = highlight;
    this.type = type;
    this.state = state;
    this.createdDate = createdDate;
    this.lastModifiedDate = lastModifiedDate;
    this.contentModel = contentModel;
    this.description = description;
    this.publisher = publisher;
    this.repositoryName = repositoryName;
    this.creator = StringUtils.join(StringUtils.split(creator, MULTIPLE_VALUE_DELIMITER), ", ");
  }

  /**
   * Getter for property 'contentModel'.
   * @return Value for property 'contentModel'.
   */
  public String getContentModel() {
    return contentModel;
  }

  /**
   * Getter for property 'createdDate'.
   * @return Value for property 'createdDate'.
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * Getter for property 'description'.
   * @return Value for property 'description'.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Getter for property 'lastModifiedDate'.
   * @return Value for property 'lastModifiedDate'.
   */
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  /**
   * Getter for property 'pid'.
   * @return Value for property 'pid'.
   */
  public String getPid() {
    return pid;
  }

  /**
   * Getter for property 'publisher'.
   * @return Value for property 'publisher'.
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * Getter for property 'repositoryName'.
   * @return Value for property 'repositoryName'.
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * Getter for property 'state'.
   * @return Value for property 'state'.
   */
  public String getState() {
    return state;
  }

  /**
   * Getter for property 'type'.
   * @return Value for property 'type'.
   */
  public String getType() {
    return type;
  }


  /**
   * Getter for property 'hitNumber'.
   * @return Value for property 'hitNumber'.
   */
  public String getHitNumber() {
    return hitNumber;
  }

  /**
   * Getter for property 'hitScore'.
   * @return Value for property 'hitScore'.
   */
  public String getHitScore() {
    return hitScore;
  }

  /**
   * Getter for property 'creator'.
   * @return Value for property 'creator'.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Getter for property 'date'.
   * @return Value for property 'date'.
   */
  public Date getDate() {
    return date;
  }

  /**
   * Getter for property 'highlight'.
   * @return Value for property 'highlight'.
   */
  public String getHighlight() {
    return highlight;
  }

  /**
   * Getter for property 'title'.
   * @return Value for property 'title'.
   */
  public String getTitle() {
    return title;
  }
}
