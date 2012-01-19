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
 * The info about a single Volume that the UI needs.
 * 
 * This class is immutable.
 */
public class VolumeInfo implements Serializable {

  private URI          id;
  private String       displayName;
  private URI          prevVolume;
  private URI          nextVolume;
  private URI          imageArticle;
  private String       description;
  private List<IssueInfo> issueInfos;

  // XXX TODO, List<URI> w/Issue DOI vs. List<IssueInfo>???

  public VolumeInfo(URI id, String displayName, URI prevVolume, URI nextVolume, URI imageArticle,
    String description, List<IssueInfo> issueInfos) {

    this.id = id;
    this.displayName = displayName;
    this.prevVolume = prevVolume;
    this.nextVolume = nextVolume;
    this.imageArticle = imageArticle;
    this.description = description;
    this.issueInfos = issueInfos;
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
   * Get the previous Volume.
   *
   * @return the previous Volume.
   */
  public URI getPrevVolume() {
    return prevVolume;
  }

  /**
   * Get the next Volume.
   *
   * @return the next Volume.
   */
  public URI getNextVolume() {
    return nextVolume;
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
   * Get the issueInfos.
   *
   * @return the issueInfos.
   */
  public List<IssueInfo> getIssueInfos() {
    return issueInfos;
  }
}