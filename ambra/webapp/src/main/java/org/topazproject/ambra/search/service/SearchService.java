/* $HeadURL::                                                                            $
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.net.URISyntaxException;
import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.search.SearchResultPage;
import org.topazproject.ambra.user.AmbraUser;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.RdfUtil;
import org.topazproject.otm.Session;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.EmbeddedDefinition;
import org.topazproject.otm.metadata.RdfDefinition;
import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * Service to provide search capabilities for the application
 *
 * @author Viru
 * @author Eric Brown
 */
public class SearchService {
  private static final Log                   log       = LogFactory.getLog(SearchService.class);
  private static final Map<String, String[]> FIELD_MAP = new HashMap<String, String[]>();
  private static final Map<String, Boolean>  SRCHB_MAP = new HashMap<String, Boolean>();
  private static final Map<String, String>   DT_MAP    = new HashMap<String, String>();
  private Configuration configuration;

  private ArticleOtmService articleService;
  private JournalService    journalService;
  private Session           session;
  private Cache             cache;

  static {
    FIELD_MAP.put("identifier",
                  new String[] { "art.id" });
    FIELD_MAP.put("title",
                  new String[] { "art.dublinCore.title" });
    FIELD_MAP.put("subject",
                  new String[] { "art.categories.mainCategory", "art.categories.subCategory" });
    FIELD_MAP.put("description",
                  new String[] { "art.dublinCore.description" });
    FIELD_MAP.put("creator",
                  new String[] { "art.dublinCore.creators" });
    FIELD_MAP.put("contributor",
                  new String[] { "art.dublinCore.contributors" });
    FIELD_MAP.put("publisher",
                  new String[] { "art.dublinCore.publisher" });
    FIELD_MAP.put("rights",
                  new String[] { "art.dublinCore.rights" });
    FIELD_MAP.put("language",
                  new String[] { "art.dublinCore.language" });
    FIELD_MAP.put("type",
                  new String[] { "art.dublinCore.type" });
    FIELD_MAP.put("format",
                  new String[] { "art.dublinCore.format" });
    FIELD_MAP.put("date",
                  new String[] { "art.dublinCore.date" });
    FIELD_MAP.put("dateSubmitted",
                  new String[] { "art.dublinCore.submitted" });
    FIELD_MAP.put("dateAccepted",
                  new String[] { "art.dublinCore.accepted" });
    FIELD_MAP.put("journalTitle",
                  new String[] { "art.dublinCore.bibliographicCitation.journal" });
    FIELD_MAP.put("volume",
                  new String[] { "art.dublinCore.bibliographicCitation.volume" });
    FIELD_MAP.put("issue",
                  new String[] { "art.dublinCore.bibliographicCitation.issue" });
    FIELD_MAP.put("issn",
                  new String[] { "art.eIssn" });
    FIELD_MAP.put("elocationId",
                  new String[] { "art.dublinCore.bibliographicCitation.eLocationId" });
    FIELD_MAP.put("body",
                  new String[] { "cast(art.representations, TextRepresentation).body" });
    FIELD_MAP.put("reference",
                  new String[] { "art.dublinCore.references.title",
                                 "art.dublinCore.references.authors.realName"
                               });
    FIELD_MAP.put("editor",
                  new String[] { "art.dublinCore.bibliographicCitation.editors.realName" });

    // FIXME: get this info from otm metadata (see isSearchable())
    SRCHB_MAP.put("identifier",    false);
    SRCHB_MAP.put("title",         true);
    SRCHB_MAP.put("subject",       true);
    SRCHB_MAP.put("description",   true);
    SRCHB_MAP.put("creator",       true);
    SRCHB_MAP.put("contributor",   true);
    SRCHB_MAP.put("publisher",     true);
    SRCHB_MAP.put("rights",        true);
    SRCHB_MAP.put("language",      false);
    SRCHB_MAP.put("type",          false);
    SRCHB_MAP.put("format",        false);
    SRCHB_MAP.put("date",          false);
    SRCHB_MAP.put("dateSubmitted", false);
    SRCHB_MAP.put("dateAccepted",  false);
    SRCHB_MAP.put("journalTitle",  true);
    SRCHB_MAP.put("volume",        true);
    SRCHB_MAP.put("issue",         true);
    SRCHB_MAP.put("issn",          false);
    SRCHB_MAP.put("elocationId",   false);
    SRCHB_MAP.put("body",          true);
    SRCHB_MAP.put("reference",     true);
    SRCHB_MAP.put("editor",        true);

    DT_MAP.put("date",          "^^<xsd:date>");
    DT_MAP.put("dateSubmitted", "^^<xsd:date>");
    DT_MAP.put("dateAccepted",  "^^<xsd:date>");
  }

