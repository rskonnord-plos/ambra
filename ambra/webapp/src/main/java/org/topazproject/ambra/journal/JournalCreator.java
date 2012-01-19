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
package org.topazproject.ambra.journal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Journal;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

/**
 * A listener class for creating journals on startup. This is equivalent of executing the
 * createJournal.groovy script. Note that the smartCollection rule is hard-coded to filter by
 * 'eIssn'.
 *
 * @author Pradeep Krishnan
 */
public class JournalCreator {
  private static Logger log = LoggerFactory.getLogger(JournalCreator.class);

  private SessionFactory sf;
  private Configuration configuration;

  /**
   * Create all configured journals.
   *
   * @throws Error to abort
   */
  public void createJournals() {

    try {
      TransactionHelper.doInTxE(sf, new TransactionHelper.ActionE<Integer, OtmException>() {
        public Integer run(Transaction tx) throws OtmException {
          return createJournals(tx.getSession());
        }
      });
    } catch (Exception e) {
      throw new Error("A journal creation operation failed. Aborting ...", e);
    }
  }

  /**
   * Create all configured journals.
   *
   * @param sess the otm session to use
   * @return the number of journals created/updated
   *
   * @throws OtmException on an error
   */
  @SuppressWarnings("unchecked")
  private int createJournals(Session sess)
                     throws OtmException {
    List<String> keys = configuration.getList("ambra.virtualJournals.journals");

    if ((keys == null) || (keys.size() == 0)) {
      log.info("No journals to create");

      return 0;
    }

    for (String key : keys)
      createJournal(sess, key);

    return keys.size();
  }

  /**
   * Journal create the journal
   *
   * @param sess the otm session to use
   * @param key the journal key
   * @throws OtmException on an error
   * @throws Error on a fatal error
   */
  @SuppressWarnings("unchecked")
  private void createJournal(Session sess, String key) throws OtmException {
    log.info("Attempting create/update journal with key '" + key + "'");

    String cKey = "ambra.virtualJournals." + key + ".eIssn";
    String eIssn = configuration.getString(cKey);

    if (eIssn == null)
      throw new Error("Missing config entry '" + cKey + "'");

    String title = configuration.getString("ambra.virtualJournals." + key + ".description");

    List<Journal> journals =
      sess.createCriteria(Journal.class).add(Restrictions.eq("key", key)).list();
    Journal journal;

    if (journals.size() != 0) {
      journal              = journals.get(0);
    } else {
      journal = new Journal();
      journal.setKey(key);
    }

    journal.seteIssn(eIssn);

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

  @Required
  public void setOtmSessionFactory(SessionFactory sf) {
    this.sf = sf;
  }

  /**
   * Setter method for configuration. Injected through Spring.
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
