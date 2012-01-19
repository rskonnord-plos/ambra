/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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
package org.topazproject.ambra.rating.service;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingContent;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.user.AmbraUser;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.criterion.Restrictions;

import com.sun.xacml.PDP;

/**
 * This service allows client code to operate on ratings objects.
 *
 * @author jonnie.
 */
public class RatingsService {
  private static final Logger     log = LoggerFactory.getLogger(RatingsService.class);
  private static final String AVG_RATINGS_KEY = "Avg-Ratings-";

  private Session              session;
  private RatingsPEP           pep;
  private Cache                articleAnnotationCache;
  private Invalidator          invalidator;
  private String               applicationId;

  public RatingsService() {
  }

  @Required
  public void setRatingsPdp(PDP pdp) {
    pep = new RatingsPEP(pdp);
  }

  /**
   * Delete the Rating identified by ratingId and update the RatingSummary.
   *
   * @param ratingId the identifier of the Rating object to be deleted
   * @throws ApplicationException on an error
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteRating(final String ratingId) throws ApplicationException {
    pep.checkAccess(RatingsPEP.DELETE_RATINGS, URI.create(ratingId));

    final Rating articleRating = session.get(Rating.class, ratingId);
    if (articleRating == null) {
      throw new ApplicationException("Failed to get Rating to delete: "+ ratingId);
    }

    final URI articleURI = articleRating.getAnnotates();
    final List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class).
      add(Restrictions.eq("annotates",articleURI.toString())).list();

    if (summaryList.size() <= 0) {
      throw new ApplicationException("No RatingSummary object found for article " +
                                     articleURI.toString());
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
    removeRating(articleRatingSummary, Rating.INSIGHT_TYPE, insight);
    removeRating(articleRatingSummary, Rating.RELIABILITY_TYPE, reliability);
    removeRating(articleRatingSummary, Rating.STYLE_TYPE, style);
    removeRating(articleRatingSummary, Rating.SINGLE_RATING_TYPE, single);

    articleAnnotationCache.put(AVG_RATINGS_KEY + articleURI,
                               new Cache.Item(new AverageRatings(articleRatingSummary)));
    session.delete(articleRating);
  }

  private void removeRating(RatingSummary articleRatingSummary, String type, int rating) {
    if (rating > 0) {
      articleRatingSummary.getBody().removeRating(type, rating);
    }
  }

  /**
   * Get a Rating by Id.
   *
   * @param ratingId Rating Id
   * @param user current ambra user
   * @return Rating
   */
  @Transactional(readOnly = true)
  public Rating getRating(final String ratingId, AmbraUser user) {
    Rating rating = session.get(Rating.class, ratingId);

    /*
     * the PEP check is against what is rated,
     * e.g. can this user see the ratings for what is rated?
     */
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
  @SuppressWarnings("unchecked")
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

    return (Rating[]) c.list().toArray(new Rating[c.list().size()]);
  }

  @Transactional(readOnly = true)
  public AverageRatings getAverageRatings(final String articleURI) {
    pep.checkAccess(RatingsPEP.GET_STATS, URI.create(articleURI));

    return articleAnnotationCache.get(AVG_RATINGS_KEY  + articleURI, -1,
            new Cache.SynchronizedLookup<AverageRatings, OtmException>(articleURI.intern()) {
              @SuppressWarnings("unchecked")
              @Override
              public AverageRatings lookup() throws OtmException {
                if (log.isDebugEnabled())
                  log.debug("retrieving rating summaries for: " + articleURI);
                List<RatingSummary> summaryList = session.createCriteria(RatingSummary.class).
                                                  add(Restrictions.eq("annotates", articleURI)).
                                                  list();
                return (summaryList.size() > 0) ? new AverageRatings(summaryList.get(0)) :
                                                  new AverageRatings();
              }
            });
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public boolean hasRated(String articleURI, AmbraUser user) {

    if (user == null)
      return false;

    if (log.isDebugEnabled()) {
      log.debug("retrieving list of user ratings for article: " + articleURI + " and user: " +
                user.getUserId());
    }

    List<Rating> ratingsList = session.createCriteria(Rating.class).
                                 add(Restrictions.eq("annotates", articleURI)).
                                 add(Restrictions.eq("creator", user.getUserId())).list();

    return ratingsList.size() > 0;
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
    private static final long serialVersionUID = -2890067268188424471L;

    private final double total;
    private final int    count;
    private final double average;
    private final double rounded;

    Average(double total, int count) {
      this.total = total;
      this.count = count;
      average = (count == 0) ? 0 : total/count;
      rounded = RatingContent.roundTo(average, 0.5);
    }

    @Override
    public String toString() {
      return "total = " + total + ", count = " + count + ", average = " + average +
             ", rounded = " + rounded;
    }

    public double getTotal() {
      return total;
    }

    public int getCount() {
      return count;
    }

    public double getAverage() {
      return average;
    }

    public double getRounded() {
      return rounded;
    }    
  }

  public static class AverageRatings implements Serializable {
    private static final long serialVersionUID = -1666766336307635633L;

    private final Average style;
    private final Average insight;
    private final Average reliability;
    private final Average single;
    private final int     numUsersThatRated;
    private final double  overall;
    private final double  roundedOverall;

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

      numUsersThatRated = ratingSummary.getBody().getNumUsersThatRated();
      overall = RatingContent.calculateOverall(insight.average, reliability.average, style.average);
      roundedOverall = RatingContent.roundTo(overall, 0.5);
    }

    @Override
    public String toString() {
      return "style = [" + style + "], " +
             "insight = [" + insight + "], " +
             "reliability = [" + reliability + "], " +
             "single = [" + single + "], " +
             "numUsersThatRated = " + numUsersThatRated +
             ", overall = " + overall +
             ", roundedOverall = " + roundedOverall;
    }

    public Average getStyle() {
      return style;
    }

    public Average getInsight() {
      return insight;
    }

    public Average getReliability() {
      return reliability;
    }

    public Average getSingle() {
      return single;
    }

    public int getNumUsersThatRated() {
      return numUsersThatRated;
    }

    public double getOverall() {
      return overall;
    }

    public double getRoundedOverall()
    {
      return roundedOverall;
    }      
  }

  private class Invalidator extends AbstractObjectListener {
    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      if (o instanceof RatingSummary) {
        if (log.isDebugEnabled())
          log.debug("Invalidating ratings-summary cache entry since it changed");
        articleAnnotationCache.remove(AVG_RATINGS_KEY + ((RatingSummary)o).getAnnotates());
      }
    }
    @Override
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
