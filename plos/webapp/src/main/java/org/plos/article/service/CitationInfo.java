/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.Date;


/**
 * Simple class that represents the citation information necesssary to generate most citation file types.
 * Used to deserialize the XML into an object which can be cached.
 * 
 * @author Stephen Cheng
 *
 */
public class CitationInfo implements Serializable {
  private static final long serialVersionUID = 2798808464271769990L;
  
  private String articleTitle;
  private Date publicationDate;
  private String journalName; 
  private String journalTitle;
  private String publisherName;
  private Author[] authors;
  private CollaborativeAuthor[] collaborativeAuthors;
  private String startPage;
  private String endPage;
  private String volume;
  private String issue;
  private String articleAbstract;
  private String DOI;


  /**
   * Empty Constructor
   *
   */
  public CitationInfo() {
  }

  /**
   * @return Returns the articleAbstract.
   */
  public String getArticleAbstract() {
    return articleAbstract;
  }
  /**
   * @param articleAbstract The articleAbstract to set.
   */
  public void setArticleAbstract(String articleAbstract) {
    this.articleAbstract = articleAbstract;
  }
  /**
   * @return Returns the articleTitle.
   */
  public String getArticleTitle() {
    return articleTitle;
  }
  /**
   * @param articleTitle The articleTitle to set.
   */
  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }
  /**
   * @return Returns the authors.
   */
  public Author[] getAuthors() {
    return authors;
  }
  /**
   * @param authors The authors to set.
   */
  public void setAuthors(Author[] authors) {
    this.authors = authors;
  }
  /**
   * @return the collaborativeAuthors
   */
  public CollaborativeAuthor[] getCollaborativeAuthors() {
    return collaborativeAuthors;
  }
  /**
   * @param collaborativeAuthors the collaborative authors to set
   */
  public void setCollaborativeAuthor(CollaborativeAuthor[] collaborativeAuthors) {
    this.collaborativeAuthors = collaborativeAuthors;
  }
  /**
   * @return Returns the dOI.
   */
  public String getDOI() {
    return DOI;
  }
  /**
   * @param doi The dOI to set.
   */
  public void setDOI(String doi) {
    DOI = doi;
  }
  /**
   * @return Returns the endPage.
   */
  public String getEndPage() {
    return (endPage==null) ? "" : endPage;
  }
  /**
   * @param endPage The endPage to set.
   */
  public void setEndPage(String endPage) {
    this.endPage = endPage;
  }
  /**
   * @return Returns the issue.
   */
  public String getIssue() {
    return issue;
  }
  /**
   * @param issue The issue to set.
   */
  public void setIssue(String issue) {
    this.issue = issue;
  }
  /**
   * @return the journalName
   */
  public String getJournalName() {
    return journalName;
  }
  /**
   * @param journalName the journalName to set
   */
  public void setJournalName(String journalName) {
    this.journalName = journalName;
  }
  /**
   * @return Returns the journalTitle.
   */
  public String getJournalTitle() {
    return journalTitle;
  }
  /**
   * @param journalTitle The journalTitle to set.
   */
  public void setJournalTitle(String journalTitle) {
    this.journalTitle = journalTitle;
  }
  /**
   * @return Returns the publicationDate.
   */
  public Date getPublicationDate() {
    return publicationDate;
  }
  /**
   * @param publicationDate The publicationDate to set.
   */
  public void setPublicationDate(Date publicationDate) {
    this.publicationDate = publicationDate;
  }
  /**
   * @return Returns the publisherName.
   */
  public String getPublisherName() {
    return publisherName;
  }
  /**
   * @param publisherName The publisherName to set.
   */
  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }
  /**
   * @return Returns the startPage.
   */
  public String getStartPage() {
    return startPage;
  }
  /**
   * @param startPage The startPage to set.
   */
  public void setStartPage(String startPage) {
    this.startPage = startPage;
  }
  /**
   * @return Returns the uRL.
   */
  public String getURL() {
    try {
      return "http://dx.doi.org/" + URLEncoder.encode(DOI, "UTF-8");
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * @return Returns the volume.
   */
  public String getVolume() {
    return volume;
  }
  /**
   * @param volume The volume to set.
   */
  public void setVolume(String volume) {
    this.volume = volume;
  }
}
