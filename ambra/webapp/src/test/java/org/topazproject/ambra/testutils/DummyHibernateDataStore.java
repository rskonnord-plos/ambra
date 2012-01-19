/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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

package org.topazproject.ambra.testutils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.DublinCore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link DummyDataStore} using a hibernate session factory to create sessions and store data.  This
 * should be autowired with the same context as the tests; see HibernateServiceBeanContext.xml
 *
 * @author Alex Kudlick Date: 5/2/11
 *         <p/>
 *         org.topazproject.ambra
 */
public class DummyHibernateDataStore implements DummyDataStore {

  private SessionFactory sessionFactory;
  private Set<String> storedIds = new HashSet<String>();
  private Set<String> storedDublinCoreIdentifiers = new HashSet<String>(); //DCs have the same identifier as their article

  @Required
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public String store(Object object) {
    Session session = sessionFactory.openSession();
    String id = getId(object, session);
    if (objectHasBeenStored(object, id)) {
      return id;
    } else {
      Transaction transaction = session.beginTransaction();
      if (object instanceof Article && !objectHasBeenStored(((Article) object).getDublinCore(),id)) {
        //store a dummy dublin core first if it's an article
        if (((Article) object).getDublinCore() == null) {
          ((Article) object).setDublinCore(new DublinCore());
        }
        ((Article) object).getDublinCore().setIdentifier(((Article) object).getId().toString());
        store(((Article) object).getDublinCore());
      }
      String generatedId = session.save(object).toString();
      transaction.commit();
      session.flush();
      session.close();
      storeId(object, generatedId);
      return generatedId;
    }
  }

  @Override
  public <T> List<String> store(List<T> objects) {
    List<String> ids = new ArrayList<String>(objects.size());
    Session session = sessionFactory.openSession();
    Transaction transaction = session.beginTransaction();
    for (T object : objects) {
      String id = getId(object, session);
      if (id != null && storedIds.contains(id)) {
        ids.add(id);
      } else {
        String generatedId = session.save(object).toString();
        ids.add(generatedId);
        storeId(object, generatedId);
      }
    }
    transaction.commit();
    session.flush();
    session.close();
    return ids;
  }

  @Override
  public void update(Object object) {
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    session.update(object);
    session.getTransaction().commit();
    session.flush();
    session.close();
  }

  private boolean objectHasBeenStored(Object object, String id) {
    if (id != null) {
      if (object instanceof DublinCore && storedDublinCoreIdentifiers.contains(id)) {
        return true;
      } else if (!(object instanceof DublinCore) && storedIds.contains(id)) {
        return true;
      }
    }
    return false;
  }

  private String getId(Object object, Session session) {
    String idField = session
        .getSessionFactory()
        .getClassMetadata(object.getClass())
        .getIdentifierPropertyName();

    String getter = "get" + idField.substring(0, 1).toUpperCase() + idField.substring(1);
    try {
      return object.getClass().getMethod(getter).invoke(object).toString();
    } catch (Exception e) {
      return null;
    }
  }

  private void storeId(Object object, String generatedId) {
    if (object instanceof DublinCore) {
      storedDublinCoreIdentifiers.add(generatedId);
    } else {
      storedIds.add(generatedId);
    }
  }
}
