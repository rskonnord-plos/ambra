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

import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Volume OTM model class. Extends Aggregation but does not use the 
 * simpleCollection defined there as it's order is not preserved. 
 * Instead, we use issueList to handle the aggregated issues. 
 *
 * @author Jeff Suttor, Alex Worden
 */
@Entity(type = PLoS.plos + "Volume", model = "ri")
public class Volume extends Aggregation {

  /** Display name.  Human friendly. */
  @Predicate(uri = PLoS.plos + "displayName")
  private String displayName;

  /** Arbitrary URI to an image. */
  @Predicate(uri = PLoS.plos + "image")
  private URI image;

  // The ordered list of DOIs of issues contained in this volume. 
  // TODO - "issueList" should probably not be prefixed with Rdf.dc_terms. We'll need a data migration to change this :(
  @Predicate(uri = Rdf.dc_terms + "issueList", collectionType = CollectionType.RDFSEQ)
  private List<URI> issueList = new ArrayList<URI>();
  
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
   * String representation for debugging.
   * 
   * @return String representation for debugging.
   */
  @Override
  public String toString() {
    return "Volume: ["
            + "displayName: " + getDisplayName()
            + ", image: " + getImage()
            + ", issueList: " + getIssueList()
            + ", " + super.toString()
            + "]";
  }
  
  /**
   * Retrieves an ordered list of issue DOIs contained in this volume
   * @return ordered list of issue DOIs contained in this volume. 
   */
  public List<URI> getIssueList() {
    /* Workaround for bug in original implementation...
     * Migrate existing issue list from super.simpleCollection to 
     * this.issueList. Remove all issue from super.simpleCollection.
     * This code can probably be removed after 0.8.2.1 release but it 
     * shouldn't do any harm to leave it in here. 
     */
    if (((this.issueList == null) || (this.issueList.size() == 0)) && 
        ((super.getSimpleCollection() != null) && (super.getSimpleCollection().size() > 0))) {
      this.issueList = super.getSimpleCollection();
    }
    return issueList;
  }

  /**
   * Set the ordered list of issue DOIs contained in this volume
   * @param issueList
   */
  public void setIssueList(List<URI> issueList) {
     this.issueList = issueList;
     super.setSimpleCollection(new ArrayList<URI>());
  }
}
