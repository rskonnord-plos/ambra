/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.webwork;

import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Custom WebWork ActionMapper.
 *
 * Map friendly URIs, e.g. "/article/feed" to WebWork actions w/o WebWorks URIs, "/article/articleFeed.action?parms"
 *
 * @author Jeff Suttor
 *
 */
public class PlosOneActionMapper extends DefaultActionMapper {

  private static final Log log = LogFactory.getLog(PlosOneActionMapper.class);

  /**
   * @see DefaultActionManager#getMapping(HttpServletRequest request).
   */
  public ActionMapping getMapping(HttpServletRequest request) {

    // filtering is driven by the request URI
    URI uri;
    try {
      uri = new URI(request.getRequestURI());
    } catch (URISyntaxException ex) {
      // should never happen
      log.error(ex);
      throw new RuntimeException(ex);
    }

    // do not care about "null"
    if (uri.getPath() == null) {
      return super.getMapping(request);
    }

    // only care about "/article/feed"
    if (uri.getPath().startsWith("/article/feed")) {
      return mapUriToAction(uri);
    }

    // use default
    return super.getMapping(request);
  }

  /**
   * @see DefaultActionManager#getUriFromActionMapping(ActionMapping mapping).
   */
  public String getUriFromActionMapping(ActionMapping mapping) {

    // only care about /article/feed
    if ("getFeed".equals(mapping.getName())
      && "/article/feed".equals(mapping.getNamespace())
      && "execute".equals(mapping.getMethod())) {
      return("/article/feed");
    }

    // use default
    return super.getUriFromActionMapping(mapping);
 }

  /**
   * Map URIs that start with /article/feed to the getFeed action.
   *
   * @return ActionMapping for getFeed.
   */
 private ActionMapping mapUriToAction(URI uri) {

   // TODO: possible to use common config?
   HashMap<String, String> parms = new HashMap();
   parms.put("feedName", "wireFeed");  // parms passed as null, for now

   return new ActionMapping(
     "getFeed",                              // name
     "/article/feed",                        // namespace
     "execute",                              // method
     null);                                  // parms
 }
}