  private String[] getDefaultFields() {
    String[] fields = configuration.getStringArray("ambra.services.search.defaultFields");
    return (fields != null) ? fields : new String[] { "description", "title", "body" };
  }

  /**
   * Find the results for a given query.
   *
   * @param query          The lucene query string the user suplied
   * @param limitToJournal The keys for the journals to which this search should be limited.
   *   Assumed to have at least one element.
   * @param startPage      The page number of the search results the user wants
   * @param pageSize       The number of results per page
   * @param user
   * @return A SearchResultPage representing the search results page to be rendered
   * @throws ParseException if <var>query</var> is not valid
   * @throws OtmException OTM exception
   */
  @Transactional(readOnly = true)
  public SearchResultPage find(final String query, final String[] limitToJournal,
                               final int startPage, final int pageSize, AmbraUser user)
                                   throws ParseException, OtmException {
    String cacheKey = "";
    for (String journalKey : limitToJournal) {
      cacheKey = cacheKey + journalKey + "_";
    }
    cacheKey = cacheKey.substring(0,cacheKey.length()-1) + "|" + query;

    Set<String> oldFilters = session.listFilters();
    for (String fn : oldFilters) {
      session.disableFilter(fn);
    }

    try {
      // Note: we don't do any cache-invalidation, but instead rely on the ttl for this cache
      Results results = cache.get(cacheKey, -1,
          new Cache.SynchronizedLookup<Results, ParseException>(cacheKey.intern()) {
            public Results lookup() throws ParseException {
              return doSearch(query, limitToJournal);
            }
          });
      return results.getPage(startPage, pageSize);
    } finally { // Reattach the filters to the session after the query, if filters had been removed.
      for (String fn : oldFilters) {
        session.enableFilter(fn);
      }
    }
  }

  /**
   * Turn the user-submitted query into an OQL query, submit that OQL query to the datastore,
   * then filter and return the resulting article URIs.  Filtering is done on a per-journal basis;
   * if an article has been published (or cross-published) in one of the journals in limitToJournal,
   * then that article is included in the returned Results.
   * <p/>
   * This method removes <strong>all</strong> Filters from the Session before composing and
   * submitting the OQL query, then adds all those Filters back to the Session before returning,
   * whether or not the query was successful.
   *
   * @param queryString The user-submitted query that will be transformed into an OQL query
   * @param limitToJournal The keys for all of the Journals that will be searched.
   *   Assumed to have at least one element.
   * @return The URIs for the articles returned by the search, as long as those articles
   *   are published (or cross-published) in one of the journals in limitToJournal
   * @throws ParseException if <var>query</var> is not valid
   * @throws OtmException OTM exception
   */
  private Results doSearch(String queryString, String[] limitToJournal)
      throws ParseException, OtmException {
    // TODO: should we get the analyzer from one of the model's @Searchable defs?
    Query lq = new MultiFieldQueryParser(getDefaultFields(),
                                         new StandardAnalyzer()).parse(queryString);

    String oql = buildOql(lq);
    if (log.isDebugEnabled())
      log.debug("Translated lucene query '" + queryString + "' to oql query '" + oql + "'");

    org.topazproject.otm.query.Results r = session.createQuery(oql).execute();
    if (r.getWarnings() != null)
      log.warn("Warnings from query '" + oql + "': " + Arrays.asList(r.getWarnings()));

    int numVars = r.getVariables().length;

    Map<String, Double> scoredIds = new HashMap<String, Double>();
    Map<String, String> idEissnMap = new HashMap<String, String>();
    while (r.next()) {
      String id = r.getString(0);
      String eIssn = r.getString(1);

      // Since the Journal Filters have been turned off, check to see if this Article is
      // published in (or has been Cross-Published to) any of the Journals in limitToJournal.
      boolean isInJournal = false;
      for (String journalKey : limitToJournal) {
        Journal journal = journalService.getJournal(journalKey);
        try {
          if (eIssn.equals(journal.geteIssn())
              || journal.getSimpleCollection().contains(new URI(id))) {
            isInJournal = true;
            break;
          }
        } catch (URISyntaxException use) {
          if (id == null)
            log.warn("Could not create a new URI from NULL id.", use);
          else
            log.warn("Could not create a new URI from id = " + id, use);
        }
      }
      if ( ! isInJournal) {
        continue;
      }

      double score = 0;
      for (int idx = 2; idx < numVars; idx++) {
        String s = r.getString(idx);
        if (s != null)
          score += Double.parseDouble(s);
      }

      Double prevScore = scoredIds.get(id);
      if (prevScore == null || prevScore < score) {
        scoredIds.put(id, score);
        if ( ! idEissnMap.containsKey(id)) {
          idEissnMap.put(id, eIssn);
        }
      }

    }

    List<SearchHit> hits = new ArrayList<SearchHit>();
    for (Map.Entry<String, Double> e : scoredIds.entrySet()) {
      String journalId = journalService.getJournalIdByEissn(idEissnMap.get(e.getKey())).getKey();
      hits.add(new SearchHit(e.getValue(), e.getKey(), null, null, null, null,
          journalId, journalService.getJournal(journalId).getDublinCore().getTitle(), null));
    }
    Collections.sort(hits);

    return new Results(hits, lq, articleService);
  }

