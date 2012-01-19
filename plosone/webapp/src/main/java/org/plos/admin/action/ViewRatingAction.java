/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.models.Rating;
import org.plos.rating.service.RatingsService;

public class ViewRatingAction extends BaseActionSupport {

  private static final Log log = LogFactory.getLog(ViewRatingAction.class);

  private String ratingId;
  private Rating rating;
  private RatingsService ratingsService;


  public String execute() throws Exception {

    rating = getRatingsService().getRating(ratingId);
    return SUCCESS;
  }

  public RatingsService getRatingsService() {
    return ratingsService;
  }

  public void setRatingsService(RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  public Rating getRating() {
    return rating;
  }

  public void setRatingId(String ratingId) {
    this.ratingId = ratingId;
  }
}
