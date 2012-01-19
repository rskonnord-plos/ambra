/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.journal;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.criterion.SubjectCriterion;
import org.topazproject.otm.filter.ConjunctiveFilterDefinition;
import org.topazproject.otm.filter.CriteriaFilterDefinition;
import org.topazproject.otm.filter.DisjunctiveFilterDefinition;
import org.topazproject.otm.filter.FilterDefinition;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

import org.plos.models.Aggregation;
import org.plos.models.Journal;
import org.plos.models.UserProfile;
import org.plos.web.VirtualJournalContext;

import org.springframework.beans.factory.annotation.Required;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

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
  private static final Log    log = LogFactory.getLog(JournalService.class);
  private static final String RI_MODEL = "ri";

  private final SessionFactory           sf;
  private final Map<String, Set<String>> journalFilters = new HashMap<String, Set<String>>();
  private final Ehcache                  journalCache;          // key    -> Journal
  private final Ehcache                  objectCarriers;        // obj-id -> Set<journal-key>

  private       Session                  session;
  private       boolean                  isLocal;

  /**
   * Create a new journal-service instance. One and only one of these should be created for evey
   * session-factory instance, and this must be done before the first session instance is created.
   *
   * @param sf           the session-factory to use
   * @param journalCache the cache to use for caching journal definitions
   * @param objectCache  the cache to use for caching the list of journals that carry each object
   */
  public JournalService(SessionFactory sf, Ehcache journalCache, Ehcache objectCache) {
    this.sf             = sf;
    this.journalCache   = journalCache;
    this.objectCarriers = objectCache;

    initialize();
  }

  private void initialize() {
    sf.setClassMetadata(new ClassMetadata(Object.class, "Object", null, Collections.EMPTY_SET,
                                          RI_MODEL, null, null, Collections.EMPTY_SET));

    Session s = sf.openSession();
    try {
      TransactionHelper.doInTx(s, new TransactionHelper.Action<Void>() {
        public Void run(Transaction tx) {
          Set<Aggregation> preloaded = new HashSet<Aggregation>();

          synchronized (journalCache) {
            List<Journal> l = (List<Journal>) tx.getSession().createCriteria(Journal.class).list();
            for (Journal j : l) {
              preloadAggregation(j, preloaded, tx.getSession());
              JournalWrapper jw = new JournalWrapper(j);
              updateJournal(j.getKey(), jw, false, tx.getSession());
              journalCache.put(new Element(j.getKey(), jw));
            }

            Map<URI, Set<Journal>> cl = buildCarrierMap(null, tx.getSession());
            for (Map.Entry<URI, Set<Journal>> e : cl.entrySet())
              objectCarriers.put(new Element(e.getKey(), getKeys(e.getValue())));

            journalCache.getCacheEventNotificationService().
                         registerListener(new JournalCacheListener());
          }

          return null;
        }
      });
    } finally {
      s.close();
    }
  }

  /**
   * We need to track and update the journalFilters cache whenever a journal change occurs, no
   * matter whether the change occurred on the local machine or a remote one. So we use a
   * cache-listener on the journal-cache get notifications of changes to the cache. In order to
   * simplify things we do all update work from within this listener, even if the change was done
   * locally; local changes then become mostly straight cache operations.
   */
  private class JournalCacheListener implements CacheEventListener {
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException cnse) {
        throw new Error("How did this happen?", cnse);
      }
    }

    public void dispose() {
    }

    public void notifyElementEvicted(Ehcache cache, Element element) {
    }

    public void notifyElementExpired(Ehcache cache, Element element) {
    }

    public void notifyElementRemoved(Ehcache cache, Element element) {
      if (element.getValue() == null)
        return;                 // wasn't in cache, so nothing to do

      synchronized (journalCache) {
        final String jName = (String) element.getKey();
        doInTx(isLocal, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            removeJournal(jName, isLocal, tx.getSession());
            return null;
          }
        });
      }
    }

    public void notifyElementPut(Ehcache cache, Element element) {
      updateJournal(element);
    }

    public void notifyElementUpdated(Ehcache cache, Element element) {
      updateJournal(element);
    }

    public void notifyRemoveAll(Ehcache cache) {
      synchronized (journalCache) {
        journalFilters.clear();
      }
    }

    private void updateJournal(Element element) {
      synchronized (journalCache) {
        final String         jName = (String)         element.getKey();
        final JournalWrapper jw    = (JournalWrapper) element.getValue();

        doInTx(isLocal, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            if (!JournalService.this.updateJournal(jName, jw, isLocal, tx.getSession()))
              journalCache.removeQuiet(jName);
            return null;
          }
        });
      }
    }

    private <T> T doInTx(boolean isLocal, final TransactionHelper.Action<T> action) {
      if (isLocal)
        return action.run(session.getTransaction());

      Session s = sf.openSession();
      try {
        return TransactionHelper.doInTx(s, action);
      } finally {
        s.close();
      }
    }
  }

  private static <K, T> void put(Map<K, Set<T>> map, K key, T value) {
    Set<T> values = map.get(key);
    if (values == null)
      map.put(key, values = new HashSet<T>());
    values.add(value);
  }

  private static Set<String> getKeys(Set<Journal> jList) {
    Set<String> keys = new HashSet<String>();
    for (Journal j: jList)
      keys.add(j.getKey());
    return keys;
  }

  /* Must be invoked with journalCache monitor held and active tx on session */
  private boolean updateJournal(String jName, JournalWrapper jw, boolean updateCarrierMap,
                                Session s) {
    if (log.isDebugEnabled())
      log.debug("updating journal '" + jName + "'");

    if (jw.getJournal() == null) {
      Journal j = retrieveJournalFromDB(jName, s);
      if (j == null)
        return false;

      preloadAggregation(j, new HashSet<Aggregation>(), s);
      jw.setJournal(j);
    }

    loadJournalFilters(jw.getJournal(), new HashSet<Aggregation>(), s);

    if (updateCarrierMap)
      updateCarrierMap(jName, false, session);

    return true;
  }

  /* Must be invoked with journalCache monitor held and active tx on session */
  private void removeJournal(String jName, boolean updateCarrierMap, Session s) {
    if (log.isDebugEnabled())
      log.debug("removing journal '" + jName + "'");

    Set<String> oldDefs = journalFilters.remove(jName);
    if (oldDefs != null) {
      for (String fn : oldDefs)
        sf.removeFilterDefinition(fn);
    }

    if (updateCarrierMap)
      updateCarrierMap(jName, true, s);
  }

  /* must be invoked with journalCache monitor held and active tx on session */
  private void loadJournalFilters(Journal j, Set<Aggregation> preloaded, Session s) {
    if (log.isDebugEnabled())
      log.debug("loading journal filters for '" + j.getKey() + "'");

    // create the filter definitions
    Map<String, FilterDefinition> jfds =
        getAggregationFilters(j, j.getKey() + "-" + System.currentTimeMillis(), s,
                              new HashSet<Aggregation>(), true);

    if (log.isDebugEnabled())
      log.debug("journal '" + j.getKey() + "' has filters: " + jfds);

    // clear old defs
    Set<String> oldDefs = journalFilters.remove(j.getKey());
    if (oldDefs != null) {
      for (String fn : oldDefs)
        sf.removeFilterDefinition(fn);
    }

    // save the new filter-defs
    for (FilterDefinition fd : jfds.values()) {
      sf.addFilterDefinition(fd);
      put(journalFilters, j.getKey(), fd.getFilterName());
    }

    if (!journalFilters.containsKey(j.getKey()))
      journalFilters.put(j.getKey(), Collections.EMPTY_SET);
  }

  /* must be invoked with journalCache monitor held and active tx on session */
  private void updateCarrierMap(String jName, boolean deleted, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    Set<URI> obj = deleted ? Collections.EMPTY_SET : getObjects(jName, null, s);

    for (URI o : (List<URI>) objectCarriers.getKeys()) {
      Element e = objectCarriers.get(o);
      if (e == null)
        continue;
      Set<String> keys = (Set<String>) e.getObjectValue();

      boolean mod = keys.remove(jName);
      if (obj.remove(o)) {
        keys.add(jName);
        mod ^= true;
      }

      if (mod)
        objectCarriers.put(e);
    }
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
      dc.setAlias(normalize(dc.getAlias(), sf));
      put(sfd, dc.getAlias(), new CriteriaFilterDefinition(pfx + "-rf-" + idx++, dc));
    }
    if (addSelf) {      // FIXME: use the addSelf below instead when queries are fixed
      // FIXME: note use of Journal v. Aggregation
      DetachedCriteria dc =
          new DetachedCriteria("Journal").add(new SubjectCriterion(a.getId().toString()));
      put(sfd, dc.getAlias(), new CriteriaFilterDefinition(pfx + "-rf-" + idx++, dc));
    }

    Map<String, FilterDefinition> smartFilterDefs = combineFilterDefs(sfd, pfx + "-af-");

    List<URI> uris = new ArrayList<URI>(a.getSimpleCollection());
    /*
    if (addSelf)
      uris.add(a.getId());
    */
    Map<String, FilterDefinition> staticFilterDefs = buildStaticFilters(uris, pfx + "-sf-", s);

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
    for (String cls : fds.keySet()) {
      ConjunctiveFilterDefinition and = new ConjunctiveFilterDefinition(pfx + idx++, cls);
      for (FilterDefinition fd : fds.get(cls))
        and.addFilterDefinition(fd);

      res.put(cls, and);
    }

    return res;
  }

  /**
   * Build the set of filter-definitions for the statically-defined list of objects.
   *
   * <p>Ideally this would create filter-definitions based on something like the following OQL
   * query:
   * <pre>
   *  select o from java.lang.Object o, Journal j where j.id = :jid and o = j.simpleCollection;
   * </pre>
   * Unfortunately, however, this cannot be turned into a Criteria which currently is needed in
   * order to apply the filter to Criteria-based queries. So instead it creates an explicit list
   * of or'd id's.
   *
   * @param uris the list of id's of the objects
   * @param pfx  the prefix to use for filter-names
   * @param s    the otm session to use
   * @return the set of filter-defs
   */
  private Map<String, FilterDefinition> buildStaticFilters(List<URI> uris, String pfx, Session s) {
    if (uris.size() == 0)
      return Collections.EMPTY_MAP;

    // get all the rdf:type's for each object
    StringBuilder typeQry = new StringBuilder(110 + uris.size() * 70);
    // Note to the unwary: this may look Article specific, but it isn't.
    typeQry.append("select id, (select o.<rdf:type> from Object x) from Object o ").
            append("where id := cast(o, Article).id and (");

    for (URI uri : uris)
      typeQry.append("id = <").append(uri).append("> or ");

    typeQry.setLength(typeQry.length() - 4);
    typeQry.append(");");

    Results r = s.createQuery(typeQry.toString()).execute();

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

      Class c = sf.mostSpecificSubClass(Object.class, types);
      if (c == null) {
        log.error("no class registered for static collection object '" + id + "'; types were '" +
                  types + "'");
        continue;
      }

      put(idsByClass, c, id);
    }

    // create a filter-definition for each class
    Map<String, FilterDefinition> fds = new HashMap<String, FilterDefinition>();
    for (Class c : idsByClass.keySet()) {
      String cname = normalize(c.getName(), sf);
      DetachedCriteria dc = new DetachedCriteria(cname);
      Disjunction or = Restrictions.disjunction();
      dc.add(or);

      for (String id : idsByClass.get(c))
        or.add(new SubjectCriterion(id));

      fds.put(cname, new CriteriaFilterDefinition(pfx + c.getName(), dc));
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

    for (String cls : l1.keySet()) {
      FilterDefinition f1 = l1.get(cls);
      FilterDefinition f2 = l2.remove(cls);
      if (f2 == null) {
        res.put(cls, f1);
      } else {
        DisjunctiveFilterDefinition or = new DisjunctiveFilterDefinition(pfx + idx++, cls);
        or.addFilterDefinition(f1);
        or.addFilterDefinition(f2);

        res.put(cls, or);
      }
    }

    res.putAll(l2);

    return res;
  }

  private static void preloadAggregation(Aggregation a, Set<Aggregation> preloaded, Session s) {
    if (a == null || preloaded.contains(a))
      return;
    preloaded.add(a);

    if (a.getEditorialBoard() != null && a.getEditorialBoard().getEditors() != null) {
      for (UserProfile up : a.getEditorialBoard().getEditors())
        up.getInterests();
    }

    a.getSimpleCollection();

    for (DetachedCriteria dc : a.getSmartCollectionRules())
      preloadCriteria(dc, s);

    preloadAggregation(a.getSupersedes(), preloaded, s);
    preloadAggregation(a.getSupersededBy(), preloaded, s);
  }

  private static void preloadCriteria(DetachedCriteria c, Session s) {
    c.getExecutableCriteria(s);         // easiest way of touching everything...
  }

  /* must be invoked with journalCache monitor held and active tx on session and in local context */
  private Map<URI, Set<Journal>> buildCarrierMap(URI oid, Session s) {
    Map<URI, Set<Journal>> carriers = new HashMap<URI, Set<Journal>>();

    for (String jName : journalFilters.keySet()) {
      Journal j = getJournalInternal(jName);

      Set<URI> obj = getObjects(jName, oid, s);
      for (URI o : obj)
        put(carriers, o, j);
    }

    return carriers;
  }

  private Set<URI> getObjects(String jName, URI obj, Session s) {
    Set<String> oldFilters = s.listFilters();
    for (String fn : oldFilters)
      s.disableFilter(fn);

    for (String fn : journalFilters.get(jName))
      s.enableFilter(fn);

    Set<URI> res = new HashSet<URI>();

    // XXX: should be "... from Object ..." but filters don't get applied then
    String q = "select id from Article o where id := cast(o, Article).id" +
                (obj != null ? " and id = <" + obj + ">;" : ";");
    try {
      Results r = s.createQuery(q).execute();
      while (r.next())
        res.add(r.getURI(0));
    } finally {
      for (String fn : s.listFilters())
        s.disableFilter(fn);

      for (String fn : oldFilters)
        s.enableFilter(fn);
    }

    return res;
  }

  /* must be inside active tx */
  private Journal retrieveJournalFromDB(String jName, Session s) {
    List l = s.createCriteria(Journal.class).add(Restrictions.eq("key", jName)).list();
    if (l.size() == 0)
      return null;

    return (Journal) l.get(0);
  }

  /* must be invoked with journalCache monitor held and active tx on session and in local context */
  private Journal getJournalInternal(String jName) {
    Element e = journalCache.get(jName);
    if (e == null) {
      isLocal = true;
      try {
        journalCache.put(e = new Element(jName, new JournalWrapper(null)));
      } finally {
        isLocal = false;
      }
    }

    return ((JournalWrapper) e.getValue()).getJournal();
  }

  /**
   * Get the names of the {@link org.topazproject.otm.Filter session filters} associated with the
   * specified journal.
   *
   * @param jName the journal's name (key)
   * @return the list of filters (which may be empty), or null if no journal by the given name is
   *         known
   */
  public Set<String> getFilters(String jName) {
    synchronized (journalCache) {
      return journalFilters.get(jName);
    }
  }

  /**
   * Get the specified journal. This assumes an active transaction on the session.
   *
   * @param jName  the journal's name
   * @return the journal, or null if no found
   */
  public Journal getJournal(String jName) {
    synchronized (journalCache) {
      return getJournalInternal(jName);
    }
  }

  /**
   * Get the current journal. This assumes an active transaction on the session.
   *
   * @return the journal, or null if none found.
   * @see #getJournal(String jName).
   */
  public Journal getJournal() {
    final String jName = ((VirtualJournalContext) ServletActionContext.getRequest()
      .getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();

    return getJournal(jName);
  }

  /**
   * Get the set of all the known journals. This assumes an active transaction on the session.
   *
   * @return all the journals, or the empty set if there are none
   */
  public Set<Journal> getAllJournals() {
    Set<Journal> res = new HashSet<Journal>();
    synchronized (journalCache) {
      for (String jName : journalFilters.keySet())
        res.add(getJournalInternal(jName));
    }
    return res;
  }

  /**
   * Signal that the given journal was modified (added or changed). The filters and object lists
   * will be updated.  This assumes an active transaction on the session.
   *
   * @param j the journal that was modified.
   */
  public void journalWasModified(Journal j) {
    synchronized (journalCache) {
      isLocal = true;
      try {
        journalCache.put(new Element(j.getKey(), new JournalWrapper(j)));
      } finally {
        isLocal = false;
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Journal was modified: " + j.getKey() + " (" + j.getEIssn() + ")");
    }
  }

  /**
   * Signal that the given journal was deleted. The object lists will be updated.
   * This assumes an active transaction on the session.
   *
   * @param j the journal that was deleted.
   */
  public void journalWasDeleted(Journal j) {
    synchronized (journalCache) {
      isLocal = true;
      try {
        journalCache.remove(j.getKey());
      } finally {
        isLocal = false;
      }
    }
  }

  /**
   * Get the list of journals which carry the given object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   * @return the list of journals which carry this object; will be empty if this object
   *         doesn't belong to any journal
   */
  public Set<Journal> getJournalsForObject(URI oid) {
    Set<Journal> jnlList;

    synchronized (journalCache) {
      Element jl = objectCarriers.get(oid);
      if (jl == null) {
        Collection<Set<Journal>> jnlSets = buildCarrierMap(oid, session).values();
        jnlList = (jnlSets.size() > 0) ? jnlSets.iterator().next() : Collections.EMPTY_SET;
        objectCarriers.put(new Element(oid, getKeys(jnlList)));
      } else {
        jnlList = new HashSet<Journal>();
        for (String key : (Set<String>) jl.getObjectValue()) {
          Journal j = getJournalInternal(key);
          if (j == null)
            log.error("Unexpected null journal for key '" + key + "'");
          else
            jnlList.add(j);
        }
      }
    }

    return jnlList;
  }

  /**
   * Notify the journal service of a newly added object (e.g. an article). This assumes an active
   * transaction on the session.
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  public void objectWasAdded(URI oid) {
    synchronized (journalCache) {
      objectCarriers.remove(oid);
      getJournalsForObject(oid);
    }

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was added and belongs to journals: " +
                objectCarriers.get(oid).getObjectValue());
  }

  /**
   * Notify the journal service of a recently deleted object (e.g. article).
   *
   * @param oid the info:&lt;oid&gt; uri of the object
   */
  public void objectWasDeleted(URI oid) {
    synchronized (journalCache) {
      objectCarriers.remove(oid);
    }

    if (log.isDebugEnabled())
      log.debug("object '" + oid + "' was removed");
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
   * A simple wrapper around a Journal object. The point of this wrapper is that it's
   * serializable even though Journal itself is not, so that normal cache replication
   * can be used to propagate updates.
   */
  private static class JournalWrapper implements Serializable {
    private transient Journal j;

    public JournalWrapper(Journal j) {
      this.j = j;
    }

    public Journal getJournal() {
      return j;
    }

    public void setJournal(Journal j) {
      this.j = j;
    }
  }
}
