/* $HeadURL$
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
package org.topazproject.ambra.article.service;

import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.cache.AbstractObjectListener;

import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to provide small blobs from cache or from blob store. This has been added to
 * improve the render performance where the article contains a lot of small images.
 *
 * @author Dragisa Krsmanovic
 */
public class SmallBlobService {
  private Cache smallBlobCache;
  private Invalidator invalidator;

  /**
   * Spring setter method to inject small objects cache
   *
   * @param smallBlobCache Small blob cache
   */
  @Required
  public void setSmallBlobCache(Cache smallBlobCache) {
    this.smallBlobCache = smallBlobCache;

    if (invalidator == null)
      smallBlobCache.getCacheManager().registerListener(invalidator = new Invalidator());
  }

  @Transactional(readOnly = true)
  public byte[] getSmallBlob(final Representation representation) throws Exception {
    String lock = representation.getId().intern();

    // Small blob. Look in the cache first
    return smallBlobCache.get(representation.getId(), -1,
        new Cache.SynchronizedLookup<byte[], Exception>(lock) {
          public byte[] lookup() {
            return representation.getBody().readAll();
          }
        });
  }

  /**
   * Invalidate smallBlobCache if representation objects are deleted. This is accomplished
   * via a listener registered with the small blob cache.
   */
  public class Invalidator extends AbstractObjectListener {
    /**
     * Notify that a Representation is being removed.
     *
     * @param session session info (unused)
     * @param cm      class metadata (unused)
     * @param id      (unused)
     * @param object  must be <code>class Representation</code>
     * @throws Exception
     */
    @Override
    public void removing(Session session, ClassMetadata cm, String id, Object object)
        throws Exception {

      if (object instanceof Representation) {
        smallBlobCache.remove(((Representation)object).getId());
      }
    }
  }
}
