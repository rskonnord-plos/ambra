/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.AbstractStore;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A common base class for triple-store impls.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractTripleStore extends AbstractStore implements TripleStore {
  private static final Log log = LogFactory.getLog(AbstractTripleStore.class);
  /**
   * Map of Criterion Builders for store specific functions. 
   */
  protected Map<String, CriterionBuilder> critBuilders = new HashMap<String, CriterionBuilder>();

  /**
   * Instantiate an object based on the statements about it found in the triple-store
   *
   * @param session the session under which this object is instantiated
   * @param instance the instance to which values are to be set or null
   * @param cm the class metadata describing
   * @param id the object's id/subject-uri
   * @param fvalues p-o mapping with s being the id
   * @param rvalues p-s mapping with o being the id
   * @param types rdf:type look-ahead for o values (crosses rdf:List, rdf:Bag)
   *
   * @return the object instance
   *
   * @throws OtmException on an error
   */
  protected Object instantiate(Session session, Object instance, ClassMetadata cm, String id,
                              Map<String, List<String>> fvalues,
                              Map<String, List<String>> rvalues, Map<String, Set<String>> types)
                        throws OtmException {
    SessionFactory sf    = session.getSessionFactory();

    if (instance == null)
      instance = cm.getEntityBinder(session).newInstance();

    cm.getIdField().getBinder(session).set(instance, Collections.singletonList(id));

    // re-map values based on the rdf:type look ahead
    Map<RdfMapper, List<String>> mvalues = new HashMap();
    boolean                   inverse = false;

    for (Map<String, List<String>> values : new Map[] { fvalues, rvalues }) {
      for (String p : values.keySet()) {
        for (String o : values.get(p)) {
          RdfMapper m = cm.getMapperByUri(sf, p, inverse, types.get(o));

          if (m != null) {
            List<String> v = mvalues.get(m);

            if (v == null)
              mvalues.put(m, v = new ArrayList<String>());

            v.add(o);
          }
        }
      }

      inverse = true;
    }

    // now assign values to fields
    for (RdfMapper m : mvalues.keySet()) {
      Binder b = m.getBinder(session);
      b.load(instance, mvalues.get(m), types, m, session);
      if (log.isDebugEnabled() && !b.isLoaded(instance))
        log.debug("Lazy collection '" + m.getName() + "' created for " + id);
    }

    boolean found = false;

    for (RdfMapper m : cm.getRdfMappers()) {
      if (m.isPredicateMap()) {
        found = true;

        break;
      }
    }

    if (found) {
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      map.putAll(fvalues);

      for (RdfMapper m : cm.getRdfMappers()) {
        if (m.isPredicateMap())
          continue;

        if (m.hasInverseUri())
          continue;

        map.remove(m.getUri());
      }

      for (RdfMapper m : cm.getRdfMappers()) {
        if (m.isPredicateMap())
          m.getBinder(session).setRawValue(instance, map);
      }
    }

    return instance;
  }

  /*
   * inherited javadoc
   */
  public <T> void insert(ClassMetadata cm, String id, T o, Connection con) throws OtmException {
    insert(cm, cm.getRdfMappers(), id, o, con);
  }

  /*
   * inherited javadoc
   */
  public <T> void delete(ClassMetadata cm, String id, T o, Connection con) throws OtmException {
    delete(cm, cm.getRdfMappers(), id, o, con);
  }

  /*
   * inherited javadoc
   */
  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return critBuilders.get(func);
  }

  /*
   * inherited javadoc
   */
  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
    critBuilders.put(func, builder);
  }
}
