/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action;

import org.ambraproject.service.search.SearchService;
import org.ambraproject.service.taxonomy.TaxonomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.SortedMap;

/**
 * Action class for displaying a list of all top-level and second-level categories
 * in the taxonomy associated with any PLOS ONE article.
 */
public class TaxonomyAction extends BaseActionSupport {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyAction.class);

  private SortedMap<String, List<String>> categories;

  private SearchService searchService;

  private TaxonomyService taxonomyService;

  @Override
  public String execute() throws Exception {

    // Currently we only allow this from PLOS ONE.
    if (!"PLoSONE".equals(getCurrentJournal())) {
      return ERROR;
    }
    List<String> solrResults = searchService.getAllSubjects(getCurrentJournal());
    categories = taxonomyService.parseTopAndSecondLevelCategories(solrResults);
    return SUCCESS;
  }

  /**
   * @return a map from top-level category to list of second-level subcategories
   *     associated with that top-level category
   */
  public SortedMap<String, List<String>> getCategories() {
    return categories;
  }

  @Required
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }

  @Required
  public void setTaxonomyService(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }
}
