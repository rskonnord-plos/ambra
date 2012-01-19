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
package org.topazproject.ambra.article.service;

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
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.time.DateUtils;


import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.permission.service.PermissionsService;
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
   * @param categories is list of categories to search for articles within (all categories if null
   *                   or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or
   *                   empty)
   * @param states     the list of article states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date).
   * @param maxResults the maximum number of results to return, or 0 for no limit
   * @return the (possibly empty) list of article ids.
   */
  @Transactional(readOnly = true)
  public List<String> getArticleIds(String startDate, String endDate, String[] categories,
                                    String[] authors, int[] states, boolean ascending,
                                    int maxResults) {
    StringBuilder qry = new StringBuilder();
    qry.append("select art.id id, d from Article art where ");
    qry.append("d := art.dublinCore.date and ");

    // apply date constraints
    if (startDate != null)
      qry.append("(gt(d, :sd) or d = :sd) and ");
    if (endDate != null)
      qry.append("(lt(d, :ed) or d = :ed) and ");

    // match categories
    if (categories != null && categories.length > 0) {
      for (String category : categories)
        qry.append("art.categories.mainCategory = '" + category + "' and ");
    }

    // match all authors
    if (authors != null && authors.length > 0) {
      for (String author : authors)
        qry.append("art.dublinCore.creators = '" + author + "' and ");
    }

    // match all states
    if (states != null && states.length > 0) {
      qry.append("(");
      for (int idx = 0; idx < states.length; idx++)
        qry.append("art.state = :st").append(idx).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ");
    }

    // trim off trailing 'and'
    if (qry.indexOf(" and ", qry.length() - 5) > 0)
      qry.setLength(qry.length() - 4);

    // add ordering and limit
    qry.append("order by d ").append(ascending ? "asc" : "desc").append(", id asc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());
    if (startDate != null)
      q.setParameter("sd", startDate);
    if (endDate != null)
      q.setParameter("ed", endDate);
    for (int idx = 0; states != null && idx < states.length; idx++)
      q.setParameter("st" + idx, states[idx]);

    // run the query
    List<URI> ids = new ArrayList<URI>();

    Results r = q.execute();
    while (r.next())
      ids.add(r.getURI(0));

    // apply access-controls
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
   * @param ascending  controls the sort order (by date).
   * @param maxResults the maximum number of results to return, or 0 for no limit
   * @return the (possibly empty) list of articles.
   */
  @Transactional(readOnly = true)
  public List<Article> getArticles(String startDate, String endDate, String[] categories,
                                   String[] authors, int[] states, boolean ascending,
                                   int maxResults) {
    List<String> articleIds =
        getArticleIds(startDate, endDate, categories, authors, states, ascending, maxResults);

    List<Article> articleList = new ArrayList<Article>();
    for (String id : articleIds)
      articleList.add(session.get(Article.class, id));

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
