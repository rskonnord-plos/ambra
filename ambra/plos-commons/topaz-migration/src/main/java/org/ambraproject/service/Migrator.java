/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.service;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.models.*;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Query;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.query.Results;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service Bean for migrating objects from Topaz.
 *
 * Migrate objects to mysql
 *
 * @author Alex Kudlick
 * @author Joe Osowski
 *
 * org.ambraproject.service
 */
public class Migrator {
  private static Logger log = LoggerFactory.getLogger(Migrator.class);
  private Map<String, Object> queryParameters = new HashMap<String, Object>();

  private int recordsPerCommit = 6;
  private int errorCount = 0;
  private int numThreads = 1;
  private int currentOffset = 0;
  private boolean endFound = false;
  private int lastRecord = 0;
  private int setSize = 50;

  private Class entityClass;
  private String query;

  private MySQLService mysql;
  private TopazService topaz;

  private static final Set<Class> entitiesWithStringId = new HashSet<Class>();

  static{
    entitiesWithStringId.add(AnnotationBlob.class);
    entitiesWithStringId.add(DublinCore.class);
    entitiesWithStringId.add(RatingContent.class);
    entitiesWithStringId.add(RatingSummaryContent.class);
    entitiesWithStringId.add(ReplyBlob.class);
    entitiesWithStringId.add(TrackbackContent.class);
    entitiesWithStringId.add(Representation.class);
    //entitiesWithStringId.add(Journal.class);
    //entitiesWithStringId.add(Volume.class);
    //entitiesWithStringId.add(Issue.class);
  }

  public Migrator(MySQLService mysql, TopazService topaz) {
    this.mysql = mysql;
    this.topaz = topaz;
  }


  public static Migrator create(MySQLService mysql, TopazService topaz) {
    return new Migrator(mysql, topaz);
  }


  /**
   * Set the class to lookup
   *
   * @param entityClass - the class to migrate
   * @return - this
   */
  public Migrator forClass(Class entityClass) {
    this.entityClass = entityClass;
    return this;
  }

  public Migrator threads(int numThreads) {
    this.numThreads = numThreads;
    return this;
  }

  public Migrator recordsPerCommit(int recordsPerCommit) {
    this.recordsPerCommit = recordsPerCommit;
    return this;
  }

  /**
   * Set the # of records for each thread to process.
   * The larger this number, the more memory will be eaten by thread
   * @param recordsPerRequest
   * @return
   */
  public Migrator recordsPerRequest(int recordsPerRequest) {
    this.setSize = recordsPerRequest;
    return this;
  }

  /**
   * Create a new query for ids of objects to migrate
   *
   * @param query - a Topaz OQL string of the query to run
   * @return - this
   */
  public Migrator createQuery(String query) {
    this.query = query;
    this.queryParameters = new HashMap<String, Object>();
    return this;
  }

  /**
   * Add a parameter to the query stored here.  The query must've already been set.
   *
   * @param name  - the parameter name in the query
   * @param value - the value of the parameter
   * @return - this
   */
  public Migrator addQueryParameter(String name, Object value) {
    this.queryParameters.put(name, value);
    return this;
  }

  /**
   * Set the query to migrate all items for the class.
   *
   * @param entityClass - the class to lookup
   * @return - this
   */
  public Migrator queryAll(Class entityClass) {
    this.entityClass = entityClass;
    return createQuery("select c.id id from " + entityClass.getSimpleName() + " c");
  }

  /**
   * Main utility method for this class. Migrate all objects whose ids are returned by the query.
   * <p/>
   * Supports objects that have either URI ids or String ids
   *
   * @return A total count of errors.
   */
  public int migrate() {
    return migrate(entitiesWithStringId.contains(entityClass));
  }

  private synchronized void incrementErrorCount()
  {
    errorCount++;
  }

  private int getOffset()
  {
    return currentOffset;
  }

  private synchronized void setOffset(int offset)
  {
    this.currentOffset = offset;
  }

  private boolean isDone()
  {
    return endFound;
  }

  private synchronized void setDoneFlag()
  {
    this.endFound = true;
  }

