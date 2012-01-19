/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.rating.action;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.FetchArticleService;
import org.plos.model.article.ArticleType;
import org.plos.models.Article;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.rating.service.RatingsPEP;
import org.plos.user.PlosOneUser;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

/**
 * General Rating action class to store and retrieve summary ratings on an article.
 *
 * @author stevec
 */
@SuppressWarnings("serial")
public class GetAverageRatingsAction extends BaseActionSupport {
  private Session          session;
  private FetchArticleService fetchArticleService;
  private String           articleURI;
  private boolean          isResearchArticle;
  private double           insightAverage;
  private double           styleAverage;
  private double           singleRatingAverage;
  private double           reliabilityAverage;
  private double           insightRoundedAverage;
  private double           styleRoundedAverage;
  private double           reliabilityRoundedAverage;
  private double           singleRatingRoundedAverage;
  private int              numInsightRatings;
  private int              numStyleRatings;
  private int              numReliabilityRatings;
  private int              numSingleRatingRatings;
  private int              numUsersThatRated;
  private double           totalInsight;
  private double           totalStyle;
  private double           totalReliability;
  private double           totalSingleRating;
  private boolean          hasRated = false;
  private static final Log log      = LogFactory.getLog(GetAverageRatingsAction.class);
  private RatingsPEP       pep;

