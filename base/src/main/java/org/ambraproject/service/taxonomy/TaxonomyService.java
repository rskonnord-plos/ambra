/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.taxonomy;

import org.ambraproject.ApplicationException;
import org.ambraproject.views.CategoryView;

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
   * Flag a particular taxonomy term (by database ID) that it may not be correct.  The authID may be null if the user
   * is not logged in.  If so, the userProfileID is left null in the database
   *
   * @param articleID articleID
   * @param categoryID categoryID
   * @param authID the user's authID.
   */
  public void flagTaxonomyTerm(final long articleID, final long categoryID, final String authID);

  /**
   * Remove a flag from a particular taxonomy term (by database ID).  The authID may be null if the user
   * is not logged in.  If the userProfileID is left null, in the database, one flag for this articleID/categoryID
   * pair is removed
   *
   * @param articleID articleID
   * @param categoryID categoryID
   * @param authID the user's authID.
   */
  public void deflagTaxonomyTerm(final long articleID, final long categoryID, final String authID);

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
  CategoryView parseCategories(String currentJournal) throws ApplicationException;

  /**
   * Returns the number of articles, for a given journal, associated with the parent term and all
   * direct children of a subject taxonomy term.
   *
   * @param taxonomy the term in the taxonomy to examine, as well as its direct children
   * @param currentJournal specifies the journal
   * @return map from taxonomy term to count of articles
   * @throws ApplicationException
   */
  Map<String, Long> getCounts(CategoryView taxonomy, String currentJournal) throws ApplicationException;
}
