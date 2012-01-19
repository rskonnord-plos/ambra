/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Aggregation;
import org.topazproject.ambra.models.Journal;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.CriteriaFilterDefinition;
import org.topazproject.otm.filter.DisjunctiveFilterDefinition;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.filter.OqlFilterDefinition;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.mapping.java.ClassBinder;

/**
 * This service manages the journal filters that are to be applied to the Session. The filter
 * definitions are loaded to the {@link org.topazproject.otm.SessionFactory SessionFactory}
 * on demand. This means that any changes in filter definitions must immediately be visible
 * to the {@link org.topazproject.otm.Session Session} that is requesting it.
 *
 * <p>This service also maintains the filter definitions in a cache - purely as a mechanism
 * to advertise changes and to accept changes from peers in a distributed installation. Any
 * time filter definitions in the cache for a journal differ with the ones applied to the
 * SessionFactory, they are regenerated.
 *
 * <p>This service also listens to changes in Journal and updates both the cache and the local
 * definitions.
 *
 * @author Ronald Tschalär
 */
class JournalFilterService {
  private static final Log    log = LogFactory.getLog(JournalFilterService.class);

  private final SessionFactory           sf;
  private final Object                   monitor = new Object(); // for synchronizing
  private final Map<String, Set<String>> journalFilters = new HashMap<String, Set<String>>();
  private final Cache                    filterCache;          // key    -> Journal
  private final String                   keyPrefix;            // in cache for filters
  private final JournalKeyService        journalKeyService;

  /**
   * Create a new journal-filter-service instance. One and only one of these should be created for evey
   * session-factory instance.
   *
   * @param sf                the session-factory to use
   * @param filterCache       the cache to use for caching journal definitions
   * @param keyPrefix         the partition in the cache for filter-service use - could be null.
   * @param journalKeyService the key service to translate keys to journals
   */
  public JournalFilterService(SessionFactory sf, Cache filterCache, String keyPrefix,
      JournalKeyService journalKeyService) {
    this.sf             = sf;
    this.filterCache    = filterCache;
    this.keyPrefix      = (keyPrefix == null) ? "" : keyPrefix;
    this.journalKeyService = journalKeyService;

    filterCache.getCacheManager().registerListener(new AbstractObjectListener() {
      public void objectChanged(Session s, ClassMetadata cm, String id, Object o, Updates updates) {
        /*
         * Note: if a smart-collection rule was updated as opposed to
         * new rules added or deleted, we wouldn't be able to detect it.
         * In that case we need to be explicitly told. But currently
         * journal definitions are not updated on the fly. So even
         * this attempt to detect a change is not likely to be hit.
         */
        if ((o instanceof Journal) && ((updates == null) ||
            updates.isChanged("smartCollectionRules") || updates.isChanged("simpleCollection")))
          invalidateFilterCache((Journal)o);
      }
      public void objectRemoved(Session s, ClassMetadata cm, String id, Object o) {
        if (o instanceof Journal)
          invalidateFilterCache((Journal)o);
      }
    });
  }

  private static <K, T> void put(Map<K, Set<T>> map, K key, T value) {
    Set<T> values = map.get(key);
    if (values == null)
      map.put(key, values = new HashSet<T>());
    values.add(value);
  }

  private void invalidateFilterCache(Journal j) {
    log.warn("invalidating filter cache for journal '" + j.getKey() + "'");
    filterCache.remove(keyPrefix + j.getKey());
  }

  private String getFilterPrefix(String jName) {
    return jName + "-" + System.currentTimeMillis();
  }

  /* Must be invoked with journalCache monitor held and active tx on session */
  private void removeJournalFilters(String jName) {
    Set<String> oldDefs = journalFilters.remove(jName);
    if ((oldDefs != null) && !oldDefs.isEmpty()) {
      log.warn("Removing old filter-defs " + oldDefs + " for journal '" + jName + "'");
      for (String fn : oldDefs)
        sf.removeFilterDefinition(fn);
    }
    log.warn("Removing filter-cache entry for journal '" + jName + "'");
    filterCache.remove(keyPrefix + jName);
  }

  /* must be invoked with journalCache monitor held and active tx on session */
  private Set<String>  loadJournalFilters(Journal j, Session s) {
    if (log.isDebugEnabled())
      log.debug("loading journal filters for '" + j.getKey() + "'");

    // create the filter definitions
    Map<String, FilterDefinition> jfds =
        getAggregationFilters(j, getFilterPrefix(j.getKey()), s,
                              new HashSet<Aggregation>(), true);

    if (log.isDebugEnabled())
      log.debug("journal '" + j.getKey() + "' has filters: " + jfds);

    // clear old defs
    Set<String> defs = journalFilters.remove(j.getKey());
    if ((defs != null) && !defs.isEmpty()) {
      log.warn("Removing old filter-defs " + defs + " for journal '" + j.getKey() + "'");
      for (String fn : defs)
        sf.removeFilterDefinition(fn);
    }

    // save the new filter-defs
    if (jfds.isEmpty()) {
      log.warn("No new filter-defs to add for journal '" + j.getKey() + "'");
      defs = Collections.emptySet();
    } else {
      defs = new HashSet<String>();
      for (FilterDefinition fd : jfds.values()) {
        sf.addFilterDefinition(fd);
        defs.add(fd.getFilterName());
      }
      log.warn("Added new filter-defs " + defs + " for journal '" + j.getKey() + "'");
    }

    journalFilters.put(j.getKey(), defs);

    log.warn("Updating filter-cache for journal '" + j.getKey() + "' with " + defs);
    filterCache.put(keyPrefix + j.getKey(), new Cache.Item(defs));

    return defs;
  }

