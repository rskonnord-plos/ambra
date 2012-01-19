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

package org.topazproject.ambra.article.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.BaseTest;
import org.topazproject.ambra.article.action.TOCArticleGroup;
import org.topazproject.ambra.model.IssueInfo;
import org.topazproject.ambra.model.VolumeInfo;
import org.topazproject.ambra.model.article.ArticleInfo;
import org.topazproject.ambra.model.article.ArticleType;
import org.topazproject.ambra.models.*;

import java.net.URI;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Test for methods of {@link BrowseService} that don't use solr.  Working directory for the test should be set to
 * either the new hope home directory or the ambra webapp home
 *
 * @author Alex Kudlick Date: 5/16/11
 *         <p/>
 *         org.topazproject.ambra.testutils
 */
public class BrowseServiceTest extends BaseTest {

  @Autowired
  protected BrowseService browseService;

  private URI articleType1 = URI.create("id://test-article-type-1");
  private URI articleType2 = URI.create("id://test-article-type-2");

  @BeforeClass
  public void addArticleTypes() {
    ArticleType.addArticleType(
        articleType1,        //type uri
        "code",              //code
        "Research Article",  //heading
        "Research Articles"  //plural heading
    );
    ArticleType.addArticleType(
        articleType2,    //type uri
        "code2",         //code
        "Test Article",  //heading
        "Test Articles"  //plural heading
    );
  }

  @DataProvider(name = "issueIds")
  public Object[][] issueIds() {
    Article imageArticle = new Article();
    imageArticle.setId(URI.create("id://test-image"));
    DublinCore dc = new DublinCore();
    dc.setIdentifier("id://test-image");
    dummyDataStore.store(dc);
    imageArticle.setDublinCore(dc);
    dummyDataStore.store(imageArticle);

    Issue testIssue = new Issue();
    testIssue.setDisplayName("Display name of test issue number 1");
    testIssue.setImage(imageArticle.getId());
    testIssue.setRespectOrder(true);
    URI issueId = URI.create(dummyDataStore.store(testIssue));

    return new Object[][]{
        {issueId, testIssue}
    };
  }

  @Test(dataProvider = "issueIds")
  public void testGetIssueInfo(URI issueDoi, Issue expectedIssue) {
    IssueInfo issueInfo = browseService.getIssueInfo(issueDoi);
    assertNotNull(issueInfo, "returned null issue info");
    assertEquals(issueInfo.getId(), issueDoi, "returned issue info with incorrect id");
    assertEquals(issueInfo.getDisplayName(), expectedIssue.getDisplayName(),
        "returned issue info with incorrect display name");
    assertEquals(issueInfo.getImageArticle(), expectedIssue.getImage(),
        "returned issue info with incorrect issue uri");
  }

  @DataProvider(name = "issueArticles")
  public Object[][] getIssueArticles() {
    Article article1 = new Article();
    article1.setId(URI.create("id:test-article-for-issue-list1"));
    DublinCore dc1 = new DublinCore();
    dc1.setIdentifier("id:test-article-for-issue-list1");
    dummyDataStore.store(dc1);
    article1.setDublinCore(dc1);
    
    Article article2 = new Article();
    article2.setId(URI.create("id:test-article-for-issue-list2"));
    DublinCore dc2 = new DublinCore();
    dc2.setIdentifier("id:test-article-for-issue-list2");
    dummyDataStore.store(dc2);
    article2.setDublinCore(dc2);

    Article article3 = new Article();
    article3.setId(URI.create("id:test-article-for-issue-list3"));
    DublinCore dc3 = new DublinCore();
    dc3.setIdentifier("id:test-article-for-issue-list3");
    dummyDataStore.store(dc3);
    article3.setDublinCore(dc3);

    List<URI> articleIds = new ArrayList<URI>(3);
    articleIds.add(URI.create(dummyDataStore.store(article1)));
    articleIds.add(URI.create(dummyDataStore.store(article2)));
    articleIds.add(URI.create(dummyDataStore.store(article3)));

    Issue issue = new Issue();
    issue.setArticleList(articleIds);
    issue.setId(URI.create(dummyDataStore.store(issue)));

    return new Object[][]{
        {issue, articleIds}
    };
  }

