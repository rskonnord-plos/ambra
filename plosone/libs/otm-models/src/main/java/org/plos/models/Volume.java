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
 * Marker class to mark an Aggregation as a "Volume".
 *
 * @author Jeff Suttor
 */
@Entity(type = PLoS.plos + "Volume", model = "ri")
public class Volume extends Aggregation {

  /** Journal's eIssn. */
  @Predicate(uri = PLoS.plos + "Volume/journal")
  private String journal;

  /** Display name.  Human friendly. */
  @Predicate(uri = PLoS.plos + "Volume/displayName")
  private String displayName;

  /** Previous Volume's DOI. */
  @Predicate(uri = PLoS.plos + "Volume/prevVolume")
  private URI prevVolume;

  /** Next Volume's DOI. */
  @Predicate(uri = PLoS.plos + "Volume/nextVolume")
  private URI nextVolume;

  /** Arbitrary URI to an image. */
  @Predicate(uri = PLoS.plos + "Volume/image")
  private URI image;

  /**
   * Get the image for this Volume.
   *
   * @return URI for the image, may be null.
   */
  public URI getImage() {
    return image;
  }

  /**
   * Set the image for this Volume.
   *
   * @param image arbitrary URI to the image, may be null.
   */
  public void setImage(URI image) {
    this.image = image;
  }

  /**
   * Get the Journal (eIssn) for this Volume.
   *
   * @return the Journal (eIssn).
   */
  public String getJournal() {
    return journal;
  }

  /**
   * Set the Journal (eIssn) for this Volume.
   *
   * @param journal the Journal (eIssn).
   */
  public void setJournal(String journal) {
    this.journal = journal;
  }

  /**
   * Get the display name for this Volume.
   *
   * @return the display name.  will not be null.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set the display name for this Volume.
   *
   * The display name should be human friendly.
   *
   * @param displayName the display name, may not be null.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get the previous Volume's DOI for this Volume.
   *
   * @return the previous Volume's DOI, may be null.
   */
  public URI getPrevVolume() {
    return prevVolume;
  }

  /**
   * Set the previous Volume's DOI for this Volume.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param prevVolume the previous Volume's DOI, may be null.
   */
  public void setPrevVolume(URI prevVolume) {
    this.prevVolume = prevVolume;
  }

  /**
   * Get the next Volume's DOI for this Volume.
   *
   * @return the next Volume's DOI, may be null.
   */
  public URI getNextVolume() {
    return nextVolume;
  }

  /**
   * Set the next Volume's DOI for this Volume.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param nextVolume the next Volume's DOI, may be null.
   */
  public void setNextVolume(URI nextVolume) {
    this.nextVolume = nextVolume;
  }
  
  /**
   * String representation of a Volume for debugging.
   */
  public String toString() {
    return this.getClass().getName() + ", doi: " + getId() + ", display name: " + displayName
      + ", journal: " + journal + ", image: " + image + ", nextVolume: " + nextVolume
      + ", prevVolume: " + prevVolume;
  }
}
