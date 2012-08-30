/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.journal;

import org.ambraproject.ApplicationException;
import org.ambraproject.action.BaseTest;
import org.ambraproject.models.Journal;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 4/17/12
 */
public class JournalCreatorTest extends BaseTest {

  @Autowired
  protected JournalCreator journalCreator;

  @Autowired
  protected Configuration configuration;


  @Test
  @SuppressWarnings("unchecked")
  public void testCreateJournals() throws ApplicationException {
    //there should be journals configured, otherwise we'll have nothing to do
    List<String> configuredJournalKeys = configuration.getList(JournalCreator.JOURNAL_CONFIG_KEY);
    assertTrue(configuredJournalKeys != null && !configuredJournalKeys.isEmpty(),
        "No Journals configured in the test config");

    //delete any existing journals so we can create them
    dummyDataStore.deleteAll(Journal.class);

    journalCreator.createJournals();

    List<Journal> storedJournals = dummyDataStore.getAll(Journal.class);
    assertEquals(storedJournals.size(), configuredJournalKeys.size(), "created incorrect number of journals");

    for (String journalKey : configuredJournalKeys) {
      Journal matchingJournal = null;
      for (Journal journal : storedJournals) {
        if (journal.getJournalKey().equals(journalKey)) {
          matchingJournal = journal;
          break;
        }
      }
      assertNotNull(matchingJournal, "didn't create a journal for key: " + journalKey);
      assertEquals(matchingJournal.geteIssn(), configuration.getString("ambra.virtualJournals." + journalKey + ".eIssn"),
          "Journal " + journalKey + " had incorrect eIssn");

      assertEquals(matchingJournal.getTitle(), configuration.getString("ambra.virtualJournals." + journalKey + ".description"),
          "Journal " + journalKey + " had incorrect title");
    }
  }

  @Test(dependsOnMethods = {"testCreateJournals"})
  public void testUpdateJournals() throws ApplicationException {
    //Journals will have been created now, let's change properties and update them

    List<Journal> storedJournals = dummyDataStore.getAll(Journal.class);
    for (Journal journal : storedJournals) {
      configuration.setProperty("ambra.virtualJournals." + journal.getJournalKey() + ".eIssn", journal.geteIssn() + "-new");
      configuration.setProperty("ambra.virtualJournals." + journal.getJournalKey() + ".description", journal.getTitle() + "-new");
    }

    journalCreator.createJournals();
    storedJournals = dummyDataStore.getAll(Journal.class);
    assertEquals(storedJournals.size(), configuration.getList(JournalCreator.JOURNAL_CONFIG_KEY).size(),
        "added/removed a journal when we should have only updated them");
    for (Journal journal : storedJournals) {
      assertTrue(journal.geteIssn().endsWith("-new"), "Didn't update eIssn for journal " + journal.getJournalKey());
      assertTrue(journal.getTitle().endsWith("-new"), "Didn't update title for journal " + journal.getJournalKey());
    }
  }
  @AfterClass
  public void revertJournalChanges() {
    for (Journal journal : dummyDataStore.getAll(Journal.class)) {
      journal.setTitle(journal.getTitle().replaceAll("-new", ""));
      journal.seteIssn(journal.geteIssn().replaceAll("-new", ""));
      dummyDataStore.update(journal);
    }
  }
}
