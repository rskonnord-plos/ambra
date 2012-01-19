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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * RatingsSummaryContent is the body for Ratings Summary.
 *
 * @author stevec
 * @author Jeff Suttor
 */
@UriPrefix(Rdf.topaz + "RatingSummaryContent/")
@Entity(model = "ri", type = Rdf.topaz + "RatingSummaryContent")
public class RatingSummaryContent {
  @Id
  @GeneratedValue(uriPrefix = "info:doi/10.1371/ratingSummaryContent/")
  private String id;

  private int    insightNumRatings;
  private double insightTotal;
  private int    reliabilityNumRatings;
  private double reliabilityTotal;
  private int    styleNumRatings;
  private double styleTotal;
  private int    numUsersThatRated;

  private static final Log log = LogFactory.getLog(RatingSummaryContent.class);

  /**
   * Creates a new RatingSummaryContent object.
   */
  public RatingSummaryContent() {
    this(0, 0, 0, 0, 0, 0, 0);
  }

  /**
   * Creates a new RatingSummaryContent object.
   *
   * @param totalValue total value
   * @param numRatings number of ratings
   */
  public RatingSummaryContent(
    int insightNumRatings,     double insightTotal,
    int reliabilityNumRatings, double reliabilityTotal,
    int styleNumRatings,       double styleTotal,
    int numUsersThatRated) {

    this.insightNumRatings     = insightNumRatings;
    this.insightTotal          = insightTotal;
    this.reliabilityNumRatings = reliabilityNumRatings;
    this.reliabilityTotal      = reliabilityTotal;
    this.styleNumRatings       = styleNumRatings;
    this.styleTotal            = styleTotal;
    this.numUsersThatRated     = numUsersThatRated;
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
   * @return Returns the number of insight Ratings.
   */
  public int getInsightNumRatings() {
    return insightNumRatings;
  }
  /**
   * @param insightNumRatings The number of insight ratings.
   */
  public void setInsightNumRatings(int insightNumRatings) {
    this.insightNumRatings = insightNumRatings;
  }

  /**
   * @return Returns the total of insight Ratings.
   */
  public double getInsightTotal() {
    return insightTotal;
  }
  /**
   * @param insightTotal The total of insight ratings.
   */
  public void setInsightTotal(double insightTotal) {
    this.insightTotal = insightTotal;
  }

  /**
   * @return Returns the number of reliability Ratings.
   */
  public int getReliabilityNumRatings() {
    return reliabilityNumRatings;
  }
  /**
   * @param reliabilityNumRatings The number of reliability ratings.
   */
  public void setReliabilityNumRatings(int reliabilityNumRatings) {
    this.reliabilityNumRatings = reliabilityNumRatings;
  }

  /**
   * @return Returns the total of reliability Ratings.
   */
  public double getReliabilityTotal() {
    return reliabilityTotal;
  }
  /**
   * @param reliabilityTotal The total of reliability ratings.
   */
  public void setReliabilityTotal(double reliabilityTotal) {
    this.reliabilityTotal = reliabilityTotal;
  }

  /**
   * @return Returns the number of style Ratings.
   */
  public int getStyleNumRatings() {
    return styleNumRatings;
  }
  /**
   * @param styleNumRatings The number of style ratings.
   */
  public void setStyleNumRatings(int styleNumRatings) {
    this.styleNumRatings = styleNumRatings;
  }

  /**
   * @return Returns the total of style Ratings.
   */
  public double getStyleTotal() {
    return styleTotal;
  }
  /**
   * @param styleTotal The total of style ratings.
   */
  public void setStyleTotal(double styleTotal) {
    this.styleTotal = styleTotal;
  }

  /**
   * @return Returns the overall Rating.
   */
  public double getOverall() {

    return RatingContent.calculateOverall(
      getInsightTotal() / getInsightNumRatings(),
      getReliabilityTotal() / getReliabilityNumRatings(),
      getStyleTotal() / getStyleNumRatings());
  }

  /**
   * @return Number of users that rated.
   */
  public int getNumUsersThatRated() {
    return numUsersThatRated;
  }

  /**
   * @param numUsersThatRated Number of users that rated.
   */
  public void setNumUsersThatRated(int numUsersThatRated) {
    this.numUsersThatRated = numUsersThatRated;
  }

  /**
   * Add a Rating value to the RatingSummary
   *
   * @param ratingType Type of Rating to add.
   * @param value Value of Rating to add.
   */
  public void addRating(String ratingType, int value) {
    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      insightNumRatings += 1;
      insightTotal      += value;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      reliabilityNumRatings += 1;
      reliabilityTotal      += value;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      styleNumRatings += 1;
      styleTotal      += value;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when adding a Rating to a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Remove a Rating value from the RatingSummary
   *
   * @param ratingType Type of Rating to remove.
   * @param value Value of Rating to remove.
   */
  public void removeRating(String ratingType, int value) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      insightNumRatings -= 1;
      insightTotal      -= value;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      reliabilityNumRatings -= 1;
      reliabilityTotal      -= value;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      styleNumRatings -= 1;
      styleTotal      -= value;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when removing a Rating from a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Retrieve the average for a Rating type.
   *
   * @param ratingType Type of Rating.
   * @return Returns the average rating value.
   */
  public double retrieveAverage(String ratingType) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      return insightTotal / insightNumRatings;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      return reliabilityTotal / reliabilityNumRatings;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      return styleTotal / styleNumRatings;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when retrieving the average Rating from a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Set the number of ratings.
   *
   * @param ratingType Type of Rating.
   * @param numRatings
   */
  public void assignNumRatings(String ratingType, int numRatings) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      insightNumRatings = numRatings;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      reliabilityNumRatings = numRatings;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      styleNumRatings = numRatings;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when assigning the number of Ratings for a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Get the number of Ratings for a Rating type.
   *
   * @param ratingType Type of Rating.
   * @return Number of Ratings.
   */
  public int retrieveNumRatings(String ratingType) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      return insightNumRatings;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      return reliabilityNumRatings;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      return styleNumRatings;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when retriving the number of Ratings for a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Set the total value of the Rating.
   *
   * @param ratingType Type of Rating.
   * @param total Total value of Rating.
   */
  public void assignTotal(String ratingType, double total) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      insightTotal = total;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      reliabilityTotal = total;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      styleTotal = total;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when assigning a total Ratings value for a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }

  /**
   * Get the total value of the Rating.
   *
   * @param ratingType Type of Rating.
   * @return total rating
   */
  public double retrieveTotal(String ratingType) {

    if (ratingType.equals(Rating.INSIGHT_TYPE)) {
      return insightTotal;
    } else if (ratingType.equals(Rating.RELIABILITY_TYPE)) {
      return reliabilityTotal;
    } else if (ratingType.equals(Rating.STYLE_TYPE)) {
      return styleTotal;
    } else {
      // should never happen
      String errorMessage = "Invalid type, " + ratingType + ", when retrieving a total Ratings value for a RatingSummary.";
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }
}
