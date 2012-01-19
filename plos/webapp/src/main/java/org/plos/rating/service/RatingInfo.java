/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.rating.service;

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.SimpleTimeZone;

import org.plos.models.Rating;

/**
 * Rating meta-data - designed to facilitate display from within the webapp.
 *
 * @author copied from Pradeep Krishnan
 */
public class RatingInfo {
  private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private Rating rating;

  static {
    fmt.setTimeZone(new SimpleTimeZone(0,"UTC"));
  }

  /**
   * Creates a new RatingInfo object.
   *
   * @param rating the Rating object that this is to wrap
   */
  public RatingInfo(Rating rating) {
    this.rating = rating;
  }

  /**
   * Get annotation type.
   *
   * @return rating type as String.
   */
  public String getType() {
    return rating.getType();
  }

  /**
   * Get annotates.
   *
   * @return annotates as a String
   */
  public String getAnnotates() {
    final URI u = rating.getAnnotates();
    final String result;

    if (u == null) {
      result = null;
    } else {
      result = u.toString();
    }

    return result;
  }

  /**
   * Get context.
   *
   * @return context as String.
   */
  public String getContext() {
    return rating.getContext();
  }

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  public String getCreator() {
    return rating.getCreator();
  }

  /**
   * Get created.
   *
   * @return created as String.
   */
  public String getCreated() {
    final Date d = rating.getCreated();
    final String result;

    synchronized (fmt) {
      if (d == null) {
        result = null;
      } else {
        result = fmt.format(d);
      }
    }

    return result;
  }

  /**
   * Get body.
   *
   * @return body as String.
   */
  public String getBody() {
    return rating.getBody().getCommentValue();
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return rating.getId().toString();
  }

  /**
   * Get title.
   *
   * @return title as String.
   */
  public String getTitle() {
    return rating.getBody().getCommentTitle();
  }

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  public String getMediator() {
    return rating.getMediator();
  }

  /**
   * Get state.
   *
   * @return state as int.
   */
  public int getState() {
    return rating.getState();
  }
}
