/**
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://www.plos.org
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

package org.topazproject.ambra.admin.service;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.models.ArticleAnnotation;

/**
 * Service class for admin cache actions
 *
 * @author russ
 */
public class CacheService {

  private CacheManager cacheManager;
  private AnnotationService annotationService;
  private AnnotationConverter annotationConverter;

  /**
   * get cache stats data for display by manageCaches.ftl
   *
   * @return SortedMap of strings
   */
  public SortedMap<String, String[]> getCacheData() {
    SortedMap<String, String[]> cacheStats = new TreeMap<String, String[]>();
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

    return cacheStats;
  }

  /**
   * Remove all keys from a cache
   *
   * @param cacheName the name of the cache to removeAll from
   */
  public void removeAllKeys(String cacheName) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    cache.removeAll();
  }

  /**
   * Remove a key from a cache
   *
   * @param cacheName Which cache to act on
   * @param cacheKey Which key to act on
   * @return True if the key was successfully removed
   */
  public boolean removeSingleKey(String cacheName, String cacheKey) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    return cache.remove(cacheKey);
  }

  /**
   * Clear cache statistics
   *
   * @param cacheName Which cache to act on
   */
  public void clearStatistics(String cacheName) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    cache.clearStatistics();
  }

  /**
   *
   * Return the value of a single cache key
   *
   * @param cacheName Which cache to act on
   * @param cacheKey Which key to act on
   * @return the value of the cache object
   */
  public Object getSingleKey(String cacheName, String cacheKey) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    Element elem = cache.get(cacheKey);
    return (elem == null) ? null : elem.getObjectValue();
  }

  /**
   * Return a list of all keys in a cache
   *
   * @param cacheName Which cache to act on
   * @return a list of keys in the cache
   */
  public List getAllKeys(String cacheName) {
    Ehcache cache = cacheManager.getEhcache(cacheName);
    return cache.getKeysNoDuplicateCheck();
  }

  /**
   * clear all keys related to a given article URI
   *
   * @param cacheURI The URI to act on
   */
  public void clearArticle(String cacheURI) {
    removeSingleKey("ObjectCache", cacheURI);
    removeSingleKey("article-state", cacheURI);
    removeSingleKey("ArticleCarriers", cacheURI);
    removeSingleKey("ArticleAnnotationCache", "ArticleAnnotationCache-Annotation-" + cacheURI);
    removeSingleKey("ArticleAnnotationCache", "ArticleAnnotationCache-Article-" + cacheURI);
    removeSingleKey("ArticleAnnotationCache", "Avg-Ratings-" + cacheURI);
    removeSingleKey("ArticleAnnotationCache", "Trackbacks-" + cacheURI);
    removeSingleKey("BrowseCache", "Article-" + cacheURI);
  }

  /**
   * clear all keys related to a give annotation URI
   *
   * @param cacheURI the URI to act on
   * @return true if the annotation exists
   */
  public boolean clearAnnotation(String cacheURI) {
    try {
      ArticleAnnotation a = annotationService.getArticleAnnotation(cacheURI);
      WebAnnotation annotation = annotationConverter.convert(a, true, true);
      removeSingleKey("ObjectCache", cacheURI);
      if (annotation.getCitation() != null) {
        removeSingleKey("ObjectCache", annotation.getCitation().getId().toString());
      }
      removeSingleKey("ArticleAnnotationCache", "ArticleAnnotationCache-Annotation-" + annotation.getAnnotates());
      return true;
    }
    catch (IllegalArgumentException iae) {
      return false;
    }
  }

  /**
   * clear all keys related to a given Issue URI
   *
   * @param cacheURI the URI to act on
   */
  public void clearIssue(String cacheURI) {
    removeSingleKey("ObjectCache", cacheURI);
    removeSingleKey("BrowseCache", "Issue-" + cacheURI);
  }

  @Required
  public void setCacheManager(CacheManager cm) {
    this.cacheManager = cm;
  }

  @Required
  public void setAnnotationService(AnnotationService as) {
    this.annotationService = as;
  }

  @Required
  public void setAnnotationConverter(AnnotationConverter ac) {
    this.annotationConverter = ac;
  }
}