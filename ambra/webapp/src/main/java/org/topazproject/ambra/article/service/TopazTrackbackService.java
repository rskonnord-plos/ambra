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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.models.TrackbackContent;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Track back service.
 */
public class TopazTrackbackService implements TrackbackService {

  private static final String TRACK_BACKS_KEY  = "Trackbacks-";
  private static final Logger log = LoggerFactory.getLogger(FetchArticleService.class);
  private Session otmSession;

  private Cache articleAnnotationCache;

  /**
   * Get the trackbacks for the given ID
   * @param trackbackId The trackbackID
   * @param getBodies set to true to NOT lazyload trackback bodies
   * @return an arrayList of trackbacks
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public ArrayList<Trackback> getTrackbacks (final String trackbackId, final boolean getBodies) {
    List<String> ids = articleAnnotationCache.get(TRACK_BACKS_KEY  + trackbackId, -1,
            new Cache.SynchronizedLookup<List<String>, OtmException>(trackbackId.intern()) {
              public List<String> lookup() throws OtmException {
                return getIds(trackbackId);
              }
            });

    ArrayList<Trackback> trackbackList = new ArrayList<Trackback>(ids.size());

    for (String id : ids) {
      Trackback t = otmSession.get(Trackback.class, id);
      trackbackList.add(t);

      if (getBodies)
        t.getBlog_name(); // for lazy load
    }

    return trackbackList;
  }

  /**
   * Saves a track back
   * @param title The title of the article
   * @param blog_name the blog name
   * @param excerpt the excertp from the blog
   * @param permalink a permalink to the article
   * @param trackback
   * @param url
   * @param trackbackId
   * @return true if the trackback was saved, false if the trackback already existed
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = { Throwable.class })
  public boolean saveTrackBack(final String title, final String blog_name,
                               final String excerpt, final URL permalink, final URI trackback,
                               final String url, final String trackbackId) throws Exception {

    List<Trackback> trackbackList = otmSession
      .createCriteria(Trackback.class)
      .add(Restrictions.eq("annotates", trackback))
      .createCriteria("body")
      .add(Restrictions.eq("url", url))
      .list();

    if (trackbackList.size() == 0) {
      if (log.isDebugEnabled()) {
        log.debug("No previous trackback found for: " + permalink);
      }

      TrackbackContent tc = new TrackbackContent (title, excerpt, blog_name, permalink);
      Trackback tb = new Trackback();
      tb.setBody(tc);
      tb.setAnnotates(trackback);
      tb.setCreated(new Date());

      otmSession.saveOrUpdate(tb);
      articleAnnotationCache.remove(TRACK_BACKS_KEY +  trackbackId);

      return true;
    }

    return false;
  }

  /**
   * Fetch trackbacks for the given trackbackID
   * @param trackbackId a URI
   * @return a list of IDs for trackbacks
   * @throws org.topazproject.otm.OtmException
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  private List<String> getIds(final String trackbackId) throws OtmException {
    if (log.isDebugEnabled())
      log.debug("retrieving trackbacks for: " + trackbackId);

    List<String> ids = new ArrayList<String>();
    Results r = otmSession.createQuery("select t.id, t.created created from Trackback t where "
       + "t.annotates = :id order by created desc;").setParameter("id", trackbackId).execute();

    while (r.next())
      ids.add(r.getString(0));

    return ids;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.otmSession = session;
  }

  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
  }
}
