/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.Years;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Volume;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.ambra.article.action.TOCArticleGroup;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session.FlushMode;
import org.topazproject.otm.query.Results;

import com.sun.xacml.PDP;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author Alex Worden, stevec
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseService.class);

  private static final String ARTBYCAT_LIST_KEY   = "ArtByCat-";
  private static final String DATE_LIST_KEY       = "DateList-";
  private static final String ARTBYDATE_LIST_KEY  = "ArtByDate-";
  private static final String ARTICLE_KEY         = "Article-";
  private static final String ISSUE_KEY           = "Issue-";

  private static final int getSecsTillMidnight() {
    Calendar cal = Calendar.getInstance();
    long now = cal.getTimeInMillis();

    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DATE, 1);

    return (int) ((cal.getTimeInMillis() - now) / 1000);
  }

  private static final Integer getYear(String date) {
    return Integer.valueOf(date.substring(0, 4));
  }

  private static final Integer getMonth(String date) {
    return Integer.valueOf(date.substring(5, 7));
  }

  private static final Integer getDay(String date) {
    return Integer.valueOf(date.substring(8, 10));
  }

  private       ArticlePEP     pep;
  private       Session        session;
  private       Cache          browseCache;
  private       JournalService journalService;
  private       Invalidator    invalidator;

  /**
   * @param journalService The journal-service to use.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param browseCache The browse-cache to use.
   */
  @SuppressWarnings("synthetic-access")
  @Required
  public void setBrowseCache(Cache browseCache) {
    this.browseCache = browseCache;
    // We are order dependent on what the journal service does. So register the listener there.
    if (invalidator == null)
      browseCache.getCacheManager().registerListener(invalidator = new Invalidator());
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


  @Required
  public void setArticlesPdp(PDP pdp) {
    pep = new ArticlePEP(pdp);
  }

  /**
   * Get the dates of all articles. The outer map is a map of years, the next inner map a map
   * of months, and finally the innermost is a list of days.
   *
   * @return the article dates.
   */
  @Transactional(readOnly = true)
  public Years getArticleDates() {
    String cacheKey  = DATE_LIST_KEY + journalService.getCurrentJournalName();

    return browseCache.get(cacheKey, -1,
        new Cache.SynchronizedLookup<Years, RuntimeException>(cacheKey.intern()) {
          @SuppressWarnings("synthetic-access")
          @Override
          public Years lookup() throws RuntimeException {
            return loadArticleDates();
          }
        });
  }

  /**
   * Get articles in the given category. One "page" of articles will be returned, i.e. articles
   * pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less than a pageSize articles
   * may be returned, either because it's the end of the list or because some articles are not
   * accessible.
   *
   * @param catName  the category for which to return the articles
   * @param pageNum  the page-number for which to return articles; 0-based
   * @param pageSize the number of articles per page
   * @param numArt   (output) the total number of articles in the given category
   * @return the articles.
   */
  @Transactional(readOnly = true)
  public List<ArticleInfo> getArticlesByCategory(final String catName, int pageNum, int pageSize,
                                                 int[] numArt) {
    List<URI> articleIds = getArticlesByCategory().get(catName);

    if (articleIds == null) {
      numArt[0] = 0;
      return null;
    }
    numArt[0] = articleIds.size();
    return loadArticles(articleIds, pageNum, pageSize);
  }

  /**
   * Get articles in the given date range, from newest to oldest, of the given article type(s).
   * One "page" of articles will be returned,
   * i.e. articles pageNum * pageSize .. (pageNum + 1) * pageSize - 1 .
   * Note that less than a pageSize articles may be returned, either because it's the end
   * of the list or because some articles are not accessible.
   * <p/>
   * Note: this method assumes the dates are truly just dates, i.e. no hours, minutes, etc.
   * <p/>
   * If the <code>articleTypes</code> parameter is null or empty,
   * then all types of articles are returned.
   * <p/>
   * This method should never return null.
   *
   * @param startDate    the earliest date for which to return articles (inclusive)
   * @param endDate      the latest date for which to return articles (exclusive)
   * @param articleTypes The URIs indicating the types of articles which will be returned,
   *                       or null for all types
   * @param pageNum      the page-number for which to return articles; 0-based
   * @param pageSize     the number of articles per page, or -1 for all articles
   * @param numArt       (output) the total number of articles in the given category
   * @return the articles.
   */
  @Transactional(readOnly = true)
  public List<ArticleInfo> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                             final List<URI> articleTypes, int pageNum,
                                             int pageSize, int[] numArt) {
    String jnlName  = journalService.getCurrentJournalName();
    String mod      = jnlName + "-" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis();
    String cacheKey = ARTBYDATE_LIST_KEY + mod;
    int    ttl      = getSecsTillMidnight();

    List<URI> uris = browseCache.get(cacheKey, ttl,
        new Cache.SynchronizedLookup<List<URI>, RuntimeException>(cacheKey.intern()) {
          @Override
          public List<URI> lookup() throws RuntimeException {
            return loadArticlesByDate(startDate, endDate, articleTypes);
          }
        });

    if (uris == null) {
      numArt[0] = 0;
      return null;
    }
    numArt[0] = uris.size();

    List<ArticleInfo> res = loadArticles(uris, pageNum, pageSize);
    //  Ensure that this method will NEVER return a null.
    if (res != null ) {
      return res;
    }
    else {
      return new ArrayList<ArticleInfo>();
    }
  }

  /**
   * Get a list of article-counts for each category.
   *
   * @return the category infos.
   */
  @Transactional(readOnly = true)
  public SortedMap<String, List<URI>> getArticlesByCategory() {
    final String currentJournal = journalService.getCurrentJournalName();
    final String cacheKey = ARTBYCAT_LIST_KEY + currentJournal;
    return browseCache.get(cacheKey, -1,
        new Cache.SynchronizedLookup<SortedMap<String, List<URI>>,
            RuntimeException>(cacheKey.intern()) {
          @Override
          public SortedMap<String, List<URI>> lookup() throws RuntimeException {

            return convertToSortedArticleIdMap(fetchArticlesByCategory());
          }
        });
  }

  /**
   * Get Issue information.
   *
   * @param doi DOI of Issue.
   * @return the Issue information.
   */
  @Transactional(readOnly = true)
  public IssueInfo getIssueInfo(final URI doi) {

    // This flush is so that our own query cache reflects change (why is this ?).
    // TODO : implement query caching in OTM and let it manage this cached query
    if (session.getFlushMode().implies(FlushMode.always))
      session.flush();

    String cacheKey = ISSUE_KEY + doi;
    return browseCache.get(cacheKey, -1,
        new Cache.SynchronizedLookup<IssueInfo, RuntimeException>(cacheKey.intern()) {
          @Override
          public IssueInfo lookup() throws RuntimeException {
            return getIssueInfo2(doi);
          }
        });
  }

  /**
   * Get Issue information inside of a Transaction.
   *
   * @param issueDOI DOI of Issue.
   * @return the Issue information.
   */
  private IssueInfo getIssueInfo2(final URI issueDOI) {

    // get the Issue
    final Issue issue = session.get(Issue.class, issueDOI.toString());
    if (issue == null) {
      log.error("Failed to retrieve Issue for doi='"+issueDOI.toString()+"'");
      return null;
    }

    // get the image Article
    URI imageArticle = null;
    String description = null;
    if ((issue.getImage() != null)
        && (issue.getImage().toString().length() != 0)) {
      final Article article = session.get(Article.class, issue.getImage().toString());
      if (article != null) {
        imageArticle = issue.getImage();
        description = article.getDublinCore().getDescription();
      }
    }

    // derive prev/next Issue, "parent" Volume
    URI prevIssueURI = null;
    URI nextIssueURI = null;
    Volume parentVolume = null;

    Results results = session.createQuery("select v from Volume v where v.issueList = :doi ;")
                             .setParameter("doi", issueDOI).execute();
    results.beforeFirst();
    if (results.next()) {
      parentVolume = (Volume)results.get("v");
      results.close();
    }
    if (parentVolume != null) {
      final List<URI> issues = parentVolume.getIssueList();
      final int issuePos = issues.indexOf(issueDOI);
      prevIssueURI = (issuePos == 0) ? null : issues.get(issuePos - 1);
      nextIssueURI = (issuePos == issues.size() - 1) ? null : issues.get(issuePos + 1);
    } else {
      log.warn("Issue: " + issue.getId() + ", not contained in any Volumes");
    }

    IssueInfo issueInfo = new IssueInfo(issue.getId(), issue.getDisplayName(), prevIssueURI,
                                        nextIssueURI, imageArticle, description,
                                        parentVolume == null ? null : parentVolume.getId());
    issueInfo.setArticleUriList(getArticleList(issue));
    return issueInfo;
  }

  /**
   * Returns the list of ArticleInfos contained in this Issue. The list will contain only
   * ArticleInfos for Articles that the current user has permission to view.
   *
   * @param issueDOI Issue ID
   * @return List of ArticleInfo objects.
   */
  @Transactional(readOnly = true)
  public List<ArticleInfo> getArticleInfosForIssue(final URI issueDOI) {

    IssueInfo iInfo = getIssueInfo(issueDOI);
    List<ArticleInfo> aInfos = new ArrayList<ArticleInfo>();

    for (URI articleDoi : iInfo.getArticleUriList()) {
      ArticleInfo ai = getArticleInfo(articleDoi);
      if (ai == null) {
        log.warn("Article " + articleDoi + " missing; member of Issue " + issueDOI);
        continue;
      }
      aInfos.add(ai);
    }
    return aInfos;
  }

  /**
   * Get a VolumeInfo for the given id. This only works if the volume is in the current journal.
   *
   * @param id Volume ID
   * @return VolumeInfo
   */
  @Transactional(readOnly = true)
  public VolumeInfo getVolumeInfo(URI id) {
    // Attempt to get the volume infos from the cached journal list...
    List<VolumeInfo> volumes = getVolumeInfosForJournal(journalService.getCurrentJournal());
    for (VolumeInfo vol : volumes) {
      if (id.equals(vol.getId())) {
        return vol;
      }
    }

    /*
     * If we have no luck with the cached journal list, attempt to load the volume re-using
     * loadVolumeInfos();
     */
    List<URI> l = new ArrayList<URI>();
    l.add(id);
    List<VolumeInfo> vols = loadVolumeInfos(l);
    if ((vols != null) && (vols.size() > 0)) {
      return vols.get(0);
    }
    return null;
  }

  /**
   * Returns a list of VolumeInfos for the given Journal. VolumeInfos are sorted in reverse order
   * to reflect most common usage. Uses the pull-through cache.
   *
   * @param journal To find VolumeInfos for.
   * @return VolumeInfos for journal in reverse order.
   */
  @Transactional(readOnly = true)
  public List<VolumeInfo> getVolumeInfosForJournal(final Journal journal) {
    final List<URI> volumeDois = journal.getVolumes();
    List<VolumeInfo> volumeInfos = loadVolumeInfos(volumeDois);
    Collections.reverse(volumeInfos);
    return volumeInfos;
  }

  /**
   * Get VolumeInfos. Note that the IssueInfos contained in the volumes have not been instantiated
   * with the ArticleInfos.
   *
   * @param volumeDois to look up.
   * @return volumeInfos.
   */
  private List<VolumeInfo> loadVolumeInfos(final List<URI> volumeDois) {
    List<VolumeInfo> volumeInfos = new ArrayList<VolumeInfo>();
    // get the Volumes
    for (int onVolumeDoi = 0; onVolumeDoi < volumeDois.size(); onVolumeDoi++) {
      final URI volumeDoi = volumeDois.get(onVolumeDoi);
      final Volume volume  = session.get(Volume.class, volumeDoi.toString());
      if (volume == null) {
        log.error("unable to load Volume: " + volumeDoi);
        continue;
      }
      // get the image Article, may be null
      URI imageArticle = null;
      String description = null;
      if (volume.getImage() != null) {
        final Article article = session.get(Article.class, volume.getImage().toString());
        if (article != null) {
          imageArticle = volume.getImage();
          description = article.getDublinCore().getDescription();
        }
      }

      List<IssueInfo> issueInfos = new ArrayList<IssueInfo>();
      for (final URI issueDoi : volume.getIssueList()) {
        issueInfos.add(getIssueInfo(issueDoi));
      }

      // calculate prev/next
      final URI prevVolumeDoi = (onVolumeDoi == 0) ? null : volumeDois.get(onVolumeDoi - 1);
      final URI nextVolumeDoi = (onVolumeDoi == volumeDois.size() - 1) ? null
                                                      : volumeDois.get(onVolumeDoi + 1);
      final VolumeInfo volumeInfo = new VolumeInfo(volume.getId(), volume.getDisplayName(),
              prevVolumeDoi, nextVolumeDoi, imageArticle, description, issueInfos);
      volumeInfos.add(volumeInfo);
    }

    return volumeInfos;
  }

  /**
   * Convert list map of Article id's to sorted map of article ID lists
   * @param articlesByCategory Map of URI sets by category
   * @return Sorted map of Article ID lists by category
   */
  private SortedMap<String, List<URI>> convertToSortedArticleIdMap(Map<String, Set<URI>> articlesByCategory) {
    SortedMap<String, List<URI>> mapOfArticleIds = new TreeMap<String, List<URI>>();
    for (Map.Entry<String, Set<URI>> entry : articlesByCategory.entrySet()) {
      if (entry.getValue().size() > 0) {
        mapOfArticleIds.put(entry.getKey(), new ArrayList<URI>(entry.getValue()));
      }
    }
    return mapOfArticleIds;
  }

  /**
   * Return unsorted map of Article objects sorted by date and article id.
   * @return Article objects by category
   */
  private Map<String, Set<URI>> fetchArticlesByCategory() {
    Results results = session.createQuery(
        "select a.categories.mainCategory cat, a.id articleId, a.dublinCore.date date " +
        "from Article a " +
        "order by date desc, articleId;")
        .execute();

    Map<String, Set<URI>> articlesByCategory = new HashMap<String, Set<URI>>();
    while (results.next()) {

      String category = results.getString(0);
      URI articleId = results.getURI(1);

      Set<URI> articleIds = articlesByCategory.get(category);
      if (articleIds == null) {
        articleIds = new LinkedHashSet<URI>();
        articlesByCategory.put(category, articleIds);
      }
      articleIds.add(articleId);
    }
    return articlesByCategory;
  }

  private ArticleInfo loadArticleInfo(final URI id) {
    ArticleInfo ai =  session.get(ArticleInfo.class, id.toString());
    if (ai == null)
      return null;

    if (log.isDebugEnabled()) {
      log.debug("loaded ArticleInfo: id='" + ai.getId() +
                "', articleTypes='" + ai.getArticleTypes() +
                "', date='" + ai.getDate() +
                "', title='" + ai.getTitle() +
                "', authors='" + ai.getAuthors() +
                "', related-articles='" + ai.getRelatedArticles() + "'");
    }

    return ai;
  }

  private Years loadArticleDates() {
    Results r = session.createQuery("select a.dublinCore.date from Article a;").execute();

    Years dates = new Years();

    r.beforeFirst();
    while (r.next()) {
      String  date = r.getString(0);
      Integer y    = getYear(date);
      Integer m    = getMonth(date);
      Integer d    = getDay(date);
      dates.getMonths(y).getDays(m).add(d);
    }

    return dates;
  }

  /**
   * Get the URIs for all of the Articles that were published between the <code>start date</code>
   * parameter and <code>end date</code> parameter and have at least one article type from the
   * <code>articleTypes</code> parameter.  If the <code>articleTypes</code> parameter
   * is null or empty, then no filtering on article type is performed.
   *
   * @param startDate The date after which an article must be published for that article to be
   *   included in the result
   * @param endDate The date before which an article must be published for that article to be
   *   included in the result
   * @param articleTypes The types of Articles which will be included in the result.
   *   If null or empty, then result will contain all article types.
   * @return The URIs of articles published after the start date parameter, before the end date
   *   parameter, and have at least one article type from the <code>articleTypes</code> parameter
   */
  private List<URI> loadArticlesByDate(final Calendar startDate, final Calendar endDate,
                                       final List<URI> articleTypes) {
    // XsdDateTimeSerializer formats dates in UTC, so make sure that doesn't change the date
    final Calendar sd = (Calendar) startDate.clone();
    sd.add(Calendar.MILLISECOND, sd.get(Calendar.ZONE_OFFSET) + sd.get(Calendar.DST_OFFSET));
    // ge(date, date) is currently broken, so tweak the start-date instead
    sd.add(Calendar.MILLISECOND, -1);

    StringBuilder queryString = new StringBuilder("select a.id articleId, date from Article a "
        + " where date := a.dublinCore.date and gt(date, :sd) and lt(date, :ed)");
    int counter = 1;
    if (articleTypes != null && articleTypes.size() > 0) {
      queryString.append(" and ( ");
      Iterator articleTypesIterator = articleTypes.iterator();
      while (articleTypesIterator.hasNext()) {
        queryString.append(" a.articleType = :acceptableType");
        queryString.append(Integer.toString(counter++));
        articleTypesIterator.next();  //  Just to iterate.  Do not need these values yet.
        if (articleTypesIterator.hasNext()) {
          queryString.append(" or ");
        }
      }
      queryString.append(" ) ");
    }
    queryString.append(" order by date desc, articleId;");

    Query query =  session.createQuery(queryString.toString());
    query.setParameter("sd", sd);
    query.setParameter("ed", endDate);
    counter = 1;
    if (articleTypes != null && articleTypes.size() > 0) {
      for (URI articleType : articleTypes) {
        query.setParameter("acceptableType" + counter++, articleType);
      }
    }

    List<URI> dates = new ArrayList<URI>();  //  The URIs which will be returned.

    Results r =  query.execute();
    r.beforeFirst();
    while (r.next()) {
      dates.add(r.getURI(0));
    }

    return dates;
  }

  private List<ArticleInfo> loadArticles(final List<URI> ids, int pageNum, int pageSize) {
    final int beg = (pageSize > 0) ? pageNum * pageSize : 0;
    final int end = (pageSize > 0) ? Math.min((pageNum + 1) * pageSize, ids.size()) : ids.size();

    List<ArticleInfo> res = new ArrayList<ArticleInfo>();

    for (int idx = beg; idx < end; idx++) {
      URI id = ids.get(idx);

      ArticleInfo ai = getArticleInfo(id);
      if (ai != null) {
        res.add(ai);
      }
    }

    return res;
  }

  @Transactional(readOnly = true)
  public ArticleInfo getArticleInfo(final URI id) {
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, id);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
      return null;
    }

    String cacheKey = ARTICLE_KEY + id;
    return browseCache.get(cacheKey, -1,
        new Cache.SynchronizedLookup<ArticleInfo, RuntimeException>(cacheKey.intern()) {
          @Override
          public ArticleInfo lookup() throws RuntimeException {
            return loadArticleInfo(id);
          }
        });
  }

  private class Invalidator extends AbstractObjectListener {

    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
                              Updates updates) {

      if (o instanceof Article && ((Article) o).getState() == Article.STATE_ACTIVE) {

        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the article that was updated.");
        notifyArticleChanged(((Article) o).getId());

      } else if (o instanceof Journal) {

        String journalKey = ((Journal) o).getKey();
        if ((updates == null) || updates.isChanged("smartCollectionRules") ||
            updates.isChanged("simpleCollection")) {
          if (log.isDebugEnabled())
            log.debug("Updating browsecache for the journal that was modified.");
          notifyJournalModified(journalKey);
        }

      } else if (o instanceof Volume) {

        if ((updates != null) && updates.isChanged("issueList")) {
          if (log.isDebugEnabled())
            log.debug("Updating issue-infos for the Volume that was modified.");
          for (URI issue : ((Volume) o).getIssueList())
            notifyIssueChanged(issue);
          for (String v : updates.getOldValue("issueList"))
            notifyIssueChanged(URI.create(v));
        }

      } else if (o instanceof Issue) {

        if (log.isDebugEnabled())
          log.debug("Updating issue-info for the Issue that was modified.");
        notifyIssueChanged(((Issue) o).getId());

      } else if (o instanceof FormalCorrection || o instanceof Retraction) {

        notifyAnnotationChanged(((ArticleAnnotation) o).getAnnotates());

      }

    }

    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {

      if (o instanceof Article && ((Article) o).getState() == Article.STATE_ACTIVE) {

        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the article that was deleted.");
        notifyArticleChanged(((Article) o).getId());

      } else if (o instanceof Journal) {

        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the journal that was deleted.");
        notifyJournalModified(((Journal) o).getKey());

      } else if (o instanceof Issue) {

        if (log.isDebugEnabled())
          log.debug("Updating issue-info for the issue that was deleted.");
        notifyIssueChanged(((Issue) o).getId());

      } else if (o instanceof FormalCorrection || o instanceof Retraction) {

        notifyAnnotationChanged(((ArticleAnnotation) o).getAnnotates());

      }

    }

    /**
     * Notify this service that articles have been changed.
     *
     * @param articleId ID of the article that changed
     */
    private void notifyArticleChanged(URI articleId) {

      browseCache.remove(ARTICLE_KEY + articleId);

      for (Journal journal : journalService.getJournalsForObject(articleId)) {
        String journalKey = journal.getKey();
        browseCache.remove(ARTBYCAT_LIST_KEY + journalKey);
        browseCache.remove(DATE_LIST_KEY + journalKey);
        for (Object key : browseCache.getKeys()) {
          if ((key instanceof String) && ((String) key).startsWith(ARTBYDATE_LIST_KEY + journalKey)) {
            browseCache.remove(key);
          }
        }
      }
    }

    /**
     * Notify this service that articles have been changed.
     *
     * @param articleId ID of the article that changed
     */
    private void notifyAnnotationChanged(URI articleId) {

      browseCache.remove(ARTICLE_KEY + articleId);

    }

    /**
     * Notify this service that a journal definition has been modified. Should only be called when the
     * rules determining articles belonging to this journal have been changed.
     *
     * @param jnlName the key of the journal that was modified
     */
    private void notifyJournalModified(final String jnlName) {
      browseCache.remove(ARTBYCAT_LIST_KEY + jnlName);
      browseCache.remove(DATE_LIST_KEY + jnlName);

      for (Object key : browseCache.getKeys()) {
        if ((key instanceof String) && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName)) {
          browseCache.remove(key);
        }
      }
    }

    /**
     * Clear the issue with the given doi from the browseCache.
     *
     * @param issueDoi DOI of the issue
     */
    public void notifyIssueChanged(URI issueDoi) {
      browseCache.remove(ISSUE_KEY + issueDoi);
    }
  }

  /**
   * Given a list of Article Groups with correctly ordered articles
   * create a CSV string of article URIs. The URIs will be in the
   * order that they show up on the TOC.
   *
   * @param  articleGroups the list of TOCArticleGroup to process.
   * @return a string of a comma separated list of article URIs
   */
  public String articleGrpListToCSV( List<TOCArticleGroup> articleGroups) {
    StringBuilder articleList = new StringBuilder();
    Iterator i = articleGroups.listIterator();

    // Group Loop
    while(i.hasNext()) {
      TOCArticleGroup ag = (TOCArticleGroup)i.next();
      Iterator y = ag.articles.listIterator();

      // Article Loop
      while(y.hasNext()) {
        ArticleInfo ai = (ArticleInfo)y.next();
        articleList.append(ai.id.toString());

        if (y.hasNext())
          articleList.append(',');
      }
      if (i.hasNext())
        articleList.append(',');
    }
    return articleList.toString();
  }

  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(URI issueURI) {
    Issue issue = session.get(Issue.class, issueURI.toString());

    // If the issue does not exist then return an empty list
    if (issue == null)
      return new ArrayList<TOCArticleGroup>();

    return getArticleGrpList(issue);
  }

  /**
   *
   */
  public List<TOCArticleGroup> getArticleGrpList(Issue issue) {
    List<TOCArticleGroup> groupList = new ArrayList<TOCArticleGroup>();

    for (ArticleType at : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup newArticleGroup = new TOCArticleGroup(at);
      groupList.add(newArticleGroup);
    }

    return pruneArticleGrps(issue,groupList);
  }

  /**
   *
   */
  public List<TOCArticleGroup> pruneArticleGrps(Issue issue, List<TOCArticleGroup> articleGroups) {
    List<ArticleInfo> articlesInIssue = getArticleInfosForIssue(issue.getId());
    /*
     * For every article that is of the same ArticleType as a TOCArticleGroup, add it to that group.
     * Articles can appear in multiple TOCArticleGroups.
     */
    for (ArticleInfo ai : articlesInIssue)
      for (TOCArticleGroup ag : articleGroups)
        for (ArticleType articleType : ai.getArticleTypes())
          if (ag.getArticleType().equals(articleType)) {
            ag.addArticle(ai);
            break;
          }

    Iterator iter = articleGroups.listIterator();
    Integer i = 0;

    while(iter.hasNext()) {
      TOCArticleGroup ag = (TOCArticleGroup) iter.next();
      // remove the group if it has no articles
      if (ag.articles.size() == 0) {
        iter.remove();
        continue;
      }
      // If we respect order then don't sort.
      if (!issue.getRespectOrder()) {
        ag.setId("tocGrp_"+ (i++));
        ag.sortArticles();
      }
    }
    return articleGroups;
  }

  /**
   * Get ordered list of articles. Either from articleList or from
   * simpleCollection if articleList is empty.
   * @param issue
   * @return List of article URI's
   */
  public List<URI> getArticleList(Issue issue) {
    List<URI> articleList = issue.getArticleList();
    if (articleList.isEmpty() && !issue.getSimpleCollection().isEmpty())
      return new ArrayList<URI>(issue.getSimpleCollection());

    return articleList;
  }

  /**
   * ArticleInfo objects store lists of FormalCorrection id's because ArticleInfo is
   * object that is not cached in the ObjectCache and it cannot hold referencews to OTM entities
   * because it will cause it to fail serialization.
   * We need to fetch FormalCorrection objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all FormalCorrection objects contained in articleGroups
   */
  public Map<URI, FormalCorrection> getCorrectionMap(List<TOCArticleGroup> articleGroups) {

    Map<URI, FormalCorrection> correctionMap = new HashMap<URI, FormalCorrection>();

    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        if (articleInfo.getCorrections() != null) {
          for (URI correctionId : articleInfo.getCorrections()) {
            correctionMap.put(
                correctionId,
                session.get(FormalCorrection.class, correctionId.toString())
            );
          }
        }
      }
    }

    return correctionMap;
  }

  /**
   * ArticleInfo objects store lists of Retraction id's because ArticleInfo is
   * object that is not cached in the ObjectCache and it cannot hold referencews to OTM entities
   * because it will cause it to fail serialization.
   * We need to fetch Retraction objects and put them in a Map so they can be displayed.
   *
   * @param articleGroups List of article groups
   * @return Map of all Retraction objects contained in articleGroups
   */
  public Map<URI, Retraction> getRetractionMap(List<TOCArticleGroup> articleGroups) {

    Map<URI, Retraction> retractionMap = new HashMap<URI, Retraction>();

    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        if (articleInfo.getRetractions() != null) {
          for (URI retractionId : articleInfo.getRetractions()) {
            retractionMap.put(
                retractionId,
                session.get(Retraction.class, retractionId.toString())
            );
          }
        }
      }
    }

    return retractionMap;
  }


}
