/* $$HeadURL::                                                                            $$
 * $$Id$$
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

package org.topazproject.ambra.admin.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.action.BaseActionSupport;

/**
 * Display Cache statistics.
 *
 * @author jsuttor
 */
@SuppressWarnings("serial")
public class ManageCachesAction extends BaseActionSupport {
  private static final Log     log            = LogFactory.getLog(ManageCachesAction.class);

  private SortedMap<String, String[]> cacheStats = new TreeMap<String, String[]>();

  private CacheManager cacheManager;
  private String cacheAction;
  private String cacheName;
  @SuppressWarnings("unchecked")
  private List   cacheKeys = new ArrayList();
  private String cacheKey;

  @SuppressWarnings("unchecked")
  @Override
  public String execute() throws Exception {

    // any actions?
    if ("removeAll".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cache.removeAll();
      addActionMessage(cacheName + ".removeAll() executed.");
    } else if ("remove".equals(cacheAction) && cacheName != null && cacheKey != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      final boolean removed = cache.remove(cacheKey);
      final String message = cacheName + ".remove(" + cacheKey + ") = " + removed;
      if (removed) {
        addActionMessage(message);
      } else {
        addActionError(message);
      }
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

    // populate stats for display
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
        String.valueOf(cache.isEternal()) + " ( "
          + String.valueOf(cache.getTimeToIdleSeconds()) + " / "
          + String.valueOf(cache.getTimeToLiveSeconds()) + " )",
        String.valueOf(cache.isOverflowToDisk()) + " / "
          + String.valueOf(cache.isDiskPersistent())
      });
    }

    return SUCCESS;
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
   * @param cacheAction The cacheAction to use.
   */
  public void setCacheAction(String cacheAction) {
    this.cacheAction = cacheAction;
  }

  /**
   * @param cacheName The cacheName to use.
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * @param cacheKey The cachekey to use.
   */
  public void setCacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
  }
}
