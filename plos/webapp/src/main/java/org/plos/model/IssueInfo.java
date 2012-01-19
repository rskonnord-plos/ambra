/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.plos.model.article.ArticleInfo;

/**
 * The info about a single Issue that the UI needs.
 *
 * This class is immutable.
 */
public class IssueInfo implements Serializable {

  private URI          id;
  private String       displayName;
  private URI          prevIssue;
  private URI          nextIssue;
  private URI          imageArticle;
  private String       description;
  private List<ArticleInfo> articlesInIssue = new ArrayList<ArticleInfo>();
  private URI parentVolume;

  // XXX TODO, List<URI> w/Article DOI vs. List<ArticleInfo>???

  public IssueInfo(URI id, String displayName, URI prevIssue, URI nextIssue, URI imageArticle,
          String description, URI parentVolume) {

    this.id = id;
    this.displayName = displayName;
    this.prevIssue = prevIssue;
    this.nextIssue = nextIssue;
    this.imageArticle = imageArticle;
    this.description = description;
    this.parentVolume = parentVolume;
  }

  public void addArticleToIssue(ArticleInfo article) {
    articlesInIssue.add(article);
  }

  /**
   * Get the id.
   *
   * @return the id.
   */
  public URI getId() {
    return id;
  }

  /**
   * Get the displayName.
   *
   * @return the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get the previous Issue.
   *
   * @return the previous Issue.
   */
  public URI getPrevIssue() {
    return prevIssue;
  }

  /**
   * Get the next Issue.
   *
   * @return the next Issue.
   */
  public URI getNextIssue() {
    return nextIssue;
  }

  /**
   * Get the image Article DOI.
   *
   * @return the image Article DOI.
   */
  public URI getImageArticle() {
    return imageArticle;
  }

  /**
   * Get the description.
   *
   * @return the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the "parent" Volume.
   *
   * @return the "parent" Volume.
   */
  public URI getParentVolume() {
    return parentVolume;
  }

  public List<ArticleInfo> getArticlesInIssue() {
    return articlesInIssue;
  }
}
