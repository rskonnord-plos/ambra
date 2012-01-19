/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.topazproject.ambra.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.annotation.service.TopazAnnotationUtil;
import org.topazproject.ambra.cache.OtmInterceptor;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.Retraction;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.util.TransactionHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Create default citations for formal corrections and retractions based on article citation.
 *
 * @author Dragisa Krsmanovic
 */
public class AnnotationCitationMigrator implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(AnnotationCitationMigrator.class);
  private SessionFactory sf;
  private OtmInterceptor oi;
  private int txnTimeout = 600;
  private boolean background = true;
  private Set<String> errorSet = new HashSet<String>();

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  @Required
  public void setOtmInterceptor(OtmInterceptor oi) {
    this.oi = oi;
  }

  /**
   * The txn time out value to set. Defaults to 10min.
   *
   * @param txnTimeout the timeout value to set
   */
  public void setTxnTimeout(int txnTimeout) {
    this.txnTimeout = txnTimeout;
  }
  /**
   * Whether to run this migration in back-ground allowing web-traffic to proceed.
   *
   * @param val the background flag value
   */
  public void setBackground(boolean val) {
    background = val;
  }

  public void init() {
    if (!background)
      migrate();
    else {
      Thread t = new Thread(this, "Annotation-Citation-Migrator");
      t.setPriority(Thread.NORM_PRIORITY - 1);
      t.setDaemon(true);
      t.start();
    }
  }

  public void run() {
    try {
      migrate();
    } catch (RuntimeException e) {
      log.error("Uncaught exception. Exiting ...", e);
    } catch (Error e) {
      log.error("Uncaught error. Exiting ...", e);
    }
  }

  /**
   * Run thru all the migrations.
   *
   * @return the count of articles successfully migrated
   */
  public long migrate() {
    log.info("Annotation Citation Migrations starting ...");
    long timestamp = System.currentTimeMillis();

    Session session = sf.openSession(oi);

    try {
      long total = 0;

      int count;
      do {
        int err = errorSet.size();
        count = migrate(session, txnTimeout);
        total += (count - (errorSet.size() - err));
      } while (count > 0);

      if (errorSet.size() > 0)
        log.error("Failed to migrateFormalCorrections " + errorSet.size() + " annotation citations. " +
            "Succeeded for " + total + " citations. Elapsed time: " +
            (System.currentTimeMillis()-timestamp)/1000 + "sec");
      else if (total == 0)
        log.info("Nothing to do. Annotation citations are all migrated.");
      else
        log.warn("Successfully migrated " + total + " annotation citations. Elapsed time: " +
            (System.currentTimeMillis()-timestamp)/1000 + "sec");

      return total;
    } finally {
      try {
        session.close();
      } catch (Exception e) {
        log.warn("Failed to close session", e);
      }
    }
  }

  private int migrate(Session session, int timeout) {
    return TransactionHelper.doInTx(session, false, timeout, new TransactionHelper.Action<Integer>() {
      public Integer run(Transaction tx) {
        Session session1 = tx.getSession();
        return migrateFormalCorrections(session1)
             + migrateRetractions(session1);
      }
    });
  }


  private int migrateFormalCorrections(Session session) {

    Results results =  session.createQuery("select f, a.dublinCore.bibliographicCitation " +
        "from FormalCorrection f, Article a where f.annotates = a;")
        .execute();

    int count = 0;
    while (results.next()) {
      FormalCorrection correction = (FormalCorrection)results.get(0);
      if (correction.getBibliographicCitation() == null) {
        Citation articleCitation = (Citation)results.get(1);
        String id = correction.getId().toString();
        if (errorSet.contains(id))
          continue;
        try {
          log.info("Migrating citation for " + id);
          TopazAnnotationUtil.createDefaultCitation(correction, articleCitation, session);
          session.flush();
          count++;
        } catch (Exception e) {
          log.error("Failed to migrate formal correction <" + id + ">.", e);
          errorSet.add(id);
          count++;
        } finally {
          session.clear();
        }
      }
    }

    return count;
  }

  private int migrateRetractions(Session session) {

    Results results = session.createQuery("select r, a.dublinCore.bibliographicCitation " +
        "from Retraction r, Article a where r.annotates = a;")
        .execute();

    int count = 0;
    while (results.next()) {
      Retraction retraction = (Retraction)results.get(0);
      Citation articleCitation = (Citation)results.get(1);
      if (retraction.getBibliographicCitation() == null) {
        String id = retraction.getId().toString();
        if (errorSet.contains(id))
          continue;
        try {
          log.info("Migrating citation for " + id);
          TopazAnnotationUtil.createDefaultCitation(retraction, articleCitation, session);
          session.flush();
          count++;
        } catch (Exception e) {
          log.error("Failed to migrate retraction <" + id + ">.", e);
          errorSet.add(id);
          count++;
        } finally {
          session.clear();
        }
      }
    }

    return count;
  }


}
