/* $HeadURL::                                                                                     $
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
package org.topazproject.ambra.search.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.lucene.search.Query;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.TextRepresentation;
import org.topazproject.ambra.search.SearchResultPage;

/**
 * Store the progress of a search. That is, when a search is done, we get the first N results
 * and don't get more until more are requested.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class Results {
  private static final Log           log  = LogFactory.getLog(Results.class);
  private static final Configuration CONF = ConfigurationStore.getInstance().getConfiguration();

  private final ArticleOtmService articleService;
  private final Query             luceneQuery;
  private final List<SearchHit>   unresolvedHits;
  private final List<SearchHit>   resolvedHits;
  private final Lock              unresLock = new ReentrantLock();
  private       int               nextUnres = 0;
  private       int               totalHits;

  /**
   * Construct a search Results object.
   *
   * @param scoredIds      the scored articles id's (i.e. only the score and uri are valid)
   * @param articleService the ArticleOtmService.
   */
  public Results(List<SearchHit> scoredIds, Query luceneQuery, ArticleOtmService articleService) {
    this.unresolvedHits = scoredIds;
    this.resolvedHits   = new ArrayList<SearchHit>(unresolvedHits.size());
    this.luceneQuery    = luceneQuery;
    this.articleService = articleService;
  }

  /**
   * Return a search results page.
   *
   * @param startPage the page number to return (starts with 0)
   * @param pageSize  the page size; this controls both the number of entries to return as well as
   *                  the the first entry to return.
   * @return The results for one page of a search. If the <var>startPage</var> is beyond the end of
   *         the results then the last page is returned; if <var>startPage</var> is less than 0 then
   *         the first page is returned. If there are no search results, the hits list will be
   *         empty.
   */
  public SearchResultPage getPage(int startPage, int pageSize) {
    pageSize = Math.max(pageSize, 1);

    int start = Math.max(startPage * pageSize, 0);
    int end   = start + pageSize;

    boolean needResolving;
    synchronized (resolvedHits) {
      if (resolvedHits.size() >= end) {
        return new SearchResultPage(totalHits, pageSize,
                                    new ArrayList(resolvedHits.subList(start, end)));
      }
    }

    resolveHits(end, pageSize);

    synchronized (resolvedHits) {
      start = Math.min(start, Math.max(((resolvedHits.size() - 1) / pageSize) * pageSize, 0));
      end   = Math.min(end, resolvedHits.size());
      return new SearchResultPage(totalHits, pageSize,
                                  new ArrayList(resolvedHits.subList(start, end)));
    }
  }

  private void resolveHits(int end, int pageSize) {
    List<SearchHit> tmp = new ArrayList<SearchHit>(pageSize);

    try {
      while (!unresLock.tryLock(2L, TimeUnit.SECONDS)) {
        synchronized (resolvedHits) {
          if (resolvedHits.size() >= end)
            return;
        }

        log.warn("Still waiting for lock to resolve hits");
      }
    } catch (InterruptedException ie) {
      throw new RuntimeException("Error waiting for search-results lock", ie);
    }

    try {
      while (resolvedHits.size() < end && nextUnres < unresolvedHits.size()) {
        while (tmp.size() < pageSize && nextUnres < unresolvedHits.size()) {
          SearchHit res = resolveHit(unresolvedHits.get(nextUnres++));
          if (res != null)
            tmp.add(res);
        }

        synchronized (resolvedHits) {
          resolvedHits.addAll(tmp);
          totalHits = resolvedHits.size() + (unresolvedHits.size() - nextUnres);
        }

        tmp.clear();
      }
    } finally {
      unresLock.unlock();
    }
  }

  private SearchHit resolveHit(SearchHit hit) {
    URI uri = URI.create(hit.getUri());

    // verify that Aricle exists, is accessible by user, is in Journal, etc., be cache aware
    Article article;
    try {
      article = articleService.getArticle(uri);
    } catch (NoSuchArticleIdException nsae) {
      // shouldn't actually happen due to filtering being applied on the query directly
      if (log.isDebugEnabled())
        log.debug("Search hit '" + uri + "' removed due to the article being filtered out", nsae);
      return null;
    }

    // build missing data
    try {
      return new SearchHit(hit.getHitScore(), hit.getUri(), article.getDublinCore().getTitle(),
                           createHighlight("body", findTextRepresentation(article).getBodyAsText()),
                           article.getDublinCore().getCreators(),
                           article.getDublinCore().getDate());
    } catch (IOException ioe) {
      log.warn("Error creating highlight for article '" + hit.getUri() + "'", ioe);
      return null;
    }
  }

  private static TextRepresentation findTextRepresentation(Article article) throws IOException {
    for (Representation r : article.getRepresentations()) {
      if (r instanceof TextRepresentation)
        return (TextRepresentation) r;
    }
    throw new IOException("No text-representation found for article " + article.getId());
  }

  private String createHighlight(String fname, String fval) throws IOException {
    int snippetsMax    = CONF.getInt("ambra.services.search.snippetsMax",    3);
    int fieldMaxLength = CONF.getInt("ambra.services.search.fieldMaxLength", 50);

    // Try to build snippets
    if (snippetsMax > 0) {
      SimpleHTMLFormatter formatter =
          new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");

      QueryScorer scorer = new QueryScorer(luceneQuery, fname);
      Highlighter highlighter = new Highlighter(formatter, scorer);
      Fragmenter fragmenter = new SimpleFragmenter(fieldMaxLength);
      highlighter.setTextFragmenter(fragmenter);
      TokenStream tokenStream = getAnalyzer().tokenStream(fname, new StringReader(fval));
      String[] fragments = highlighter.getBestFragments(tokenStream, fval, snippetsMax);
      if (fragments != null && fragments.length > 0) {
        StringBuilder sb = new StringBuilder(snippetsMax * (fieldMaxLength + 25));
        for (int i = 0; i < fragments.length; i++) {
          sb.append(stripTrailingEntity(fragments[i]));
          if (i < (fragments.length - 1))
            sb.append(" ... ");
        }
        return sb.toString();
      }
    }

    if (fieldMaxLength > 0 && fval.length() > fieldMaxLength)
      return stripTrailingEntity(fval.substring(0, fieldMaxLength)) + " ... ";
    else
      return fval;
  }

  private static String stripTrailingEntity(String src) {
    // Walk the string looking for matching &, ; pairs
    int pos = 0;
    while (true) {
      int iamp = src.indexOf("&", pos);
      if (iamp == -1)
        return src;
      pos = src.indexOf(";", iamp);
      if (pos == -1)
        return src.substring(0, iamp);
      /* See if entity contains any invalid characters (if so, strip entire thing)
       * Okay, not invalid characters, just any entity that is too long
       */
      if (pos - iamp > 7)
        src = src.substring(0, iamp) + src.substring(pos + 1);
    }
  }

  private static final Analyzer getAnalyzer() {
    return new StandardAnalyzer();
  }
}