  //It's possible this property was set by more then one thread
  //the value it returns is an estimate (Which is likely very close)
  private int getLastRecord()
  {
    return lastRecord;
  }

  private synchronized void setLastRecord(int lastRecord)
  {
    this.lastRecord = lastRecord;
  }

  private int migrate(final boolean stringIds) {
    log.info("Starting " + this.numThreads + " thread(s) for " + entityClass.getSimpleName());

    this.endFound = false;
    this.currentOffset = 0;
    this.errorCount = 0;

    //If the setsize is larger then the configured record limit, set them to be the same
    if(topaz.getRecordLimit() > 0 && this.setSize > topaz.getRecordLimit()) {
      this.setSize = topaz.getRecordLimit();
    }

    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(this.numThreads);
    ThreadPoolExecutor es = new ThreadPoolExecutor(this.numThreads, this.numThreads, 10, TimeUnit.SECONDS, queue);

    while(isDone() == false) {
      //Make sure job queue is full until no more records are found
      if(es.getActiveCount() < this.numThreads)
      {
        es.execute(newThread(stringIds, this.getOffset(), this.setSize));
        this.setOffset(this.getOffset() + this.setSize);
      }

      if(topaz.getRecordLimit() > 0 && this.getOffset() >= topaz.getRecordLimit()) {
        log.debug("Configured Limit of " + topaz.getRecordLimit() + " reached.  Waiting for remaining threads to finish.");
        setDoneFlag();
      }

      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if(topaz.getRecordLimit() < 0) {
      log.debug("Found last record (estimated): " + this.getLastRecord() + ", waiting for remaining threads to finish.");
    }

    //One of the threads found the end, wait here until all threads complete.
    while(es.getActiveCount() > 0)
    {
      try {
        Thread.sleep(1000);
        log.debug("Active Threads: " + es.getActiveCount());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    es.shutdown();

    log.info("All threads for " + entityClass.getSimpleName() + " complete with " + this.errorCount + " error(s).");

    return this.errorCount;
  }

  private Thread newThread(final boolean stringIds, final int offset, final int limit) {
    String threadName = entityClass.getSimpleName() + " Migrator (" + offset + "-" + (offset + limit) + ")";

    log.debug("Thread " + threadName + " starting up.");

    return new Thread(threadName)
    {
      @Override
      public void run() {
        List<Serializable> ids = getObjectIDs(offset, limit);

        if (ids.size() < setSize) {
          //The set returned is less then the setsize, we must be at the end
          log.debug("Thread " + this.getName() + " found end.");
          setLastRecord(getOffset() + ids.size());
          setDoneFlag();
        }

        log.debug("Thread " + this.getName() + " running for " + ids.size() + " items.");

        Session topazSession = topaz.openSession();
        org.hibernate.Session hibernateSession = mysql.openSession();

        for (int i = 0; i < ids.size(); i += recordsPerCommit) {
          org.hibernate.Transaction hibernateTransaction = hibernateSession.beginTransaction();
          Transaction topazTransaction = null;

          try {
            //Topaz gets put into a strange state... sometimes.  This try / catch block should get around
            //the problem
            topazTransaction = topaz.beginTransaction(topazSession);
          } catch(Exception e) {
            try {
              topazSession.close();
            } catch (Exception e1) {};

            topazSession = topaz.openSession();
            topazTransaction = topazSession.beginTransaction();
          }

          int setMax = (i + recordsPerCommit) > ids.size() ? ids.size() : (i + recordsPerCommit);
          int setSize = setMax - i;

          for (int j = i; j < setMax; j++) {
            //Id could be a string or a URI
            Serializable id = stringIds ? ids.get(j).toString() : ids.get(j);

            try {
              if (hibernateSession.get(entityClass, id) == null) {
                log.debug("Migrating Object ID: " + id);
                Object obj = null;

                try {
                  //I've seen some behavior that throws connection closed errors on occasion.
                  //If that occurs, let's just try again.
                  obj = loadTopazObject(id, topazSession, entityClass);
                } catch (OtmException ex) {
                  try {
                    topazSession.close();
                  } catch (Exception e1) {};

                  topazSession = topaz.openSession();
                  topazTransaction = topazSession.beginTransaction();

                  obj = loadTopazObject(id, topazSession, entityClass);
                }

                //TODO: Confirm why topaz would return an ID for an object that does not load
                if(obj != null) {
                  //Merge article classes only, they appear to be the only class that needs it.
                  if(Article.class.equals(obj.getClass())) {
                    obj = hibernateSession.merge(obj);
                  }

                  if(Comment.class.equals(obj.getClass()) || Citation.class.equals(obj.getClass())) {
                    obj = hibernateSession.merge(obj);
                  }

                  hibernateSession.save(entityClass.getName(), obj);
                } else {
                  log.warn("Class: " + entityClass.getName() + " with ID: " + id + " not found.");
                }
              } else {
                log.debug("Object ID: " + id + " already migrated, ignoring.");
                //Merge is causing changes in some topaz objects that topaz is trying to commit
                //Being this is a data migration, we don't have to worry about the object's state being changed
                //over time, if the object exists in the database, we can be sure that it is the same.
                //hibernateSession.merge(entityClass.getName(), loadTopazObject(id, topazSession, entityClass));
              }
            } catch (Exception e) {
              log.error("Error migrating " + entityClass.getSimpleName() + ": " + id, e);
              incrementErrorCount();
            }
          }

          try {
            log.info("Committing " + setSize + " record(s) for " + this.getName());

            hibernateTransaction.commit();
            topazTransaction.rollback();

            log.info("Successfully committed " + setSize + " record(s) for " + this.getName());
          } catch (Exception e) {
            log.error("Error committing transaction; continuing with the rest of " + entityClass.getSimpleName() + "s", e);
            incrementErrorCount();
          }
        }

        topazSession.close();
        hibernateSession.close();

        log.debug("Thread " + this.getName() + " shutting down.");
      }
    };
  }

  /**
   * Run the query for ids, and return a list of the results.
   * @return - a list of the ids found by the query in this object
   */
  private List<Serializable> getObjectIDs(int offset, int limit) {
    List<Serializable> ids = new ArrayList<Serializable>();
    Session topazSession = topaz.openSession();

    String queryText = this.query + " limit " + limit + " offset " + offset + ";";
    Query topazQuery = topazSession.createQuery(queryText);

    log.debug("Running Topaz Query: " + queryText);

    for (String name : queryParameters.keySet()) {
      topazQuery.setParameter(name, queryParameters.get(name));
    }

    Transaction topazTransaction = topazSession.beginTransaction();

    try {
      Results results = topazQuery.execute();

      while (results.next()) {
        ids.add((URI) results.get(0));
      }

      results.close();
      topazTransaction.rollback();
      topazSession.close();
    } catch (OtmException e) {
      throw new RuntimeException("Error querying for " + entityClass.getSimpleName() + " ids", e);
    }

    log.debug("Topaz Query Complete.");
    //Finish looking for ids
    return ids;
  }

  @SuppressWarnings("unchecked")
  private Object loadTopazObject(Serializable id, Session topazSession, Class entityClass) throws Exception {
    List list = topazSession
        .createCriteria(entityClass)
        .add(Restrictions.eq("id", id)).list();

    if(list.size() == 0) {
      log.debug("Not able to find " + entityClass.toString() + " id:" + id);
      //Throw an exception here.  Higher up the chain this will be caught and a retry attempted
      throw new OtmException("Not able to find " + entityClass.toString() + " id:" + id);
    } else {
      Object entity = list.get(0);
      Object clone = null;

      log.debug("Loading and cloning Topaz Object: " + entityClass.getSimpleName() + " id:" + id);

      //clone so we don't lazy load later on
      try {
        clone = CloningUtil.clone(entity, entityClass);
      } catch (IllegalArgumentException e) {
        clone = BeanUtils.cloneBean(entity);
      }

      log.debug("Loaded Topaz Object: " + entityClass.getSimpleName());
      return clone;
    }
  }
}

