/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.plos.filestore.FSIDMapper;
import org.plos.filestore.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.rating.action.ArticleRatingSummary;
import org.topazproject.ambra.service.HibernateServiceImpl;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provide Article "services"
 */
public class ArticlePersistenceServiceImpl extends HibernateServiceImpl implements ArticlePersistenceService {
  private static final Logger log = LoggerFactory.getLogger(ArticlePersistenceServiceImpl.class);

  private String smallImageRep;
  private String largeImageRep;
  private String mediumImageRep;

  private PermissionsService permissionsService;
  private FileStoreService fileStoreService;

  public ArticlePersistenceServiceImpl() {
  }

  /**
   * Get the contents of the given object.
   *
   * @param uri     the uri of the object
   * @param repType the representation to get
   * @param authId the authorization ID of the current user
   *
   * @return the contents
   * @throws NoSuchObjectIdException if the object cannot be found
   */
  @Transactional(readOnly = true)
  public DataSource getContent(final String uri, final String repType, final String authId)
    throws NoSuchObjectIdException, NoSuchArticleIdException {

    // Using the uri (doi) and representation (the file type)
    // construct the fsid to get the Inputstream.

    Representation rep = null;

    // PDF's and XML article representaions are linked to the article
    // everything else is through ObjectInfo.
    // WTO: we should probably have a getRepresentation(uri) method to do this!
    if (repType.equalsIgnoreCase("PDF") || repType.equalsIgnoreCase("XML")) {
      Article article = getArticle(URI.create(uri), authId);
      rep = article.getRepresentation(repType);
    } else {
      ObjectInfo objectInfo = getObjectInfo(uri, authId);
      rep = objectInfo.getRepresentation(repType);
    }

    String fsid = FSIDMapper.doiTofsid(uri, repType);
    if (fsid == null)
      throw new NoSuchObjectIdException(uri);

    return new ByteArrayDataSource(fileStoreService, fsid, rep);
  }

