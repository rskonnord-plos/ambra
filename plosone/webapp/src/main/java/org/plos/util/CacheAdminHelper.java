/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.util;

import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper to deal with cache administration.
 *
 * <p>Example usage:
 * <pre>
 *    Foo f =
 *      CacheAdminHelper.getFromCache(cache, fooId, null, "some foo",
 *                                    new CacheAdminHelper.CacheUpdater<Foo>() {
 *        public Foo lookup(boolean[] updated) {
 *          Foo res;
 *          try {
 *            res = getFoo();
 *            updated[0] = true;
 *          } catch (BlahException be) {
 *            res = null;
 *          }
 *          return res;
 *        }
 *      });
 * </pre>
 *
 * @author Ronald Tschalär
 */
public class CacheAdminHelper {
  private static Log log = LogFactory.getLog(CacheAdminHelper.class);

  /** 
   * Not meant to be instantiated. 
   */
  private CacheAdminHelper() {
  }

  /** 
   * Look up a value in the cache, updating the cache with a new value if not found or if the old
   * value expired.
   * 
   * @param cache   the cache to look up the value in
   * @param key     the key to use for the lookup
   * @param refresh the max-age of entries in the cache, or -1 for indefinite
   * @param groups  the groups to store the new value under (if necessary)
   * @param desc    a short description of the object being retrieved (for logging only)
   * @param updater the updater to call to get the value if not found in the cache
   * @return the value in the cache, or returned by the <var>updater</var> if not in the cache
   */
  public static <T> T getFromCache(GeneralCacheAdministrator cache, String key, int refresh,
                                   String[] groups, String desc, CacheUpdater<T> updater) {
    T res;
    try {
      // Get from the cache
      res = (T) cache.getFromCache(key, (refresh >= 0) ? refresh : CacheEntry.INDEFINITE_EXPIRY);
      if (log.isDebugEnabled()) {
        log.debug("retrieved " + desc + " from cache");
      }
    } catch (NeedsRefreshException nre) {
      if (log.isDebugEnabled())
        log.debug("retrieving " + desc + " from db", nre);

      boolean[] updated = new boolean[] { false };
      try {
        //  Get the value from TOPAZ
        res = updater.lookup(updated);

        // Store in the cache
        if (updated[0]) {
          if (groups != null)
            cache.putInCache(key, res, groups);
          else
            cache.putInCache(key, res);
        }
      } finally {
        if (!updated[0]) {
            // It is essential that cancelUpdate is called if the
            // cached content could not be rebuilt
            cache.cancelUpdate(key);
        }
      }
    }

    return res;
  }

  /** 
   * Look up a value in the cache, updating the cache with a new value if not found or if the old
   * value expired.
   * 
   * @param cache   the cache to look up the value in
   * @param key     the key to use for the lookup
   * @param refresh the max-age of entries in the cache (in seconds), or -1 for indefinite
   * @param lock    the object to synchronized on
   * @param desc    a short description of the object being retrieved (for logging only)
   * @param updater the updater to call to get the value if not found in the cache; may be null
   * @return the value in the cache, or returned by the <var>updater</var> if not in the cache
   */
  public static <T> T getFromCache(Ehcache cache, String key, int refresh, Object lock,
                                   String desc, EhcacheUpdater<T> updater) {
    synchronized (lock) {
      Element e = cache.get(key);

      if (e == null) {
        if (updater == null)
          return null;

        if (log.isDebugEnabled())
          log.debug("retrieving " + desc + " from db");

        e = new Element(key, updater.lookup());
        if (refresh > 0)
          e.setTimeToLive(refresh);

        cache.put(e);
      } else if (log.isDebugEnabled()) {
        log.debug("retrieved " + desc + " from cache");
      }

      return (T) e.getValue();
    }
  }

  /**
   * The interface updaters must implement.
   */
  public static interface CacheUpdater<T> {
    /** 
     * @param updated (output) set <var>updated[0]</var> to true if the lookup/retrieval was
     *                successful
     * @return the value to return; if <var>updated[0]</var> was set to true then this value will
     *         be put into the cache
     */
    T lookup(boolean[] updated);
  }

  /**
   * The interface Ehcache updaters must implement.
   */
  public static interface EhcacheUpdater<T> {
    /** 
     * @return the value to return; this value will be put into the cache
     */
    T lookup();
  }
}
