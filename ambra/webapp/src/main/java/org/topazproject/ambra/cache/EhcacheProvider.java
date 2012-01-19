/* $HeadURL::                                                                            $
 * $Id$
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
  private static final Log   log          = LogFactory.getLog(EhcacheProvider.class);
  private final CacheManager cacheManager;
  private final Ehcache      cache;
  private final String       name;

  /**
   * Creates a new EhcacheProvider object.
   *
   * @param cache the ehcache object
   */
  public EhcacheProvider(CacheManager cacheManager, Ehcache cache) {
    this.cache                            = cache;
    this.cacheManager                     = cacheManager;
    this.name                             = cache.getName();
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

  /*
   * inherited javadoc
   */
  public CachedItem rawGet(Object key) {
    TxnContext ctx = cacheManager.getTxnContext();
    CachedItem val = ctx.getLocal(getName()).get(key);

    if ((val == null) && ctx.getRemovedAll(getName()))
      val = DELETED;

    if (val == null) {
      Element e = cache.get(key);

      if (e != null)
        val = (Item) e.getValue();
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

                if (val == null)
                  val = new Item(lookup.lookup());

                Element e = new Element(key, val);

                if (refresh > 0)
                  e.setTimeToLive(refresh);

                cache.put(e);

                if (log.isDebugEnabled() && (val.getValue() == null))
                  log.debug(getName() + ".populate-null(" + key + ")");
                else if (log.isDebugEnabled())
                  log.debug(getName() + ".populate(" + key + ")");

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
  public void put(final Object key, final Object val) {
    TxnContext ctx = cacheManager.getTxnContext();
    ctx.getLocal(getName()).put(key, new Item(val));
    ctx.enqueue(new CacheEvent() {
        public void execute(TxnContext ctx, boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(ctx, key);
        }
      });
    cacheManager.cacheEntryChanged(this, key, val);
  }

  /*
   * inherited javadoc
   */
  public void remove(final Object key) {
    TxnContext ctx = cacheManager.getTxnContext();
    ctx.getLocal(getName()).put(key, DELETED);
    ctx.enqueue(new CacheEvent() {
        public void execute(TxnContext ctx, boolean commit) {
          if (commit)
            EhcacheProvider.this.commit(ctx, key);
        }
      });
    cacheManager.cacheEntryRemoved(this, key);
  }

  /*
   * inherited javadoc
   */
  public void removeAll() {
    TxnContext ctx = cacheManager.getTxnContext();
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

  /*
   * inherited javadoc
   */
  public Set<?> getKeys() {
    TxnContext              ctx        = cacheManager.getTxnContext();
    Map<Object, CachedItem> local      = ctx.getLocal(getName());
    boolean                 removedAll = ctx.getRemovedAll(getName());
    Set                     keys       = removedAll ? new HashSet() : new HashSet(cache.getKeys());

    for (Object key : local.keySet()) {
      if (DELETED.equals(local.get(key)))
        keys.remove(key);
      else
        keys.add(key);
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

  /*
   * inherited javadoc
   */
  public void rawPut(Object key, CachedItem val) {
    if (DELETED.equals(val)) {
      cache.remove(key);

      if (log.isDebugEnabled())
        log.debug(getName() + ".remove(" + key + ")");
    } else if (val instanceof Item) {
      cache.put(new Element(key, val));


      if (log.isDebugEnabled() && (((Item)val).getValue() == null))
        log.debug(getName() + ".put-null(" + key + ")");
      else if (log.isDebugEnabled())
        log.debug(getName() + ".put(" + key + ")");
    } else
      throw new IllegalArgumentException("Expecting an 'Item' for " + key + " instead found " + val);
  }
}