  private RatingsPEP getPEP() {
    try {
      if (pep == null)
        pep                           = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
    return pep;
  }

  /**
   * Execute the ratings summary action.
   *
   * @return WebWork action status
   */
  @Override
  public String execute() {
    final PlosOneUser   user               = PlosOneUser.getCurrentUser();

    getPEP().checkAccess(RatingsPEP.GET_STATS, URI.create(articleURI));

    return TransactionHelper.doInTx(session,
                              new TransactionHelper.Action<String>() {
      @SuppressWarnings("unchecked")
      public String run(Transaction tx) {
        RatingSummary ratingSummary      = null;

        if (log.isDebugEnabled()) {
          log.debug("retrieving rating summaries for: " + articleURI);
        }

        List<RatingSummary> summaryList = session
          .createCriteria(RatingSummary.class)
          .add(Restrictions.eq("annotates", articleURI))
          .list();

        if (summaryList.size() > 0) {
          ratingSummary = summaryList.get(0);

          insightAverage          = ratingSummary.getBody().retrieveAverage(Rating.INSIGHT_TYPE);
          insightRoundedAverage   = RatingContent.roundTo(insightAverage, 0.5);
          numInsightRatings       = ratingSummary.getBody().getInsightNumRatings();
          totalInsight            = ratingSummary.getBody().retrieveTotal(Rating.INSIGHT_TYPE);

          reliabilityAverage          = ratingSummary.getBody().retrieveAverage(Rating.RELIABILITY_TYPE);
          reliabilityRoundedAverage   = RatingContent.roundTo(reliabilityAverage, 0.5);
          numReliabilityRatings       = ratingSummary.getBody().getReliabilityNumRatings();
          totalReliability            = ratingSummary.getBody().retrieveTotal(Rating.RELIABILITY_TYPE);

          styleAverage          = ratingSummary.getBody().retrieveAverage(Rating.STYLE_TYPE);
          styleRoundedAverage   = RatingContent.roundTo(styleAverage, 0.5);
          numStyleRatings       = ratingSummary.getBody().getStyleNumRatings();
          totalStyle            = ratingSummary.getBody().retrieveTotal(Rating.STYLE_TYPE);

          singleRatingAverage          = ratingSummary.getBody().retrieveAverage(Rating.SINGLE_RATING_TYPE);
          singleRatingRoundedAverage   = RatingContent.roundTo(singleRatingAverage, 0.5);
          numSingleRatingRatings       = ratingSummary.getBody().getSingleRatingNumRatings();
          totalSingleRating            = ratingSummary.getBody().retrieveTotal(Rating.SINGLE_RATING_TYPE);
          
          numUsersThatRated     = ratingSummary.getBody().getNumUsersThatRated();
        }

        if (user != null) {
          if (log.isDebugEnabled()) {
            log.debug("retrieving list of user ratings for article: " + articleURI + " and user: "
                    + user.getUserId());
          }

          List ratingsList =
            session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleURI))
                  .add(Restrictions.eq("creator", user.getUserId())).list();

          if (ratingsList.size() > 0) {
            hasRated = true;
          }
        }
        return SUCCESS;
      }
    });

  }

  /** 
	 * Set the fetch article service
   * @param fetchArticleService fetchArticleService
   */
  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
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
   * Sets the URI of the article being rated.
   *
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) throws Exception {
    if(articleURI != null && articleURI.equals(this.articleURI)) {
      return;
    }
    this.articleURI = articleURI;
    
    // resolve article type and supported properties
    Article artInfo = fetchArticleService.getArticleInfo(articleURI); 
    assert artInfo != null : "artInfo is null (Should have already been cached.  Is the articleURI correct?)";
    ArticleType articleType = ArticleType.getKnownArticleTypeForURI(URI.create(articleURI));
    assert articleType != null;
    articleType = ArticleType.getDefaultArticleType();
    for (URI artTypeUri : artInfo.getArticleType()) {
      if (ArticleType.getKnownArticleTypeForURI(artTypeUri)!= null) {
        articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
        break;
      }
    }
    isResearchArticle = ArticleType.isResearchArticle(articleType);
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
    return insightAverage;
  }

  /**
   * Sets the insight ratinngs average.
   *
   * @param insightAverage The insightAverage to set.
   */
  public void setInsightAverage(double insightAverage) {
    this.insightAverage = insightAverage;
  }

  /**
   * Gets the count of insight ratings.
   *
   * @return Returns the numInsightRatings.
   */
  public int getNumInsightRatings() {
    return numInsightRatings;
  }

  /**
   * Sets the count of insight ratings.
   *
   * @param numInsightRatings The numInsightRatings to set.
   */
  public void setNumInsightRatings(int numInsightRatings) {
    this.numInsightRatings = numInsightRatings;
  }

  /**
   * Gets the count of reliability ratings.
   *
   * @return Returns the numReliabilityRatings.
   */
  public int getNumReliabilityRatings() {
    return numReliabilityRatings;
  }

  /**
   * Sets the count of reliability ratings.
   *
   * @param numReliabilityRatings The numReliabilityRatings to set.
   */
  public void setNumReliabilityRatings(int numReliabilityRatings) {
    this.numReliabilityRatings = numReliabilityRatings;
  }

  /**
   * @return the numSingleRatingRatings
   */
  public int getNumSingleRatingRatings() {
    return numSingleRatingRatings;
  }

  /**
   * @param numSingleRatingRatings the numSingleRatingRatings to set
   */
  public void setNumSingleRatingRatings(int numSingleRatingRatings) {
    this.numSingleRatingRatings = numSingleRatingRatings;
  }

  /**
   * Gets the count of style ratings.
   *
   * @return Returns the numStyleRatings.
   */
  public int getNumStyleRatings() {
    return numStyleRatings;
  }

  /**
   * Sets the count of style ratings.
   *
   * @param numStyleRatings The numStyleRatings to set.
   */
  public void setNumStyleRatings(int numStyleRatings) {
    this.numStyleRatings = numStyleRatings;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Gets the overall average ratings.
   *
   * @return Returns the overallAverage.
   */
  public double getOverallAverage() {

    return RatingContent.calculateOverall(getInsightAverage(), getReliabilityAverage(), getStyleAverage());
  }

  /**
   * Gets the reliability ratings average.
   *
   * @return Returns the reliabilityAverage.
   */
  public double getReliabilityAverage() {
    return reliabilityAverage;
  }

  /**
   * Sets the reliability ratings average.
   *
   * @param reliabilityAverage The reliabilityAverage to set.
   */
  public void setReliabilityAverage(double reliabilityAverage) {
    this.reliabilityAverage = reliabilityAverage;
  }

  /**
   * Gets the style ratings average.
   *
   * @return Returns the styleAverage.
   */
  public double getStyleAverage() {
    return styleAverage;
  }

  /**
   * Sets the style ratings average.
   *
   * @param styleAverage The styleAverage to set.
   */
  public void setStyleAverage(double styleAverage) {
    this.styleAverage = styleAverage;
  }

  /**
   * @return the singleRatingAverage
   */
  public double getSingleRatingAverage() {
    return singleRatingAverage;
  }

  /**
   * @param singleRatingAverage the singleRatingAverage to set
   */
  public void setSingleRatingAverage(double singleRatingAverage) {
    this.singleRatingAverage = singleRatingAverage;
  }

  /**
   * Gets the total on insight ratings.
   *
   * @return Returns the totalInsight.
   */
  public double getTotalInsight() {
    return totalInsight;
  }

  /**
   * Sets the total on insight ratings.
   *
   * @param totalInsight The totalInsight to set.
   */
  public void setTotalInsight(double totalInsight) {
    this.totalInsight = totalInsight;
  }

  /**
   * Gets the total on reliability ratings.
   *
   * @return Returns the totalReliability.
   */
  public double getTotalReliability() {
    return totalReliability;
  }

  /**
   * Sets the total on reliability ratings.
   *
   * @param totalReliability The totalReliability to set.
   */
  public void setTotalReliability(double totalReliability) {
    this.totalReliability = totalReliability;
  }

  /**
   * @return the totalSingleRating
   */
  public double getTotalSingleRating() {
    return totalSingleRating;
  }

  /**
   * @param totalSingleRating the totalSingleRating to set
   */
  public void setTotalSingleRating(double totalSingleRating) {
    this.totalSingleRating = totalSingleRating;
  }

  /**
   * Gets the total on style ratings.
   *
   * @return Returns the totalStyle.
   */
  public double getTotalStyle() {
    return totalStyle;
  }

  /**
   * Sets the total on style ratings.
   *
   * @param totalStyle The totalStyle to set.
   */
  public void setTotalStyle(double totalStyle) {
    this.totalStyle = totalStyle;
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
   * Sets a flag to indicate that an article has been rated.
   *
   * @param hasRated The hasRated to set.
   */
  public void setHasRated(boolean hasRated) {
    this.hasRated = hasRated;
  }

  /**
   * Gets the rounded average on insight ratings.
   *
   * @return Returns the insightRoundedAverage.
   */
  public double getInsightRoundedAverage() {
    return insightRoundedAverage;
  }

  /**
   * Sets the rounded average on insight ratings.
   *
   * @param insightRoundedAverage The insightRoundedAverage to set.
   */
  public void setInsightRoundedAverage(double insightRoundedAverage) {
    this.insightRoundedAverage = insightRoundedAverage;
  }

  /**
   * Gets the overall rounded average.
   *
   * @return Returns the overallRoundedAverage.
   */
  public double getOverallRoundedAverage() {

    return RatingContent.roundTo(getOverallAverage(), 0.5);
  }

  /**
   * Gets the rounded average on reliability ratings.
   *
   * @return Returns the reliabilityRoundedAverage.
   */
  public double getReliabilityRoundedAverage() {
    return reliabilityRoundedAverage;
  }

  /**
   * Sets the rounded average on reliability ratings.
   *
   * @param reliabilityRoundedAverage The reliabilityRoundedAverage to set.
   */
  public void setReliabilityRoundedAverage(double reliabilityRoundedAverage) {
    this.reliabilityRoundedAverage = reliabilityRoundedAverage;
  }

  /**
   * @return the singleRatingRoundedAverage
   */
  public double getSingleRatingRoundedAverage() {
    return singleRatingRoundedAverage;
  }

  /**
   * @param singleRatingRoundedAverage the singleRatingRoundedAverage to set
   */
  public void setSingleRatingRoundedAverage(double singleRatingRoundedAverage) {
    this.singleRatingRoundedAverage = singleRatingRoundedAverage;
  }

  /**
   * Gets the rounded average on style ratings.
   *
   * @return Returns the styleRoundedAverage.
   */
  public double getStyleRoundedAverage() {
    return styleRoundedAverage;
  }

  /**
   * Sets the rounded average on style ratings.
   *
   * @param styleRoundedAverage The styleRoundedAverage to set.
   */
  public void setStyleRoundedAverage(double styleRoundedAverage) {
    this.styleRoundedAverage = styleRoundedAverage;
  }

  /**
   * Gets the number of users that rated.
   *
   * @return Number of users that rated.
   */
  public int getNumUsersThatRated() {
    return numUsersThatRated;
  }
}
