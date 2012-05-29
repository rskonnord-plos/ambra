/* $HeadURL$
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

package org.ambraproject.article.service;

import org.ambraproject.BaseTest;
import org.ambraproject.views.TOCArticleGroup;
import org.ambraproject.views.IssueInfo;
import org.ambraproject.views.VolumeInfo;
import org.ambraproject.model.article.ArticleInfo;
import org.ambraproject.model.article.ArticleType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Volume;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for methods of {@link BrowseService} that don't use solr.  Working directory for the test should be set to
 * either the new hope home directory or the ambra webapp home
 *
 * @author Alex Kudlick Date: 5/16/11
 * @author Joe Osowski
 *
 */
public class BrowseServiceTest extends BaseTest {

  @Autowired
  protected BrowseService browseService;

  private String articleType1 = "id://test-article-type-1";
  private String articleType2 = "id://test-article-type-2";

  @BeforeClass
  public void addArticleTypes() {
    ArticleType.addArticleType(
        URI.create(articleType1),        //type uri
        "code",              //code
        "Research Article",  //heading
        "Research Articles"  //plural heading
    );
    ArticleType.addArticleType(
        URI.create(articleType2),    //type uri
        "code2",         //code
        "Test Article",  //heading
        "Test Articles"  //plural heading
    );
  }

  @DataProvider(name = "issueIds")
  public Object[][] issueIds() {
    Article imageArticle = new Article();
    imageArticle.setDoi("id://test-image");
    imageArticle.setTitle("Bill and Ted's Excellent Adventure");
    imageArticle.setDescription("Two seemingly dumb teens struggle " +
        "to prepare a historical presentation with the help of a time machine. ");

    dummyDataStore.store(imageArticle);

    Issue testIssue = new Issue();
    testIssue.setDisplayName("Display name of test issue number 1");
    testIssue.setImageUri(imageArticle.getDoi());
    testIssue.setRespectOrder(true);
    testIssue.setTitle(imageArticle.getTitle());
    testIssue.setDescription(imageArticle.getDescription());
    testIssue.setIssueUri("info:doi/issue/1");
    testIssue.setArticleDois(Arrays.asList(
        "id:test-doi-issue1",
        "id:test-doi-issue2",
        "id:test-doi-issue3",
        "id:test-doi-issue4"
    ));
    dummyDataStore.store(testIssue);
    Issue prevIssue = new Issue("info:doi/issue/previous");
    dummyDataStore.store(prevIssue);
    Issue nextIssue = new Issue("info:doi/issue/next");
    dummyDataStore.store(nextIssue);

    Volume parentVolume = new Volume("info:doi/test-parent-for-get-issue-info");
    parentVolume.setIssues(Arrays.asList(
        dummyDataStore.get(Issue.class, prevIssue.getID()),
        dummyDataStore.get(Issue.class, testIssue.getID()),
        dummyDataStore.get(Issue.class, nextIssue.getID())
    ));
    dummyDataStore.store(parentVolume);

    return new Object[][]{
        { testIssue.getIssueUri(), testIssue, parentVolume.getVolumeUri(), prevIssue.getIssueUri(), nextIssue.getIssueUri() }
    };
  }


  @Test(dataProvider = "issueIds")
  public void testGetIssueInfo(String issueDoi, Issue expectedIssue, String parentVolume, String prevIssue, String nextIssue) {
    IssueInfo issueInfo = browseService.getIssueInfo(issueDoi);
    assertNotNull(issueInfo, "returned null issue info");
    assertEquals(issueInfo.getIssueURI(), issueDoi, "returned issue info with incorrect id");
    assertEquals(issueInfo.getDisplayName(), expectedIssue.getDisplayName(),
        "returned issue info with incorrect display name");
    assertEquals(issueInfo.getImageArticle(), expectedIssue.getImageUri(),
        "returned issue info with incorrect issue uri");
    assertEquals(issueInfo.getDescription(), expectedIssue.getDescription(),
        "returned issue info with incorrect description");
    assertEqualsNoOrder(issueInfo.getArticleUriList().toArray(), expectedIssue.getArticleDois().toArray(),
        "returned issue info with incorrect article dois");
    assertEquals(issueInfo.getPrevIssue(), prevIssue, "Returned issue with incorrect previous issue uri");
    assertEquals(issueInfo.getNextIssue(), nextIssue, "Returned issue with incorrect next issue uri");
    assertEquals(issueInfo.getParentVolume(), parentVolume, "Returned issue with incorrect parent volume uri");
  }

