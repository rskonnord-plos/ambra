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
import java.util.List;

/**
 * The info about a single Issue that the UI needs. 
 *
 * This class is immutable.
 */
public class IssueInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  private URI          id;
  private String       displayName;
  private URI          prevIssue;
  private URI          nextIssue;
  private URI          imageArticle;
  private String       description;
  private URI parentVolume;
  private List<URI> articleUriList;

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

  /**
   * Set the list of article DOI's that this issue contains. Please update
   * the browseCache for this IssueInfo if you change this list. 
   * @param articleList
   */
  public void setArticleUriList(List<URI> articleList) {
    articleUriList = articleList;
  }
  
  /**
   * Returns a list of Article DOI's that this issue contains. Note that is you need the actual 
   * articles themselves, use BrowseService.getArticleInfosForIssue() since it attempts to use
   * cached ArticleInfos for efficiency. 
   * 
   * @return
   */
  public List<URI> getArticleUriList() {
    return articleUriList;
  }
}
