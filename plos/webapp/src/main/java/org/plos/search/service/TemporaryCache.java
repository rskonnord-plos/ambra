/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.ConcurrentModificationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mimic a Map and cache items for a specific limited time (or until the garbage collector
 * cleans up our SoftReferences).
 *
 * @author Eric Brown
 * @version $Id$
 */
public class TemporaryCache implements Runnable {
  private static final Log log = LogFactory.getLog(TemporaryCache.class);

  private static class Value {
    long lastAccess;
    Object data;
  }

  private long expiration;
  // TODO: Does this really need to be syncronized?
  private Map cache = Collections.synchronizedMap(new HashMap());

  /**
   * Create a temporary cache where items will live for the specified number of milli-seconds.
   *
   * @param expiration The number of milli-seconds an item should live in the cache.
   */
  public TemporaryCache(long expiration) {
    Thread thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Return an object from the cache (if it hasn't expired).
   *
   * @param key The key for the object being retrieved.
   * @return The cached object or null if the item is not found.
   */
  public synchronized Object get(Object key) {
    SoftReference ref = (SoftReference) cache.get(key);
    if (ref != null) {
      Value value = (Value) ref.get();
      long now = System.currentTimeMillis();
      if (value != null && (now - value.lastAccess < expiration)) {
        if (log.isDebugEnabled())
          log.debug(key + ": Hit " + (now - value.lastAccess) + "ms");
        value.lastAccess = now;
        return value.data;
      } else if (log.isDebugEnabled())
        log.debug(key + ": Miss " + (value == null ? "Empty Reference" : "Expired"));
    }

    return null;
  }

  /**
   * Add an item to the cache. It will be added via a SoftReference and expire based on
   * the expiration configured in the constructor.
   *
   * @param key The key to add the object for.
   * @param data The actual item to add.
   */
  public synchronized void put(Object key, Object data) {
    Value value = new Value();
    value.lastAccess = System.currentTimeMillis();
    value.data = data;
    SoftReference ref = new SoftReference(value);
    cache.put(key, ref);
  }

  /**
   * The cleanup thread. It will run every "expiration" milli-seconds and purge any items
   * in the cache that haven't been accessed in the past "expiration" milli-seconds.
   */
  public void run() {
    try {
      Thread.sleep(expiration);
    } catch (InterruptedException ie) {
      log.debug("Cache cleaner exiting");
      return; // we must be exiting -- no big deal, it is all SoftReferences anyway
    }

    long now = System.currentTimeMillis();

    for (int i = 0; i < 20; i++) {
      try {
        for (Iterator iter = cache.keySet().iterator(); iter.hasNext(); ) {
          SoftReference ref = (SoftReference) iter.next();
          Value value = (Value) ref.get();
          if (value == null)
            cache.remove(ref);
          else if (now - value.lastAccess > expiration)
            cache.remove(ref);
        }

        break;
      } catch (ConcurrentModificationException cme) {
        log.debug("Cache modified during purge, restarting purge. Try " + i, cme);
      }
    }
  }
}
