/*
 * Copyright (c) 2006-2013 by Public Library of Science
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
package org.ambraproject.service.search;

import org.ambraproject.ApplicationException;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.util.Pair;
import org.ambraproject.views.SearchHit;
import org.ambraproject.views.SearchResultSinglePage;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Service to provide search capabilities for the application.
 *
 * @author Scott Sterling
 * @author Dragisa Krsmanovic
 * @author Joe Osowski
 */
public class SolrSearchService implements SearchService {
  private static final Logger log = LoggerFactory.getLogger(SolrSearchService.class);

  private static final int CACHE_TTL = 3600 * 24;  // one day

  private SolrServerFactory serverFactory;
  private Cache cache;
  private int queryTimeout;

  private static final int MAX_FACET_SIZE = 100;
  private static final int MIN_FACET_COUNT = 1;

  // sort option possible values (sort direction is optional)
  // field desc|asc
  // sum(field1, field2) desc|asc
  // break up the option string on comma: ","
  private static final Pattern SORT_OPTION_PATTERN = Pattern.compile(",(?![^\\(\\)]*\\))");

  private Map validKeywords = null;
  private List pageSizes = null;

  //We have two collections here, as list supports ordering
  //And we want to keep the sorts in the order in which they are defined
  private List displaySorts = null;
  private Map validSorts = null;

  /**
   * Perform an "all the words" search (across most article fields)
   * <p/>
   * It uses <a href="http://wiki.apache.org/solr/DisMaxRequestHandler">DisMax Query Parser</a>.
   *
   * @param sParams The search parameters to use.
   * @return One "page" of articles which contain the terms in <code>queryString</code>
   * @throws ApplicationException Thrown by a failed query attempt
   */
  public SearchResultSinglePage simpleSearch(SearchParameters sParams) throws ApplicationException {
    sParams.setQuery(sParams.getQuery());

    log.debug("Simple Search performed on the String: " + sParams.getQuery());

    //We query SOLR three times.
    // 1 - The main 'big' query
    // 2 - Make the cross journals facet
    // 3 - (If applicable) the Keywords facet

    SolrQuery query = createQuery(sParams.getQuery(),
        sParams.getStartPage(), sParams.getPageSize(), true);

    //Notice: there is some code duplication here. note below
    SolrQuery journalFacetsQuery = createFacetsQuery(sParams.getQuery(), "cross_published_journal_key", true);
    SolrQuery articleTypeFacetsQuery = createFacetsQuery(sParams.getQuery(), "article_type_facet", true);

    //Set filters for the three queries,
    setFilters(query, sParams, false, false);

    //The journals query doesn't get the journal filter and the articles query doesn't get the articles filter
    //Notice: there is some code duplication here. note below
    setFilters(journalFacetsQuery, sParams, true, false);
    setFilters(articleTypeFacetsQuery, sParams, false, true);

    //Set the sort ordering for results, if applicable.
    setSort(query, sParams);

    //If the keywords parameter is specified, we need to change what field we're querying against
    //aka, body, conclusions, materials and methods ... etc ...
    if (sParams.getFilterKeyword().length() > 0) {
      String fieldkey = sParams.getFilterKeyword();

      if (!validKeywords.containsKey(fieldkey)) {
        throw new ApplicationException("Invalid filterKeyword value of " +
            fieldkey + " specified");
      }

      String fieldName = (String) validKeywords.get(fieldkey);

      //Set the field for dismax to use
      query.set("qf", fieldName);
      journalFacetsQuery.set("qf", fieldName);
      articleTypeFacetsQuery.set("qf", fieldName);
    }

    //Perform searches!
    SearchResultSinglePage results = search(query);

    QueryResponse journalFacetsResponse = getSOLRResponse(journalFacetsQuery);
    QueryResponse articleTypeFacetsResponse = getSOLRResponse(articleTypeFacetsQuery);
    FacetField journals = journalFacetsResponse.getFacetField("cross_published_journal_key");
    FacetField articleTypes = articleTypeFacetsResponse.getFacetField("article_type_facet");

    results.setJournalFacet(facetCountsToHashMap(journals));
    results.setArticleTypeFacet(facetCountsToHashMap(articleTypes));

    //Only execute the keyword search facet if the keyword wasn't specified
    if (sParams.getFilterKeyword().length() == 0) {
      SolrQuery keywordFacetQuery = createKeywordFacetQuery(sParams.getQuery());
      setFilters(keywordFacetQuery, sParams, false, false);
      FacetField keywords = facetSearch(keywordFacetQuery, "doc_partial_type");
      results.setKeywordFacet(facetCountsToHashMap(keywords));
    }

    return results;
  }

