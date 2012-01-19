package org.topazproject.ambra.models;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.assertEquals;

import java.util.Date;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * @author Scott Sterling
 */
public class SyndicationTest {

  private Syndication syn;

  @BeforeClass
  public void setUp() throws URISyntaxException {
    this.syn = new Syndication("fakeArticleIdInitial", "testTargetInitial");
  }

  @Test
  public void testMakeSyndicationId() {
    String newSynId = Syndication.makeSyndicationId("fakeArticleId", "testTarget");
    assertEquals(newSynId, "fakeArticleId/testTarget");
  }

  @Test
  public void testId() throws URISyntaxException {
    syn.setId(new URI("fakeArticleId/testTarget"));
    assertEquals(syn.getId().toString(), "fakeArticleId/testTarget");
  }

  @Test
  public void testArticleId() throws URISyntaxException {
    syn.setArticleId(new URI("fakeArticleId"));
    assertEquals(syn.getArticleId().toString(), "fakeArticleId");
  }

  @Test
  public void testStatus() {
    syn.setStatus(Syndication.STATUS_IN_PROGRESS);
    assertEquals(syn.getStatus(), Syndication.STATUS_IN_PROGRESS);
  }

  @Test
  public void testStatusTimestamp() {
    Date dateStatus = new Date();
    syn.setStatusTimestamp(dateStatus);
    assertEquals(syn.getStatusTimestamp(), dateStatus);
  }

  @Test
  public void testSubmissionCount() {
    syn.setSubmissionCount(437);
    assertEquals(syn.getSubmissionCount(), 437);
  }

  @Test
  public void testSubmitTimestamp() {
    Date dateSubmit = new Date();
    syn.setSubmitTimestamp(dateSubmit);
    assertEquals(syn.getSubmitTimestamp(), dateSubmit);
  }

  @Test
  public void testTarget() {
    syn.setTarget("testTarget");
    assertEquals(syn.getTarget(), "testTarget");
  }

  @Test
  public void testErrorMessage() {
    syn.setErrorMessage("This is a Test error message.");
    assertEquals(syn.getErrorMessage(), "This is a Test error message.");
  }

  @Test
  public void testContructor1() throws URISyntaxException {
    Syndication synShort = new Syndication("testArticleId", "testTarget");

    assertEquals(synShort.getId(), new URI("testArticleId/testTarget"));
    assertEquals(synShort.getStatus(), Syndication.STATUS_PENDING);
    assertEquals(synShort.getTarget(), "testTarget");
    assertEquals(synShort.getSubmissionCount(), 0);
    assertEquals(synShort.getSubmitTimestamp(), null);
    assertEquals(synShort.getErrorMessage(), null);
  }

  @Test
  public void testContructor2() throws URISyntaxException {
    Date submitDate = new Date();
    Date statusDate = new Date(submitDate.getTime() + 10000L);
    Syndication synFull = new Syndication("testArticleId", Syndication.STATUS_SUCCESS,
        "testTargetTwo", 592, statusDate, submitDate, "This is another test Error Message.");

    assertEquals(synFull.getId(), new URI("testArticleId/testTargetTwo"));
    assertEquals(synFull.getStatus(), Syndication.STATUS_SUCCESS);
    assertEquals(synFull.getTarget(), "testTargetTwo");
    assertEquals(synFull.getSubmissionCount(), 592);
    assertEquals(synFull.getStatusTimestamp(), statusDate);
    assertEquals(synFull.getSubmitTimestamp(), submitDate);
    assertEquals(synFull.getErrorMessage(), "This is another test Error Message.");
  }
}