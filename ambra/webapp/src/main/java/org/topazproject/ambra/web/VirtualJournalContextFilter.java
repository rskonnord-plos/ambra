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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;

import org.topazproject.ambra.configuration.ConfigurationStore;

/**
 * A Filter that sets the {@link VirtualJournalContext} as an attribute in the ServletRequest.
 *
 * Application usage:
 * <pre>
 * VirtualJournalContext requestContent = ServletRequest.getAttribute(PUB_VIRTUALJOURNAL_CONTEXT);
 * String requestJournal = requestContext.getJournal();
 * </pre>
 *
 * See WEB-INF/classes/ambra/configuration/defaults.xml for configuration examples.
 */
public class VirtualJournalContextFilter implements Filter {

  public static final String CONF_VIRTUALJOURNALS          = "ambra.virtualJournals";
  public static final String CONF_VIRTUALJOURNALS_DEFAULT  = CONF_VIRTUALJOURNALS + ".default";
  public static final String CONF_VIRTUALJOURNALS_JOURNALS = CONF_VIRTUALJOURNALS + ".journals";

  /** Allow setting/overriding the virtual journal as a URI param. */
  // public static final String URI_PARAM_VIRTUALJOURNAL = "virtualJournal";
  /** Allow setting/overriding the mapping prefix as a URI param. */
  // public static final String URI_PARAM_MAPPINGPREFIX = "mappingPrefix";

  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static final Log log = LogFactory.getLog(VirtualJournalContextFilter.class);

  /*
   * @see javax.servlet.Filter#init
   */
  public void init(final FilterConfig filterConfig) throws ServletException {

    // settings & overrides are in the Configuration
    if (configuration == null) {
      // should never happen
      final String errorMessage = "No Configuration is available to set Virtual Journal context";
      log.error(errorMessage);
      throw new ServletException(errorMessage);
    }
  }

  /*
   * @see javax.servlet.Filter#destroy
   */
  public void destroy() {

    // nothing to do
  }