  /**
   * Execute a Solr search composed from the contents of the <code>SearchParameters.unformattedQuery</code> property.
   * The query is filtered by the journal and category fields also contained in the <code>searchParameters</code>
   * parameter.  No filter is created for date ranges, since that is assumed to be contained in
   * <code>SearchParameters.unformattedQuery</code>.
   *
   * @param searchParameters Contains all the parameters necessary to execute a search against the Solr query engine
   * @return A subset (determined by <code>SearchParameters.startPage</code> and <code>SearchParameters.pageSize</code>
   *         of the results of the Solr query generated from the contents of the <code>searchParameters</code>
   *         parameter
   * @throws ApplicationException Thrown during failed interactions with the Solr Server
   */
  public SearchResultSinglePage advancedSearch(SearchParameters searchParameters) throws ApplicationException {
    SearchParameters sp = cleanStrings(searchParameters); // Does not impact unformattedQuery field.
    if (log.isDebugEnabled()) {
      log.debug("Solr Search performed on the unformattedSearch String: "
          + searchParameters.getUnformattedQuery().trim());
    }

    SolrQuery query = createQuery(null, sp.getStartPage(), sp.getPageSize(), false);
    query.setQuery(searchParameters.getUnformattedQuery().trim());

    SolrQuery journalFacetsQuery = createFacetsQuery(query.getQuery(), "cross_published_journal_key", false);
    SolrQuery articleTypeFacetsQuery = createFacetsQuery(query.getQuery(), "article_type_facet", false);

    setFilters(query, sp, false, false);

    //The journals query doesn't get the journal filter and the articles query doesn't get the articles filter
    //Notice: there is some code duplication here. note above
    setFilters(journalFacetsQuery, sp, true, false);
    setFilters(articleTypeFacetsQuery, sp, false, true);

    setSort(query, sp);

    QueryResponse journalFacetsResponse = getSOLRResponse(journalFacetsQuery);
    QueryResponse articleTypeFacetsResponse = getSOLRResponse(articleTypeFacetsQuery);

    //Notice: there is some code duplication here. note above
    FacetField journals = journalFacetsResponse.getFacetField("cross_published_journal_key");
    FacetField articleTypes = articleTypeFacetsResponse.getFacetField("article_type_facet");

    SearchResultSinglePage results = search(query.setQuery(searchParameters.getUnformattedQuery().trim()));

    results.setJournalFacet(facetCountsToHashMap(journals));
    results.setArticleTypeFacet(facetCountsToHashMap(articleTypes));

    return results;
  }