  /**
   * Turn the given lucene query into an equivalent oql query. The oql query will query for
   * articles with the given lucene constraints; the result will have 2 + N columns, with the
   * first column containing the article id, the second column containing the article eIssn,
   * and the remaining N columns containing scores.
   *
   * @param lq  the parsed lucene query
   * @return the resulting oql query
   */
  private String buildOql(Query lq) throws ParseException {
    StringBuilder sel = new StringBuilder("select art.id , art.eIssn ");
    StringBuilder whr = new StringBuilder(500);

    int[] scnt = new int[] { 0 };
    buildOql(lq, sel, whr, scnt);

    sel.append("from Article art ");
    if (whr.length() > 0)
      sel.append("where ").append(whr);

    return sel.append(';').toString();
  }

  private void buildOql(Query lq, StringBuilder sel, StringBuilder whr, int[] scnt)
        throws ParseException {
    // FIXME: add in boost

    if (lq instanceof BooleanQuery)
      buildOql((BooleanQuery) lq, sel, whr, scnt);
    else if (lq instanceof ConstantScoreRangeQuery)
      buildOql((ConstantScoreRangeQuery) lq, sel, whr, scnt);
    else
      buildOql(lq.toString(), sel, whr, scnt);
  }

  /**
   * This builds an equivalent oql (tql) query for the given lucene boolean query. Each clause in
   * Lucene's boolean query can be marked required (MUST, '+'), optional (SHOULD, ''), or
   * not-allowed (MUST_NOT, '-'). The mapping to a true boolean expression is as follows:
   * <pre>
   *   +a +b    -&gt; a and b
   *    a  b    -&gt; a or b
   *   -a -b    -&gt; !a and !b  ==  !(a or b)
   *
   *   +a  b    -&gt; a and (b or true)
   *    a -b    -&gt; a and !b
   *   +a -b    -&gt; a and !b
   * </pre>
   * The 4th and 5th cases are interesting. In case 4 it would seem that the (b or true) part could
   * be left out, because it won't affect the truth outcome; however, the score from b still
   * participates in the overall score, and hence b must still be evaluated. For case 5 one needs
   * to observe that lucene will only subtract from some otherwise selected set, never from the
   * set of all documents; i.e. the query '!a' always yields no results.
   *
   * <p>Regarding cases 3 and 6, lucene treats 'a (-b AND -c)' and 'a (-b OR -c)' identically,
   * namely as the former.
   *
   * <p>In terms of mulgara's query algebra the 'and' and 'or' translate directly, the '!' is the
   * tql minus operator, and 'true' is UNBOUND (generated in the code below by a '0 &lt; 1'
   * constraint).
   */
  @SuppressWarnings("unchecked")
  private void buildOql(BooleanQuery lq, StringBuilder sel, StringBuilder whr, int[] scnt)
        throws ParseException {
    if (lq.clauses().size() == 0)
      return;

    simplify(lq);

    whr.append("((");

    boolean havePhb = false;

    // 'and' up all required clauses
    for (BooleanClause c : (List<BooleanClause>) lq.clauses()) {
      if (c.isRequired()) {
        buildOql(c.getQuery(), sel, whr, scnt);
        whr.append(" and ");
      } else if (c.isProhibited()) {
        havePhb = true;
      }
    }

    boolean haveRqd = (whr.charAt(whr.length() - 1) != '(');

    // 'or' up all optional clauses
    if (haveRqd)
      whr.append("(");

    for (BooleanClause c : (List<BooleanClause>) lq.clauses()) {
      if (c.getOccur() == Occur.SHOULD) {
        buildOql(c.getQuery(), sel, whr, scnt);
        whr.append(" or ");
      }
    }

    boolean haveOpt = (whr.charAt(whr.length() - 1) != '(');

    if (haveRqd && haveOpt)
      whr.append("lt('0'^^<xsd:int>, '1'^^<xsd:int>))");        // 'true' (UNBOUND)
    else if (haveRqd)
      whr.setLength(whr.length() - 6);  // remove trailing ' and ('
    else if (haveOpt)
      whr.setLength(whr.length() - 4);  // remove trailing ' or '
    else {
      whr.setLength(whr.length() - 2);  // remove '(('
      whr.append("gt('0'^^<xsd:int>, '1'^^<xsd:int>)");         // 'false' (EMPTY)
      return;
    }

    whr.append(")");

    // 'minus' the prohibited clauses
    if (havePhb) {
      whr.append(" minus (");

      for (BooleanClause c : (List<BooleanClause>) lq.clauses()) {
        if (c.isProhibited()) {
          buildOql(c.getQuery(), sel, whr, null);
          whr.append(" or ");
        }
      }

      whr.setLength(whr.length() - 4);    // remove ' or '
      whr.append(")");
    }

    // clean up
    whr.append(") ");
  }