  /*
   * @see javax.servlet.Filter#doFilter
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final Collection<String> virtualJournals = configuration.getList(CONF_VIRTUALJOURNALS_JOURNALS);

    String journalName = null;
    String description    = null;
    String mappingPrefix  = null;

    // need to do <rule> based processing?
    if (journalName == null) {
      final VirtualJournalContext ruleValues = processVirtualJournalRules(
        configuration, (HttpServletRequest) request);
      if (ruleValues != null) {
        journalName = ruleValues.getJournal();
        description    = ruleValues.getDescription();
        mappingPrefix  = ruleValues.getMappingPrefix();
        if (journalName != null) {
          if (log.isTraceEnabled()) {
            log.trace("virtual journal from rules: journal = \"" + journalName + "\""
              + ", mappingPrefix = \"" + mappingPrefix + "\"");
          }
        }
      }
    }

    // was a simple config <default> specified?
    if (journalName == null) {
      journalName = configuration.getString(CONF_VIRTUALJOURNALS_DEFAULT + ".journal");
      description    = configuration.getString(CONF_VIRTUALJOURNALS_DEFAULT + ".description");
      mappingPrefix  = configuration.getString(CONF_VIRTUALJOURNALS_DEFAULT + ".mappingPrefix");

      if (log.isTraceEnabled()) {
        log.trace("virtual journal from defaults: journal = \"" + journalName + "\""
          + ", mappingPrefix = \"" + mappingPrefix + "\"");
      }
    }

    // use system default if not set
    if (journalName == null) {
      journalName = VirtualJournalContext.PUB_VIRTUALJOURNAL_DEFAULT_JOURNAL;
      description    = VirtualJournalContext.PUB_VIRTUALJOURNAL_DEFAULT_DESCRIPTION;
      mappingPrefix  = VirtualJournalContext.PUB_VIRTUALJOURNAL_DEFAULT_MAPPINGPREFIX;

      if (log.isTraceEnabled()) {
        log.trace("setting virtual journal = \"" + journalName + "\""
          + ", mappingPrefix = \"" + mappingPrefix + "\""
          + ", no <default> specified, no <rule>s match");
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("journal = \"" + journalName + "\"" 
          + ", mappingPrefix = \"" + mappingPrefix + "\" for " 
          + ((HttpServletRequest)request).getRequestURL());
    }

    // put virtualJournal context in the ServletRequest for webapp to use
    request.setAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT,
      new VirtualJournalContext(journalName, description, mappingPrefix, request.getScheme(),
        request.getServerPort(), request.getServerName(),
        ((HttpServletRequest) request).getContextPath(), virtualJournals));

    // establish a "Nested Diagnostic Context" for logging, e.g. prefix log entries w/journal name
    // http://logging.apache.org/log4j/docs/api/org/apache/log4j/NDC.html
    if (journalName != null) {
      NDC.push(journalName);
    }

    try {
      // continue the Filter chain ...
      filterChain.doFilter(request, response);
    } finally {
      // cleanup "Nested Diagnostic Context" for logging
      if (journalName != null) {
        NDC.pop();
        NDC.remove(); // TODO: appropriate place to cleanup for Thread?
      }
    }
  }

  /**
   * Process all &lt;${journal-name}&gt;&lt;rules&gt;&lt;${http-header-name}&gt;s looking for a match.
   *
   * @param configuration <code>Configuration</code> that contains the rules.
   * @param request <code>HttpServletRequest</code> to apply the rules against.
   * @ return VirtualJournalContext.  May be <code>null</code>.
   */
  private VirtualJournalContext processVirtualJournalRules(
    Configuration configuration, HttpServletRequest request) {

    String virtualJournal = null;
    String description    = null;
    String mappingPrefix  = null;

    // process all <virtualjournal><journals> entries looking for a match
    final List<String> journals = configuration.getList(CONF_VIRTUALJOURNALS_JOURNALS);
    final Iterator onJournal = journals.iterator();
    while(onJournal.hasNext()
      && virtualJournal == null) {
      final String journal = (String) onJournal.next();

      if (log.isTraceEnabled()) {
        log.trace("processing virtual journal: " + journal);
      }

      // get the <rules> for this journal
      final String rulesPrefix = CONF_VIRTUALJOURNALS + "." + journal + ".rules";
      final Iterator rules = configuration.getKeys(rulesPrefix);
      while (rules.hasNext()
        && virtualJournal == null) {
        final String rule       = (String) rules.next();
        final String httpHeader = rule.substring(rulesPrefix.length() + 1);
        final String httpValue  = configuration.getString(rule);

        if (log.isTraceEnabled()) {
          log.trace("processing rule: " + httpHeader + " = " + httpValue);
        }

        // test Request HTTP header value against match
        final String reqHttpValue = request.getHeader(httpHeader);
        if (log.isTraceEnabled()) {
          log.trace("testing Request: " + httpHeader + "=" + reqHttpValue);
        }
        if (reqHttpValue == null) {
          if (httpValue == null) {
            virtualJournal = journal;
            description = configuration.getString(CONF_VIRTUALJOURNALS + "." + journal
                    + ".description");
            mappingPrefix = configuration.getString(CONF_VIRTUALJOURNALS + "." + journal + ".mappingPrefix");
            break;
          }
          continue;
        }

        if (reqHttpValue.matches(httpValue)) {
          virtualJournal = journal;
          description = configuration.getString(CONF_VIRTUALJOURNALS + "." + journal
                  + ".description");
          mappingPrefix = configuration.getString(CONF_VIRTUALJOURNALS + "." + journal + ".mappingPrefix");
          break;
        }
      }
    }

    // return match or null
    return new VirtualJournalContext(virtualJournal, description, mappingPrefix, null, 0, null,
            null, null);
  }
}
