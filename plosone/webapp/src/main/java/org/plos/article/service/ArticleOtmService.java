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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.util.ArticleUtil;
import org.plos.article.util.IngestException;
import org.plos.article.util.Ingester;
import org.plos.article.util.DuplicateArticleIdException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.article.util.Zip;
import org.plos.models.Article;
import org.plos.models.Category;
import org.plos.models.ObjectInfo;
import org.plos.service.BaseConfigurableService;
import org.plos.service.WSTopazContext;
import org.plos.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.authentication.ProtectedService;
import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.fedora.client.FedoraAPIM;
import org.topazproject.fedoragsearch.service.FgsOperationsServiceLocator;
import org.topazproject.fedoragsearch.service.FgsOperations;
import org.topazproject.mulgara.itql.ItqlHelper;
// TODO: should feed mv up to webapp?
import org.topazproject.feed.ArticleFeed;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;

/**
 * Provide Article "services" via OTM.
 */
public class ArticleOtmService extends BaseConfigurableService {

  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  private ArticlePEP pep;
  private Session session;

  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();
  private static final Log log = LogFactory.getLog(ArticleOtmService.class);
  private static final List FGS_URLS = CONF.getList("topaz.fedoragsearch.urls.url");

  private final WSTopazContext ctx = new WSTopazContext(getClass().getName());
  
