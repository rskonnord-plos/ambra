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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * RatingContent is the body of a Rating.
 * It stores, insight, reliability, style and comment values.
 *
 * @author stevec
 * @author Jeff Suttor
 */
@UriPrefix(Rdf.topaz + "RatingContent/")
@Entity(model = "ri", type = Rdf.topaz + "RatingContent")
public class RatingContent implements Serializable {

  public static final int INSIGHT_WEIGHT = 6;
  public static final int RELIABILITY_WEIGHT = 5;
  public static final int STYLE_WEIGHT = 4;

  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/ratingContent/")
  private String id;

  private int insightValue;
  private int reliabilityValue;
  private int styleValue;
  private int singleRatingValue;
  private String commentTitle;
  private String commentValue;

  private static final Log log = LogFactory.getLog(RatingContent.class);

  /**
   * Creates a new RatingContent object with default values.
   */
  public RatingContent() {

    this(0, 0, 0, null, null);
  }

  /**
   * Constructor - Used for single ratings (just one overall rating "category")
   * @param singleRatingValue
   * @param commentTitle
   * @param commentValue
   */
  public RatingContent(int singleRatingValue, String commentTitle, String commentValue) {
    super();
    this.singleRatingValue = singleRatingValue;
    this.commentTitle = commentTitle;
    this.commentValue = commentValue;
  }

  /**
   * Creates a new RatingContent object with specified values.
   */
  public RatingContent(int insight, int reliability, int style, String commentTitle, String commentValue) {

    this.insightValue      = insight;
    this.reliabilityValue  = reliability;
    this.styleValue        = style;
    this.commentTitle = commentTitle;
    this.commentValue = commentValue;
  }

  /**
   * Get insightValue value.
   *
   * @return Insight value.
   */
  public int getInsightValue() {
    return insightValue;
  }
  /**
   * Set insightValue value.
   *
   * @param insight value.
   */
  public void setInsightValue(int insight) {
    this.insightValue = insight;
  }

  /**
   * Get reliabilityValue value.
   *
   * @return Reliability value.
   */
  public int getReliabilityValue() {
    return reliabilityValue;
  }
  /**
   * Set reliabilityValue value.
   *
   * @param reliability value.
   */
  public void setReliabilityValue(int reliability) {
    this.reliabilityValue = reliability;
  }

  /**
   * Get styleValue value.
   *
   * @return Style value.
   */
  public int getStyleValue() {
    return styleValue;
  }
  /**
   * Set styleValue value.
   *
   * @param style value.
   */
  public void setStyleValue(int style) {
    this.styleValue = style;
  }

  public int getSingleRatingValue() {
    return singleRatingValue;
  }

  public void setSingleRatingValue(int singleRatingValue) {
    this.singleRatingValue = singleRatingValue;
  }

  /**
   * Get overall (weighted) value.
   *
   * @return Overall value.
   */
  public double getOverallValue() {

    return calculateOverall(getInsightValue(), getReliabilityValue(), getStyleValue());
  }

  /**
   * Get comment title.
   *
   * @return Comment title.
   */
  public String getCommentTitle() {
    return commentTitle;
  }
  /**
   * Set comment title.
   *
   * @param commentTitle title.
   */
  public void setCommentTitle(String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Get comment value.
   *
   * @return Comment value.
   */
  public String getCommentValue() {
    return commentValue;
  }
  /**
   * Set comment value.
   *
   * @param commentValue value.
   */
  public void setCommentValue(String commentValue) {
    this.commentValue = commentValue;
  }

  /**
   * @return Returns the id.
   */
  public String getId() {
    return id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Round a rating.
   * @param x Rating value to round.
   * @param r Typically 0.5, to round to half stars
   * @return Rounded rating value
   */
  public static double roundTo(double x, double r) {
    if (r == 0) {
      return x;
    }

    return Math.round(x * (1 / r)) / (1 / r);
  }

  /**
   * Calculate weighted overall.
   *
   * @param insightValue Insight value.
   * @param reliabilityValue Reliability value.
   * @param styleValue Style value.
   * @return Weighted overall.
   */
  public static double calculateOverall(int insightValue, int reliabilityValue, int styleValue) {

    return calculateOverall((double) insightValue, (double) reliabilityValue, (double) styleValue);
  }
  
  /**
   * Calculate weighted overall.
   *
   * @param insightValue Insight value.
   * @param reliabilityValue Reliability value.
   * @param styleValue Style value.
   * @return Weighted overall.
   */
  public static double calculateOverall(double insightValue, double reliabilityValue, double styleValue) {

    int    runningWeight = 0;
    double runningTotal  = 0;

    if (insightValue > 0) {
      runningWeight += RatingContent.INSIGHT_WEIGHT;
      runningTotal += insightValue * RatingContent.INSIGHT_WEIGHT;
      if (log.isDebugEnabled())
        log.debug("INSIGHT: runningWeight = " + runningWeight + " runningTotal: " + runningTotal);
    }

    if (reliabilityValue > 0) {
      runningWeight += RatingContent.RELIABILITY_WEIGHT;
      runningTotal += reliabilityValue * RatingContent.RELIABILITY_WEIGHT;
      if (log.isDebugEnabled())
        log.debug("RELIABILITY: runningWeight = " + runningWeight+ " runningTotal: " + runningTotal);
    }

    if (styleValue > 0) {
      runningWeight += RatingContent.STYLE_WEIGHT;
      runningTotal += styleValue * RatingContent.STYLE_WEIGHT;
      if (log.isDebugEnabled())
        log.debug("STYLE: runningWeight = " + runningWeight + " runningTotal: " + runningTotal);
    }

    return runningTotal / runningWeight;
  }
}
