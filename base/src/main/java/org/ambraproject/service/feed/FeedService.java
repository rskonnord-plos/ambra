/* $HeadURL$
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
package org.ambraproject.service.feed;

import org.ambraproject.ApplicationException;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.LinkbackView;
import org.w3c.dom.Document;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

/**
 * The <code>FeedService</code> supplies the API for querying feed requests. <code>FeedService</code> is a Spring
 * injected singleton which coordinates access to the <code>annotationService</code>, and
 * <code></code>articleService</code>
 */
public interface FeedService {

  //  When the "formatting" parameter is set to this value, the feed will show the complete text of every available field.
  public static final String FEED_FORMATTING_COMPLETE = "complete";

  /**
   * The feedAction data model has a types parameter which specifies the feed type (currently Article or Annotation).
   * Invalid is used when the types parameter does not match any of the current feed types.
   */
  public static enum FEED_TYPES {
    Comment(AnnotationType.COMMENT.toString()),
    Reply(AnnotationType.REPLY.toString()),
    Annotation("Annotation"),
    Article("Article"),
    Issue("Issue"),
    Trackback("Trackback"),
    // Invalid must remain last.
    Invalid(null);

    private String type;

    private FEED_TYPES(String type) {
      this.type = type;
    }

    public String type() {
      return type;
    }
  }

  /**
   * Creates and returns a new <code>FeedSearchParameters</code> for clients of FeedService.
   *
   * @return Key a new FeedSearchParameters to be used as a data model for the FeedAction.
   */
  public FeedSearchParameters newSearchParameters();

  /**
   * Queries for a list of articles from solr using the parameters set in searchParams
   *
   * @param searchParameters
   * @return solr search result that contains list of articles
   */
  public Document getArticles(final FeedSearchParameters searchParameters);

  /**
   * Queries for a list of articles from solr using the parameters set in searchParams
   *
   * @param searchParameters
   * @return solr search result that contains list of articles
   */
  public Document getSearchArticles(final FeedSearchParameters searchParameters) throws ApplicationException;

  /**
   * @param searchParams input parameters
   * @param journal      Current journal
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException ApplicationException
   * @throws URISyntaxException   URISyntaxException
   * @parem authId the current user authId
   */
  public List<ArticleInfo> getIssueArticles(final FeedSearchParameters searchParams, String journal, String authId) throws
      URISyntaxException, ApplicationException;

  /**
   * Returns a list of annotationViews based on parameters contained in searchParams. If a start date is not specified
   * then a default date is used but not stored in searchParams.
   *
   * @param searchParams input parameters.
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException Converts all exceptions to ApplicationException
   */
  public List<AnnotationView> getAnnotations(final AnnotationFeedSearchParameters searchParams)
      throws ParseException, URISyntaxException;

  /**
   * Returns a list of trackbackViews based on parameters contained in the searchParams. If a start date is not
   * specified then a default date is used but not stored in searchParams.
   *
   * @param searchParams search parameters
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException Converts all exceptions to ApplicationException
   */
  public List<LinkbackView> getTrackbacks(final AnnotationFeedSearchParameters searchParams)
      throws ParseException, URISyntaxException;

}