  /**
   * Delete an article.
   *
   * @param article the uri of the article
   * @param authId the authorization ID of the current user
   *
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(rollbackFor = {Throwable.class})
  public void delete(final String article, String authId) throws NoSuchArticleIdException {
    permissionsService.checkRole(PermissionsService.ADMIN_ROLE, authId);

    hibernateTemplate.execute(new HibernateCallback() {
      /**
       * Used to delete annotations recursively
       * @param session
       * @param annotationUri
       */
      @SuppressWarnings("unchecked")
      private void deleteAnnotation(Session session, String annotationUri) {
        List<String> annotationUris = session.createSQLQuery("select annotationUri from Annotation" +
          " where annotates = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .list();

        for(String childAnnotationUri : annotationUris) {
          deleteAnnotation(session, childAnnotationUri);
        }

        session.createSQLQuery("delete r,rb,rt from Reply r" +
          " join ReplyBlob rb on r.body = rb.blobUri" +
          " left join RepliesJoinTable rt on rt.originalThreadUri = r.replyUri" +
          " where r.root = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .executeUpdate();

        session.createSQLQuery("delete ae,ac from Annotation a" +
          " join AnnotationEditorCitationJoinTable ae on a.bibliographicCitationUri = ae.citationUri" +
          " join ArticleContributor ac on ac.contributorUri = ae.contributorUri" +
          " where a.annotationUri = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .executeUpdate();

        session.createSQLQuery("delete ae,ac from Annotation a" +
          " join AnnotationAuthorCitationJoinTable ae on a.bibliographicCitationUri = ae.citationUri" +
          " join ArticleContributor ac on ac.contributorUri = ae.contributorUri" +
          " where a.annotationUri = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .executeUpdate();

        //Get the related citation before deleting
        Object citationUri = session.createSQLQuery("select bibliographicCitationUri from Annotation" +
          " where annotationUri = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .uniqueResult();

        //Delete the annotation first due to foreign key constraints
        session.createSQLQuery("delete a,ab from Annotation a" +
          " join AnnotationBlob ab on ab.blobUri = a.body " +
          " where a.annotationUri = :annotationUri")
          .setParameter("annotationUri", annotationUri)
          .executeUpdate();

        session.createSQLQuery("delete c from Citation c" +
          " where c.citationUri = :citationUri")
          .setParameter("citationUri", citationUri)
          .executeUpdate();
      }

      @SuppressWarnings("unchecked")
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        //Yes, this bit of code is a bit large.  Hibernate should support cascading deletes
        //But for various reasons, that doesn't work right now.  Until that time that it does
        //I do it here in a manual way.  Of note, I'm not sure that all these tables are even in use

        List<String> annotationUris = session.createSQLQuery("select annotationUri from Annotation" +
          " where annotates = :articleUri")
          .setParameter("articleUri", article)
          .list();

        for(String annotation : annotationUris) {
          deleteAnnotation(session, annotation);
        }

        session.createSQLQuery("delete from Syndication " +
          " where articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete t,a from TrackbackContent t" +
          " join Annotation a on t.trackBackContentUri = a.body" +
          " where a.annotates = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete r,a from RatingContent r" +
          " join Annotation a on r.ratingContentUri = a.body" +
          " where a.annotates = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete r,a from RatingSummaryContent r" +
          " join Annotation a on r.ratingSummaryContentUri = a.body" +
          " where a.annotates = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete d from DublinCoreSummary d" +
          " where d.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete d from DublinCoreSubjects d" +
          " where d.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete r,cp from Citation c" +
          " join ReferencedAuthorCitationJoinTable r on c.citationUri = r.citationUri" +
          " join CitedPerson cp on cp.citedPersonUri = r.citedPersonUri" +
          " where c.doi = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        List citationUris = session.createSQLQuery("select citationUri from DublinCoreReferences " +
          " where articleUri = :articleUri")
          .setParameter("articleUri", article)
          .list();

        for(Object citationUri : citationUris) {
          session.createSQLQuery("delete from DublinCoreReferences" +
            " where citationUri = :citationUri")
            .setParameter("citationUri", citationUri)
            .executeUpdate();

          session.createSQLQuery("delete cp,racj from ReferencedAuthorCitationJoinTable racj" +
            " join CitedPerson cp on cp.citedPersonUri = racj.citedPersonUri" +
            " where racj.citationUri = :citationUri")
            .setParameter("citationUri", citationUri)
            .executeUpdate();

          session.createSQLQuery("delete cp,recj from ReferencedEditorCitationJoinTable recj" +
            " join CitedPerson cp on cp.citedPersonUri = recj.citedPersonUri" +
            " where recj.citationUri = :citationUri")
            .setParameter("citationUri", citationUri)
            .executeUpdate();

          session.createSQLQuery("delete from Citation" +
            " where citationUri = :citationUri")
            .setParameter("citationUri", citationUri)
            .executeUpdate();
        }

        session.createSQLQuery("delete d from DublinCoreLicenses d" +
          " where d.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete d from ArticlePartsJoinTable ap" +
          " join DublinCoreCreators d on d.articleUri = ap.objectInfoUri" +
          " where ap.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete ca from Citation c" +
          " join CollaborativeAuthors ca on ca.citationUri = c.citationUri" +
          " where c.doi = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete ca,up from Citation c" +
          " join CitationAuthors ca on ca.citationUri = c.citationUri" +
          " join UserProfile up on up.userProfileUri = ca.authorUri" +
          " where c.doi = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete ce,up from Citation c" +
          " join CitationEditors ce on ce.citationUri = c.citationUri" +
          " join UserProfile up on up.userProfileUri = ce.editorUri" +
          " where c.doi = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete c from Citation c" +
          " where c.doi = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        //Do an update to avoid a foreign constraint error there should only be one bibliographicCitationUri citation
        //per article
        Object value = session.createSQLQuery("select bibliographicCitationUri from DublinCore" +
          " where articleUri = :articleUri")
          .setParameter("articleUri", article)
          .uniqueResult();

        session.createSQLQuery("update DublinCore set bibliographicCitationUri = null" +
          " where articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete c from Citation c " +
          " where c.citationUri = :citationUri")
          .setParameter("citationUri", value)
          .executeUpdate();

        session.createSQLQuery("delete r,aj from Representation r" +
          " join ArticleRepresentationsJoinTable aj on aj.representationsUri = r.representationUri" +
          " where aj.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        List objectUris = session.createSQLQuery("select apj.objectInfoUri from ArticlePartsJoinTable apj" +
          " where apj.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .list();

        for(Object objectUri : objectUris) {
          session.createSQLQuery("delete oir,r from ObjectInfoRepresentationsJoinTable oir" +
            " join Representation r on r.representationUri = oir.representationsUri" +
            " where oir.articleUri = :objectInfoUri")
          .setParameter("objectInfoUri", objectUri)
          .executeUpdate();

          session.createSQLQuery("delete from ArticlePartsJoinTable where objectInfoUri = :objectInfoUri")
          .setParameter("objectInfoUri", objectUri)
          .executeUpdate();

          session.createSQLQuery("delete from ObjectInfo where objectInfoUri = :objectInfoUri")
          .setParameter("objectInfoUri", objectUri)
          .executeUpdate();

          session.createSQLQuery("delete from DublinCoreCreators where articleUri = :articleUri")
          .setParameter("articleUri", objectUri)
          .executeUpdate();

          session.createSQLQuery("delete from DublinCore where articleUri = :articleUri")
          .setParameter("articleUri", objectUri)
          .executeUpdate();
        }

        session.createSQLQuery("delete aj, ar from ArticleRelatedJoinTable aj join " +
          " ArticleRelated ar on aj.articleRelatedUri = ar.articleRelatedUri" +
          " where aj.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete a from ArticleTypes a where a.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        List<String> articleCategoryUris = session.createSQLQuery("select articleCategoryUri from ArticleCategoryJoinTable" +
          " where articleUri = :articleUri")
          .setParameter("articleUri", article)
          .list();

        for(String categoryUri : articleCategoryUris) {
          session.createSQLQuery("delete from ArticleCategoryJoinTable where articleCategoryUri = :categoryUri")
            .setParameter("categoryUri", categoryUri)
            .executeUpdate();

          session.createSQLQuery("delete from Category where categoryUri = :categoryUri")
            .setParameter("categoryUri", categoryUri)
            .executeUpdate();
        }

        session.createSQLQuery("delete c,ac from Category c" +
          " join ArticleCategoryJoinTable ac on c.categoryUri = ac.articleCategoryUri" +
          " where ac.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete ac, aj from ArticleEditorJoinTable aj" +
          " join ArticleContributor ac on aj.contributorUri = ac.contributorUri" +
          " where aj.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete ac, aj from ArticleAuthorJoinTable aj" +
          " join ArticleContributor ac on aj.contributorUri = ac.contributorUri" +
          " where aj.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete i from IssueArticleList i where i.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete a from AggregationSimpleCollection a where a.uri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete a from Article a where a.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete from DublinCoreCreators where articleUri = :articleUri")
        .setParameter("articleUri", article)
        .executeUpdate();

        session.createSQLQuery("delete d from DublinCore d where d.articleUri = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();

        session.createSQLQuery("delete d,o from ObjectInfo o" +
          " join DublinCore d on o.objectInfoUri = d.articleUri " +
          " where o.isPartOf = :articleUri")
          .setParameter("articleUri", article)
          .executeUpdate();


        return null;
      }
    });
  }

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param authId the authorization ID of the current user
   * @param state   state
   *
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(rollbackFor = {Throwable.class})
  public void setState(final String article, String authId, final int state) throws NoSuchArticleIdException {
    permissionsService.checkRole(PermissionsService.ADMIN_ROLE, authId);

    Article a = (Article) hibernateTemplate.get(Article.class, URI.create(article));
    if (a == null)
      throw new NoSuchArticleIdException(article);

    a.setState(state);

    hibernateTemplate.update(a);
  }

  /**
   * Get all of the articles satisfying the given criteria.
   * <p/>
   * TODO: Replace the String representations of "startDate" and "endDate" with Date objects.
   *
   * @param startDate        is the date to start searching from. If null, start from begining of time Can be iso8601
   *                         formatted or string representation of Date object
   * @param endDate          is the date to search until. If null, search until present date
   * @param categories       return articles that have ALL of these as a main category (no filter if null or empty)
   * @param authors          return articles that have ALL of these as author (no filter if null or empty)
   * @param eIssn            eIssn of the Journal that this Article belongs to (no filter if null or empty)
   * @param states           list of article states to search for (all states if null or empty)
   * @param orderField       field on which to sort the results (defaults to Article DOI) Should be one of the
   *                         ORDER_BY_FIELD_ constants from the Interface. Assumed to be the name of a column in the
   *                         Article table
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @param maxResults       maximum number of results to return, or 0 for no limit
   * @return all of the articles satisfying the given criteria (possibly null) Key is the Article DOI.  Value is the
   *         Article itself.
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<Article> getArticles(final String startDate, final String endDate, final String[] categories,
                                   final String[] authors, final String eIssn, final int[] states,
                                   final String orderField, final boolean isOrderAscending,
                                   final int maxResults) {

    return (List<Article>) this.hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria query = session.createCriteria(Article.class)
            .createAlias("dublinCore","dublinCore");

        if (states != null && states.length > 0) {
          List<Integer> statesList = new ArrayList<Integer>(states.length);
          for (int state : states) {
            statesList.add(state);
          }
          query.add(Restrictions.in("state", statesList));
        }
        if (eIssn != null) {
          query.add(Restrictions.eq("eIssn", eIssn));
        }
        if (orderField != null) {
          if (isOrderAscending) {
            query.addOrder(Order.asc(orderField));
          } else {
            query.addOrder(Order.desc(orderField));
          }
        }

        try {
          if (startDate != null) {
            query.add(Restrictions.ge("dublinCore.created", parseDateParam(startDate).getTime()));
          }
          if (endDate != null) {
            query.add(Restrictions.le("dublinCore.created", parseDateParam(endDate).getTime()));
          }
        } catch (ParseException e) {
          throw new HibernateException("Couldn't parse date param", e);
        }

        if (maxResults > 0) {
          query.setMaxResults(maxResults);
        }
        //Filter the results post-db access since the kind of restriction we want isn't easily representable in sql -
        //we want only articles such that the list of full names of the author list contains ALL the names in the given
        //author array.  This is NOT an 'in' restriction or an 'equals' restriction
        List<Article> queryResults = query.list();
        List<Article> filteredResults = new ArrayList<Article>(queryResults.size());
        for (Article article : queryResults) {
          if (matchesAuthorFilter(authors, article.getAuthors())
              && matchesCategoriesFilter(categories, article.getCategories())) {
            filteredResults.add(article);
          }
        }

        return filteredResults;
      }
    });
  }

  /**
   * Get the ids of all articles satisfying the given criteria.
   * <p/>
   * This method calls <code>getArticles(...)</code> then parses the Article IDs from that List.
   * <p/>
   * TODO: Replace the String representations of "startDate" and "endDate" with Date objects.
   *
   * @param startDate        is the date to start searching from. If null, start from begining of time Can be iso8601
   *                         formatted or string representation of Date object
   * @param endDate          is the date to search until. If null, search until present date
   * @param categories       return articles that have ALL of these as a main category (no filter if null or empty)
   * @param authors          return articles that have ALL of these as author (no filter if null or empty)
   * @param eIssn            eIssn of the Journal that this Article belongs to (no filter if null or empty)
   * @param states           list of article states to search for (all states if null or empty)
   * @param orderField       field on which to sort the results (no sort if null or empty) Should be one of the
   *                         ORDER_BY_FIELD_ constants from the Interface. Assumed to be the name of a column in the
   *                         Article table
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @param maxResults       maximum number of results to return, or 0 for no limit
   * @return the (possibly empty) list of article ids.
   * @throws java.text.ParseException if any of the dates could not be parsed
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<String> getArticleIds(final String startDate, final String endDate, final String[] categories,
                                    final String[] authors, final String eIssn, final int[] states,
                                    final String orderField, final boolean isOrderAscending,
                                    final int maxResults) throws ParseException {
    List<Article> articles = getArticles(startDate, endDate, categories, authors, eIssn,
        states, orderField, isOrderAscending, maxResults);
    List<String> articleIds = new ArrayList<String>(articles.size());

    for (Article article : articles) {
      articleIds.add(article.getId().toString());
    }

    return articleIds;
  }

  /**
   * Helper method for getArticleIds(), since the type of restriction needed doesn't seem to be do-able in HQL or with
   * criteria - we want articles where each of the Strings in the category array is the main category of one of the
   * article's categories
   *
   * @param filterCategories - the array of main categories that was passed in to getArticleIds() to use to filter
   *                         results
   * @param categorySet      - the 'categories' property of an article
   * @return - true if the article passes the category filter, false otherwise
   */
  private boolean matchesCategoriesFilter(String[] filterCategories, Set<Category> categorySet) {
    if (filterCategories != null && filterCategories.length > 0) {

      if (filterCategories.length > categorySet.size()) {
        return false; //can't possibly contain all the categories if there's more of them than you have
      }

      //Just get the main category
      Set<String> mainCategories = new HashSet<String>(categorySet.size());
      for (Category category : categorySet) {
        mainCategories.add(category.getMainCategory());
      }
      //check that all the filter categories are in their
      for (String cat : filterCategories) {
        if (!mainCategories.contains(cat)) {
          return false;
        }
      }

    }
    return true;
  }

  /**
   * Help0er method for getArticleIds() since the restriction we want doesn't appear
   *
   * @param authorFilter
   * @param authors
   * @return
   */
  private boolean matchesAuthorFilter(String[] authorFilter, List<ArticleContributor> authors) {
    if (authorFilter != null && authorFilter.length > 0) {
      List<String> authorNames = new ArrayList<String>(authors.size());
      for (ArticleContributor author : authors) {
        authorNames.add(author.getFullName());
      }
      for (String author : authorFilter) {
        if (!authorNames.contains(author)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Convert a date passed in as a string to a Date object. Support both string representations of the Date object and
   * iso8601 formatted dates.
   *
   * @param date the string to convert to a Date object
   * @return a date object (or null if date is null)
   * @throws java.text.ParseException if unable to parse date
   */
  public static Calendar parseDateParam(String date) throws ParseException {
    if (date == null)
      return null;
    try {
      Date parsedDate = new Date(date);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(parsedDate);
      return calendar;
    } catch (IllegalArgumentException iae) {
      if (log.isDebugEnabled())
        log.debug("failed to parse date '" + date + "' use Date - trying iso8601 format", iae);
      return parseDate(date);
    }
  }

  /**
   * Parse an xsd date into a java Date object.
   *
   * @param iso8601date is the date string to parse.
   * @return a java Date object.
   * @throws java.text.ParseException if there is a problem parsing the string
   */
  private static Calendar parseDate(String iso8601date) {
    // Obvious formats:
    SimpleDateFormat df = new SimpleDateFormat();
    for (String format : new String[]{
        "yyyy-MM-dd", "y-M-d", "y-M-d'T'H:m:s", "y-M-d'T'H:m:s.S",
        "y-M-d'T'H:m:s.Sz", "y-M-d'T'H:m:sz"}) {
      df.applyPattern(format);
      try {
        Date date = df.parse(iso8601date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
      } catch (ParseException e) {
        //try other formats
      }
    }
    return null;
  }

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   * @param authId the authorization ID of the current user
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  @Transactional(readOnly = true)
  public ObjectInfo getObjectInfo(final String uri, final String authId) throws NoSuchObjectIdException {
    // sanity check parms
    if (uri == null)
      throw new IllegalArgumentException("URI == null");

    // logged in user is automatically resolved by the ServletActionContextAttribute
    ObjectInfo objectInfo = (ObjectInfo) hibernateTemplate.get(ObjectInfo.class, URI.create(uri));
    if (objectInfo == null)
      throw new NoSuchObjectIdException(uri);

    Article article = objectInfo.getIsPartOf();

    //If the article is in an ublished state, none of the related objects should be returned
    if (article.getState() == Article.STATE_UNPUBLISHED) {
      try {
        permissionsService.checkRole(PermissionsService.ADMIN_ROLE, authId);
      } catch(SecurityException se) {
        throw new NoSuchObjectIdException(uri.toString());
      }
    }

    //If the article is disabled don't the object ever
    if (article.getState() == Article.STATE_DISABLED) {
      throw new NoSuchObjectIdException(uri.toString());
    }

    return objectInfo;
  }

  /**
   * Get an Article by URI.
   *
   * @param uri URI of Article to get.
   * @param authId the authorization ID of the current user
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true, noRollbackFor = {SecurityException.class})
  public Article getArticle(final URI uri, final String authId) throws NoSuchArticleIdException {
    // sanity check parms
    if (uri == null)
      throw new IllegalArgumentException("URI == null");

    Article article = (Article) hibernateTemplate.get(Article.class, uri);

    if (article == null) {
      throw new NoSuchArticleIdException(uri.toString());
    }

    //If the article is unpublished, it should not be returned if the user is not an admin
    if (article.getState() == Article.STATE_UNPUBLISHED) {
      try {
        permissionsService.checkRole(PermissionsService.ADMIN_ROLE, authId);
      } catch(SecurityException se) {
        throw new NoSuchArticleIdException(uri.toString());
      }
    }

    //If the article is disabled, don't display it ever
    if (article.getState() == Article.STATE_DISABLED) {
      throw new NoSuchArticleIdException(uri.toString());
    }

    return article;
  }

  /**
   * Get articles based on a list of Article id's.
   *
   * @param articleIds list of article id's
   * @param authId the authorization ID of the current user
   * @return <code>List&lt;Article&gt;</code> of articles requested
   * @throws java.text.ParseException when article ids are invalid
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true)
  public List<Article> getArticles(List<String> articleIds, final String authId) throws ParseException, NoSuchArticleIdException
  {
    List<Article> articleList = new ArrayList<Article>();

    for (String id : articleIds)
      articleList.add(getArticle(URI.create(id), authId));

    return articleList;
  }

  /**
   * Return the list of secondary objects
   *
   * @param article uri
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  @Transactional(readOnly = true)
  public SecondaryObject[] listSecondaryObjects(final String article, final String authId)
      throws NoSuchArticleIdException {
    Article art = getArticle(URI.create(article), authId);
    return convert(art.getParts(), null);
  }

  /**
   * Return a list of Figures and Tables in DOI order.
   *
   * @param article DOI.
   * @param authId the authorization ID of the current user
   *
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  @Transactional(readOnly = true)
  public SecondaryObject[] listFiguresTables(final String article, final String authId) throws NoSuchArticleIdException {
    Article art = getArticle(URI.create(article), authId);
    // convert to SecondaryObject's.
    return convert(art.getParts(), new String[]{"fig", "table-wrap"});
  }

  private SecondaryObject[] convert(List<ObjectInfo> objectInfos, String[] contextFilter) {
    if (objectInfos == null) {
      return null;
    }
    if (contextFilter != null) {
      List<ObjectInfo> filtered = new ArrayList<ObjectInfo>(objectInfos.size());
      for (ObjectInfo o : objectInfos) {
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
   * Determines if the articleURI is of type researchArticle
   *
   * @param articleURI The URI of the article
   * @param authId the authorization ID of the current user
   * @return True if the article is a research article
   * @throws org.topazproject.ambra.ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isResearchArticle(final String articleURI, final String authId)
      throws ApplicationException, NoSuchArticleIdException {
    // resolve article type and supported properties
    Article artInfo = getArticle(URI.create(articleURI), authId);
    ArticleType articleType = ArticleType.getDefaultArticleType();

    for (URI artTypeUri : artInfo.getArticleType()) {
      if (ArticleType.getKnownArticleTypeForURI(artTypeUri) != null) {
        articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
        break;
      }
    }

    if (articleType == null) {
      throw new ApplicationException("Unable to resolve article type for: " + articleURI);
    }

    return ArticleType.isResearchArticle(articleType);
  }

  /**
   * Get rating summaries for the passed in articleURI
   *
   * @param articleURI articleURI to get rating summaries for
   * @return List<RatingSummary> of rating Summaries
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<RatingSummary> getRatingSummaries(final URI articleURI) {
    /* assume if valid RatingsPEP.GET_RATINGS, OK to GET_STATS
     * RatingSummary for this Article
     */
    return (List<RatingSummary>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        return session.createCriteria(RatingSummary.class)
            .add(Restrictions.eq("annotates", articleURI))
            .list();
      }
    });
  }

  /**
   * Get article rating summaries for the passed in articleURI
   *
   * @param articleURI articleURI to get rating summaries for
   * @return List<ArticleRatingSummary> of rating Summaries
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<ArticleRatingSummary> getArticleRatingSummaries(final URI articleURI) {
    return (List<ArticleRatingSummary>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List<ArticleRatingSummary> articleRatingSummaries = new ArrayList<ArticleRatingSummary>();

        List<Rating> articleRatings = session.createCriteria(Rating.class)
            .add(Restrictions.eq("annotates", articleURI))
            .addOrder(Order.desc("created"))
            .list();

        // create ArticleRatingSummary(s)
        for (Rating rating : articleRatings) {
          ArticleRatingSummary summary = new ArticleRatingSummary(articleURI.toString(), null);
          summary.setRating(rating);
          summary.setCreated(rating.getCreated());
          summary.setArticleURI(articleURI.toString());
          summary.setCreatorURI(rating.getCreator());

          // get public 'name' for user
          UserAccount ua = (UserAccount) session.get(UserAccount.class, URI.create(rating.getCreator()));
          if (ua != null) {
            summary.setCreatorName(ua.getProfile().getDisplayName());
          } else {
            summary.setCreatorName("Unknown");
            log.error("Unable to look up UserAccount for " + rating.getCreator() +
                " for Rating " + rating.getId());
          }
          articleRatingSummaries.add(summary);
        }

        return articleRatingSummaries;
      }
    });
  }

  /**
   * Get a List of all of the Journal/Volume/Issue combinations that contain the <code>articleURI</code> which was
   * passed in. Each primary List element contains a secondary List of six Strings which are, in order: <ul>
   * <li><strong>Element 0: </strong> Journal URI</li> <li><strong>Element 1: </strong> Journal key</li>
   * <li><strong>Element 2: </strong> Volume URI</li> <li><strong>Element 3: </strong> Volume name</li>
   * <li><strong>Element 4: </strong> Issue URI</li> <li><strong>Element 5: </strong> Issue name</li> </ul> A Journal
   * might have multiple Volumes, any of which might have multiple Issues that contain the <code>articleURI</code>. The
   * primary List will always contain one element for each Issue that contains the <code>articleURI</code>.
   *
   * @param articleURI Article URI that is contained in the Journal/Volume/Issue combinations which will be returned
   * @return All of the Journal/Volume/Issue combinations which contain the articleURI passed in
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<List<String>> getArticleIssues(final URI articleURI) {
    return (List<List<String>>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        List<Object[]> articleIssues = session.createSQLQuery(
            "select {j.*}, {v.*}, {i.*} " +
                "from AggregationSimpleCollection ial, VolumeIssueList vil, JournalVolumeList jvl, Issue i, Volume v, " +
                     "Journal j, DublinCore dc " +
                "where ial.uri = :articleURI " +
                "and vil.issueUri = ial.aggregationArticleUri " +
                "and jvl.volumeUri = vil.aggregationUri " +
                "and i.aggregationUri = ial.aggregationArticleUri " +
                "and v.aggregationUri = vil.aggregationUri " +
                "and j.aggregationUri = jvl.aggregationUri " +
                "and dc.articleUri = i.aggregationUri " +
                "order by dc.created desc ")
            .addEntity("j", Journal.class)
            .addEntity("v", Volume.class)
            .addEntity("i", Issue.class)
            .setString("articleURI", articleURI.toString())
            .list();

        List<List<String>> finalResults = new ArrayList<List<String>>(articleIssues.size());
        for (Object[] row : articleIssues) {
          Journal journal = (Journal) row[0];
          Volume volume = (Volume) row[1];
          Issue issue = (Issue) row[2];

          List<String> secondaryList = new ArrayList<String>();
          secondaryList.add(journal.getId().toString());  // Journal URI
          secondaryList.add(journal.getKey());            // Journal Key
          secondaryList.add(volume.getId().toString());   // Volume URI
          secondaryList.add(volume.getDisplayName());     // Volume name
          secondaryList.add(issue.getId().toString());    // Issue URI
          secondaryList.add(issue.getDisplayName());      // Issue name
          finalResults.add(secondaryList);
        }

        return finalResults;
      }
    });
  }

  /**
   * Get track backs for a given trackbackId
   *
   * @param trackbackId Trackback ID
   * @return List<Trackback> List of track backs
   */
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<Trackback> getTrackbacks(final String trackbackId) {
    return (List<Trackback>) hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List<Trackback> trackbackList = session.createCriteria(Trackback.class)
            .add(Restrictions.eq("annotates", URI.create(trackbackId)))
            .addOrder(Order.desc("created"))
            .list();

        for (Trackback t : trackbackList) {
          t.getBlog_name(); //for lazy-load
        }

        return trackbackList;

      }
    });
  }

  private SecondaryObject convert(final ObjectInfo objectInfo) {
    return new SecondaryObject(objectInfo, smallImageRep, mediumImageRep, largeImageRep);
  }

  /**
   * Set the small image representation
   *
   * @param smallImageRep smallImageRep
   */
  public void setSmallImageRep(final String smallImageRep) {
    this.smallImageRep = smallImageRep;
  }

  /**
   * Set the medium image representation
   *
   * @param mediumImageRep mediumImageRep
   */
  public void setMediumImageRep(final String mediumImageRep) {
    this.mediumImageRep = mediumImageRep;
  }

  /**
   * Set the large image representation
   *
   * @param largeImageRep largeImageRep
   */
  public void setLargeImageRep(final String largeImageRep) {
    this.largeImageRep = largeImageRep;
  }

  /**
   * @param permissionsService the permissions service to use
   */
  @Required
  public void setPermissionsService(PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  /**
   * Set the FileStoreService
   *
   * @param fileStoreService
   */
  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  //WTO: ToDo does this contentType thing belong here?
  private static class ByteArrayDataSource implements DataSource {
    private final FileStoreService fileStoreService;
    private final String fsid;
    private final Representation representation;

    public ByteArrayDataSource(FileStoreService fileStoreService, String fsid, Representation rep) {
      this.fileStoreService = fileStoreService;
      this.fsid = fsid;
      this.representation = rep;
    }

    public String getName() {
      return representation.getObject().getId() + "#" + representation.getName();
    }

    public String getContentType() {
      String ct = representation.getContentType();
      return (ct != null) ? ct : "application/octet-stream";
    }

    public InputStream getInputStream() {
      InputStream fs = null;

      try {
        fs = fileStoreService.getFileInStream(fsid);
      } catch (Exception e) {
        log.error("Error retrieving filestore input stream: ", e.getMessage());
      }
      return fs;
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("writing not supported");
    }
  }

  /**
   * Returns a Map of publishable articles in the order indicated.  The key of each element is the DOI (URI) of the
   * Article.  The value of each element is the Article itself.
   *
   * @param eIssn            eIssn of the Journal that this Article belongs to (no filter if null or empty)
   * @param orderField       field on which to sort the results (no sort if null or empty) Should be one of the
   *                         ORDER_BY_FIELD_ constants from this class
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @return Map of publishable articles sorted as indicated
   * @throws org.topazproject.ambra.ApplicationException
   *
   */
  public List<Article> getPublishableArticles(String eIssn, String orderField,
                                              boolean isOrderAscending) throws ApplicationException {
    try {
      return getArticles(null, null, null, null, eIssn,
          new int[] { Article.STATE_UNPUBLISHED }, orderField, isOrderAscending, 0);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

}
