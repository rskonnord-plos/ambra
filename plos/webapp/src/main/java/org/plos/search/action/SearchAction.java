/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.BrowseService;
import org.plos.search.SearchResultPage;
import org.plos.search.SearchUtil;
import org.plos.search.service.SearchHit;
import org.plos.search.service.SearchService;
import org.plos.util.FileUtils;
import org.topazproject.otm.query.QueryParser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search Action class to search for simple or advanced search.
 *
 * @author Viru
 */
public class SearchAction extends BaseActionSupport {
  private String query;
  private int startPage = 0;
  private int pageSize = 10;
  private SearchService searchService;
  private BrowseService browseService;

  private static final Log log = LogFactory.getLog(SearchAction.class);
  private Collection<SearchHit> searchResults;
  Map<String, Integer> categoryInfos = new HashMap<String, Integer>(); // empty map for non-null safety
  private String title;
  private String text;
  private int totalNoOfResults;
  private String description;
  private String[] creator = null;
  private String authorNameOp;
  private String textSearchAll;
  private String textSearchExactPhrase;
  private String textSearchAtLeastOne;
  private String textSearchWithout;
  private String textSearchOption;
  private String dateTypeSelect;
  private String startYear;
  private String startMonth;
  private String startDay;
  private String endYear;
  private String endMonth;
  private String endDay;
  private String subjectCatOpt;
  private String[] limitToCategory = null;

  /**
   * @return return simple search result
   */
  public String executeSimpleSearch() {
    return executeSearch(query);
  }

  /**
   * @return return simple search result
   */
  public String executeAdvancedSearch() {
    query = buildAdvancedQuery();
    return executeSearch(query);
  }

  private String executeSearch(final String queryString) {
    try {
      if (StringUtils.isBlank(queryString)) {
        addActionError("Please enter a query string.");
        categoryInfos = browseService.getCategoryInfos();
        return INPUT;
      }
      final SearchResultPage searchResultPage = searchService.find(queryString, startPage, pageSize);
//      final SearchResultPage searchResultPage = getMockSearchResults(startPage, pageSize);
      totalNoOfResults = searchResultPage.getTotalNoOfResults();
      searchResults = searchResultPage.getHits();
    } catch (ApplicationException e) {
      addActionError("Search failed ");
      log.error("Search failed with error with query string: " + queryString, e);
      return ERROR;
    }
    return SUCCESS;
  }

  private SearchResultPage getMockSearchResults(final int startPage, final int pageSize) throws ApplicationException {
    final Collection<SearchHit> hits = new ArrayList<SearchHit>();
    final String searchResultXml = "searchResult.xml";

    final String text;
    try {
      text = FileUtils.getTextFromUrl(new File(searchResultXml).toURL().toString());
      return SearchUtil.convertSearchResultXml(text);
    } catch (Exception e) {
      throw new ApplicationException("error");
    }
  }

