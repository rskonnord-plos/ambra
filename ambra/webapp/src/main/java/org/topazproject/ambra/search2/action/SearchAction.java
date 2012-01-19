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
package org.topazproject.ambra.search2.action;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.action.BaseSessionAwareActionSupport;
import org.topazproject.ambra.article.service.BrowseService;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.search2.SearchParameters;
import org.topazproject.ambra.search2.SearchResultSinglePage;
import org.topazproject.ambra.search2.SearchHit;
import org.topazproject.ambra.search2.service.SearchService;

import java.net.URI;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Search Action class to search for simple or advanced search.
 */
@SuppressWarnings("serial")
public class SearchAction extends BaseSessionAwareActionSupport {

  private static final Logger log = LoggerFactory.getLogger(SearchAction.class);
  private static final String SEARCH_PAGE_SIZE = "ambra.services.search.pageSize";

  // Flag telling this action whether or not the search should be executed.
  private String noSearchFlag;
  private SearchParameters searchParameters;

  private SearchService searchService;
  private BrowseService browseService;
  private JournalService journalService;

  private Collection<SearchHit> searchResults;
  private int totalNoOfResults;
  private boolean isSimpleSearch = false;
  // Creates list of all searchable Journals
  private Set<Journal> journals;
  // Creates list of all searchable Categories.  Empty map for non-null safety
  private Map<String, List<URI>> categoryInfos = new HashMap<String, List<URI>>();


  /**
   * @return return simple search result
   */
  @Transactional(readOnly = true)
  public String executeSimpleSearch() {
    setDefaultsCommon(); // journalOpt and limitToJournal are NOT set in form in global_header.ftl
    isSimpleSearch = true;

    final String queryString = getSearchParameters().getQuery();

    if (StringUtils.isBlank(queryString) || queryString.equals("Search articles...")) {
      addFieldError("query", "Please enter a search query.");
      setQuery("");
      setTextSearchAll("");

      return INPUT;
    } else {
      try {
        SearchResultSinglePage results = searchService.simpleSearch(queryString, getCurrentJournalEissn(),
            getSearchParameters().getStartPage(), getSearchParameters().getPageSize());

        //  TODO: take out these intermediary objects and pass "SearchResultSinglePage" to the FTL
        totalNoOfResults = results.getTotalNoOfResults();
        searchResults = results.getHits();

        int totPages = (totalNoOfResults + getPageSize() - 1) / getPageSize();
        setStartPage(Math.max(0, Math.min(getStartPage(), totPages - 1)));
      } catch (ApplicationException e) {
        addActionError("Search failed");
        log.error("Search failed for the query string: " + queryString, e);
        return ERROR;
      }

      return SUCCESS;
    }
  }

  /**
   * @return return advanced search result
   */
  @Transactional(readOnly = true)
  public String executeAdvancedSearch() {
    setDefaultsCommon();

    // For transitioning from advanced to simple searches. These searches are almost equivalent.
    if (StringUtils.isBlank(getTextSearchAll()) && ! StringUtils.isBlank(getQuery())) {
      setTextSearchAll(getQuery());
    }
    
    if ("range".equals(getDateTypeSelect())) {
      if ( ! getSearchParameters().isDateRangeValid()) {
        if (getSearchParameters().getStartDate() == null) {
          log.warn("The search start date is invalid");
          addFieldError("startDateAsString", "The search start date is invalid.");
        }
        if (getSearchParameters().getEndDate() == null) {
          log.warn("The search end date is invalid");
          addFieldError("endDateAsString", "The search end date is invalid.");
        }
        if (getSearchParameters().getStartDate() != null && getSearchParameters().getEndDate() != null
            && getSearchParameters().getEndDate().before(getSearchParameters().getStartDate())) {
          log.warn("The search end date must be on or after the search start date");
          addFieldError("startDateAsString", "The search end date must be on or after the search start date.");
        }
        return ERROR;
      }
    } else {
      Calendar cal = new GregorianCalendar();
      if ("week".equals(getDateTypeSelect())) {
        cal.add(Calendar.DATE, -7);
      } else if ("month".equals(getDateTypeSelect())) {
        cal.add(Calendar.MONTH, -1);
      } else if ("3months".equals(getDateTypeSelect())) {
        cal.add(Calendar.MONTH, -3);
      } else if ("6months".equals(getDateTypeSelect())) {
        cal.add(Calendar.MONTH, -6);
      }
      getSearchParameters().setStartDate(cal.getTime());
      getSearchParameters().setEndDate(new Date());
    }

    if (doSearch()) {
      if ( ! getSearchParameters().isContainsSearchTerms()) {
        addFieldError("creator", "Please enter a search query.");
        return INPUT;
      }
      return executeAdvancedSearch(getSearchParameters());
    }

    return INPUT;
  }

