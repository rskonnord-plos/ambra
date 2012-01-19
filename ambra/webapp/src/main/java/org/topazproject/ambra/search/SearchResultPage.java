/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.ambra.search;

import org.topazproject.ambra.search.service.SearchHit;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value object that denotes a single page of search result.<p>
 *
 * Presumably this is part of the data-model passed to freemarker.
 *
 * @author Viru
 * @author Eric Brown
 */
public class SearchResultPage {
  private static final Logger log = LoggerFactory.getLogger(SearchResultPage.class);

  private final int totalNoOfResults;
  private final int pageSize;
  private final Collection<SearchHit> hits;

  public SearchResultPage(final int totalResults, final int pageSize,
                          final Collection<SearchHit> hits) {
    this.totalNoOfResults = totalResults;
    this.pageSize = pageSize;
    this.hits = hits;

    if (log.isDebugEnabled())
      log.debug("Got " + hits.size() + " on page of " + pageSize + " of total " + totalResults);
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
