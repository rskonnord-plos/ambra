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
package org.topazproject.ambra.article.service;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.rating.action.ArticleRatingSummary;

import javax.activation.DataSource;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Provide Article "services" via OTM.
 */
public interface ArticlePersistenceService {

  public static final String ORDER_BY_FIELD_DATE_CREATED = "dublinCore.created"; //DublinCore created column
  public static final String ORDER_BY_FIELD_ARTICLE_DOI = "id"; // Article.articleUri column.
  public static final String ORDER_BY_FIELD_DATE_PUBLISHED = "dublinCore.date";  // DublinCore date column

  /**
   * Get the contents of the given object.
   *
   * @param obj the uri of the object
   * @param rep the representation to get
   * @param authId the authorization ID of the current user
   * @return the contents
   * @throws NoSuchObjectIdException if the object cannot be found
   * @throws NoSuchArticleIdException if the article cannot be found
   */
  public DataSource getContent(final String obj, final String rep, final String authId)
    throws NoSuchObjectIdException, NoSuchArticleIdException;

  /**
   * Delete an article.
   *
   * @param article the uri of the article
   * @param authId the authorization ID of the current user
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void delete(String article, final String authId) throws NoSuchArticleIdException;

  /**
   * Change an articles state.
   *
   * @param article uri
   * @param authId the authorization ID of the current user
   * @param state   state
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void setState(final String article, final String authId, final int state) throws NoSuchArticleIdException;

  /**
   * Get the ids of all articles satisfying the given criteria.
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
   *                         ORDER_BY_FIELD_ constants from this Interface
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @param maxResults       maximum number of results to return, or 0 for no limit
   * @return the (possibly empty) list of article ids.
   * @throws java.text.ParseException if any of the dates could not be parsed
   */
  public List<String> getArticleIds(final String startDate, final String endDate, final String[] categories,
                                    final String[] authors, final String eIssn, final int[] states,
                                    final String orderField, final boolean isOrderAscending,
                                    final int maxResults) throws ParseException;

  /**
   * Get all of the articles satisfying the given criteria.
   *
   * @param startDate  is the date to start searching from. If null, start from begining of time. Can be iso8601
   *                   formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param categories is list of categories to search for articles within (all categories if null or empty)
   * @param authors    is list of authors to search for articles within (all authors if null or empty)
   * @param eIssn      the journal eIssn to filter by.  Null for no filter
   * @param states     the list of article states to search for (all states if null or empty)
   * @param orderField the field to order by. Must be {@link ArticlePersistenceService#ORDER_BY_FIELD_ARTICLE_DOI},
   *                   {@link ArticlePersistenceService#ORDER_BY_FIELD_DATE_CREATED}, or null
   * @param ascending  controls the sort order (by date).
   * @param maxResults the maximum number of results to return, or 0 for no limit
   * @return the (possibly empty) list of articles.
   */
  public List<Article> getArticles(String startDate, String endDate, String[] categories,
                                   final String[] authors, final String eIssn, final int[] states,
                                   final String orderField, final boolean ascending,
                                   final int maxResults);

  /**
   * Get the Article's ObjectInfo by URI.
   *
   * @param uri uri
   *
   * @return the object-info of the object
   * @throws NoSuchObjectIdException NoSuchObjectIdException
   */
  public ObjectInfo getObjectInfo(final String uri, final String authId) throws NoSuchObjectIdException;

  /**
   * Get an Article by URI.
   *
   * @param uri URI of Article to get.
   * @param authId the authorization ID of the current user
   *
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public Article getArticle(final URI uri, final String authId) throws NoSuchArticleIdException;

  /**
   * Get articles based on a list of Article id's.
   *
   * @param articleIds list of article id's
   * @param authId the authorization ID of the current user
   * @return <code>List&lt;Article&gt;</code> of articles requested
   * @throws ParseException   when article ids are invalid
   * @throws RuntimeException when session.get fails.
   * @throws NoSuchArticleIdException when the article doesn't exist
   */
  public List<Article> getArticles(List<String> articleIds, final String authId) throws ParseException, NoSuchArticleIdException;

  /**
   * Return the list of secondary objects
   *
   * @param article uri
   * @param authId the authorization ID of the current user
   * @return the secondary objects of the article
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public SecondaryObject[] listSecondaryObjects(final String article, final String authId)
      throws NoSuchArticleIdException;

  /**
   * Return a list of Figures and Tables in DOI order.
   *
   * @param article DOI.
   * @param authId the authorization ID of the current user
   * @return Figures and Tables for the article in DOI order.
   * @throws NoSuchArticleIdException NoSuchArticleIdException.
   */
  public SecondaryObject[] listFiguresTables(final String article, final String authId) throws NoSuchArticleIdException;

  /**
   * Determines if the articleURI is of type researchArticle
   *
   * @param articleURI The URI of the article
   * @param authId the authorization ID of the current user
   * @return True if the article is a research article
   * @throws ApplicationException     if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isResearchArticle(String articleURI, final String authId)
      throws ApplicationException, NoSuchArticleIdException;

  /**
   * Get rating summaries for the passed in articleURI
   *
   * @param articleURI articleURI to get rating summaries for
   * @return List<RatingSummary> of rating Summaries
   */
  public List<RatingSummary> getRatingSummaries(URI articleURI);

  /**
   * Get article rating summaries for the passed in articleURI
   *
   * @param articleURI articleURI to get rating summaries for
   * @return List<ArticleRatingSummary> of rating Summaries
   */
  public List<ArticleRatingSummary> getArticleRatingSummaries(URI articleURI);

  /**
   * Get a List of all of the Journal/Volume/Issue combinations that contain the <code>articleURI</code> which was
   * passed in. Each primary List element contains a secondary List of six Strings which are, in order: <ul>
   * <li><strong>Element 0: </strong> Journal URI</li> <li><strong>Element 1: </strong> Journal key</li>
   * <li><strong>Element 2: </strong> Volume URI</li> <li><strong>Element 3: </strong> Volume name</li>
   * <li><strong>Element 4: </strong> Issue URI</li> <li><strong>Element 5: </strong> Issue name</li> </ul> A Journal
   * might have multiple Volumes, any of which might have multiple Issues that contain the <code>articleURI</code>.
   * The primary List will always contain one element for each Issue that contains the <code>articleURI</code>.
   *
   * @param articleURI Article URI that is contained in the Journal/Volume/Issue combinations which will be returned
   * @return All of the Journal/Volume/Issue combinations which contain the articleURI passed in
   */
  public List<List<String>> getArticleIssues(URI articleURI);

  /**
   * Get track backs for a given trackbackId
   *
   * @param trackbackId Trackback ID
   * @return List<Trackback> List of track backs
   */
  public List<Trackback> getTrackbacks(String trackbackId);

  /**
   * Returns a list of publishable articles
   *
   * @param eIssn            eIssn of the Journal that this Article belongs to (no filter if null or empty)
   * @param orderField       field on which to sort the results (no sort if null or empty) Should be one of the
   *                         ORDER_BY_FIELD_ constants from this class
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @return list of publishable articles sorted by pub date
   * @throws org.topazproject.ambra.ApplicationException
   *
   */
  public List<Article> getPublishableArticles(String eIssn, String orderField,
                                              boolean isOrderAscending) throws ApplicationException;
}
