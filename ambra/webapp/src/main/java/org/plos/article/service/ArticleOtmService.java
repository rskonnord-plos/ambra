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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.ParseException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.DateUtils;

import org.plos.models.Article;
import org.plos.models.ObjectInfo;
import org.plos.models.Representation;
import org.plos.permission.service.PermissionsService;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

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
  private static final Log log = LogFactory.getLog(ArticleOtmService.class);

  private String         smallImageRep;
  private String         largeImageRep;
  private String         mediumImageRep;

  private ArticlePEP         pep;
  private Session            session;
  private PermissionsService permissionsService;

  public ArticleOtmService() throws IOException {
    pep = new ArticlePEP();
  }

  /**
   * Ingest an article.
   *
   * @param article the article
   * @param force   if true don't check for duplicate and instead always (re-)ingest
   * @return the ingested article
   * @throws IngestException IngestException
   * @throws DuplicateIdException DuplicateIdException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public Article ingest(DataSource article, boolean force)
          throws DuplicateArticleIdException, IngestException, RemoteException, ServiceException,
                 IOException {
    pep.checkAccess(ArticlePEP.INGEST_ARTICLE, ArticlePEP.ANY_RESOURCE);

    // ingest article
    Ingester ingester = new Ingester(session, permissionsService);
    Article art = ingester.ingest(createZip(article), force);

    return art;
  }

  private static Zip createZip(DataSource article) throws IOException {
    if (article instanceof FileDataSource)
      return new Zip.FileZip(((FileDataSource) article).getFile().toString());
    return new Zip.DataSourceZip(article);
  }

  /**
   * Get the contents of the given object.
   *
   * @param obj the uri of the object
   * @param rep the representation to get
   * @return the contents
   * @throws NoSuchObjectIdException if the object cannot be found
   */
  @Transactional(readOnly = true)
  public DataSource getContent(String obj, String rep) throws NoSuchObjectIdException {
    pep.checkAccess(ArticlePEP.GET_OBJECT_CONTENT, URI.create(obj));

    ObjectInfo oi = session.get(ObjectInfo.class, obj);
    if (oi == null)
      throw new NoSuchObjectIdException(obj);

    Representation r = oi.getRepresentation(rep);
    if (r == null)
      throw new NoSuchObjectIdException(obj, "No such Representation: " + rep);

    if (r.getBody() == null)
      throw new NoSuchObjectIdException(obj, "Missing body for Representation: " + rep);

    return new ByteArrayDataSource(r);
  }

  /**
   * Delete an article.
   *
   * @param article the uri of the article
   * @throws RemoteException RemoteException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void delete(String article)
          throws NoSuchArticleIdException, RemoteException, ServiceException {
    pep.checkAccess(ArticlePEP.DELETE_ARTICLE, URI.create(article));

    Article a = session.get(Article.class, article);
    if (a == null)
      throw new NoSuchArticleIdException(article);

    session.delete(a);
    SearchUtil.delete(article);

    // TODO: find a better way to know what needs to be deleted, rather than "hardcoding" it here
    permissionsService.cancelPropagatePermissions(article,
                                permissionsService.listPermissionPropagations(article, false));
  }

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param state state
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setState(final String article, final int state) throws NoSuchArticleIdException {
    pep.checkAccess(ArticlePEP.SET_ARTICLE_STATE, URI.create(article));

    Article a = session.get(Article.class, article);
    if (a == null)
      throw new NoSuchArticleIdException(article);

    a.setState(state);
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
  @Transactional(readOnly = true)
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
  @Transactional(readOnly = true)
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
  @Transactional(readOnly = true)
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

    return articleList;
  }

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
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
   * @param uri URI of Article to get.
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIDException NoSuchArticleIdException
   */
  @Transactional(readOnly = true)
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

    return article;
  }


  /**
   * Return the list of secondary objects
   * @param article uri
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true)
  public SecondaryObject[] listSecondaryObjects(final String article)
        throws NoSuchArticleIdException {
    pep.checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));

    // make use of object-cache
    Article art = session.get(Article.class, article);
    if (art == null)
      throw new NoSuchArticleIdException(article);
    // convert to SecondaryObject's. TODO: re-factor to return ObjectInfo
    return convert(art.getParts(), null);
  }

  /**
   * Return a list of Figures and Tables in DOI order.
   *
   * @param article DOI.
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  @Transactional(readOnly = true)
  public SecondaryObject[] listFiguresTables(final String article) throws NoSuchArticleIdException {
    pep.checkAccess(ArticlePEP.LIST_SEC_OBJECTS, URI.create(article));

    // make use of object-cache
    Article art = session.get(Article.class, article);
    if (art == null)
      throw new NoSuchArticleIdException(article);
    // convert to SecondaryObject's. TODO: re-factor to return ObjectInfo
    return convert(art.getParts(), new String[] {"fig", "table-wrap"});
  }

  private SecondaryObject[] convert(List<ObjectInfo> objectInfos, String[] contextFilter) {
    if (objectInfos == null) {
      return null;
    }
    if (contextFilter != null) {
      List<ObjectInfo> filtered = new ArrayList<ObjectInfo>(objectInfos.size());
      for(ObjectInfo o : objectInfos) {
        for (String context : contextFilter) {
          if (context.equals(o.getContextElement())) {
              filtered.add(o);
              break;
          }
        }
      }
      objectInfos = filtered;
    }
    SecondaryObject[] convertedObjectInfos = new SecondaryObject[objectInfos.size()];
    for (int i = 0; i < objectInfos.size(); i++) {
      convertedObjectInfos[i] = convert(objectInfos.get(i));
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
  @Transactional(readOnly = true)
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
   * @param permissionsService the permissions service to use
   */
  @Required
  public void setPermissionsService(PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
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

  private static class ByteArrayDataSource implements DataSource {
    private final Representation rep;

    public ByteArrayDataSource(Representation rep) {
      this.rep = rep;
    }

    public String getName() {
      return rep.getObject().getId() + "#" + rep.getName();
    }

    public String getContentType() {
      String ct = rep.getContentType();
      return (ct != null) ? ct : "application/octet-stream";
    }

    public InputStream getInputStream() {
      return new ByteArrayInputStream(rep.getBody());
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("writing not supported");
    }
  }
}
