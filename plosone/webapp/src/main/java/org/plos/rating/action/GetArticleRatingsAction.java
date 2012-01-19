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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.googlecode.jsonplugin.annotations.JSON;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.article.service.ArticleOtmService;
import org.plos.models.Article;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.UserAccount;
import org.plos.rating.service.RatingsPEP;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;

import org.springframework.beans.factory.annotation.Required;

/**
 * Rating action class to retrieve all ratings for an Article.
 *
 * @author Jeff Suttor
 */
public class GetArticleRatingsAction extends BaseActionSupport {

  private Session            session;
  private ArticleOtmService  articleOtmService;
  private String             articleURI;
  private String             articleTitle;
  private String             articleDescription;
  private boolean            hasRated = false;
  private List<ArticleRatingSummary> articleRatingSummaries = new ArrayList();
  private double             articleOverall = 0;
  private static final Log log = LogFactory.getLog(GetArticleRatingsAction.class);
  private RatingsPEP pep;

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
   * Execute the ratings action.
   *
   * @return WebWork action status
   */
  public String execute() throws Exception {
    Transaction tx  = null;

    getPEP().checkAccess(RatingsPEP.GET_RATINGS, URI.create(articleURI));

    // fill in Article title if necessary
    // TODO, expose more of the Article metadata, need a articleOtmService.getArticleInfo(URI)
    Article article = null;
    if (articleTitle == null) {
      article = articleOtmService.getArticle(URI.create(articleURI));
      articleTitle = article.getDublinCore().getTitle();
    }
    if (articleDescription == null) {
      if (article == null) {
        article = articleOtmService.getArticle(URI.create(articleURI));
      }
      articleDescription = article.getDublinCore().getDescription();
    }

    try {
      tx = session.beginTransaction();

      // assume if valid RatingsPEP.GET_RATINGS, OK to GET_STATS
      // RatingSummary for this Article
      List<RatingSummary> summaryList = session
        .createCriteria(RatingSummary.class)
        .add(Restrictions.eq("annotates", article.getId()))
        .list();
      if (summaryList.size() == 1) {
        articleOverall = summaryList.get(0).getBody().getOverall();
      } else {
        // unexpected
        log.warn("Unexpected: " + summaryList.size() + " RatingSummary for " + article.getId());
        articleOverall = 0;
      }

      // list of Ratings that annotate this article
      List<Rating> articleRatings = session
        .createCriteria(Rating.class)
        .add(Restrictions.eq("annotates", articleURI))
        .addOrder(new Order("created", true))
        .list();

      if (log.isDebugEnabled()) {
        log.debug("retrieved all ratings, " + articleRatings.size() + ", for: " + articleURI);
      }

      // create ArticleRatingSummary(s)
      Iterator iter = articleRatings.iterator();
      while (iter.hasNext()) {
        Rating rating = (Rating) iter.next();
        ArticleRatingSummary summary = new ArticleRatingSummary(getArticleURI(),getArticleTitle());
        summary.addRating(rating);
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
          log.error("Unable to look up UserAccount for " + rating.getCreator() + " for Rating " + rating.getId());
        }
        articleRatingSummaries.add(summary);
      }

      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    }

    if (articleRatingSummaries.size() > 0) {
      hasRated = true;
    }

    if (log.isDebugEnabled()) {
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
   * Gets the title of the article being rated.
   *
   * @return Returns the articleTitle.
   */
  public String getArticleTitle() {

    if (articleTitle != null) {
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

    if (articleDescription != null) {
      return articleDescription;
    }

    articleDescription = "Article Description place holder for testing, resolve " + articleURI;
    return articleDescription;
  }

  /**
   * Gets the otm session.
   *
   * @return Returns the otm session.
   */
  @JSON(serialize = false)
  public Session getOtmSession() {
    return session;
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
   * Gets the ArticleOtmService.
   * Use ArticleOtmService v native OTM as it's aware of Article semantics.
   *
   * @return The ArticleOtmService.
   */
  @JSON(serialize = false)
  public ArticleOtmService getArticleOtmService() {
    return articleOtmService;
  }

  /**
   * Sets the ArticleOtmService.
   * Use ArticleOtmService v native OTM as it's aware of Article semantics.
   * Called by Spring's bean wiring.
   *
   * @param ArticleOtmService to set.
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
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
