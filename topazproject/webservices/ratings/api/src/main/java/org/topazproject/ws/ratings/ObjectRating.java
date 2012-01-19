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
 * This defines a single object rating. Each rating is associated with a category.
 *
 * <p>This class is a bean.
 * 
 * @author Ronald Tschalär
 */
public class ObjectRating {
  /** The category of the rating. */
  private String category;
  /** The rating. */
  private float  rating;

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
   * Get the rating.
   *
   * @return the rating
   */
  public float getRating() {
    return rating;
  }

  /**
   * Set the rating.
   *
   * @param rating the rating
   */
  public void setRating(float rating) {
    this.rating = rating;
  }
}
