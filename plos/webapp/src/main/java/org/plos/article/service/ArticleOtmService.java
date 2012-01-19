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
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.DateUtils;

import org.plos.article.util.ArticleDeleteException;
import org.plos.article.util.ArticleUtil;
import org.plos.article.util.IngestException;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.article.util.Zip;
import org.plos.journal.JournalService;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.models.Citation;
import org.plos.models.DublinCore;
import org.plos.models.License;
import org.plos.models.ObjectInfo;
import org.plos.models.RelatedArticle;
import org.plos.models.UserProfile;
import org.plos.util.CacheAdminHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;

/**
 * Provide Article "services" via OTM.
 */
public class ArticleOtmService {
  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  private ArticlePEP     pep;
  private Session        session;
  private JournalService jrnlSvc;
  private Ehcache        articleAnnotationCache;

  private static final Log log = LogFactory.getLog(ArticleOtmService.class);

  public ArticleOtmService() throws IOException {
    pep = new ArticlePEP();
  }

  /**
   * Ingest an article.
   *
   * @param dataHandler dataHandler
   * @return the uri of the article ingested
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final DataHandler dataHandler)
          throws DuplicateArticleIdException, IngestException, RemoteException, ServiceException,
                 IOException {
    // ask PEP if ingest is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    pep.checkAccess(ArticlePEP.INGEST_ARTICLE, ArticlePEP.ANY_RESOURCE);

    // ingest article
    ArticleUtil util = new ArticleUtil(session);
    String ret = util.ingest(
      new Zip.DataSourceZip(
        new org.apache.axis.attachments.ManagedMemoryDataSource(dataHandler.getInputStream(),
                                          8192, "application/octet-stream", true)));

    // notify journal service of new article
    jrnlSvc.objectWasAdded(URI.create(ret));

    return ret;
  }

  /**
   * Ingest an article.
   *
   * @param articleUrl articleUrl
   * @return the uri of the article ingested
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final URL articleUrl)
          throws DuplicateArticleIdException, IngestException, RemoteException, ServiceException,
                 IOException  {
    return ingest(new DataHandler(articleUrl));
  }

  /**
   * Mark an article as superseded by another article
   * @param oldUri oldUri
   * @param newUri newUri
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void markSuperseded(final String oldUri, final String newUri)
          throws NoSuchArticleIdException {
    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Get the URL from which the objects contents can be retrieved via GET.
   *
   * @param obj uri
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public String getObjectURL(final String obj, final String rep) throws NoSuchObjectIdException {
    // ask PEP if getting Object URL is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    pep.checkAccess(ArticlePEP.GET_OBJECT_URL, URI.create(obj));

    ObjectInfo a = session.get(ObjectInfo.class, obj);
    if (a == null)
      throw new NoSuchObjectIdException(obj);

    return ArticleUtil.getFedoraDataStreamURL(a.getPid(), rep);
  }

  /**
   * Delete an article.
   *
   * @param article uri
   * @throws RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void delete(String article)
          throws RemoteException, NoSuchArticleIdException, IOException, ArticleDeleteException {
    // ask PEP if delete is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    pep.checkAccess(ArticlePEP.DELETE_ARTICLE, URI.create(article));

    ArticleUtil.delete(article, session);
    jrnlSvc.objectWasDeleted(URI.create(article));
  }

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param state state
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void setState(final String article, final int state) throws NoSuchArticleIdException {
    pep.checkAccess(ArticlePEP.SET_ARTICLE_STATE, URI.create(article));

    Article a = session.get(Article.class, article);
    if (a == null)
      throw new NoSuchArticleIdException(article);

    a.setState(state);
    for (ObjectInfo oi : a.getParts())
      oi.setState(state);
    for (Category c : a.getCategories())
      c.setState(state);

    session.saveOrUpdate(a);
  }

  /**
   * Get the ids of all articles satisfying the given criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date).
   * @return the (possibly empty) list of article ids.
   */
  public List<String> getArticleIds(final String startDate, final String endDate,
                                    final int[] states, boolean ascending) throws Exception {
    final StringBuilder qry = new StringBuilder();
    qry.append("select art.id, art.dublinCore.date d from Article art where ");

    if (startDate != null)
      qry.append("ge(art.dublinCore.date, :sd) and ");
    if (endDate != null)
      qry.append("le(art.dublinCore.date, :ed) and ");

    if (states != null && states.length > 0) {
      qry.append("(");
      for (int idx = 0; idx < states.length; idx++)
        qry.append("art.state = :st").append(idx).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(")");
    }

    if (qry.indexOf(" and ", qry.length() - 5) > 0)
      qry.setLength(qry.length() - 4);
    if (qry.indexOf(" where ", qry.length() - 7) > 0)
      qry.setLength(qry.length() - 6);

    qry.append("order by d ").append(ascending ? "asc" : "desc").append(";");

    Query q = session.createQuery(qry.toString());
    if (startDate != null)
      q.setParameter("sd", startDate);
    if (endDate != null)
      q.setParameter("ed", endDate);
    for (int idx = 0; states != null && idx < states.length; idx++)
      q.setParameter("st" + idx, states[idx]);

    List<URI> ids = new ArrayList<URI>();

    Results r = q.execute();
    while (r.next())
      ids.add(r.getURI(0));

    List<String> res = new ArrayList<String>();
    for (URI id : ids) {
      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, id);
        res.add(id.toString());
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }

