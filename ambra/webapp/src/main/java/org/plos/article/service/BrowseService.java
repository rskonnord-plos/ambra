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

package org.plos.article.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.journal.JournalService;
import org.plos.model.IssueInfo;
import org.plos.model.VolumeInfo;
import org.plos.model.article.ArticleInfo;
import org.plos.model.article.NewArtInfo;
import org.plos.model.article.Years;
import org.plos.models.Article;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.Volume;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.topazproject.otm.Session.FlushMode;
import org.topazproject.otm.query.Results;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author Alex Worden, stevec
 */
public class BrowseService {
  private static final Log log = LogFactory.getLog(BrowseService.class);

  private static final String CAT_INFO_LOCK       = "CatLock-";
  private static final String CAT_INFO_KEY        = "CatInfo-";
  private static final String ARTBYCAT_LIST_KEY   = "ArtByCat-";

  private static final String DATE_LIST_LOCK      = "DateLock-";
  private static final String DATE_LIST_KEY       = "DateList-";

  private static final String ARTBYDATE_LIST_LOCK = "ArtByDateLock-";
  private static final String ARTBYDATE_LIST_KEY  = "ArtByDate-";

  private static final String ARTICLE_LOCK        = "ArticleLock-";
  private static final String ARTICLE_KEY         = "Article-";

  private static final String ISSUE_LOCK        = "IssueLock-";
  private static final String ISSUE_KEY         = "Issue-";

  private final ArticlePEP     pep;
  private       Session        session;
  private       Cache          browseCache;
  private       JournalService journalService;
  private       Invalidator    invalidator;



  /**
   * Create a new instance.
   */
  public BrowseService() throws IOException {
    pep = new ArticlePEP();
  }

  /**
   * Get the dates of all articles. The outer map is a map of years, the next inner map a map
   * of months, and finally the innermost is a list of days.
   *
   * @return the article dates.
   */
  @Transactional(readOnly = true)
  public Years getArticleDates() {
    return getArticleDates(true, getCurrentJournal());
  }

  private Years getArticleDates(boolean load, String jnlName) {
    String key  = DATE_LIST_KEY + jnlName;
    Object lock = (DATE_LIST_LOCK + jnlName).intern();

    return browseCache.get(key, -1, 
                  !load ? null : new Cache.SynchronizedLookup<Years, RuntimeException> (lock) {
        public Years lookup() throws RuntimeException {
          return loadArticleDates();
        }
      });
  }

  private void updateArticleDates(Years dates, String jnlName) {
    browseCache.put(DATE_LIST_KEY + jnlName, dates);
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
    List<URI> uris = ((SortedMap<String, List<URI>>)
                        getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", true)).get(catName);

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return loadArticles(uris, pageNum, pageSize);
    }
  }


