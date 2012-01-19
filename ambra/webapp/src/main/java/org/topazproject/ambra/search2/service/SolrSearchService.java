/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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
package org.topazproject.ambra.search2.service;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.search2.SearchHit;
import org.topazproject.ambra.search2.SearchParameters;
import org.topazproject.ambra.search2.SearchResultSinglePage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Service to provide search capabilities for the application.
 *
 * @author Scott Sterling
 * @author Dragisa Krsmanovic
 */
public class SolrSearchService implements SearchService {
  private static final Logger log = LoggerFactory.getLogger(SolrSearchService.class);

  private SolrServerFactory serverFactory;
  private int queryTimeout;
  private static final DateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Perform an "all the words" search (across most article fields) for the terms in
   * <code>queryString</code>, in the journal defined by <code>eissn</code>.
   *
   * @param queryString The terms that will be sought in the indexed articles
   * @param eissn The Electronic ISSN of the journal that the user is currently viewing.
   * @param startPage Which group of Articles to return.  Value = 0 for page one.
   * @param pageSize The number of Articles that will be returned in a group
   * @return One "page" of articles which contain the terms in <code>queryString</code>
   * @throws ApplicationException Thrown by a failed query attempt
   */
  public SearchResultSinglePage simpleSearch(String queryString, String eissn, int startPage, int pageSize) throws ApplicationException {
    queryString = cleanString(queryString);
    log.debug("Simple Search performed on the String: " + queryString
        + " startPage: " + startPage
        + " pageSize: " + pageSize);

    SolrQuery query = createQuery(queryString, startPage, pageSize);

    if (eissn != null && eissn.length() > 0) {
      query.addFilterQuery(createFilterLimitToJournals(new String[]{eissn}));
    }
    return search(query);
  }