  @Test(dataProvider = "issueArticles")
  public void testGetArticleList(Issue issue, List<URI> expectedArticleURIs) {
    List<URI> results = browseService.getArticleList(issue);
    assertNotNull(results, "returned null list of article ids");
    assertEquals(results.size(), expectedArticleURIs.size(), "returned incorrect number of article ids");
    assertEqualsNoOrder(results.toArray(), expectedArticleURIs.toArray(), "returned incorrect article ids");
  }

  @Test(dataProvider = "issueArticles")
  public void testGetArticleInfosForIssue(Issue issue, List<URI> expectedArticleURIs) {
    List<ArticleInfo> results = browseService.getArticleInfosForIssue(issue.getId(),DEFAULT_ADMIN_AUTHID);
    assertNotNull(results, "returned null list of article infos");
    assertEquals(results.size(), expectedArticleURIs.size(), "returned incorrect number of results");
    List<URI> actualArticleURIs = new ArrayList<URI>(results.size());
    for (ArticleInfo articleInfo : results) {
      actualArticleURIs.add(articleInfo.getId());
    }
    assertEqualsNoOrder(actualArticleURIs.toArray(), expectedArticleURIs.toArray(),
        "returned incorrect list of articles");
  }


  @DataProvider(name = "volumes")
  public Object[][] getVolumes() {
    Article imageArticle1 = new Article();
    imageArticle1.setId(URI.create("id://volume-1-image"));
    DublinCore dc1 = new DublinCore();
    dc1.setIdentifier("id://volume-1-image");
    dummyDataStore.store(dc1);
    imageArticle1.setDublinCore(dc1);
    dummyDataStore.store(imageArticle1);

    Article imageArticle2 = new Article();
    imageArticle2.setId(URI.create("id://volume-2-image"));
    DublinCore dc2 = new DublinCore();
    dc2.setIdentifier("id://volume-2-image");
    dummyDataStore.store(dc2);
    imageArticle2.setDublinCore(dc2);
    dummyDataStore.store(imageArticle2);

    Volume volume1 = new Volume();
    volume1.setDisplayName("Volume 1");
    volume1.setImage(imageArticle1.getId());
    URI volume1Id = URI.create(dummyDataStore.store(volume1));

    Volume volume2 = new Volume();
    volume2.setDisplayName("Volume 2");
    volume2.setImage(imageArticle2.getId());
    URI volume2Id = URI.create(dummyDataStore.store(volume2));

    List<URI> volumeIds = new ArrayList<URI>();
    volumeIds.add(volume1Id);
    volumeIds.add(volume2Id);

    Journal journal = new Journal();
    journal.setKey("test-journal-key");
    journal.setVolumes(volumeIds);
    dummyDataStore.store(journal);

    return new Object[][]{
        {volume1Id, journal.getKey(), volume1},
        {volume2Id, journal.getKey(), volume2}
    };
  }

  @Test(dataProvider = "volumes")
  public void testGetVolumeInfo(URI volumeId, String journalKey, Volume expectedVolume) {
    VolumeInfo volumeInfo = browseService.getVolumeInfo(volumeId, journalKey);
    assertNotNull(volumeInfo, "returned null volume info");
    assertEquals(volumeInfo.getId(), volumeId, "returned volume info with incorrect id");
    assertEquals(volumeInfo.getImageArticle(), expectedVolume.getImage(),
        "returned volume with incorrect image article");
    assertEquals(volumeInfo.getDisplayName(), expectedVolume.getDisplayName(),
        "returned volume info with incorrect display name");
  }

