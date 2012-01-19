/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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

package org.ambraproject.topazmigration;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.*;
import org.topazproject.otm.criterion.DetachedCriteria;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Test for the migration of all subclasses of {@link org.topazproject.ambra.models.Aggregation}
 *
 * @author Alex Kudlick Date: Mar 25, 2011
 *         <p/>
 *         org.ambraproject.topazmigration
 */
public class AggregationMigrationTest extends BaseMigrationTest {

  //The number of aggregations to load up at a time
  private static final int INCREMENT_SIZE = 50;

  @DataProvider(name = "journals")
  public Iterator<Object[]> journals() throws Exception {
    assertTrue(restartSessions());
    return new MigrationDataIterator(
        this, Journal.class,
        INCREMENT_SIZE
    );
  }

  @Test(dataProvider = "journals")
  public void diffJournals(Journal mysqlJournal, Journal topazJournal) {

    assertNotNull(topazJournal, "Topaz returned a null journal");
    topazJournal = loadTopazObject(topazJournal.getId(), Journal.class);

    assertNotNull(mysqlJournal, "Mysql didn't return a journal for id: " + topazJournal.getId());
    compareAggregationProperties(mysqlJournal, topazJournal);
    assertEquals(mysqlJournal.getKey(), topazJournal.getKey(),
        "Mysql and topaz Journals didn't have matching keys");
    assertEquals(mysqlJournal.geteIssn(), topazJournal.geteIssn(),
        "Mysql and topaz Journals didn't have matching eIssn's");
    assertMatchingLists(mysqlJournal.getVolumes(), topazJournal.getVolumes(),
        "Mysql and topaz Journals didn't have matching volume lists");
    assertEquals(mysqlJournal.getImage(), topazJournal.getImage(),
        "Mysql and topaz Journals didn't have matching keys");
  }

  @DataProvider(name = "volumes")
  public Iterator<Object[]> volumes() throws Exception {
    assertTrue(restartSessions());
    return new MigrationDataIterator(
        this, Volume.class,
        INCREMENT_SIZE
    );
  }

  @Test(dataProvider = "volumes")
  public void diffVolumes(Volume mysqlVolume, Volume topazVolume) {

    assertNotNull(topazVolume, "Topaz returned a null volume");
    topazVolume = loadTopazObject(topazVolume.getId(), Volume.class);

    assertNotNull(mysqlVolume, "Mysql didn't return a volume for id: " + topazVolume.getId());
    compareAggregationProperties(mysqlVolume, topazVolume);
    assertEquals(mysqlVolume.getDisplayName(), topazVolume.getDisplayName(),
        "Mysql and topaz Volumes didn't have matching display names; id: " + mysqlVolume.getId());
    assertEquals(mysqlVolume.getImage(), topazVolume.getImage(),
        "Mysql and topaz Volumes didn't have matching image URIs; id: " + mysqlVolume.getId());
    assertMatchingLists(mysqlVolume.getIssueList(), topazVolume.getIssueList(),
        "Mysql and topaz Volumes didn't have matching issueLists; id: " + mysqlVolume.getId());
  }

  @DataProvider(name = "issues")
  public Iterator<Object[]> issues() throws Exception {
    assertTrue(restartSessions());
    return new MigrationDataIterator(
        this, Issue.class,
        INCREMENT_SIZE
    );
  }

  @Test(dataProvider = "issues")
  public void diffIssues(Issue mysqlIssue, Issue topazIssue) {

    assertNotNull(topazIssue, "Topaz returned a null issue");
    topazIssue = loadTopazObject(topazIssue.getId(), Issue.class);

    assertNotNull(mysqlIssue, "Mysql didn't return an issue for id: " + topazIssue.getId());
    compareAggregationProperties(mysqlIssue, topazIssue);
    assertEquals(mysqlIssue.getDisplayName(), topazIssue.getDisplayName(),
        "Mysql and topaz Issues didn't have matching display names");
    assertMatchingLists(topazIssue.getArticleList(), mysqlIssue.getArticleList(),
        "Mysql and topaz Issues didn't have matching Article Lists");
    assertEquals(mysqlIssue.getRespectOrder(), topazIssue.getRespectOrder(),
        "Mysql and topaz Issues didn't have matching values for respect order");
    assertEquals(mysqlIssue.getImage(), topazIssue.getImage(),
        "Mysql and topaz Issues didn't have matching image URIs");
  }


