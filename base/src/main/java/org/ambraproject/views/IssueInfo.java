/* $HeadURL$
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

package org.ambraproject.views;

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
  private String       issueURI;
  private String       displayName;
  private String       prevIssue;
  private String       nextIssue;
  private String       imageArticle;
  private String       description;
  private String       parentVolume;
  private boolean      respectOrder;
  private List<String> articleUriList;
  private String       issueTitle;
  private String       issueImageCredit;
  private String       issueDescription;

  // XXX TODO, List<URI> w/Article DOI vs. List<ArticleInfo>???
  public IssueInfo(String issueURI, String displayName, String prevIssue, String nextIssue, String imageArticle,
          String description, String parentVolume, boolean respectOrder) {
    this.issueURI = issueURI;
    this.displayName = displayName;
    this.prevIssue = prevIssue;
    this.nextIssue = nextIssue;
    this.imageArticle = imageArticle;
    this.description = description;
    this.parentVolume = parentVolume;
    this.respectOrder = respectOrder;
  }

  /**
   * Get the id.
   *
   * @return the id.
   */
  public String getIssueURI() {
    return issueURI;
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
  public String getPrevIssue() {
    return prevIssue;
  }

  /**
   * Get the next Issue.
   *
   * @return the next Issue.
   */
  public String getNextIssue() {
    return nextIssue;
  }

  /**
   * Get the image Article DOI.
   *
   * @return the image Article DOI.
   */
  public String getImageArticle() {
    return imageArticle;
  }

  /**
   * Get the entire / full description.
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
  public String getParentVolume() {
    return parentVolume;
  }

  /**
   * Set the list of article DOI's that this issue contains
   * @param articleList
   */
  public void setArticleUriList(List<String> articleList) {
    articleUriList = articleList;
  }

  /**
   * Returns a list of Article DOI's that this issue contains. Note that is you need the actual 
   * articles themselves, use BrowseService.getArticleInfosForIssue() since it attempts to use
   * cached ArticleInfos for efficiency. 
   * 
   * @return
   */
  public List<String> getArticleUriList() {
    return articleUriList;
  }

  public boolean isRespectOrder() {
    return respectOrder;
  }

  /**
   * get the issue description.  (does not contain image credit and title information)
   * @return
   */
  public String getIssueDescription() {
    return issueDescription;
  }

  public void setIssueDescription(String issueDescription) {
    this.issueDescription = issueDescription;
  }

  /**
   * get issue image image credit
   * @return
   */
  public String getIssueImageCredit() {
    return issueImageCredit;
  }

  public void setIssueImageCredit(String issueImageCredit) {
    this.issueImageCredit = issueImageCredit;
  }

  /**
   * get issue title
   * @return
   */
  public String getIssueTitle() {
    return issueTitle;
  }

  public void setIssueTitle(String issueTitle) {
    this.issueTitle = issueTitle;
  }
}
