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
package org.ambraproject.journal;

import org.ambraproject.ApplicationException;
import org.ambraproject.models.Journal;
import org.ambraproject.service.HibernateServiceImpl;
import org.apache.commons.configuration.Configuration;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * A listener class for creating journals on startup. This is equivalent of executing the createJournal.groovy script.
 *
 * @author Joe Osowski
 */
public class JournalCreatorImpl extends HibernateServiceImpl implements JournalCreator {
  private static Logger log = LoggerFactory.getLogger(JournalCreatorImpl.class);

  private Configuration configuration;

  /**
   * Create all configured journals.
   *
   * @throws Error to abort
   */
  @Override
  @SuppressWarnings("unchecked")
  public void createJournals() throws ApplicationException {
    List<String> keys = configuration.getList(JOURNAL_CONFIG_KEY);

    if ((keys == null) || (keys.size() == 0)) {
      log.info("No journals to create");
      return;
    }

    for (final String key : keys) {
        log.debug("Attempting create/update journal with key '" + key + "'");

        String configurationKey = "ambra.virtualJournals." + key + ".eIssn";
        String eIssn = configuration.getString(configurationKey);

        if (eIssn == null) {
          throw new ApplicationException("Missing config entry '" + configurationKey + "'");
        }

        String title = configuration.getString("ambra.virtualJournals." + key + ".description");

      try {
        List<Journal> journals = hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(Journal.class)
                .add(Restrictions.eq("journalKey", key)));

        Journal journal;

        if (journals.size() != 0) {
          //journal already exists
          journal = journals.get(0);
        } else {
          //need to make a journal
          journal = new Journal();
          journal.setJournalKey(key);
        }

        if (title != null) {
          //don't want to update a title to null if the journal already exists and we lost the config element
          journal.setTitle(title);
        }
        journal.seteIssn(eIssn);

        //Generate the journal Id first so the dublin core can have a matching identifier
        hibernateTemplate.saveOrUpdate(journal);

        if (journals.size() != 0) {
          log.info("Updated journal with key '" + key + "'");
        } else {
          log.info("Created journal with key '" + key + "'");
        }

      } catch (Exception e) {
        throw new ApplicationException("Failed to create/update journal with key: " + key, e);
      }
    }
  }

  /**
   * Setter method for configuration. Injected through Spring.
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