  /**
   * This attempts to simplify the clauses of a boolean query, especially the minus clauses.
   * The issues are that:
   * <ul>
   *   <li>lucene's MultiFieldQueryParser often creates nested clauses for minus terms; e.g.
   *       'a (-b)' generates '(a) (-(b))', i.e. the second clause appears as an optional clause
   *       in the top-level boolean-query</li>
   *   <li>we don't really want to ever subtract from all, so if we have only minus terms then
   *       we try to pull the minus up a level. E.g. 'a (-foo -bar)' can be turned into
   *       'a -(foo OR bar)'</li>
   *   <li>for performance we want to group all optional clauses with the same field into a single
   *       clause because each clause becomes a separate lucene query when turned in tql. Note
   *       that we can't group required clauses because if the field has a cardinality greater 1
   *       and one terms matches in one value and a second term in another value then we'd get no
   *       results because no single value has both terms.</li>
   * </ul>
   *
   * @param q the boolean-query to simplify
   */
  @SuppressWarnings("unchecked")
  private static void simplify(BooleanQuery q) {
    // flatten things where possible
    List<BooleanClause> added   = new ArrayList<BooleanClause>();
    List<BooleanClause> removed = new ArrayList<BooleanClause>();

    for (BooleanClause c : (List<BooleanClause>) q.clauses()) {
      if (!(c.getQuery() instanceof BooleanQuery))
        continue;

      BooleanQuery bq = (BooleanQuery) c.getQuery();
      simplify(bq);

      if (bq.clauses().size() == 1) {
        // flatten single-element list
        BooleanClause c2 = (BooleanClause) bq.clauses().get(0);
        c.setOccur(combineOccurs(c.getOccur(), c2.getOccur()));
        c.setQuery(c2.getQuery());
      } else if (bq.clauses().size() > 1 && allMatch(bq, Occur.MUST_NOT)) {
        // pull up must-not (-)
        for (BooleanClause c2 : (List<BooleanClause>) bq.clauses())
          c2.setOccur(Occur.SHOULD);
        c.setOccur(combineOccurs(c.getOccur(), Occur.MUST_NOT));
      } else if (bq.clauses().size() > 1 && allMatch(bq, c.getOccur())) {
        // flatten list where all elements have same occurs and it matches our occurs
        added.addAll((List<BooleanClause>) bq.clauses());
        bq.clauses().clear();
        removed.add(c);
      }
    }

    q.clauses().addAll(added);
    q.clauses().removeAll(removed);

    /* group optional and prohibited clauses for same field together.
     * (note: LinkedHashMap is to make the tests stable).
     */
    Map<String, List<BooleanClause>> fieldMap = new LinkedHashMap<String, List<BooleanClause>>();
    for (BooleanClause c : (List<BooleanClause>) q.clauses())
      multiMapPut(fieldMap, getField(c.getQuery()), c);

    q.clauses().clear();
    for (Map.Entry<String, List<BooleanClause>> entry : fieldMap.entrySet()) {
      if (entry.getKey() == null || entry.getValue().size() == 1) {
        q.clauses().addAll(entry.getValue());
      } else {
        List<BooleanClause> shoulds = new ArrayList<BooleanClause>();
        for (BooleanClause c : entry.getValue()) {
          if (c.getOccur() == Occur.SHOULD)
            shoulds.add(c);
          else
            q.add(c);
        }

        if (shoulds.size() > 0)
          q.add(new SameFieldQuery(entry.getKey(), shoulds), Occur.SHOULD);
      }
    }
  }