  @DataProvider(name = "article")
  public Object[][] getArticle() {
    Article article = new Article();
    article.setId(URI.create("id://test-article-47"));
    DublinCore dublinCore = new DublinCore();
    dublinCore.setIdentifier(article.getId().toString());
    dublinCore.setTitle("test title for article info");
    dublinCore.setDate(new Date());
    dublinCore.setDescription("test, test, test, this is a test");

    Citation bibCitation = new Citation();
    dublinCore.setBibliographicCitation(bibCitation);
    //You need a bib citation to load up the authors
    dummyDataStore.store(bibCitation);
    dummyDataStore.store(dublinCore);
    article.setDublinCore(dublinCore);

    List<String> authorNames = new ArrayList<String>(2);
    List<ArticleContributor> authors = new ArrayList<ArticleContributor>(2);
    ArticleContributor author1 = new ArticleContributor();
    author1.setFullName("Some fake author");
    author1.setIsAuthor(true);
    author1.setId(URI.create(dummyDataStore.store(author1)));
    authors.add(author1);

    ArticleContributor author2 = new ArticleContributor();
    author2.setFullName("Michael Eisen");
    author2.setIsAuthor(true);
    author2.setId(URI.create(dummyDataStore.store(author2)));
    authors.add(author2);
    
    article.setAuthors(authors);
    dummyDataStore.store(article);
    authorNames.add(author1.getFullName());
    authorNames.add(author2.getFullName());
    return new Object[][]{
        {article.getId(), article, authorNames}
    };
  }


  @Test(dataProvider = "article")
  public void testGetArticleInfo(URI id, Article expectedArticle, List<String> expectedAuthors) {
    ArticleInfo result = browseService.getArticleInfo(id, DEFAULT_ADMIN_AUTHID);
    assertNotNull(result, "returned null article info");
    assertEquals(result.getId(), id, "returned article info with incorrect id");
    assertEquals(result.getTitle(), expectedArticle.getDublinCore().getTitle(),
        "returned article info with incorrect title");
    assertEquals(result.getDate(), expectedArticle.getDublinCore().getDate(),
        "returned article info with incorrect date");
    assertEquals(result.getDescription(), expectedArticle.getDublinCore().getDescription());
    assertNotNull(result.getAuthors(),"returned null list of authors");
    assertEqualsNoOrder(result.getAuthors().toArray(),expectedAuthors.toArray(),"had incorrect author names");

  }

  @DataProvider(name = "volumeList")
  public Object[][] getVolumeList() {
    Volume volume1 = new Volume();
    volume1.setDisplayName("Volume 1");
    volume1.setImage(URI.create("id://volume-1-image"));
    URI volume1Id = URI.create(dummyDataStore.store(volume1));

    Volume volume2 = new Volume();
    volume2.setDisplayName("Volume 2");
    volume2.setImage(URI.create("id://volume-2-image"));
    URI volume2Id = URI.create(dummyDataStore.store(volume2));

    List<URI> volumeIds = new ArrayList<URI>();
    volumeIds.add(volume1Id);
    volumeIds.add(volume2Id);

    Journal journal = new Journal();
    journal.setKey("test-journal");
    journal.setVolumes(volumeIds);

    return new Object[][]{
        {journal, volumeIds}
    };
  }

  @Test(dataProvider = "volumeList")
  public void testGetVolumeInfosForJournal(Journal journal, List<URI> expectedIds) {
    List<VolumeInfo> volumeInfos = browseService.getVolumeInfosForJournal(journal);
    assertNotNull(volumeInfos, "returned null list of volume infos");
    assertEquals(volumeInfos.size(), expectedIds.size(), "returned incorrect number of volume infos");
    List<URI> actualIds = new ArrayList<URI>(volumeInfos.size());
    for (VolumeInfo volumeInfo : volumeInfos) {
      actualIds.add(volumeInfo.getId());
    }
    assertEqualsNoOrder(actualIds.toArray(), expectedIds.toArray(), "Didn't return expected volumes");
  }

