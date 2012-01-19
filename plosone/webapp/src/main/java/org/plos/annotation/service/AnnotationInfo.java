/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.plos.models.AbstractAnnotation;
import org.plos.models.Annotation;

/**
 * Annotation meta-data - compatible with topaz annotation ws.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationInfo {
  private Annotation              ann;
  private FedoraHelper            fedora;
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  static {
    fmt.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

/**
   * Creates a new AnnotationInfo object.
   */
  public AnnotationInfo(Annotation ann, FedoraHelper fedora) {
    this.ann      = ann;
    this.fedora   = fedora;
  }

  /**
   * Get annotation type.
   *
   * @return annotation type as String.
   */
  public String getType() {
    return ann.getType();
  }

  /**
   * Get annotates.
   *
   * @return annotates as a URI
   */
  public String getAnnotates() {
    URI u = ann.getAnnotates();

    return (u == null) ? null : u.toString();
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return ann.getContext();
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return ann.getCreator();
  }

  /**
   * Get created.
   *
   * @return created as Date.
   */
  public String getCreated() {
    Date d = ann.getCreated();

    synchronized (fmt) {
      return (d == null) ? null : fmt.format(d);
    }
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public String getBody() {
    URI u = ann.getBody();

    return (u == null) ? null : fedora.getBodyURL(u.toString());
  }

  /**
   * Get supersedes.
   *
   * @return supersedes as String.
   */
  public String getSupersedes() {
    AbstractAnnotation a = ann.getSupersedes();

    return (a == null) ? null : a.getId().toString();
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return ann.getId().toString();
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return ann.getTitle();
  }

  /**
   * Get supersededBy.
   *
   * @return supersededBy as String.
   */
  public String getSupersededBy() {
    AbstractAnnotation a = ann.getSupersededBy();

    return (a == null) ? null : a.getId().toString();
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return ann.getMediator();
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return ann.getState();
  }
}
