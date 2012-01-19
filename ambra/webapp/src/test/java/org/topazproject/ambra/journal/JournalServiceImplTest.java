/*
 * $HeadURL: http://ambraproject.org/svn/ambra/branches/ANH-Conversion/ambra/webapp/src/test/java/org/topazproject/ambra/crossref/CrossRefLookupServiceImplTest.java $
 * $Id: CrossRefLookupServiceImplTest.java 9117 2011-06-10 22:07:29Z josowski $
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

package org.topazproject.ambra.journal;

import org.testng.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.testutils.DummyDataStore;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.EQCriterion;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class JournalServiceImplTest extends BaseTest {
  @Autowired
  protected JournalService journalService;

  @Autowired
  protected DummyDataStore dummyDataStore;

  @DataProvider(name = "journals")
  public Object[][] journalData() {
    return new Object[][] {
      { "journal", "0000-0000", "journal-title" },
      { "journal1", "0000-0001", "journal-title1" },
      { "journal2", "0000-0002", "journal-title2" },
      { "journal3", "0000-0003", "journal-title3" },
      { "journal4", "0000-0004", "journal-title4" },
      { "journal5", "0000-0005", "journal-title5" },
      { "journal6", "0000-0006", "journal-title6" }
    };
  }

  @DataProvider(name = "articles")
  public Object[][] articles() {
    return new Object[][] {
      { "journal", URI.create("info:doi/1") },
      { "journal1", URI.create("info:doi/1") },
      { "journal2", URI.create("info:doi/1") },
      { "journal3", URI.create("info:doi/1") },
      { "journal", URI.create("info:doi/2") },
      { "journal1", URI.create("info:doi/3") },
      { "journal2", URI.create("info:doi/3") },
      { "journal1", URI.create("info:doi/4") },
      { "journal2", URI.create("info:doi/4") },
      { "journal5", URI.create("info:doi/5") },
      { "journal6", URI.create("info:doi/6") },
      { "journal1", URI.create("info:doi/7") },
      { "journal2", URI.create("info:doi/7") },
      { "journal3", URI.create("info:doi/7") }
    };
  }

  @BeforeMethod
  private void setupDB() {
    for(Object[] j : journalData()) {
      createJournals((String)j[0], (String)j[1], (String)j[2]);
    }

    for(Object[] a : articles()) {
      addArticleToJournal((String)a[0], (URI)a[1]);
    }
  }

  /**
   * Dirty the context and then exit.  This is here to put the DB into a fresh state regardless of
   * how other tests left it
   */
  @Test(priority = 1)
  @DirtiesContext
  public void forceDBReset() {
  }

  private void addArticleToJournal(final String journalKey, final URI articleId) {
    Journal j = journalService.getJournal(journalKey);

    j.getSimpleCollection().add(articleId);

    dummyDataStore.update(j);
  }

  private String getJournalEissn(String key) {
    for(int a = 0; a < journalData().length; a++) {
      if(((String)journalData()[a][0]).equals(key))
      {
        return (String)journalData()[a][1];
      }
    }
    return null;
  }

  private int getJournalCount(URI articleId) {
    int count = 0;

    for(int a = 0; a < articles().length; a++) {
      if(((URI)articles()[a][1]).equals(articleId))
      {
        count++;
      }
    }

    return count;
  }


  private void createJournals(final String journalKey, final String eIssn, final String title)
  {
    Journal j = new Journal();
    j.seteIssn(eIssn);
    j.setKey(journalKey);

    DublinCore dc = new DublinCore();
    dc.setTitle(title);
    dummyDataStore.store(dc);

    j.setDublinCore(dc);

    //Setup smart collection for the journal
    EQCriterion eQCriterion = new EQCriterion();
    eQCriterion.setFieldName("eIssn");
    eQCriterion.setValue(eIssn);

    dummyDataStore.store(eQCriterion);

    List<Criterion> cList = new ArrayList<Criterion>();
    cList.add(eQCriterion);

    DetachedCriteria detachedCriteria = new DetachedCriteria();
    detachedCriteria.setCriterionList(cList);

    dummyDataStore.store(detachedCriteria);

    List<DetachedCriteria> dcList = new ArrayList<DetachedCriteria>();
    dcList.add(detachedCriteria);

    j.setSmartCollectionRules(dcList);

    dummyDataStore.store(j);
  }

  @Test(dataProvider = "journals")
  @DirtiesContext
  public void getJournalTest(final String journalKey, final String eIssn, final String title) {
    Journal j = journalService.getJournal(journalKey);

    Assert.assertEquals(journalKey, j.getKey());
    Assert.assertEquals(eIssn, j.geteIssn());
    Assert.assertEquals(title, j.getDublinCore().getTitle());
  }

  @Test(dataProvider = "journals")
  @DirtiesContext
  public void getJournalByEissn(final String journalKey, final String eIssn, final String title) {
    Journal j = journalService.getJournalByEissn(eIssn);

    Assert.assertEquals(journalKey, j.getKey());
    Assert.assertEquals(eIssn, j.geteIssn());
    Assert.assertEquals(title, j.getDublinCore().getTitle());
  }

  @Test
  @DirtiesContext
  public void getAllJournalsTest() {
    Set<String> journalKeys = journalService.getAllJournalNames();

    List<String> keys = new ArrayList<String>();
    keys.add("journal");
    keys.add("journal1");

    Assert.assertEquals(journalKeys.size(), 2);

    for(int a = 0; a < keys.size(); a++) {
      for(String s : journalKeys)
      {
        if(keys.get(a).equals(s)) {
          keys.remove(a);
          a = 0;
        }
      }
    }

    Assert.assertEquals(keys.size(), 0);
  }

  @Test
  @DirtiesContext
  public void getAllJournalNamesTest() {
    Set<String> journalNames = journalService.getAllJournalNames();

    Assert.assertEquals(2, journalNames.size());

    List<String> names = new ArrayList<String>();
    names.add("journal");
    names.add("journal1");

    Assert.assertEquals(journalNames.size(), 2);

    for(int a = 0; a < names.size(); a++) {
      for(String s : journalNames)
      {
        if(names.get(a).equals(s)) {
          names.remove(a);
          a = 0;
        }
      }
    }

    Assert.assertEquals(names.size(), 0);
  }

  @Test(dataProvider = "articles")
  @DirtiesContext
  public void getJournalsForObjectTest(final String journalKey, final URI oid) {
    Set<Journal> journals = journalService.getJournalsForObject(oid);

    int journalCount = getJournalCount(oid);
    String journalEissn = getJournalEissn(journalKey);

    //Lets make sure the article is in the correct count of journals
    Assert.assertEquals(journals.size(), journalCount);

    //Lets make sure the article is in the journal passed in
    boolean foundJournal = false;
    for(Object o : journals) {
      Journal j = (Journal)o;

      if(j.geteIssn().equals(journalEissn)) {
        foundJournal = true;
      }
    }
    Assert.assertTrue(foundJournal);
  }

  @Test(dataProvider = "articles")
  @DirtiesContext
  public void getJournalURIsForObjectText(final String journalEissn, final URI oid) {
    Set<URI> journals = journalService.getJournalURIsForObject(oid);

    int journalCount = getJournalCount(oid);

    Assert.assertEquals(journals.size(), journalCount);
  }
}
