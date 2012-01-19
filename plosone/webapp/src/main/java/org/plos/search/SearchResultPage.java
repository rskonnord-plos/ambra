/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search;

import org.plos.search.service.SearchHit;

import java.util.Collection;

/**
 * Value object that denotes a single page of search result
 */
public class SearchResultPage {
  private final int totalNoOfResults;
  private final int pageSize;
  private final Collection<SearchHit> hits;

  public SearchResultPage(final int totalResults, final int pageSize, final Collection<SearchHit> hits) {
    this.totalNoOfResults = totalResults;
    this.pageSize = pageSize;
    this.hits = hits;
  }

  /**
   * Getter for property 'hits'.
   * @return Value for property 'hits'.
   */
  public Collection<SearchHit> getHits() {
    return hits;
  }

  /**
   * Getter for property 'pageSize'.
   * @return Value for property 'pageSize'.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Getter for property 'totalNoOfResults'.
   * @return Value for property 'totalNoOfResults'.
   */
  public int getTotalNoOfResults() {
    return totalNoOfResults;
  }
}
