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
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * Editorial Board for an aggregation.
 *
 * @author Pradeep Krishnan
  */
@Entity(type = PLoS.plos + "EditorialBoard", model = "ri")
public class EditorialBoard {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/editorialBoard/")
  private URI            id;
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private EditorialBoard supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private EditorialBoard supersededBy;
  @Predicate(uri = PLoS.plos + "editors", storeAs = Predicate.StoreAs.rdfSeq)
  private List<UserProfile>      editors = new ArrayList();

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
   * Get supersedes.
   *
   * @return supersedes as EditorialBoard.
   */
  public EditorialBoard getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(EditorialBoard supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy as EditorialBoard.
   */
  public EditorialBoard getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(EditorialBoard supersededBy) {
    this.supersededBy = supersededBy;
  }

  /**
   * Get editors.
   *
   * @return editors as List.
   */
  public List<UserProfile> getEditors() {
    return editors;
  }

  /**
   * Set editors.
   *
   * @param editors the value to set.
   */
  public void setEditors(List<UserProfile> editors) {
    this.editors = editors;
  }
}
