/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xml.transform;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.ref.SoftReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Look up the URL in an in-memory cache. If the URL is not cached, then look it up
 * via the delegate and store it in the cache for later use. Cached entities are
 * <code>SoftReference</code>s such that the garbage collector can re-claim them if
 * memory is low.
 *
 * @author Ronald Tschalär
 * @version $Id$
 */
public class MemoryCacheURLRetriever implements URLRetriever {
  private static final Log   log = LogFactory.getLog(MemoryCacheURLRetriever.class);
  private final Map          cache = new HashMap();
  private final URLRetriever delegate;

  /**
   * Create a <code>URLRetriever</code> that will cache content fetched by its delegate.
   *
   * @param delegate the <code>URLRetriever</code> to use on a cache-miss.
   */
  public MemoryCacheURLRetriever(URLRetriever delegate) {
    this.delegate = delegate;
  }

  /**
   * Lookup the <code>url</code> in the cache. If found, return results. Otherwise, call
   * delegate to fetch content.
   * 
   * @param url the url to retrieve
   * @return the contents, or null if not found
   * @throws IOException if an error occurred retrieving the contents (other than not-found)
   */
  public synchronized byte[] retrieve(String url) throws IOException {
    SoftReference ref = (SoftReference) cache.get(url);
    byte[] res = (ref != null) ? (byte[]) ref.get() : null;

    if (log.isDebugEnabled())
      log.debug("Memory cache('" + url + "'): " +
                (res != null ? "found" : ref != null ? "expired" : "not found"));

    if (res != null || delegate == null)
      return res;

    res = delegate.retrieve(url);
    if (res == null)
      return null;

    if (log.isDebugEnabled())
      log.debug("Caching '" + url + "'");

    cache.put(url, new SoftReference(res));
    return res;
  }
}
