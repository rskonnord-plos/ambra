/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.ambra.action;

import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.ServletActionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.models.TrackbackContent;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.criterion.Restrictions;

import org.apache.roller.util.LinkbackExtractor;

/**
 * Class to process trackback requests from external sites.  Writes information to
 * store if previous one does not exist and spam checking is passed.
 *
 * @author Stephen Cheng
 *
 */
public class TrackbackAction extends BaseActionSupport {
  private static final Log log = LogFactory.getLog(TrackbackAction.class);
  private static final String TRACK_BACKS_KEY = "Trackbacks-";
  private int error = 0;
  private String errorMessage = "";
  private String title;
  private String url;
  private String excerpt;
  private String blog_name;
  private String trackbackId;
  private String articleURI;
  private List<Trackback> trackbackList;
  private ObjectInfo articleObj;
  private Cache articleAnnotationCache;

  private ArticleOtmService articleOtmService;

  private Session          session;
  private static final String ARTICLE_ACTION = "ambra.platform.articleAction";

  /**
   * Main action execution.
   *
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public String execute() throws Exception {

    if (!ServletActionContext.getRequest().getMethod().equals("POST")) {
      if (log.isDebugEnabled()) {
        log.debug("Returning error because HTTP POST was not used.");
      }
      return returnError ("HTTP method must be POST");
    }

    // TODO: you should not need to access request directly in struts action, use parameter map here
    VirtualJournalContext journalContext = (VirtualJournalContext) ServletActionContext.getRequest()
        .getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);

    final URL permalink;
    final URI trackback;
    try {
      permalink = new URL (url);
    } catch (Exception e) {
      if (log.isInfoEnabled()) {
        log.info ("Could not construct URL with parameter: " + url);
      }
      return returnError("URL invalid");
    }

    try {
      trackback = new URI (trackbackId);
    } catch (Exception e) {
      if (log.isInfoEnabled()) {
        log.info ("Could not construct URI with parameter: " + trackbackId);
      }
      return returnError("Object URI invalid");
    }

    boolean inserted = false;
    List<Trackback> trackbackList = session
      .createCriteria(Trackback.class)
      .add(Restrictions.eq("annotates", trackback))
      .createCriteria("body")
      .add(Restrictions.eq("url", url))
      .list();

    if (trackbackList.size() == 0) {
      if (log.isDebugEnabled()) {
        log.debug("No previous trackback found for: " + permalink);
      }

      LinkbackExtractor linkback = new LinkbackExtractor(url,
                                 getArticleUrl(journalContext.getBaseUrl(), trackbackId));
      if (linkback.getExcerpt() == null) {
        if (log.isDebugEnabled()) {
          log.debug("Trackback failed verification: " + permalink);
        }
        return returnError("No trackback present");
      }

      TrackbackContent tc = new TrackbackContent (title, excerpt, blog_name, permalink);
      Trackback tb = new Trackback();
      tb.setBody(tc);
      tb.setAnnotates(trackback);
      tb.setCreated(new Date());

      session.saveOrUpdate(tb);
      articleAnnotationCache.remove(TRACK_BACKS_KEY +  trackbackId);
      inserted = true;
    }

    if (log.isInfoEnabled() && inserted) {
      if (log.isDebugEnabled() && inserted) {
        StringBuilder msg = new StringBuilder("Successfully inserted trackback for resource: ")
                                           .append (trackbackId)
                                           .append ("; with title: ").append (title)
                                           .append ("; url: ").append (url)
                                           .append ("; excerpt: ").append (excerpt)
                                           .append ("; blog_name: ").append (blog_name);
        log.debug(msg);
      } else {
        StringBuilder msg = new StringBuilder("Successfully inserted trackback for resource: ")
                                           .append (trackbackId);
        log.info(msg);
      }
    }
    return SUCCESS;
  }


  /**
   * Sets the trackbackList with the trackback objects only, not their bodies in
   * an effort to make things more efficient.  Useful if you only want to know the
   * number of trackbacks, or any other information in the base Annotation class.
   *
   * @return status
   */
  @Transactional(readOnly = true)
  public String getTrackbackCount() {
    if (trackbackId == null)
      trackbackId = articleURI;
    return getTrackbacks(false);
  }