  private ArticlePEP getPEP() {
    // create an XACML PEP for Articles
    try {
      if (pep == null) {
        pep = new ArticlePEP();
      }
    } catch (Exception e) {
      throw new Error("Failed to create ArticlePEP", e);
    }
    return pep;
  }

  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService permissionProtectedService = getProtectedService();
  }

  /**
   * Ingest an article.
   * @param dataHandler dataHandler
   * @return the uri of the article ingested
   * @throws RemoteException RemoteException
   * @throws ServiceException ServiceException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final DataHandler dataHandler)
          throws DuplicateArticleIdException, IngestException, RemoteException, ServiceException {

    // session housekeeping ...
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if ingest is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
     getPEP().checkAccess(ArticlePEP.INGEST_ARTICLE, ArticlePEP.ANY_RESOURCE);

    // create an Ingester using the values from the WSTopazContext
    ctx.activate();

    // TODO: remove debug
    if (log.isDebugEnabled()) {
      log.debug("WSTopazContext isActive: " + ctx.isActive()
                + ", " + ctx.getItqlHelper()
                + "" + ctx.getFedoraAPIM()
                + ", " + ctx.getFedoraUploader()
                + ", " + getFgsOperations());
    }

    Ingester ingester = null;
    try {
      ingester = new Ingester(ctx.getItqlHelper(), ctx.getFedoraAPIM(), ctx.getFedoraUploader(), getFgsOperations());
    } finally {
      ctx.passivate();
    }

    // let Ingester.ingest() do the real work
    try {
      return ingester.ingest(
        new Zip.DataSourceZip(
          new org.apache.axis.attachments.ManagedMemoryDataSource(dataHandler.getInputStream(), 8192, "application/octet-stream", true))
          );
//      return ingester.ingest(new Zip.DataSourceZip(dataHandler.getDataSource()));
    } catch(IOException ioe) {
      log.error("Ingestion failed: ", ioe);
      throw new IngestException("Ingestion failed: ", ioe);
    }
  }

  /**
   * Ingest an article.
   * @param articleUrl articleUrl
   * @return the uri of the article ingested
   * @throws RemoteException RemoteException
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  public String ingest(final URL articleUrl)
          throws  DuplicateArticleIdException, IngestException, RemoteException, ServiceException {
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
    ensureInitGetsCalledWithUsersSessionAttributes();

    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Get the URL from which the objects contents can be retrieved via GET.
   *
   * @param obj uri
   * @param rep rep
   * @return the URL, or null if this object doesn't exist in the desired version
   * @throws RemoteException RemoteException
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public String getObjectURL(final String obj, final String rep)
          throws RemoteException, NoSuchObjectIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if getting Object URL is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    getPEP().checkAccess(ArticlePEP.GET_OBJECT_URL, URI.create(obj));

    // let the Article utils do the real work
    ArticleUtil articleUtil = null;
    try {
      articleUtil = new ArticleUtil();  // no arg, utils use default config
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    } catch (ServiceException se) {
      throw new RemoteException("Failed getting URL for \"" + obj + "\", \"" + rep + "\"", se);
    }

    // trap the Exception for better debgging
    String objectURL = null;
    try {
      objectURL= articleUtil.getObjectURL(obj, rep);
      if (log.isDebugEnabled()) {
        log.debug("ArticleOtmService.getObjectURL("+obj+", " + rep + ") = " + objectURL);
      }
    } catch (NoSuchObjectIdException nsoe) {
      if (log.isDebugEnabled()) {
        log.debug("failed to ArticleOtmService.getObjectURL(" + obj + ", " + rep + ")");
      }
      throw nsoe;
    }

    return objectURL;
  }

  /**
   * Delete an article.
   *
   * @param article uri
   * @throws RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void delete(final String article)
    throws RemoteException, ServiceException, NoSuchArticleIdException {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // ask PEP if delete is allowed
    // logged in user is automatically resolved by the ServletActionContextAttribute
    getPEP().checkAccess(ArticlePEP.DELETE_ARTICLE, URI.create(article));

    // let the Article utils do the real work
    ArticleUtil articleUtil = null;
    try {
     articleUtil = new ArticleUtil();  // no arg, utils use default config
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
    articleUtil.delete(article);
  }

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param state state
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void setState(final String article, final int state) throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    getPEP().checkAccess(ArticlePEP.SET_ARTICLE_STATE, URI.create(article));

    TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<Void, NoSuchArticleIdException>() {
      public Void run(Transaction tx) throws NoSuchArticleIdException {
        Article a = tx.getSession().get(Article.class, article);
        if (a == null)
          throw new NoSuchArticleIdException(article);

        a.setState(state);
        for (ObjectInfo oi : a.getParts())
          oi.setState(state);
        for (Category c : a.getCategories())
          c.setState(state);

        tx.getSession().saveOrUpdate(a);
        return null;
      }
    });
  }

  /**
   * Get a list of all articles
   * @param startDate startDate
   * @param endDate endDate
   * @return list of article uri's
   * @throws java.rmi.RemoteException RemoteException
   */
  public String getArticles(final String startDate, final String endDate) throws RemoteException {

    ensureInitGetsCalledWithUsersSessionAttributes();

    // PEP is called by full signature getArticles()
    return getArticles(
      startDate,
      endDate,
      null,   // convention for all categories
      null,   // convention for all authors
      null,   // convention for all states
      true);  // ascending
  }

  /*  * @param startDate  is the date to start searching from. If null, start from begining of time.
  *                   Can be iso8601 formatted or string representation of Date object.
  * @param endDate    is the date to search until. If null, search until present date
  * @param states     the list of article states to search for (all states if null or empty)
  * @param ascending  controls the sort order (by date). If used for RSS feeds, decending would
  *                   be appropriate. For archive display, ascending would be appropriate.
  * @return the xml for the specified feed
  * @throws RemoteException if there was a problem talking to the alerts service
  */
  public String getArticles(final String startDate, final String endDate,
                 final int[] states, boolean ascending) throws RemoteException {

    ensureInitGetsCalledWithUsersSessionAttributes();

    // PEP is called by full signature getArticles()
    return getArticles(
      startDate,
      endDate,
      null,  // convention for all categories
      null,  // convention for all authors
      states,
      true);
  }

  /**
   * @see org.topazproject.ws.article.Article#getArticleInfos(String, String, String[], String[], int[], boolean)
   */
  public ArticleInfo[] getArticleInfos(final String startDate, final String endDate,
                                       final String[] categories, final String[] authors,
                                       final int[] states, final boolean ascending)
      throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    return TransactionHelper.doInTxE(session,
                               new TransactionHelper.ActionE<ArticleInfo[], RemoteException>() {
      public ArticleInfo[] run(Transaction tx) throws RemoteException {
        // get a list of Articles that meet the specified Criteria and Restrictions
        List<Article> articleList =
            findArticles(startDate, endDate, categories, authors, states, ascending, tx);

        List<ArticleInfo> infoList = new ArrayList<ArticleInfo>();
        for (Iterator it = articleList.iterator(); it.hasNext(); )
          infoList.add(convert((Article) it.next()));

        return infoList.toArray(new ArticleInfo[infoList.size()]);
      }
    });
  }

  private static ArticleInfo convert(Article art) {
    ArticleInfo ai = new ArticleInfo();
    ai.setUri(art.getId().toString());
    ai.setTitle(art.getTitle());
    ai.setArticleDate(art.getDate());
    ai.setAuthors(art.getAuthors().toArray(new String[0]));
    ai.setSubjects(art.getSubjects().toArray(new String[0]));
    ai.setState(art.getState());

    Set<String> mainCats = new HashSet<String>();
    for (Category cat : art.getCategories())
      mainCats.add(cat.getMainCategory());
    ai.setCategories(mainCats.toArray(new String[mainCats.size()]));

    return ai;
  }

  /**
   * A full featured getArticles.
   */
  public List<Article> getArticles(
    final String startDate,
    final String endDate,
    final String[] categories,
    final String[] authors,
    final int[] states,
    final Map<String, Boolean> orderBy,
    final int maxResults)
    throws RemoteException {

    ensureInitGetsCalledWithUsersSessionAttributes();

    return TransactionHelper.doInTxE(session,
                               new TransactionHelper.ActionE<List<Article>, RemoteException>() {
      public List<Article> run(Transaction tx) throws RemoteException {
        // get a list of Articles that meet the specified Criteria and Restrictions
        List<Article> articleList =
            findArticles(startDate, endDate, categories, authors, states, orderBy, maxResults, tx);

        return articleList;
      }
    });
  }

  /**
   * Search for articles matching the specific criteria. Must be called with a transaction
   * active.
   *
   * FIXME: shouldn't be throwing RemoteException - that's only because ArticleFeed.parseDateParam
   * throws it.
   */
  private List<Article> findArticles(
    String startDate,
    String endDate,
    String[] categories,
    String[] authors,
    int[] states,
    Map<String, Boolean> orderBy,
    int maxResults,
    Transaction tx) throws RemoteException {

    // build up Criteria for the Articles
    Criteria articleCriteria = tx.getSession().createCriteria(Article.class);

    // normalize dates for query
    if (startDate != null) {
      articleCriteria = articleCriteria.add(Restrictions.ge("date", ArticleFeed.parseDateParam(startDate)));
    }
    if (endDate != null) {
      articleCriteria = articleCriteria.add(Restrictions.le("date", ArticleFeed.parseDateParam(endDate)));
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
        articleCriteria = articleCriteria.add(Restrictions.eq("authors", author));
      }
    }

    // match all states
    if (states != null) {
      for (int state : states) {
        articleCriteria = articleCriteria.add(Restrictions.eq("state", state));
      }
    }

    // order by
    if (orderBy != null) {
      for (String field : orderBy.keySet()) {
        boolean ascending = (boolean) orderBy.get(field);
        if (ascending) {
          articleCriteria = articleCriteria.addOrder(Order.asc(field));
        } else {
          articleCriteria = articleCriteria.addOrder(Order.desc(field));
        }
      }
    }

    // max results
    if (maxResults > 0) {
      articleCriteria = articleCriteria.setMaxResults(maxResults);
    }

    // get a list of Articles that meet the specified Criteria and Restrictions
    List<Article> articleList = articleCriteria.list();

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    for (Iterator it = articleList.iterator(); it.hasNext(); ) {
      Article article = (Article) it.next();
      try {
        getPEP().checkAccess(ArticlePEP.READ_META_DATA, article.getId());
      } catch (SecurityException se) {
        it.remove();
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + article.getId()
                    + " from Article list due to PEP SecurityException", se);
      }
    }

    return articleList;
  }

  /**
   * Get list of articles for a given set of categories, authors and states bracked by specified
   * times.
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
   * @throws RemoteException if there was a problem talking to any service
   */
  public String getArticles(final String startDate, final String endDate, final String[] categories,
                            final String[] authors, final int[] states, final boolean ascending)
      throws RemoteException {

    final List<ArticleFeedData> articleFeedData = new ArrayList();

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    TransactionHelper.doInTxE(session, new TransactionHelper.ActionE<Void, RemoteException>() {
      public Void run(Transaction tx) throws RemoteException {
        // get a list of Articles that meet the specified Criteria and Restrictions
        List<Article> articleList =
            findArticles(startDate, endDate, categories, authors, states, ascending, tx);

        for (Iterator it = articleList.iterator(); it.hasNext(); ) {
          Article article = (Article) it.next();
          ArticleFeedData articleFeedDatum = new ArticleFeedData(
            article.getId().toString(),
            article.getTitle(),
            article.getDescription(),
            article.getDate(),
            article.getAuthors(),
            article.getSubjects(),
            article.getCategories(),
            article.getState());
          articleFeedData.add(articleFeedDatum);
        }

        return null;
      }
    });

    return buildArticleFeedXml(articleFeedData);
    // TODO: confirm calling chain logic
    // return articleList.toArray(new Article[articleList.size()]);
  }

  /**
   * Search for articles matching the specific criteria. Must be called with a transaction
   * active.
   *
   * FIXME: shouldn't be throwing RemoteException - that's only because ArticleFeed.parseDateParam
   * throws it.
   */
  private List<Article> findArticles(String startDate,String endDate, String[] categories,
                                     String[] authors, int[] states, boolean ascending,
                                     Transaction tx) throws RemoteException {
    // build up Criteria for the Articles
    Criteria articleCriteria = tx.getSession().createCriteria(Article.class);

    // normalize dates for query
    if (startDate != null) {
      articleCriteria = articleCriteria.add(Restrictions.ge("date", ArticleFeed.parseDateParam(startDate)));
    }
    if (endDate != null) {
      articleCriteria = articleCriteria.add(Restrictions.le("date", ArticleFeed.parseDateParam(endDate)));
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
        articleCriteria = articleCriteria.add(Restrictions.eq("authors", author));
      }
    }

    // match all states
    if (states != null) {
      for (int state : states) {
        articleCriteria = articleCriteria.add(Restrictions.eq("state", state));
      }
    }

    // order by date
    if (ascending) {
      articleCriteria = articleCriteria.addOrder(Order.asc("date"));
    } else {
      articleCriteria = articleCriteria.addOrder(Order.desc("date"));
    }

    // get a list of Articles that meet the specified Criteria and Restrictions
    List<Article> articleList = articleCriteria.list();

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    for (Iterator it = articleList.iterator(); it.hasNext(); ) {
      Article article = (Article) it.next();
      try {
        getPEP().checkAccess(ArticlePEP.READ_META_DATA, article.getId());
      } catch (SecurityException se) {
        it.remove();
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + article.getId()
                    + " from Article list due to PEP SecurityException", se);
      }
    }

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

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check parms
    if (uri == null) throw new IllegalArgumentException("URI == null");
    URI realURI = URI.create(uri);

    // filter access by id with PEP
    // logged in user is automatically resolved by the ServletActionContextAttribute
    try {
      getPEP().checkAccess(ArticlePEP.READ_META_DATA, realURI);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " from ObjectInfo list due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    return TransactionHelper.doInTxE(session,
                            new TransactionHelper.ActionE<ObjectInfo, NoSuchObjectIdException>() {
      public ObjectInfo run(Transaction tx) throws NoSuchObjectIdException {
        ObjectInfo objectInfo = tx.getSession().get(ObjectInfo.class, uri);
        if (objectInfo == null)
          throw new NoSuchObjectIdException(uri);
        return objectInfo;
      }
    });
  }

  /**
   * Get an Article by URI.
   *
   * @param uri URI of Article to get.
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIDException NoSuchArticleIdException
   */
  public Article getArticle(final URI uri) throws NoSuchArticleIdException {

    // sanity check parms
    if (uri == null) throw new IllegalArgumentException("URI == null");

    // filter access by id with PEP
    try {
      getPEP().checkAccess(ArticlePEP.READ_META_DATA, uri);
    } catch (SecurityException se) {
      if (log.isDebugEnabled()) {
        log.debug("Filtering URI "
          + uri
          + " due to PEP SecurityException", se);
      }

      // it's still a SecurityException
      throw se;
    }

    return TransactionHelper.doInTxE(session,
                            new TransactionHelper.ActionE<Article, NoSuchArticleIdException>() {
      public Article run(Transaction tx) throws NoSuchArticleIdException {
        Article article = tx.getSession().get(Article.class, uri.toString());
        if (article == null)
          throw new NoSuchArticleIdException(uri.toString());
        return article;
      }
    });
  }

  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article)
      throws NoSuchArticleIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    getPEP().checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));

    return TransactionHelper.doInTxE(session,
                new TransactionHelper.ActionE<SecondaryObject[], NoSuchArticleIdException>() {
      public SecondaryObject[] run(Transaction tx) throws NoSuchArticleIdException {
        // get objects
        Article art = tx.getSession().get(Article.class, article);
        if (art == null)
          throw new NoSuchArticleIdException(article);

        /* sort the object list.
         *
         * TODO: the list should be stored using RdfSeq or RdfList instead of this next-object hack
         * so we can just use art.getParts()
         */
        List<ObjectInfo> sorted = new ArrayList<ObjectInfo>();
        for (ObjectInfo oi = art.getNextObject(); oi != null; oi = oi.getNextObject())
          sorted.add(oi);

        // convert to SecondaryObject's. TODO: can't we just return ObjectInfo?
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
      convertedObjectInfos[i] = convert (objectInfos[i]);
    }
    return convertedObjectInfos;
  }

  /**
   * Get the most commented Articles.
   * The actual # of Articles returned maybe < maxArticles
   * as PEP filtering is done on the results.
   *
   * @param maxArticles Maximum # of Articles to retrieve.
   * @returns Article[] of most commented Articles.
   */
  public Article[] getCommentedArticles(final int maxArticles) {

    // session housekeeping
    ensureInitGetsCalledWithUsersSessionAttributes();

    // sanity check args
    if (maxArticles < 0) {
      throw new IllegalArgumentException("Requesting a maximum # of commented articles < 0: " +  maxArticles);
    }
    if (maxArticles == 0) {
      return new Article[0];
    }

    final String oqlQuery =
      "select a, count(n) c from Article a, Annotation n where n.annotates = a order by c desc limit " + maxArticles;

    return TransactionHelper.doInTx(session, new TransactionHelper.Action<Article[]>() {
      public Article[] run(Transaction tx) {
        Results commentedArticles = commentedArticles = tx.getSession().doQuery(oqlQuery);

        // check access control on all Article results
        // logged in user is automatically resolved by the ServletActionContextAttribute
        ArrayList<Article> returnArticles = new ArrayList();

        commentedArticles.beforeFirst();
        while (commentedArticles.next()) {
          Article commentedArticle = (Article) commentedArticles.get("a");
          try {
            getPEP().checkAccess(ArticlePEP.READ_META_DATA, commentedArticle.getId());
            returnArticles.add(commentedArticle);
          } catch (SecurityException se) {
            if (log.isDebugEnabled())
              log.debug("Filtering URI "
                + commentedArticle.getId()
                + " from commented Article list due to PEP SecurityException", se);
          }
        }

        return returnArticles.toArray(new Article[returnArticles.size()]);
      }
    });
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
    ensureInitGetsCalledWithUsersSessionAttributes();

    getPEP().checkAccess(ArticlePEP.SET_REPRESENTATION, URI.create(objId));

    try {
      TransactionHelper.doInTxE(session, new TransactionHelper.ActionE<Void, Exception>() {
        public Void run(Transaction tx) throws NoSuchObjectIdException, RemoteException {
          // get the object info
          ObjectInfo obj = tx.getSession().get(ObjectInfo.class, objId);
          if (obj == null)
            throw new NoSuchObjectIdException(objId);

          String ct = content.getContentType();
          if (ct == null)
            ct = "application/octet-stream";

          // update fedora
          long len = setDatastream(obj.getPid(), rep, ct, content);

          // update the rdf
          obj.getRepresentations().add(rep);

          obj.getData().put("topaz:" + rep + "-contentType", toList(ct));
          obj.getData().put("topaz:" + rep + "-objectSize", toList(Long.toString(len)));

          tx.getSession().saveOrUpdate(obj);

          // done
          return null;
        }
      });
    } catch (Exception e) {
      if (e instanceof RuntimeException)
        throw (RuntimeException) e;
      if (e instanceof NoSuchObjectIdException)
        throw (NoSuchObjectIdException) e;
      throw (RemoteException) e;
    }
  }

  private static <T> List<T> toList(T item) {
    List<T> res = new ArrayList<T>();
    res.add(item);
    return res;
  }

  private long setDatastream(String pid, String rep, String ct, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    ctx.activate();
    try {
      FedoraAPIM apim = ctx.getFedoraAPIM();

      if (content != null) {
        CountingInputStream cis = new CountingInputStream(content.getInputStream());
        String reLoc = ctx.getFedoraUploader().upload(cis);
        try {
          apim.modifyDatastreamByReference(pid, rep, null, null, false, ct, null, reLoc, "A",
                                           "Updated datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist yet - " +
                      "creating it", re);

          apim.addDatastream(pid, rep, new String[0], "Represention", false, ct, null, reLoc, "M",
                             "A", "New representation");
        }

        return cis.getByteCount();
      } else {
        try {
          apim.purgeDatastream(pid, rep, null, "Purged datastream", false);
        } catch (RemoteException re) {
          if (!isNoSuchDatastream(re))
            throw re;

          if (log.isDebugEnabled())
            log.debug("representation '" + rep + "' for '" + pid + "' doesn't exist", re);
        }

        return 0;
      }
    } catch (RemoteException re) {
      if (isNoSuchObject(re))
        throw new NoSuchObjectIdException(pid, "object '" + pid + "' doesn't exist in fedora", re);
      else
        throw re;
    } catch (IOException ioe) {
      throw new RemoteException("Error uploading representation", ioe);
    } finally {
      ctx.passivate();
    }
  }

  private static boolean isNoSuchObject(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null &&
           msg.startsWith("fedora.server.errors.ObjectNotInLowlevelStorageException:");
  }

  private static boolean isNoSuchDatastream(RemoteException re) {
    // Ugh! What a hack...
    String msg = re.getMessage();
    return msg != null && msg.equals("java.lang.Exception: Uncaught exception from Fedora Server");
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
   * Gets the otm session.
   *
   * @return Returns the otm session.
   */
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

  // TODO: mv feeds to webapp?
  // inspired by ArticleFeed.buildXml(Collection articles)
  private static String buildArticleFeedXml(Collection articles) {

    final String XML_RESPONSE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<articles>\n${articles}</articles>\n";
    final String XML_ARTICLE_TAG =
      "  <article>\n" +
      "    <uri>${uri}</uri>\n" +
      "    <title>${title}</title>\n" +
      "    <description>${description}</description>\n" +
      "    <date>${date}</date>\n" +
      "    ${authors}\n" +
      "    ${categories}\n" +
      "    ${subjects}\n" +
      "  </article>\n";

    String articlesXml = "";
    for (Iterator articleIt = articles.iterator(); articleIt.hasNext(); ) {
      ArticleFeedData article = (ArticleFeedData)articleIt.next();

      StringBuffer authorsSb = new StringBuffer();
      if (article.authors != null && article.authors.size() > 0) {
        for (Iterator authorsIt = article.authors.iterator(); authorsIt.hasNext(); ) {
          authorsSb.append("      <author>");
          authorsSb.append(authorsIt.next());
          authorsSb.append("</author>\n");
        }
        authorsSb.insert(0, "<authors>\n");
        authorsSb.append("    </authors>");
      }

      StringBuffer categoriesSb = new StringBuffer();
      if (article.categories != null && article.categories.size() > 0) {
        for (Iterator categoriesIt = article.categories.iterator(); categoriesIt.hasNext(); ) {
          categoriesSb.append("      <category>");
          categoriesSb.append(categoriesIt.next());
          categoriesSb.append("</category>\n");
        }
        categoriesSb.insert(0, "<categories>\n");
        categoriesSb.append("    </categories>");
      }

      StringBuffer subjectsSb = new StringBuffer();
      if (article.subjects != null && article.subjects.size() > 0) {
        for (Iterator subjectsIt = article.subjects.iterator(); subjectsIt.hasNext(); ) {
          subjectsSb.append("      <subject>");
          subjectsSb.append(subjectsIt.next());
          subjectsSb.append("</subject>\n");
        }
        subjectsSb.insert(0, "<subjects>\n");
        subjectsSb.append("    </subjects>");
      }

      Map values = new HashMap();
      /* internationalize () */
      values.put("uri", article.uri);
      values.put("title", nullToEmpty(article.title));
      values.put("description", nullToEmpty(article.description));
      values.put("date", ArticleFeed.formatDate(article.date));
      values.put("authors", authorsSb.toString());
      values.put("subjects", subjectsSb.toString());
      values.put("categories", categoriesSb.toString());
      articlesXml += ItqlHelper.bindValues(XML_ARTICLE_TAG, values);
    }

    Map values = new HashMap();
    values.put("articles", articlesXml);
    return ItqlHelper.bindValues(XML_RESPONSE, values);
  }

  private static String nullToEmpty(String str) {
    return (str != null) ? str : "";
  }

  // methods & functionality that were "pulled up" from org.topazproject.ws.article.impl.ArticleImpl

  private static FgsOperations[] getFgsOperations() throws ServiceException {

    FgsOperations ops[] = new FgsOperations[FGS_URLS.size()];

    for (int i = 0; i < ops.length; i++) {
      String url = FGS_URLS.get(i).toString();
      try {
        ops[i] = new FgsOperationsServiceLocator().getOperations(new URL(url));
      } catch (MalformedURLException mue) {
        throw new ServiceException("Invalid fedoragsearch URL '" + url + "'", mue);
      }
      if (ops[i] == null)
        throw new ServiceException(
          "Unable to create fedoragsearch service at '" + url + "'");
    }

    return ops;
  }
}
