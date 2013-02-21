/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.category;

import java.util.List;

/**
 * Service used to retrieve article category information from ambra.
 * <p/>
 * Note that this service is intended for apps that want to retrieve article category
 * information without directly communicating with the ambra database.  It should not
 * be confused with {@link org.ambraproject.service.article.AIArticleClassifier} or
 * {@link org.ambraproject.service.taxonomy.TaxonomyService}.
 */
public interface CategoryService {

  /**
   * Returns category strings from ambra servers.
   *
   * @param doi article DOI
   * @return List of category Strings
   */
  List<String> fetchCategoriesFromAmbra(String doi);

  /**
   * Returns a list of all unique top-level categories in the supplied input.
   * Top-level categories are returned in the order they are encountered.
   *
   * @param categories List of forward-slash-delimited category paths ("topLevel/foo/bar")
   * @return List of unique top-level categories ("topLevel")
   */
  List<String> getTopLevelCategories(List<String> categories);

  void setAmbraServer(String ambraServer);
}