  private String buildAdvancedQuery() {
    final Collection<String> fields = new ArrayList<String>();

    // Build Search terms for Authors if specified
    if ((creator != null) && (creator.length > 0) && (StringUtils.isNotBlank(creator[0]))) {
      StringBuffer buf = new StringBuffer("(creator:(");
      boolean allAuthors = false;
      if ("all".equals(authorNameOp)) {
        allAuthors = true;
      }
      for (int i=0; i<creator.length; i++) {
        String creatorName = creator[i];
        if (StringUtils.isNotBlank(creatorName)) {
          buf.append("\"");
          buf.append(escape(creatorName));
          buf.append("\"");
        }
        if ((i < creator.length-1) && (StringUtils.isNotBlank(creator[i+1]))) {
          if (allAuthors) {
            buf.append(" AND ");
          } else {
            buf.append(" OR ");
          }
        }
      }
      buf.append(" ))");
      fields.add(buf.toString());
    }
    
    // Build Search Article Text section of advanced search. 
    
    String textSearchField = null;
    if (StringUtils.isNotBlank(textSearchOption)) {
      if ("abstract".equals(textSearchOption)) {
        textSearchField = "description:";
      }
      if ("refs".equals(textSearchField)) {
        textSearchField = "citation:";
      }
      if ("title".equals(textSearchField)) {
        textSearchField = "title:";
      }
    }
    // All words field should be in parenthesis with AND between each word. 
    if (StringUtils.isNotBlank(textSearchAll)) {
      String[] words = StringUtils.split(textSearchAll);
      StringBuffer buf = new StringBuffer();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      for (int i=0; i<words.length; i++) {
        buf.append(escape(words[i]));
        if (i < words.length-1) {
          buf.append(" AND ");
        }
      }
      buf.append(" )");
      fields.add(buf.toString());
    }
    
    // Exact phrase should be placed in quotes
    if (StringUtils.isNotBlank(textSearchExactPhrase)) {
      StringBuffer buf = new StringBuffer();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("(\"");
      buf.append(escape(textSearchExactPhrase));
      buf.append("\")");
      fields.add(buf.toString());
    }
    
    // At least one of should be placed in parenthesis separated by spaced (OR'ed)
    if (StringUtils.isNotBlank(textSearchAtLeastOne)) {
      StringBuffer buf = new StringBuffer();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      buf.append(escape(textSearchAtLeastOne));
      buf.append(" )");
      fields.add(buf.toString());
    }
    
    // Without the words - in parenthesis with NOT operator and AND'ed together
    // E.g. (-"the" AND -"fat" AND -"cat")
    if (StringUtils.isNotBlank(textSearchWithout)) {
      // TODO - we might want to allow the entry of phrases to omit (entered using quotes?) 
      // - in which case we have to be smarter about how we split...
      String[] words = StringUtils.split(textSearchWithout);
      StringBuffer buf = new StringBuffer();
      if (textSearchField != null) {
        buf.append(textSearchField);
      }
      buf.append("( ");
      for (int i=0; i<words.length; i++) {
        buf.append("-\"");
        buf.append(escape(words[i]));
        buf.append("\"");
        if (i < words.length-1) {
          buf.append(" AND ");
        }
      }
      buf.append(" )");
      fields.add(buf.toString());
    }
    
    if (StringUtils.isNotBlank(dateTypeSelect)) {
      if ("range".equals(dateTypeSelect)) {
        if (isDigit(startYear)&& isDigit(startMonth) && isDigit(startDay) && 
            isDigit(endYear) && isDigit(endMonth) && isDigit(endDay)) {
          StringBuffer buf = new StringBuffer("date:[");
          buf.append(startYear).append(startMonth).append(startDay);
          buf.append(" TO ");
          buf.append(endYear).append(endMonth).append(endDay);
          buf.append("]");
          fields.add(buf.toString());
        }
      } else {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String endDateStr = sdf.format(new Date());
        Calendar cal = new GregorianCalendar();
        if ("week".equals(dateTypeSelect)) {
          cal.add(Calendar.DATE, -7);
        }
        if ("month".equals(dateTypeSelect)) {
          cal.add(Calendar.MONTH, -1);
        }
        if ("month".equals(dateTypeSelect)) {
          cal.add(Calendar.MONTH, -3);
        }
        String startDateStr = sdf.format(cal.getTime());
        
        StringBuffer buf = new StringBuffer("date:[");
        buf.append(startDateStr).append(" TO ").append(endDateStr).append("]");
        fields.add(buf.toString());
      }
    }
    
    if ("some".equals(subjectCatOpt)) {
      if ((limitToCategory != null) && (limitToCategory.length > 0)) {
        StringBuffer buf = new StringBuffer("subject:( " );
        for (int i=0; i < limitToCategory.length; i++) {
          buf.append("\"").append(escape(limitToCategory[i])).append("\" ");
        }
        buf.append(")");
      }
    }
    
    String advSearchQueryStr = StringUtils.join(fields.iterator(), " AND ");
    if (log.isDebugEnabled()) {
      log.debug("Generated advanced search query: "+advSearchQueryStr);
    }
    return StringUtils.join(fields.iterator(), " AND ");
  }
  
  /**
   * Static helper method to escape special characters in a Lucene search string with a \
   */
  protected static final String escapeChars = "+-&|!(){}[]^\"~*?:\\";
  protected String escape(String in) {
    StringBuffer buf = new StringBuffer();
    for (int i=0; i < in.length(); i++) {
      if (escapeChars.indexOf(in.charAt(i)) != -1) {
        buf.append("\\");
      }
      buf.append(in.charAt(i));
    }
    return buf.toString();
  }
  
