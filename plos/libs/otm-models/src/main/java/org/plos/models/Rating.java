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
import org.topazproject.otm.annotations.Predicate;

/**
 * General base Rating class to store a RatingContent body.
 *
 * @author Stephen Cheng
 */
@Entity(type = Rdf.topaz + "RatingsAnnotation")
public class Rating extends Annotation {

  /** Style */
  public static final String STYLE_TYPE = Rdf.topaz + "StyleRating";
  /** Insight */
  public static final String INSIGHT_TYPE = Rdf.topaz + "InsightRating";
  /** Reliability */
  public static final String RELIABILITY_TYPE = Rdf.topaz + "ReliabilityRating";
  /** Overall */
  public static final String OVERALL_TYPE = Rdf.topaz + "OverallRating";
  /** Single Rating */
  public static final String SINGLE_RATING_TYPE = Rdf.topaz + "SingleRating";

  @Predicate(uri = Annotea.NS + "body")
  private RatingContent body;

  /**
   * Creates a new Rating object.
   */
  public Rating() {
  }

  /**
   * Creates a new Rating object.
   *
   * @param id the rating annotation id
   */
  public Rating(URI id) {
    super(id);
  }

  /**
   * @return Returns the rating.
   */
  public RatingContent getBody() {
    return this.body;
  }

  /**
   * @param rating The rating to set.
   */
  public void setBody(RatingContent rating) {
    this.body = rating;
  }
}