  @Test(dataProvider = "issueIds")
  public void testCreateIssueInfo(String issueDoi, Issue expectedIssue, String parentVolume, String prevIssue, String nextIssue) {
    IssueInfo issueInfo = browseService.createIssueInfo(expectedIssue);
    assertNotNull(issueInfo, "returned null issue info");
    assertEquals(issueInfo.getIssueURI(), issueDoi, "returned issue info with incorrect id");
    assertEquals(issueInfo.getDisplayName(), expectedIssue.getDisplayName(),
        "returned issue info with incorrect display name");
    assertEquals(issueInfo.getImageArticle(), expectedIssue.getImageUri(),
        "returned issue info with incorrect issue uri");
    assertEquals(issueInfo.getDescription(), expectedIssue.getDescription(),
        "returned issue info with incorrect description");
    assertEqualsNoOrder(issueInfo.getArticleUriList().toArray(), expectedIssue.getArticleDois().toArray(),
        "returned issue info with incorrect article dois");
    assertEquals(issueInfo.getPrevIssue(), prevIssue, "Returned issue with incorrect previous issue uri");
    assertEquals(issueInfo.getNextIssue(), nextIssue, "Returned issue with incorrect next issue uri");
    assertEquals(issueInfo.getParentVolume(), parentVolume, "Returned issue with incorrect parent volume uri");
  }

  @DataProvider(name = "issueArticles")
  public Object[][] getIssueArticles() {
    Article article1 = new Article();
    article1.setDoi("id:test-article-for-issue-list1");

    Article article2 = new Article();
    article2.setDoi("id:test-article-for-issue-list2");

    Article article3 = new Article();
    article3.setDoi("id:test-article-for-issue-list3");

    dummyDataStore.store(article1);
    dummyDataStore.store(article2);
    dummyDataStore.store(article3);

    List<String> articleIds = new ArrayList<String>(3);
    articleIds.add(article1.getDoi());
    articleIds.add(article2.getDoi());
    articleIds.add(article3.getDoi());

    Issue issue = new Issue();
    issue.setArticleDois(articleIds);
    issue.setIssueUri("id://issueUri.one/");
    dummyDataStore.store(issue);

    return new Object[][]{
      { issue.getIssueUri(), articleIds }
    };
  }

  @Test(dataProvider = "issueArticles")
  public void testGetArticleList(String issueUri, List<String> expectedArticleURIs) {
    IssueInfo issueInfo = browseService.getIssueInfo(issueUri);
    List<String> results = issueInfo.getArticleUriList();
    assertNotNull(results, "returned null list of article ids");
    assertEquals(results.size(), expectedArticleURIs.size(), "returned incorrect number of article ids");
    assertEqualsNoOrder(results.toArray(), expectedArticleURIs.toArray(), "returned incorrect article ids");
  }

  @DataProvider(name = "volumes")
  public Object[][] getVolumes() {
    Article imageArticle1 = new Article();
    imageArticle1.setDoi("id://volume-1-image");
    dummyDataStore.store(imageArticle1);

    Article imageArticle2 = new Article();
    imageArticle2.setDoi("id://volume-2-image");
    dummyDataStore.store(imageArticle2);

    Volume volume1 = new Volume();
    volume1.setDisplayName("Volume 1");
    volume1.setVolumeUri("id://volume1");
    volume1.setImageUri(imageArticle1.getDoi());

    Volume volume2 = new Volume();
    volume2.setDisplayName("Volume 2");
    volume2.setVolumeUri("id://volume2");
    volume2.setImageUri(imageArticle2.getDoi());

    List<Volume> volumes = new ArrayList<Volume>();
    volumes.add(volume1);
    volumes.add(volume2);

    Journal journal = new Journal();
    journal.setJournalKey("test-journal-key");
    journal.setVolumes(volumes);
    dummyDataStore.store(journal);

    return new Object[][]{
        { volume1.getVolumeUri(), journal.getJournalKey(), volume1 },
        { volume2.getVolumeUri(), journal.getJournalKey(), volume2 }
    };
  }

