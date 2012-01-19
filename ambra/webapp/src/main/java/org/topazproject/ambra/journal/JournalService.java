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

import java.net.URI;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.Configuration;

import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.util.TextUtils;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

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
public class JournalService {
  private static final Logger    log = LoggerFactory.getLogger(JournalService.class);
  private static final String RI_MODEL = "ri";

  private SessionFactory           sf;
  private Cache                    journalCache;          // key    -> Journal
  private Cache                    objectCarriers;        // obj-id -> Set<journal-key>
  private Configuration            configuration;

  private JournalKeyService        journalKeyService;
  private JournalFilterService     journalFilterService;
  private JournalCarrierService    journalCarrierService;

  private Session                  session;
  private Invalidator invalidator;

  private HashMap<String, String> journalsByEissn; // Key = eIssn. Value = Journal ID.

  /**
   * Create a new journal-service instance. One and only one of these should be created for every
   * session-factory instance, and this must be done before the first session instance is created.
   */
  public JournalService() {
  }

  /**
   * Initialize this service. Must be called after all properties are set.
   */
  @Transactional(readOnly = true)
  public void init() {
    journalKeyService = new JournalKeyService(configuration, journalCache, "KEY-");
    journalFilterService = new JournalFilterService(sf, journalCache, "FILTER-", journalKeyService);
    journalCarrierService = new JournalCarrierService(sf, objectCarriers,
                                                      journalKeyService, journalFilterService);
  }

  /**
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   *
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  @Transactional(readOnly = true)
  public Set<String> getFilters(String jName) {
    return journalFilterService.getFilters(jName, session);
  }

  /**
   * Get the specified journal.
   *
   * @param jName  the journal's name
   * @return the journal, or null if not found
   */
  @Transactional(readOnly = true)
  public Journal getJournal(String jName) {
    return journalKeyService.getJournal(jName, session);
  }

  /**
   * Get the ID of a Journal from its <strong>eIssn</strong>.
   *
   * @param eIssn  the journal's eIssn value.
   * @return the journal, or null if not found
   */
  @Transactional(readOnly = true)
  public synchronized Journal getJournalIdByEissn(String eIssn) {
    if (journalsByEissn == null) {
      journalsByEissn = new HashMap<String, String>();
      for (Journal journal : getAllJournals()) {
        journalsByEissn.put(journal.geteIssn(), journal.getId().toString());
      }
    }
    if (eIssn == null || "".equals(eIssn))
      return null;
    return session.get(Journal.class, journalsByEissn.get(eIssn));
  }

  /**
   * This method makes services dependent on servlet context.
   * Use getCurrentJournal() method in Action class instead.
   *
   * Get the current journal.
   *
   * @return the current journal
   */
  @Deprecated
  @Transactional(readOnly = true)
  public Journal getCurrentJournal() {
    return journalKeyService.getCurrentJournal(session);
  }

  /**
   * Get the set of all the known journals.
   *
   * @return all the journals, or the empty set if there are none
   */
  @Transactional(readOnly = true)
  public Set<Journal> getAllJournals() {
    return journalKeyService.getAllJournals(session);
  }

  /**
   * Get the names of all the known journals.
   *
   * @return the list of names; may be empty if there are no known journals
   */
  @Transactional(readOnly = true)
  public Set<String> getAllJournalNames() {
    return journalKeyService.getAllJournalKeys();
  }

  /**
   * This method makes services dependent on servlet context. 
   * Use getCurrentJournal() method in Action class instead.
   * .
   * Get the name of the current journal.
   *
   * @return the name of the current journal, or null if there is no current journal
   */
  @Deprecated
  public String getCurrentJournalName() {
    return journalKeyService.getCurrentJournalKey();
  }

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  @Transactional(readOnly = true)
  public Set<Journal> getJournalsForObject(URI oid) {
    return journalCarrierService.getJournalsForObject(oid, session, true);
  }

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @param lookInCache If true method will first check ArtcileCarrier cache.
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  @Transactional(readOnly = true)
  public Set<Journal> getJournalsForObject(URI oid, boolean lookInCache) {
    return journalCarrierService.getJournalsForObject(oid, session, lookInCache);
  }

  /**
   * Get the list of journal URIs which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journal URIs which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  @Transactional(readOnly = true)
  public Set<String> getJournalURIsForObject(URI oid) {
    return journalCarrierService.getJournalKeysForObject(oid, session);
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the OTM session-facctory. Called by spring's bean wiring.
   *
   * @param sf the otm session-facctory
   */
  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  /**
   * Set the journal cache. Called by spring's bean wiring.
   *
   * @param journalCache the journal cache
   */
  @Required
  public void setJournalCache(Cache journalCache) {
    this.journalCache = journalCache;
  }

  /**
   * Set the carrier cache. Called by spring's bean wiring.
   *
   * @param carrierCache the carrier cache
   */
  @Required
  public void setCarrierCache(Cache carrierCache) {
    this.objectCarriers = carrierCache;

    if(invalidator == null) {
      this.objectCarriers.getCacheManager().registerListener(invalidator = new Invalidator());
    }
  }

  /**
   * Setter method for configuration. Injected through Spring.
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  private class Invalidator extends AbstractObjectListener {
    @Override
    public void objectChanged(Session s, ClassMetadata cm, String id, Object o, Interceptor.Updates updates) {
      if (o instanceof Article) {
        log.debug("Invalidating the cache entry for the article that was changed");
        objectCarriers.remove(((Article) o).getId());
      } else if (o instanceof Journal) {
        if (updates.isChanged("simpleCollection")) {
          log.debug("Invalidating the cache entry for the journal that was changed");

          List<String> simpleCollection = TextUtils.toStringList(((Journal) o).getSimpleCollection());
          List<String> oldValues = updates.getOldValue("simpleCollection");

          Set<String> added = new HashSet<String>(simpleCollection);
          added.removeAll(oldValues);
          for (String articleId : added) {
            objectCarriers.remove(articleId);
          }

          Set<String> deleted = new HashSet<String>(oldValues);
          deleted.removeAll(simpleCollection);
          for (String articleId : deleted) {
            objectCarriers.remove(articleId);
          }
        }
        if (updates.isChanged("smartCollectionRules")) {
          log.debug("Invalidating all cache entries for smart collection rules change");
          objectCarriers.removeAll();
        }
      }
    }

    @Override
    public void objectRemoved(Session s, ClassMetadata cm, String id, Object o) {
      if (o instanceof Article) {
        log.debug("Invalidating the cache entry for the article that was deleted");
        objectCarriers.remove(((Article) o).getId());
      } else if (o instanceof Journal) {
        log.debug("Invalidating all cache entries for the journal that was deleted");
        objectCarriers.removeAll();
      }
    }
  }  
}
