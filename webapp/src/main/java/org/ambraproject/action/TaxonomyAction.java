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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Action class for displaying a list of all top-level and second-level categories
 * in the taxonomy associated with any PLOS ONE article.
 */
public class TaxonomyAction extends BaseActionSupport {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyAction.class);

  private SortedMap<String, List<String>> categories;

  private SearchService searchService;

  @Override
  public String execute() throws Exception {
    if (!"PLoSONE".equals(getCurrentJournal())) {
      return ERROR;
    }
    List<String> solrResults = searchService.getAllSubjects(getCurrentJournal());
    setCategoriesFromSolrResults(solrResults);
    return SUCCESS;
  }

  private void setCategoriesFromSolrResults(List<String> results) {

    // Since there are lots of duplicates, we start by adding the second-level
    // categories to a Set instead of a List.
    Map<String, Set<String >> map = new HashMap<String, Set<String>>();
    for (String category : results) {

      // If the category doesn't start with a slash, it's one of the old-style
      // categories where we didn't store the full path.  Ignore these.
      if (category.charAt(0) == '/') {
        String[] fields = category.split("\\/");
        if (fields.length >= 3) {
          Set<String> subCats = map.get(fields[1]);
          if (subCats == null) {
            subCats = new HashSet<String>();
          }
          subCats.add(fields[2]);
          map.put(fields[1], subCats);
        }
      }
    }

    // Now sort all the subcategory lists, and add them to the result.
    categories = new TreeMap<String, List<String>>();
    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
      List<String> subCatList = new ArrayList<String>(entry.getValue());
      Collections.sort(subCatList);
      categories.put(entry.getKey(), subCatList);
    }
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
}