  @Test(dataProvider = "volumes")
  public void testGetVolumeInfo(String volumeUri, String journalKey, Volume expectedVolume) {
    VolumeInfo volumeInfo = browseService.getVolumeInfo(volumeUri, journalKey);
    assertNotNull(volumeInfo, "returned null volume info");
    assertEquals(volumeInfo.getVolumeUri(), volumeUri, "returned volume info with incorrect id");
    assertEquals(volumeInfo.getImageArticle(), expectedVolume.getImageUri(),
        "returned volume with incorrect image article");
    assertEquals(volumeInfo.getDisplayName(), expectedVolume.getDisplayName(),
        "returned volume info with incorrect display name");
  }

  @DataProvider(name = "article")
  public Object[][] getArticle() {
    Article article = new Article();
    article.setDoi("id://test-article-47");

    article.setTitle("test title for article info");
    article.setDate(new Date());
    article.setDescription("test, test, test, this is a test");

    List<String> authorNames = new ArrayList<String>(2);
    List<ArticleAuthor> authors = new ArrayList<ArticleAuthor>(2);
    ArticleAuthor author1 = new ArticleAuthor();
    author1.setFullName("Some fake author");
    dummyDataStore.store(author1);
    authors.add(author1);

    ArticleAuthor author2 = new ArticleAuthor();
    author2.setFullName("Michael Eisen");
    dummyDataStore.store(author2);
    authors.add(author2);

    article.setAuthors(authors);
    dummyDataStore.store(article);
    authorNames.add(author1.getFullName());
    authorNames.add(author2.getFullName());
    return new Object[][]{
        {article.getDoi(), article, authorNames}
    };
  }

  @DataProvider(name = "volumeList")
  public Object[][] getVolumeList() {
    Volume volume1 = new Volume();
    volume1.setDisplayName("Volume 1");
    volume1.setVolumeUri("id://volume-1-uri");
    volume1.setImageUri("id://volume-1-image");
    dummyDataStore.store(volume1);

    Volume volume2 = new Volume();
    volume2.setDisplayName("Volume 2");
    volume2.setImageUri("id://volume-2-uri");
    volume2.setImageUri("id://volume-2-image");
    dummyDataStore.store(volume2);

    List<String> volumeIds = new ArrayList<String>();
    volumeIds.add(volume1.getVolumeUri());
    volumeIds.add(volume2.getVolumeUri());

    Journal journal = new Journal();
    journal.setJournalKey("test-journal");
    journal.setVolumes(Arrays.asList(volume1, volume2));

    return new Object[][]{
        { journal, volumeIds }
    };
  }

  @Test(dataProvider = "volumeList")
  public void testGetVolumeInfosForJournal(Journal journal, List<String> expectedIds) {
    List<VolumeInfo> volumeInfos = browseService.getVolumeInfosForJournal(journal);
    assertNotNull(volumeInfos, "returned null list of volume infos");
    assertEquals(volumeInfos.size(), expectedIds.size(), "returned incorrect number of volume infos");
    List<String> actualIds = new ArrayList<String>(volumeInfos.size());
    for (VolumeInfo volumeInfo : volumeInfos) {
      actualIds.add(volumeInfo.getVolumeUri());
    }
    assertEqualsNoOrder(actualIds.toArray(), expectedIds.toArray(), "Didn't return expected volumes");
  }

