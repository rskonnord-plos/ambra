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

package org.ambraproject.models;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class JournalTest extends BaseHibernateTest {

  @Test
  public void testSaveBasicJournal() {
    Journal journal = new Journal();
    journal.setJournalKey("test journal key");
    journal.setDescription("test journal description");
    journal.seteIssn("test eISsn");
    journal.setTitle("test title");
    journal.setImageUri("test image uri");

    Serializable id = hibernateTemplate.save(journal);

    Journal savedJournal = (Journal) hibernateTemplate.get(Journal.class, id);
    assertNotNull(savedJournal, "couldn't save journal");
    assertEquals(savedJournal, journal, "didn't store correct journal properties");
    assertNotNull(savedJournal.getCreated(), "saved journal didn't get created date set");
  }

  @Test
  public void testSaveWithCurrentIssue() {
    Journal journal = new Journal("journal key with current issue");
    Issue issue = new Issue("id:testCurrentIssueForJournal");
    issue.setDescription("test current issue");
    issue.setDisplayName("current issue display name");
    issue.setImageUri("id:currentIssueImageUri");
    issue.setRespectOrder(false);
    issue.setTitle("current issue title");
    issue.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));
    journal.setCurrentIssue(issue);

    Serializable journalId = hibernateTemplate.save(journal);
    Journal savedJournal = (Journal) hibernateTemplate.get(Journal.class, journalId);
    assertNotNull(savedJournal, "didn't save journal");
    assertNotNull(savedJournal.getCurrentIssue(), "didn't cascade save to issue");
    assertEquals(savedJournal.getCurrentIssue(), issue, "Issue didn't have correct properties");
    assertNotNull(savedJournal.getCurrentIssue().getCreated(), "Issue didn't get created date set");
  }

  @Test
  public void testSaveWithVolumes() {
    Journal journal = new Journal("journal key with volumes");

    final Volume volume1 = new Volume("testVolumeForJournal1");
    volume1.setTitle("volume 1 title");
    final Volume volume2 = new Volume("testVolumeForJournal2");
    volume2.setTitle("volume 2 title");
    final Volume volume3 = new Volume("testVolumeForJournal3");
    volume3.setTitle("volume 3 title");

    journal.setVolumes(Arrays.asList(volume1, volume2, volume3));

    final Serializable journalId = hibernateTemplate.save(journal);

    //need to access the volumes in a session b/c they're lazy
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal savedJournal = (Journal) session.get(Journal.class, journalId);
        assertNotNull(savedJournal, "didn't save journal");
        assertEquals(savedJournal.getVolumes().toArray(), new Volume[]{volume1, volume2, volume3},
            "saved journal had incorrect volumes");
        for (Volume v : savedJournal.getVolumes()) {
          assertNotNull(v.getCreated(), "Volume didn't get created date set");
        }
        return null;
      }
    });
  }

  @Test
  public void testSaveWithArticleList() {
    Journal journal = new Journal("journal key with article list");

    final ArticleList articleList1 = new ArticleList("testarticleListForJournal1");
    articleList1.setDisplayName("News");
    articleList1.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));

    final ArticleList articleList2 = new ArticleList("testarticleListForJournal2");
    articleList2.setDisplayName("SPAM");
    articleList2.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));

    journal.setArticleList(Arrays.asList(articleList1, articleList2));

    final Serializable journalId1 = hibernateTemplate.save(journal);

    //need to access the article list in a session b/c they're lazy
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal savedJournal = (Journal) session.get(Journal.class, journalId1);
        assertNotNull(savedJournal, "didn't save journal");
        assertEquals(savedJournal.getArticleList().toArray(), new ArticleList[]{articleList1, articleList2},
            "saved journal had incorrect article list");
        for (ArticleList ai : savedJournal.getArticleList()) {
          assertNotNull(ai.getCreated(), "Article List didn't get created date set");
        }
        return null;
      }
    });
  }

  @Test
  public void testUpdate() {
    final long testStart = Calendar.getInstance().getTimeInMillis();
    final Journal journal = new Journal("old journal key");
    journal.setDescription("old description");
    journal.setTitle("old title");

    final Serializable journalId = hibernateTemplate.save(journal);

    journal.setDescription("new description");
    journal.setJournalKey("new journal key");
    journal.setTitle("new title");
    journal.setCurrentIssue(new Issue("id:testCurrentIssueForUpdateJournal"));
    journal.setVolumes(Arrays.asList(
        new Volume("updatedJournalVolume1"),
        new Volume("updatedJournalVolume2"),
        new Volume("updatedJournalVolume3")));
    hibernateTemplate.update(journal);

    //need to access volumes in a session b/c they're lazy
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Journal savedJournal = (Journal) session.get(Journal.class, journalId);
        assertEquals(journal, savedJournal, "journal didn't get properties updated");
        assertNotNull(savedJournal.getCurrentIssue(), "journal didn't get current issue added");
        assertEquals(savedJournal.getVolumes().size(), journal.getVolumes().size(), "journal didn't get volumes added");
        assertNotNull(savedJournal.getLastModified(), "journal didn't get last modified set");
        assertTrue(savedJournal.getLastModified().getTime() > testStart, "last modified wasn't after test start");
        assertTrue(savedJournal.getLastModified().getTime() > savedJournal.getCreated().getTime(),
            "last modified wasn't after created");
       return null;
      }
    });
  }

}