  /**
   * This creates filter definitions from all the simple- and smart-collection-rules, and'ing
   * together the individual smart rules and then or'ing the result with all the simple rules
   * (so that an object passes the filters if its id matches any of the given id's or if the
   * object satisfies all the smart rules). Filters are grouped by class, i.e. for each class
   * all the smart- and simple-rules for that class are collected into a filter definition
   * (and'ing and or'ing as descibed above). The filters are then expanded: any aggregation
   * objects they select are in recursively run through this method and the resulting filters
   * or'd with the current filters (again on a per-class basis). This way nested aggregations
   * are resolved into the their final rules.
   *
   * <p>Problem: The rules for journals don't usually specify the Aggregation class, but
   * subclasses thereof. Because filters are not applied for subclasses, listing all
   * Aggregations therefore does not get filtered and returns all Aggregations in the db.
   * A hack was added where list-self was done via an Aggregation filter, but that then
   * filtered all other aggregations such as Volumes and Issues. But changing that to filter
   * Journal's then causes all Aggregations to be loaded.
   *
   * <p>However, fixing the filter-application alone is not enough. Because we can't (shouldn't)
   * rely on a journal config having filters for all subclasses of Aggregation, we need to ensure
   * there's a rule for Aggregation itself (this scenario is likely when a strict nesting of
   * Aggregations is used, e.g. Journal containing Volume's, and Volume's containing Issue's, so
   * the Journal def does not contain rules for Issue and hence the first round of retrieving
   * Aggregation objects will retrieve all issues for all journals).
   *
   * <p>So, another hack for now: assume aggregations don't contain further aggregations, i.e.
   * that Journal contains rules for all Aggregation subclasses.
   */
  private Map<String, FilterDefinition> getAggregationFilters(Aggregation a, String pfx, Session s,
                                                              Set<Aggregation> processed,
                                                              boolean addSelf) {
    processed.add(a);

    // create a combined set of filter-defs from the rule-based and static collections
    Map<String, Set<FilterDefinition>> sfd = new HashMap<String, Set<FilterDefinition>>();
    int idx = 0;
    for (DetachedCriteria dc : a.getSmartCollectionRules()) {
      s.evict(dc);      // we don't want to save the possible alias change b/c we're in a r/o txn
      dc.setAlias(normalize(dc.getAlias(), sf));
      put(sfd, dc.getAlias(), new CriteriaFilterDefinition(pfx + "-rf-" + idx++, dc));
    }

    /* disable for now 5/13/2008
     * FIXME: use the addSelf below instead when queries are fixed
     * FIXME: note use of Journal v. Aggregation
     * if (addSelf) {
     *   DetachedCriteria dc =
     *     new DetachedCriteria("Journal").add(new SubjectCriterion(a.getId().toString()));
     *   put(sfd, dc.getAlias(), new CriteriaFilterDefinition(pfx + "-rf-" + idx++, dc));
     * }
     */

    Map<String, FilterDefinition> smartFilterDefs = combineFilterDefs(sfd, pfx + "-af-");

    Map<String, FilterDefinition> staticFilterDefs =
                                                buildSimpleCollFilters(a, pfx + "-sf-", s, addSelf);

    Map<String, FilterDefinition> afds =
        mergeFilterDefs(smartFilterDefs, staticFilterDefs, pfx + "-of-");

    // recursively resolve aggregations selected by the filters
    /* disabled for now
    Set<String> oldFilters = s.listFilters();
    for (String fn : oldFilters)
      s.disableFilter(fn);

    for (FilterDefinition fd : afds.values())
      s.enableFilter(fd);

    List<Aggregation> aggs = (List<Aggregation>) s.createCriteria(Aggregation.class).list();

    for (String fn : s.listFilters())
      s.disableFilter(fn);

    for (String fn : oldFilters)
      s.enableFilter(fn);

    idx = 0;
    for (Aggregation ag : aggs) {
      if (!processed.contains(ag))
        afds =
          mergeFilterDefs(afds, getAggregationFilters(ag, pfx + "-ag-" + idx, s, processed, false),
                          pfx + "-mf-" + idx++);
    }
    */

    // done
    return afds;
  }

