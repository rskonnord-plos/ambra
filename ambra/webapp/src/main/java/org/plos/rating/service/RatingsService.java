/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.rating.service;

import static org.plos.annotation.service.BaseAnnotation.FLAG_MASK;
import static org.plos.annotation.service.BaseAnnotation.PUBLIC_MASK;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.models.Article;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.RatingSummaryContent;
import org.plos.user.PlosOneUser;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.Interceptor.Updates;

/**
 * This service allows client code to operate on ratings objects.
 *
 * @author jonnie.
 */
public class RatingsService {
  private static final Log     log = LogFactory.getLog(RatingsService.class);
  private static final String AVG_RATINGS_KEY = "Avg-Ratings-";
  private Session              session;
  private RatingsPEP           pep;
  private Cache articleAnnotationCache;
  private Invalidator          invalidator;
  private String               applicationId;

  public RatingsService() {
    try {
      pep = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
  }

  /**
   * Unflag a Rating
   *
   * @param ratingId the identifier of the Rating object to be unflagged
   * @throws ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void unflagRating(final String ratingId)
                  throws ApplicationException {
    setPublic(ratingId);
  }

  /**
   * Delete the Rating identified by ratingId and update the RatingSummary.
   *
   * @param ratingId the identifier of the Rating object to be deleted
   * @throws ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteRating(final String ratingId)
                         throws ApplicationException {
    pep.checkAccess(RatingsPEP.DELETE_RATINGS, URI.create(ratingId));

    final Rating articleRating = session.get(Rating.class, ratingId);
    if (articleRating == null) {
      throw new ApplicationException("Failed to get Rating to delete: "+ ratingId);
    }

    final URI articleURI = articleRating.getAnnotates();
    final List<RatingSummary> summaryList = session
      .createCriteria(RatingSummary.class)
      .add(Restrictions.eq("annotates",articleURI.toString()))
      .list();

    if (summaryList.size() <= 0) {
      throw new ApplicationException("No RatingSummary object found for article " + articleURI.toString());
    }
    if (summaryList.size() > 1) {
      throw new ApplicationException("Multiple RatingSummary objects found found " +
                                  "for article " + articleURI.toString());
    }

    final RatingSummary articleRatingSummary = summaryList.get(0);
    final int newNumberOfRatings = articleRatingSummary.getBody().getNumUsersThatRated() - 1;
    final int insight = articleRating.getBody().getInsightValue();
    final int reliability = articleRating.getBody().getReliabilityValue();
    final int style = articleRating.getBody().getStyleValue();
    final int single = articleRating.getBody().getSingleRatingValue();

    articleRatingSummary.getBody().setNumUsersThatRated(newNumberOfRatings);
    if (insight > 0) {
      articleRatingSummary.getBody().removeRating(Rating.INSIGHT_TYPE,insight);
    }
    if (reliability > 0) {
      articleRatingSummary.getBody().removeRating(Rating.RELIABILITY_TYPE,reliability);
    }
    if (style > 0) {
      articleRatingSummary.getBody().removeRating(Rating.STYLE_TYPE,style);
    }
    if (single > 0) {
      articleRatingSummary.getBody().removeRating(Rating.SINGLE_RATING_TYPE,single);
    }

    articleAnnotationCache.put(AVG_RATINGS_KEY + articleURI,
        new AverageRatings(articleRatingSummary));
    session.delete(articleRating);
  }

  /**
   * Set the annotation as public.
   *
   * @param ratingId the id of the Rating
   * @throws ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setPublic(final String ratingId) throws ApplicationException {
    Rating r = session.get(Rating.class,ratingId);
    r.setState(PUBLIC_MASK);
  }

  /**
   * Set the rating as flagged.
   *
   * @param ratingId the id of rating object
   * @throws ApplicationException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setFlagged(final String ratingId) throws ApplicationException {
    Rating r = session.get(Rating.class,ratingId);
    r.setState(PUBLIC_MASK | FLAG_MASK);
  }

  /**
   * Get a Rating by Id.
   *
   * @param ratingId Rating Id
   *
   * @return Rating
   */
  @Transactional(readOnly = true)
  public Rating getRating(final String ratingId) {
    Rating rating = session.get(Rating.class, ratingId);

    // the PEP check is against what is rated,
    // e.g. can this user see the ratings for what is rated?
    PlosOneUser user = PlosOneUser.getCurrentUser();
    pep.checkObjectAccess(RatingsPEP.GET_RATINGS, URI.create(user.getUserId()),
                          rating.getAnnotates());

    return rating;
  }

  /**
   * List the set of Ratings in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state    the state to filter the list of annotations by or 0 to return annotations
   *                 in any administrative state
   * @return an array of rating metadata; if no matching annotations are found, an empty array
   *         is returned
   */
  @Transactional(readOnly = true)
  public Rating[] listRatings(final String mediator,final int state) {
    Criteria c = session.createCriteria(Rating.class);

    if (mediator != null)
      c.add(Restrictions.eq("mediator",mediator));

    if (state == 0) {
      c.add(Restrictions.ne("state", "0"));
    } else {
      c.add(Restrictions.eq("state", "" + state));
    }

    return (Rating[]) c.list().toArray(new Rating[0]);
  }

  @Transactional(readOnly = true)
  public AverageRatings getAverageRatings(final String articleURI) {
    pep.checkAccess(RatingsPEP.GET_STATS, URI.create(articleURI));

    return articleAnnotationCache.get(AVG_RATINGS_KEY  + articleURI, -1,
            new Cache.SynchronizedLookup<AverageRatings, OtmException>(articleURI.intern()) {
              public AverageRatings lookup() throws OtmException {
                if (log.isDebugEnabled())
                  log.debug("retrieving rating summaries for: " + articleURI);
                List<RatingSummary> summaryList = session
                                    .createCriteria(RatingSummary.class)
                                    .add(Restrictions.eq("annotates", articleURI))
                                    .list();
                return (summaryList.size() > 0) ? new AverageRatings(summaryList.get(0)) : new AverageRatings();
              }
            });
  }

  @Transactional(readOnly = true)
  public boolean hasRated(String articleURI) {
    final PlosOneUser   user               = PlosOneUser.getCurrentUser();

    if (user != null) {
      if (log.isDebugEnabled()) {
        log.debug("retrieving list of user ratings for article: " + articleURI + " and user: "
                + user.getUserId());
      }

      List ratingsList =
        session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleURI))
              .add(Restrictions.eq("creator", user.getUserId())).list();

      if (ratingsList.size() > 0) {
        return true;
      }
    }
    return false;
  }


  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the id of the application
   * @param applicationId applicationId
   */
  @Required
  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      articleAnnotationCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * @return the application id
   */
  public String getApplicationId() {
    return applicationId;
  }

  public static class Average implements Serializable {
    public final double total;
    public final int    count;
    public final double average;
    public final double rounded;

    Average(double total, int count) {
      this.total = total;
      this.count = count;
      average = (count == 0) ? 0 : total/count;
      rounded = RatingContent.roundTo(average, 0.5);
    }

    public String toString() {
      return "total = " + total + ", count = " + count + ", average = " + average
        + ", rounded = " + rounded;
    }
  }

  public static class AverageRatings implements Serializable {
    public final Average style;
    public final Average insight;
    public final Average reliability;
    public final Average single;
    public final int numUsersThatRated;
    public final double overall;
    public final double roundedOverall;

    AverageRatings() {
      style = new Average(0, 0);
      insight = new Average(0, 0);
      reliability = new Average(0, 0);
      single = new Average(0, 0);
      numUsersThatRated = 0;
      overall = 0;
      roundedOverall = 0;
    }

    AverageRatings(RatingSummary ratingSummary) {
      insight = new Average(ratingSummary.getBody().getInsightTotal(),
         ratingSummary.getBody().getInsightNumRatings());
      reliability = new Average(ratingSummary.getBody().getReliabilityTotal(),
         ratingSummary.getBody().getReliabilityNumRatings());
      style = new Average(ratingSummary.getBody().getStyleTotal(),
         ratingSummary.getBody().getStyleNumRatings());
      single = new Average(ratingSummary.getBody().getSingleRatingTotal(),
         ratingSummary.getBody().getSingleRatingNumRatings());

      numUsersThatRated     = ratingSummary.getBody().getNumUsersThatRated();
      overall = RatingContent.calculateOverall(insight.average, reliability.average, style.average);
      roundedOverall = RatingContent.roundTo(overall, 0.5);
    }

    public String toString() {
      return "style = [" + style + "], insight = [" + insight + "], reliability = [" + reliability
        + "], single = [" + single + "], numUsersThatRated = " + numUsersThatRated + ", overall = "
        + overall + ", roundedOverall = " + roundedOverall;
    }
  }

  private class Invalidator implements ObjectListener {
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      if (o instanceof RatingSummary) {
        if (log.isDebugEnabled())
          log.debug("Invalidating ratings-summary cache entry since it changed");
        articleAnnotationCache.remove(AVG_RATINGS_KEY + ((RatingSummary)o).getAnnotates());
      }
    }
    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      if (o instanceof Article) {
        if (log.isDebugEnabled())
          log.debug("Invalidating ratings-summary cache entry for the article that was deleted.");
        articleAnnotationCache.remove(AVG_RATINGS_KEY + id);
      } else if (o instanceof RatingSummary) {
        if (log.isDebugEnabled())
          log.debug("Invalidating ratings-summary cache entry that was deleted.");
        articleAnnotationCache.remove(AVG_RATINGS_KEY + ((RatingSummary)o).getAnnotates());
      }
    }
  }
}
