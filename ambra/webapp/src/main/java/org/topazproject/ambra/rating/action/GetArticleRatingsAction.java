/*$HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingContent;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.RatingSummaryContent;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.rating.service.RatingsPEP;
import org.topazproject.ambra.rating.service.RatingsService;
import org.topazproject.ambra.rating.service.RatingsService.AverageRatings;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;

import com.sun.xacml.PDP;

/**
 * Rating action class to retrieve all ratings for an Article.
 *
 * @author Jeff Suttor
 */
@SuppressWarnings("serial")
public class GetArticleRatingsAction extends AbstractRatingAction {
  protected static final Logger log = LoggerFactory.getLogger(GetArticleRatingsAction.class);
  private RatingsService ratingsService;

  private Session session;
  private String articleURI;
  private String  articleTitle;
  private String articleDescription;
  private boolean isResearchArticle;
  private AverageRatings averageRatings;
  private final List<ArticleRatingSummary> articleRatingSummaries =
                                             new ArrayList<ArticleRatingSummary>();
  private double articleOverall = 0;
  private double articleSingleRating = 0;
  private RatingsPEP pep;

  private RatingsPEP getPEP() {
    return pep;
  }

  @Required
  public void setRatingsPdp(PDP pdp) {
    this.pep = new RatingsPEP(pdp);
  }

  /**
   * Execute the ratings action.
   *
   * @return WebWork action status
   */
  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public String execute() throws Exception {
    assert articleURI != null : "An article URI must be specified";
    URI articleId = URI.create(articleURI);
    getPEP().checkAccess(RatingsPEP.GET_RATINGS, articleId);

    final Article article = articleOtmService.getArticle(articleId);
    assert article != null : "article of URI: " + articleURI + " not found.";

    articleTitle = article.getDublinCore().getTitle();
    articleDescription = article.getDublinCore().getDescription();
    averageRatings = ratingsService.getAverageRatings(articleURI);

    isResearchArticle = articleOtmService.isResearchArticle(articleURI);

    // assume if valid RatingsPEP.GET_RATINGS, OK to GET_STATS
    // RatingSummary for this Article
    List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class).add(
        Restrictions.eq("annotates", articleURI)).list();

    if(summaryList.size() == 1) {
      RatingSummaryContent rsc = summaryList.get(0).getBody();
      articleOverall = rsc.getOverall();
      articleSingleRating = rsc.getSingleRating();
    } else {
      log.warn("Unexpected: " + summaryList.size() + " RatingSummary for " + articleURI);
      articleOverall = 0;
      articleSingleRating = 0;
    }

    // list of Ratings that annotate this article
    List<Rating> articleRatings = session.createCriteria(Rating.class).add(
        Restrictions.eq("annotates", articleURI)).addOrder(new Order("created", true)).list();

    if(log.isDebugEnabled()) {
      log.debug("retrieved all ratings, " + articleRatings.size() + ", for: " + articleURI);
    }

    // create ArticleRatingSummary(s)
    for (Rating rating : articleRatings) {
      ArticleRatingSummary summary = new ArticleRatingSummary(getArticleURI(), getArticleTitle());
      summary.setRating(rating);
      summary.setCreated(rating.getCreated());
      summary.setArticleURI(getArticleURI());
      summary.setArticleTitle(getArticleTitle());
      summary.setCreatorURI(rating.getCreator());
      // get public 'name' for user
      UserAccount ua = session.get(UserAccount.class, rating.getCreator());
      if (ua != null) {
        summary.setCreatorName(ua.getProfile().getDisplayName());
      } else {
        summary.setCreatorName("Unknown");
        log.error("Unable to look up UserAccount for " + rating.getCreator() +
                  " for Rating " + rating.getId());
      }
      articleRatingSummaries.add(summary);
    }

    if(log.isDebugEnabled()) {
      log.debug("created ArticleRatingSummaries, " + articleRatingSummaries.size() +
                ", for: " + articleURI);
    }

    return SUCCESS;
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
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Gets the Overall rounded rating of the article being rated.
   *
   * @return Overall rounded rating.
   */
  public double getArticleOverallRounded() {

    return RatingContent.roundTo(articleOverall, 0.5);
  }

  /**
   * Gets the single rounded rating of the article being rated.
   *
   * @return Single rounded rating.
   */
  public double getArticleSingleRatingRounded() {

    return RatingContent.roundTo(articleSingleRating, 0.5);
  }

  /**
   * Gets the title of the article being rated.
   *
   * @return Returns the articleTitle.
   */
  public String getArticleTitle() {

    if(articleTitle != null) {
      return articleTitle;
    }

    articleTitle = "Article title place holder for testing, resolve " + articleURI;
    return articleTitle;
  }

  /**
   * Sets the title of the article being rated.
   *
   * @param articleTitle The article's title.
   */
  public void setArticleTitle(String articleTitle) {
    this.articleTitle = articleTitle;
  }

  /**
   * Gets the description of the Article being rated.
   *
   * @return Returns the articleDescription.
   */
  public String getArticleDescription() {
    if(articleDescription != null) {
      return articleDescription;
    }

    articleDescription = "Article Description place holder for testing, resolve " + articleURI;
    return articleDescription;
  }

  /**
   * @return the isResearchArticle
   */
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  /**
   * @param isResearchArticle the isResearchArticle to set
   */
  public void setIsResearchArticle(boolean isResearchArticle) {
    this.isResearchArticle = isResearchArticle;
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
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Gets all ratings for the Article.
   *
   * @return Returns Ratings for the Article.
   */
  public Collection<ArticleRatingSummary> getArticleRatingSummaries() {
    return articleRatingSummaries;
  }

  /*
  * Gets averageRatings info
  *
  * @return returns averageRatings info
  */
  public RatingsService.AverageRatings getAverageRatings() {
    return averageRatings;
  }

 /**
  * Has this article been rated?
  *
  * @return true if the article has been rated
  */
  public boolean getHasRated() {
    return (articleRatingSummaries.size() > 0);
  }
}