  /**
   * Get articles in the given date range, from newest to olders. One "page" of articles will be
   * returned, i.e. articles pageNum * pageSize .. (pageNum + 1) * pageSize - 1 . Note that less
   * than a pageSize articles may be returned, either because it's the end of the list or because
   * some articles are not accessible.
   *
   * <p>Note: this method assumes the dates are truly just dates, i.e. no hours, minutes, etc.
   *
   * @param startDate the earliest date for which to return articles (inclusive)
   * @param endDate   the latest date for which to return articles (exclusive)
   * @param pageNum   the page-number for which to return articles; 0-based
   * @param pageSize  the number of articles per page, or -1 for all articles
   * @param numArt    (output) the total number of articles in the given category
   * @return the articles.
   */
  @Transactional(readOnly = true)
  public List<ArticleInfo> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                             int pageNum, int pageSize, int[] numArt) {
    String jnlName = getCurrentJournal();
    String mod     = jnlName + "-" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis();
    String key     = ARTBYDATE_LIST_KEY + mod;
    Object lock    = (ARTBYDATE_LIST_LOCK + mod).intern();
    int    ttl     = getSecsTillMidnight();

    List<URI> uris = browseCache.get(key, ttl,
                              new Cache.SynchronizedLookup<List<URI>, RuntimeException>(lock) {
        public List<URI> lookup() throws RuntimeException {
          return loadArticlesByDate(startDate, endDate);
        }
      });

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return loadArticles(uris, pageNum, pageSize);
    }
  }

  private static final int getSecsTillMidnight() {
    Calendar cal = Calendar.getInstance();
    long now = cal.getTimeInMillis();

    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE,      0);
    cal.set(Calendar.SECOND,      0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.add(Calendar.DATE,        1);

    return (int) ((cal.getTimeInMillis() - now) / 1000);
  }

  /**
   * Get a list of article-counts for each category.
   *
   * @return the category infos.
   */
  @Transactional(readOnly = true)
  public SortedMap<String, Integer> getCategoryInfos() {
    return (SortedMap<String, Integer>) getCatInfo(CAT_INFO_KEY, "category infos", true);
  }

  /**
   * Get Issue information.
   *
   * @param issue
   *          DOI of Issue.
   * @return the Issue information.
   */
  @Transactional(readOnly = true)
  public IssueInfo getIssueInfo(final URI doi) {
    // This flush is so that our own query cache reflects change.
    // TODO : implement query caching in OTM and let it manage this cached query
    if (session.getFlushMode().implies(FlushMode.always))
      session.flush();
    return browseCache.get(ISSUE_KEY + doi, -1,
        new Cache.SynchronizedLookup<IssueInfo, RuntimeException>((ISSUE_LOCK + doi).intern()) {
          public IssueInfo lookup() throws RuntimeException {
            return getIssueInfo2(doi);
          }
        });
  }

  /**
   * Get Issue information inside of a Transaction.
   *
   * @param issue
   *          DOI of Issue.
   * @return the Issue information.
   */
  private IssueInfo getIssueInfo2(final URI issueDOI) {

    // get the Issue
    final Issue issue = session.get(Issue.class, issueDOI.toString());
    if (issue == null) {
      log.error("Faiiled to retrieve Issue for doi='"+issueDOI.toString()+"'");
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

    // String oqlQuery = "select v from Volume v where v.issueList = <" + issueDOI + "> ;";
    // Results results = session.createQuery(oqlQuery).execute();

    Results results = session.createQuery("select v from Volume v where v.issueList = :doi ;")
    .setParameter("doi", issueDOI).execute();

    results.beforeFirst();
    if (results.next()) {
      parentVolume = (Volume)results.get("v");
    }
    if (parentVolume != null) {
      final List<URI> issues = parentVolume.getIssueList();
      final int issuePos = issues.indexOf(issueDOI);
      prevIssueURI = (issuePos == 0) ? null : issues.get(issuePos - 1);
      nextIssueURI = (issuePos == issues.size() - 1) ? null : issues.get(issuePos + 1);
    } else {
      log.warn("Issue: " + issue.getId() + ", not contained in any Volumes");
    }

    IssueInfo issueInfo = new IssueInfo(issue.getId(), issue.getDisplayName(), prevIssueURI, nextIssueURI,
                                        imageArticle, description, parentVolume.getId());
    issueInfo.setArticleUriList(issue.getSimpleCollection());
    return issueInfo;
  }

  /**
   * Returns the list of ArticleInfos contained in this Issue. The list will contain only ArticleInfos for
   * Articles that the current user has permission to view.
   *
   * @param issueDOI
   * @return
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
   * @param id
   * @return
   */
  @Transactional(readOnly = true)
  public VolumeInfo getVolumeInfo(URI id) {
    // Attempt to get the volume infos from the cached journal list...
    List<VolumeInfo> volumes = getVolumeInfosForJournal(journalService.getCurrentJournal(session));
    for (VolumeInfo vol : volumes) {
      if (id.equals(vol.getId())) {
        return vol;
      }
    }

    // If we have no luck with the cached journal list, attempt to load the volume re-using loadVolumeInfos();
    List<URI> l = new ArrayList<URI>();
    l.add(id);
    List<VolumeInfo> vols = loadVolumeInfos(l);
    if ((vols != null) && (vols.size() > 0)) {
      return vols.get(0);
    }
    return null;
  }

  /**
   * Returns a list of VolumeInfos for the given Journal.
   * VolumeInfos are sorted in reverse order to reflect most common usage.
   * Uses the pull-through cache. 
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

  private Object getCatInfo(String key, String desc, boolean load) {
    return getCatInfo(key, desc, load, getCurrentJournal());
  }

  private Object getCatInfo(String key, String desc, boolean load, final String jnlName) {
    final String jnlkey = key + jnlName;
    return browseCache.get(jnlkey, -1, !load ? null : 
        new Cache.SynchronizedLookup<Object, RuntimeException>((CAT_INFO_LOCK + jnlName).intern()) {
          public Object lookup() throws RuntimeException {
             loadCategoryInfos(jnlName);
             Cache.Item item = browseCache.get(jnlkey);
             return (item == null) ? null : item.getValue();
          }
      });
  }

  private String getCurrentJournal() {
    return journalService.getCurrentJournalKey();
  }

  /**
   * Notify this service that articles have been changed.
   *
   * @param uris the list of id's of changed articles
   */
  private void notifyArticlesChanged(String[] uris) {

    for (Object key : browseCache.getKeys()) {
      if ((key instanceof String) && 
             (((String)key).startsWith(ARTBYDATE_LIST_KEY) ||
              ((String)key).startsWith(DATE_LIST_KEY) ||
              ((String)key).startsWith(ARTBYCAT_LIST_KEY) ||
              ((String)key).startsWith(CAT_INFO_KEY)))
         browseCache.remove(key);
    }
    // update article cache
    for (String uri : uris) {
      browseCache.remove(ARTICLE_KEY + uri);
    }
  }

  /**
   * Build list of articles for each journal.
   */
  private Map<String, Set<String>> getArticlesByJournal(String[] artUris) {
    Map<String, Set<String>> artMap = new HashMap<String, Set<String>>();

    for (String uri : artUris) {
      Set<String> jks = log.isDebugEnabled() ? new HashSet<String>() : null;
      for (Journal j : journalService.getJournalsForObject(URI.create(uri))) {
        Set<String> artList = artMap.get(j.getKey());
        if (artList == null) {
          artMap.put(j.getKey(), artList = new HashSet<String>());
        }
        artList.add(uri);
        if (log.isDebugEnabled())
          jks.add(j.getKey());
      }
      if (log.isDebugEnabled())
        log.debug("Article with id <" + uri + "> is part of journals " + jks);
    }

    return artMap;
  }

  /**
   * Notify this service that a journal definition has been modified. Should only be called when
   * the rules determining articles belonging to this journal have been changed.
   *
   * @param jnlName the key of the journal that was modified
   */
  private void notifyJournalModified(final String jnlName) {
    browseCache.remove(CAT_INFO_KEY + jnlName);
    browseCache.remove(ARTBYCAT_LIST_KEY + jnlName);

    browseCache.remove(DATE_LIST_KEY + jnlName);

    for (Object key : browseCache.getKeys()) {
      if ((key instanceof String) && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName)) {
        browseCache.remove(key);
      }
    }
  }

  /**
   * Load all (cat, art.id) from the db and stick the stuff in the cache.
   *
   * Loading (cat, art-id) from the db and doing the counting ourselves is faster than loading
   * (cat, count(art-id)) (at least 10 times as fast with 50'000 articles in 10'000 categories).
   */
  private void loadCategoryInfos(String jnlName) {
    // get all article-ids in all categories
    Results r = session.createQuery(
              "select cat, a.id articleId, a.dublinCore.date date from Article a " +
              "where cat := a.categories.mainCategory order by date desc, articleId;").execute();

    SortedMap<String, List<URI>> artByCat = new TreeMap<String, List<URI>>();
    r.beforeFirst();
    while (r.next()) {
      String cat = r.getString(0);
      List<URI> ids = artByCat.get(cat);
      if (ids == null) {
        artByCat.put(cat, ids = new ArrayList<URI>());
      }
      URI id = r.getURI(1);
      if (!ids.contains(id))
       ids.add(id);
    }

    updateCategoryCaches(artByCat, jnlName);
  }

  private void updateCategoryCaches(SortedMap<String, List<URI>> artByCat, String jnlName) {
    // calculate number of articles in each category
    SortedMap<String, Integer> catSizes = new TreeMap<String, Integer>();
    for (Map.Entry<String, List<URI>> e : artByCat.entrySet()) {
      catSizes.put(e.getKey(), e.getValue().size());
    }

    // save in cache
    browseCache.put(ARTBYCAT_LIST_KEY + jnlName, artByCat);
    browseCache.put(CAT_INFO_KEY + jnlName, catSizes);
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

  private static final Integer getYear(String date) {
    return Integer.valueOf(date.substring(0, 4));
  }

  private static final Integer getMonth(String date) {
    return Integer.valueOf(date.substring(5, 7));
  }

  private static final Integer getDay(String date) {
    return Integer.valueOf(date.substring(8, 10));
  }

  private static void insertDate(Years dates, Date newDate) {
    Calendar newCal = Calendar.getInstance();
    newCal.setTime(newDate);

    int y = newCal.get(Calendar.YEAR);
    int m = newCal.get(Calendar.MONTH) + 1;
    int d = newCal.get(Calendar.DATE);

    dates.getMonths(y).getDays(m).add(d);
  }

  private List<URI> loadArticlesByDate(final Calendar startDate, final Calendar endDate) {
    // XsdDateTimeSerializer formats dates in UTC, so make sure that doesn't change the date
    final Calendar sd = (Calendar) startDate.clone();
    sd.add(Calendar.MILLISECOND, sd.get(Calendar.ZONE_OFFSET) + sd.get(Calendar.DST_OFFSET));
    // ge(date, date) is currently broken, so tweak the start-date instead
    sd.add(Calendar.MILLISECOND, -1);

    Results r = session.createQuery(
            "select a.id articleId, date from Article a where " +
            "date := a.dublinCore.date and gt(date, :sd) and lt(date, :ed) order by date desc, articleId;").
            setParameter("sd", sd).setParameter("ed", endDate).execute();

    List<URI> dates = new ArrayList<URI>();

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

    return
      browseCache.get(ARTICLE_KEY + id, -1,
           new Cache.SynchronizedLookup<ArticleInfo, RuntimeException>((ARTICLE_LOCK + id).intern()) {
        public ArticleInfo lookup() throws RuntimeException {
          return loadArticleInfo(id);
        }
      });
  }

  private List<NewArtInfo> getNewArticleInfos(final Set<String> uris) {
    List<NewArtInfo> res = new ArrayList<NewArtInfo>(uris.size());

    for (String uri : uris) {
      NewArtInfo nai = session.get(NewArtInfo.class, uri);
      if (nai != null)
        res.add(nai);
    }

    // order by desc date, asc id
    Collections.sort(res, new Comparator<NewArtInfo>() {
      public int compare(NewArtInfo o1, NewArtInfo o2) {
        int res = (o2.date != null) ? o2.date.compareTo(o1.date) : ((o1.date != null) ? -1 : 0);
        return (res == 0) ? o1.id.compareTo(o2.id) : res;
      }
    });

    return res;
  }

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

  /**
   * Clear the issue with the given doi from the browseCache. 
   * 
   * @param issueDoi
   */
  public void clearIssueInfoCache(URI issueDoi) {
    browseCache.remove(BrowseService.ISSUE_KEY + issueDoi);
  }

  private class Invalidator implements ObjectListener {
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
        Updates updates) {
      if (o instanceof Article) {
        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the article that was updated.");
        notifyArticlesChanged(new String[]{id});
      } else if (o instanceof Journal) {
        String key = ((Journal)o).getKey();
        if ((updates == null)
                 || updates.isChanged("smartCollectionRules")
                 || updates.isChanged("simpleCollection")) {
          if (log.isDebugEnabled())
            log.debug("Updating browsecache for the journal that was modified.");
          notifyJournalModified(key);
        }
      } else if (o instanceof Volume) {
        if ((updates != null) && updates.isChanged("issueList")) {
          if (log.isDebugEnabled())
            log.debug("Updating issue-infos for the Volume that was modified.");
          for (URI issue : ((Volume)o).getIssueList())
            clearIssueInfoCache(issue);
          for (String v : updates.getOldValue("issueList"))
            clearIssueInfoCache(URI.create(v));
        }
      } else if (o instanceof Issue) {
        if (log.isDebugEnabled())
          log.debug("Updating issue-info for the Issue that was modified.");
        clearIssueInfoCache(((Issue)o).getId());
      }
    }

    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      if (o instanceof Article) {
        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the article that was deleted.");
        notifyArticlesChanged(new String[]{id});
      } else if (o instanceof Journal) {
        if (log.isDebugEnabled())
          log.debug("Updating browsecache for the journal that was deleted.");
        notifyJournalModified(((Journal)o).getKey());
      } else if (o instanceof Issue) {
          if (log.isDebugEnabled())
            log.debug("Updating issue-info for the issue that was deleted.");
          clearIssueInfoCache(((Issue)o).getId());
      }
    }
  }
}