  public SearchResultSinglePage advancedSearch(SearchParameters searchParameters) throws ApplicationException {
    SearchParameters sp = cleanStrings(searchParameters);
    log.debug("Advanced Search performed on the SearchParameters: " + sp);

    SolrQuery query = createQuery(null, sp.getStartPage(), sp.getPageSize());

    // Set highlighting fields.  Defaults to field list in "highlightFieldsDefault" variable.
    if ("abstract".equals(sp.getTextSearchExactPhrase())) {
      query.set("hl.fl", "abstract");
    } else if ("refs".equals(sp.getTextSearchOption())) {
      query.set("hl.fl", "citation");
    } else if ("title".equals(sp.getTextSearchOption())) {
      query.set("hl.fl", "title");
    }

    StringBuilder q = new StringBuilder();

    // Form field description: "Author Name:"
    if (sp.getCreator().length > 0 && StringUtils.isNotBlank(sp.getCreator()[0])) {
      q.append(" ( ");
      for (int i = 0; i < sp.getCreator().length; i++) {
        String creatorName = sp.getCreator()[i];
        if (StringUtils.isNotBlank(creatorName)) {
          q.append(" author:\"").append(creatorName).append("\"");
        }
        if (i < sp.getCreator().length - 1
            && StringUtils.isNotBlank(sp.getCreator()[i + 1])) {
          if ("all".equals(sp.getAuthorNameOp())) {
            q.append(" AND ");
          } else {
            q.append(" OR ");
          }
        }
      }
      q.append(" ) ");
    }

    // Form field description: "for at least one of the words:"
    if (sp.getTextSearchAtLeastOne().trim().length() > 0) {
      q.append(" AND ").append(
          addFields(" OR ", sp.getTextSearchOption(), sp.getTextSearchAtLeastOne().trim())
      );
    }

    // Form field description: "for all the words:"
    if (sp.getTextSearchAll().trim().length() > 0) {
      q.append(" AND ").append(
          addFields(" AND ", sp.getTextSearchOption(), sp.getTextSearchAll().trim())
      );
    }

    // Form field description: "for the exact phrase:"
    if (sp.getTextSearchExactPhrase().trim().length() > 0) {
      q.append(" AND ");
      if ("abstract".equals(sp.getTextSearchOption())) {
        q.append("abstract:\"").append(sp.getTextSearchExactPhrase().trim()).append("\"");
      } else if ("refs".equals(sp.getTextSearchOption())) {
        q.append("citation:\"").append(sp.getTextSearchExactPhrase().trim()).append("\"");
      } else if ("title".equals(sp.getTextSearchOption())) {
        q.append("title:\"").append(sp.getTextSearchExactPhrase().trim()).append("\"");
      } else {
        q.append("\"").append(sp.getTextSearchExactPhrase().trim()).append("\"");
      }
      q.append(" ");
    }

    // Form field description: "without the words:"
    if (sp.getTextSearchWithout().trim().length() > 0) {
      q.append(" AND ").append(
          addFieldsWithoutTheWords(" AND ", sp.getTextSearchOption(),
              sp.getTextSearchWithout().trim())
      );
    }

    // Form field description: "Dates".  Query Filter.
    if (sp.getDateTypeSelect().trim().length() > 0) {
      query.addFilterQuery(createFilterDateRange(sp.getStartDate(), sp.getEndDate()));
    }

    // Form field description: "Journals".  Query Filter.
    if ("some".equals(sp.getJournalOpt()) && sp.getLimitToJournal().length > 0) {
      query.addFilterQuery(createFilterLimitToJournals(sp.getLimitToJournal()));
    }

    // Form field description: "Subject Categories".  Query Filter.
    if ("some".equals(sp.getSubjectCatOpt()) && sp.getLimitToCategory().length > 0) {
      query.addFilterQuery(createFilterLimitToCategories(sp.getLimitToCategory()));
    }


    if (q.indexOf(" AND ") == 0) {
      q.replace(0, 4, ""); // Remove the preceding " AND ", if there is one.
    }
    if (q.length() < 1) {
      q.append("*"); // Default to query = * so that every article will be returned.
      query.setFields(null); // If there are defined fields, then the wildcard expansion results in a TooManyClauses exception.
      query.setHighlight(false); // Also necessary to avoiding a TooManyClauses exception.
    }

    return search(query.setQuery(q.toString()));
  }

  public void setConfiguration(Configuration configuration) {
    queryTimeout = configuration.getInt("ambra.services.search.timeout", 60000); // default to 1 min
  }

  public void setServerFactory(SolrServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }

  private String createFilterDateRange(Date startDate, Date endDate) {
    StringBuilder fq = new StringBuilder();
    fq.append("publication_date:[").append(solrDateFormat.format(startDate))
        .append("T00:00:00Z TO ").append(solrDateFormat.format(endDate)).append("T00:00:00Z] ");
    return fq.toString();
  }

  private String createFilterLimitToJournals(String[] journals) {
    Arrays.sort(journals); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String journal : journals) {
      fq.append(" cross_published_journal_eissn:").append(journal).append(" OR");
    }
    return fq.replace(fq.length() - 3, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterLimitToCategories(String[] categories) {
    Arrays.sort(categories); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String category : categories) {
      fq.append(" subject_facet:").append("\"").append(category).append("\"").append(" OR");
    }
    return fq.replace(fq.length() - 3, fq.length(), "").toString(); // Remove last "OR".
  }

  private SearchResultSinglePage search(SolrQuery query) throws ApplicationException {

    if (serverFactory.getServer() == null) {
      throw new ApplicationException("Search server is not configured");
    }

    QueryResponse queryResponse;
    try {
      queryResponse = serverFactory.getServer().query(query);
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
      throw new ApplicationException("Unable to execute a query on the Solr Server.", e);
    }

    return readQueryResults(queryResponse, query);
  }

