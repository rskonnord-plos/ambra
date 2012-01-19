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
package org.plos.cache;

/**
 * Listener for cache update events. These events are fired as the
 * 'local' cache is updated and <b>not</b> when the transaction is completed and
 * the backing cache is updated.
 *
 * @author Pradeep Krishnan
 */
public interface CacheListener extends Listener {
  /**
   * Fired when an object is added to the local cache
   *
   * @param cache the cache
   * @param key the key
   * @param val the value
   */
  void cacheEntryChanged(Cache cache, Object key, Object val);

  /**
   * Fired when an object is removed from the local cache.
   *
   * @param cache the cache
   * @param key the key
   */
  void cacheEntryRemoved(Cache cache, Object key);

  /**
   * Fired when all objects are removed from the local cache.
   *
   * @param cache the cache
   */
  void cacheCleared(Cache cache);
}