  private void compareAggregationProperties(Aggregation mysqlAggregation, Aggregation topazAggregation) {
    assertEquals(mysqlAggregation.getId(), topazAggregation.getId(),
        "Topaz and Mysql had differing ids for Aggregations");
    compareEditorialBoards(mysqlAggregation.getEditorialBoard(), topazAggregation.getEditorialBoard(), mysqlAggregation.getId());
    //compare dublin cores
    if (mysqlAggregation.getDublinCore() == null) {
      assertNull(topazAggregation.getDublinCore(),
          "MySQL had a null Dublin Core for aggregation and Topaz didn't; id: " + mysqlAggregation.getId());
    } else if (topazAggregation.getDublinCore() == null) {
      assertNull(mysqlAggregation.getDublinCore(),
          "Topaz had a null Dublin Core for aggregation and MySQL didn't; id: " + mysqlAggregation.getId());
    } else {
      ArticleMigrationTest.compareDublinCore(mysqlAggregation.getDublinCore(), topazAggregation.getDublinCore(),
          "aggregations");
    }
    assertMatchingLists(mysqlAggregation.getSimpleCollection(), topazAggregation.getSimpleCollection(),
        "Mysql and Topaz had differing simpleCollection lists for Aggregation objects; id: " + mysqlAggregation.getId());

    //just compare ids for smart collection
    assertMatchingLists(getSmartCollectionIds(mysqlAggregation.getSmartCollectionRules()),
        getSmartCollectionIds(topazAggregation.getSmartCollectionRules()),
        "MySQL and Topaz Aggregation objects had differing smart collection rules;" +
            " id: " + mysqlAggregation.getId());

    //just compare ids for supersedes and supersededBy
    if (mysqlAggregation.getSupersedes() == null) {
      assertNull(topazAggregation.getSupersedes(),
          "MySQL had a null supersedes for Aggregation and Topaz didn't; id: " + mysqlAggregation.getId());
    } else if (topazAggregation.getSupersedes() == null) {
      assertNull(mysqlAggregation.getSupersedes(),
          "Topaz had a null supersedes for Aggregation and MySQL didn't; id: " + mysqlAggregation.getId());
    } else {
      assertEquals(mysqlAggregation.getSupersedes().getId(), topazAggregation.getSupersedes().getId(),
          "Mysql and Topaz didn't have the same supersedes value for an Aggregation object;" +
              " id: " + mysqlAggregation.getId());
    }
    if (mysqlAggregation.getSupersededBy() == null) {
      assertNull(topazAggregation.getSupersededBy(),
          "MySQL had a null supersededBy for Aggregation and Topaz didn't;" +
              " id: " + mysqlAggregation.getId());
    } else if (topazAggregation.getSupersededBy() == null) {
      assertNull(mysqlAggregation.getSupersededBy(),
          "Topaz had a null supersededBy for Aggregation and MySQL didn't;" +
              " id: " + mysqlAggregation.getId());
    } else {
      assertEquals(mysqlAggregation.getSupersededBy().getId(), topazAggregation.getSupersededBy().getId(),
          "Mysql and Topaz didn't have the same supersededBy value for an Aggregation object;" +
              " id: " + mysqlAggregation.getId());
    }


  }

  private List<URI> getSmartCollectionIds(List<DetachedCriteria> smartCollectionRules) {
    List<URI> ids = new ArrayList<URI>();
    if (smartCollectionRules != null) {
      for (DetachedCriteria dc : smartCollectionRules) {
        ids.add(dc.getCriteriaId());
      }
    }
    return ids;
  }

  private void compareEditorialBoards(EditorialBoard mysqlEditorialBoard, EditorialBoard topazEditorialBoard, URI aggregationId) {
    if (mysqlEditorialBoard == null) {
      assertNull(topazEditorialBoard, "Mysql had null EditorialBoard and Topaz didn't; aggregation: "  + aggregationId);
      return;
    } else if (topazEditorialBoard == null) {
      assertNull(topazEditorialBoard, "Mysql had null EditorialBoard and Topaz didn't; aggregation: "  + aggregationId);
      return;
    }
    assertEquals(mysqlEditorialBoard.getId(), topazEditorialBoard.getId(),
        "MySql and topaz didn't have matching Editorial Board ids; aggregation: " + aggregationId);
    //Just compare ids of the supersedes and supersededBy properties
    if (mysqlEditorialBoard.getSupersedes() == null) {
      assertNull(topazEditorialBoard.getSupersedes(),
          "MySQL had a null supersedes for EditorialBoard and Topaz didn't;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    } else if (topazEditorialBoard.getSupersedes() == null) {
      assertNull(mysqlEditorialBoard.getSupersedes(),
          "Topaz had a null supersedes for EditorialBoard and MySQL didn't;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    } else {
      assertEquals(mysqlEditorialBoard.getSupersedes().getId(), topazEditorialBoard.getSupersedes().getId(),
          "Topaz and Mysql EditorialBoards didn't have the same supersedes values;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    }
    if (mysqlEditorialBoard.getSupersededBy() == null) {
      assertNull(topazEditorialBoard.getSupersededBy(),
          "MySQL had a null supersededBy for EditorialBoard and Topaz didn't;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    } else if (topazEditorialBoard.getSupersededBy() == null) {
      assertNull(mysqlEditorialBoard.getSupersededBy(),
          "Topaz had a null supersededBy for EditorialBoard and MySQL didn't;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    } else {
      assertEquals(mysqlEditorialBoard.getSupersededBy().getId(), topazEditorialBoard.getSupersededBy().getId(),
          "Topaz and Mysql EditorialBoards didn't have the same supersededBy values;" +
              " editorial board: " + mysqlEditorialBoard.getId());
    }

    //Just compare ids of the editors
    assertMatchingLists(getUserProfileIds(topazEditorialBoard.getEditors()),
        getUserProfileIds(mysqlEditorialBoard.getEditors()),
        "Mysql and Topaz EditorialBoards didn't have matching editor lists;" +
            " editorial board: " + mysqlEditorialBoard.getId());
  }

  private List<URI> getUserProfileIds(List<UserProfile> editors) {
    List<URI> ids = new ArrayList<URI>();
    if (editors != null) {
      for (UserProfile profile : editors) {
        ids.add(profile.getId());
      }
    }
    return ids;
  }


}
