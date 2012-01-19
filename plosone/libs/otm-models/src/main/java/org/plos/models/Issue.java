/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Marker class to mark an Aggregation as a "Issue".
 *
 * @author Jeff Suttor
 */
@Entity(type = PLoS.plos + "Issue", model = "ri")
public class Issue extends Aggregation {

  /** Journal's eIssn. */
  @Predicate(uri = PLoS.plos + "Issue/journal")
  private String journal;

  /** Volume's DOI */
  @Predicate(uri = PLoS.plos + "Issue/volume")
  private URI volume;

  /** Display name.  Human friendly. */
  @Predicate(uri = PLoS.plos + "Issue/displayName")
  private String displayName;

  /** Previous Issue's DOI */
  @Predicate(uri = PLoS.plos + "Issue/prevIssue")
  private URI prevIssue;

  /** Next Issue's DOI */
  @Predicate(uri = PLoS.plos + "Issue/nextIssue")
  private URI nextIssue;

  /** Arbitrary URI to an image */
  @Predicate(uri = PLoS.plos + "Issue/image")
  private URI image;

  /**
   * Get the Journal (eIssn) for this Issue.
   *
   * @return the Journal (eIssn).
   */
  public String getJournal() {
    return journal;
  }

  /**
   * Set the Journal (eIssn) for this Issue.
   *
   * @param journal the Journal (eIssn).
   */
  public void setJournal(String journal) {
    this.journal = journal;
  }

  /**
   * Get the image for this Issue.
   *
   * @return URI for the image, may be null.
   */
  public URI getImage() {
    return image;
  }

  /**
   * Set the image for this Issue.
   *
   * @param image arbitrary URI to the image, may be null.
   */
  public void setImage(URI image) {
    this.image = image;
  }

  /**
   * Get the Volume's DOI for this Issue.
   *
   * @return the Volume's DOI.
   */
  public URI getVolume() {
    return volume;
  }

  /**
   * Set the Volume's DOI for this Issue.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param volume the Volume's DOI.
   */
  public void setVolume(URI volume) {
    this.volume = volume;
  }

  /**
   * Get the display name for this Issue.
   *
   * @return the display name.  will not be null.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set the display name for this Issue.
   *
   * The display name should be human friendly.
   *
   * @param displayName the display name, may not be null.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get the previous Issue's DOI for this Issue.
   *
   * @return the previous Issue's DOI, may be null.
   */
  public URI getPrevIssue() {
    return prevIssue;
  }

  /**
   * Set the previous Issue's DOI for this Issue.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param prevIssue the previous Issue's DOI, may be null.
   */
  public void setPrevIssue(URI prevIssue) {
    this.prevIssue = prevIssue;
  }

  /**
   * Get the next Issue's DOI for this Issue.
   *
   * @return the next Issue's DOI, may be null.
   */
  public URI getNextIssue() {
    return nextIssue;
  }

  /**
   * Set the next Issue's DOI for this Issue.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param nextIssue the next Issue's DOI, may be null.
   */
  public void setNextIssue(URI nextIssue) {
    this.nextIssue = nextIssue;
  }

  /**
   * String representation of an Issue for debugging.
   */
  public String toString() {
    return this.getClass().getName() + ", doi: " + getId() + ", display name: " + displayName
      + ", journal: " + journal + ", volume: " + volume + ", image: " + image
      + ", nextIssue: " + nextIssue + ", prevIssue: " + prevIssue;
  }
}
