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

import java.util.ArrayList;
import java.util.List;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Marker class to mark an Aggregation as a "Journal".
 *
 * @author Pradeep Krishnan
 */
@Entity(type = PLoS.plos + "Journal", model = "ri")
public class Journal extends Aggregation {
  @Predicate(uri = PLoS.plos + "key")
  private String  key;

  @Predicate(uri = "http://prismstandard.org/namespaces/1.2/basic/eIssn")
  private String  eIssn;

  /** DOI of "current issue" */
  @Predicate(uri = PLoS.plos + "Journal/currentIssue")
  private URI currentIssue;
  
  @Predicate(uri = PLoS.plos + "Journal/volumes")
  private List<URI> volumes = new ArrayList();

  @Predicate(uri = PLoS.plos + "Journal/image")
  private URI image;

  /**
   * Get the internal key used to identify this journal.
   *
   * @return the key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Set the internal key used to identify this journal.
   *
   * @param key the key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Get the e-issn of this journal.
   *
   * @return the e-issn.
   */
  public String getEIssn() {
    return eIssn;
  }

  /**
   * Set the e-issn of this journal.
   *
   * @param eIssn the e-issn.
   */
  public void setEIssn(String eIssn) {
    this.eIssn = eIssn;
  }

  /**
   * Get the image for this journal.
   *
   * @return the image, may be null.
   */
  public URI getImage() {
    return image;
  }

  /**
   * Set the image for this journal.
   *
   * @param image the image, may be null.
   */
  public void setImage(URI image) {
    this.image = image;
  }

  /**
   * Get the current Issue for this journal.
   *
   * @return the current Issue's DOI, may be null.
   */
  public URI getCurrentIssue() {
    return currentIssue;
  }

  /**
   * Set the current Issue's DOI for this journal.
   *
   * The DOI is arbitrary, treated as opaque and encouraged to be human friendly.
   *
   * @param currentIssue the current Issue, may be null.
   */
  public void setCurrentIssue(URI currentIssue) {
    this.currentIssue = currentIssue;
  }

  /**
   * Get the Volumes for this journal.
   *
   * @return the Volumes for this journal.
   */
  public List<URI> getVolumes() {
    return volumes;
  }

  /**
   * Set the Volumes for this journal.
   *
   * @param volumes the Volumes for this journal.
   */
  public void setVolumes(List<URI> volumes) {
    this.volumes= volumes;
  }

  /**
   * String representation for debugging.
   * 
   * @return String representation for debugging.
   */
  @Override
  public String toString() {
    return "Journal: ["
            + "eIssn: " + getEIssn()
            + ", key: " + getKey()
            + ", image: " + getImage()
            + ", currentIssue: " + getCurrentIssue()
            + ", volumes: " + getVolumes()
            + ", " + super.toString()
            + "]";
  }
}
