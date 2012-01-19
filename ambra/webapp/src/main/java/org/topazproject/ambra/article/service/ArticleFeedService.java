/* $HeadURL$
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
package org.topazproject.ambra.article.service;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.ApplicationException;

import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;

/**
 * The <code>ArticleFeedService</code> supplies the API for querying and caching
 * feed request. <code>ArticleFeedService</code> is a Spring injected singleton
 * which coordinates access to the <code>annotationService, articleOtmService</code>
 * and <code>feedCache</code>.
 */
public class ArticleFeedService {
  private static final Log log = LogFactory.getLog(ArticleFeedService.class);

  private AnnotationService  annotationService;   // Annotation service Spring injected.
  private ArticleOtmService  articleOtmService;   // Article Otm service Spring injected
  private JournalService     journalService;      // Journal service Spring injected.
  private Cache              feedCache;           // Feed Cache Spring injected
  private Invalidator        invalidator;         // Cache invalidator
  private Session            session;

  /**
   * The feedAction data model has a types parameter which specifies
   * the feed type (currently Article or Annotation). Invalid
   * is used when the types parameter does not match any of the current
   * feed types.
   */
  public enum FEED_TYPES {
    Article                { public String rdfType() { return null;                       } },
    Annotation             { public String rdfType() { return ArticleAnnotation.RDF_TYPE; } },
    CommentAnnot           { public String rdfType() { return Comment.RDF_TYPE;           } },
    FormalCorrectionAnnot  { public String rdfType() { return FormalCorrection.RDF_TYPE;  } },
    MinorCorrectionAnnot   { public String rdfType() { return MinorCorrection.RDF_TYPE;   } },
    RatingAnnot            { public String rdfType() { return Rating.RDF_TYPE;            } },
    RatingSummaryAnnot     { public String rdfType() { return RatingSummary.RDF_TYPE;     } },
    ReplyAnnot             { public String rdfType() { return Reply.RDF_TYPE;             } },
    // Invalid must remain last.
    Invalid                { public String rdfType() { return null;                       } };

    public abstract String rdfType();
  }

  /**
   * Constructor - currently does nothing.
   */
  public ArticleFeedService(){
  }

  /**
   * Creates and returns a new <code>Key</code> for clients of FeedService.
   *
   * @return Key a new cache key to be used as a data model for the FeedAction.
   */
  public FeedCacheKey newCacheKey() {
    return new FeedCacheKey();
  }

  /**
   * Querys the OtmService using the parameters set in <code>cacheKey</code>.
   * The routine first looks in the <code>feedCache</code> for the Id list,
   * or queries the data store if there is a miss.
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException ApplicationException
   */
  public List<String> getArticleIds(final FeedCacheKey cacheKey) throws ApplicationException {
    // Create a local lookup based on the feed URI.
    Cache.Lookup<List<String>, ApplicationException> lookUp =
      new Cache.SynchronizedLookup<List<String>, ApplicationException>(cacheKey) {
        public List<String> lookup() throws ApplicationException {
          return fetchArticleIds(cacheKey);
        }
      };
    // Get articel ID's from the feed cache or add it
    return feedCache.get(cacheKey, -1, lookUp);
  }

  /**
   * Given a list of articleIds return a list of articles corresponding to the Ids.
   *
   * @param articleIds  List&lt;String&gt; of article Ids to fetch
   * @return <code>List&lt;Article&gt;</code> of articles
   * @throws ParseException ParseException
   */
  public List<Article> getArticles(List<String> articleIds) throws ParseException {
    return articleOtmService.getArticles(articleIds);
  }