  private SolrQuery createQuery(String queryString, int startPage, int pageSize) {
    SolrQuery query = new SolrQuery(queryString);
    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(true); // The relevance (of each results element) to the search terms.
    query.setHighlight(true);
    query.setHighlightFragsize(50); // Max number of characters per highlighted snippet
    query.setHighlightSnippets(3);
    query.setHighlightSimplePre("<span class=\"highlight\">");
    query.setHighlightSimplePost("</span>");
    query.set("hl.usePhraseHighlighter", true);
    query.set("hl.highlightMultiTerm", true);
    query.set("hl.mergeContiguous", true);
    query.set("hl.fl", "abstract", "body", "everything", "title"); // TODO: Move the values for this field into a config file!
    query.setStart(startPage * pageSize); // Which results element to return first in this batch.
    query.setRows(pageSize); // The number of results elements to return.
    // request only fields that we need to display
    query.setFields("id", "score", "title", "publication_date", "eissn", "journal", "article_type", "author");
    query.set("spellcheck", true); // Return a list of possible matches for misspelled words.
    query.setFacet(true);
    query.set("facet.method", "enum");
    query.addFacetField("eissn");
    query.addFacetField("subject_facet");
    return query;
  }

  private StringBuilder addFields(String operation, String textSearchOption, String searchString) {
    StringBuilder sb = new StringBuilder();
    for(String token : searchString.split(" ")) {
      if (token.trim().length() < 1) {
        continue;
      }

      if(sb.length() == 0) {
        sb.append(" ( ");
      } else {
        sb.append(operation);
      }

      if ("abstract".equals(textSearchOption)) {
        sb.append("abstract:").append(token);
      } else if ("refs".equals(textSearchOption)) {
        sb.append("citation:").append(token);
      } else if ("title".equals(textSearchOption)) {
        sb.append("title:").append(token);
      } else {
        sb.append(token);
      }
    }

    if (sb.length() > 0) {
      sb.append(" ) ");
    }

    return sb;
  }

  /**
   * Reminder: do NOT put parentheses around this expression because it can (when combined with
   * certain other search terms) give a result of zero rows, even though (logically) some rows
   * should be returned.
   *
   * @param operation
   * @param textSearchOption
   * @param searchString
   * @return
   */
  private StringBuilder addFieldsWithoutTheWords(String operation, String textSearchOption, String searchString) {
    StringBuilder sb = new StringBuilder();
    for(String token : searchString.split(" ")) {
      if (token.trim().length() < 1) {
        continue;
      }

      if(sb.length() > 0) {
        sb.append(operation);
      }

      if ("abstract".equals(textSearchOption)) {
        sb.append("-abstract:").append(token);
      } else if ("refs".equals(textSearchOption)) {
        sb.append("-citation:").append(token);
      } else if ("title".equals(textSearchOption)) {
        sb.append("-title:").append(token);
      } else {
        sb.append("-").append(token).append(" ");
      }
    }
    return sb;
  }

