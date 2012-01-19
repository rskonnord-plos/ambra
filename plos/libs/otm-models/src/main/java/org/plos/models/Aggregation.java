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

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.criterion.DetachedCriteria;

/**
 * An aggregation of resources. 
 * 
 * @author Pradeep Krishnan
 * @author Jeff Suttor
 * @author Ronald Tschalär
 * @author Eric Brown
 * @author Amit Kapoor
 */
@Entity(type = "http://purl.org/dc/dcmitype/Collection", model = "ri")
public class Aggregation {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/aggregation/")
  private URI                            id;
  @Predicate(uri = PLoS.plos + "editorialBoard")
  private EditorialBoard                 editorialBoard;
  @Embedded
  private DublinCore        dublinCore;
  @Predicate(uri = Rdf.dc_terms + "hasPart")
  private List<URI>                      simpleCollection = new ArrayList();
  @Predicate(uri = PLoS.plos + "smartCollectionRules")
  private List<DetachedCriteria>         smartCollectionRules = new ArrayList();
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private Aggregation                    supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private Aggregation                    supersededBy;

  /**
   * Get id.
   *
   * @return id as URI.
   */
  public URI getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get editorialBoard.
   *
   * @return editorialBoard as EditorialBoard.
   */
  public EditorialBoard getEditorialBoard() {
    return editorialBoard;
  }

  /**
   * Set editorialBoard.
   *
   * @param editorialBoard the value to set.
   */
  public void setEditorialBoard(EditorialBoard editorialBoard) {
    this.editorialBoard = editorialBoard;
  }

  /**
   * Get simple collection of articles.
   *
   * @return collection of article URI
   */
  public List<URI> getSimpleCollection() {
    return simpleCollection;
  }

  /**
   * Set simple collection of articles.
   *
   * @param simpleCollection collection of article URI
   */
  public void setSimpleCollection(List<URI> simpleCollection) {
    this.simpleCollection = simpleCollection;
  }

  /**
   * Get smart collection rules for articles (and other entities).
   *
   * @return smart collection rules
   */
  public List<DetachedCriteria> getSmartCollectionRules() {
    return smartCollectionRules;
  }

  /**
   * Set smart collection rules.
   *
   * @param smartCollectionRules as a list of detached criteria
   */
  public void setSmartCollectionRules(List<DetachedCriteria> smartCollectionRules) {
    this.smartCollectionRules = smartCollectionRules;
  }

  /**
   * Get dublinCore.
   *
   * @return dublinCore as DublinCore.
   */
  public DublinCore getDublinCore() {
      return dublinCore;
  }

  /**
   * Set dublinCore.
   *
   * @param dublinCore the value to set.
   */
  public void setDublinCore(DublinCore dublinCore) {
      this.dublinCore = dublinCore;
  }

  /**
   * Get supersedes.
   *
   * @return supersedes as Aggregation.
   */
  public Aggregation getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(Aggregation supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy as Aggregation.
   */
  public Aggregation getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(Aggregation supersededBy) {
    this.supersededBy = supersededBy;
  }

  /**
   * String representation for debugging.
   * 
   * @return String representation for debugging.
   */
  @Override
  public String toString() {
    return "Aggregation: ["
            + "id: " + getId()
            + ", simpleCollection: " + getSimpleCollection()
            + ", " + getSmartCollectionRules()
            + ", " + getDublinCore()
            + "]";
  }
}