  /**
   * Returns a list of annotations associated with a particular article.
   *
   * @param targetId  limits annotationIds to a particlular target
   * @param annotType  a filter list of rdf types for the annotations.
   * @param maxResult maximum number of annotations to return
   * @param needBody   boolean specifying whether the body of the annotation is needed
   * @return <code>List&lt;WebAnnotation&gt;</code> a list of webannotations that related to the
   *         specified article
   * @throws ApplicationException  Converts all exceptions to ApplicationException
   */
  public List<WebAnnotation> getAnnotations(final String targetId, List<String> annotType,
      int maxResult, boolean needBody) throws ApplicationException {
    AnnotationConverter converter = new AnnotationConverter();
    List<WebAnnotation> webAnnot;

    try {
      List<ArticleAnnotation> annotations = annotationService.getAnnotations(
                                              targetId, null, null, null,
                                              annotType, null, true,  maxResult);
      webAnnot = converter.convert(annotations,true, needBody);
    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return webAnnot;
  }

  /**
   * Returns a list of annotation Ids based on parameters contained in
   * the cache key. If a start date is not specified tjen a default
   * date is used but not stored in the key.
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @param annotType  a filter list of rdf types for the annotations.
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException   Converts all exceptions to ApplicationException
   */
  public List<String> getAnnotationIds(final FeedCacheKey cacheKey, List<String> annotType)
      throws ApplicationException {
    List<String> annotIds;
    try {
      annotIds = annotationService.getAnnotationIds(
                      null, cacheKey.getSDate(), cacheKey.getEDate(),
                      null, annotType, null, true, cacheKey.getMaxResults());

    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return  annotIds;
  }

  /**
   * Returns a list of annotations which meet the criteria set by parameters in
   * the cache key.
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @param annotType  a filter list of rdf types for the annotations.
   * @return <code>List&lt;WebAnnotation&gt;</code> a list of webannotations that related to the
   *         specified article
   * @throws ApplicationException   Converts all exceptions to ApplicationException
   */
  public List<WebAnnotation> getAnnotations(final FeedCacheKey cacheKey, List<String> annotType)
      throws ApplicationException {
    AnnotationConverter converter = new AnnotationConverter();
    List<WebAnnotation> webAnnot;

    try {
      List<ArticleAnnotation> annotations = annotationService.getAnnotations(
                                null, cacheKey.getSDate(), cacheKey.getEDate(),
                                null, annotType, null, true, cacheKey.getMaxResults());
      webAnnot = converter.convert(annotations,true,true);
    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return webAnnot;
  }

  /**
   * Returns a list of annotations associated with a particular list
   * annotation Ids.
   *
   * @param annotIds a list of annotations Ids to retrieve.
   * @return <code>List&lt;WebAnnotation&gt;</code> a list of webannotations
   *         with the specified Ids.
   * @throws ApplicationException   Converts all exceptions to ApplicationException
   */
  public List<WebAnnotation> getAnnotations(final List<String> annotIds)
      throws ApplicationException {
    AnnotationConverter converter = new AnnotationConverter();
    List<WebAnnotation> webAnnot;

    try {
      List<ArticleAnnotation> annotations = annotationService.getAnnotations(annotIds);
      webAnnot = converter.convert(annotations,true,true);
    } catch (Exception ex) {
      throw new ApplicationException(ex);
    }
    return webAnnot;
  }

  /**
   * Use the OTM Service to build a list of article ID's that match the query.  Build the category
   * and author list needed by the article ID query then make the query
   *
   * @param cacheKey is both the feedAction data model and cache key.
   * @return  <code>List&lt;String&gt;</code> a list of article ID's.
   * @throws  ApplicationException Converts all exceptions to ApplicationException
   */
  private List<String> fetchArticleIds(final FeedCacheKey cacheKey) throws ApplicationException {
    List<String> categoriesList = new ArrayList<String>();
    if (cacheKey.getCategory() != null && cacheKey.getCategory().length() > 0) {
      categoriesList.add(cacheKey.getCategory());
    }

    List<String> authorsList = new ArrayList<String>();
    if (cacheKey.getAuthor() != null) {
      authorsList.add(cacheKey.getAuthor());
    }

    try {
      String startDate = null;
      String endDate = null;

      if (cacheKey.getSDate() != null)
        startDate = cacheKey.getSDate().toString();

      if (cacheKey.getEDate() != null)
        endDate = cacheKey.getEDate().toString();

      return articleOtmService.getArticleIds(
              startDate, endDate,
              categoriesList.toArray(new String[categoriesList.size()]),
              authorsList.toArray(new String[authorsList.size()]),
              Article.ACTIVE_STATES, false, cacheKey.getMaxResults());
    } catch (ParseException ex) {
      throw new ApplicationException(ex);
    }
  }

  /**
   * @param journalService   Journal Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param articleOtmService   ArticleOtm Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @param annotationService   Annotation Service
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @param feedCache  Feed Cache
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setFeedCache(Cache feedCache) {
    this.feedCache = feedCache;
    // We are order dependent on what the journal service does. So register the listener there.
    if (invalidator == null)
      feedCache.getCacheManager().registerListener(invalidator = new Invalidator());
  }

  /**
   * @param session    Otm Session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Get the UserAccount info using the Id.
   * 
   * @param id  user id
   * @return  user account
   */
  public UserAccount getUserAcctFrmID(String id) {
    UserAccount ua = session.get(UserAccount.class, id);
    if(ua == null) {
      log.error("Unable to look up UserAccount for " + id);
    }
    return ua;
  }

  /**
   * Invalidate feedCache if new articles are ingested or articles are deleted. This is accomplished
   * via a listener registered with the feed cache. This listener is notified when articles are
   * added or deleted which could potentially affect the results cached.
   *
   * The <code>Invalidator</code> uses two entry points to accomplish this.
   * <p>
   * <code>Invalidator.objectChanged</code> is called whenever an article or journal is updated.
   * <code>Invalidator.removing</code> is called whenever an article is removed.
   * <p>
   *
   * The basic process is the same for both. Loop through all the feed cache keys and see if the
   * articles that changed could potentially affect the query results of the cache entry. If it does
   * then remove that cache entry.
   *
   * @see ArticleFeedService
   */
  public class Invalidator extends AbstractObjectListener {
    /**
     * Notify the <code>Invalidator</code> that an object in the cache may have changed.
     *
     * @param session  session info (unused)
     * @param cm       class metadata  (unused)
     * @param id       (unused)
     * @param object   either <code>class Article</code> or <code>class Journal<code>
     * @param updates  journal updates
     */
    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object object,
                              Interceptor.Updates updates) {
      /* If this is an Active Article check to see if it invalidates the feed cache
       * Else if this is a Journal change invalidate for journals.
       */
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE) {
        invalidateFeedCacheForArticle((Article)object);
      } else if (object instanceof Journal) {
        invalidateFeedCacheForJournal((Journal)object, updates);
      }
    }

    /**
     * Notify that an article is being removed. If it matches a cache entry's query criteria then
     * remove that entry from the cache.
     *
     * @param session session info (unused)
     * @param cm      class metadata (unused)
     * @param id      (unused)
     * @param object  must be <code>class Article</code>
     * @throws Exception
     */
    @Override
    public void removing(Session session, ClassMetadata cm, String id, Object object)
        throws Exception {
      // If this is an Active Article check to see if it invalidates the feed cache
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE)
        invalidateFeedCacheForArticle((Article)object);
    }

    /**
     * @param journal  the journal name
     * @param updates  updates to articles
     */
    private void invalidateFeedCacheForJournal(Journal journal, Interceptor.Updates updates) {

      /* If the simple collect changed size then assume
       * the change was an addition or deletion and
       * flush cache entries for this journal.
       */
      List<String> oldArticles = updates.getOldValue("simpleCollection");
      List<URI> curArticles = journal.getSimpleCollection();

      if ((oldArticles == null) || (curArticles==null))
        return;

      if ( oldArticles.size() != curArticles.size() )
        invalidateFeedCacheForJournalArticle(journal);
    }

    /**
     * Loop through the feed cache keys and match key parameters to the article. If the article
     * matches those found in the cache key then that query is invalid and remove it.
     *
     * @param article the article which might change the cash.
     */
    @SuppressWarnings("unchecked")
    private void invalidateFeedCacheForArticle(Article article) {
      for (FeedCacheKey key : (Set<FeedCacheKey>) feedCache.getKeys()) {
        if (matches(key, article, true))
          feedCache.remove(key);
      }
    }

    /**
     * Invalidate the cache entries based on a journal and a list of article that are part of that
     * journal.
     *
     * @param journal  the journal of interest
     */
    @SuppressWarnings("unchecked")
    private void invalidateFeedCacheForJournalArticle(Journal journal) {
      for (FeedCacheKey key : (Set<FeedCacheKey>) feedCache.getKeys()) {
        if (key.getJournal().equals(journal.getKey())) {
          feedCache.remove(key);
        }
      }
    }

    /**
     * This is the linchpin to the entire process. Basically, query results are currently affected
     * by 5 parameters:
     *
     * <ul>
     * <li>startDate/endDate
     * <li>author
     * <li>category
     * <li>maxResult
     * </ul>
     *
     * If the article affects the results of any one of these parameters then then the query could
     * be different and hence needs to be ejected from the cache.
     *
     * Match criteria:
     *
     * If the journal doesn't match - don't remove
     * If the date of the article isn't between start and end - don't remove
     *
     * The Category and Author have to follow the following truth table.
     *                                                                      desired result
     * matchesCat       matchesAuthor     (!matchesCat and !matchesAuthor)   removeEntry
     *     F                  F                         T                       F
     *     F                  T                         F                       T
     *     T                  F                         F                       T
     *     T                  T                         F                       T
     *
     * If both do not match then don't remove the key
     * @param key          the cache key and input parameters
     * @param article      article that has caused the change
     * @param checkJournal include journal as part of match if true.
     * @return boolean true if we need to remove this entry from the cache
     */
    private boolean matches(FeedCacheKey key, Article article, boolean checkJournal) {
      if (checkJournal && !matchesJournal(key, article))
        return false;

      DublinCore dc = article.getDublinCore();
 
      if (!matchesDates(key, dc))
        return false;

      return (!matchesCategory(key, article) && !matchesAuthor(key, dc));

    }

    /**
     * Compares the author in the cache key to the creators specified in Dublin core.
     * If key.author = null return match
     * If key.author = one of the creators return match
     * Else return no match.
     *
     * @param key the cache key
     * @param dc  Dublin core field from Article
     * @return  boolean true if there is a match
     */
    private boolean matchesAuthor(FeedCacheKey key, DublinCore dc) {
      boolean matches = false;

      if (key.getAuthor() != null) {
        for (String author : dc.getCreators()) {
          if (key.getAuthor().equalsIgnoreCase(author)) {
            matches = true;
            break;
          }
        }
      } else {
        matches = true;
      }
      return matches;
    }

    /**
     * Compare key.categery to the article.category
     * If the key.category = null then wildcard match
     * Else If the key.category = article.category then match
     * Else return no match .
     *
     * @param key      a cache key and actiopn data model
     * @param article  the article
     *
     * @return boolean true if the category matches (key.category = null is wildcard)
     */
    private boolean matchesCategory(FeedCacheKey key, Article article) {
      boolean matches = false;

      if (key.getCategory() != null) {
        for (Category category : article.getCategories()) {
          if (category.getMainCategory().equalsIgnoreCase(key.getCategory())) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;

      return matches;
    }

    /**
     * Check to see if the article date is between the start and end date specified in the key. If
     * it is then return true and the entry for this key should be removed.
     *
     * @param key cache key
     * @param dc  Dublincore field from the article
     *
     * @return boolean true if the article date falls between the start and end date
     */
    private boolean matchesDates(FeedCacheKey key, DublinCore dc) {
      Date articleDate = dc.getDate();
      boolean matches = false;

      // If start and end are null then it doesn't matter what the article date is.
      if ((key.getEDate() == null) && (key.getSDate() == null)) {
        matches = true;
      } else if (articleDate != null) {
        if ((key.getEDate() == null) && articleDate.after(key.getSDate())) {
          matches = true;
        } else if ((key.getSDate() == null) && articleDate.before(key.getEDate())) {
          matches = true;
        } else if (articleDate.after(key.getSDate()) && articleDate.before(key.getEDate())) {
          matches = true;
        }
      }
      return matches;
    }

    /**
     * Loop thorugh the Journals to see if the key.journal matches one of the journals the list of
     * journals the article belongs to.
     *
     * @param key      a cache key and actiopn data model
     * @param article  the article
     * @return boolean true if key.journal matches one of the journals returned
     *         by the journal service.
     */
    private boolean matchesJournal(FeedCacheKey key, Article article) {
      boolean matches = false;

      if (key.getJournal() != null) {
        for (Journal journal : journalService.getJournalsForObject(article.getId())) {
          if (journal.getKey().equals(key.getJournal())) {
            matches = true;
            break;
          }
        }
      } else
        matches = true;
      return matches;
    }
  }
}
