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

import java.util.Properties;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Look up the URL in a resource cache.
 *
 * @author Ronald Tschalär
 * @version $Id$
 */
public class ResourceURLRetriever implements URLRetriever {
  private static final Log   log = LogFactory.getLog(ResourceURLRetriever.class);

  private final URLRetriever delegate;
  private final Properties   urlMap;
  private final String       relativePrefix;
  private final Class        resourceLoader;

  public ResourceURLRetriever(URLRetriever delegate, Properties urlMap, String relativePrefix) {
    this.delegate = delegate;
    this.urlMap = urlMap;
    this.relativePrefix = relativePrefix;
    this.resourceLoader = ResourceURLRetriever.class;
  }

  public ResourceURLRetriever(URLRetriever delegate, Properties urlMap, Class resourceLoader) {
    this.delegate = delegate;
    this.urlMap = urlMap;
    this.relativePrefix = null;
    this.resourceLoader = resourceLoader;
  }

  public byte[] retrieve(String url) throws IOException {
    String resource = urlMap.getProperty(url);

    if (log.isDebugEnabled())
      log.debug("Resource retriever ('" + url + "'): " +
                (resource != null ? "found" : "not found"));

    if (resource == null)
      return (delegate != null) ? delegate.retrieve(url) : null;

    // Deal with relative prefixes
    if (relativePrefix != null && !resource.startsWith("/"))
      resource = relativePrefix + resource;
    
    return IOUtils.toByteArray(resourceLoader.getResourceAsStream(resource));
  }
}
