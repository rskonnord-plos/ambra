/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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
import org.ambraproject.service.journal.JournalService;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.search.SolrException;
import org.ambraproject.service.search.SolrFieldConversion;
import org.ambraproject.service.search.SolrHttpService;
import org.ambraproject.service.trackback.TrackbackService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.LinkbackView;
import org.ambraproject.views.TOCArticleGroup;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


  private static final String MAX_FACET_SIZE         = "100";
  private static final String MIN_FACET_COUNT        = "1";
  private static final String MAX_HIGHLIGHT_SNIPPETS = "3";

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
  public Document getSearchArticles(final FeedSearchParameters searchParameters) {
    Map<String, String> params = new HashMap<String, String>();
    // result format
    params.put("wt", "xml");
    // what I want returned, the fields needed for rss feed
    params.put("fl", "id,title_display,publication_date,author_without_collab_display,author_collab_only_display," +
        "author_display,volume,issue,article_type,subject_hierarchy,abstract_primary_display,copyright");

    //set query string
    params.put("q", searchParameters.getQuery());

    //set query time out
    //params.put( "timeAllowed", queryTimeout);

    // The relevance (of each results element) to the search terms.
    params.put("fl", "score");

    //setting highlighting to true
    params.put("hl", "true");

   // params.put("hl.fl", this.highlightFields);
    params.put("hl.requireFieldMatch", "true");

    //adding the filters
    StringBuilder filterQuery = new StringBuilder();
    filterQuery.append("doc_type:full " +
        "AND !article_type_facet:\"Issue Image\" ");


    if (searchParameters.getFilterJournals() != null && searchParameters.getFilterJournals().length > 0) {
      filterQuery.append(" AND ");
      filterQuery.append("cross_published_journal_key:") ;
      for (int i=0;i<searchParameters.getFilterJournals().length;i++) {
        filterQuery.append(searchParameters.getFilterJournals()[i]).append(" OR ");
      }
       filterQuery.replace(filterQuery.length() - 4, filterQuery.length(), ""); // Remove last " OR"

      }

 //   filterQuery.append("cross_published_journal_key:").append("PLoSBiology OR PLoSMedicine OR PLoSONE");

    params.put("fq", filterQuery.toString());

    //adding facets
    params.put("facet","true");
    params.put("facet.field","subject_facet");
    params.put("facet.field","author_facet");
    params.put("facet.field","editor_facet");
    params.put("facet.field","article_type_facet");
    params.put("facet.field","affiliate_facet");
    params.put("facet.field","cross_published_journal_key");

    params.put("facet.limit", MAX_FACET_SIZE);
    params.put("facet.mincount", MIN_FACET_COUNT);
    params.put("facet.method", "fc");

    params.put("defType", "dismax");





    Document result = null;
    try {
      result = solrHttpService.makeSolrRequest(params);
    } catch (SolrException e) {
      e.printStackTrace();
    }

    return result;
  }


  /**
   * @param searchParameters the feedAction data model
   * @param journal          Current journal
   * @return List&lt;String&gt; if article Ids.
   * @throws ApplicationException        ApplicationException
   * @throws java.net.URISyntaxException URISyntaxException
   */
  @Override
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
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Required
  public void setSolrFieldConverter(SolrFieldConversion solrFieldConverter) {
    this.solrFieldConverter = solrFieldConverter;
  }
}