    return res;
  }

  /**
   * Get all articles satisfying the given criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
   *                   be appropriate. For archive display, ascending would be appropriate.
   * @return the (possibly empty) list of articles.
   * @throws ParseException if any of the dates could not be parsed
   */
  public Article[] getArticles(final String startDate, final String endDate, final String[] categories,
                               final String[] authors, final int[] states, final boolean ascending)
      throws ParseException {
    
    // get a list of Articles that meet the specified Criteria and Restrictions
    Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();
    orderBy.put("dublinCore.date", ascending); // first order by publication date
    orderBy.put("id", Boolean.TRUE); // secondary order by article id
    List<Article> articleList = findArticles(startDate, endDate, categories, authors, states, orderBy, 0);
    return articleList.toArray(new Article[articleList.size()]);
  }

  /**
   * A full featured getArticles.
   * 
   * @param startDate
   *            is the date to start searching from. If null, start from begining of time. Can be
   *            iso8601 formatted or string representation of Date object.
   * @param endDate
   *            is the date to search until. If null, search until present date
   * @param categories
   *            is list of categories to search for articles within (all categories if null or
   *            empty)
   * @param authors
   *            is list of authors to search for articles within (all authors if null or empty)
   * @param states
   *            the list of article states to search for (all states if null or empty)
   * @param orderBy
   *            controls the ordering. The keys specify the fields to be ordered, and the values
   *            whether order is ascending (true) or descending (false).
   * @return the (possibly empty) list of articles.
   * @throws ParseException
   *             if any of the dates could not be parsed
   */
  public List<Article> getArticles(final String startDate, final String endDate,
                                   final String[] categories, final String[] authors,
                                   final int[] states, final Map<String, Boolean> orderBy,
                                   final int maxResults)
      throws ParseException {
    // get a list of Articles that meet the specified Criteria and Restrictions
    return
        findArticles(startDate, endDate, categories, authors, states, orderBy, maxResults);
  }

  /**
   * Search for articles matching the specific criteria. Must be called with a transaction
   * active.
   */
  private List<Article> findArticles(String startDate, String endDate, String[] categories,
                                     String[] authors, int[] states, Map<String, Boolean> orderBy,
                                     int maxResults)
      throws ParseException {
    // build up Criteria for the Articles
    Criteria articleCriteria = session.createCriteria(Article.class);

    // normalize dates for query
    if (startDate != null) {
      articleCriteria.add(Restrictions.ge("dublinCore.date", parseDateParam(startDate)));
    }
    if (endDate != null) {
      articleCriteria.add(Restrictions.le("dublinCore.date", parseDateParam(endDate)));
    }

    // match all categories
    if (categories != null) {
      Criteria catCriteria = articleCriteria.createCriteria("categories");
      for (String category : categories) {
        catCriteria.add(Restrictions.eq("mainCategory", category));
      }
    }

    // match all authors
    if (authors != null) {
      for (String author : authors) {
        articleCriteria.add(Restrictions.eq("dublinCore.creators", author));
      }
    }

    // match all states
    if (states != null && states.length > 0) {
      Disjunction or = Restrictions.disjunction();
      for (int state : states) {
        or.add(Restrictions.eq("state", state));
      }
      articleCriteria.add(or);
    }

    // order by
    if (orderBy != null) {
      for (String field : orderBy.keySet()) {
        boolean ascending = (boolean) orderBy.get(field);
        articleCriteria.addOrder(ascending ? Order.asc(field) : Order.desc(field));
      }
    }

    // max results
    if (maxResults > 0) {
      articleCriteria.setMaxResults(maxResults);
    }

    // get a list of Articles that meet the specified Criteria and Restrictions
    List<Article> articleList = articleCriteria.list();

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    for (Iterator it = articleList.iterator(); it.hasNext(); ) {
      Article article = (Article) it.next();
      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, article.getId());
      } catch (SecurityException se) {
        it.remove();
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + article.getId()
                    + " from Article list due to PEP SecurityException", se);
      }
    }

    // TODO: touch all of the Article to force OTM to resolve references,
    // needed for cache Serialization, avoid lazy loading issues in webapp usage.
    for (Article article : articleList) {  getAllArticle(article); }

    return articleList;
  }

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public ObjectInfo getObjectInfo(final String uri) throws NoSuchObjectIdException {
    // sanity check parms
    if (uri == null)
      throw new IllegalArgumentException("URI == null");
    URI realURI = URI.create(uri);

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, realURI);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " from ObjectInfo list due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    ObjectInfo objectInfo = session.get(ObjectInfo.class, uri);
    if (objectInfo == null)
      throw new NoSuchObjectIdException(uri);
    return objectInfo;
  }

  /**
   * Get an Article by URI.
   *
   * NOTE: Use FetchArticleService.getArticleInfo() instead of method since it caches
   * the Article. Also - ensure that the cache is cleared if the data you want to access is updated.
   *
   * @param uri URI of Article to get.
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIDException NoSuchArticleIdException
   */
  public Article getArticle(final URI uri) throws NoSuchArticleIdException {
    // sanity check parms
    if (uri == null)
      throw new IllegalArgumentException("URI == null");

    // filter access by id with PEP
    try {
      pep.checkAccess(ArticlePEP.READ_META_DATA, uri);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    Article article = session.get(Article.class, uri.toString());
    if (article == null) {
      throw new NoSuchArticleIdException(uri.toString());
    }
    
    // TODO: touch all of the Article to force OTM to resolve references,
    // needed for cache Serialization, avoid lazy loading issues in webapp usage.
    getAllArticle(article);
    return article;
  }


  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article) throws NoSuchArticleIdException {
    
    pep.checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));
    
    // do cache aware lookup
    final Object lock = (FetchArticleService.ARTICLE_LOCK + article).intern(); // lock @ Article
    return CacheAdminHelper
        .getFromCacheE(articleAnnotationCache, FetchArticleService.ARTICLE_SECONDARY_KEY + article, -1, lock,
                       "secondaryObjects",
                       new CacheAdminHelper.EhcacheUpdaterE<SecondaryObject[], NoSuchArticleIdException>() {
                         public SecondaryObject[] lookup() throws NoSuchArticleIdException {
                           // get objects
                           Article art = session.get(Article.class, article);
                           if (art == null) {
                             throw new NoSuchArticleIdException(article);
                           }
                           // TODO: Article.parts should be RdfSeq or RdfList, forced to walk
                            // nextObject
                           List<ObjectInfo> sorted = new ArrayList<ObjectInfo>();
                           for (ObjectInfo oi = art.getNextObject(); oi != null; oi = oi.getNextObject()) {
                             sorted.add(oi);
                           }
                           // convert to SecondaryObject's. TODO: re-factor to return ObjectInfo
                           return convert(sorted.toArray(new ObjectInfo[sorted.size()]));
                         }
                       });
  }

  /**
   * Return a list of Figures and Tables in DOI order.
   *
   * @param article DOI.
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  public SecondaryObject[] listFiguresTables(final String article) throws NoSuchArticleIdException {

    final String FIGURE_CONTEXT = "fig";
    final String TABLE_CONTEXT = "table-wrap";

    pep.checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));

    // do cache aware lookup
    final Object lock = (FetchArticleService.ARTICLE_LOCK + article).intern();  // lock @ Article
    return CacheAdminHelper.getFromCacheE(articleAnnotationCache,
      FetchArticleService.ARTICLE_FIGURESTABLE_KEY + article, -1, lock, "figuresTables",
      new CacheAdminHelper.EhcacheUpdaterE<SecondaryObject[], NoSuchArticleIdException>() {
        public SecondaryObject[] lookup() throws NoSuchArticleIdException {
          Disjunction figuresTables = Restrictions.disjunction();
          figuresTables.add(Restrictions.eq("contextElement", FIGURE_CONTEXT))
                  .add(Restrictions.eq("contextElement", TABLE_CONTEXT));
          
          List<ObjectInfo> sorted = session.createCriteria(ObjectInfo.class)
                  .add(Restrictions.eq("isPartOf", article))
                  .add(figuresTables)
                  .addOrder(Order.asc("id"))
                  .list();
          
          if (log.isDebugEnabled()) {
            log.debug("list of figures and tables for " + article + ":" + sorted);
          }
          
          // TODO: why sort it again?
          Collections.sort(sorted, new Comparator<ObjectInfo>() {
            public int compare(ObjectInfo arg0, ObjectInfo arg1) {
              return arg0.getId().compareTo((arg1.getId()));
            }
          });

          if (log.isDebugEnabled()) {
            log.debug("*post sort*: list of figures and tables for " + article + ":" + sorted);
          }

          // convert to SecondaryObject's. TODO: re-factor to return ObjectInfo
          return convert(sorted.toArray(new ObjectInfo[sorted.size()]));
        }
    });
  }

  private SecondaryObject[] convert(final ObjectInfo[] objectInfos) {
    if (objectInfos == null) {
      return null;
    }
    SecondaryObject[] convertedObjectInfos = new SecondaryObject[objectInfos.length];
    for (int i = 0; i < objectInfos.length; i++) {
      convertedObjectInfos[i] = convert(objectInfos[i]);
    }
    return convertedObjectInfos;
  }

  /**
   * Get the most commented Articles.
   * The actual # of Articles returned maybe &lt; maxArticles
   * as PEP filtering is done on the results.
   *
   * @param maxArticles Maximum # of Articles to retrieve.
   * @return Article[] of most commented Articles.
   */
  public Article[] getCommentedArticles(final int maxArticles) {
    // sanity check args
    if (maxArticles < 0) {
      throw new IllegalArgumentException("Requesting a maximum # of commented articles < 0: " +
                                         maxArticles);
    }
    if (maxArticles == 0) {
      return new Article[0];
    }

    final String oqlQuery =
      "select a, count(n) c from Article a, Annotation n where n.annotates = a order by c desc limit " + maxArticles;

    Results commentedArticles = session.createQuery(oqlQuery).execute();

    // check access control on all Article results
    // logged in user is automatically resolved by the ServletActionContextAttribute
    ArrayList<Article> returnArticles = new ArrayList();

    commentedArticles.beforeFirst();
    while (commentedArticles.next()) {
      Article commentedArticle = (Article) commentedArticles.get("a");
      try {
        pep.checkAccess(ArticlePEP.READ_META_DATA, commentedArticle.getId());
        returnArticles.add(commentedArticle);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + commentedArticle.getId()
                    + " from commented Article list due to PEP SecurityException", se);
      }
    }

    return returnArticles.toArray(new Article[returnArticles.size()]);
  }


  private SecondaryObject convert(final ObjectInfo objectInfo) {
    return new SecondaryObject(objectInfo, smallImageRep, mediumImageRep, largeImageRep);
  }

  /**
   * Create or update a representation of an object. The object itself must exist; if the specified
   * representation does not exist, it is created, otherwise the current one is replaced.
   *
   * @param objId    the URI of the object
   * @param rep      the name of this representation
   * @param content  the actual content that makes up this representation; if this contains a
   *                 content-type then that will be used; otherwise the content-type will be
   *                 set to <var>application/octet-stream</var>; may be null, in which case
   *                 the representation is removed.
   * @throws NoSuchObjectIdException if the object does not exist
   * @throws RemoteException if some other error occured
   * @throws NullPointerException if any of the parameters are null
   */
  public void setRepresentation(final String objId, final String rep, final DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    pep.checkAccess(ArticlePEP.SET_REPRESENTATION, URI.create(objId));

    // get the object info
    ObjectInfo obj = session.get(ObjectInfo.class, objId);
    if (obj == null)
      throw new NoSuchObjectIdException(objId);

    String ct = content.getContentType();
    if (ct == null)
      ct = "application/octet-stream";

    // update fedora
    long len = ArticleUtil.setDatastream(obj.getPid(), rep, ct, content);

    // update the rdf
    obj.getRepresentations().add(rep);

    obj.getData().put("topaz:" + rep + "-contentType", toList(ct));
    obj.getData().put("topaz:" + rep + "-objectSize", toList(Long.toString(len)));

    session.saveOrUpdate(obj);
  }

  private static <T> List<T> toList(T item) {
    List<T> res = new ArrayList<T>();
    res.add(item);
    return res;
  }

  /**
   * Set the small image representation
   * @param smallImageRep smallImageRep
   */
  public void setSmallImageRep(final String smallImageRep) {
    this.smallImageRep = smallImageRep;
  }

  /**
   * Set the medium image representation
   * @param mediumImageRep mediumImageRep
   */
  public void setMediumImageRep(final String mediumImageRep) {
    this.mediumImageRep = mediumImageRep;
  }

  /**
   * Set the large image representation
   * @param largeImageRep largeImageRep
   */
  public void setLargeImageRep(final String largeImageRep) {
    this.largeImageRep = largeImageRep;
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
   * Set the journal service. Called by spring's bean wiring.
   *
   * @param service The journal session to set.
   */
  @Required
  public void setJournalService(JournalService service) {
    this.jrnlSvc = service;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }

  /**
   * Parse a kowari date into a java Date object.
   *
   * @param iso8601date is the date string to parse.
   * @return a java Date object.
   * @throws ParseException if there is a problem parsing the string
   */
  private static Date parseDate(String iso8601date) throws ParseException {
    // Obvious formats:
    final String[] defaultFormats = new String [] {
      "yyyy-MM-dd", "y-M-d", "y-M-d'T'H:m:s", "y-M-d'T'H:m:s.S",
      "y-M-d'T'H:m:s.Sz", "y-M-d'T'H:m:sz" };

    // TODO: Replace with fedora.server.utilities.DateUtility some how?
    // XXX: Deal with ' ' instead of 'T'
    // XXX: Deal with timezone in iso8601 format (not java's idea)

    return DateUtils.parseDate(iso8601date, defaultFormats);
  }

  /**
   * Convert a date passed in as a string to a Date object. Support both string representations
   * of the Date object and iso8601 formatted dates.
   *
   * @param date the string to convert to a Date object
   * @return a date object (or null if date is null)
   * @throws ParseException if unable to parse date
   */
  private static Date parseDateParam(String date) throws ParseException {
    if (date == null)
      return null;
    try {
      return new Date(date);
    } catch (IllegalArgumentException iae) {
      if (log.isDebugEnabled())
        log.debug("failed to parse date '" + date + "' use Date - trying iso8601 format", iae);
      return parseDate(date);
    }
  }

  private static void getAllArticle(Article article) {

    if (article == null) { return; }

    iterateAll(article.getArticleType());
    if (article.getCategories() != null) {
      for (Category category : article.getCategories()) { getAllCategory(category); }
    }
    if (article.getParts() != null) {
      for (ObjectInfo objectInfo : article.getParts()) { getAllObjectInfo(objectInfo); }
    }
    if (article.getRelatedArticles() != null) {
      for (RelatedArticle relatedArticle : article.getRelatedArticles()) {
        getAllRelatedArticle(relatedArticle);
      }
    }
    getAllObjectInfo((ObjectInfo) article);
  }

  private static void getAllObjectInfo(ObjectInfo objectInfo) {

    if (objectInfo == null) { return; }

    objectInfo.getContextElement();
    // objectInfo.getData();
    getAllDublinCore(objectInfo.getDublinCore());
    // objectInfo.getEIssn();
    // objectInfo.getId();
    // skip objectInfo.getIsPartOf(), avoid recursing into self Article
    getAllObjectInfo(objectInfo.getNextObject());
    // objectInfo.getPid();
    // iterateAll(objectInfo.getRepresentations());
    // objectInfo.getState();
  }

  private static void getAllRelatedArticle(RelatedArticle relatedArticle) {

    if (relatedArticle == null) { return; }

    relatedArticle.getArticle();
    // relatedArticle.getId();
    // relatedArticle.getRelationType();
  }

  private static void getAllDublinCore(DublinCore dublinCore) {

    if (dublinCore == null) { return; }

    dublinCore.getAccepted();
    // dublinCore.getAvailable();
    getAllCitation(dublinCore.getBibliographicCitation());
    // dublinCore.getConformsTo();
    // iterateAll(dublinCore.getContributors());
    // dublinCore.getCopyrightYear();
    // dublinCore.getCreated();
    // iterateAll(dublinCore.getCreators());
    // dublinCore.getDate();
    // dublinCore.getDescription();
    // dublinCore.getFormat();
    // dublinCore.getIdentifier();
    // dublinCore.getIssued();
    // dublinCore.getLanguage();
    if (dublinCore.getLicense() != null) {
      for (License license : dublinCore.getLicense()) { getAllLicense(license); }
    }
    // dublinCore.getModified();
    // dublinCore.getPublisher();
    if (dublinCore.getReferences() != null) {
      for (Citation citation : dublinCore.getReferences()) { getAllCitation(citation); }
    }
    // dublinCore.getSource();
    // iterateAll(dublinCore.getSubjects());
    // dublinCore.getSubmitted();
    // iterateAll(dublinCore.getSummary());
    // dublinCore.getTitle();
    // dublinCore.getType();
  }

  private static void getAllCategory(Category category) {

    if (category == null) { return; }

    category.getId();
    // category.getMainCategory();
    // category.getPid();
    // category.getState();
    // category.getSubCategory();
  }

  private static void getAllCitation(Citation citation) {

    if (citation == null) { return; }

    if (citation.getAuthors() != null) {
      for (UserProfile userProfile : citation.getAuthors()) { getAllUserProfile(userProfile); }
    }
    iterateAll(citation.getAuthorsRealNames());
    // citation.getCitationType();
    // citation.getDisplayYear();
    if (citation.getEditors() != null) {
      for (UserProfile userProfile : citation.getEditors()) { getAllUserProfile(userProfile); }
    }
    // citation.getId();
    // citation.getIssue();
    // citation.getJournal();
    // citation.getKey();
    // citation.getMonth();
    // citation.getNote();
    // citation.getPages();
    // citation.getPublisherLocation();
    // citation.getPublisherName();
    // citation.getSummary();
    // citation.getTitle();
    // citation.getUrl();
    // citation.getVolume();
    // citation.getVolumeNumber();
    // citation.getYear();
  }

  private static void getAllUserProfile(UserProfile userProfile) {

    if (userProfile == null) { return; }

    userProfile.getBiography();
    // userProfile.getBiographyText();
    // userProfile.getCity();
    // userProfile.getCountry();
    // userProfile.getDisplayName();
    // userProfile.getEmail();
    // userProfile.getEmailAsString();
    // userProfile.getGender();
    // userProfile.getGivenNames();
    // userProfile.getHomePage();
    // userProfile.getId();
    // iterateAll(userProfile.getInterests());
    // userProfile.getInterestsText();
    // userProfile.getOrganizationName();
    // userProfile.getOrganizationType();
    // userProfile.getPositionType();
    // userProfile.getPostalAddress();
    // userProfile.getPublications();
    // userProfile.getRealName();
    // userProfile.getResearchAreasText();
    // userProfile.getSurnames();
    // userProfile.getTitle();
    // userProfile.getWeblog();
  }

  private static void getAllLicense(License license) {

    if (license == null) { return; }

    license.getId();
  }

  private static void iterateAll(Collection collection) {

    if (collection == null) { return; }

    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      iterator.next();
    }
  }
}
