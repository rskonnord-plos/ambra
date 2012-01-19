/*
 * $HeadURL:
 * $Id:
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.migrationtests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick Date: Mar 18, 2011
 *         <p/>
 *         org.plos.topazMigration
 */
public class CitationMigrationTest extends BaseMigrationTest {

  //number of citations to load up at a time
  private static final int INCREMENT_SIZE = 200;

  @DataProvider(name = "citations")
  public Iterator<Object[]> getCitations() throws Exception {
    return new MigrationDataIterator(
        this, Citation.class,
        INCREMENT_SIZE
    );

  }

  @Test(dataProvider = "citations")
  public void diffCitations(Citation mysqlCitation, Citation topazCitation) {
    assertNotNull(topazCitation, "Topaz returned a null citation");
    assertNotNull(mysqlCitation, "MySql didn't return a citation for id: " + topazCitation.getId());
    compareCitations(mysqlCitation, topazCitation);
  }

  public static void compareCitations(Citation mysqlCitation, Citation topazCitation) {
    assertEquals(mysqlCitation.getKey(), topazCitation.getKey(), "Mysql and topaz didn't have matching Keys; citation: " + mysqlCitation.getId());

    assertEquals(mysqlCitation.getYear(), topazCitation.getYear(), "Mysql and topaz didn't have matching Years; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getDisplayYear(), topazCitation.getDisplayYear(),"Mysql and topaz didn't have matching Display Years; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getMonth(), topazCitation.getMonth(),"Mysql and topaz didn't have matching Months; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getDay(), topazCitation.getDay(),"Mysql and topaz didn't have matching Days; citation: " + mysqlCitation.getId());

    assertEquals(mysqlCitation.getVolumeNumber(), topazCitation.getVolumeNumber(),"Mysql and topaz didn't have matching Volume Numbers; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getVolume(), topazCitation.getVolume(),"Mysql and topaz didn't have matching Volumes; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getIssue(), topazCitation.getIssue(),"Mysql and topaz didn't have matching Issues; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getTitle(), topazCitation.getTitle(),"Mysql and topaz didn't have matching Titles; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getPublisherLocation(), topazCitation.getPublisherLocation(),"Mysql and topaz didn't have matching Publisher Locations; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getPublisherName(), topazCitation.getPublisherName(),"Mysql and topaz didn't have matching Publisher names; citation: " + mysqlCitation.getId());

    assertEquals(mysqlCitation.getPages(), topazCitation.getPages(),"Mysql and topaz didn't have matching Pages; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getELocationId(), topazCitation.getELocationId(),"Mysql and topaz didn't have matching eLocationIds; citation: " + mysqlCitation.getId());

    assertEquals(mysqlCitation.getJournal(), topazCitation.getJournal(),"Mysql and topaz didn't have matching Journals; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getNote(), topazCitation.getNote(),"Mysql and topaz didn't have matching Notes; citation: " + mysqlCitation.getId());

    List<UserProfile> topazEditors = new ArrayList<UserProfile>(topazCitation.getEditors());
    List<UserProfile> mysqlEditors = new ArrayList<UserProfile>(mysqlCitation.getEditors());
    if (mysqlEditors.size() != 0) {
    //we removed all the authors and editors from article and reference citations, so it's ok not to have any citations
      assertEquals(mysqlEditors.size(), topazEditors.size(),"Mysql and topaz didn't have the same number of editors; citation: " + mysqlCitation.getId());
      for (int i = 0; i < topazEditors.size(); i++) {
        UserProfileMigrationTest.compareUserProfiles(topazEditors.get(i), mysqlEditors.get(i));
      }
    }

    List<UserProfile> topazAuthors = new ArrayList<UserProfile>(topazCitation.getAuthors());
    List<UserProfile> mysqlAuthors = new ArrayList<UserProfile>(mysqlCitation.getAuthors());
    if (mysqlAuthors.size() != 0) {
    //we removed all the authors and editors from article and reference citations, so it's ok not to have any citations
      assertEquals(mysqlAuthors.size(), topazAuthors.size(),"Mysql and topaz didn't have the same number of Authors; citation: " + mysqlCitation.getId());
      for (int i = 0; i < topazAuthors.size(); i++) {
        UserProfileMigrationTest.compareUserProfiles(topazAuthors.get(i), mysqlAuthors.get(i));
      }
    }


    assertMatchingLists(mysqlCitation.getCollaborativeAuthors(),topazCitation.getCollaborativeAuthors(),
        "MySQL and Topaz citations didn't have matching collaborative authors");
    assertEquals(mysqlCitation.getUrl(), topazCitation.getUrl(),"Mysql and topaz didn't have matching URLs; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getDoi(), topazCitation.getDoi(),"Mysql and topaz didn't have matching doi's; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getSummary(), topazCitation.getSummary(),"Mysql and topaz didn't have matching Summaries; citation: " + mysqlCitation.getId());
    assertEquals(mysqlCitation.getCitationType(), topazCitation.getCitationType(),"Mysql and topaz didn't have matching Citation Types; citation: " + mysqlCitation.getId());
  }
}