  /**
   * Set values used for processing and/or display by both the Simple and Advanced searches.
   */
  private void setDefaultsCommon() {

    log.debug("  ***  This SearchAction class is in the search2 directory.");

    categoryInfos = browseService.getArticlesByCategory(); // Categories for ALL articles in this journal.
    journals = getJournalsForCrossJournalSearch(); // All journals that can be searched.

    // For transitioning from simple to/from advanced. These searches are almost equivalent.
    if (StringUtils.isBlank(getQuery()) && ! StringUtils.isBlank(getTextSearchAll())) {
      setQuery(getTextSearchAll());
    } else if (StringUtils.isBlank(getTextSearchAll()) && ! StringUtils.isBlank(getQuery())) {
      setTextSearchAll(getQuery());
    }

    // Set defaults for "limitToJournal" and "journalOpt".
    if (getLimitToJournal().length < 1) {
      setLimitToJournal(new String[]{getCurrentJournalEissn()});

      if ( ! "all".equals(getJournalOpt()) && ! "some".equals(getJournalOpt())) {
        if (getLimitToJournal().length < 1) {
          setJournalOpt("all"); // If no default journal could be set, then try to search all journals
        } else {
          setJournalOpt("some"); // Since one default journal was set, search only that one journal
        }
      }
    }

    // Set default for "pageSize".
    if (getPageSize() == 0) {
      setPageSize(configuration.getInt(SEARCH_PAGE_SIZE, 10));
    }
  }

