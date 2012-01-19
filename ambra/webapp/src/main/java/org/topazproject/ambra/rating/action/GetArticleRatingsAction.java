/*$HeadURL::                                                                            $
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingContent;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.RatingSummaryContent;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.rating.service.RatingsPEP;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;

/**
 * Rating action class to retrieve all ratings for an Article.
 *
 * @author Jeff Suttor
 */
@SuppressWarnings("serial")
public class GetArticleRatingsAction extends AbstractRatingAction {
  protected static final Log log = LogFactory.getLog(GetArticleRatingsAction.class);

  private Session session;
  private String articleURI;
  private String articleTitle;
  private String articleDescription;
  private boolean isResearchArticle;
  private boolean hasRated = false;
  private final List<ArticleRatingSummary> articleRatingSummaries = new ArrayList<ArticleRatingSummary>();
  private double articleOverall = 0;
  private double articleSingleRating = 0;
  private RatingsPEP pep;

  private RatingsPEP getPEP() {
    try {
      if (pep == null)
        pep                           = new RatingsPEP();
    } catch(Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
    return pep;
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
    getPEP().checkAccess(RatingsPEP.GET_RATINGS, URI.create(articleURI));

    final Article article = fetchArticleService.getArticleInfo(articleURI);
    assert article != null : "article of URI: " + articleURI + " not found.";

    articleTitle = article.getDublinCore().getTitle();
    articleDescription = article.getDublinCore().getDescription();

    isResearchArticle = isResearchArticle(articleURI);

    // assume if valid RatingsPEP.GET_RATINGS, OK to GET_STATS
    // RatingSummary for this Article
    List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class).add(
        Restrictions.eq("annotates", articleURI)).list();

    if(summaryList.size() == 1) {
      RatingSummaryContent rsc = summaryList.get(0).getBody();
      articleOverall = rsc.getOverall();
      articleSingleRating = rsc.getSingleRating();
    } else {
      // unexpected
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
    Iterator<Rating> iter = articleRatings.iterator();
    while(iter.hasNext()) {
      Rating rating = iter.next();
      ArticleRatingSummary summary = new ArticleRatingSummary(
          getArticleURI(), getArticleTitle());
      summary.setRating(rating);
      summary.setCreated(rating.getCreated());
      summary.setArticleURI(getArticleURI());
      summary.setArticleTitle(getArticleTitle());
      summary.setCreatorURI(rating.getCreator());
      // get public 'name' for user
      UserAccount ua = session.get(UserAccount.class, rating.getCreator());
      if(ua != null) {
        summary.setCreatorName(ua.getProfile().getDisplayName());
      } else {
        summary.setCreatorName("Unknown");
      log.error("Unable to look up UserAccount for " + rating.getCreator() + " for Rating " + rating.getId());
      }
      articleRatingSummaries.add(summary);
    }

    if(articleRatingSummaries.size() > 0) {
      hasRated = true;
    }

    if(log.isDebugEnabled()) {
      log.debug("created ArticleRatingSummaries, " + articleRatingSummaries.size() + ", for: " + articleURI);
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
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
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
   * Gets all ratings for the Article.
   *
   * @return Returns Ratings for the Article.
   */
  public Collection<ArticleRatingSummary> getArticleRatingSummaries() {

    return articleRatingSummaries;
  }
}
