/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.cache.CacheManager.CacheEvent;
import org.topazproject.ambra.cache.CacheManager.TxnContext;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * A cache implementation using Ehcache.
 *
 * @author Pradeep Krishnan
 */
public class EhcacheProvider implements Cache {
  private static final Log   log = LogFactory.getLog(EhcacheProvider.class);

  private final CacheManager cacheManager;
  private final Ehcache      cache;
  private final String       name;
  private final Boolean      allowNulls;

  /**
   * Creates a new EhcacheProvider object.
   *
   * @param cache the ehcache object
   */
  public EhcacheProvider(CacheManager cacheManager, Ehcache cache) {
    this.cache        = cache;
    this.cacheManager = cacheManager;
    this.name         = cache.getName();
    this.allowNulls   = true;
  }

  /**
   * Creates a new EhcacheProvider object.
   *
   * @param cache the ehcache object
   */
  public EhcacheProvider(CacheManager cacheManager, Ehcache cache, Boolean allowNulls) {
    this.cache        = cache;
    this.cacheManager = cacheManager;
    this.name         = cache.getName();
    this.allowNulls   = allowNulls;
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return name;
  }

  /*
   * inherited javadoc
   */
  public CacheManager getCacheManager() {
    return cacheManager;
  }

  /*
   * inherited javadoc
   */
  public Item get(Object key) {
    CachedItem val = rawGet(key);

    return DELETED.equals(val) ? null : (Item) val;
  }

  private CachedItem rawGet(Object key) {
    TxnContext ctx = cacheManager.getTxnContext();
    CachedItem val = ctx.getLocal(getName()).get(key);

    if ((val == null) && ctx.getRemovedAll(getName()))
      val = DELETED;

    if (val == null) {
      Element e = cache.get(key);

      /*
       * v0.9.2 and prior versions of Ambra used to store 'Item.class' in the underlying EhCache.
       * This is now changed to only store Item#getValue. The additional check below is to work
       * with those old entries during an upgrade since those entries could be coming from a disk
       * persisted cache or from an older peer.
       */
      if (e != null)
        val = (e.getObjectValue() instanceof Item) ? (Item) val : new Item(e.getObjectValue());
    }

    if (log.isTraceEnabled())
      log.trace("Cache " + ((val == null) ? "miss" : "hit") + " for '" + key + "' in " + getName());

    return val;
  }

  /*
   * inherited javadoc
   */
  public <T, E extends Exception> T get(final Object key, final int refresh,
                                        final Lookup<T, E> lookup)
                                 throws E {
    Item val = get(key);

    try {
      if ((val == null) && (lookup != null)) {
        val =
          lookup.execute(new Lookup.Operation() {
              public Item execute(boolean degraded) throws Exception {
                if (degraded)
                  log.warn("Degraded mode lookup for key '" + key + "' in cache '"
                           + EhcacheProvider.this.getName() + "'");

                Item val = get(key);

                if (val == null) {
                  Object o = lookup.lookup();
                  val = new Item(o, refresh);
                  if (allowNulls || (o != null))
                    put(key, val);
                  else
                    if (log.isWarnEnabled())
                      log.warn("Cache request to save null when allowNulls = false. '" +
                          key + "' in cache '" + EhcacheProvider.this.getName() + "'",
                          new Exception());
                }
                return val;
              }
            });
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw (E) e;
    }

    return (val == null) ? null : (T) val.getValue();
  }

  /*
   * inherited javadoc
   */
  public void put(final Object key, final Item val) {
    schedulePut(key, val);
    cacheManager.cacheEntryChanged(this, key, val);
  }

  /*
   * inherited javadoc
   */
  public void remove(final Object key) {
    schedulePut(key, DELETED);
    cacheManager.cacheEntryRemoved(this, key);
  }

  private void schedulePut(final Object key, CachedItem item) {
    TxnContext ctx = cacheManager.getTxnContext();
    if (ctx.isRollbackOnly())
      rawPut(key, item);
    else {
      ctx.getLocal(getName()).put(key, item);
      ctx.enqueue(new CacheEvent() {
          public void execute(TxnContext ctx, boolean commit) {
            if (commit)
              EhcacheProvider.this.commit(ctx, key);
          }
        });
    }
  }

  /*
   * inherited javadoc
   */
  public void removeAll() {
    TxnContext ctx = cacheManager.getTxnContext();
    if (ctx.isRollbackOnly())
      commitRemoveAll(ctx);
    else {
      ctx.getLocal(getName()).clear();
      ctx.setRemovedAll(getName(), true);
      ctx.enqueueHead(new CacheEvent() {
          public void execute(TxnContext ctx, boolean commit) {
            if (commit)
              EhcacheProvider.this.commitRemoveAll(ctx);
          }
        });
      cacheManager.cacheCleared(this);
    }
  }

  /*
   * inherited javadoc
   */
  public Set<?> getKeys() {
    TxnContext              ctx        = cacheManager.getTxnContext();
    Map<Object, CachedItem> local      = ctx.getLocal(getName());
    boolean                 removedAll = ctx.getRemovedAll(getName());
    Set                     keys       = removedAll ? new HashSet() : new HashSet(cache.getKeys());

    for (Map.Entry<Object, CachedItem> entry : local.entrySet()) {
      if (DELETED.equals(entry.getValue()))
        keys.remove(entry.getKey());
      else
        keys.add(entry.getKey());
    }

    return keys;
  }

  private void commitRemoveAll(TxnContext ctx) {
    cache.removeAll();
    if (log.isDebugEnabled())
      log.debug(getName() + ".removeAll()");
  }

  private void commit(TxnContext ctx, Object key) {
    Map<Object, CachedItem> local = ctx.getLocal(getName());
    CachedItem              val   = local.get(key);

    if (val != null)
      rawPut(key, val);

    local.remove(key);
  }

  private void rawPut(Object key, CachedItem val) {
    if (DELETED.equals(val)) {
      cache.remove(key);

      if (log.isDebugEnabled())
        log.debug(getName() + ".remove(" + key + ")");
    } else if (val instanceof Item) {
      Item item = (Item) val;
      Element e = new Element(key, item.getValue());
      if (item.getTtl() > 0)
        e.setTimeToLive(item.getTtl());

      cache.put(e);

      if (log.isDebugEnabled() && (item.getValue() == null))
        log.debug(getName() + ".put-null(" + key + ")");
      else if (log.isDebugEnabled())
        log.debug(getName() + ".put(" + key + ")");
    } else
      throw new IllegalArgumentException("Expecting an 'Item' for " + key + " instead found " + val);
  }
}
