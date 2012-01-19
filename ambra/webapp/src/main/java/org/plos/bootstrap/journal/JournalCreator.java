/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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
package org.plos.bootstrap.journal;

import java.net.URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;
import org.plos.configuration.WebappItqlClientFactory;

import org.plos.models.Article;
import org.plos.models.DublinCore;
import org.plos.models.EditorialBoard;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.Volume;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.NECriterion;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.util.TransactionHelper;

/**
 * A listener class for creating journals on startup. This is equivalent of executing the
 * createJournal.groovy script. Note that the smartCollection rule is hard-coded to filter by
 * 'eIssn'.
 *
 * @author Pradeep Krishnan
 */
public class JournalCreator implements ServletContextListener {
  private static Log log = LogFactory.getLog(JournalCreator.class);

  /**
   * Shutdown things.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
  }

  /**
   * Initialize things.
   *
   * @param event init event
   */
  public void contextInitialized(ServletContextEvent event) {
    createJournals();
  }

  /**
   * Create all configured journals.
   *
   * @throws Error to abort
   */
  public void createJournals() {
    Session             sess = null;
    final Configuration conf = ConfigurationStore.getInstance().getConfiguration();

    try {
      URI            service =
        new URI(conf.getString("ambra.topaz.tripleStore.mulgara.itql.uri"));

      SessionFactory factory = new SessionFactoryImpl();
      factory.setTripleStore(new ItqlStore(service, WebappItqlClientFactory.getInstance()));
      factory.addModel(new ModelConfig("ri", URI.create(conf.getString("ambra.models.articles")),
                                       null));
      factory.addModel(new ModelConfig("profiles",
                                       URI.create(conf.getString("ambra.models.profiles")), null));
      factory.addModel(new ModelConfig("criteria",
                                       URI.create(conf.getString("ambra.models.criteria")), null));

      Configuration aliases = conf.subset("ambra.aliases");
      Iterator it           = aliases.getKeys();
      while (it.hasNext()) {
        String key = (String) it.next();

        if ((key.indexOf("[") >= 0) || (key.indexOf(".") >= 0))
          continue;

        factory.addAlias(key, aliases.getString(key));
      }

      factory.preload(DetachedCriteria.class);
      factory.preload(EQCriterion.class);
      factory.preload(NECriterion.class);
      factory.preload(EditorialBoard.class);
      factory.preload(Issue.class);
      factory.preload(Journal.class);
      factory.preload(Volume.class);
      factory.preload(Article.class);

      sess = factory.openSession();

      Integer count =
        TransactionHelper.doInTxE(sess,
                                  new TransactionHelper.ActionE<Integer, Exception>() {
            public Integer run(Transaction tx) throws Exception {
              return createJournals(tx.getSession(), conf);
            }
          });
    } catch (Exception e) {
      throw new Error("A journal creation operation failed. Aborting ...", e);
    } finally {
      try {
        if (sess != null)
          sess.close();
      } catch (Throwable t) {
        log.warn("Error closing session", t);
      }
    }
  }

  /**
   * Create all configured journals.
   *
   * @param sess the otm session to use
   * @param conf the configuration
   *
   * @return the number of journals created/updated
   *
   * @throws OtmException on an error
   */
  public int createJournals(Session sess, Configuration conf)
                     throws OtmException {
    List<String> keys = conf.getList("ambra.virtualJournals.journals");

    if ((keys == null) || (keys.size() == 0)) {
      log.info("No journals to create");

      return 0;
    }

    for (String key : keys)
      createJournal(sess, key, conf);

    return keys.size();
  }

  /**
   * Journal create the journal
   *
   * @param sess the otm session to use
   * @param key the journal key
   * @param conf the configuration
   *
   * @throws OtmException on an error
   * @throws Error on a fatal error
   */
  public void createJournal(Session sess, String key, Configuration conf)
                     throws OtmException {
    log.info("Attempting create/update journal with key '" + key + "'");

    String cKey;
    String eIssn = conf.getString(cKey = "ambra.virtualJournals." + key + ".eIssn");

    if (eIssn == null)
      throw new Error("Missing config entry '" + cKey + "'");

    String        title    = conf.getString(cKey = "ambra.virtualJournals." + key + ".description");

    List<Journal> journals =
      sess.createCriteria(Journal.class).add(Restrictions.eq("key", key)).list();
    Journal       journal;

    if (journals.size() != 0) {
      journal              = journals.get(0);
    } else {
      journal = new Journal();
      journal.setKey(key);
    }

    journal.setEIssn(eIssn);

    DublinCore dc = journal.getDublinCore();

    if (dc == null) {
      dc = new DublinCore();
      journal.setDublinCore(dc);
    }

    if (title != null)
      dc.setTitle(title);

    List<DetachedCriteria> rules = journal.getSmartCollectionRules();

    if ((rules == null) || (rules.size() == 0) || !containsEIssnRule(rules, eIssn)) {
      EQCriterion eqEIssn = (EQCriterion) Restrictions.eq("eIssn", eIssn);
      eqEIssn.setSerializedValue(eIssn);

      DetachedCriteria rule = new DetachedCriteria("Article");
      rule.add(eqEIssn);

      rules = new ArrayList<DetachedCriteria>();
      rules.add(rule);
      journal.setSmartCollectionRules(rules);
      log.info("Smart collection rule created for 'Article' with eIssn = " + eIssn);
    }

    sess.saveOrUpdate(journal);

    if (journals.size() != 0)
      log.info("Updated journal with key '" + key + "'");
    else
      log.info("Created journal with key '" + key + "'");
  }

  private boolean containsEIssnRule(List<DetachedCriteria> rules, String eIssn) {
    for (DetachedCriteria rule : rules) {
      if (!"Article".equals(rule.getAlias()))
        continue;

      List<Criterion> crits = rule.getCriterionList();

      if ((crits == null) || (crits.size() != 1))
        continue;

      Criterion crit = crits.get(0);

      if (!(crit instanceof EQCriterion))
        continue;

      EQCriterion eq = (EQCriterion) crit;

      if ("eIssn".equals(eq.getFieldName()) && eIssn.equals(eq.getValue()))
        return true;
    }

    return false;
  }
}
