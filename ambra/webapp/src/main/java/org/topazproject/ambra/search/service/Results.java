/* $HeadURL::                                                                                     $
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

  private static final String HIGHLIGHT_TAG_START = "<span class=\"highlight\">";
  private static final String HIGHLIGHT_TAG_END = "</span>";


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

    // verify that Article exists, is accessible by user, is in Journal, etc., be cache aware
    Article article;
    try {
      article = articleService.getArticle(uri);
    } catch (NoSuchArticleIdException nsae) {
      // shouldn't actually happen due to filtering being applied on the query directly
      if (log.isDebugEnabled())
        log.debug("Search hit '" + uri + "' removed due to the article being filtered out", nsae);
      return null;
    } catch (SecurityException se) {
      if (log.isDebugEnabled())
        log.debug("Search hit '" + uri + "' removed due to the article being restricted", se);
      return null;
    }

    // build missing data
    try {
      return new SearchHit(hit.getHitScore(), hit.getUri(), article.getDublinCore().getTitle(),
                           createHighlight("body", findTextRepresentation(article).getBodyAsText()),
                           article.getDublinCore().getBibliographicCitation().getAuthorsRealNames(),
                           article.getDublinCore().getDate(), hit.getJournalKey());
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
    int fieldMaxLength = CONF.getInt("ambra.services.search.fieldMaxLength", 100);

    // Try to build snippets
    if (snippetsMax > 0) {
      SimpleHTMLFormatter formatter =
          new SimpleHTMLFormatter(HIGHLIGHT_TAG_START, HIGHLIGHT_TAG_END);

      QueryScorer scorer = new QueryScorer(luceneQuery, fname);
      Highlighter highlighter = new Highlighter(formatter, scorer);
      Fragmenter fragmenter = new SimpleFragmenter(fieldMaxLength);
      highlighter.setTextFragmenter(fragmenter);
      TokenStream tokenStream = getAnalyzer().tokenStream(fname, new StringReader(fval));
      String[] fragments = highlighter.getBestFragments(tokenStream, fval, snippetsMax);
      if (fragments != null && fragments.length > 0) {
        StringBuilder sb = new StringBuilder(snippetsMax * (fieldMaxLength + 25));
        for (int i = 0; i < fragments.length; i++) {
          sb.append(stripTrailingEntity(stripTags(fragments[i])));
          if (i < (fragments.length - 1))
            sb.append(" ... ");
        }
        return sb.toString();
      }
    }

    if (fieldMaxLength > 0 && fval.length() > fieldMaxLength) {
      String res = stripTrailingEntity(stripTags(fval.substring(0, fieldMaxLength)));

      if(res.trim().length() > 0) {
        return res + " ... ";
      } else {
        return "";
      }
    } else {
      return fval;
    }
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

  /**
   * Remove all of the non-Highlight tags from a String, including whole tags
   * and fragments of tags at the begining and end of the String.
   * If a Highlight tag appears inside a non-Highlight tag (whole or fragmentary),
   * then return a zero-length String
   * (this happens when a query term is located inside one
   * of the non-Highlight tags and that query term is marked-up with a Highlight tag).
   * <p/>
   * A "tag" is anything between the greater-than and less-than characters.
   *
   * @param src The String which will have its trags removed
   * @return The input String without any non-Highlight tags
   */
  private static String stripTags(String src) {
    // Remove Highlight tags inside other tags.  Highlight tags always appear in start/end pairs.
    if (src.indexOf(HIGHLIGHT_TAG_START) > -1 && src.indexOf(HIGHLIGHT_TAG_END) > -1) {
      // If the leading tag or fragment contains a Highlight tag, then discard entire "src" String
      if (src.indexOf('>', src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length()) > -1
          && ( src.indexOf('>', src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length())
              < src.indexOf('<', src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length())
          || src.indexOf('<', src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length()) < 0) ) {
        return "";
      }
      // If the trailing tag or fragment contains a Highlight tag, then discard entire "src" String
      String choppedSrc = src.substring(0, src.lastIndexOf(HIGHLIGHT_TAG_START));
      if (choppedSrc.lastIndexOf('>') < choppedSrc.lastIndexOf('<') ) {
        return "";
      }
    }

    // Remove all leading and trailing tag fragments, if those fragments exist.
    //  If the first > appears before the first < then remove the leading tag fragment.
    if (src.indexOf('>') < src.indexOf('<') || (src.indexOf('>') > -1 && src.indexOf('<') < 0)) {
      src = src.substring(src.indexOf('>') + 1, src.length());
    }
    //  If the last < appears after the last > then remove the trailing tag fragment.
    if (src.lastIndexOf('>') < src.lastIndexOf('<') ) {
      src = src.substring(0, src.lastIndexOf('<'));
    }

    if (src.indexOf('<') < 0 && src.indexOf('>') < 0) {
      return src;
    }
    //  Remove the whole tags that are not Hightlight tags.
    StringBuffer sb = new StringBuffer("");
    while (src.indexOf('<') > -1 && src.indexOf('>') > -1) {
      //  If there is a Highlight tag pair, then keep it, along with the text inside the tag pair.
      if (src.indexOf('<') == src.indexOf(HIGHLIGHT_TAG_START)) {
        if (src.indexOf('<') > 0) {
          sb.append(src.substring(0, src.indexOf('<') - 1) + " ");
          src = src.substring(src.indexOf('<'), src.length());
        }
        sb.append(src.substring(
            0, src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length()) + " ");
        src = src.substring(
            src.indexOf(HIGHLIGHT_TAG_END) + HIGHLIGHT_TAG_END.length(), src.length());

      } else {
        if (src.indexOf('<') > 0) {
          sb.append(src.substring(0, src.indexOf('<')) + " ");
        }
        src = src.substring(src.indexOf('>') + 1, src.length());
      }
    }
    return sb.toString() + src;
  }

  private static final Analyzer getAnalyzer() {
    return new StandardAnalyzer();
  }
}
