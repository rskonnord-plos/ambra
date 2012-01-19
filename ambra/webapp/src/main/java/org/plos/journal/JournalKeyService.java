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

package org.plos.journal;

import java.net.URI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Restrictions;

import org.plos.cache.Cache;
import org.plos.cache.ObjectListener;
import org.plos.models.Article;
import org.plos.models.Journal;
import org.plos.web.VirtualJournalContext;



/**
 * This service manages journal definitions by journal key.
 *
 * @author Ronald Tschalär
 */
public class JournalKeyService {
  private static final Log    log = LogFactory.getLog(JournalKeyService.class);

  private final Cache                  journalCache;          // key    -> Journal
  private final String                 keyPrefix;


  /**
   * Create a new journal-service instance. One and only one of these should be created for evey
   * session-factory instance, and this must be done before the first session instance is created.
   *
   * @param journalCache the cache to use for caching journal definitions
   * @param keyPrefix  the prefix used to partition the cache
   */
  public JournalKeyService(Cache journalCache, String keyPrefix) {
    this.journalCache   = journalCache;
    this.keyPrefix      = keyPrefix;

    journalCache.getCacheManager().registerListener(new ObjectListener() {
      public void objectChanged(Session s, ClassMetadata cm, String id, Object o, Updates updates) {
      }
      public void objectRemoved(Session s, ClassMetadata cm, String id, Object o) {
        if (o instanceof Journal) 
          journalRemoved(((Journal)o).getKey(), s);
      }
    });
  }

  private void journalRemoved(String jName, Session sess) {
    journalCache.remove(keyPrefix);
    journalCache.remove(keyPrefix + jName);
  }


  /* must be inside active tx */
  private Journal retrieveJournalFromDB(String jName, Session s) {
    List l = s.createCriteria(Journal.class).add(Restrictions.eq("key", jName)).list();
    if (l.size() == 0)
      return null;

    return (Journal) l.get(0);
  }

  private Set<String> loadJournals(Session s) {
    List<Journal> l = s.createCriteria(Journal.class).list();
    Set<String> keys = new HashSet<String>(l.size());
    for (Journal j: l)
      keys.add(j.getKey());
    return keys;
  }

  /**
   * Get the specified journal. This assumes an active transaction on the session.
   *
   * @param jName  the journal's name
   * @return the journal, or null if no found
   */
  public Journal getJournal(final String jName, final Session sess) {
    if ("".equals(jName) || (jName == null))
      return null;

    String id = journalCache.get(keyPrefix + jName, -1,
        new Cache.SynchronizedLookup<String, RuntimeException>((keyPrefix + jName).intern()) {
          public String lookup() {
            Journal j = retrieveJournalFromDB(jName, sess);
            return (j == null) ? null : j.getId().toString();
          }
        });

    return (id == null) ? null : sess.get(Journal.class, id);
  }

  /**
   * Get the current JournalInfo object. 
   *
   * @param session the otm session to use
   * @return the journal, or null if none found.
   */
  public Journal getCurrentJournal(Session session) {
    return getJournal(getCurrentJournalKey(), session);
  }

  /**
   * Get the set of all the known journals. This assumes an active transaction on the session.
   *
   * @return all the journals, or the empty set if there are none
   */
  public Set<Journal> getAllJournals(Session session) {
    Set<Journal> res = new HashSet<Journal>();
    for (String jName : getAllJournalKeys(session))
       res.add(getJournal(jName, session));

    res.remove(null);  // in case something got deleted from under us
    return res;
  }

  public Set<String> getAllJournalKeys(final Session session) {
    return journalCache.get(keyPrefix, -1,
        new Cache.SynchronizedLookup<Set<String>, RuntimeException>(this) {
          public Set<String> lookup() throws RuntimeException {
            return loadJournals(session);
          }
        });
  }

  public String getCurrentJournalKey() {
    VirtualJournalContext vjc = (VirtualJournalContext) ServletActionContext.getRequest()
      .getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    return (vjc == null) ? null : vjc.getJournal();
  }

}
