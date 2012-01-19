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
package org.plos.rating.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.RatingSummaryContent;
import org.plos.rating.service.RatingsPEP;
import org.plos.user.PlosOneUser;
import org.plos.util.ProfanityCheckingService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * General Rating action class to store and retrieve a users's rating
 *
 * @author Stephen Cheng
 */
@SuppressWarnings("serial")
public class RateAction extends AbstractRatingAction {
  private static final Log log = LogFactory.getLog(RateAction.class);

  private double           insight;
  private double           reliability;
  private double           style;
  private double           singleRating;
  private String           articleURI;
  private boolean          isResearchArticle;
  private String           commentTitle;
  private String           comment;
  private Session          session;
  private RatingsPEP       pep;
  private ProfanityCheckingService profanityCheckingService;

  private RatingsPEP getPEP() {
    try {
      if (pep == null)
        pep                      = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
    return pep;
  }

  /**
   * Rates an article for the currently logged in user.  Will look to see if there are
   * existing rating. If so, will update the ratings, otherwise will insert new ones.
   *
   * @return WebWork action status
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public String rateArticle() {
    final PlosOneUser       user               = PlosOneUser.getCurrentUser();
    final Date              now                = new Date(System.currentTimeMillis());
    final URI               annotatedArticle;

    try {
      annotatedArticle = new URI(articleURI);
    } catch (URISyntaxException ue) {
      log.info("Could not construct article URI: " + articleURI, ue);
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

      return ERROR;
    }

    if (user == null) {
      log.info("User is null for rating articles");
      addActionError("Must be logged in");
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

      return ERROR;
    }

    getPEP().checkObjectAccess(RatingsPEP.SET_RATINGS, URI.create(user.getUserId()), annotatedArticle);

    try {
      isResearchArticle = isResearchArticle(articleURI);
    } catch (ApplicationException ae) {
      log.info("Could not get article info for: " + articleURI, ae);
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ERROR;
    }

    if (isResearchArticle) {
      // must rate at least one rating category
      if (insight == 0 && reliability == 0 && style == 0) {
        addActionError("At least one category must be rated");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return INPUT;
      }
    } else {
      // ensure the single rating specified
      if (singleRating == 0) {
        addActionError("A rating must be specified.");
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return INPUT;
      }
    }

    // reject profanity in content
    final List<String> profaneWordsInCommentTitle = profanityCheckingService.validate(commentTitle);
    final List<String> profaneWordsInComment      = profanityCheckingService.validate(comment);
    if (profaneWordsInCommentTitle.size() != 0 || profaneWordsInComment.size() != 0) {
      addProfaneMessages(profaneWordsInCommentTitle, "commentTitle", "title");
      addProfaneMessages(profaneWordsInComment, "comment", "comment");
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return INPUT;
    }

    Rating        articleRating        = null;
    RatingSummary articleRatingSummary = null;
    boolean       newRating            = false;

    if (log.isDebugEnabled()) {
      log.debug("Retrieving user Ratings for article: " + articleURI + " and user: "
              + user.getUserId());
    }

    // Ratings by this User for Article
    List<Rating> ratingsList = session
      .createCriteria(Rating.class)
      .add(Restrictions.eq("annotates", articleURI))
      .add(Restrictions.eq("creator", user.getUserId()))
      .list();
    if (ratingsList.size() == 0) {
      newRating = true;
      articleRating = new Rating();
      articleRating.setAnnotates(annotatedArticle);
      articleRating.setContext("");
      articleRating.setCreator(user.getUserId());
      articleRating.setCreated(now);
      articleRating.setBody(new RatingContent());

      session.saveOrUpdate(articleRating);
    } else if (ratingsList.size() == 1) {
      articleRating = ratingsList.get(0);
    } else {
      // should never happen
      String errorMessage = "Multiple Ratings, " + ratingsList.size() + ", for Article, "
        + articleURI + ", for user, " + user.getUserId();
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }

    // RatingsSummary for Article
    List<RatingSummary> summaryList = session
      .createCriteria(RatingSummary.class)
      .add(Restrictions.eq("annotates", articleURI))
      .list();
    if (summaryList.size() == 0) {
      articleRatingSummary = new RatingSummary();
      articleRatingSummary.setAnnotates(annotatedArticle);
      articleRatingSummary.setContext("");
      articleRatingSummary.setCreated(now);
      articleRatingSummary.setBody(new RatingSummaryContent());

      session.saveOrUpdate(articleRatingSummary);
    } else if (summaryList.size() == 1) {
      articleRatingSummary = summaryList.get(0);
    } else {
      // should never happen
      String errorMessage = "Multiple RatingsSummary, " + summaryList.size() + ", for Article "
        + articleURI;
      log.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }

    // update Rating + RatingSummary with new values
    articleRating.setCreated(now);
    articleRatingSummary.setCreated(now);

    // if they had a prior insight Rating, remove it from the RatingSummary
    if (articleRating.getBody().getInsightValue() > 0) {
      articleRatingSummary.getBody().
        removeRating(Rating.INSIGHT_TYPE, articleRating.getBody().getInsightValue());
    }

    // update insight Article Rating, don't care if new, update or 0
    articleRating.getBody().setInsightValue((int) insight);

    // if user rated insight, add to to Article RatingSummary
    if (insight > 0) {
      articleRatingSummary.getBody().addRating(Rating.INSIGHT_TYPE, (int) insight);
    }

    // if they had a prior reliability Rating, remove it from the RatingSummary
    if (articleRating.getBody().getReliabilityValue() > 0) {
      articleRatingSummary.getBody().
        removeRating(Rating.RELIABILITY_TYPE, articleRating.getBody().getReliabilityValue());
    }

    // update reliability Article Rating, don't care if new, update or 0
    articleRating.getBody().setReliabilityValue((int) reliability);

    // if user rated reliability, add to to Article RatingSummary
    if (reliability > 0) {
      articleRatingSummary.getBody().addRating(Rating.RELIABILITY_TYPE, (int) reliability);
    }

    // if they had a prior style Rating, remove it from the RatingSummary
    if (articleRating.getBody().getStyleValue() > 0) {
      articleRatingSummary.getBody().
        removeRating(Rating.STYLE_TYPE, articleRating.getBody().getStyleValue());
    }

    // update style Article Rating, don't care if new, update or 0
    articleRating.getBody().setStyleValue((int) style);

    // if user rated style, add to to Article RatingSummary
    if (style > 0) {
      articleRatingSummary.getBody().addRating(Rating.STYLE_TYPE, (int) style);
    }

    // if they had a prior single Rating, remove it from the RatingSummary
    if (articleRating.getBody().getSingleRatingValue() > 0) {
      articleRatingSummary.getBody().
        removeRating(Rating.SINGLE_RATING_TYPE, articleRating.getBody().getSingleRatingValue());
    }

    // update single Article Rating, don't care if new, update or 0
    articleRating.getBody().setSingleRatingValue((int) singleRating);

    // if user rated single, add to to Article RatingSummary
    if (singleRating > 0) {
      articleRatingSummary.getBody().addRating(Rating.SINGLE_RATING_TYPE, (int) singleRating);
    }

    // Rating comment
    articleRating.getBody().setCommentTitle(commentTitle);
    articleRating.getBody().setCommentValue(comment);

    // if this is a new Rating, the summary needs to know
    if (newRating) {
      articleRatingSummary.getBody().
        setNumUsersThatRated(articleRatingSummary.getBody().getNumUsersThatRated() + 1);
    }

    return SUCCESS;
  }

  /**
   * Get the ratings and comment for the logged in user
   *
   * @return WebWork action status
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public String retrieveRatingsForUser() {
    final PlosOneUser user = PlosOneUser.getCurrentUser();

    if (user == null) {
      log.info("User is null for retrieving user ratings");
      addActionError("Must be logged in");

      return ERROR;
    }

    getPEP().checkObjectAccess(RatingsPEP.GET_RATINGS, URI.create(user.getUserId()),
        URI.create(articleURI));

    List<Rating> ratingsList = session.createCriteria(Rating.class)
      .add(Restrictions.eq("annotates", articleURI))
      .add(Restrictions.eq("creator", user.getUserId())).list();

    if (ratingsList.size() < 1) {
      log.debug("didn't find any matching ratings for user: " + user.getUserId());
      addActionError("No ratings for user");
      return ERROR;
    }

    Rating rating = ratingsList.get(0);

    setInsight(rating.getBody().getInsightValue());
    setReliability(rating.getBody().getReliabilityValue());
    setStyle(rating.getBody().getStyleValue());
    setSingleRating(rating.getBody().getSingleRatingValue());

    setCommentTitle(rating.getBody().getCommentTitle());
    setComment(rating.getBody().getCommentValue());

    return SUCCESS;
  }

  /**
   * Gets the URI of the article being rated.
   *
   * @return Returns the articleURI.
   */
  @RequiredStringValidator(message = "Article URI is a required field")
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Sets the URI of the article being rated.
   *
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) {
    if (articleURI != null && articleURI.equals(this.articleURI)) {
      return;
    }
    this.articleURI = articleURI;
  }