  /*
   * + .  ->  +
   * - .  ->  -
   * . .  ->  .
   * + +  ->  +
   * - -  ->  +
   * + -  ->  -
   */
  private static Occur combineOccurs(Occur outer, Occur inner) {
    if (outer == Occur.SHOULD)
      return inner;
    if (inner == Occur.SHOULD)
      return outer;
    if (outer == inner)
      return Occur.MUST;
    return Occur.MUST_NOT;
  }

  @SuppressWarnings("unchecked")
  private static boolean allMatch(BooleanQuery bq, Occur cond) {
    for (BooleanClause c : (List<BooleanClause>) bq.clauses()) {
      if (c.getOccur() != cond)
        return false;
    }
    return true;
  }

  private static String getField(Query q) {
    if (q instanceof TermQuery)
      return ((TermQuery) q).getTerm().field();
    if (q instanceof MultiTermQuery)
      return ((MultiTermQuery) q).getTerm().field();
    if (q instanceof PhraseQuery)
      return ((PhraseQuery) q).getTerms()[0].field();
    if (q instanceof MultiPhraseQuery)
      return ((Term[]) ((MultiPhraseQuery) q).getTermArrays().get(0))[0].field();
    if (q instanceof PrefixQuery)
      return ((PrefixQuery) q).getPrefix().field();
    if (q instanceof RangeQuery)
      return ((RangeQuery) q).getField();
    if (q instanceof ConstantScoreRangeQuery)
      return ((ConstantScoreRangeQuery) q).getField();
    return null;
  }

  private static <K,V> void multiMapPut(Map<K, List<V>> map, K key, V value) {
    List<V> vals = map.get(key);
    if (vals == null)
      map.put(key, vals = new ArrayList<V>());
    vals.add(value);
  }

  /**
   * This represents a query consisting of a list of boolean clauses that are all for
   * the same field. We use this to group these clauses together and generate a single
   * oql search term. This can significantly increase the performance because it replaces
   * N separate lucene queries and the associated joins with a single lucene query.
   */
  private static class SameFieldQuery extends Query {
    private final String              field;
    private final List<BooleanClause> clauses;

    /**
     * Create a new query.
     *
     * @param field   this query's field
     * @param clauses the clauses belonging up this query
     */
    public SameFieldQuery(String field, List<BooleanClause> clauses) {
      this.field   = field;
      this.clauses = clauses;
    }

    public String toString(String field) {
      StringBuilder sb = new StringBuilder(clauses.size() * 10);
      if (!this.field.equals(field))
        sb.append(this.field).append(':');

      sb.append('(');
      for (BooleanClause c : clauses)
        sb.append(c.getOccur()).append(c.getQuery().toString(this.field)).append(' ');
      sb.setLength(sb.length() - 1);
      sb.append(')');

      return sb.toString();
    }
  }

  /**
   * Turn a lucene range query into an oql constraint. If the field represents a property that is
   * searchable then an oql search() function is added to the where clause; otherwise an oql
   * equality lt() and/or a gt() is added.
   *
   * @param lq   the lucene range query; it's expected to be in &lt;field&gt;:&lt;query&gt; format
   * @param sel  the oql select clause
   * @param whr  the oql where clause
   * @param scnt the current score-variable counter, or null if no scores should be generated
   */
  private void buildOql(ConstantScoreRangeQuery lq, StringBuilder sel, StringBuilder whr,
                        int[] scnt) throws ParseException {
    if (!FIELD_MAP.containsKey(lq.getField()))
      throw new ParseException("Unknown field '" + lq.getField() + "'");
 
    //if (isSearchable(fexpr)) {
    if (SRCHB_MAP.get(lq.getField())) {
      buildOql(lq.toString(), sel, whr, scnt);
      return;
    }

    whr.append("(");
    for (String fexpr : FIELD_MAP.get(lq.getField())) {
      if (lq.getLowerVal() != null)
        whr.append(lq.includesLower() ? "ge(" : "gt(").append(fexpr).append(", '").
            append(RdfUtil.escapeLiteral(lq.getLowerVal())).append("'").
            append(DT_MAP.containsKey(lq.getField()) ? DT_MAP.get(lq.getField()) : "").append(")");

      if (lq.getLowerVal() != null && lq.getUpperVal() != null)
        whr.append(" and ");

      if (lq.getUpperVal() != null)
        whr.append(lq.includesUpper() ? "le(" : "lt(").append(fexpr).append(", '").
            append(RdfUtil.escapeLiteral(lq.getUpperVal())).append("'").
            append(DT_MAP.containsKey(lq.getField()) ? DT_MAP.get(lq.getField()) : "").append(")");

      whr.append(" or ");
    }

    whr.setLength(whr.length() - 4);  // remove trailing ' or '
    whr.append(")");
  }

