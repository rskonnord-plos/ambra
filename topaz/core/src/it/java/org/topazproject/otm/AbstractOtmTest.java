/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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
package org.topazproject.otm;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.mulgara.itql.DefaultItqlClientFactory;

import org.topazproject.otm.impl.SessionFactoryImpl;

import org.topazproject.otm.samples.Annotea.Body;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;

/**
 * Base class for all integration tests done using the samples classes.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractOtmTest {
  private static final Log log = LogFactory.getLog(AbstractOtmTest.class);

  /**
   * Used to ensure that all test classes get different graphs so as to avoid
   * conflicts when tests from different classes are interleaved by testng.
   */
  private static int graphCnt = 1;

  /**
   * Shared session factory
   */
  protected SessionFactory factory;

  protected void initFactory() throws OtmException {

    factory = new SessionFactoryImpl();

    log.info("initializing otm session factory ...");
    GraphConfig[] graphs = new GraphConfig[] {
      new GraphConfig("ri", URI.create("local:///topazproject#otmtest" + graphCnt++), null),
      new GraphConfig("grants", URI.create("local:///topazproject#otmtest" + graphCnt++), null),
      new GraphConfig("revokes", URI.create("local:///topazproject#otmtest" + graphCnt++), null),
      new GraphConfig("criteria", URI.create("local:///topazproject#otmtest" + graphCnt++), null),
      new GraphConfig("str", URI.create("local:///topazproject#str"),
                                      URI.create("http://topazproject.org/graphs#StringCompare")),
      new GraphConfig("prefix", URI.create("local:///topazproject#prefix"),
                                      URI.create("http://mulgara.org/mulgara#PrefixGraph")),
    };
    URI storeUri = URI.create("local:///topazproject");
    DefaultItqlClientFactory cf = new DefaultItqlClientFactory();
    cf.setDbDir("target/mulgara-db");
    TripleStore tripleStore = new ItqlStore(storeUri, cf);
    factory.setTripleStore(tripleStore);
    factory.setBlobStore(new SimpleBlobStore("target/blob-store"));

    for (GraphConfig graph : graphs)
      factory.addGraph(graph);

    Session session = null;
    Transaction txn = null;
    try {
      session = factory.openSession();
      txn = session.beginTransaction();
      session.createGraph("str");
      session.createGraph("prefix");
      txn.commit();
    } catch (OtmException e) {
      if (txn != null) txn.rollback();
      throw e;
    } finally {
      if (session != null) session.close();
    }

    factory.preloadFromClasspath();
    factory.preload(Body.class);

    factory.validate();
  }

  protected void initGraphs() throws OtmException {
    log.info("initializing mulgara test graphs ...");
    String names[] = new String[] {"ri", "grants", "revokes", "criteria"};

    Session s = null;
    Transaction txn = null;
    try {
      s = factory.openSession();
      for (String name : names) {
        GraphConfig graph = factory.getGraph(name);
        txn = s.beginTransaction();
        try {
          s.dropGraph(graph.getId());
          txn.commit();
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("Failed to drop graph '" + name + "'", t);
          txn.rollback();
        }
        txn = null;
        txn = s.beginTransaction();
        s.createGraph(graph.getId());
        txn.commit();
      }
    } catch (OtmException e) {
      if (txn != null) txn.rollback();
      throw e;
    } finally {
      if (s != null) s.close();
    }
  }

 /**
   * Run the given action within a session.
   *
   * @param action the action to run
   * @return the value returned by the action
   */
  protected void doInSession(Action action) throws OtmException {
    doInSession(false, action);
  }

  protected void doInReadOnlySession(Action action) throws OtmException {
    doInSession(true, action);
  }

  protected void doInSession(boolean readOnly, Action action) throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction(readOnly, -1);
      action.run(session);
      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

  /**
   * The interface actions must implement.
   */
  protected static interface Action {
    /**
     * This is run within the context of a session.
     *
     * @param session the current transaction
     */
    void run(Session session) throws OtmException;
  }

}