  @DataProvider(name = "articleGroupList")
  public Object[][] getArticleGroupList() {
    Set<String> bothTypes = new HashSet<String>(2);
    bothTypes.add(articleType1);
    bothTypes.add(articleType2);

    Set<String> firstType = new HashSet<String>(1);
    firstType.add(articleType1);

    Set<String> secondType = new HashSet<String>(1);
    secondType.add(articleType2);

    Article articleWithBothTypes = new Article();
    articleWithBothTypes.setDoi("id://article-1");
    articleWithBothTypes.setTypes(bothTypes);
    articleWithBothTypes.setDate(new Date());

    Article articleWithFirstType = new Article();
    articleWithFirstType.setDoi("id://test-article2");
    articleWithFirstType.setTypes(firstType);
    articleWithFirstType.setDate(new Date());

    Article articleWithSecondType = new Article();
    articleWithSecondType.setDoi("id://test-article-3");
    articleWithSecondType.setTypes(secondType);
    articleWithSecondType.setDate(new Date());

    Article articleWithSecondType2 = new Article();
    articleWithSecondType2.setDoi("id://test-article-4");
    articleWithSecondType2.setTypes(secondType);
    articleWithSecondType2.setDate(new Date());

    dummyDataStore.store(articleWithBothTypes);
    dummyDataStore.store(articleWithFirstType);
    dummyDataStore.store(articleWithSecondType);
    dummyDataStore.store(articleWithSecondType2);

    List<String> articleList = new ArrayList<String>();
    articleList.add(articleWithBothTypes.getDoi());
    articleList.add(articleWithFirstType.getDoi());
    articleList.add(articleWithSecondType.getDoi());
    articleList.add(articleWithSecondType2.getDoi());

    Issue issue = new Issue();
    issue.setArticleDois(articleList);
    issue.setIssueUri("id://issue1/");
    dummyDataStore.store(issue);

    Volume volume = new Volume();
    volume.setVolumeUri("id://volume.test.one");
    volume.setTitle("volume test one title");
    volume.setDescription("volume test one description");
    volume.setIssues(Arrays.asList(issue));
    dummyDataStore.store(volume);

    Map<String, Integer> numExpectedPerGroup = new HashMap<String, Integer>(2);
    numExpectedPerGroup.put(articleType1, 2);
    numExpectedPerGroup.put(articleType2, 3);

    return new Object[][]{
      { issue.getIssueUri(), numExpectedPerGroup }
    };
  }

  @Test(dataProvider = "articleGroupList")
  public void testGetArticleGroupList(String issueUri, Map<URI, Integer> numExpectedPerGroup) {
    List<TOCArticleGroup> results = browseService.getArticleGrpList(issueUri, DEFAULT_ADMIN_AUTHID);
    assertNotNull(results, "returned null article group list");
    assertEquals(results.size(), numExpectedPerGroup.size(), "returned incorrect number of groups");
    for (TOCArticleGroup articleGroup : results) {
      Integer expectedCount = numExpectedPerGroup.get(articleGroup.getArticleType().getUri().toString());
      assertNotNull(expectedCount,
          "returned group for unexpected article type: " + articleGroup.getArticleType().getUri());
      assertEquals(articleGroup.getCount(), expectedCount.intValue(),
          "returned incorrect number of articles for article group: " + articleGroup);
    }
  }

  @DataProvider(name = "articleGroupListWithIssueId")
  public Object[][] getArticleGroupListWithIssueId() {
    String issueUri = (String) getArticleGroupList()[0][0];
    Map<URI, Integer> numExpectedPerGroup = (Map<URI, Integer>) getArticleGroupList()[0][1];
    return new Object[][]{
        { issueUri, numExpectedPerGroup}
    };
  }

  @Test(dataProvider = "articleGroupListWithIssueId")
  public void testGetArticleGroupListByIssueId(String issueUri, Map<URI, Integer> numExpectedPerGroup) {
    List<TOCArticleGroup> results = browseService.getArticleGrpList(issueUri, DEFAULT_ADMIN_AUTHID);
    assertNotNull(results, "returned null article group list");
    assertEquals(results.size(), numExpectedPerGroup.size(), "returned incorrect number of groups");
    for (TOCArticleGroup articleGroup : results) {
      Integer expectedCount = numExpectedPerGroup.get(articleGroup.getArticleType().getUri().toString());
      assertNotNull(expectedCount,
          "returned group for unexpected article type: " + articleGroup.getArticleType().getUri());
      assertEquals(articleGroup.getCount(), expectedCount.intValue(),
          "returned incorrect number of articles for article group");
    }
  }

