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

package org.ambraproject.service.taxonomy;

import java.util.List;
import java.util.SortedMap;

/**
 * Provides services related to the subject category taxonomy.
 */
public interface TaxonomyService {

  /**
   * Parses a list of slash-delimited categories, as returned by solr, into a sorted map
   * from top-level category to a list of second-level categories.
   *
   * @param fullCategoryPaths List of category strings, slash-delimited to indicate hierarchy.
   *     Example:
   *     "/Biology and life sciences/Plant science/Plant anatomy/Flowers"
   * @return map with keys of (sorted) top-level categories
   */
  SortedMap<String, List<String>> parseTopAndSecondLevelCategories(List<String> fullCategoryPaths);
}
