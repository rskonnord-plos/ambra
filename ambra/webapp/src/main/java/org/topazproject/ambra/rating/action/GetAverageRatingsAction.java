/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.rating.action;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.rating.service.RatingsService;
import org.topazproject.ambra.user.AmbraUser;

/**
 * General Rating action class to store and retrieve summary ratings on an article.
 *
 * @author stevec
 */
@SuppressWarnings("serial")
public class GetAverageRatingsAction extends AbstractRatingAction {
  private RatingsService                  ratingsService;
  private RatingsService.AverageRatings   avg;
  private String                          articleURI;
  private boolean                         isResearchArticle;
  private boolean                         hasRated = false;

  /**
   * Execute the ratings summary action.
   *
   * @return WebWork action status
   */
  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    avg = ratingsService.getAverageRatings(articleURI);
    hasRated = ratingsService.hasRated(articleURI, getCurrentUser());
    isResearchArticle = isResearchArticle(articleURI);
    return SUCCESS;
  }

  /**
   * Set the ratings service.
   *
   * @param ratingsService the ratings service
   */
  @Required
  public void setRatingsService(final RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  /**
   * Sets the URI of the article being rated.
   *
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Gets the URI of the article being rated.
   *
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * @return the isResearchArticle
   */
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  /**
   * Gets the insight ratings average.
   *
   * @return Returns the insightAverage.
   */
  public double getInsightAverage() {
    return avg.insight.average;
  }

  /**
   * Gets the count of insight ratings.
   *
   * @return Returns the numInsightRatings.
   */
  public int getNumInsightRatings() {
    return avg.insight.count;
  }

  /**
   * Gets the count of reliability ratings.
   *
   * @return Returns the numReliabilityRatings.
   */
  public int getNumReliabilityRatings() {
    return avg.reliability.count;
  }

  /**
   * @return the numSingleRatingRatings
   */
  public int getNumSingleRatingRatings() {
    return avg.single.count;
  }

  /**
   * Gets the count of style ratings.
   *
   * @return Returns the numStyleRatings.
   */
  public int getNumStyleRatings() {
    return avg.style.count;
  }


  /**
   * Gets the overall average ratings.
   *
   * @return Returns the overallAverage.
   */
  public double getOverallAverage() {
    return avg.overall;
  }

  /**
   * Gets the reliability ratings average.
   *
   * @return Returns the reliabilityAverage.
   */
  public double getReliabilityAverage() {
    return avg.reliability.average;
  }

  /**
   * Gets the style ratings average.
   *
   * @return Returns the styleAverage.
   */
  public double getStyleAverage() {
    return avg.style.average;
  }

  /**
   * @return the singleRatingAverage
   */
  public double getSingleRatingAverage() {
    return avg.single.average;
  }


  /**
   * Gets the total on insight ratings.
   *
   * @return Returns the totalInsight.
   */
  public double getTotalInsight() {
    return avg.insight.total;
  }

  /**
   * Gets the total on reliability ratings.
   *
   * @return Returns the totalReliability.
   */
  public double getTotalReliability() {
    return avg.reliability.total;
  }

  /**
   * @return the totalSingleRating
   */
  public double getTotalSingleRating() {
    return avg.single.total;
  }

  /**
   * Gets the total on style ratings.
   *
   * @return Returns the totalStyle.
   */
  public double getTotalStyle() {
    return avg.style.total;
  }

  /**
   * Tests if this article has been rated.
   *
   * @return Returns the hasRated.
   */
  public boolean isHasRated() {
    return hasRated;
  }

  /**
   * Gets the rounded average on insight ratings.
   *
   * @return Returns the insightRoundedAverage.
   */
  public double getInsightRoundedAverage() {
    return avg.insight.rounded;
  }

  /**
   * Gets the overall rounded average.
   *
   * @return Returns the overallRoundedAverage.
   */
  public double getOverallRoundedAverage() {
    return avg.roundedOverall;
  }

  /**
   * Gets the rounded average on reliability ratings.
   *
   * @return Returns the reliabilityRoundedAverage.
   */
  public double getReliabilityRoundedAverage() {
    return avg.reliability.rounded;
  }

  /**
   * @return the singleRatingRoundedAverage
   */
  public double getSingleRatingRoundedAverage() {
    return avg.single.rounded;
  }

  /**
   * Gets the rounded average on style ratings.
   *
   * @return Returns the styleRoundedAverage.
   */
  public double getStyleRoundedAverage() {
    return avg.style.rounded;
  }

  /**
   * Gets the number of users that rated.
   *
   * @return Number of users that rated.
   */
  public int getNumUsersThatRated() {
    return avg.numUsersThatRated;
  }
}