  private SearchResultSinglePage readQueryResults(QueryResponse queryResponse, SolrQuery query) {
    SolrDocumentList documentList = queryResponse.getResults();

    if (log.isInfoEnabled()) {
      StringBuilder filterQueriesForLog = new StringBuilder();
      if (query.getFilterQueries() != null && query.getFilterQueries().length > 0) {
        for (String filterQuery : query.getFilterQueries()) {
          filterQueriesForLog.append(filterQuery).append(" , ");
        }
        if (filterQueriesForLog.length() > 3) {
          filterQueriesForLog.replace(filterQueriesForLog.length() - 3, filterQueriesForLog.length(), "");
        } else {
          filterQueriesForLog.append("No Filter Queries");
        }
      }

      log.info("  ***  query.getQuery():{ " + query.getQuery() + " }"
          + ", query.getFilterQueries():{ " + filterQueriesForLog.toString() + " }"
          + ", found:" + documentList.getNumFound()
          + ", start:" + documentList.getStart()
          + ", max_score:" + documentList.getMaxScore()
          + ", QTime:" + queryResponse.getQTime() + "ms");

      // TODO: implement spell-checking in a meaningful manner.  This loop exists only to generate log output.
      // TODO: Add "spellcheckAlternatives" or something like it to the SearchHits class so it can be displayed to the user like Google's "did you mean..."
      // TODO: Turn off spellchecking for the "author" field.
//      Map<String, List<String>> spellcheckAlternatives = new LinkedHashMap<String, List<String>>();
//        spellcheckAlternatives.put(token, queryResponse.getSpellCheckResponse().getSuggestionMap().get(token).getAlternatives());
      if (queryResponse != null && queryResponse.getSpellCheckResponse() != null
          && queryResponse.getSpellCheckResponse().getSuggestionMap() != null
          && queryResponse.getSpellCheckResponse().getSuggestionMap().keySet().size() > 0) {
        StringBuilder sb = new StringBuilder("Spellcheck alternative suggestions:");
        for (String token : queryResponse.getSpellCheckResponse().getSuggestionMap().keySet()) {
          sb.append(" { ").append(token).append(" : ");
          if (queryResponse.getSpellCheckResponse().getSuggestionMap().get(token).getAlternatives().size() < 1) {
            sb.append("NO ALTERNATIVES");
          } else {
            for ( String alternative : queryResponse.getSpellCheckResponse().getSuggestionMap().get(token).getAlternatives()) {
              sb.append(alternative).append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), ""); // Remove last comma and space.
          }
          sb.append(" } ,");
        }
        log.info(sb.replace(sb.length() - 2, sb.length(), "").toString()); // Remove last comma and space.
      } else {
        log.info("Solr thinks everything in the query is spelled correctly.");
      }

      // TODO: Implement faceting
      // TODO: Turn on these facet debug statements and reformat them in a more condensed and readable format.
//      if (queryResponse.getFacetFields().size() > 0) {
//        for (FacetField facetField : queryResponse.getFacetFields()) {
//          log.info(" ### facetField.getName() = " + facetField.getName() + " ::: facetField.getValueCount() = " + facetField.getValueCount());
//          for (FacetField.Count count : facetField.getValues()) {
//            log.info("   %%% count.getName() = " + count.getName() + " ::: count.getCount() = " + count.getCount()
//                + " ::: count.getAsFilterQuery() = " + count.getAsFilterQuery());
//          }
//        }
//      } else {
//        log.info(" ### There is no facet information for this query.");
//      }
    } //  end if (log.isInfoEnabled()).

    Map<String, Map<String, List<String>>> highlightings = queryResponse.getHighlighting();

    List<SearchHit> searchResults = new ArrayList<SearchHit>();
    for (SolrDocument document : documentList) {

      String id = getFieldValue(document, "id", String.class, query.toString());
      String message = id == null ? query.toString() : id;
      Float score = getFieldValue(document, "score", Float.class, message);
      String title = getFieldValue(document, "title", String.class, message);
      Date publicationDate = getFieldValue(document, "publication_date", Date.class, message);
      String eissn = getFieldValue(document, "eissn", String.class, message);
      String journal = getFieldValue(document, "journal", String.class, message);
      String articleType = getFieldValue(document, "article_type", String.class, message);

      List<String> authorList = getFieldMultiValue(document, message, String.class, "author");

      String highlights = null;
      if (query.getHighlight()) {
        highlights = getHighlights(query.getHighlightFields(), highlightings.get(id));
      }


      SearchHit hit = new SearchHit(
          score, id, title, highlights, authorList, publicationDate, eissn, journal, articleType);

      if (log.isDebugEnabled())
        log.debug(hit.toString());

      searchResults.add(hit);
    }

