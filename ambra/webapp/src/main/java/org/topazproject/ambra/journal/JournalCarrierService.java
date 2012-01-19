/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.java.ClassBinder;


/**
 * This service manages the reverse mapping from an object to the set of journals it appears in.
 *
 * <p>There should be exactly one instance of this class per {@link
 * org.topazproject.otm.SessionFactory SessionFactory} instance.
 *
 * @see org.topazproject.ambra.journal.JournalService
 *
 * @author Ronald Tschalär
 */
class JournalCarrierService {
  private static final Log    log = LogFactory.getLog(JournalCarrierService.class);
  private static final String RI_MODEL = "ri";

  private final SessionFactory           sf;
  private final Cache                    objectCarriers;        // obj-id -> Set<journal-key>
  private final JournalKeyService        journalKeyService;
  private final JournalFilterService     journalFilterService;

  /**
   * Create a new journal-service instance. One and only one of these should be created for evey
   * session-factory instance, and this must be done before the first session instance is created.
   *
   * @param sf           the session-factory to use
   * @param objectCache  the cache to use for caching the list of journals that carry each object
   * @param journalKeyService key service
   * @param journalFilterService filter service
   */
  public JournalCarrierService(SessionFactory sf, Cache objectCache, 
      JournalKeyService journalKeyService, JournalFilterService journalFilterService) {
    this.sf                   = sf;
    this.objectCarriers       = objectCache;
    this.journalKeyService    = journalKeyService;
    this.journalFilterService = journalFilterService;

    addObjectClassToSf();

    objectCarriers.getCacheManager().registerListener(new AbstractObjectListener() {
      public void objectChanged(Session s, ClassMetadata cm, String id, Object o, Updates updates) {
        /*
         * Note: if a smart-collection rule was updated as opposed to
         * new rules added or deleted, we wouldn't be able to detect it.
         * In that case we need to be explicitly told. But currently
         * journal definitions are not updated on the fly. So even
         * this attempt to detect a change is not likely to be hit.
         */
        if ((o instanceof Journal) && ((updates == null) ||
            updates.isChanged("smartCollectionRules") ||
            updates.isChanged("simpleCollection"))) {
          if (log.isDebugEnabled())
            log.debug("Dumping the carrier-cache because the journal rules/collection changed");
          objectCarriers.removeAll();
        }

        if (o instanceof Article) {
          if (log.isDebugEnabled())
            log.debug("Invalidating the cache entry for the article that was changed");
          objectCarriers.remove(((Article) o).getId());
        }
      }
      @Override
      public void objectRemoved(Session s, ClassMetadata cm, String id, Object o) {
        if (o instanceof Journal)
          objectCarriers.removeAll();

        if (o instanceof Article) {
          if (log.isDebugEnabled())
            log.debug("Invalidating the cache entry for the article that was deleted");
          objectCarriers.remove(((Article) o).getId());
        }
      }
    });
  }

  private void addObjectClassToSf() {
    Map<EntityMode, EntityBinder> binders = new HashMap<EntityMode, EntityBinder>();
    binders.put(EntityMode.POJO, new ClassBinder(Object.class));
    sf.setClassMetadata(new ClassMetadata(binders, "Object",
          Collections.EMPTY_SET, Collections.EMPTY_SET, RI_MODEL, null, Collections.EMPTY_SET, null,
          Collections.EMPTY_SET, Collections.EMPTY_SET));
  }

  public Set<String> getJournalKeysForObject(final URI oid, final Session session) {
    return objectCarriers.get(oid, -1,
        new Cache.SynchronizedLookup<Set<String>, RuntimeException>(oid.toString().intern()) {
          public Set<String> lookup() {
            return loadJournals(oid, session);
          }
        });
  }

  private Set<String> loadJournals(URI oid, Session session) {

    Set<String> journals = new HashSet<String>();

    Set<String> oldFilters = session.listFilters();
    for (String fn : oldFilters)
      session.disableFilter(fn);

    try {
      for (String journalKey : journalKeyService.getAllJournalKeys(session)) {

        for (String fn : journalFilterService.getFilters(journalKey, session))
          session.enableFilter(fn);

        try {
          // Query has to be created here because filters are applied at the time of creation
          Results r = session.createQuery("select o.id from Article o where o.id = :articleId;")
              .setUri("articleId", oid)
              .execute();
          // check if Article exists in the journal context
          if (r.next())
            journals.add(journalKey);
          r.close();
        } finally {
          for (String fn : session.listFilters())
            session.disableFilter(fn);
        }
      }
    } finally {
      for (String fn : oldFilters)
        session.enableFilter(fn);
    }

    return journals;
  }

  public Set<Journal> getJournalsForObject(final URI oid, final Session s) {
    Set<Journal> jnlList = new HashSet<Journal>();

    for (String key : getJournalKeysForObject(oid, s))
      jnlList.add(journalKeyService.getJournal(key, s));

    jnlList.remove(null); // just in case something was deleted

    return jnlList;
  }
}
