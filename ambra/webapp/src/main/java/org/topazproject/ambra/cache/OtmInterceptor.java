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
package org.topazproject.ambra.cache;

import java.io.Serializable;

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

import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Journal;
import org.topazproject.otm.Blob;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.PropertyBinder.RawFieldData;
import org.topazproject.otm.mapping.PropertyBinder.Streamer;
import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * An interceptor that listens to changes from OTM and maintains an object cache  that acts as
 * a second level cache for OTM.
 *
 * @author Pradeep Krishnan
 */
public class OtmInterceptor implements Interceptor {
  private static final Log     log            = LogFactory.getLog(OtmInterceptor.class);
  private static final String  NO_JOURNAL     = "__NO_JOURNAL__";
  private final CacheManager   cacheManager;
  private final Cache          objCache;
  private final JournalService journalService;

  /**
   * Creates a new OtmInterceptor object.
   *
   * @param cacheManager Cache manager
   * @param objCache the object cache
   * @param journalService the journal service
   */
  public OtmInterceptor(CacheManager cacheManager, Cache objCache, JournalService journalService) {
    this.cacheManager    = cacheManager;
    this.objCache        = objCache;
    this.journalService  = journalService;
  }

  /*
   * inherited javadoc
   */
  public Object getEntity(Session session, ClassMetadata cm, String id, Object instance) {
    if (cm.isView())
      return null;

    if (isFiltered(session, cm)) {
      String journal = getCurrentJournal();

      if (getChangedJournals().contains(journal)) {
        // Note: This is only in this transaction context. ie. Pending commit.
        if (log.isDebugEnabled())
          log.debug(cm.getName() + " with id <" + id +
                    "> must be loaded from the database since the journal '" + journal +
                    "' is marked as changed.");

        return null;
      }

      Cache.Item o = objCache.get(journal + "-" + id);

      if (o == null) {
        if (log.isDebugEnabled())
          log.debug(cm.getName() + " with id <" + id + "> is not in the cache for journal '" +
                    journal + "'");

        return null;
      }

      if (o.getValue() == null) {
        if (log.isDebugEnabled())
          log.debug(cm.getName() + " with id <" + id + "> is marked in cache as 'non-existant'." +
                    " Forcing OTM to return a 'null'");

        return NULL;
      }
    }

    Cache.Item val = objCache.get(id);

    if (val == null) {
      if (log.isDebugEnabled())
        log.debug(cm.getName() + " with id <" + id + "> is not in the cache");

      return null;
    }

    if (log.isDebugEnabled())
      log.debug(cm.getName() + " with id <" + id + "> is found in the cache");

    /*
     * Note: Assumes get filtering produces identical results.
     * This assumption currently holds because filters are
     * not currently applied on 'writes' by OTM.
     */
    return ((Entry) val.getValue()).get(session, cm, id, instance);
  }

  private void attach(Session session, ClassMetadata cm, String id, Object instance,
                      Collection<RdfMapper> fields, BlobMapper blob, boolean read) {
    if (cm.isView())
      return; // since we can't safely invalidate

    if (instance != null) {
      Cache.Item item = objCache.get(id);
      Object     val  = (item == null) ? null : item.getValue();
      Entry      e;

      if (val instanceof Entry) {
        e = (Entry) val;
        if (!read || !e.isEntityLoaded(cm))
          e = new Entry(e);  // copy before mutation
      } else {
        e             = new Entry();
        fields        = cm.getRdfMappers();
        blob          = cm.getBlobField();
      }

      if (val != e) {   // mutable?
        if (log.isDebugEnabled())
          log.debug(((val == null) ? "Inserting" : "Updating") + " cache entry <" + id + "> with data for " + cm);

        e.set(session, cm, instance, fields, blob);
        objCache.put(id, e);
      }
    }

    if (isFiltered(session, cm)) {
      String key = getCurrentJournal() + "-" + id;
      Object val = (instance == null) ? null : id;

      Cache.Item o = objCache.get(key);
      if ((o == null) || !same(o.getValue(), val))
        objCache.put(key, val);
    }
  }

  private static boolean same(Object o1, Object o2) {
    return (o1 == null) ? (o2 == null) : o1.equals(o2);
  }

