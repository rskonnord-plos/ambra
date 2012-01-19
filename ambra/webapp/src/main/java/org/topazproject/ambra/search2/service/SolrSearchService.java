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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

  //  TODO: Dynamically generate an array of field names to highlight.  Should include ALL of the fields that
  //  TODO:    the user is searching in.  (e.g., "anywhere in article" should include highlights from citations, title, etc.)
  private static final String HIGHLIGHT_FIELD = "body"; 

  public void setConfiguration(Configuration configuration) {
    queryTimeout = configuration.getInt("ambra.services.search.timeout", 60000); // default to 1 min
  }

  public void setServerFactory(SolrServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }

  private SearchResultSinglePage search(SolrQuery query) throws ApplicationException {

    if (serverFactory.getServer() == null) {
      throw new ApplicationException("Search server is not configured");
    }

    log.debug("The submitted SolrQuery is: " + query);

    QueryResponse queryResponse;
    try {
      queryResponse = serverFactory.getServer().query(query);
    } catch (SolrServerException e) {
      log.error("Unable to execute a query on the Solr Server.", e);
      throw new ApplicationException("Unable to execute a query on the Solr Server.", e);
    }

    return readQueryResults(queryResponse, query);
  }

  public SearchResultSinglePage simpleSearch(String query, int startPage, int pageSize) throws ApplicationException {
    log.debug("Simple Search performed on the String: " + query
        + " startPage: " + startPage
        + " pageSize: " + pageSize);

    return search(createQuery(query, startPage, pageSize));
  }

  private SolrQuery createQuery(String queryString, int startPage, int pageSize) {
    SolrQuery query = new SolrQuery(queryString);
    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(true);
    query.setHighlight(true);
    query.setHighlightFragsize(50);
    query.setHighlightSnippets(3);
    query.setHighlightSimplePre("<span class=\"highlight\">");
    query.setHighlightSimplePost("</span>");
    query.set("hl.fl", HIGHLIGHT_FIELD);
    query.set("hl.usePhraseHighlighter", true);
    query.set("hl.highlightMultiTerm", true);
    query.set("hl.mergeContiguous", true);
    query.setStart(startPage * pageSize);
    query.setRows(pageSize);
    // request only fields that we need to display
    query.setFields("doi", "score", "title", "publication_date", "eissn", "journal", "article_type", "author");
    return query;
  }

  public SearchResultSinglePage advancedSearch(SearchParameters searchParameters) throws ApplicationException {
    log.debug("Advanced Search performed on the SearchParameters: " + searchParameters);

    SolrQuery query = createQuery(null, searchParameters.getStartPage(), searchParameters.getPageSize());

    StringBuilder q = new StringBuilder();

    // Form field description: "Author Name:"
    if (searchParameters.getCreator().length > 0 && StringUtils.isNotBlank(searchParameters.getCreator()[0])) {
      q.append(" ( ");
      for (int i = 0; i < searchParameters.getCreator().length; i++) {
        String creatorName = searchParameters.getCreator()[i];
        if (StringUtils.isNotBlank(creatorName)) {
          q.append(" author:").append(creatorName);
        }
        if (i < searchParameters.getCreator().length - 1
            && StringUtils.isNotBlank(searchParameters.getCreator()[i + 1])) {
          if ("all".equals(searchParameters.getAuthorNameOp())) {
            q.append(" AND ");
          } else {
            q.append(" OR ");
          }
        }
      }
      q.append(" ) ");
    }

    // Form field description: "for at least one of the words:"
    if (searchParameters.getTextSearchAtLeastOne().trim().length() > 0) {
      q.append(" AND ").append(
          addFields(" OR ", searchParameters.getTextSearchOption(), searchParameters.getTextSearchAtLeastOne().trim())
      );
    }

    // Form field description: "for all the words:"
    if (searchParameters.getTextSearchAll().trim().length() > 0) {
      q.append(" AND ").append(
          addFields(" AND ", searchParameters.getTextSearchOption(), searchParameters.getTextSearchAll().trim())
      );
    }

    // Form field description: "for the exact phrase:"
    if (searchParameters.getTextSearchExactPhrase().trim().length() > 0) {
      q.append(" AND \"");
      if ("abstract".equals(searchParameters.getTextSearchExactPhrase())) {
        q.append("abstract:").append(searchParameters.getTextSearchExactPhrase().trim());
      } else if ("refs".equals(searchParameters.getTextSearchOption())) {
        q.append("citation:").append(searchParameters.getTextSearchExactPhrase().trim());
      } else if ("title".equals(searchParameters.getTextSearchOption())) {
        q.append("title:").append(searchParameters.getTextSearchExactPhrase().trim());
      } else {
        q.append(searchParameters.getTextSearchExactPhrase().trim());
      }
      q.append("\" ");
    }

    // Form field description: "without the words:"
    if (searchParameters.getTextSearchWithout().trim().length() > 0) {
      q.append(" AND ").append(
          addFieldsWithoutTheWords(" AND ", searchParameters.getTextSearchOption(),
              searchParameters.getTextSearchWithout().trim())
      );
    }

    // Form field description: "Dates"
    // TODO: This is the wrong way to make this decision because it implicitly relies on the value submitted from the HTML form.
    if (searchParameters.getDateTypeSelect() != null && searchParameters.getDateTypeSelect().trim().length() > 0) {
      q.append(" AND publication_date:[").append(searchParameters.getStartDate()).append("T00:00:00Z TO ")
          .append(searchParameters.getEndDate()).append("T00:00:00Z] ");
    }


    // If there is no query at this point, then there is nothing to search on, so throw exception.
    if (q.length() < 1) {
      throw new ApplicationException("Please enter one or more search terms. ");
    }

    // Form field description: "Journals"
    // FIXME: For performance, this should be done with filters not by concatenating "AND" clause.
    addFilter(q, searchParameters.getJournalOpt(), searchParameters.getLimitToJournal(), " eissn:");

    // Form field description: "Subject Categories"
    // FIXME: For performance, this should be done with filters not by concatenating "AND" clause.
    addFilter(q, searchParameters.getSubjectCatOpt(), searchParameters.getLimitToCategory(), " subject:");

    if (q.indexOf(" AND ") == 0)
      q.replace(0, 4, ""); // Remove the preceding " AND ".

    return search(query.setQuery(q.toString()));
  }

  private void addFilter(StringBuilder q, String option, String[] limits, String field) {
    if ("some".equals(option) && limits.length > 0) { // Option "all" does not modify the query.
      q.append(" AND (");
      for (String limit : limits) {
        q.append(field).append(limit).append(" OR");
      }
      q.replace(q.length() - 3, q.length(), " ) "); // Remove last "OR". Add closing parenthesis.
    }
  }

  private StringBuilder addFields(String operation, String textSearchOption, String searchString) {
    StringBuilder sb = new StringBuilder();
    for(String token : searchString.split(" ")) {

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

    log.info("  ***  query.getQuery():{ " + query.getQuery() + " }"
        + ", found:" + documentList.getNumFound()
        + ", start:" + documentList.getStart()
        + ", max_score:" + documentList.getMaxScore()
        + ", QTime:" + queryResponse.getQTime() + "ms");

    Map<String, Map<String, List<String>>> highlightings = queryResponse.getHighlighting();

    List<SearchHit> searchResults = new ArrayList<SearchHit>();
    for (SolrDocument document : documentList) {

      String doi = getFieldValue(document, "doi", String.class, query.toString());
      String message = doi == null ? query.toString() : doi;
      Float score = getFieldValue(document, "score", Float.class, message);
      String title = getFieldValue(document, "title", String.class, message);
      Date publicationDate = getFieldValue(document, "publication_date", Date.class, message);
      String eissn = getFieldValue(document, "eissn", String.class, message);
      String journal = getFieldValue(document, "journal", String.class, message);
      String articleType = getFieldValue(document, "article_type", String.class, message);

      List<String> authorList = getFieldMultiValue(document, message, String.class, "author");

      String highlights = null;
      if (query.getHighlight()) {
        highlights = getHighlights(highlightings.get(doi));
      }


      SearchHit hit = new SearchHit(
          score, doi, title, highlights, authorList, publicationDate, eissn, journal, articleType);

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


  private String getHighlights(Map<String, List<String>> articleHighlights) {
    String hitHighlights = null;
    if (articleHighlights != null) {
      List<String> snippets = articleHighlights.get(HIGHLIGHT_FIELD);
      if (snippets != null && snippets.size() > 0) {
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
}