  @DataProvider(name = "articleGroupList")
  public Object[][] getArticleGroupList() {
    Set<URI> bothTypes = new HashSet<URI>(2);
    bothTypes.add(articleType1);
    bothTypes.add(articleType2);
    Set<URI> firstType = new HashSet<URI>(1);
    firstType.add(articleType1);
    Set<URI> secondType = new HashSet<URI>(1);
    secondType.add(articleType2);

    Article articleWithBothTypes = new Article();
    articleWithBothTypes.setId(URI.create("id://article-1"));
    articleWithBothTypes.setArticleType(bothTypes);
    DublinCore dc1 = new DublinCore();
    dc1.setDate(new Date());
    dc1.setIdentifier(articleWithBothTypes.getId().toString());
    dummyDataStore.store(dc1);
    articleWithBothTypes.setDublinCore(dc1);

    Article articleWithFirstType = new Article();
    articleWithFirstType.setId(URI.create("id://test-article2"));
    articleWithFirstType.setArticleType(firstType);
    DublinCore dc2 = new DublinCore();
    dc2.setDate(new Date());
    dc2.setIdentifier(articleWithFirstType.getId().toString());
    dummyDataStore.store(dc2);
    articleWithFirstType.setDublinCore(dc2);

    Article articleWithSecondType = new Article();
    articleWithSecondType.setId(URI.create("id://test-article-3"));
    articleWithSecondType.setArticleType(secondType);
    DublinCore dc3 = new DublinCore();
    dc3.setDate(new Date());
    dc3.setIdentifier(articleWithSecondType.getId().toString());
    dummyDataStore.store(dc3);
    articleWithSecondType.setDublinCore(dc3);

    Article articleWithSecondType2 = new Article();
    articleWithSecondType2.setId(URI.create("id://test-article-4"));
    articleWithSecondType2.setArticleType(secondType);
    DublinCore dc4 = new DublinCore();
    dc4.setDate(new Date());
    dc4.setIdentifier(articleWithSecondType2.getId().toString());
    dummyDataStore.store(dc4);
    articleWithSecondType2.setDublinCore(dc4);

    List<URI> articleList = new ArrayList<URI>();
    articleList.add(URI.create(dummyDataStore.store(articleWithBothTypes)));
    articleList.add(URI.create(dummyDataStore.store(articleWithFirstType)));
    articleList.add(URI.create(dummyDataStore.store(articleWithSecondType)));
    articleList.add(URI.create(dummyDataStore.store(articleWithSecondType2)));

    Issue issue = new Issue();
    issue.setArticleList(articleList);
    issue.setSimpleCollection(articleList);
    issue.setId(URI.create(dummyDataStore.store(issue)));
    Map<URI, Integer> numExpectedPerGroup = new HashMap<URI, Integer>(2);
    numExpectedPerGroup.put(articleType1, 2);
    numExpectedPerGroup.put(articleType2, 3);

    return new Object[][]{
        {issue, numExpectedPerGroup}
    };
  }

  @Test(dataProvider = "articleGroupList")
  public void testGetArticleGroupList(Issue issue, Map<URI, Integer> numExpectedPerGroup) {
    List<TOCArticleGroup> results = browseService.getArticleGrpList(issue, DEFAULT_ADMIN_AUTHID);
    assertNotNull(results, "returned null article group list");
    assertEquals(results.size(), numExpectedPerGroup.size(), "returned incorrect number of groups");
    for (TOCArticleGroup articleGroup : results) {
      Integer expectedCount = numExpectedPerGroup.get(articleGroup.getArticleType().getUri());
      assertNotNull(expectedCount,
          "returned group for unexpected article type: " + articleGroup.getArticleType().getUri());
      assertEquals(articleGroup.getCount(), expectedCount.intValue(),
          "returned incorrect number of articles for article group: " + articleGroup);
    }
  }

  @DataProvider(name = "articleGroupListWithIssueId")
  public Object[][] getArticleGroupListWithIssueId() {
    Issue issue = (Issue) getArticleGroupList()[0][0];
    Map<URI, Integer> numExpectedPerGroup = (Map<URI, Integer>) getArticleGroupList()[0][1];
    return new Object[][]{
        {issue.getId(), numExpectedPerGroup}
    };
  }