  /**
   * @inheritDoc
   */
  @Override
  public SearchHit getMostSharedForJournalCategory(String journal, String subjectArea)
      throws ApplicationException {
    SearchParameters sp = new SearchParameters();

    sp.setFilterSubjects(new String[] { subjectArea });
    sp.setFilterJournals(new String[] { journal });
    //We only need one record
    sp.setPageSize(1);
    sp.setStartPage(0);

    //Only search for articles with shares
    //We might turn this info a filter query for a small performance boost
    sp.setUnformattedQuery("alm_twitterCount:[1 TO *] OR alm_facebookCount:[1 TO *]");
    sp.setSortValue("sum(alm_twitterCount, alm_facebookCount) desc");

    SearchResultSinglePage results = advancedSearch(sp);
    if(results.getHits().size() > 0) {
      return results.getHits().get(0);
    } else {
      return null;
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public SearchHit getMostViewedForJournalCategory(String journal, String subjectArea)
      throws ApplicationException {
    SearchParameters sp = new SearchParameters();

    sp.setFilterSubjects(new String[] { subjectArea });
    sp.setFilterJournals(new String[] { journal });
    //We only need one record
    sp.setPageSize(1);
    sp.setStartPage(0);

    //Only search for articles with shares
    //We might turn this info a filter query for a small performance boost
    sp.setUnformattedQuery("counter_total_month:[1 TO *]");
    sp.setSortValue("counter_total_month desc");

    SearchResultSinglePage results = advancedSearch(sp);
    if(results.getHits().size() > 0) {
      return results.getHits().get(0);
    } else {
      return null;
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public SearchHit getMostViewedAllTimeForJournalCategory(String journal, String subjectArea)
      throws ApplicationException {
    SearchParameters sp = new SearchParameters();

    sp.setFilterSubjects(new String[] { subjectArea });
    sp.setFilterJournals(new String[] { journal });
    //We only need one record
    sp.setPageSize(1);
    sp.setStartPage(0);

    //Only search for articles with shares
    //We might turn this info a filter query for a small performance boost
    sp.setUnformattedQuery("counter_total_all:[1 TO *]");
    sp.setSortValue("counter_total_all desc");

    SearchResultSinglePage results = advancedSearch(sp);
    if(results.getHits().size() > 0) {
      return results.getHits().get(0);
    } else {
      return null;
    }
  }



  /**
   * Populate facets of the search object.
   * <p/>
   * If no search results and hence facets are found remove defined filters and try the search again.  Journals will
   * always be the complete list.
   *
   * @param searchParameters The search parameters
   * @return a populared SearchResultSinglePage object
   * @throws ApplicationException
   */
  public SearchResultSinglePage getFilterData(SearchParameters searchParameters) throws ApplicationException {
    //TODO: This function queries SOLR for the journal and article type list
    //We should migrate this away from config and into a database when it is
    //available

    //Does not impact unformattedQuery field.
    SearchParameters sp = cleanStrings(searchParameters);

    String q = searchParameters.getUnformattedQuery().trim();

    //In this use case, if the query string is empty, we want to get facets for everything
    if (q.length() == 0) {
      q = "*:*";
    }

    if (log.isDebugEnabled()) {
      log.debug("Solr Search performed to get facet data on the unformattedSearch String: "
          + q);
    }

    //We want a complete set of facet data.  So first, lets get it all
    SolrQuery query = createQuery("*:*", 0, 0, false);

    //Remove facets we don't use in this case
    query.removeFacetField("author_facet");
    query.removeFacetField("editor_facet");
    query.removeFacetField("affiliate_facet");
    //Add the one we do want in this case.
    query.addFacetField("cross_published_journal_key");
    query.addFacetField("article_type");
    query.setFacetLimit(MAX_FACET_SIZE);

    //Related to JO: http://joborder.plos.org/view.php?id=17480
    //(for now) we don't want to search on Issue Images
    query.addFilterQuery(createFilterNoIssueImageDocuments());

    SearchResultSinglePage preFilterResults = search(query);

    setFilters(query, sp, false, false);

    query.setQuery(q);

    SearchResultSinglePage results = null;
    try {
      results = search(query);
    } catch (SolrException e) {
      query.setQuery("*:*");
      if (log.isWarnEnabled()) {
        log.warn("Solr Search failed on the unformattedSearch String: { " + query.getQuery()
            + " } so the query will be re-run using the String *:* to populate the Filters"
            + " on the Advanced Search page.", e);
      }
    }

    if (results == null || results.getTotalNoOfResults() == 0) {
      //If no results, remove optional filters and try again
      for (String filter : query.getFilterQueries()) {
        if (filter.indexOf(createFilterFullDocuments()) < 0) {
          query.removeFilterQuery(filter);
        }
      }

      results = search(query);

      //If results are STILL empty.  We must return something for subjects and article type.
      //So let's use the global list
      if (results.getTotalNoOfResults() == 0) {
        results.setSubjectFacet(preFilterResults.getSubjectFacet());
        results.setArticleTypeFacet(preFilterResults.getArticleTypeFacet());
      }

      results.setFiltersReset(true);
    }

    //Lets always return ALL values for journals
    //These lists will not be dependant on the user's other
    //selections other then the query
    //However, subjects and article type will be!
    results.setJournalFacet(preFilterResults.getJournalFacet());
    results.setArticleTypeFacet(preFilterResults.getArticleTypeFacet());

    return results;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getAllSubjects(String journal) throws ApplicationException {
    QueryResponse queryResponse = executeSubjectFacetSearch("subject_hierarchy", journal);
    FacetField facet = queryResponse.getFacetField("subject_hierarchy");
    List<String> results = new ArrayList<String>(facet.getValues().size());
    for (FacetField.Count count : facet.getValues()) {
      results.add(count.getName());
    }
    return results;
  }

  /**
   * @inheritDoc
   */
  @Override
  public SubjectCounts getAllSubjectCounts(String journal) throws ApplicationException {
    QueryResponse queryResponse = executeSubjectFacetSearch("subject_facet", journal);
    FacetField facet = queryResponse.getFacetField("subject_facet");
    SubjectCounts results = new SubjectCounts();
    for (FacetField.Count count : facet.getValues()) {
      results.subjectCounts.put(count.getName(), count.getCount());
    }
    results.totalArticles = queryResponse.getResults().getNumFound();
    return results;
  }

  /**
   * Executes a search where results are grouped by one of the subject facets in the solr schema.
   *
   * @param facetName the subject facet of interest.  Depending on the application, this should be
   *     either "subject_facet" or "subject_hierarchy".  The first does not include the entire taxonomy
   *     path, while the second does.
   * @param journal journal of interest
   * @return solr server response
   * @throws ApplicationException
   */
  private QueryResponse executeSubjectFacetSearch(String facetName, String journal) throws ApplicationException {
    SolrQuery query = createQuery("*:*", 0, 0, false);

    // We don't care about results, just facet counts.
    query.setRows(0);

    // We only care about full documents
    query.addFilterQuery(createFilterFullDocuments());
    query.addFilterQuery(createFilterNoIssueImageDocuments());

    // Remove facets we don't use in this case.
    query.removeFacetField("author_facet");
    query.removeFacetField("editor_facet");
    query.removeFacetField("affiliate_facet");
    query.removeFacetField("subject_facet");
    query.removeFacetField("subject_hierarchy");

    // Add the one we do want.
    query.addFacetField(facetName);

    if(journal != null && journal.length() > 0) {
      query.addFilterQuery("cross_published_journal_key:" + journal);
    }

    query.setFacetLimit(-1);  // unlimited
    return getSOLRResponse(query);
  }

  /**
   * @enheritDoc
   */
  public SortedMap<String, Long> getTopSubjects() throws ApplicationException {
    if (cache == null) {
      return getTopSubjectsFromSOLR();
    } else {
      String key = "topLevelCategoriesCacheKey".intern();
      return cache.get(key, CACHE_TTL,
        new Cache.SynchronizedLookup<SortedMap<String, Long>, ApplicationException>(key) {
          @Override
          public SortedMap<String, Long> lookup() throws ApplicationException {
            return getTopSubjectsFromSOLR();
          }
        });
    }
  }

  private SortedMap<String, Long> getTopSubjectsFromSOLR() throws ApplicationException {
    SolrQuery query = createQuery("*:*", 0, 0, false);

    // We don't care about results, just facet counts.
    query.setRows(0);

    // We only care about full documents
    query.addFilterQuery(createFilterFullDocuments());

    // Remove facets we don't use in this case.
    query.removeFacetField("author_facet");
    query.removeFacetField("editor_facet");
    query.removeFacetField("affiliate_facet");
    query.removeFacetField("subject_facet");

    // Add the one we do want.
    query.addFacetField("subject_level_1");
    query.setFacetLimit(-1);  // unlimited

    QueryResponse queryResponse = getSOLRResponse(query);
    FacetField facet = queryResponse.getFacetField("subject_level_1");

    SortedMap<String, Long> results = new TreeMap<String, Long>();

    //If there is no facet.  Should never happen outside a unit test
    if(facet.getValues() == null) {
      log.warn("No subject_level_1 facet");
    } else {
      for (FacetField.Count count : facet.getValues()) {
        results.put(count.getName(), count.getCount());
      }
    }

    return results;
  }

  /**
   * Add a <i>sort</i> (on a single field) clause to the <code>query</code> parameter.  If the
   * <code>SearchParameters.sort</code> variable contains a single value (no white space), then that value is assumed to
   * be a field name.  If the <code>SearchParameters.sort</code> variable contains two values (separated by whitespace),
   * then the first is assumed to be a field name and the second is assumed to be a <i>sort direction</i>, one of
   * <strong>desc</strong> or <strong>asc</strong>.
   * <p/>
   * If there is only one value in the <code>SearchParameters.sort</code> variable or if the second value is not
   * (non-case-sensitive) <strong>asc</strong>, then the <i>sort direction</i> defaults to <strong>desc</strong>.
   *
   * @param query The SolrQuery which will have a <i>sort</i> clause attached
   * @param sp    The SearchParameters DTO which contains the <code>sort</code> field used by this method
   */
  private void setSort(SolrQuery query, SearchParameters sp) throws ApplicationException {
    if (log.isDebugEnabled()) {
      log.debug("SearchParameters.sort = " + sp.getSortKey());
    }

    if (sp.getSortKey().length() > 0 || (sp.getSortValue() != null && sp.getSortValue().length() > 0)) {
      String sortKey = sp.getSortKey();
      String sortValue = (String) validSorts.get(sortKey);

      //This bit allows a consumer of the method to explicitly set the sort instead of specifying it by key
      if(sp.getSortValue() != null && sp.getSortValue().length() > 0) {
        sortValue = sp.getSortValue();
      } else {
        if (sortValue == null) {
          throw new ApplicationException("Invalid sort key of '" + sp.getSortKey() + "' specified.");
        }
      }

      String[] sortOptions = SORT_OPTION_PATTERN.split(sortValue);
      for (String sortOption : sortOptions) {
        sortOption = sortOption.trim();
        int index = sortOption.lastIndexOf(" ");

        String fieldName = sortOption;
        String sortDirection = null;

        if (index != -1) {
          fieldName = sortOption.substring(0, index);
          sortDirection = sortOption.substring(index + 1).trim();
        }

        if (sortDirection == null || !sortDirection.toLowerCase().equals("asc")) {
          query.addSortField(fieldName, SolrQuery.ORDER.desc);
        } else {
          query.addSortField(fieldName, SolrQuery.ORDER.asc);
        }
      }
    }

    if (query.getSortField() == null || query.getSortField().length() == 0) {
      //Always default to score if it's not defined
      query.addSortField("score", SolrQuery.ORDER.desc);
      //If two articles are ranked the same, give the one with a more recent publish date a bump
      query.addSortField("publication_date", SolrQuery.ORDER.desc);
      //If everything else is equal, order by id
      query.addSortField("id", SolrQuery.ORDER.desc);
    }
  }

  /**
   * Execute a Solr search composed from the contents of the <i>Find An Article</i> search block including the
   * properties: <code>volume</code>, <code>eNumber</code>, and/or <code>id</code> (DOI).
   * <p/>
   * The query is filtered by the <code>SearchParameters.filterJournals</code> property also contained in the
   * <code>searchParameters</code> parameter.
   * <p/>
   * No filter is created for date ranges or subject categories.
   *
   * @param searchParameters Contains all the parameters necessary to execute a search against the Solr query engine
   * @return A subset (determined by <code>SearchParameters.startPage</code> and <code>SearchParameters.pageSize</code>
   *         of the results of the Solr query generated from the contents of the <code>searchParameters</code>
   *         parameter
   * @throws ApplicationException Thrown during failed interactions with the Solr Server
   */
  public SearchResultSinglePage findAnArticleSearch(SearchParameters searchParameters) throws ApplicationException {
    SearchParameters sp = cleanStrings(searchParameters); // Does not impact unformattedQuery field.
    if (log.isDebugEnabled()) {
      log.debug("Solr Search performed on the following selection of the SearchParameters properties: "
          + "{ filterJournals=" + (sp.getFilterJournals() == null ? null : Arrays.asList(sp.getFilterJournals()))
          + "\', volume = " + sp.getVolume()
          + "\', eLocationId = " + sp.getELocationId()
          + "\', id = " + sp.getId() + "\' }");
    }

    // We should always have exactly one journal.
    if (sp.getFilterJournals().length != 1) {
      throw new ApplicationException("Please select exactly one journal.");
    }

    SolrQuery query = createQuery(null, sp.getStartPage(), sp.getPageSize(), false);

    // If ID exists, then search on that first, ignoring all the other fields.
    if (sp.getId().length() > 0) {
      query.setQuery("id:\"" + sp.getId() + "\"");
      return search(query);
      //if (resultsFromId.getTotalNoOfResults() > 0) {
      //  return resultsFromId;
      //}
    }

    // If no ID or if ID search gives no results,
    // then attempt a query based on the other submitted fields, if those fields exist

    int volume = 0;
    try {
      volume = Integer.parseInt(sp.getVolume());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Unable to create an integer from the String volume = " + sp.getVolume());
      }
    }

    StringBuilder q = new StringBuilder(); // The Query which will be submitted to Solr.

    if (volume > 0) {
      q.append(" volume:").append(volume);
    }
    if (sp.getELocationId().length() > 0) {
      if (q.length() > 0) {
        q.append(" AND ");
      }
      q.append(" elocation_id:").append(sp.getELocationId());
    }

    if (log.isDebugEnabled()) {
      log.debug("findAnArticleSearch: query = " + q.toString());
    }

    query.setQuery(q.toString());

    // Form field description: "Journals".  Query Filter.
    query.addFilterQuery(createFilterLimitForJournals(sp.getFilterJournals()));

    return search(query);
  }

  public void setConfiguration(Configuration config) throws ApplicationException {
    queryTimeout = config.getInt("ambra.services.search.timeout", 60000); // default to 1 min

    List sizes = config.getList("ambra.services.search.pageSizes.size");

    if (sizes == null) {
      throw new ApplicationException("ambra.services.search.pageSizes not defined " +
          "in configuration.");
    }

    pageSizes = sizes;

    if (config.containsKey("ambra.services.search.sortOptions.option")) {
      validSorts = new HashMap();
      displaySorts = new ArrayList();

      HierarchicalConfiguration hc = (HierarchicalConfiguration) config;
      List<HierarchicalConfiguration> sorts =
          hc.configurationsAt("ambra.services.search.sortOptions.option");

      for (HierarchicalConfiguration s : sorts) {
        String key = s.getString("[@displayName]");
        String value = s.getString("");
        validSorts.put(key, value);
        displaySorts.add(key);
      }

      ((HierarchicalConfiguration) config).setExpressionEngine(null);
    } else {
      throw new ApplicationException("ambra.services.search.sortOptions.option not defined " +
          "in configuration.");
    }

    if (config.containsKey("ambra.services.search.keywordFields.field")) {
      validKeywords = new HashMap();
      HierarchicalConfiguration hc = (HierarchicalConfiguration) config;
      List<HierarchicalConfiguration> sorts =
          hc.configurationsAt("ambra.services.search.keywordFields.field");

      for (HierarchicalConfiguration s : sorts) {
        String key = s.getString("[@displayName]");
        String value = s.getString("");
        validKeywords.put(key, value);

      }
    } else {
      throw new ApplicationException("ambra.services.search.keywordFields.field not defined " +
          "in configuration.");
    }
  }

  public void setServerFactory(SolrServerFactory serverFactory) {
    this.serverFactory = serverFactory;
  }

  private void setFilters(SolrQuery query, SearchParameters sp, boolean ignoreJournals, boolean ignoreArticleTypes) {
    //Related to JO: http://joborder.plos.org/view.php?id=17480
    //(for now) we don't want to search on Issue Images
    query.addFilterQuery(createFilterNoIssueImageDocuments());

    // Form field description: "Journals".  Query Filter.
    if(!ignoreJournals) {
      if (sp.getFilterJournals() != null && sp.getFilterJournals().length > 0) {
        query.addFilterQuery(createFilterLimitForJournals(sp.getFilterJournals()));
      }
    }

    if(!ignoreArticleTypes) {
      // Form field description: "Article Types".  Query Filter.
      if (sp.getFilterArticleTypes() != null && sp.getFilterArticleTypes().length > 0) {
        query.addFilterQuery(createFilterLimitForArticleTypes(sp.getFilterArticleTypes()));
      }
    }

    // Form field description: "Subject Categories".  Query Filter.
    if (sp.getFilterSubjects() != null && sp.getFilterSubjects().length > 0) {
      query.addFilterQuery(createFilterLimitForSubject(sp.getFilterSubjects()));
    }

    // Not used in form, but in savedSearch alerts
    if (sp.getFilterSubjectsDisjunction() != null && sp.getFilterSubjectsDisjunction().length > 0) {
      query.addFilterQuery(createFilterLimitForSubjectDisjunction(sp.getFilterSubjectsDisjunction()));
    }

    // Form field description: "Authors".  Query Filter.
    if (sp.getFilterAuthors() != null && sp.getFilterAuthors().length > 0) {
      query.addFilterQuery(createFilterLimitForAuthor(sp.getFilterAuthors()));
    }

    if (sp.getFilterStartDate() != null && sp.getFilterEndDate() != null) {
      query.addFilterQuery(createFilterLimitForPublishDate(sp.getFilterStartDate(), sp.getFilterEndDate()));
    }
  }

  private String createFilterLimitForPublishDate(Date startDate, Date endDate) {
    StringBuilder fq = new StringBuilder();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String sDate = sdf.format(startDate) + "T00:00:00Z";
    String eDate = sdf.format(endDate) + "T00:00:00Z";

    fq.append("publication_date:[" + sDate + " TO " + eDate + "]");

    return fq.toString();
  }

  private String createFilterLimitForJournals(String[] journals) {
    Arrays.sort(journals); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String journal : journals) {
      fq.append("cross_published_journal_key:").append(journal).append(" OR ");
    }
    return fq.replace(fq.length() - 4, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterLimitForAuthor(String[] authors) {
    Arrays.sort(authors); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String author : authors) {
      fq.append("author:\"").append(author).append("\" AND ");
    }
    return fq.replace(fq.length() - 5, fq.length(), "").toString(); // Remove last " AND".
  }

  private String createFilterLimitForSubject(String[] subjects) {
    Arrays.sort(subjects); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String category : subjects) {
      fq.append("subject:\"").append(category).append("\" AND ");
    }
    return fq.replace(fq.length() - 5, fq.length(), "").toString(); // Remove last " AND".
  }

  private String createFilterLimitForSubjectDisjunction(String[] subjects) {
    Arrays.sort(subjects); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String category : subjects) {
      fq.append("subject:\"").append(category).append("\" OR ");
    }
    return fq.replace(fq.length() - 4, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterLimitForArticleTypes(String[] articleTypes) {
    Arrays.sort(articleTypes); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String articleType : articleTypes) {
      fq.append("article_type:\"").append(articleType).append("\" OR ");
    }
    return fq.replace(fq.length() - 4, fq.length(), "").toString(); // Remove last " OR".
  }

  /**
   * Filter that limits results to only the complete documents, excluding partial documents.
   *
   * @return A filter that excludes partial documents
   */
  private String createFilterFullDocuments() {
    return "doc_type:full";
  }

  private String createFilterPartialDocuments() {
    return "doc_type:partial";
  }

  private String createFilterNoIssueImageDocuments() {
    return "!article_type_facet:\"Issue Image\"";
  }

  private QueryResponse getSOLRResponse(SolrQuery query) throws ApplicationException {

    if (serverFactory.getServer() == null) {
      throw new ApplicationException("Search server is not configured");
    }

    QueryResponse queryResponse;
    try {
      log.info("SOLR Query: " + query.toString());
      queryResponse = serverFactory.getServer().query(query);
      log.info("SOLR Query response time(milliseconds): " + queryResponse.getElapsedTime());
    } catch (SolrServerException e) {
      throw new ApplicationException("Unable to execute a query on the Solr Server.", e);
    }

    return queryResponse;
  }

  private SearchResultSinglePage search(SolrQuery query) throws ApplicationException {
    QueryResponse queryResponse = getSOLRResponse(query);

    return readQueryResults(queryResponse, query);
  }

  private FacetField facetSearch(SolrQuery query, String name) throws ApplicationException {
    QueryResponse queryResponse = getSOLRResponse(query);

    FacetField facet = queryResponse.getFacetField(name);

    if (facet == null) {
      throw new ApplicationException("No facet found with name of:" + name);
    }

    return facet;
  }

  private List<Map> facetCountsToHashMap(FacetField field) {
    List<FacetField.Count> counts = field.getValues();
    ArrayList<Map> result = new ArrayList<Map>();

    if (counts != null) {
      for (FacetField.Count count : counts) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("name", count.getName());
        hm.put("count", count.getCount());
        result.add(hm);
      }
      return result;
    } else {
      return null;
    }
  }

  private SolrQuery createQuery(String queryString, int startPage, int pageSize, boolean useDismax) {
    SolrQuery query = new SolrQuery(queryString);
    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(true); // The relevance (of each results element) to the search terms.
    query.setHighlight(false);

    if (useDismax) {
      query.set("defType", "dismax");
    }

    //TODO: Put The "options" from the "queryField" picklist into a config file.
    //This list matches the "options" from the "queryField" picklist on unformattedSearch.ftl,
    //without the "date" fields.

    query.setStart(startPage * pageSize); // Which results element to return first in this batch.
    query.setRows(pageSize); // The number of results elements to return.
    // request only fields that we need to display
    query.setFields("id", "score", "title_display", "publication_date", "eissn", "journal", "article_type",
        "author_display", "abstract", "abstract_primary_display", "striking_image", "figure_table_caption",
        "subject", "expression_of_concern", "retraction");
    query.addFacetField("subject_facet");
    query.addFacetField("author_facet");
    query.addFacetField("editor_facet");
    query.addFacetField("article_type_facet");
    query.addFacetField("affiliate_facet");
    query.set("facet.method", "fc");
    query.setFacetLimit(MAX_FACET_SIZE);
    query.setFacetMinCount(MIN_FACET_COUNT);
    // Add a filter to ensure that Solr never returns partial documents
    query.addFilterQuery(createFilterFullDocuments());

    return query;
  }

  private SolrQuery createFacetsQuery(String queryString, String field, boolean useDismax) {
    SolrQuery query = new SolrQuery(queryString);
    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(false);
    query.setHighlight(false);
    query.setRows(0);
    query.setFacetLimit(MAX_FACET_SIZE);
    query.setFacetMinCount(MIN_FACET_COUNT);

    if (useDismax) {
      query.set("defType", "dismax");
    }

    query.addFacetField(field);
    // Add a filter to ensure that Solr never returns partial documents
    query.addFilterQuery(createFilterFullDocuments());

    return query;
  }

  private SolrQuery createKeywordFacetQuery(String queryString) {
    SolrQuery query = new SolrQuery();
    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(false);
    query.setHighlight(false);
    query.setRows(0);
    query.set("defType", "dismax");
    query.set("qf", "doc_partial_body");
    query.addFacetField("doc_partial_type");
    query.setFacetLimit(MAX_FACET_SIZE);
    query.setFacetMinCount(MIN_FACET_COUNT);
    // Add a filter to ensure that Solr never returns partial documents
    query.addFilterQuery(createFilterPartialDocuments());
    query.setQuery(queryString);

    return query;
  }

  @SuppressWarnings("unchecked")
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

      log.info("query.getQuery():{ " + query.getQuery() + " }"
          + ", query.getSortFields():{ " + (query.getSortFields() == null ? null : Arrays.asList(query.getSortFields())) + " }"
          + ", query.getFilterQueries():{ " + filterQueriesForLog.toString() + " }"
          + ", found:" + documentList.getNumFound()
          + ", start:" + documentList.getStart()
          + ", max_score:" + documentList.getMaxScore()
          + ", QTime:" + queryResponse.getQTime() + "ms");

      // TODO: implement spell-checking in a meaningful manner.  This loop exists only to generate log output.
      // TODO: Add "spellcheckAlternatives" or something like it to the SearchHits class so it can be displayed to the user like Google's "did you mean..."
      // TODO: Turn off spellchecking for the "author" field.
      if (queryResponse.getSpellCheckResponse() != null
          && queryResponse.getSpellCheckResponse().getSuggestionMap() != null
          && queryResponse.getSpellCheckResponse().getSuggestionMap().keySet().size() > 0) {
        StringBuilder sb = new StringBuilder("Spellcheck alternative suggestions:");
        for (String token : queryResponse.getSpellCheckResponse().getSuggestionMap().keySet()) {
          sb.append(" { ").append(token).append(" : ");
          if (queryResponse.getSpellCheckResponse().getSuggestionMap().get(token).getAlternatives().size() < 1) {
            sb.append("NO ALTERNATIVES");
          } else {
            for (String alternative : queryResponse.getSpellCheckResponse().getSuggestionMap().get(token).getAlternatives()) {
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
    }

    List<SearchHit> searchResults = new ArrayList<SearchHit>();
    for (SolrDocument document : documentList) {

      String id = SolrServiceUtil.getFieldValue(document, "id", String.class, query.toString());
      String message = id == null ? query.toString() : id;
      Float score = SolrServiceUtil.getFieldValue(document, "score", Float.class, message);
      String title = SolrServiceUtil.getFieldValue(document, "title_display", String.class, message);
      Date publicationDate = SolrServiceUtil.getFieldValue(document, "publication_date", Date.class, message);
      String eissn = SolrServiceUtil.getFieldValue(document, "eissn", String.class, message);
      String journal = SolrServiceUtil.getFieldValue(document, "journal", String.class, message);
      String articleType = SolrServiceUtil.getFieldValue(document, "article_type", String.class, message);
      String strikingImage = SolrServiceUtil.getFieldValue(document, "striking_image", String.class, message);
      List<String> abstractText = SolrServiceUtil.getFieldMultiValue(document, "abstract", String.class, message);
      List<String> abstractPrimary = SolrServiceUtil.getFieldMultiValue(document, "abstract_primary_display", String.class, message);
      List<String> authorList = SolrServiceUtil.getFieldMultiValue(document, "author_display", String.class, message);
      // TODO create a dedicated field for checking the existence of assets for a given article.
      List<String> figureTableCaptions = SolrServiceUtil.getFieldMultiValue(document, "figure_table_caption", String.class, message);
      List<String> subjects = SolrServiceUtil.getFieldMultiValue(document, "subject", String.class, message);
      List<String> expressionOfconcern = SolrServiceUtil.getFieldMultiValue(document, "expression_of_concern", String.class, message);
      String retraction = SolrServiceUtil.getFieldValue(document, "retraction", String.class, message);
      String abstractResult = "";

      //Use the primary abstract if it exists
      if (abstractPrimary.size() > 0) {
        abstractResult = StringUtils.join(abstractPrimary, ", ");
      } else {
        if (abstractText.size() > 0) {
          abstractResult = StringUtils.join(abstractText, ", ");
        }
      }

      //Flatten the list of subjects to a unique set
      Set<String> flattenedSubjects = new HashSet<String>();
      for(String subject : subjects) {
        for(String temp : subject.split("/")) {
          if(temp.trim().length() > 0) {
            flattenedSubjects.add(temp);
          }
        }
      }

      SearchHit hit = SearchHit.builder()
        .setHitScore(score)
        .setUri(id)
        .setTitle(title)
        .setListOfCreators(authorList)
        .setDate(publicationDate)
        .setIssn(eissn)
        .setJournalTitle(journal)
        .setArticleTypeForDisplay(articleType)
        .setAbstractText(abstractResult)
        .setStrikingImage(strikingImage)
        .setHasAssets(figureTableCaptions.size() > 0)
        .setSubjects(flattenedSubjects)
        .setSubjectsPolyhierarchy(subjects)
        .setExpressionOfConcern(expressionOfconcern)
        .setRetraction(retraction)
        .build();

      if (log.isDebugEnabled())
        log.debug(hit.toString());

      searchResults.add(hit);
    }

    //here we assume that number of hits is always going to be withing range of int
    SearchResultSinglePage results = new SearchResultSinglePage((int) documentList.getNumFound(), -1,
        searchResults, query.getQuery());

    if (queryResponse.getFacetField("subject_facet") != null) {
      List<Map> subjects = facetCountsToHashMap(queryResponse.getFacetField("subject_facet"));

      if(subjects != null) {
        List<Map> subjectResult = new ArrayList<Map>();
        SortedMap<String, Long> topSubjects = null;

        try {
          topSubjects = getTopSubjects();
        } catch (ApplicationException ex) {
          throw new RuntimeException(ex.getMessage(), ex);
        }

        //Remove top level 1 subjects from list, FEND-805
        for(Map<String, Object> m : subjects) {
          if(!topSubjects.containsKey(m.get("name"))) {
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("name", m.get("name"));
            hm.put("count", m.get("count"));
            subjectResult.add(hm);
          }
        }

        results.setSubjectFacet(subjectResult);
      } else {
        results.setSubjectFacet(null);
      }
    }

    if (queryResponse.getFacetField("author_facet") != null) {
      results.setAuthorFacet(facetCountsToHashMap(queryResponse.getFacetField("author_facet")));
    }

    if (queryResponse.getFacetField("editor_facet") != null) {
      results.setEditorFacet(facetCountsToHashMap(queryResponse.getFacetField("editor_facet")));
    }

    if (queryResponse.getFacetField("article_type_facet") != null) {
      results.setArticleTypeFacet(facetCountsToHashMap(queryResponse.getFacetField("article_type_facet")));
    }

    if (queryResponse.getFacetField("affiliate_facet") != null) {
      results.setInstitutionFacet(facetCountsToHashMap(queryResponse.getFacetField("affiliate_facet")));
    }

    if (queryResponse.getFacetField("cross_published_journal_key") != null) {
      results.setJournalFacet(facetCountsToHashMap(queryResponse.getFacetField("cross_published_journal_key")));
    }

    return results;
  }

  /**
   * @inheritDoc
   */
  public List savedSearchAlerts(SearchParameters sParams, Date lastSearchTime, Date currentSearchTime, int resultLimit) throws ApplicationException {
    SolrQuery query = null;
    SearchParameters sp = null;

    if (sParams.getUnformattedQuery() == null || sParams.getUnformattedQuery().equals("")) {
      if (log.isDebugEnabled()) {
        log.debug("Simple Saved Search performed on the unformattedSearch String: "
            + sParams.getQuery().trim());
      }

      query = createQuery(sParams.getQuery(), 0, resultLimit, false);
      query.setQuery(sParams.getQuery());
      //If the keywords parameter is specified, we need to change what field we're querying against
      //aka, body, conclusions, materials and methods ... etc ...
      if (sParams.getFilterKeyword().length() > 0) {
        String fieldkey = sParams.getFilterKeyword();

        if (!validKeywords.containsKey(fieldkey)) {
          throw new ApplicationException("Invalid filterKeyword value of " +
              fieldkey + " specified");
        }

        String fieldName = (String) validKeywords.get(fieldkey);

        //Set the field for dismax to use
        query.set("qf", fieldName);
      }
      setFilters(query, sParams, false, false);

    } else {

      log.debug("Advanced Saved Search performed on the unformattedSearch String: {}",
          sParams.getUnformattedQuery().trim());
      sp = cleanStrings(sParams);
      query = createQuery(null, 0, resultLimit, false);
      query.setQuery(sParams.getUnformattedQuery());
      setFilters(query, sp, false, false);
    }

    query.addFilterQuery(createFilterLimitForPublishDate(lastSearchTime, currentSearchTime));

    SearchResultSinglePage results = search(query);

    return results.getHits();
  }


  /**
   * Remove dangerous and unwanted values from the Strings in selected fields in the SearchParameters parameter.
   * <p/>
   * Note that <code>SearchParameters.unformattedQuery</code> is excluded from this list, for the reason implied by its
   * name.
   *
   * @param searchParameters A SearchParameters object the needs to have some of its fields "cleaned"
   * @return The SearchParameters parameter with some of its fields "cleaned"
   */
  private SearchParameters cleanStrings(SearchParameters searchParameters) {
    SearchParameters sp = searchParameters.copy();
    sp.setQuery(cleanString(searchParameters.getQuery()));
    return sp;
  }

  /**
   * Change all input to lower case and, in front of each character that Solr recognizes as an operator, place a
   * backslash (i.e., \) so that these characters are "escaped" such that they may be used as normal characters in
   * searches.
   * <p/>
   * Since Solr uses upper case to define the operators <code>AND</code>,  <code>OR</code>, <code>NOT</code>, and
   * <code>TO</code>, setting these values to lower case means that they are not seen as operators by Solr.
   *
   * @param toBeCleaned String that will have each Solr operator-character "escaped" with a backslash
   * @return The original <code>toBeCleaned</code> object with each Solr operator-character "escaped" with a backslash
   */
  private String cleanString(String toBeCleaned) {
    return toBeCleaned.replaceAll("[:!&\"\'\\^\\+\\-\\|\\(\\)\\[\\]\\{\\}\\\\]", "\\\\$0").toLowerCase();
  }

  /**
   * The map of sorts that are valid for this provider
   *
   * @return
   */
  public List getSorts() {
    return this.displaySorts;
  }

  /**
   * The valid page sizes for this provider
   *
   * @return
   */
  public List getPageSizes() {
    return pageSizes;
  }

  private static final String DOI_SCHEME = "info:doi/";

  @Override
  public String fetchAbstractText(String articleDoi) throws ApplicationException {
    if (articleDoi.startsWith(DOI_SCHEME)) {
      articleDoi = articleDoi.substring(DOI_SCHEME.length());
    }
    SolrQuery query = new SolrQuery("id:\"" + articleDoi + "\"");
    query.setFields("abstract", "abstract_primary_display");
    List<SearchHit> hits = search(query).getHits();

    if (hits.size() != 1) {
      String message = (hits.isEmpty()) ? "Article not found" : "Non-unique ID";
      throw new ApplicationException(message + ": " + articleDoi);
    }
    String abstractText = hits.get(0).getAbstract();
    if (abstractText == null) {
      // Even an article with no abstract should have produced an empty (non-null) string
      throw new ApplicationException("Abstract not found for article: " + articleDoi);
    }
    return abstractText;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }
}