  protected boolean isDigit(String str) {
    if (str == null) return false;
    for (int i=0; i < str.length(); i++) {
      if (!(Character.isDigit(str.charAt(i)))) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Set the simple query
   * @param query query
   */
  public void setQuery(final String query) {
    this.query = query;
  }

  /**
   * Set the startPage
   * @param startPage startPage
   */
  public void setStartPage(final int startPage) {
    this.startPage = startPage;
  }

  /**
   * Set the pageSize
   * @param pageSize pageSize
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Set the searchService
   * @param searchService searchService
   */
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Spring injected BrowseService
   * @param browseService searchService
   */
  public void setBrowseService(final BrowseService browseService) {
    this.browseService = browseService;
  }
  
  /**
   * @return the search results.
   */
  public Collection<SearchHit> getSearchResults() {
    return searchResults;
  }


  /**
   * Getter for property 'pageSize'.
   * @return Value for property 'pageSize'.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Getter for property 'query'.
   * @return Value for property 'query'.
   */
  public String getQuery() {
    return query;
  }

  /**
   * Getter for property 'startPage'.
   * @return Value for property 'startPage'.
   */
  public int getStartPage() {
    return startPage;
  }

  /**
   * Getter for property 'text'.
   * @return Value for property 'text'.
   */
  public String getText() {
    return text;
  }

  /**
   * Setter for property 'text'.
   * @param text Value to set for property 'text'.
   */
  public void setText(final String text) {
    this.text = text;
  }

  /**
   * Getter for property 'title'.
   * @return Value for property 'title'.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Setter for property 'title'.
   * @param title Value to set for property 'title'.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Getter for property 'totalNoOfResults'.
   * @return Value for property 'totalNoOfResults'.
   */
  public int getTotalNoOfResults() {
    return totalNoOfResults;
  }

  /**
   * Getter for property 'description'.
   * @return Value for property 'description'.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Setter for property 'description'.
   * @param description Value to set for property 'description'.
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  public String getTextSearchAll() {
    return textSearchAll;
  }

  public void setTextSearchAll(String textSearchAll) {
    this.textSearchAll = textSearchAll;
  }

  public String getTextSearchExactPhrase() {
    return textSearchExactPhrase;
  }

  public void setTextSearchExactPhrase(String textSearchExactPhrase) {
    this.textSearchExactPhrase = textSearchExactPhrase;
  }

  public String getTextSearchAtLeastOne() {
    return textSearchAtLeastOne;
  }

  public void setTextSearchAtLeastOne(String textSearchAtLeastOne) {
    this.textSearchAtLeastOne = textSearchAtLeastOne;
  }

  public String getTextSearchWithout() {
    return textSearchWithout;
  }

  public void setTextSearchWithout(String textSearchWithout) {
    this.textSearchWithout = textSearchWithout;
  }

  public String getTextSearchOption() {
    return textSearchOption;
  }

  public void setTextSearchOption(String textSearchOption) {
    this.textSearchOption = textSearchOption;
  }

  public String getDateTypeSelect() {
    return dateTypeSelect;
  }

  public void setDateTypeSelect(String dateTypeSelect) {
    this.dateTypeSelect = dateTypeSelect;
  }

  public String getStartYear() {
    return startYear;
  }

  public void setStartYear(String startYear) {
    this.startYear = startYear;
  }

  public String getStartMonth() {
    return startMonth;
  }

  public void setStartMonth(String startMonth) {
    this.startMonth = startMonth;
  }

  public String getStartDay() {
    return startDay;
  }

  public void setStartDay(String startDay) {
    this.startDay = startDay;
  }

  public String getEndYear() {
    return endYear;
  }

  public void setEndYear(String endYear) {
    this.endYear = endYear;
  }

  public String getEndMonth() {
    return endMonth;
  }

  public void setEndMonth(String endMonth) {
    this.endMonth = endMonth;
  }

  public String getEndDay() {
    return endDay;
  }

  public void setEndDay(String endDay) {
    this.endDay = endDay;
  }

  public String getSubjectCatOpt() {
    return subjectCatOpt;
  }

  public void setSubjectCatOpt(String subjectCatOpt) {
    this.subjectCatOpt = subjectCatOpt;
  }

  public Map<String, Integer> getCategoryInfos() {
    return categoryInfos;
  }

  public void setCategoryInfos(Map<String, Integer> categoryInfos) {
    this.categoryInfos = categoryInfos;
  }

  public String[] getCreator() {
    return creator;
  }

  public void setCreator(String[] creator) {
    this.creator = creator;
  }

  public String[] getLimitToCategory() {
    return limitToCategory;
  }

  public void setLimitToCategory(String[] limitToCategory) {
    this.limitToCategory = limitToCategory;
  }
}