  @Test(dataProvider = "articleGroupListWithIssueId")
  public void testGetArticleGroupListByIssueId(URI issueId, Map<URI, Integer> numExpectedPerGroup) {
    List<TOCArticleGroup> results = browseService.getArticleGrpList(issueId, DEFAULT_ADMIN_AUTHID);
    assertNotNull(results, "returned null article group list");
    assertEquals(results.size(), numExpectedPerGroup.size(), "returned incorrect number of groups");
    for (TOCArticleGroup articleGroup : results) {
      Integer expectedCount = numExpectedPerGroup.get(articleGroup.getArticleType().getUri());
      assertNotNull(expectedCount,
          "returned group for unexpected article type: " + articleGroup.getArticleType().getUri());
      assertEquals(articleGroup.getCount(), expectedCount.intValue(),
          "returned incorrect number of articles for article group");
    }
  }

  @Test(dataProvider = "articleGroupList",
      dependsOnMethods = {"testGetArticleGroupListByIssueId", "testGetArticleGroupList"},
      alwaysRun = true, ignoreMissingDependencies = true)
  public void testBuildArticleGroups(Issue issue, Map<URI, Integer> notUsedInThisTest) {
    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issue, DEFAULT_ADMIN_AUTHID);

    List<TOCArticleGroup> builtArticleGroups = browseService.buildArticleGroups(issue, articleGroups, DEFAULT_ADMIN_AUTHID);

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
  public void testGetArticleGrpListToCsv(Issue issue, Map<URI, Integer> notUsedInThisTest) {
    List<TOCArticleGroup> articleGroups = browseService.getArticleGrpList(issue, DEFAULT_ADMIN_AUTHID);

    String csv = browseService.articleGrpListToCSV(articleGroups);
    assertNotNull(csv, "returned null csv");
    assertTrue(csv.length() > 0, "returned empty csv");
    for (TOCArticleGroup articleGroup : articleGroups) {
      for (ArticleInfo articleInfo : articleGroup.getArticles()) {
        assertTrue(csv.indexOf(articleInfo.getId().toString()) != -1,
            "csv didn't contain expected article id: " + articleInfo.getId());
      }
    }

  }

  @DataProvider(name = "correctionMap")
  public Object[][] getCorrectionMap() {
    Issue issue = (Issue) getArticleGroupList()[0][0];
    List<URI> articleIds = issue.getArticleList();

    FormalCorrection correction1 = new FormalCorrection();
    correction1.setAnnotates(articleIds.get(0));
    FormalCorrection correction2 = new FormalCorrection();
    correction2.setAnnotates(articleIds.get(0));
    FormalCorrection correction3 = new FormalCorrection();
    correction3.setAnnotates(articleIds.get(1));

    List<URI> correctionIds = new ArrayList<URI>();
    correctionIds.add(URI.create(dummyDataStore.store(correction1)));
    correctionIds.add(URI.create(dummyDataStore.store(correction2)));
    correctionIds.add(URI.create(dummyDataStore.store(correction3)));

    return new Object[][]{
        {browseService.getArticleGrpList(issue, DEFAULT_ADMIN_AUTHID), correctionIds}
    };
  }

  @Test(dataProvider = "correctionMap",
      dependsOnMethods = {"testGetArticleGroupListByIssueId", "testGetArticleGroupList"},
      alwaysRun = true, ignoreMissingDependencies = true)
  public void testGetCorrectionMap(List<TOCArticleGroup> articleGroups, List<URI> expectedCorrections) {
    Map<URI, FormalCorrection> results = browseService.getCorrectionMap(articleGroups);
    assertNotNull(results, "returned null correction map");
    assertEquals(results.size(), expectedCorrections.size(), "returned incorrect number of corrections");
    for (URI id : expectedCorrections) {
      assertTrue(results.containsKey(id), "didn't return entry for id: " + id);
      assertNotNull(results.get(id), "returned null correction for id: " + id);
    }

  }

