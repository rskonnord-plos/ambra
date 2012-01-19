/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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

package org.topazproject.ambra.article.action;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticleFeedService;
import org.topazproject.ambra.article.service.FeedCacheKey;
import org.topazproject.ambra.article.service.ArticleFeedService.FEED_TYPES;
import org.topazproject.ambra.web.VirtualJournalContext;

import com.opensymphony.xwork2.ModelDriven;

/**
 * The <code>class ArticleFeed</code> provides an API for criteria based retrieval of articles and
 * article information. The <code>class ArticleFeed</code> implements the Struts ModelDrive
 * interface. The data model used for <code>ArticleFeed</code> is <code>class Key</code>. The the
 * field <code>ArticleFeed.cacheKey</code> is accessible to Struts through the
 * <code>ArticleFeed.getModel</code> and <code>ArticleFeed.getCacheKey</code> bean getter. The
 * ModelDriven Interceptor parses the input parameters, converts them to the appropriate Java types
 * then assigns them to fields in the data model.
 *
 * <p>
 * The <code>ArticleFeed.cacheKey</code> serves the following purposes:
 * <ul>
 * <li> Receives and validates the parameters passed in during a Post/Get.
 * <li> Uses these parameters to compute a hashcode for cache lookups.
 * <li> Used to pass these parameters to AmbraFeedResult via the ValueStack.
 * <li> Registers a cache Invalidator as a cache listner.
 * </ul>
 * <p>
 *
 * ArticleFeed implements the <code>ArticleFeed.execute</code> and <code>ArticleFeed.validate
 * </code> Struts entry points. The <code>ArticleFeed.validate</code> method assigns default values
 * to fields not provided by user input and checks parameters that are provided by the user. By the
 * time Struts invokes the <code>ArticleFeed.execute</code> all model data variables should be in a
 * known and acceptable state for execution. <code>ArticleFeed.execute</code> first checks the feed
 * cache for identical queries or calls <code>ArticleFeed.getFeedData</code> if there is a miss. A
 * list of article ID's is the result. It is up to the result handler to fetch the articles and
 * serialize the output.
 *
 * <p>
 * <ul>
 * <li>Define a hard limit of 200 articles returned in one query.
 * <li>If startDate &gt; endDate then startDate set to endDate.
 * </ul>
 *
 * <h4>Action URI</h4>
 * http://.../article/feed
 * <h4>Parameters</h4>
 * <pre>
 * <strong>
 * Param        Format        Required     Default                 Description </strong>
 * startDate ISO yyyy/MM/dd     No         -3 months   Start Date - search for articles
 *                                                     dated &gt;= to sDate
 * endDate   ISO yyyy/MM/dd     No         today       End Date - search for articles
 *                                                     dated <= to eDate
 * category    String           No         none        Article Category
 * author      String           No         none        Article Author name ex: John+Smith
 * relLinks    Boolean          No         false       If relLinks=true; internal links will be
 *                                                     relative to xmlbase
 * extended    Boolean          No         false       If extended=true; provide additional feed
 *                                                     information
 * title       String           No         none        Sets the title of the feed
 * selfLink    String           No         none        URL of feed that is to be put in the feed
 *                                                     data.
 * maxResults  Integer          No         30          The maximun number of result to return.
 * type        String           No         Article     Article,Annotation,FormalCorrectionAnnot
 *                                                     MinorCorrectionAnnot,CommentAnnot
 * </pre>
 *
 * @see       org.topazproject.ambra.article.service.FeedCacheKey
 * @see       org.topazproject.ambra.struts2.AmbraFeedResult
 *
 * @author Jeff Suttor
 * @author Eric Brown
 */
 @SuppressWarnings("UnusedDeclaration")
public class ArticleFeed extends BaseActionSupport implements ModelDriven {
  private static final Log log = LogFactory.getLog(ArticleFeed.class);

  //TODO: move these to BaseAction support and standardize on result dispatching.
  private static final String ATOM_RESULT = "ATOM1_0";
  private static final String JSON_RESULT = "json";

  private        ArticleFeedService  articleFeedService; // Feed Service Spring injected.
  private        FeedCacheKey        cacheKey;           // The cache key and action data model
  private        List<String>        articleIds;         // List of Article IDs; result of search

  /**
   * Try and find the query in the feed cache or query the Article OTM Service if nothing
   * is found. The parameters are valid by this point.
   *
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    List<String> annotTypes = new ArrayList<String>();
    FEED_TYPES t = cacheKey.feedType();

    switch (t) {
      case Annotation :
        articleIds = articleFeedService.getAnnotationIds(cacheKey, null);
        break;
      case FormalCorrectionAnnot :
        annotTypes.add(t.rdfType()) ;
        articleIds = articleFeedService.getAnnotationIds(cacheKey,annotTypes);
        break;
      case MinorCorrectionAnnot :
        annotTypes.add(t.rdfType()) ;
        articleIds = articleFeedService.getAnnotationIds(cacheKey,annotTypes);
        break;
     case CommentAnnot :
        annotTypes.add(t.rdfType()) ;
        articleIds = articleFeedService.getAnnotationIds(cacheKey,annotTypes);
        break;
      case Article :
        articleIds = articleFeedService.getArticleIds(cacheKey);
        break;
    }

    return SUCCESS;
  }

  /**
   * Validate the input parameters or create defaults when they are not provided.  Struts calls this
   * automagically after the parameters are parsed and the proper fields are set in the data model.
   * It is assumed that all necessary fields are checked for validity and created if not specified.
   * The <code>ArticleFeed.execute</code> should be able to use them without any further checks.
   */
  @Override
  public void validate () {
    /*
     * The cacheKey must have both the current Journal and start date.  Current Journal is set here
     * and startDate will be set in the data model validator.
     */
    cacheKey.setJournal(getCurrentJournal());
    cacheKey.validate(this);
  }

  /**
   * Set <code>articleFeedService</code> field to the article Feed service singleton.
   *
   * @param  articleFeedService  the object transaction model reference
   */
  public void setArticleFeedService(final ArticleFeedService articleFeedService) {
    this.articleFeedService = articleFeedService;
  }

  /**
   * This is the results of the query which consist of a list of article ID's.
   *
   * @return the list of article ID's returned from the query.
   */
  public List<String> getIds() {
    return articleIds;
  }

  /**
   * Return the cache key being used by this action.
   *
   * @return  Key to the cache which is also the data model of the action
   */
  public FeedCacheKey getCacheKey() {
    return this.cacheKey;
  }

  /**
   * Return the a cache key which is also the data model for the model driven interface.
   *
   * @return Key to the cache which is also the data model of the action
   */
  public Object getModel() {
    /*
     * getModel is invoked several times by different interceptors
     * Make sure caheKey is not created twice.
     */
    return (cacheKey == null) ? cacheKey = articleFeedService.newCacheKey() : cacheKey;
  }

  /**
   * Retrieve the <code>VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT</code> from the servlet
   * container. This is set by the <code>VirtualJournalContextFilter.doFilter</code>
   *
   * @see org.topazproject.ambra.web.VirtualJournalContextFilter
   *
   * @return String virtual journal of request
   */
  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
        getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }
}
