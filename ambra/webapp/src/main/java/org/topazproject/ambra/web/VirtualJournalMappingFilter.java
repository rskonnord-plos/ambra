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
package org.topazproject.ambra.web;

import java.io.IOException;
import java.io.Serializable;

import java.net.MalformedURLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * A Filter that maps incoming URI Requests to an appropriate virtual journal resources. If a
 * virtual journal context is set, a lookup is done to see if an override for the requested
 * resource exists for the virtual journal.  If so, the Request is wrapped with the override
 * values and passed on to the FilterChain.  If not, the Request is wrapped with default values
 * for the resource and then passed to the FilterChain.
 */
public class VirtualJournalMappingFilter implements Filter {
  private static final Log log            = LogFactory.getLog(VirtualJournalMappingFilter.class);
  private ServletContext   servletContext = null;
  private Ehcache          cache          = null;

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {
    // need ServletContext to get "real" path/file names
    servletContext = filterConfig.getServletContext();

    try {
      CacheManager cacheManager = CacheManager.getInstance();
      cache = cacheManager.getEhcache("VirtualJournalMappingFilter");
    } catch (CacheException ce) {
      throw new ServletException("Error getting cache-manager", ce);
    } catch (IllegalStateException ise) {
      throw new ServletException("Error getting cache", ise);
    }

    if (cache == null)
      throw new ServletException("No cache configuration found for VirtualJournalMappingFilter");
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {
    CacheManager.getInstance().shutdown();
  }

  /*
   * @see javax.servlet.Filter#doFilter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest r      = (HttpServletRequest) request;
      HttpServletRequest mapped = mapRequest(r);

      if (log.isDebugEnabled()) {
        if (mapped == r)
          log.debug("Passed thru unchanged " + r.getRequestURI());
        else
          log.debug("Mapped " + r.getRequestURI() + " to " + mapped.getRequestURI());
      }

      request = mapped;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Map the request to a resource in the journal context.. If resource exists within the
   * virtual journal context, return a WrappedRequest with the mappingPrefix,  else return a
   * WrappedRequest for the default resource.
   *
   * @param request <code>HttpServletRequest</code> to apply the lookup against.
   *
   * @return WrappedRequest for the resource.
   *
   * @throws ServletException on an error
   */
  private HttpServletRequest mapRequest(HttpServletRequest request)
                                 throws ServletException {
    VirtualJournalContext context =
      (VirtualJournalContext) request.getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);

    if (context == null)
      return request;

    String journal = context.getJournal();

    if (journal == null)
      return request;

    String mappingPrefix = context.getMappingPrefix();

    if ((mappingPrefix == null) || (mappingPrefix.length() == 0))
      return request;

    String   key     = journal + "-" + request.getRequestURI();
    Element  element = cache.get(key);
    String[] mapped;

    if (element != null)
      mapped = ((CachedValue) element.getObjectValue()).getMapped();
    else {
      String cp      = request.getContextPath();
      String sp      = request.getServletPath();
      String pi      = request.getPathInfo();

      mapped         = getMappedPaths(context.virtualizeUri(cp, sp, pi));

      if (mapped == null)
        mapped = getMappedPaths(context.defaultUri(cp, sp, pi));

      if ((mapped != null) && mapped[3].equals(request.getRequestURI()))
        mapped = null;

      cache.put(new Element(key, new CachedValue(mapped)));
    }

    return (mapped == null) ? request : wrapRequest(request, mapped);
  }

  private String[] getMappedPaths(String[] paths)
                           throws ServletException {
    String resource = paths[3].substring(paths[0].length());

    if (resourceExists(resource))
      return paths;

    String path = "/struts" + resource;

    if (Thread.currentThread().getContextClassLoader().getResource("struts" + resource) != null)
      return new String[] { paths[0], "", path, paths[0] + path };

    return null;
  }

  private boolean resourceExists(String resource) throws ServletException {
    try {
      return servletContext.getResource(resource) != null;
    } catch (MalformedURLException mre) {
      throw new ServletException("Invalid resource path: " + resource, mre);
    }
  }

  private static class CachedValue implements Serializable {
    private String[] mapped;

    public CachedValue(String[] mapped) {
      this.mapped = mapped;
    }

    public String[] getMapped() {
      return mapped;
    }

    public String toString() {
      return (mapped != null) ? mapped[3] : "Not Mapped";
    }
  }

  /**
   * Wrap an HttpServletRequest with arbitrary URI values.
   *
   * @param request the request to wrap
   * @param paths the paths to substitute
   *
   * @return the wrapped request instance
   *
   * @throws IllegalArgumentException DOCUMENT ME!
   */
  public static HttpServletRequest wrapRequest(final HttpServletRequest request,
                                               final String[] paths) {
    if ((paths == null) || (paths.length != 4))
      throw new IllegalArgumentException("Invalid path list");

    return new HttpServletRequestWrapper(request) {
        public String getRequestURI() {
          return paths[3];
        }

        public String getContextPath() {
          return paths[0];
        }

        public String getServletPath() {
          return paths[1];
        }

        public String getPathInfo() {
          return paths[2];
        }
      };
  }
}
