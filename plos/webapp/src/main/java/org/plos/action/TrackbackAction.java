/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.action;

import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Date;
import java.util.List;

import org.apache.struts2.ServletActionContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.FetchArticleService;
import org.plos.configuration.ConfigurationStore;
import org.plos.models.ObjectInfo;
import org.plos.models.Trackback;
import org.plos.models.TrackbackContent;
import org.plos.web.VirtualJournalContext;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

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
  private int error = 0;
  private String errorMessage = "";
  private String title;
  private String url;
  private String excerpt;
  private String blog_name;
  private String trackbackId;
  private String articleURI;
  private List<Trackback> trackbackList;
  private VirtualJournalContext journalContext;
  private ObjectInfo articleObj;

  private FetchArticleService fetchArticleService;
  private static final Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();

  private Session          session;

  /**
   * Main action execution.
   *
   */
  public String execute() throws Exception {

    if (!ServletActionContext.getRequest().getMethod().equals("POST")) {
      if (log.isDebugEnabled()) {
        log.debug("Returning error because HTTP POST was not used.");
      }
      return returnError ("HTTP method must be POST");
    }

    journalContext = (VirtualJournalContext)ServletActionContext.getRequest()
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

    return TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<String, Exception>() {
      public String run(Transaction tx) throws Exception {
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
          inserted = true;
        }

        if (log.isInfoEnabled() && inserted) {
          if (log.isDebugEnabled() && inserted){
            StringBuilder msg = new StringBuilder ("Successfully inserted trackback for resource: ")
                                               .append (trackbackId)
                                               .append ("; with title: ")
                                               .append (title)
                                               .append ("; url: ")
                                               .append (url)
                                               .append ("; excerpt: ")
                                               .append (excerpt)
                                               .append ("; blog_name: ")
                                               .append (blog_name);
            log.debug(msg);
          } else {
            StringBuilder msg = new StringBuilder ("Successfully inserted trackback for resource: ")
                                               .append (trackbackId);
            log.info(msg);
          }
        }
        return SUCCESS;
      }
    });
  }


  /**
   * Sets the trackbackList with the trackback objects only, not their bodies in
   * an effort to make things more efficient.  Useful if you only want to know the
   * number of trackbacks, or any other information in the base Annotation class.
   *
   * @return status
   * @throws Exception
   */
  public String getTrackbackCount() throws Exception {
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
  public String getTrackbacksForArticle() throws Exception {
    if (trackbackId == null)
      trackbackId = articleURI;
    articleObj = fetchArticleService.getArticleInfo(trackbackId);

    return getTrackbacks(true);
  }

  /**
   * Set trackbackList with all trackbacks for the resource given by trackbackId.
   *
   * @return status
   * @throws Exception
   */
  public String getTrackbacks() throws Exception {
    return getTrackbacks(true);
  }

  private String getTrackbacks (final boolean getBodies) throws Exception {
    return TransactionHelper.doInTxE(session,
                              new TransactionHelper.ActionE<String, Exception>() {
      public String run(Transaction tx) throws Exception {
        if (log.isDebugEnabled()) {
          log.debug("retrieving trackbacks for: " + trackbackId);
        }

        trackbackList = session
          .createCriteria(Trackback.class)
          .add(Restrictions.eq("annotates", trackbackId))
          .addOrder(Order.desc("created"))
          .list();

        if (getBodies)
          for (Trackback t : trackbackList)
             t.getBlog_name(); // for lazy load
        return SUCCESS;
      }
    });
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
    String escapedURI = null;
    try {
      escapedURI = URLEncoder.encode(articleURI, "UTF-8");
    } catch (UnsupportedEncodingException ue) {
      escapedURI = articleURI;
    }
    
    StringBuilder url = new StringBuilder(baseURL).append("/").append (myConfig.getString("pub.article-action"))
    .append(escapedURI);
    
    if (log.isDebugEnabled()) {
      log.debug("article url to find is: " + url.toString());
    }
    
    return url.toString();
  }

  /**
   * @param fetchArticleService The fetchArticleService to set.
   */
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
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
