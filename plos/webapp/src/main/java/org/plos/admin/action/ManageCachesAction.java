/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;

import org.springframework.beans.factory.annotation.Required;

/**
 * Display Cache statistics.
 *
 * @author jsuttor
 */
public class ManageCachesAction extends BaseActionSupport {

  private SortedMap<String, String[]> cacheStats = new TreeMap<String, String[]>();

  private CacheManager cacheManager;
  private String cacheAction;
  private String cacheName;
  private List   cacheKeys = new ArrayList();

  private static final Log log = LogFactory.getLog(ManageCachesAction.class);

  @Override
  public String execute() throws Exception {

    // any actions?
    if ("removeAll".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cache.removeAll();
      addActionMessage(cacheName + ".removeAll() executed.");
    }
    if ("clearStatistics".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cache.clearStatistics();
      addActionMessage(cacheName + ".clearStatistics() executed.");
    }
    if ("getKeys".equals(cacheAction) && cacheName != null) {
      Ehcache cache = cacheManager.getEhcache(cacheName);
      cacheKeys = cache.getKeysNoDuplicateCheck();
      Collections.sort(cacheKeys);
      addActionMessage(cacheName + ".getKeysNoDuplicateCheck() executed.");
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
}
