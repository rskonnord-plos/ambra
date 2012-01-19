/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.topazproject.ambra.journal;

import org.topazproject.ambra.models.Journal;

import java.net.URI;
import java.util.Set;

/**
 * This service manages journal definitions and associated info. All retrievals and modifications
 * should go through here so it can keep the cache up-to-date.
 *
 * <p>There should be exactly one instance of this class per {@link
 * org.topazproject.otm.SessionFactory SessionFactory} instance. Also, the instance must be created
 * before any {@link org.topazproject.otm.Session Session} instance is created as it needs to
 * register the filter-definition with the session-factory.
 *
 * <p>This services does extensive caching of journal objects, the filters associated with each
 * journal, and the list of journals each object (article) belongs to (according to the filters).
 * For this reason it must be notified any time a journal or article is added, removed, or changed.
 *
 * @author Ronald Tschalär
 */
public interface JournalService {

  /**
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   *
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  public Set<String> getFilters(String jName);

  /**
   * Get the specified journal.
   *
   * @param jName  the journal's name
   * @return the journal, or null if not found
   */
  public Journal getJournal(String jName);

  /**
   * Get the ID of a Journal from its <strong>eIssn</strong>.
   *
   * @param eIssn  the journal's eIssn value.
   * @return the journal, or null if not found
   */
  public Journal getJournalIdByEissn(String eIssn);

  /**
   * This method makes services dependent on servlet context.
   * Use getCurrentJournal() method in Action class instead.
   *
   * Get the current journal.
   *
   * @return the current journal
   */
  @Deprecated
  public Journal getCurrentJournal();

  /**
   * Get the set of all the known journals.
   *
   * @return all the journals, or the empty set if there are none
   */
  public Set<Journal> getAllJournals();

  /**
   * Get the names of all the known journals.
   *
   * @return the list of names; may be empty if there are no known journals
   */
  public Set<String> getAllJournalNames();

  /**
   * This method makes services dependent on servlet context. 
   * Use getCurrentJournal() method in Action class instead.
   * .
   * Get the name of the current journal.
   *
   * @return the name of the current journal, or null if there is no current journal
   */
  @Deprecated
  public String getCurrentJournalName();

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  public Set<Journal> getJournalsForObject(URI oid);

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @param lookInCache If true method will first check ArtcileCarrier cache.
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  public Set<Journal> getJournalsForObject(URI oid, boolean lookInCache);

  /**
   * Get the list of journal URIs which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journal URIs which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  public Set<String> getJournalURIsForObject(URI oid);

}