  /**
   * Turn a non-boolean/range lucene query into an oql constraint. If the field represents a
   * property that is searchable then an oql search() function is added to the where clause;
   * otherwise an oql equality ('=') is added. If a search() function is added then a new score
   * is added to the select clause.
   *
   * @param qs   the lucene query; it's expected to be in &lt;field&gt;:&lt;query&gt; format
   * @param sel  the oql select clause
   * @param whr  the oql where clause
   * @param scnt the current score-variable counter, or null if no scores should be generated
   */
  private void buildOql(String qs, StringBuilder sel, StringBuilder whr, int[] scnt)
        throws ParseException {
    int colon = qs.indexOf(':');
    String field = qs.substring(0, colon);
    String text  = qs.substring(colon + 1);

    if (!FIELD_MAP.containsKey(field))
      throw new ParseException("Unknown field '" + field + "'");

    whr.append("(");

    for (String fexpr : FIELD_MAP.get(field)) {
      //if (isSearchable(fexpr)) {
      if (SRCHB_MAP.get(field)) {
        whr.append("search(").append(fexpr).append(", '").
            append(RdfUtil.escapeLiteral(text)).append("'");
        if (scnt != null)
          whr.append(", score").append(scnt[0]);
        whr.append(") or ");

        if (scnt != null)
          sel.append(", score").append(scnt[0]++).append(" ");
      } else {
        whr.append(fexpr).append(" = '").append(RdfUtil.escapeLiteral(text)).append("'").
            append(DT_MAP.containsKey(field) ? DT_MAP.get(field) : "").
            append(" or ");
      }
    }

    whr.setLength(whr.length() - 4);  // remove trailing ' or '
    whr.append(") ");
  }

  // FIXME: method is not used anywhere
  private boolean isSearchable(String fexpr) {
    // FIXME: this doesn't handle subclasses correctly

    String[] derefs = fexpr.split("\\.");
    String entity = session.getSessionFactory().getClassMetadata(Article.class).getName();
    char sep = ':';

    for (int idx = 1; idx < derefs.length - 1; idx++) {
      Definition def = session.getSessionFactory().getDefinition(entity + sep + derefs[idx]);
      if (def == null) {
        for (String sup : session.getSessionFactory().getClassMetadata(entity).getSuperEntities()) {
          def = session.getSessionFactory().getDefinition(sup + sep + derefs[idx]);
          if (def != null) {
            entity = sup;
            break;
          }
        }
      }

      if (def instanceof RdfDefinition) {
        entity = ((RdfDefinition) def).getAssociatedEntity();
        sep = ':';
      } else if (def instanceof EmbeddedDefinition) {
        entity = entity + sep + derefs[idx];
        sep = '.';
      } else {
        throw new RuntimeException("Could find definitions for '" + fexpr + "'; current entity = " +
                                   "'" + entity + "', last deref = '" + derefs[idx] + "'");
      }
    }

    Definition def = SearchableDefinition.findForProp(session.getSessionFactory(),
                                                      entity + sep + derefs[derefs.length - 1]);
    return (def != null);
  }

  /**
   * Set ArticleOtmService.  Enable Spring autowiring.
   *
   * @param articleService to use.
   */
  @Required
  public void setArticleOtmService(final ArticleOtmService articleService) {
    this.articleService = articleService;
  }

  /**
   * Set the JournalService.
   *
   * @param journalService to use.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Set the search cache to use.
   *
   * @param cache the cache
   */
  @Required
  public void setSearchCache(Cache cache) {
    this.cache = cache;
  }

  /**
   * Set the otm session to use.
   *
   * @param session the session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Setter method for configuration. Injected through Spring.
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
