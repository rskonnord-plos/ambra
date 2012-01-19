/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.owl;

import java.net.URI;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.OtmException;

/**
 * Utility to add otm classes to #metadata in owl form.
 *
 * @author Eric Brown
 */
public class OwlHelper {
  private static final Log log = LogFactory.getLog(OwlHelper.class);

  public static final URI HAS_ALIAS_PRED     = URI.create(Rdf.topaz + "hasAlias");

  /**
   * Traverse MetaClassdata stored in the supplied SessionFactory and store that in
   * #metadata in owl form.
   *
   * @param factory where MetaClassdata exists
   * @param mc Model where owl metadata is to be stored
   */
  public static void addFactory(SessionFactory factory, ModelConfig mc) throws OtmException {
    SessionFactory metaFactory = createMetaSessionFactory(factory, mc);

    Session session = metaFactory.openSession();
    Transaction tx = null;

    try {
      tx = session.beginTransaction();

      createRdfAliases(session);

      // Loop over all classes in factory
      for (ClassMetadata cm: factory.listClassMetadata()) {
        String type = cm.getType();
        OwlClass oc = null;

        // Ignore anonymous classes
        if (type != null) {
          // Create OwlClass
          oc = new OwlClass();
          oc.setOwlClass(URI.create(type));

          // Add sub-classes
          for (String t: cm.getTypes())
            if (t != type)
              oc.addSuperClass(URI.create(t));

          // Set model
          if (cm.getModel() != null) {
             ModelConfig c = factory.getModel(cm.getModel());
             if (c != null)
               oc.setModel(c.getUri());
          }

          session.saveOrUpdate(oc);
        }

        // Build a list of super-classes (for use below)
        List<Class> superClasses = new LinkedList<Class>();
        Class clazz = cm.getSourceClass();
        while (clazz != Object.class) {
          clazz = clazz.getSuperclass();
          if (clazz != Object.class)
            superClasses.add(0, clazz);
        }

        // Now let's iterate over the fields
        for (Mapper m: cm.getFields()) {
          // See if this field really belongs to our class/type
          Field f = m.getField();
          clazz = f.getDeclaringClass();
          if (clazz != cm.getSourceClass()) {
            /* Now, we need to walk up the chain until we find a non-anonymous class that
             * does have metadata. If that class is not us, that we can continue
             */
            boolean bFoundDeclaringClass = false;
            boolean bFoundOwlClass = false;
            for (Class c: superClasses) {
              // Don't do anything until we find the declaring class
              if (!bFoundDeclaringClass) {
                if (c == clazz)
                  bFoundDeclaringClass = true;
                else
                  continue;
              }

              ClassMetadata fCm = factory.getClassMetadata(c);
              if (fCm != null && fCm.getType() != null) {
                bFoundOwlClass = true;
                break;
              }
            }

            // We found an owl class that defines this property, so don't redefine it below
            if (bFoundOwlClass)
              continue;
          }

          if (m.getUri() == null)
            continue;

          // See if we've already created this property
          ObjectProperty op = session.get(ObjectProperty.class, m.getUri());
          if (op == null)
            op = new ObjectProperty();

          // Set the property name
          op.setProperty(URI.create(m.getUri()));

          // Add to
          if (oc != null) {
            DomainUnion union = op.getDomains();
            if (union == null)
              union = new DomainUnion();
            union.id = URI.create(m.getUri().toString() + "Union"); // fake id for now
            union.domains.add(oc);

            if (m.getDataType() != null)
              op.setRanges(new URI[] { URI.create(m.getDataType()) });
            else {
              ClassMetadata cm2 = factory.getClassMetadata(m.getComponentType());
              if ((cm2 != null) && (cm2.getType() != null))
                op.setRanges(new URI[] { URI.create(cm2.getType()) });
            }

            op.setDomains(union);
          }

          session.saveOrUpdate(op);
        }
      }

      tx.commit();
      tx = null;
    } finally {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (OtmException oe) {
          log.warn("Rollback failed", oe);
        }
      }

      try {
        session.close();
      } catch (OtmException oe) {
        log.warn("Closing session failed", oe);
      }
    }
  }

  /**
   * Create topaz:hasAlias entries into #metadata.
   *
   * TODO: Update this when this info is available in the SessionFactory or ClassMetadata
   */
  private static void createRdfAliases(Session session) throws OtmException {
    for (Field f: Rdf.class.getFields()) {
      if (Modifier.isStatic(f.getModifiers())) {
        try {
          Alias a = session.get(Alias.class, f.get(Rdf.class).toString());
          if (a == null)
            a = new Alias();

          a.setPrefix(URI.create(f.get(Rdf.class).toString()));
          a.addAlias(f.getName());
          session.saveOrUpdate(a);
        } catch (IllegalAccessException iae) {
          log.debug("Unable to create alias for field 'Rdf." + f.getName() + "'", iae);
        }
      }
    }
  }

  /**
   * Setup our model and session. DON'T forget to close the session!
   */
  private static SessionFactory createMetaSessionFactory(SessionFactory factory, ModelConfig mc)
      throws OtmException {
    SessionFactory metaFactory = new SessionFactory();
    metaFactory.setTripleStore(factory.getTripleStore());

    metaFactory.addModel(mc);
    metaFactory.getTripleStore().createModel(mc);

    metaFactory.preload(OwlClass.class);
    metaFactory.preload(ObjectProperty.class);
    metaFactory.preload(Alias.class);
    metaFactory.preload(DomainUnion.class);

    return metaFactory;
  }
}