  @DataProvider(name = "retractionMap")
  public Object[][] getRetractionMap() {
    Issue issue = (Issue) getArticleGroupList()[0][0];
    List<URI> articleIds = issue.getArticleList();

    Retraction retraction1 = new Retraction();
    retraction1.setAnnotates(articleIds.get(0));
    Retraction retraction2 = new Retraction();
    retraction2.setAnnotates(articleIds.get(0));
    Retraction retraction3 = new Retraction();
    retraction3.setAnnotates(articleIds.get(1));

    List<URI> retractionIds = new ArrayList<URI>();
    retractionIds.add(URI.create(dummyDataStore.store(retraction1)));
    retractionIds.add(URI.create(dummyDataStore.store(retraction2)));
    retractionIds.add(URI.create(dummyDataStore.store(retraction3)));

    return new Object[][]{
        {browseService.getArticleGrpList(issue, DEFAULT_ADMIN_AUTHID), retractionIds}
    };
  }

  @Test(dataProvider = "retractionMap",
      dependsOnMethods = {"testGetArticleGroupListByIssueId", "testGetArticleGroupList"},
      alwaysRun = true, ignoreMissingDependencies = true)
  public void testGetRetractionMap(List<TOCArticleGroup> articleGroups, List<URI> expectedRetractions) {
    Map<URI, Retraction> results = browseService.getRetractionMap(articleGroups);
    assertNotNull(results, "returned null correction map");
    assertEquals(results.size(), expectedRetractions.size(), "returned incorrect number of corrections");
    for (URI id : expectedRetractions) {
      assertTrue(results.containsKey(id), "didn't return entry for id: " + id);
      assertNotNull(results.get(id), "returned null correction for id: " + id);
    }

  }

  @DataProvider(name = "latestIssue")
  public Object[][] getLatestIssue() {
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_MONTH, -1);

    DublinCore olderIssueDublinCore = new DublinCore();
    olderIssueDublinCore.setDate(yesterday.getTime());
    olderIssueDublinCore.setIdentifier("older dublin core for issue");
    dummyDataStore.store(olderIssueDublinCore);
    DublinCore newerIssueDublinCore = new DublinCore();
    newerIssueDublinCore.setDate(new Date());
    newerIssueDublinCore.setIdentifier("newer dublin core for issue");
    dummyDataStore.store(newerIssueDublinCore);

    DublinCore olderVolumeDublinCore = new DublinCore();
    olderVolumeDublinCore.setDate(yesterday.getTime());
    olderVolumeDublinCore.setIdentifier("older dublin core for volume");
    dummyDataStore.store(olderVolumeDublinCore);
    DublinCore newerVolumeDublinCore = new DublinCore();
    newerVolumeDublinCore.setDate(new Date());
    newerVolumeDublinCore.setIdentifier("newer dublin core for volume");
    dummyDataStore.store(newerVolumeDublinCore);

    Issue olderIssue = new Issue();
    olderIssue.setDublinCore(olderIssueDublinCore);
    Issue newerIssue = new Issue();
    newerIssue.setDublinCore(newerIssueDublinCore);

    List<URI> issues = new ArrayList<URI>(2);
    issues.add(URI.create(dummyDataStore.store(olderIssue)));
    issues.add(URI.create(dummyDataStore.store(newerIssue)));

    Volume olderVolume = new Volume();
    olderVolume.setDublinCore(olderVolumeDublinCore);
    olderVolume.setIssueList(issues);
    Volume newerVolume = new Volume();
    newerVolume.setDublinCore(newerVolumeDublinCore);
    newerVolume.setIssueList(issues);

    List<URI> volumes = new ArrayList<URI>(2);
    volumes.add(URI.create(dummyDataStore.store(newerVolume)));
    volumes.add(URI.create(dummyDataStore.store(olderVolume)));

    Journal journal = new Journal();
    journal.setVolumes(volumes);
    journal.setId(URI.create(dummyDataStore.store(journal)));

    return new Object[][]{
        {journal, newerIssue.getId()}
    };
  }

  @Test(dataProvider = "latestIssue")
  public void testGetLatestIssueFromLatestVolume(Journal journal, URI expectedURI) {
    URI result = browseService.getLatestIssueFromLatestVolume(journal);
    assertNotNull(result, "returned null URI");
    assertEquals(result, expectedURI, "returned incorrect URI");
  }


}
