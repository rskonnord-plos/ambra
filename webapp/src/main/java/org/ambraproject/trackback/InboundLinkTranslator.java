/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
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

package org.ambraproject.trackback;

import org.ambraproject.models.Journal;
import org.ambraproject.util.UriUtil;
import org.apache.commons.configuration.Configuration;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * An instance of this class translates links into DOIs according to the rules for a particular DOI resolver or journal
 * domain.
 *
 * @author Ryan Skonnord
 */
public abstract class InboundLinkTranslator {

  protected static final String WWW = "www.";
  protected static final String DEFAULT_DOI_SCHEME = "info:doi/";

  protected static final String LOCAL_RESOLVER_KEY = "ambra.services.crossref.plos.doiurl";
  protected static final String ARTICLE_ACTION_KEY = "ambra.platform.articleAction";
  protected static final String JOURNAL_HOST_FORMAT = "ambra.virtualJournals.%s.url";

  protected final String hostname;
  protected final boolean acceptWww;

  protected static String getHostnameForJournal(String journalKey, Configuration configuration) {
    String journalUrl = configuration.getString(String.format(JOURNAL_HOST_FORMAT, journalKey));
    try {
      return new URL(journalUrl).getHost();
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Accessed by public static factory methods.
   * <p/>
   * The {@code hostname} argument must be lowercase and not have a leading "www." regardless of {@code acceptWww}. If
   * the hostname is null, the new object will treat any hostname in an input link as valid. A null hostname should be
   * passed in only by {@link #forAnyJournal}.
   *
   * @param hostname  the only valid hostname
   * @param acceptWww if {@code true}, ignore leading "www." on input hostnames
   */
  private InboundLinkTranslator(String hostname, boolean acceptWww) {
    this.hostname = hostname;
    this.acceptWww = acceptWww;
  }

  /**
   * If the link refers to a DOI-identified resource on the domain that this object represents, return the DOI. Such a
   * return value <em>can</em> be the DOI of an object in this system but is not necessarily. Null means that the link
   * either does not refer to this object's domain or does not contain a DOI in a valid syntax.
   *
   * @return the DOI to which the link points, or {@code null} if the link does not indicate a DOI validly for this
   *         object's domain
   */
  public final String getDoi(URL link) {
    if (!"http".equals(link.getProtocol())) {
      // Links to articles can only be in HTTP
      // This will need to be updated if articles are ever served on other protocols such as HTTPS
      return null;
    }

    if (hostname != null && !validateHostname(link)) {
      return null;
    }

    String decodedPath = UriUtil.decodeUtf8(link.getPath());
    if (decodedPath.length() <= 1) {
      return null;
    }
    decodedPath = decodedPath.substring(1); // strip leading slash
    String doiCandidate = getDoiFromPath(decodedPath);
    if (doiCandidate == null) {
      return null;
    }

    try {
      URI uri = new URI(doiCandidate);
      if (uri.getScheme() == null) {
        return null; // All DOIs used as article keys are prefixed with a URI scheme
      }
    } catch (URISyntaxException e) {
      return null; // By definition, all valid DOIs are valid URIs
    }
    return doiCandidate;
  }

  /**
   * Parse a DOI from a link's path.
   * <p/>
   * Return {@code null} if a DOI can't be found or if the link is definitely invalid. This method must not return a
   * valid DOI if the link wouldn't take a browser to that article. However, it is safe (but less efficient) to return
   * an invalid DOI which will eventually fail to match an article.
   *
   * @param path a URL path with the leading slash removed
   * @return a DOI that can be used as an article key (meaning it should have a scheme, usually {@code info:doi/}), or
   *         null if the path is invalid
   */
  protected abstract String getDoiFromPath(String path);

  /**
   * Check whether the host of the link matches this object's expected host, ignoring a "www." prefix if this object
   * says so. Hostnames are specified to be lowercase and browsers generally treat them as case-insensitive, so the
   * comparison is case-insensitive.
   *
   * @param link the URL to check
   * @return whether the URL's host matches
   */
  protected boolean validateHostname(URL link) {
    String linkHost = link.getHost();
    if (linkHost == null) {
      return false;
    }
    if (!acceptWww) {
      return linkHost.equalsIgnoreCase(hostname);
    }
    boolean linkHostHasWww = WWW.regionMatches(true, 0, linkHost, 0, WWW.length());
    int linkHostOffset = linkHostHasWww ? WWW.length() : 0;
    int length = linkHost.length() - linkHostOffset;
    if (length != hostname.length()) {
      return false;
    }
    return hostname.regionMatches(true, 0, linkHost, linkHostOffset, length);
  }


  /**
   * Reflects behavior of the public DOI resolver at {@code http://dx.doi.org/}.
   */
  public static final InboundLinkTranslator GLOBAL_RESOLVER = new InboundLinkTranslator("dx.doi.org", false) {
    @Override
    protected String getDoiFromPath(String path) {
      return path.startsWith(DEFAULT_DOI_SCHEME) ? path : DEFAULT_DOI_SCHEME + path;
    }
  };

  /**
   * Construct a translator to reflect behavior of this Ambra instance's DOI resolver.
   *
   * @param configuration the local configuration
   * @return the translator object
   */
  public static InboundLinkTranslator forLocalResolver(Configuration configuration) {
    URL resolverUrl;
    try {
      resolverUrl = new URL(configuration.getString(LOCAL_RESOLVER_KEY));
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
    final String rootPath = resolverUrl.getPath().substring(1); // strip leading slash

    return new InboundLinkTranslator(resolverUrl.getHost(), false) {
      @Override
      protected String getDoiFromPath(String path) {
        if (path.startsWith(rootPath)) {
          path = path.substring(rootPath.length());
        }

        // Expect "info:doi/" *not* to be in the link, but append it for the returned key
        return path.startsWith(DEFAULT_DOI_SCHEME) ? null : DEFAULT_DOI_SCHEME + path;
      }
    };
  }


  public static InboundLinkTranslator forJournal(Journal journal, Configuration configuration) {
    return forJournal(journal.getJournalKey(), configuration);
  }

  public static InboundLinkTranslator forJournal(String journalKey, Configuration configuration) {
    String journalHost = getHostnameForJournal(journalKey, configuration);
    return forJournalByHost(journalHost, configuration);
  }

  public static InboundLinkTranslator forAnyJournal(Configuration configuration) {
    return forJournalByHost(null, configuration);
  }

  /**
   * Construct a translator that acts on URLs for a particular journal's host.
   *
   * @param journalHost   the hostname of the journal
   * @param configuration the local configuration
   * @return the translator object
   */
  private static InboundLinkTranslator forJournalByHost(String journalHost, Configuration configuration) {
    final String articleAction = configuration.getString(ARTICLE_ACTION_KEY);
    return new InboundLinkTranslator(journalHost, true) {
      @Override
      protected String getDoiFromPath(String path) {
        return path.startsWith(articleAction) ? path.substring(articleAction.length()) : null;
      }
    };
  }

}
