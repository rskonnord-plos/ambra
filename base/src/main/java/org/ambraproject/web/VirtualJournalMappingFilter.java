/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.web;

import java.io.IOException;
import java.io.File;

import java.net.MalformedURLException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.Configuration;
import org.topazproject.ambra.configuration.ConfigurationStore;

/**
 * A Filter that maps incoming URI Requests to an appropriate virtual journal resources. If a
 * virtual journal context is set, a lookup is done to see if an override for the requested
 * resource exists for the virtual journal.  If so, the Request is wrapped with the override
 * values and passed on to the FilterChain.  If not, the Request is wrapped with default values
 * for the resource and then passed to the FilterChain.
 */
public class VirtualJournalMappingFilter implements Filter {
  private static final Logger log            = LoggerFactory.getLogger(VirtualJournalMappingFilter.class);
  private ServletContext   servletContext = null;
  private Configuration configuration = null;

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {
    // need ServletContext to get "real" path/file names
    this.servletContext = filterConfig.getServletContext();
    this.configuration = ConfigurationStore.getInstance().getConfiguration();
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {

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
  HttpServletRequest mapRequest(HttpServletRequest request)
                                 throws ServletException {
    VirtualJournalContext context =
      (VirtualJournalContext) request.getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    if (context == null)
      return request;
    return context.mapRequest(request, configuration, servletContext);
  }
}
