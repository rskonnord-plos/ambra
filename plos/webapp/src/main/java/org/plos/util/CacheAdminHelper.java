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

      // as Objects are in the Cache, get by Object value (PLoS models are not Serializable)
      return (T) e.getObjectValue();
    }
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