  @Test(dataProvider = "articleGroupList",
      dependsOnMethods = {"testGetArticleGroupListByIssueId", "testGetArticleGroupList"},
      alwaysRun = true, ignoreMissingDependencies = true)
  public void testBuildArticleGroups(String issueUri, Map<URI, Integer> notUsedInThisTest) {
    IssueInfo issueInfo = browseService.getIssueInfo(issueUri);
    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issueInfo, DEFAULT_ADMIN_AUTHID);
    List<TOCArticleGroup> builtArticleGroups = browseService.buildArticleGroups(issueInfo,
      articleGroups, DEFAULT_ADMIN_AUTHID);

    assertNotNull(builtArticleGroups, "returned null list of built article groups");
    assertEquals(builtArticleGroups.size(), articleGroups.size(), "returned incorrect number of article groups");
    for (int i = 0; i < builtArticleGroups.size(); i++) {
      TOCArticleGroup builtGroup = builtArticleGroups.get(i);
      TOCArticleGroup group = articleGroups.get(i);
      assertNotNull(builtGroup, "returned null built article group");
      assertEquals(builtGroup.getArticleType(), group.getArticleType(),
          "returned built group with incorrect article type");
      assertEquals(builtGroup.getCount(), group.getCount(), "returned article group with incorrect article count");
    }
  }

  @Test(dataProvider = "articleGroupList",
      dependsOnMethods = {"testGetArticleGroupListByIssueId", "testGetArticleGroupList"},
      alwaysRun = true, ignoreMissingDependencies = true)
  public void testGetArticleGrpListToCsv(String issueUri, Map<URI, Integer> notUsedInThisTest) {
    IssueInfo issueInfo = browseService.getIssueInfo(issueUri);
    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issueInfo, DEFAULT_ADMIN_AUTHID);

    String csv = browseService.articleGrpListToCSV(articleGroups);
    assertNotNull(csv, "returned null csv");
    assertTrue(csv.length() > 0, "returned empty csv");
    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        assertTrue(csv.indexOf(articleInfo.getDoi()) != -1,
            "csv didn't contain expected article id: " + articleInfo.getDoi());
      }
    }

  }

  @DataProvider(name = "latestIssue")
  public Object[][] getLatestIssue() {
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_MONTH, -1);

    Issue olderIssue = new Issue();
    olderIssue.setCreated(yesterday.getTime());
    olderIssue.setIssueUri("info:doi/old.issue");
    dummyDataStore.store(olderIssue);

    Issue newerIssue = new Issue();
    newerIssue.setCreated(new Date());
    newerIssue.setIssueUri("info:doi/new.issue");
    dummyDataStore.store(newerIssue);

    Volume olderVolume = new Volume();
    olderVolume.setCreated(yesterday.getTime());
    olderVolume.setVolumeUri("info:doi/old.volume");
    olderVolume.setIssues(Arrays.asList(olderIssue, newerIssue));
    dummyDataStore.store(olderVolume);

    Volume newerVolume = new Volume();
    newerVolume.setCreated(new Date());
    newerVolume.setVolumeUri("info:doi/new.volume");
    newerVolume.setIssues(Arrays.asList(olderIssue, newerIssue));
    dummyDataStore.store(newerVolume);

    Journal journal = new Journal();
    journal.setVolumes(Arrays.asList(olderVolume, newerVolume));
    journal.setJournalKey("id://journal.one");
    dummyDataStore.store(journal);

    return new Object[][]{
        { journal, newerIssue.getIssueUri() }
    };
  }

  @Test(dataProvider = "latestIssue")
  public void testGetLatestIssueFromLatestVolume(Journal journal, String expectedURI) {
    String result = browseService.getLatestIssueFromLatestVolume(journal);
    assertNotNull(result, "returned null URI");
    assertEquals(result, expectedURI, "returned incorrect URI");
  }
}