  private String executeAdvancedSearch(final SearchParameters searchParameters) {
    try {
      SearchResultSinglePage results = searchService.advancedSearch(searchParameters);

      //  TODO: take out these intermediary objects and pass "SearchResultSinglePage" to the FTL
      totalNoOfResults = results.getTotalNoOfResults();
      searchResults = results.getHits();

      int totPages = (totalNoOfResults + getPageSize() - 1) / getPageSize();
      setStartPage(Math.max(0, Math.min(getStartPage(), totPages - 1)));
    } catch (ApplicationException e) {
      addActionError("Search failed");
      log.error("Search failed for the query string: " + searchParameters.getQuery(), e);
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Get all journals that are to be included in cross-journal searches, ordered by journal title.
   * By default, each journal is included, unless the property
   * <code>ambra.virtualJournals.JOURNAL_KEY.isIncludeInCrossJournalSearch</code> is set to
   * <code>false</code> in the configuration for a given journal.
   *
   * @return journals that are to be included in cross-journal searches
   */
  private Set<Journal> getJournalsForCrossJournalSearch() {
    TreeSet<Journal> orderedByName = new TreeSet<Journal>(
        new Comparator<Journal>() {
          public int compare(Journal journal1, Journal journal2) {
            return journal1.getDublinCore().getTitle().compareTo(
                journal2.getDublinCore().getTitle());
          }
        }
    );

    orderedByName.addAll(journalService.getAllJournals());
    Iterator journalIterator = orderedByName.iterator();
    while (journalIterator.hasNext()) {
      Journal journal = (Journal) journalIterator.next();
      if (!configuration.getBoolean("ambra.virtualJournals." + journal.getKey()
          + ".isIncludeInCrossJournalSearch", true)) {
        journalIterator.remove();
      }
    }
    return orderedByName;
  }

  /**
   *  TODO: There has GOT to be a better way of getting the ISSN for the current journal!
   * @return The Electronic ISSN for the journal that the user is currently viewing
   */
  private String getCurrentJournalEissn() {
    for (Journal journal : journalService.getAllJournals()) {
      if (getCurrentJournal() != null && getCurrentJournal().equals(journal.getKey()) && journal.geteIssn() != null) {
        return journal.geteIssn();
      }
    }
    return "";
  }

  //  Getters and Setters that belong to this Action

  /**
   * @return the noSearchFlag
   */
  public String getNoSearchFlag() {
    return noSearchFlag;
  }

  public boolean getIsSimpleSearch() {
    return isSimpleSearch;
  }

  /**
   * @param noSearchFlag the noSearchFlag to set
   */
  public void setNoSearchFlag(String noSearchFlag) {
    this.noSearchFlag = noSearchFlag;
  }

  private boolean doSearch() {
    return noSearchFlag == null;
  }

  /**
   * @return the search results.
   */
  public Collection<SearchHit> getSearchResults() {
    return searchResults;
  }

  /**
   * Getter for property 'totalNoOfResults'.
   *
   * @return Value for property 'totalNoOfResults'.
   */
  public int getTotalNoOfResults() {
    return totalNoOfResults;
  }

  public Set<Journal> getJournals() {
    return journals;
  }

  public Map<String, List<URI>> getCategoryInfos() {
    return categoryInfos;
  }

  /**
   * Set the searchService
   *
   * @param searchService searchService
   */
  @Required
  public void setSearchService2(final SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Spring injected BrowseService
   *
   * @param browseService browseService
   */
  @Required
  public void setBrowseService(final BrowseService browseService) {
    this.browseService = browseService;
  }

  /**
   * Spring injected JournalService
   *
   * @param journalService journalService
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Return the object used to store all of the parameters used to create a query.
   * If no searchParameter exists, then a new one is created.
   *
   * @return The object used to store all of the parameters used to create a query
   */
  public SearchParameters getSearchParameters() {
    if (searchParameters == null) {
      searchParameters = new SearchParameters();
    }
    return searchParameters;
  }

  //  Getters and Setters that belong to SearchParameters class

  /**
   * Set the simple query
   *
   * @param query query
   */
  public void setQuery(final String query) {
    getSearchParameters().setQuery(query);
  }

  /**
   * Getter for property 'query'.
   *
   * @return Value for property 'query'.
   */
  public String getQuery() {
    return getSearchParameters().getQuery();
  }

  /**
   * Set the startPage
   *
   * @param startPage startPage
   */
  public void setStartPage(final int startPage) {
    getSearchParameters().setStartPage(startPage);
  }

  /**
   * Set the pageSize
   *
   * @param pageSize pageSize
   */
  public void setPageSize(final int pageSize) {
    getSearchParameters().setPageSize(pageSize);
  }

  /**
   * Getter for property 'pageSize'.
   *
   * @return Value for property 'pageSize'.
   */
  public int getPageSize() {
    return getSearchParameters().getPageSize();
  }

  /**
   * Getter for property 'startPage'.
   *
   * @return Value for property 'startPage'.
   */
  public int getStartPage() {
    return getSearchParameters().getStartPage();
  }

  public String getTextSearchAll() {
    return getSearchParameters().getTextSearchAll();
  }

  public void setTextSearchAll(String textSearchAll) {
    getSearchParameters().setTextSearchAll(textSearchAll);
  }

  public String getTextSearchExactPhrase() {
    return getSearchParameters().getTextSearchExactPhrase();
  }

  public void setTextSearchExactPhrase(String textSearchExactPhrase) {
    getSearchParameters().setTextSearchExactPhrase(textSearchExactPhrase);
  }

  public String getTextSearchAtLeastOne() {
    return getSearchParameters().getTextSearchAtLeastOne();
  }

  public void setTextSearchAtLeastOne(String textSearchAtLeastOne) {
    getSearchParameters().setTextSearchAtLeastOne(textSearchAtLeastOne);
  }

  public String getTextSearchWithout() {
    return getSearchParameters().getTextSearchWithout();
  }

  public void setTextSearchWithout(String textSearchWithout) {
    getSearchParameters().setTextSearchWithout(textSearchWithout);
  }

  public String getTextSearchOption() {
    return getSearchParameters().getTextSearchOption();
  }

  public void setTextSearchOption(String textSearchOption) {
    getSearchParameters().setTextSearchOption(textSearchOption);
  }

  public void setDateTypeSelect(String dateTypeSelect) {
    getSearchParameters().setDateTypeSelect(dateTypeSelect);
  }

  public String getDateTypeSelect() {
    return getSearchParameters().getDateTypeSelect();
  }

  public void setStartDateAsString(String startDateAsString) {
    try {
      getSearchParameters().setStartDateAsString(startDateAsString);
    } catch (ParseException e) {
      try {
        getSearchParameters().setStartDateAsString(null);
      } catch (ParseException e1) {
        log.warn("Could not set SearchParameters.startDate to NULL.", e1);
      }
    }
  }
  public String getStartDateAsString() {
    return getSearchParameters().getStartDateAsString();
  }

  public void setEndDateAsString(String endDateAsString) {
    try {
      getSearchParameters().setEndDateAsString(endDateAsString);
    } catch (ParseException e) {
      try {
        getSearchParameters().setEndDateAsString(null);
      } catch (ParseException e1) {
        log.warn("Could not set SearchParameters.endDate to NULL.", e1);
      }
    }
  }
  public String getEndDateAsString() {
    return getSearchParameters().getEndDateAsString();
  }

  public void setCreator(String[] creator) {
    getSearchParameters().setCreator(rectify(creator));
  }

  public String[] getCreator() {
    return getSearchParameters().getCreator();
  }

  /**
   * TODO: This method is superfluous.  "creatorStr" should be replaced with simple references to "creator".
   *
   * @return The {@link #getCreator()} array as a single comma delimited String.
   */
  public String getCreatorStr() {
    if (getCreator().length < 1) return "";
    StringBuilder sb = new StringBuilder();
    for (String author : getCreator()) {
      sb.append(author.trim()).append(',');
    }
    return sb.replace(sb.length() - 1, sb.length(), "").toString();
  }

  /**
   * TODO: This method is superfluous.  "creatorStr" should be replaced with simple references to "creator".
   *
   * Converts a String array whose first element may be a comma delimited String
   * into a new String array whose elements are the split comma delimited elements.
   *
   * @param arr String array that may contain one or more elements having a comma delimited String.
   * @return Rectified String[] array or
   *         <code>null</code> when the given String array is <code>null</code>.
   */
  private String[] rectify(String[] arr) {
    if (arr != null && arr.length == 1 && arr[0].length() > 0) {
      arr = arr[0].split(",");
      for (int i = 0; i < arr.length; i++) {
        arr[i] = arr[i] == null ? null : arr[i].trim();
      }
    }
    return arr;
  }

  public void setJournalOpt(String journalOpt) {
    getSearchParameters().setJournalOpt(journalOpt);
  }

  public String getJournalOpt() {
    return getSearchParameters().getJournalOpt();
  }

  public void setLimitToJournal(String[] limitToJournal) {
    getSearchParameters().setLimitToJournal(rectify(limitToJournal));
  }

  public String[] getLimitToJournal() {
    return getSearchParameters().getLimitToJournal();
  }

  public void setSubjectCatOpt(String subjectCatOpt) {
    getSearchParameters().setSubjectCatOpt(subjectCatOpt);
  }

  public String getSubjectCatOpt() {
    return getSearchParameters().getSubjectCatOpt();
  }

  public void setLimitToCategory(String[] limitToCategory) {
    getSearchParameters().setLimitToCategory(rectify(limitToCategory));
  }

  public String[] getLimitToCategory() {
    return getSearchParameters().getLimitToCategory();
  }

  public String getAuthorNameOp() {
    return getSearchParameters().getAuthorNameOp();
  }

  public void setAuthorNameOp(String authorNameOp) {
    getSearchParameters().setAuthorNameOp(authorNameOp);
  }
}