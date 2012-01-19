/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings;

/** 
 * This defines the stats for all the ratings of an object in a given category.
 *
 * <p>This class is a bean.
 * 
 * @author Ronald Tschalär
 */
public class ObjectRatingStats {
  /** The category of the ratings. */
  private String category;
  /** The number of ratings. */
  private int    numRatings;
  /** The average (arithmetic mean). */
  private float  average;
  /** The variance (sigma^2). */
  private float  variance;

  /**
   * Get the category.
   *
   * @return the category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Set the category.
   *
   * @param category the category
   */
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   * Get the number of ratings.
   *
   * @return the number of ratings
   */
  public int getNumberOfRatings() {
    return numRatings;
  }

  /**
   * Set the number of ratings.
   *
   * @param numRatings the number of ratings
   */
  public void setNumberOfRatings(int numRatings) {
    this.numRatings = numRatings;
  }

  /**
   * Get the average rating (arithmetic mean).
   *
   * @return the average rating
   */
  public float getAverage() {
    return average;
  }

  /**
   * Set the average rating (arithmetic mean).
   *
   * @param average the average rating
   */
  public void setAverage(float average) {
    this.average = average;
  }

  /**
   * Get the variance (sigma squared).
   *
   * @return the variance
   */
  public float getVariance() {
    return variance;
  }

  /**
   * Set the variance (sigma squared).
   *
   * @param variance the variance
   */
  public void setVariance(float variance) {
    this.variance = variance;
  }
}
