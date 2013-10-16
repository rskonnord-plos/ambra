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
import org.ambraproject.views.article.ArticleInfo;

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
   * Find a "Featured Article" for the given subject area
   *
   * First query the database for a manually defined article for the term
   *
   *  If database doesn't have article, query SOLR for:
   *   - Most shared in social media (using same roll-up/counting methods used in search sort options) over the last 7 days.
   *   - If no shares
   *     - Most viewed Article (using same roll-up/counting methods used in search sort options) over the last 7 days.
   *     - If no views over past 7 days
   *       - most viewed Article (over all time) (using same roll-up/counting methods used in search sort options)
   *
   * @param journalKey the key of the journal
   * @param subjectArea the subject area to search for
   */
  public ArticleInfo getFeaturedArticleForSubjectArea(final String journalKey, final String subjectArea);

  /**
   * For the given journal, get a map of subject areas (key) and their article DOIS (values)
   * @param journalKey The journal key to look for
   *
   * @return a map of subject areas, and article DOIs
   */
  public Map<String, String> getFeaturedArticles(final String journalKey);

  /**
   * Delete a featured article
   *
   * @param journalKey the journal
   * @param subjectArea the subject area to remove
   * @param authID the authID of the current user
   */
  public void deleteFeaturedArticle(final String journalKey, final String subjectArea, final String authID);

  /**
   * Create a featured article
   *
   * @param journalKey the journal
   * @param subjectArea the subject area
   * @param doi the doi
   * @param authID the authID of the current user
   */
  public void createFeaturedArticle(final String journalKey, final String subjectArea, final String doi, final String authID);

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
