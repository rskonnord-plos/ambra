/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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
package org.ambraproject.service.feed;

import org.ambraproject.ApplicationException;
import org.ambraproject.models.Journal;
import org.ambraproject.service.annotation.AnnotationService;
import org.ambraproject.service.article.BrowseService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.journal.JournalService;
import org.ambraproject.service.search.SolrException;
import org.ambraproject.service.search.SolrFieldConversion;
import org.ambraproject.service.search.SolrHttpService;
import org.ambraproject.service.trackback.TrackbackService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.LinkbackView;
import org.ambraproject.views.TOCArticleGroup;
import org.ambraproject.views.article.ArticleInfo;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The <code>FeedService</code> supplies the API for querying feed requests. <code>FeedService</code> is a Spring
 * injected singleton which coordinates access to the <code>annotationService and articleService</code>
 */
public class FeedServiceImpl extends HibernateServiceImpl implements FeedService {
  private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);

  private AnnotationService annotationService;    // Annotation service Spring injected.
  private TrackbackService trackbackService;     // Trackback service Spring injected
  private BrowseService browseService;        // Browse Article Servcie Spring Injected
  private JournalService journalService;       // Journal service Spring injected.
  private SolrHttpService solrHttpService;      // solr service
  private Configuration configuration;
  private SolrFieldConversion solrFieldConverter;

  private int queryTimeout = 60000;
  private Map validSorts = null;
  private List displaySorts = null;
  private Map validKeywords = null;

  private static final Pattern SORT_OPTION_PATTERN = Pattern.compile(",(?![^\\(\\)]*\\))");
  /**
   * Constructor - currently does nothing.
   */
  public FeedServiceImpl() {
  }

  /**
   * Creates and returns a new <code>Key</code> for clients of FeedService.
   *
   * @return Key a new FeedSearchParameters to be used as a data model for the FeedAction.
   */
  @Override
  public FeedSearchParameters newSearchParameters() {
    return new FeedSearchParameters();
  }

  /**
   * Queries for a list of articles from solr using the parameters set in searchParams
   *
   * @param searchParameters
   * @return solr search result that contains list of articles
   */
  @Override
  public Document getArticles(final FeedSearchParameters searchParameters) {
    Map<String, String> params = new HashMap<String, String>();
    // result format
    params.put("wt", "xml");
    // what I want returned, the fields needed for rss feed
    params.put("fl", "id,title_display,publication_date,author_without_collab_display,author_collab_only_display," +
        "author_display,volume,issue,article_type,subject_hierarchy,abstract_primary_display,copyright");

    // filters
    String fq = "doc_type:full " +
        "AND !article_type_facet:\"Issue Image\" " +
        "AND cross_published_journal_key:" + searchParameters.getJournal();

    String[] categories = searchParameters.getCategories();
    if (categories != null && categories.length > 0) {
      StringBuffer sb = new StringBuffer();
      for (String category : categories) {
        sb.append("\"").append(category).append("\" AND ");
      }
      params.put("q", "subject_level_1:(" + sb.substring(0, sb.length() - 5) + ")");
    }

    if (searchParameters.getAuthor() != null) {
      fq = fq + " AND author:\"" + searchParameters.getAuthor() + "\"";
    }

    String startDate = "*";
    String endDate = "*";
    boolean addDateRange = false;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    if (searchParameters.getSDate() != null) {
      startDate = sdf.format(searchParameters.getSDate().getTime());
      startDate = startDate + "T00:00:00Z";
      addDateRange = true;
    }
    if (searchParameters.getEDate() != null) {
      endDate = sdf.format(searchParameters.getEDate().getTime());
      endDate = endDate + "T00:00:00Z";
      addDateRange = true;
    }

    if (addDateRange == true) {
      fq = fq + " AND publication_date:[" + startDate + " TO " + endDate + "]";
    }

    params.put("fq", fq);

    // number of results
    params.put("rows", Integer.toString(searchParameters.getMaxResults()));

    // sort the result

    if (searchParameters.isMostViewed()) {
      // Sorts RSS Feed for the most viewed articles linked from the most viewed tab.
      String mostViewedKey = "ambra.virtualJournals." + journalService.getCurrentJournalName() + ".mostViewedArticles";
      Integer days = configuration.getInt(mostViewedKey + ".timeFrame");
      String sortField = (days != null) ? solrFieldConverter.getViewCountingFieldName(days)
          : solrFieldConverter.getAllTimeViewsField();
      params.put("sort", sortField + " desc");
    } else {
      params.put("sort", "publication_date desc");
    }

    Document result = null;
    try {
      result = solrHttpService.makeSolrRequest(params);
    } catch (SolrException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  public Document getSearchArticles(final FeedSearchParameters sParams) throws ApplicationException {

    log.debug("Performing RSS Feed Search");

    SolrQuery query = null;

    if(sParams.getUnformattedQuery().equals("")){
      log.debug("Simple Rss Search performed on the String: " + sParams.getQuery());
      query = new SolrQuery(sParams.getQuery());
      query.set("defType", "dismax");
    }else {
      log.debug("Simple Rss Search performed on the String: " + sParams.getUnformattedQuery());
      query = new SolrQuery(sParams.getUnformattedQuery());
    }

    query.setTimeAllowed(queryTimeout);
    query.setIncludeScore(true); // The relevance (of each results element) to the search terms.
    query.setHighlight(false);
    // request only fields that we need
    query.setFields("id","title_display","publication_date","author_without_collab_display","author_collab_only_display","author_display","volume","issue","article_type","subject_hierarchy","abstract_primary_display","copyright");
    query.addFilterQuery("doc_type:full");
    query.addFilterQuery("!article_type_facet:\"Issue Image\"");
    query.addFacetField("cross_published_journal_key");

    // Form field description: "Journals".  Query Filter.
    if (sParams.getFilterJournals() != null && sParams.getFilterJournals().length > 0) {
      query.addFilterQuery(createFilterLimitForJournals(sParams.getFilterJournals()));
    }


    // Form field description: "Subject Categories".  Query Filter.
    if (sParams.getFilterSubjects() != null && sParams.getFilterSubjects().length > 0) {
      query.addFilterQuery(createFilterLimitForSubject(sParams.getFilterSubjects()));
    }

    // Form field description: "Article Types".  Query Filter.
    if (sParams.getFilterArticleType() != null && sParams.getFilterArticleType().length > 0) {
      query.addFilterQuery(createFilterLimitForArticleType(sParams.getFilterArticleType()));
    }

    //Set the sort ordering for results, if applicable.
    setSort(query, sParams);

    //If the keywords parameter is specified, we need to change what field we're querying against
    //aka, body, conclusions, materials and methods ... etc ...
    if(sParams.getFilterKeyword().length() > 0) {
      String fieldkey = sParams.getFilterKeyword();

      if(!validKeywords.containsKey(fieldkey)) {
        throw new ApplicationException("Invalid filterKeyword value of " +
            fieldkey + " specified");
      }
      String fieldName = (String)validKeywords.get(fieldkey);
      query.set("qf", fieldName);
    }

    query.set("wt", "xml") ;

    Document result = null;
    try {
      result = solrHttpService.makeSolrRequestForRss(query.toString());
    } catch (SolrException e) {
      e.printStackTrace();
    }
    return result;
  }

  private void setSort(SolrQuery query, FeedSearchParameters sp) throws ApplicationException {
    if (log.isDebugEnabled()) {
      log.debug("SearchParameters.sort = " + sp.getSort());
    }

    if (sp.getSort().length() > 0) {
      String sortKey = sp.getSort();
      String sortValue = (String)validSorts.get(sortKey);

      if(sortValue == null) {
        throw new ApplicationException("Invalid sort of '" + sp.getSort() + "' specified.");
      }

      String[] sortOptions = SORT_OPTION_PATTERN.split(sortValue);
      for (String sortOption: sortOptions) {
        sortOption = sortOption.trim();
        int index = sortOption.lastIndexOf(" ");

        String fieldName = sortOption;
        String sortDirection = null;

        if (index != -1) {
          fieldName = sortOption.substring(0, index);
          sortDirection = sortOption.substring(index + 1).trim();
        }

        if ( sortDirection == null || ! sortDirection.toLowerCase().equals("asc")) {
          query.addSortField(fieldName, SolrQuery.ORDER.desc);
        } else {
          query.addSortField(fieldName, SolrQuery.ORDER.asc);
        }
      }
    }

    if(query.getSortField() == null || query.getSortField().length() == 0) {
      //Always default to score if it's not defined
      query.addSortField("score", SolrQuery.ORDER.desc);
      //If two articles are ranked the same, give the one with a more recent publish date a bump
      query.addSortField("publication_date", SolrQuery.ORDER.desc);
      //If everything else is equal, order by id
      query.addSortField("id", SolrQuery.ORDER.desc);
    }
  }


  private String createFilterLimitForJournals(String[] journals) {
    Arrays.sort(journals); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String journal : journals) {
      fq.append("cross_published_journal_key:").append(journal).append(" OR ");
    }
    return fq.replace(fq.length() - 4, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterLimitForSubject(String[] subjects) {
    Arrays.sort(subjects); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String category : subjects) {
      fq.append("subject:\"").append(category).append("\" AND ");
    }
    return fq.replace(fq.length() - 5, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterLimitForArticleType(String[] articleTypes) {
    Arrays.sort(articleTypes); // Consistent order so that each filter will only be cached once.
    StringBuilder fq = new StringBuilder();
    for (String articleType : articleTypes) {
      fq.append("article_type:\"").append(articleType).append("\" OR ");
    }
    return fq.replace(fq.length() - 4, fq.length(), "").toString(); // Remove last " OR".
  }

  private String createFilterFullDocuments() {
    return "doc_type:full";
  }

  @Required
  public void setConfiguration(Configuration configuration) throws ApplicationException {
    this.configuration = configuration;
    StringBuilder hightlightFieldBuilder = new StringBuilder();
    queryTimeout = configuration.getInt("ambra.services.search.timeout", 60000); // default to 1 min

    if(configuration.containsKey("ambra.services.search.sortOptions.option")) {
      validSorts = new HashMap();
      displaySorts = new ArrayList();

      HierarchicalConfiguration hc = (HierarchicalConfiguration)configuration;
      List<HierarchicalConfiguration> sorts =
          hc.configurationsAt("ambra.services.search.sortOptions.option");

      for (HierarchicalConfiguration s : sorts) {
        String key = s.getString("[@displayName]");
        String value = s.getString("");
        validSorts.put(key, value);
        displaySorts.add(key);
      }

      ((HierarchicalConfiguration) configuration).setExpressionEngine(null);
    } else {
      throw new ApplicationException("ambra.services.search.sortOptions.option not defined " +
          "in configuration.");
    }


    if(configuration.containsKey("ambra.services.search.keywordFields.field")) {
      validKeywords = new HashMap();
      HierarchicalConfiguration hc = (HierarchicalConfiguration)configuration;
      List<HierarchicalConfiguration> sorts =
          hc.configurationsAt("ambra.services.search.keywordFields.field");

      for (HierarchicalConfiguration s : sorts) {
        String key = s.getString("[@displayName]");
        String value = s.getString("");
        validKeywords.put(key, value);

        //These fields can be highlighted too!
        if(hightlightFieldBuilder.length() > 0) {
          hightlightFieldBuilder.append(",");
        }
        hightlightFieldBuilder.append(value);
      }
    } else {
      throw new ApplicationException("ambra.services.search.keywordFields.field not defined " +
          "in configuration.");
    }
  }

  /**
   * @param searchParameters the feedAction data model
   * @param journal          Current journal
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException        ApplicationException
   * @throws java.net.URISyntaxException URISyntaxException
   */
  @Override
  @Transactional(readOnly = true)
  public List<ArticleInfo> getIssueArticles(final FeedSearchParameters searchParameters, String journal, String authId) throws
      URISyntaxException, ApplicationException {
    List<ArticleInfo> articleList = new ArrayList<ArticleInfo>();

    String issurURI = (searchParameters.getIssueURI() != null) ? searchParameters.getIssueURI() : null;

    if (issurURI == null) {
      Journal curJrnl = journalService.getJournal(journal);

      //There is no current issue, return empty result
      if (curJrnl.getCurrentIssue() == null) {
        return articleList;
      }

      issurURI = curJrnl.getCurrentIssue().getIssueUri();
    }

    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issurURI, authId);

    for (TOCArticleGroup ag : articleGroups)
      articleList.addAll(ag.articles);

    return articleList;
  }

  /**
   * Returns a list of annotationViews based on parameters contained in the searchParams. If a start date is not
   * specified then a default date is used but not stored in the searchParams.
   *
   * @param searchParams input parameters.
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException Converts all exceptions to ApplicationException
   */
  @Override
  @Transactional(readOnly = true)
  public List<AnnotationView> getAnnotations(final AnnotationFeedSearchParameters searchParams)
      throws ParseException, URISyntaxException {
    return annotationService.getAnnotations(
        searchParams.getStartDate(), searchParams.getEndDate(), searchParams.getAnnotationTypes(),
        searchParams.getMaxResults(), searchParams.getJournal());
  }

  /**
   * Returns a list of trackbackViews based on the parameters. If a start date is not specified then a default date is
   * used but not stored in the key.
   *
   * @param searchParams input params.
   * @return <code>List&lt;String&gt;</code> a list of annotation Ids
   * @throws ApplicationException Converts all exceptions to ApplicationException
   */
  @Override
  @Transactional(readOnly = true)
  public List<LinkbackView> getTrackbacks(final AnnotationFeedSearchParameters searchParams)
      throws ParseException, URISyntaxException {
    return trackbackService.getTrackbacks(
        searchParams.getStartDate(), searchParams.getEndDate(), searchParams.getMaxResults(), searchParams.getJournal());
  }


  /**
   * @param journalService Journal Service
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param annotationService Annotation Service
   */
  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @param browseService Browse Service
   */
  @Required
  public void setBrowseService(BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * @param trackbackService Trackback Service
   */
  @Required
  public void setTrackBackService(TrackbackService trackbackService) {
    this.trackbackService = trackbackService;
  }

  /**
   * Set solr http service
   *
   * @param solrHttpService solr http service
   */
  @Required
  public void setSolrHttpService(SolrHttpService solrHttpService) {
    this.solrHttpService = solrHttpService;
  }

  @Required
  public void setSolrFieldConverter(SolrFieldConversion solrFieldConverter) {
    this.solrFieldConverter = solrFieldConverter;
  }
}
