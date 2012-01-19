/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.search.SearchResultPage;
import org.plos.search.SearchUtil;

/**
 * Service to provide search capabilities for the application
 */
public class SearchService {
  private int snippetsMax;
  private int fieldMaxLength;
  private String indexName;
  private String resultPageXslt;
  private SearchWebService searchWebService;

  private static final Log log = LogFactory.getLog(SearchService.class);

  /**
   * Find the results for a given query.
   * @param query query
   * @param startPage startPage
   * @param pageSize pageSize
   * @return a Collection<SearchResult>
   * @throws ApplicationException ApplicationException
   */
  public SearchResultPage find(final String query, final int startPage, final int pageSize) throws ApplicationException {
    try {
      final int hitStartPage = startPage * pageSize;
      final String findResult = searchWebService.find(query, hitStartPage, pageSize, snippetsMax, fieldMaxLength, indexName, resultPageXslt);
      log.debug("findResult = " + findResult);

      return SearchUtil.convertSearchResultXml(findResult);
    } catch (Exception e) {
      throw new ApplicationException("Search failed with exception:", e);
    }
  }

  /**
   * Setter for property 'fieldMaxLength'.
   * @param fieldMaxLength Value to set for property 'fieldMaxLength'.
   */
  public void setFieldMaxLength(final int fieldMaxLength) {
    this.fieldMaxLength = fieldMaxLength;
  }

  /**
   * Setter for property 'indexName'.
   * @param indexName Value to set for property 'indexName'.
   */
  public void setIndexName(final String indexName) {
    this.indexName = indexName;
  }

  /**
   * Setter for property 'resultPageXslt'.
   * @param resultPageXslt Value to set for property 'resultPageXslt'.
   */
  public void setResultPageXslt(final String resultPageXslt) {
    this.resultPageXslt = resultPageXslt;
  }

  /**
   * Setter for property 'snippetsMax'.
   * @param snippetsMax Value to set for property 'snippetsMax'.
   */
  public void setSnippetsMax(final int snippetsMax) {
    this.snippetsMax = snippetsMax;
  }

  /**
   * Setter for property 'searchWebService'.
   * @param searchWebService Value to set for property 'searchWebService'.
   */
  public void setSearchWebService(final SearchWebService searchWebService) {
    this.searchWebService = searchWebService;
  }
}
