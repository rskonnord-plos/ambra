/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.topazproject.ambra.admin.action;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.models.ArticleAnnotation;

/**
 * Display Cache statistics.
 *
 * @author jsuttor
 * @author russ
 */
@SuppressWarnings("serial")
public class ManageCachesAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ManageCachesAction.class);
  private SortedMap<String, String[]> cacheStats = new TreeMap<String, String[]>();
  private CacheManager cacheManager;
  private List   cacheKeys = new ArrayList();

  // fields usd for execute
  private String cacheAction;
  private String cacheName;
  private String cacheKey;

  // fields used for clearCacheWithAssistance
  private String cacheURI;
  private String cacheURIType;

  private AnnotationService annotationService;
  private AnnotationConverter annotationConverter;

  @SuppressWarnings("unchecked")
  @Override
  public String execute() throws Exception {

    // create a faux journal object for template
    initJournal();

    // any actions?
    if ("removeAll".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cache.removeAll();
      addActionMessage(cacheName + ".removeAll() executed.");
    } else if ("remove".equals(cacheAction) && cacheName != null && cacheKey != null) {
      removeCacheKey(cacheName, cacheKey);
    } else if ("clearStatistics".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cache.clearStatistics();
      addActionMessage(cacheName + ".clearStatistics() executed.");
    } else if ("get".equals(cacheAction) && cacheName != null && cacheKey != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      Element elem = cache.get(cacheKey);
      final Object objectValue = (elem == null) ? null : elem.getObjectValue();
      addActionMessage(cacheName + ".get(" + cacheKey + ") = " + objectValue);
    } else if ("getKeys".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cacheKeys = cache.getKeysNoDuplicateCheck();
      try {
        Collections.sort(cacheKeys);
        addActionMessage(cacheName + ".getKeysNoDuplicateCheck() executed.");
      } catch (ClassCastException e) {
        log.warn("Caught ClassCastException while sorting cachekeys for " + cacheName, e);
        addActionMessage(cacheName + ".getKeysNoDuplicateCheck() executed. (unsorted list)");
      }
    }

    getCacheData();

    return SUCCESS;
  }

  /**
   * Intelligently Clear a set of cache keys for a given URI
   * @return webwork status
   * @throws Exception Exception
   */
  public String clearCacheWithAssistance() throws Exception {

    // create a faux journal object for template
    initJournal();

    assert cacheURI != null;

    if ("Article".equals(cacheURIType)) {
      removeCacheKey("ObjectCache", cacheURI);
      removeCacheKey("article-state", cacheURI);
      removeCacheKey("ArticleCarriers", cacheURI);
      removeCacheKey("ArticleAnnotationCache", "ArticleAnnotationCache-Annotation-" + cacheURI);
      removeCacheKey("ArticleAnnotationCache", "ArticleAnnotationCache-Article-" + cacheURI);
      removeCacheKey("ArticleAnnotationCache", "Avg-Ratings-" + cacheURI);
      removeCacheKey("BrowseCache", "Article-" + cacheURI);
    }
    else if ("Annotation".equals(cacheURIType)) {
      try {
        ArticleAnnotation a = annotationService.getArticleAnnotation(cacheURI);
        WebAnnotation annotation = annotationConverter.convert(a, true, true);
        removeCacheKey("ObjectCache", cacheURI);
        if (annotation.getCitation() != null) {
          removeCacheKey("ObjectCache", annotation.getCitation().getId().toString());
        }
        removeCacheKey("ArticleAnnotationCache", "ArticleAnnotationCache-Annotation-" + annotation.getAnnotates());
      } catch (IllegalArgumentException iae) {
        addActionError("There is no Annotation with the ID: " + cacheURI);
      }
    }
    else if ("Issue".equals(cacheURIType)) {
      removeCacheKey("ObjectCache", cacheURI);
      removeCacheKey("BrowseCache", "Issue-" + cacheURI);
    }
    else {
      assert false;
    }

    getCacheData();

    return SUCCESS;
  }

  /**
   * get cache stats data for display by manageCaches.ftl
   *
   */
  private void getCacheData() {
    // header row
    cacheStats.put("", new String[] {
      "Size #/Objects",
      "Hits (Memory/Disk)",
      "Misses",
      "Eternal (TTI/TTL)",
      "Disk Overflow/Persistent",
    });

    final String[] cacheNames = cacheManager.getCacheNames();
    for (final String displayName : cacheNames) {
      final Ehcache cache = cacheManager.getEhcache(displayName);
      final Statistics statistics = cache.getStatistics();
      cacheStats.put(displayName, new String[] {
        String.valueOf(cache.getSize()) + " / "
          + String.valueOf(statistics.getObjectCount()),
        String.valueOf(statistics.getCacheHits()) + " ( "
          + String.valueOf(statistics.getInMemoryHits()) + " / "
          + String.valueOf(statistics.getOnDiskHits()) + " )",
        String.valueOf(statistics.getCacheMisses()),
        String.valueOf(cache.getCacheConfiguration().isEternal()) + " ( "
          + String.valueOf(cache.getCacheConfiguration().getTimeToIdleSeconds()) + " / "
          + String.valueOf(cache.getCacheConfiguration().getTimeToLiveSeconds()) + " )",
        String.valueOf(cache.getCacheConfiguration().isOverflowToDisk()) + " / "
          + String.valueOf(cache.getCacheConfiguration().isDiskPersistent())
      });
    }
  }

  /**
   * Remove a key from a cache
   *
   * @param cacheName The name of the cache to remove the key from
   * @param cacheKey The key to remove
   */
  private void removeCacheKey(String cacheName, String cacheKey) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    boolean removed = cache.remove(cacheKey);
    if (removed) {
      addActionMessage(cacheName + ".remove(" + cacheKey + ") = " + removed);
    }
    else {
      addActionError(cacheName + ".remove(" + cacheKey + ") = " + removed);
    }
  }
  
  /**
   * Get cacheName.
   *
   * @return Cache name.
   */
  public String getCacheName() {
    return cacheName;
  }

  /**
   * Get cacheStats.
   *
   * @return Cache stats.
   */
  public SortedMap<String, String[]> getCacheStats() {
    return cacheStats;
  }

  /**
   * Get cacheKeys.
   *
   * @return Cache keys.
   */
  @SuppressWarnings("unchecked")
  public List getCacheKeys() {
    return cacheKeys;
  }

  /**
   * @param cacheManager The cacheManager to use.
   */
  @Required
  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * getter for cacheURIType.
   * @return Value of cacheURIType.
   */
  public String getCacheURIType() {
    if (StringUtils.isBlank(cacheURIType)) {
      return "Article";
    }
    else {
      return cacheURIType;
    }
  }

  /**
   * @param cacheAction The cacheAction to use.
   */
  @Required
  public void setCacheAction(String cacheAction) {
    this.cacheAction = cacheAction;
  }

  /**
   * @param cacheName The cacheName to use.
   */
  @Required
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * @param cacheKey The cachekey to use.
   */
  @Required
  public void setCacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
  }

  /**
   * @param cacheURI The cache URI to use.
   */
  @Required
  public void setCacheURI(String cacheURI) {
    this.cacheURI = cacheURI;
  }

  /**
   * @param cacheURIType The cache URI Type to use.
   */
  @Required
  public void setCacheURIType(String cacheURIType) {
    this.cacheURIType = cacheURIType;
  }

  /**
   * Set AnnotationService.
   * @param annotationService the annotation service to set
   */
  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter converter) {
    this.annotationConverter = converter;
  }
}
