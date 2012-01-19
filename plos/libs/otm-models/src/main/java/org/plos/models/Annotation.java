/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.models;

import java.net.URI;


import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * Annotation meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Annotea.NS + "Annotation")
public abstract class Annotation extends Annotea {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/annotation/")
  private URI                                                               id;
  private URI                                                               annotates;
  private String                                                            context;
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private Annotation                                                        supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private Annotation                                                        supersededBy;

  /**
   * Creates a new Annotation object.
   */
  public Annotation() {
  }

  /**
   * Creates a new Annotation object.
   */
  public Annotation(URI id) {
    this.id = id;
  }

  /**
   * Get annotates.
   *
   * @return annotates as Uri.
   */
  public URI getAnnotates() {
    return annotates;
  }

  /**
   * Set annotates.
   *
   * @param annotates the value to set.
   */
  public void setAnnotates(URI annotates) {
    this.annotates = annotates;
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return context;
  }

  /**
   * Set context.
   *
   * @param context the value to set.
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Get supersedes.
   *
   * @return supersedes.
   */
  public Annotation getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(Annotation supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy.
   */
  public Annotation getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(Annotation supersededBy) {
    this.supersededBy = supersededBy;
  }

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
}