    // here we assume that number of hits is always going to be withing range of int
    return new SearchResultSinglePage((int) documentList.getNumFound(), -1, searchResults);
  }

  private <T> T getFieldValue(SolrDocument document, String fieldName, Class<T> type, String message) {
    Object value = document.getFieldValue(fieldName);
    if (value != null) {
      if (type.isInstance(value)) {
        return type.cast(value);
      } else {
        log.error("Field " + fieldName + " is not of type " + type.getName() + " for " + message);
      }
    } else {
      log.warn("No \'" + fieldName + "\' field for " + message);
    }

    return null;
  }

  private <T> List<T> getFieldMultiValue(SolrDocument document, String message, Class<T> type, String fieldName) {
    List<T> authorList = new ArrayList<T>();
    Object authors = document.getFieldValue(fieldName);
    if (authors != null) {
      if (authors instanceof Collection) {
        authorList.addAll((Collection<T>) authors);
      } else {
        T value = getFieldValue(document, fieldName, type, message);
        if (value != null) {
          authorList.add(value);
        }
      }
    } else {
      log.warn("No \'" + fieldName + "\' field for " + message);
    }
    return authorList;
  }

  private String getHighlights(String[] highlightFields, Map<String, List<String>> articleHighlights) {
    String hitHighlights = null;
    if (articleHighlights != null) {
      List<String> snippets = new LinkedList<String>();
      for (String highlightField : highlightFields) {
        if (articleHighlights.get(highlightField) != null && articleHighlights.get(highlightField).size() > 0) {
          snippets.addAll(articleHighlights.get(highlightField));
        }
      }
      if (snippets.size() > 0) {
        StringBuilder sb = new StringBuilder();
        for (String snippet : snippets) {
          if (sb.length() > 0) {
            sb.append(" ... ");
          }
          sb.append(snippet);
        }

        hitHighlights = sb.toString();
      }
    }
    return hitHighlights;
  }

  /**
   * Remove dangerous and unwanted values from the Strings in selected fields in the SearchParameters parameter.
   *
   * @param searchParameters A SearchParameters object the needs to have some of its fields "cleaned"
   * @return The SearchParameters parameter with some of its fields "cleaned"
   */
  private SearchParameters cleanStrings(SearchParameters searchParameters) {
    SearchParameters sp = searchParameters.copy();
    sp.setQuery(cleanString(searchParameters.getQuery()));
    if (searchParameters.getCreator().length > 0) {
      String [] tempCreator = new String[searchParameters.getCreator().length];
      int counter = 0;
      for (String author : searchParameters.getCreator()) {
        tempCreator[counter++] = cleanString(author);
      }
      sp.setCreator(tempCreator);
    }
    sp.setTextSearchAll(cleanString(searchParameters.getTextSearchAll()));
    sp.setTextSearchAtLeastOne(cleanString(searchParameters.getTextSearchAtLeastOne()));
    sp.setTextSearchExactPhrase(cleanString(searchParameters.getTextSearchExactPhrase()));
    sp.setTextSearchWithout(cleanString(searchParameters.getTextSearchWithout()));
    return sp;
  }

  /**
   * Change all input to lower case and, in front of each character that Solr recognizes as
   * an operator, place a backslash (i.e., \) so that these characters are "escaped" such
   * that they may be used as normal characters in searches.
   * <p/>
   * Since Solr uses upper case to define the operators <code>AND</code>,  <code>OR</code>,
   * <code>NOT</code>, and  <code>TO</code>, setting these values to lower case means that they
   * are not seen as operators by Solr.
   * 
   * @param toBeCleaned String that will have each Solr operator-character "escaped" with a backslash
   * @return The original <code>toBeCleaned</code> object with each Solr operator-character
   *   "escaped" with a backslash
   */
  private String cleanString(String toBeCleaned) {
    return toBeCleaned.replaceAll("[:!&\"\'\\^\\+\\-\\|\\(\\)\\[\\]\\{\\}\\\\]", "\\\\$0").toLowerCase();
  }
}