  /**
   * @return the isResearchArticle
   */
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  /**
   * Gets the style rating.
   *
   * @return Returns the style.
   */
  public double getStyle() {
    return style;
  }

  /**
   * Sets the style rating.
   *
   * @param style The elegance to set.
   */
  public void setStyle(double style) {
    this.style = style;
  }

  /**
   * Gets the insight rating.
   *
   * @return Returns the insight.
   */
  public double getInsight() {
    return insight;
  }

  /**
   * Sets the insight rating.
   *
   * @param insight The insight to set.
   */
  public void setInsight(double insight) {
    if (log.isDebugEnabled())
      log.debug("setting insight to: " + insight);

    this.insight = insight;
  }

  /**
   * Gets the reliability rating.
   *
   * @return Returns the security.
   */
  public double getReliability() {
    return reliability;
  }

  /**
   * Sets the reliability rating.
   *
   * @param reliability The security to set.
   */
  public void setReliability(double reliability) {
    this.reliability = reliability;
  }

  /**
   * Gets the overall rating.
   *
   * @return Returns the overall.
   */
  public double getOverall() {
    return RatingContent.calculateOverall(getInsight(), getReliability(), getStyle());
  }

  /**
   * @return the singleRating
   */
  public double getSingleRating() {
    return singleRating;
  }

  /**
   * @param singleRating the singleRating to set
   */
  public void setSingleRating(double singleRating) {
    this.singleRating = singleRating;
  }

  /**
   * Gets the rating comment.
   *
   * @return Returns the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the ratings comment.
   *
   * @param comment The comment to set.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Gets the rating comment title.
   *
   * @return Returns the commentTitle.
   */
  public String getCommentTitle() {
    return commentTitle;
  }

  /**
   * Sets the rating comment title.
   *
   * @param commentTitle The commentTitle to set.
   */
  public void setCommentTitle(String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the profanityCheckingService.
   *
   * @param profanityCheckingService profanityCheckingService
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }
}
