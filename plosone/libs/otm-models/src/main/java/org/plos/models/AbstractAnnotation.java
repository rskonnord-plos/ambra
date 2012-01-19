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

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

/**
 * AbstractAnnotation meta-data.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Annotea.NS + "Annotation")
public abstract class AbstractAnnotation extends Annotea {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/annotation/")
  private URI                                                               id;
  private URI                                                               annotates;
  private String                                                            context;
  @Predicate(uri = Rdf.dc_terms + "replaces")
  private AbstractAnnotation                                                supersedes;
  @Predicate(uri = Rdf.dc_terms + "isReplacedBy")
  private AbstractAnnotation                                                supersededBy;

/**
   * Creates a new AbstractAnnotation object.
   */
  public AbstractAnnotation() {
  }

/**
   * Creates a new AbstractAnnotation object.
   */
  public AbstractAnnotation(URI id) {
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
  public AbstractAnnotation getSupersedes() {
    return supersedes;
  }

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  public void setSupersedes(AbstractAnnotation supersedes) {
    this.supersedes = supersedes;
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy.
   */
  public AbstractAnnotation getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  public void setSupersededBy(AbstractAnnotation supersededBy) {
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