  /*
   * inherited javadoc
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance) {
    if (log.isDebugEnabled())
      log.debug(cm.getName() + " with id <" + id + "> is "
                + ((instance != null) ? "loaded" : "null"));

    /*
     * Note: this will have to be also transactionally added - since the read
     * may have depended on prior writes that we are not aware of.
     */
    attach(session, cm, id, instance, cm.getRdfMappers(), cm.getBlobField(), true);
  }

  /*
   * inherited javadoc
   */
  public void onPostRead(Session session, ClassMetadata cm, String id, Object instance, Mapper m) {
    if (log.isDebugEnabled())
      log.debug(m.getName() + " in " + cm.getName() + " for id <" + id + "> is loaded");

    Collection rdf  = (m instanceof RdfMapper)  ? Collections.singleton(m) : Collections.emptySet();
    BlobMapper blob = (m instanceof BlobMapper) ? (BlobMapper) m           : null;

    attach(session, cm, id, instance, (Collection<RdfMapper>) rdf, blob, true);
  }

  /*
   * inherited javadoc
   */
  public void onPostWrite(Session session, ClassMetadata cm, String id, Object instance,
                          Interceptor.Updates updates) {
    if (log.isDebugEnabled())
      log.debug(cm.getName() + " with id <" + id + "> is updated.");

    if (updates == null)
      attach(session, cm, id, instance, cm.getRdfMappers(), cm.getBlobField(), false);
    else
      attach(session, cm, id, instance, updates.rdfMappers,
             updates.blobChanged ? cm.getBlobField() : null, false);

    /*
     * Note: if a smart-collection rule was updated as opposed to
     * new rules added or deleted, we wouldn't be able to detect it.
     * In that case we need to be explicitly told. But currently
     * journal definitions are not updated on the fly. So even
     * this attempt to detect a change is not likely to be hit.
     */
    if ((instance instanceof Journal) && ((updates == null) ||
          updates.rdfMappers.contains(cm.getMapperByName("smartCollectionRules")) ||
          updates.rdfMappers.contains(cm.getMapperByName("simpleCollection"))))
      journalChanged(((Journal) instance).getKey(), false);

    cacheManager.objectChanged(session, cm, id, instance, updates);
  }

  /*
   * inherited javadoc
   */
  public void onPostDelete(Session session, ClassMetadata cm, String id, Object instance) {
    if (log.isDebugEnabled())
      log.debug(cm.getName() + " with id <" + id + "> is deleted.");

    objCache.remove(id);

    if (isFiltered(session, cm)) {
      for (String journal : journalService.getAllJournalNames())
        if (!"".equals(journal))
          objCache.remove(journal + "-" + id);

      objCache.remove(NO_JOURNAL + "-" + id);
    }

    if (instance instanceof Journal)
      journalChanged(((Journal) instance).getKey(), true);

    cacheManager.objectRemoved(session, cm, id, instance);
  }

  /*
   * inherited javadoc
   */
  public void onPreDelete(Session session, ClassMetadata cm, String id, Object instance)
                            throws OtmException {
    if (log.isDebugEnabled())
      log.debug(cm.getName() + " with id <" + id + "> is about to be deleted.");

    try {
      cacheManager.removing(session, cm, id, instance);
    } catch (Exception e) {
      throw new OtmException("Aborted delete of '" + cm.getName() + "' with id <" + id + ">", e);
    }
  }

  private boolean isFiltered(Session session, ClassMetadata test) {
    for (String f : session.listFilters()) {
      ClassMetadata cm =
        session.getSessionFactory()
                .getClassMetadata(session.enableFilter(f).getFilterDefinition().getFilteredClass());

      if ((cm != null) && cm.isAssignableFrom(test, session.getEntityMode()))
        return true;
    }

    return false;
  }

  private void journalChanged(String journal, boolean deleted) {
    getChangedJournals().add(journal);

    String prefix = journal + "-";
    int    count  = 0;

    for (String key : (Collection<String>) objCache.getKeys()) {
      if (key.startsWith(prefix)) {
        objCache.remove(key);
        count++;
      }
    }

    if (count > 0)
      log.warn("Removed(pending-commit) " + count + " entries from '" + objCache.getName()
               + "' because the journal '" + journal + "' was "
               + (deleted ? "deleted." : "updated."));
  }

  private Set<String> getChangedJournals() {
    return cacheManager.getTxnContext().getChangedJournals();
  }

  private String getCurrentJournal() {
    String journal = journalService.getCurrentJournalName();

    if ((journal == null) || "".equals(journal))
      journal = NO_JOURNAL;

    return journal;
  }

  /**
   * An object cache-entry. The value map here is based on RdfDefinition name and therefore
   * it is dependent only on subject-id. Values are the serialized representations.
   */
  private static class Entry implements TripleStore.Result, Serializable {
    private final Set<String>               types;
    private final Set<String>               entities;
    private final Map<String, List<String>> fvalues;
    private final Map<String, List<String>> rvalues;
    private byte[]                          blob;
    private boolean                         immutable = false;

    public Entry() {
      blob     = null;
      types    = new HashSet<String>();
      entities = new HashSet<String>();
      fvalues   = new HashMap<String, List<String>>();
      rvalues   = new HashMap<String, List<String>>();
    }

    public Entry(Entry other) {
      blob     = copy(other.blob);
      types    = new HashSet<String>(other.types);
      entities = new HashSet<String>(other.entities);
      fvalues   = copy(other.fvalues);
      rvalues   = copy(other.rvalues);
    }

    public Map<String, List<String>> getFValues() {
      return Collections.unmodifiableMap(fvalues);
    }

    public Map<String, List<String>> getRValues() {
      return Collections.unmodifiableMap(rvalues);
    }

    public Object get(Session sess, ClassMetadata cm, String id, Object instance) {
      cm = sess.getSessionFactory().getSubClassMetadata(cm, sess.getEntityMode(), types, this);

      if (cm == null)
        return null;

      if (!isEntityLoaded(cm))
        return null;

      instance = cm.getEntityBinder(sess).loadInstance(instance, id, this, sess);
      if (cm.getBlobField() != null) {
        PropertyBinder binder = cm.getBlobField().getBinder(sess);
        Streamer streamer = binder.getStreamer();
        if (!streamer.isManaged())
          streamer.setBytes(binder, instance, copy(blob));
        else {
          Blob blob = sess.getSessionFactory().getBlobStore().getBlob(cm, id, instance,
                                                                      sess.getBlobStoreCon());
          streamer.attach(binder, instance, blob);
        }
      }

      return instance;
    }

    public boolean isEntityLoaded(ClassMetadata cm) {
      if (entities.contains(cm.getName()))
        return true;
      for (RdfMapper mapper : cm.getRdfMappers()) {
        if (!mapper.isPredicateMap()) {
          Map<String, List<String>> values = mapper.hasInverseUri() ? rvalues : fvalues;
          if (!values.containsKey(mapper.getUri()))
            return false;
        }
      }
      entities.add(cm.getName());
      return true;
    }

    public void set(Session sess, ClassMetadata cm, Object instance,
                    Collection<RdfMapper> fields, BlobMapper blobField) {

      if (immutable)
        throw new IllegalStateException("Attempting to update immutable cache entry");

      types.addAll(cm.getAllTypes());

      if (blobField != null) {
        PropertyBinder binder = blobField.getBinder(sess);
        Streamer streamer = binder.getStreamer();
        if (!streamer.isManaged())
          blob = copy(streamer.getBytes(binder, instance));
      }

      if (fields.size() != cm.getRdfMappers().size()) {
        // Get other fields to create the spliced
        Collection<RdfMapper> l = new ArrayList<RdfMapper>();
        for (RdfMapper m : fields) {
          if (!m.isPredicateMap())
            l.addAll(cm.getMappersByUri(m.getUri(), m.hasInverseUri()));
          else {
            l = cm.getRdfMappers();
            break;
          }
        }

        fields = l;
      }

      // special handling for predicate-map
      RdfMapper predMap = null;
      for (RdfMapper m : fields) {
        if (m.isPredicateMap()) {
          predMap = m;
          fvalues.clear();    // predicate-map + fields indicates complete set. Clear everything.
          PropertyBinder       b = m.getBinder(sess);
          // cache predicate-map values
          Map<String, List<String>> pmap = (Map<String, List<String>>) b.getRawValue(instance, true);
          for (Map.Entry<String, List<String>> entry : pmap.entrySet())
            fvalues.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
          break;
        }
      }

      // clear old values
      if (predMap == null) {
        for (RdfMapper m : fields) {
          Map<String, List<String>> vmap =  m.hasInverseUri() ? rvalues : fvalues;
          vmap.remove(m.getUri());
        }
      }

      // splice together the uri maps
      for (RdfMapper m : fields) {
        if (m.isPredicateMap())
          continue;
        PropertyBinder       b = m.getBinder(sess);
        RawFieldData data   = b.getRawFieldData(instance);
        List<String> nv;

        if (data != null)
          nv = data.getValues();
        else if (!m.isAssociation())
          nv = b.get(instance);
        else {
          List          a  = b.get(instance);
          List<String>  v  = new ArrayList<String>(a.size());
          ClassMetadata am =
            sess.getSessionFactory().getClassMetadata(m.getAssociatedEntity());

          for (Object o : a) {
            ClassMetadata aom =
              sess.getSessionFactory().getInstanceMetadata(am, sess.getEntityMode(), o);
            String        oid = (String) aom.getIdField().getBinder(sess).get(o).get(0);
            v.add(oid);
          }

          nv = v;
        }

        Map<String, List<String>> vmap =  m.hasInverseUri() ? rvalues : fvalues;

        List<String> v = vmap.get(m.getUri());
        if (v != null)
          v.addAll(nv);
        else
          vmap.put(m.getUri(), nv);
      }

      entities.add(cm.getName());
      immutable = true;
    }

    private static byte[] copy(byte[] src) {
      if (src == null)
        return null;

      byte[] dest = new byte[src.length];
      System.arraycopy(src, 0, dest, 0, src.length);

      return dest;
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> src) {
      if (src == null)
        return null;

      Map<String, List<String>> dest = new HashMap<String, List<String>>(src.size());

      for (Map.Entry<String, List<String>> e : src.entrySet())
        dest.put(e.getKey(), new ArrayList<String>(e.getValue()));

      return dest;
    }

    public String toString() {
      return "entities = " + entities + ", types = " + types + ", fvalues = " + fvalues
        + ", rvalues = " + rvalues + ", blob-size = " + ((blob == null) ? 0 : blob.length);
    }
  }
}
