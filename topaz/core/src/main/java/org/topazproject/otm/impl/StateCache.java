/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A cache used to track modificationsto an object instance.
 *
 * @author Pradeep Krishnan
 */
class StateCache {
  private static final Log                       log    = LogFactory.getLog(StateCache.class);
  private Map<ObjectReference<?>, InstanceState> states =
    new HashMap<ObjectReference<?>, InstanceState>(1001);
  private ReferenceQueue<ObjectReference<?>>     queue  = new ReferenceQueue<ObjectReference<?>>();

  static enum BlobChange {noChange, insert, update, delete};

  /**
   * Insert an object into the cache.
   *
   * @param o the object to insert
   * @param cm it's class metadata
   * @param session the Session that is requesting the insert
   */
  public void insert(Object o, ClassMetadata cm, Session session) throws OtmException {
    expunge();
    states.put(new ObjectReference(o, queue), new InstanceState(o, cm, session));
  }

  /**
   * Update an object's state. If the object is in the cache, the fields that are updated are
   * returned, Otherwise the object is inserted and a null is returned to indicate this condition.
   *
   * @param o the object to update/insert
   * @param cm it's class metaday
   * @param session the Session that is requestin the update
   *
   * @return the collection of fields that were updated or null
   */
  public Collection<RdfMapper> update(Object o, ClassMetadata cm, Session session)
      throws OtmException {
    expunge();

    InstanceState is = states.get(new ObjectReference(o));

    if (is != null)
      return is.update(o, cm, session);

    states.put(new ObjectReference(o, queue), new InstanceState(o, cm, session));

    return null;
  }

  /**
   * Update a blob digest.
   *
   * @param o the object
   * @param bf the blob field
   *
   * @return a value indicating how the blob changed
   */
  public BlobChange digestUpdate(Object o, Binder bf) throws OtmException {
    // expected to be called after update - so no expunge and no null check
    return states.get(new ObjectReference(o)).digestUpdate(o, bf);
  }

  /** 
   * Start doing change-track monitoring on this field too.
   * 
   * @param o the object whose field was lazy loaded
   * @param field the field that is lazy loaded
   * @param session the session that is notifying this 
   *
   * @throws OtmException on an error
   */
  public void delayedLoadComplete(Object o, RdfMapper field, Session session) throws OtmException {
    // expected to be called after insert - so no expunge
    InstanceState is = states.get(new ObjectReference(o));
    if (is != null)
      is.delayedLoadComplete(o, field, session);
  }


  /**
   * Removes an object from the cache.
   *
   * @param o the object to remove
   */
  public void remove(Object o) {
    expunge();
    states.remove(new ObjectReference(o));
  }

  private void expunge() {
    int                count = 0;
    ObjectReference<?> ref;

    while ((ref = (ObjectReference<?>) queue.poll()) != null) {
      states.remove(ref);
      count++;
    }

    if (log.isDebugEnabled() && (count > 0))
      log.debug("Expunged " + count + " objects from states-cache. Size is now " + states.size());
  }

  /**
   * The state of an object as last seen by a Session.
   */
  private static class InstanceState {
    private final Map<RdfMapper, List<String>> vmap; // serialized field values
    private Map<String, List<String>>       pmap; // serialized predicate map values
    private int blobLen = 0;
    private byte[] blobDigest;

    public <T>InstanceState(T instance, ClassMetadata cm, Session session) throws OtmException {
      vmap                   = new HashMap<RdfMapper, List<String>>();

      for (RdfMapper m : cm.getRdfMappers()) {
        Binder b = m.getBinder(session);
        if (m.isPredicateMap())
          pmap = (Map<String, List<String>>) b.getRawValue(instance, true);
        else if (b.isLoaded(instance)) {
          List<String> nv =
            !m.isAssociation() ? b.get(instance) : session.getIds(b.get(instance));
          vmap.put(m, nv);
        }
      }
    }

    public void delayedLoadComplete(Object o, RdfMapper m, Session session) throws OtmException {
      Binder b = m.getBinder(session);
      vmap.put(m, !m.isAssociation() ? b.get(o) : session.getIds(b.get(o)));
    }

    public <T> Collection<RdfMapper> update(T instance, ClassMetadata cm, Session session)
        throws OtmException {
      Collection<RdfMapper> rdfMappers = new ArrayList<RdfMapper>();
      boolean pmapChanged = false;

      for (RdfMapper m : cm.getRdfMappers()) {
        Binder b = m.getBinder(session);
        if (m.isPredicateMap()) {
          Map<String, List<String>> nv = (Map<String, List<String>>) b.getRawValue(instance, true);
          boolean                   eq = (pmap == null) ? (nv == null) : pmap.equals(nv);

          if (!eq) {
            pmap      = nv;
            pmapChanged = true;
          }
        } else if (b.isLoaded(instance)) {
          List<String> ov = vmap.get(m);
          List<String> nv =
            !m.isAssociation() ? b.get(instance) : session.getIds(b.get(instance));
          boolean      eq = (ov == null) ? (nv == null) : ov.equals(nv);

          if (!eq) {
            vmap.put(m, nv);
            rdfMappers.add(m);
          }
        }
      }

      if (pmapChanged)
        rdfMappers = cm.getRdfMappers(); // all fields since predicate-map is a wild-card

      return rdfMappers;
    }

    public BlobChange digestUpdate(Object instance, Binder blobField) throws OtmException {
      byte[] blob = (byte[])blobField.getRawValue(instance, false);
      int len = 0;
      byte[] digest = null;
      if (blob != null) {
        len = blob.length;
        try {
          digest = MessageDigest.getInstance("SHA-1").digest(blob);
        } catch (Exception e) {
          throw new OtmException("Failed to create a digest", e);
        }
      }
      BlobChange ret;
      if ((len == blobLen) && Arrays.equals(blobDigest, digest))
        ret = BlobChange.noChange;
      else if (blobDigest == null)
        ret = BlobChange.insert;
      else if (digest == null)
        ret = BlobChange.delete;
      else
        ret = BlobChange.update;

      blobLen = len;
      blobDigest = digest;

      return ret;
    }
  }

  /**
   * A weak reference to the object with identity hash code and instance equality.
   * This reference is used as the key for the cache so that when the application
   * stops refering the object, it can be removed from our cache also.
   */
  private static class ObjectReference<T> extends WeakReference<T> {
    private final int hash;

    public ObjectReference(T o) {
      super(o);
      hash = System.identityHashCode(o);
    }

    public ObjectReference(T o, ReferenceQueue<?super T> queue) {
      super(o, queue);
      hash = System.identityHashCode(o);
    }

    public boolean equals(Object o) {
      return (o instanceof ObjectReference)
              && ((o == this) || (get() == ((ObjectReference<T>) o).get()));
    }

    public int hashCode() {
      return hash;
    }
  }
}
