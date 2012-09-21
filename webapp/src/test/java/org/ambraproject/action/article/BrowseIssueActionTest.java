package org.ambraproject.action.article;

import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Volume;
import org.ambraproject.views.IssueInfo;
import org.ambraproject.web.VirtualJournalContext;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 9/7/12
 */
public class BrowseIssueActionTest extends AmbraWebTest {

  @Autowired
  protected BrowseIssueAction action;
  private final String journalKey = "BrowseIssueActionTestJournalKey";

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }


  @Override
  protected void doModifyRequest(Map<String, Object> request) {
    //set the current journal
    VirtualJournalContext virtualJournalContext = new VirtualJournalContext(journalKey,
        defaultJournal.getJournalKey(), "http", 80, "localhost", "/", Collections.EMPTY_LIST);
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, virtualJournalContext);
  }



  @DataProvider(name = "journal")
  public Object[][] getJournal() {
    Journal journal = new Journal(journalKey);
    journal.setVolumes(new ArrayList<Volume>(3));

    for (String volumeName : new String[] {"OldVolume", "NewVolume"}) {
      Volume volume = new Volume("id:BrowseIssueAction" + volumeName);
      volume.setTitle(volumeName);
      volume.setDescription("A plane crash causes the firm to go after the account for the airline involved " +
          "and drop the smaller airline they have an account with. Peggy pays a visit to her mother and sister, " +
          "who are now guardians of a child.");
      volume.setIssues(new ArrayList<Issue>(4));
      for (int i = 1; i <= 3; i++) {
        Issue issue = new Issue("id:" + volumeName + "Issue" + i);
        issue.setTitle(volumeName + " Issue " + i);
        issue.setDescription(volumeName + " Issue " + i);
        issue.setArticleDois(Arrays.asList(
            volumeName + "Issue" + i + "Article1",
            volumeName + "Issue" + i + "Article2",
            volumeName + "Issue" + i + "Article3"
        ));
        volume.getIssues().add(issue);
      }
      journal.getVolumes().add(volume);
    }

    journal.setCurrentIssue(journal.getVolumes().get(1).getIssues().get(0));
    Long id = Long.valueOf(dummyDataStore.store(journal));

    return new Object[][]{
        {dummyDataStore.get(Journal.class, id)}
    };
  }

  @AfterMethod
  public void clearIssue() {
    action.setIssue(null);
  }

  @Test(dataProvider = "journal")
  public void testBrowseCurrentIssue(Journal journal) throws Exception {
    assertEquals(action.execute(), BaseActionSupport.SUCCESS, "Action didn't return success");
    IssueInfo issueInfo = action.getIssueInfo();
    assertNotNull(issueInfo, "action had null issue info");
    assertEquals(issueInfo.getArticleUriList(), journal.getCurrentIssue().getArticleDois(),
        "action's issue had incorrect article dois");
    assertEquals(issueInfo.getIssueURI(), journal.getCurrentIssue().getIssueUri(),
        "Action's issue had incorrect issue uri");
  }

  @Test(dataProvider = "journal")
  public void testBrowseLatestIssueFromLatestVolume(Journal journal) {
    //set journal to have a null currentIssue
    Issue originalIssue = journal.getCurrentIssue();
    journal.setCurrentIssue(null);
    dummyDataStore.update(journal);

    try {
      assertEquals(action.execute(), BaseActionSupport.SUCCESS, "Action didn't return success");
      IssueInfo issueInfo = action.getIssueInfo();
      assertNotNull(issueInfo, "action had null issue info");
      //Load up latest volume w/ dummy datastore to defeat lazy-loaded issues
      Long volumeId = journal.getVolumes().get(journal.getVolumes().size() - 1).getID();
      Volume latestVolume = dummyDataStore.get(Volume.class, volumeId);
      Issue latestIssue = latestVolume.getIssues().get(latestVolume.getIssues().size() - 1);

      assertEquals(issueInfo.getIssueURI(), latestIssue.getIssueUri(), "action had incorrect issue uri");
      assertEquals(issueInfo.getDescription(), latestIssue.getDescription(), "action had incorrect issue description");
      assertEquals(action.getVolumeInfo().getVolumeUri(), latestVolume.getVolumeUri(), "action had incorrect volume");
    } finally {
      //reset state
      journal.setCurrentIssue(originalIssue);
      dummyDataStore.update(journal);
    }
  }
}
