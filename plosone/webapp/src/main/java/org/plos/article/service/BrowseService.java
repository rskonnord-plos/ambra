/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.article.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts2.ServletActionContext;

import org.plos.models.Article;
import org.plos.models.Issue;
import org.plos.models.PLoS;
import org.plos.models.Volume;
import org.plos.util.CacheAdminHelper;
import org.plos.web.VirtualJournalContext;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Rdf;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Required;

/**
 * Class to get all Articles in system and organize them by date and by category
 *
 * @author stevec
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

  private static final URI    RESEARCH_ART = URI.create(PLoS.PLOS_ArticleType + "research-article");
  private static final URI    EDITORIAL    = URI.create(PLoS.PLOS_ArticleType + "editorial");
  private static final URI    CORRECTION   = URI.create(PLoS.PLOS_ArticleType + "correction");

  private final ArticlePEP pep;
  private       Session    session;
  private       Ehcache    browseCache;


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
  public Years getArticleDates() {
    return getArticleDates(true);
  }

  private Years getArticleDates(boolean load) {
    String jnlName = getCurrentJournal();
    String key     = DATE_LIST_KEY + jnlName;
    Object lock    = (DATE_LIST_LOCK + jnlName).intern();

    return
      CacheAdminHelper.getFromCache(browseCache, key, -1, lock, "article dates",
                                    !load ? null : new CacheAdminHelper.EhcacheUpdater<Years>() {
        public Years lookup() {
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
  public List<ArticleInfo> getArticlesByCategory(final String catName, int pageNum, int pageSize,
                                                 int[] numArt) {
    List<URI> uris = ((SortedMap<String, List<URI>>)
                        getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", true)).get(catName);

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return getArticles(uris, pageNum, pageSize);
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
  public List<ArticleInfo> getArticlesByDate(final Calendar startDate, final Calendar endDate,
                                             int pageNum, int pageSize, int[] numArt) {
    String jnlName = getCurrentJournal();
    String mod     = jnlName + "-" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis();
    String key     = ARTBYDATE_LIST_KEY + mod;
    Object lock    = (ARTBYDATE_LIST_LOCK + mod).intern();
    int    ttl     = getSecsTillMidnight();

    List<URI> uris =
      CacheAdminHelper.getFromCache(browseCache, key, ttl, lock, "articles by date",
                                    new CacheAdminHelper.EhcacheUpdater<List<URI>>() {
        public List<URI> lookup() {
          return loadArticlesByDate(startDate, endDate);
        }
      });

    if (uris == null) {
      numArt[0] = 0;
      return null;
    } else {
      numArt[0] = uris.size();
      return getArticles(uris, pageNum, pageSize);
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
  public SortedMap<String, Integer> getCategoryInfos() {
    return (SortedMap<String, Integer>) getCatInfo(CAT_INFO_KEY, "category infos", true);
  }

  /**
   * Get Issue information.
   *
   * @param issue DOI of Issue.
   * @return the Issue information.
   */
  public IssueInfo getIssueInfo(final URI doi) {

    // XXX look up IssueInfo in Cache

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<IssueInfo>() {

        // TODO should all of this be in a tx???

        public IssueInfo run(Transaction tx) {

          return getIssueInfoInTx(session, doi);
        }
      });
  }

  /**
   * Get Issue information inside of a Transaction.
   *
   * @param issue DOI of Issue.
   * @return the Issue information.
   */
  private IssueInfo getIssueInfoInTx(Session session, final URI doi) {

    // XXX look up IssueInfo in Cache

    // get the Issue
    final Issue issue = session.get(Issue.class, doi.toString());
    if (issue == null) { return null; }

    // get the image Article
    URI imageArticle = null;
    String description = null;
    if (issue.getImage() != null
        && issue.getImage().toString().length() != 0) {
      final Article article = session.get(Article.class, issue.getImage().toString());
      if (article != null) {
        imageArticle = issue.getImage();
        description = article.getDublinCore().getDescription();
      }
    }

    // display is by Article type
    List<ArticleInfo> editorials = new ArrayList();
    List<ArticleInfo> researchArticles = new ArrayList();
    List<ArticleInfo> corrections = new ArrayList();
    for (final URI articleDoi : issue.getSimpleCollection()) {
      ArticleInfo articleInIssue = getArticleInfo(articleDoi, session.getTransaction());
      if (articleInIssue == null) {
        log.warn("Article " + articleDoi + " missing; member of Issue " + doi);
        continue;
      }

      boolean articleAdded = false;
      if (articleInIssue.getArticleTypes().contains(RESEARCH_ART)) {
        researchArticles.add(articleInIssue);
        articleAdded = true;
      }
      if (articleInIssue.getArticleTypes().contains(EDITORIAL)) {
        editorials.add(articleInIssue);
        articleAdded = true;
      }
      if (articleInIssue.getArticleTypes().contains(CORRECTION)) {
        corrections.add(articleInIssue);
        articleAdded = true;
      }
      // ensure Article is displayed
      if (!articleAdded) {
        researchArticles.add(articleInIssue);
      }
    }

    return new IssueInfo(issue.getId(), issue.getDisplayName(), issue.getPrevIssue(),
      issue.getNextIssue(), imageArticle, description, editorials, researchArticles,
      corrections);
  }

  /**
   * Get VolumeInfos.
   *
   * @return volumeInfos.
   */
  public List<VolumeInfo> getVolumeInfos() {

    // XXX look up VolumeInfos in Cache

    // OTM usage wants to be in a Transaction
    return TransactionHelper.doInTx(session,
      new TransactionHelper.Action<List<VolumeInfo>>() {

        // TODO should all of this be in a tx???

        public List<VolumeInfo> run(Transaction tx) {

          List<VolumeInfo> volumeInfos = new ArrayList();
          // get the Volumes
          for (final Volume volume : (List<Volume>) session.createCriteria(Volume.class).list()) {

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

            List<IssueInfo> issueInfos = new ArrayList();
            for (final URI issueDoi : volume.getSimpleCollection()) {
              issueInfos.add(getIssueInfoInTx(session, issueDoi));
            }

            final VolumeInfo volumeInfo = new VolumeInfo(volume.getId(), volume.getDisplayName(),
              volume.getPrevVolume(), volume.getNextVolume(), imageArticle, description,
              issueInfos);
            volumeInfos.add(volumeInfo);
          }

          return volumeInfos;
        }
      });
  }

  private Object getCatInfo(String key, String desc, boolean load) {
    String jnlName = getCurrentJournal();
    key += jnlName;

    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      Element e = browseCache.get(key);

      if (e == null) {
        if (!load)
          return null;

        if (log.isDebugEnabled())
          log.debug("retrieving " + desc + " from db");

        loadCategoryInfos(jnlName);
        e = browseCache.get(key);
      } else if (log.isDebugEnabled()) {
        log.debug("retrieved " + desc + " from cache");
      }

      return e.getValue();
    }
  }

  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
               getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * Notify this service that articles have been deleted from the system.
   *
   * @param uris the list of id's of the deleted articles
   */
  public void notifyArticlesDeleted(String[] uris) {
    String jnlName = getCurrentJournal();

    // update category lists
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
          getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false);
      if (artByCat != null) {
        Set<URI> uriSet = new HashSet<URI>();
        for (String uri : uris)
          uriSet.add(URI.create(uri));

        cat: for (Iterator<List<URI>> catIter = artByCat.values().iterator(); catIter.hasNext(); ) {
          List<URI> articles = catIter.next();
          for (Iterator<URI> artIter = articles.iterator(); artIter.hasNext(); ) {
            URI art = artIter.next();
            if (uriSet.remove(art)) {
              artIter.remove();

              if (articles.isEmpty())
                catIter.remove();

              if (uriSet.isEmpty())
                break cat;
            }
          }
        }

        updateCategoryCaches(artByCat, jnlName);
      }
    }

    // update date lists
    browseCache.remove(DATE_LIST_KEY + jnlName);

    for (Object key : browseCache.getKeysNoDuplicateCheck()) {
      if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
        browseCache.remove(key);
    }

    // update article cache
    for (String uri : uris)
      browseCache.remove(ARTICLE_KEY + uri);
  }

  /**
   * Notify this service that articles have been added to the system.
   *
   * @param uris the list of id's of the added articles
   */
  public void notifyArticlesAdded(String[] uris) {
    String jnlName = getCurrentJournal();

    // get some info about the new articles
    List<NewArtInfo> nais = getNewArticleInfos(uris);

    // update category lists
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      final SortedMap<String, List<URI>> artByCat = (SortedMap<String, List<URI>>)
                                    getCatInfo(ARTBYCAT_LIST_KEY, "articles by category ", false);
      if (artByCat != null) {
        for (NewArtInfo nai: nais) {
          List<URI> arts = artByCat.get(nai.category);
          if (arts == null)
            artByCat.put(nai.category, arts = new ArrayList<URI>());
          arts.add(0, nai.id);
        }

        updateCategoryCaches(artByCat, jnlName);
      }
    }

    // update date lists
    synchronized ((DATE_LIST_LOCK + jnlName).intern()) {
      Years dates = getArticleDates(false);
      if (dates != null) {
        for (NewArtInfo nai: nais)
          insertDate(dates, nai.date);
      }
    }

    for (Object key : browseCache.getKeysNoDuplicateCheck()) {
      if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
        browseCache.remove(key);
    }
  }

  /**
   * Notify this service that a journal definition has been modified. Should only be called when
   * the rules determining articles belonging to this journal have been changed.
   *
   * @param jnlName the key of the journal that was modified
   */
  public void notifyJournalModified(final String jnlName) {
    synchronized ((CAT_INFO_LOCK + jnlName).intern()) {
      browseCache.remove(CAT_INFO_KEY + jnlName);
      browseCache.remove(ARTBYCAT_LIST_KEY + jnlName);
    }

    synchronized ((DATE_LIST_LOCK + jnlName).intern()) {
      browseCache.remove(DATE_LIST_KEY + jnlName);
    }

    for (Object key : browseCache.getKeysNoDuplicateCheck()) {
      if (key instanceof String && ((String) key).startsWith(ARTBYDATE_LIST_KEY + jnlName))
        browseCache.remove(key);
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
    SortedMap<String, List<URI>> artByCat =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<SortedMap<String, List<URI>>>() {
        public SortedMap<String, List<URI>> run(Transaction tx) {
          Results r = tx.getSession().createQuery(
              "select cat, a.id, a.dublinCore.date date from Article a " +
              "where cat := a.categories.mainCategory order by date desc;").execute();

          SortedMap<String, List<URI>> artByCat = new TreeMap<String, List<URI>>();
          r.beforeFirst();
          while (r.next()) {
            String cat = r.getString(0);
            List<URI> ids = artByCat.get(cat);
            if (ids == null)
              artByCat.put(cat, ids = new ArrayList<URI>());
            ids.add(r.getURI(1));
          }

          return artByCat;
        }
      });

    updateCategoryCaches(artByCat, jnlName);
  }

  private void updateCategoryCaches(SortedMap<String, List<URI>> artByCat, String jnlName) {
    // calculate number of articles in each category
    SortedMap<String, Integer> catSizes = new TreeMap<String, Integer>();
    for (Map.Entry<String, List<URI>> e : artByCat.entrySet())
      catSizes.put(e.getKey(), e.getValue().size());

    // save in cache
    browseCache.put(new Element(ARTBYCAT_LIST_KEY + jnlName, artByCat));
    browseCache.put(new Element(CAT_INFO_KEY + jnlName, catSizes));
  }

  private ArticleInfo loadArticleInfo(final URI id, Transaction tx) {
    Results r = tx.getSession().createQuery(
        "select a.id, dc.date, dc.title, ci, " +
        "(select a.articleType from Article aa), " +
        "(select aa2.id, aa2.dublinCore.title from Article aa2 " +
        "   where aa2 = a.relatedArticles.article) " +
        "from Article a, BrowseService$CitationInfo ci " +
        "where a.id = :id and dc := a.dublinCore and ci.id = dc.bibliographicCitation.id;").
        setParameter("id", id).execute();

    r.beforeFirst();
    if (!r.next())
      return null;

    ArticleInfo ai = new ArticleInfo();
    ai.id    = id;
    ai.date  = r.getLiteralAs(1, Date.class);
    ai.title = r.getString(2);

    for (UserProfileInfo upi : ((CitationInfo) r.get(3)).authors) {
      upi.hashCode();     // force load
      ai.authors.add(upi.realName);
    }

    Results sr = r.getSubQueryResults(4);
    while (sr.next())
      ai.articleTypes.add(sr.getURI(0));

    sr = r.getSubQueryResults(5);
    while (sr.next())
      ai.relatedArticles.add(new RelatedArticleInfo(sr.getURI(0), sr.getString(1)));

    if (log.isDebugEnabled())
      log.debug("loaded ArticleInfo: id='" + ai.id + "', articleTypes='" + ai.articleTypes
                + "', date='" + ai.date + "', title='" + ai.title
                + "', authors='" + ai.authors + "', related-articles='" + ai.relatedArticles +
                "'");

    return ai;
  }

  private Years loadArticleDates() {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<Years>() {
      public Years run(Transaction tx) {
        Results r = tx.getSession().createQuery(
            "select a.dublinCore.date from Article a;").execute();

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
    });
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

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<URI>>() {
      public List<URI> run(Transaction tx) {
        Results r = tx.getSession().createQuery(
            "select a.id, date from Article a where " +
            "date := a.dublinCore.date and gt(date, :sd) and lt(date, :ed) order by date desc;").
            setParameter("sd", sd).setParameter("ed", endDate).execute();

        List<URI> dates = new ArrayList<URI>();

        r.beforeFirst();
        while (r.next())
          dates.add(r.getURI(0));

        return dates;
      }
    });
  }

  private List<ArticleInfo> getArticles(final List<URI> ids, int pageNum, int pageSize) {
    final int beg = (pageSize > 0) ? pageNum * pageSize : 0;
    final int end = (pageSize > 0) ? Math.min((pageNum + 1) * pageSize, ids.size()) : ids.size();

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<ArticleInfo>>() {
      public List<ArticleInfo> run(Transaction tx) {
        List<ArticleInfo> res = new ArrayList<ArticleInfo>();

        for (int idx = beg; idx < end; idx++) {
          URI id = ids.get(idx);

          ArticleInfo ai = getArticleInfo(id, tx);
          if (ai != null)
            res.add(ai);
        }

        return res;
      }
    });
  }

  public ArticleInfo getArticleInfo(final URI id, final Transaction tx) {
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, id);
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      return null;
    }

    return
      CacheAdminHelper.getFromCache(browseCache, ARTICLE_KEY + id, -1, ARTICLE_LOCK + id,
                                    "article " + id,
                                    new CacheAdminHelper.EhcacheUpdater<ArticleInfo>() {
        public ArticleInfo lookup() {
          return loadArticleInfo(id, tx);
        }
      });
  }

  private List<NewArtInfo> getNewArticleInfos(final String[] uris) {
    return TransactionHelper.doInTx(session, new TransactionHelper.Action<List<NewArtInfo>>() {
      public List<NewArtInfo> run(Transaction tx) {
        String query =
            "select cat, a.id, a.dublinCore.date date from Article a " +
            "where cat := a.categories.mainCategory and (";
        for (String uri : uris)
          query += "a.id = <" + uri + "> or ";
        query = query.substring(0, query.length() - 4) + ") order by date desc;";

        Results r = tx.getSession().createQuery(query).execute();

        List<NewArtInfo> res = new ArrayList<NewArtInfo>();
        r.beforeFirst();
        while (r.next()) {
          NewArtInfo nai = new NewArtInfo();
          nai.category = r.getString(0);
          nai.id       = r.getURI(1);
          nai.date     = r.getLiteralAs(2, Date.class);
          res.add(nai);
        }

        return res;
      }
    });
  }

  private static class NewArtInfo {
    public URI          id;
    public Date         date;
    public String       category;
  }

  /**
   * @param browseCache The browse-cache to use.
   */
  @Required
  public void setBrowseCache(Ehcache browseCache) {
    this.browseCache = browseCache;
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
   * The info about a single article that the UI needs.
   */
  public static class ArticleInfo implements Serializable {
    public URI                     id;
    public Set<URI>                articleTypes = new HashSet<URI>();
    public Date                    date;
    public String                  title;
    public List<String>            authors = new ArrayList<String>();
    public Set<RelatedArticleInfo> relatedArticles = new HashSet<RelatedArticleInfo>();

    /**
     * Get the id.
     *
     * @return the id.
     */
    public URI getId() {
      return id;
    }

    /**
     * Get the Article types.
     *
     * @return the Article types.
     */
    public Set<URI> getArticleTypes() {
      return articleTypes;
    }

    /**
     * Get the date.
     *
     * @return the date.
     */
    public Date getDate() {
      return date;
    }

    /**
     * Get the title.
     *
     * @return the title.
     */
    public String getTitle() {
      return title;
    }

    /**
     * Get the authors.
     *
     * @return the authors.
     */
    public List<String> getAuthors() {
      return authors;
    }

    /**
     * Get the related articles.
     *
     * @return the related articles.
     */
    public Set<RelatedArticleInfo> getRelatedArticles() {
      return relatedArticles;
    }
  }

  public static class RelatedArticleInfo implements Serializable {
    public final URI    uri;
    public final String title;

    /**
     * Create a new related-article-info object.
     *
     * @param uri   the uri of the article
     * @param title the article's title
     */
    public RelatedArticleInfo(URI uri, String title) {
      this.uri   = uri;
      this.title = title;
    }

    /**
     * Get the article uri.
     *
     * @return the article uri.
     */
    public URI getUri() {
      return uri;
    }

    /**
     * Get the title.
     *
     * @return the title.
     */
    public String getTitle() {
      return title;
    }

    public String toString() {
      return "RelatedArticleInfo[uri='" + uri + "', title='" + title + "']";
    }
  }

  /**
   * Just the list of authors.
   */
  @Entity(type = PLoS.bibtex + "Entry", model = "ri")
  public static class CitationInfo {
    @Id
    public URI id;

    @Predicate(uri = PLoS.plos + "hasAuthorList", storeAs = Predicate.StoreAs.rdfSeq)
    public List<UserProfileInfo> authors = new ArrayList<UserProfileInfo>();
  }

  /**
   * Just the full name.
   */
  @Entity(type = Rdf.foaf + "Person", model = "profiles")
  public static class UserProfileInfo {
    @Id
    public URI id;

    @Predicate(uri = Rdf.foaf + "name")
    public String realName;
  }

  /**
   * The info about a single Issue that the UI needs.
   */
  public static class IssueInfo implements Serializable {

    private URI          id;
    private String       displayName;
    private URI          prevIssue;
    private URI          nextIssue;
    private URI          imageArticle;
    private String       description;
    private List<ArticleInfo> editorials;
    private List<ArticleInfo> researchArticles;
    private List<ArticleInfo> corrections;

    // XXX TODO, List<URI> w/Article DOI vs. List<ArticleInfo>???

    public IssueInfo(URI id, String displayName, URI prevIssue, URI nextIssue, URI imageArticle,
      String description, List<ArticleInfo> editorials,
      List<ArticleInfo> researchArticles, List<ArticleInfo> corrections) {

      this.id = id;
      this.displayName = displayName;
      this.prevIssue = prevIssue;
      this.nextIssue = nextIssue;
      this.imageArticle = imageArticle;
      this.description = description;
      this.editorials = editorials;
      this.researchArticles = researchArticles;
      this.corrections = corrections;
    }

    /**
     * Get the id.
     *
     * @return the id.
     */
    public URI getId() {
      return id;
    }

    /**
     * Get the displayName.
     *
     * @return the displayName.
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Get the previous Issue.
     *
     * @return the previous Issue.
     */
    public URI getPrevIssue() {
      return prevIssue;
    }

    /**
     * Get the next Issue.
     *
     * @return the next Issue.
     */
    public URI getNextIssue() {
      return nextIssue;
    }

    /**
     * Get the image Article DOI.
     *
     * @return the image Article DOI.
     */
    public URI getImageArticle() {
      return imageArticle;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Get the editorials.
     *
     * @return the editorials.
     */
    public List<ArticleInfo> getEditorials() {
      return editorials;
    }

    /**
     * Get the research articles.
     *
     * @return the research articles.
     */
    public List<ArticleInfo> getResearchArticles() {
      return researchArticles;
    }
    /**
     * Get the corrections.
     *
     * @return the corrections.
     */
    public List<ArticleInfo> getCorrections() {
      return corrections;
    }
  }

  /**
   * The info about a single Volume that the UI needs.
   */
  public static class VolumeInfo implements Serializable {

    private URI          id;
    private String       displayName;
    private URI          prevVolume;
    private URI          nextVolume;
    private URI          imageArticle;
    private String       description;
    private List<IssueInfo> issueInfos;

    // XXX TODO, List<URI> w/Issue DOI vs. List<IssueInfo>???

    public VolumeInfo(URI id, String displayName, URI prevVolume, URI nextVolume, URI imageArticle,
      String description, List<IssueInfo> issueInfos) {

      this.id = id;
      this.displayName = displayName;
      this.prevVolume = prevVolume;
      this.nextVolume = nextVolume;
      this.imageArticle = imageArticle;
      this.description = description;
      this.issueInfos = issueInfos;
    }

    /**
     * Get the id.
     *
     * @return the id.
     */
    public URI getId() {
      return id;
    }

    /**
     * Get the displayName.
     *
     * @return the displayName.
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Get the previous Volume.
     *
     * @return the previous Volume.
     */
    public URI getPrevVolume() {
      return prevVolume;
    }

    /**
     * Get the next Volume.
     *
     * @return the next Volume.
     */
    public URI getNextVolume() {
      return nextVolume;
    }

    /**
     * Get the image Article DOI.
     *
     * @return the image Article DOI.
     */
    public URI getImageArticle() {
      return imageArticle;
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Get the issueInfos.
     *
     * @return the issueInfos.
     */
    public List<IssueInfo> getIssueInfos() {
      return issueInfos;
    }
  }

  /**
   * An ordered list of years (as year numbers). Each year has a list of months.
   */
  public static class Years extends TreeMap<Integer, Months> {
    /**
     * @return the list of months (possibly emtpy, but always non-null)
     */
    public Months getMonths(Integer year) {
      Months months = get(year);
      if (months == null)
        put(year, months = new Months());
      return months;
    }
  }

  /**
   * An ordered list of months (as month numbers, from 1 to 12). Each month has a list of days.
   */
  public static class Months extends TreeMap<Integer, Days> {
    /**
     * @return the list of days (possibly emtpy, but always non-null)
     */
    public Days getDays(Integer mon) {
      Days days = get(mon);
      if (days == null)
        put(mon, days = new Days());
      return days;
    }
  }

  /**
   * An ordered list of days.
   */
  public static class Days extends TreeSet<Integer> {
  }
}
