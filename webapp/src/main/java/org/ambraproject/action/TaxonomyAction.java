/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action;

import org.ambraproject.service.taxonomy.TaxonomyService;
import org.ambraproject.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Action class for displaying a list of all top-level and second-level categories
 * in the taxonomy associated with any PLOS ONE article.
 *
 * @author John Callaway
 * @author Joe Osowski
 */
public class TaxonomyAction extends BaseActionSupport {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyAction.class);

  private Map<String, List<String>> topAndSecondLevelCategories;
  private Map<String, Object> categories;
  private TaxonomyService taxonomyService;
  private String root;
  private String[] filter;

  @Override
  public String execute() throws Exception {

    // Currently we only allow this from PLOS ONE.
    if (!"PLoSONE".equals(getCurrentJournal())) {
      return ERROR;
    }

    topAndSecondLevelCategories = taxonomyService.parseTopAndSecondLevelCategories(getCurrentJournal());
    categories = taxonomyService.parseCategories(getCurrentJournal());

    return SUCCESS;
  }

  /**
   * @return a map from top-level category to list of second-level subcategories
   *     associated with that top-level category
   */
  public Map<String, List<String>> getTopAndSecondLevelCategories() {
    return topAndSecondLevelCategories;
  }

  @Required
  public void setTaxonomyService(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }

  /**
   * Returns a set of categories and the count of children
   *
   * Note: This is will return a subset of categories post filter applied.
   *
   * If there is no filter, the set returned will be the root nodes
   * If "/Biotech" is set as the filter, the set returned will have "/BioTech" as the root node.
   * If "/Biotech/Genetics" is set as the filter, the set returned will have "/BioTech/Genetics" as the root node.
   *
   * @return A map of categories
   */
  @SuppressWarnings("unchecked")
  public Map<String, Integer> getCategories() {
    //Should probably implement this in the setter if the getter ever starts to get
    //called more then once

    if(this.root == null) {
      return MapUtils.keyCounts(categories);
    }

    //Ignore first slash if it exists
    if(this.root.trim().length() > 0 && this.root.trim().charAt(0) == '/') {
      this.root = this.root.trim().substring(1);
    }

    if(this.root.trim().length() == 0) {
      return MapUtils.keyCounts(categories);
    } else {
      String[] levels = this.root.split("/");
      Map<String, Object> res = categories;

      for(String level : levels) {
        res = (SortedMap<String, Object>)res.get(level);
      }

      return MapUtils.keyCounts(res);
    }
  }

  /**
   * We never want to return the entire map as it is too large.  However,
   * if filters are defined, we can return a subset
   *
   * @return
   */
  public Map<String, Object> getMap() {
    //Should probably implement this in the setter if the getter ever starts to get
    //called more then once
    if(this.filter != null && this.filter.length > 0) {
      return MapUtils.filterMap(categories, this.filter);
    }

    return null;
  }

  /**
   * Set the root for what categories are to be returned
   */
  public void setFilter(String[] filter) {
    this.filter = filter;
  }

  /**
   * Set the root for what categories are to be returned
   */
  public void setRoot(String root) {
    this.root = root;
  }
}