  /**
   * Sets the trackbackList with all trackbacks for a given article (including bodies)
   * and also retrieves the article information object and sets articleObj with that
   * information.
   *
   * @return status
   * @throws Exception
   */
  @Transactional(readOnly = true)
  public String getTrackbacksForArticle() throws Exception {
    if (trackbackId == null)
      trackbackId = articleURI;
    articleObj = articleOtmService.getArticle(URI.create(trackbackId));

    return getTrackbacks(true);
  }

  /**
   * Set trackbackList with all trackbacks for the resource given by trackbackId.
   *
   * @return status
   */
  @Transactional(readOnly = true)
  public String getTrackbacks() {
    return getTrackbacks(true);
  }

  private String getTrackbacks (final boolean getBodies) {
    List<String> ids = articleAnnotationCache.get(TRACK_BACKS_KEY  + trackbackId, -1,
            new Cache.SynchronizedLookup<List<String>, OtmException>(trackbackId.intern()) {
              public List<String> lookup() throws OtmException {
                return getIds();
              }
            });

    trackbackList = new ArrayList<Trackback>(ids.size());

    for (String id : ids) {
      Trackback t = session.get(Trackback.class, id);
      trackbackList.add(t);

      if (getBodies)
        t.getBlog_name(); // for lazy load
    }

    return SUCCESS;
  }

  private List<String> getIds() throws OtmException {
    if (log.isDebugEnabled())
      log.debug("retrieving trackbacks for: " + trackbackId);

    List<String> ids = new ArrayList<String>();
    Results r = session.createQuery("select t.id, t.created created from Trackback t where "
       + "t.annotates = :id order by created desc;").setParameter("id", trackbackId).execute();

    while (r.next())
      ids.add(r.getString(0));

    return ids;
  }

  private String returnError (String errMsg) {
    error = 1;
    errorMessage = errMsg;
    return ERROR;
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    return blog_name;
  }

  /**
   * @param blog_name The blog_name to set.
   */
  public void setBlog_name(String blog_name) {
    this.blog_name = blog_name;
  }

  /**
   * @return Returns the error.
   */
  public int getError() {
    return error;
  }

  /**
   * @param error The error to set.
   */
  public void setError(int error) {
    this.error = error;
  }

  /**
   * @return Returns the errorMessage.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage The errorMessage to set.
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * @param excerpt The excerpt to set.
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the url.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url The url to set.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return Returns the trackbackId.
   */
  public String getTrackbackId() {
    return trackbackId;
  }

  /**
   * @param trackbackId The trackbackId to set.
   */
  public void setTrackbackId(String trackbackId) {
    this.trackbackId = trackbackId;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }

  /**
   * @return Returns the trackbackList.
   */
  public List<Trackback> getTrackbackList() {
    return trackbackList;
  }

  /**
   * @param trackbackList The trackbackList to set.
   */
  public void setTrackbackList(List<Trackback> trackbackList) {
    this.trackbackList = trackbackList;
  }

  private String getArticleUrl (String baseURL, String articleURI) {
    String escapedURI;
    try {
      escapedURI = URLEncoder.encode(articleURI, "UTF-8");
    } catch (UnsupportedEncodingException ue) {
      escapedURI = articleURI;
    }

    StringBuilder url = new StringBuilder(baseURL).append("/").
                                                   append(configuration.getString(ARTICLE_ACTION)).
                                                   append(escapedURI);

    if (log.isDebugEnabled()) {
      log.debug("article url to find is: " + url.toString());
    }

    return url.toString();
  }

  /**
   * @param articleOtmService The ArticleOtmService to set.
   */
  @Required
  public void setArticleOtmService(ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * @return Returns the articleInfo.
   */
  public ObjectInfo getArticleObj() {
    return articleObj;
  }

  /**
   * @param articleURI The articleURI to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }
}
