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

package org.ambraproject.service.taxonomy;

import org.ambraproject.ApplicationException;
import java.util.List;
import java.util.Map;

/**
 * Provides services related to the subject category taxonomy.
 *
 * @author John Callaway
 * @author Joe Osowski
 */
public interface TaxonomyService {

  /**
   * Parses a list of slash-delimited categories, as returned by solr, into a sorted map
   * from top-level category to a list of second-level categories.
   *
   * @param currentJournal The current journal
   *
   * @return map with keys of (sorted) top-level categories
   */
  Map<String, List<String>> parseTopAndSecondLevelCategories(String currentJournal) throws ApplicationException;

  /**
   * For the current journal return a complete structured map of the taxonomic categories
   *
   * @param currentJournal the current journal
   *
   * @return a complete structured map of the taxonomic categories
   *
   * @throws ApplicationException
   */
  Map<String, Object> parseCategories(String currentJournal) throws ApplicationException;
}