  /**
   * and(...) the filter-defs for each class.
   */
  private Map<String, FilterDefinition> combineFilterDefs(Map<String, Set<FilterDefinition>> fds,
                                                          String pfx) {
    Map<String, FilterDefinition> res = new HashMap<String, FilterDefinition>();

    int idx = 0;
    for (Map.Entry<String, Set<FilterDefinition>> entry : fds.entrySet()) {
      ConjunctiveFilterDefinition and = new ConjunctiveFilterDefinition(pfx + idx++, entry.getKey());
      for (FilterDefinition fd : entry.getValue())
        and.addFilterDefinition(fd);

      res.put(entry.getKey(), and);
    }

    return res;
  }

  /**
   * Build the set of filter-definitions for the aggregation's simple collection.
   *
   * <p>This creates the following filter-definition:
   * <pre>
   *  select o from Aggregation a where a.id = :jid and o := a.simpleCollection;
   * </pre>
   *
   * @param a       the aggregation
   * @param pfx     the prefix to use for filter-names
   * @param s       the otm session to use
   * @param addSelf whether or not the filters should select the aggregation object itself too
   * @return the set of filter-defs
   */
  private Map<String, FilterDefinition> buildSimpleCollFilters(Aggregation a, String pfx, Session s,
                                                               boolean addSelf) {
    // FIXME: handle 'addSelf'

    /*
     * Get all the rdf:type's for each object.
     * Note to the unwary: this may look Article specific, but it isn't.
     */

    Results r = s.createQuery("select id, (select o.<rdf:type> from Object x) " +
        "from Object o, Aggregation a " +
        "where id := cast(o, Article).id and a.id = :a and o = a.simpleCollection;")
        .setParameter("a", a.getId()).execute();

    // build a map of uri's keyed by class
    Map<Class, Set<String>> idsByClass = new HashMap<Class, Set<String>>();
    while (r.next()) {
      String  id = r.getString(0);
      Results tr = r.getSubQueryResults(1);

      Set<String> types = new HashSet<String>();
      while (tr.next())
        types.add(tr.getString(0));

      if (types.size() == 0) {
        log.warn("object '" + id + "' from static collection either doesn't exist or has no type" +
                 " - ignoring it");
        continue;
      }

      ClassMetadata c = sf.getSubClassMetadata(null, EntityMode.POJO, types, null);
      if (c == null) {
        log.error("no class registered for static collection object '" + id + "'; types were '" +
                  types + "'");
        continue;
      }

      put(idsByClass, ((ClassBinder) c.getEntityBinder(EntityMode.POJO)).getSourceClass(), id);
    }

    // create a filter-definition for each class
    Map<String, FilterDefinition> fds = new HashMap<String, FilterDefinition>();
    for (Class c : idsByClass.keySet()) {
      String cname = normalize(c.getName(), sf);
      String qry   = "select o from Aggregation a where o := cast(a.simpleCollection, " + cname +
                     ") and a.id = <" + a.getId() + ">;";
      fds.put(cname, new OqlFilterDefinition(pfx + c.getName(), cname, qry));
    }

    return fds;
  }

  private static String normalize(String name, SessionFactory sf) {
    ClassMetadata cm = sf.getClassMetadata(name);
    return (cm != null) ? cm.getName() : name;
  }

  /**
   * or(...) together the filter-definitions for each class.
   */
  private static Map<String, FilterDefinition> mergeFilterDefs(Map<String, FilterDefinition> l1,
                                                               Map<String, FilterDefinition> l2,
                                                               String pfx) {
    Map<String, FilterDefinition> res = new HashMap<String, FilterDefinition>();
    int idx = 0;

    for (Map.Entry<String, FilterDefinition> entry : l1.entrySet()) {
      FilterDefinition f1 = entry.getValue();
      FilterDefinition f2 = l2.remove(entry.getKey());
      if (f2 == null) {
        res.put(entry.getKey(), f1);
      } else {
        DisjunctiveFilterDefinition or = new DisjunctiveFilterDefinition(pfx + idx++, entry.getKey());
        or.addFilterDefinition(f1);
        or.addFilterDefinition(f2);

        res.put(entry.getKey(), or);
      }
    }

    res.putAll(l2);

    return res;
  }

  /**
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   *
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  public Set<String> getFilters(String jName, Session s) {
    synchronized (monitor) {
      Set<String> local = journalFilters.get(jName);
      Cache.Item item = filterCache.get(keyPrefix + jName);
      Set<String> cached = (item == null) ? null : (Set<String>) item.getValue();
      if (local != null && local.equals(cached))
        return local;
      // there is a mismatch. So reload
      Journal j = retrieveJournalFromDB(jName, s);
      if (j != null)
        return loadJournalFilters(j, s);
      if (local != null)
        removeJournalFilters(jName);
      return null;
    }
  }

  /* must be inside active tx */
  private Journal retrieveJournalFromDB(String jName, Session s) {
    return journalKeyService.getJournal(jName, s);
  }
}
