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
package org.ambraproject.topazmigration;

import org.hibernate.Hibernate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.*;

import java.util.Arrays;
import java.util.Iterator;

import static org.testng.Assert.*;


/**
 * Test for all subclasses of {@link org.topazproject.ambra.models.Annotation}
 *
 * @author Alex Kudlick Date: Mar 21, 2011
 *         <p/>
 *         org.plos.topazMigration
 */
public class AnnotationMigrationTest extends BaseMigrationTest {

  //The number of annotations (of each type) to test at a time
  private static final int INCREMENT_SIZE = 100;

  /**
   * Helper method for tests to compare all the properties common to Annotea objects
   *
   * @param mysqlAnnotation - the annotation pulled up from MySQL
   * @param topazAnnotation - the annotation pulled up from Topaz
   */
  public static void compareAnnoteaProperties(Annotea<?> mysqlAnnotation, Annotea<?> topazAnnotation) {
    assertMatchingDates(topazAnnotation.getCreated(), mysqlAnnotation.getCreated(),
        "Topaz and Mysql didn't return matching creation dates; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getCreator(), topazAnnotation.getCreator(),
        "Topaz and Mysql didn't return matching creators; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getAnonymousCreator(), topazAnnotation.getAnonymousCreator(),
        "Topaz and Mysql didn't return matching anonymous creators; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getTitle(), topazAnnotation.getTitle(),
        "Topaz and Mysql didn't return matching titles; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getMediator(), topazAnnotation.getMediator(),
        "Topaz and Mysql didn't return matching mediators; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getState(), topazAnnotation.getState(),
        "Topaz and Mysql didn't return matching states; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getType(), topazAnnotation.getType(),
        "Topaz and Mysql didn't return matching types; annotation: " + mysqlAnnotation.getId());
    assertEquals(mysqlAnnotation.getWebType(), topazAnnotation.getWebType(),
        "Topaz and Mysql didn't return matching web types; annotation: " + mysqlAnnotation.getId());
    if (mysqlAnnotation.getBody() == null) {
      assertNull(topazAnnotation.getBody(), "Mysql had null annotation body and topaz didn't;" +
          " annotation: " + mysqlAnnotation.getId());
    } else if (topazAnnotation.getBody() == null) {
      assertNull(mysqlAnnotation.getBody(), "Topaz had null annotation body and mysql didn't;" +
          " annotation: " + mysqlAnnotation.getId() );
    } else {
      compareBodyProperties(mysqlAnnotation, topazAnnotation);
    }

  }

  public static void compareBodyProperties(Annotea<?> mysqlAnnotation, Annotea<?> topazAnnotation) {
    if (AnnotationBlob.class == Hibernate.getClass(mysqlAnnotation.getBody())) {
      AnnotationBlob topazBody = (AnnotationBlob) topazAnnotation.getBody();
      AnnotationBlob mysqlBody = (AnnotationBlob) mysqlAnnotation.getBody();
      assertEquals(mysqlBody.getId(), topazBody.getId(),
          "Topaz and Mysql didn't return the same Id for the Annotation bodies");
      assertEquals(mysqlBody.getCIStatement(), topazBody.getCIStatement(),
          "Topaz and Mysql didn't return the same ciStatement for the Annotation bodies; id: " + mysqlBody.getId());
      assertTrue(Arrays.equals(topazBody.getBody(), mysqlBody.getBody()),
          "Topaz and Mysql didn't return the same bytes for the Annotation bodies");
    } else if (TrackbackContent.class == Hibernate.getClass(mysqlAnnotation.getBody())) {
      TrackbackContent topazBody = (TrackbackContent) topazAnnotation.getBody();
      TrackbackContent mysqlBody = (TrackbackContent) mysqlAnnotation.getBody();
      assertEquals(mysqlBody.getId(), topazBody.getId(),
          "Topaz and Mysql didn't return the same Id for Trackback content");
      assertEquals(mysqlBody.getTitle(), topazBody.getTitle(),
          "Topaz and Mysql didn't return the same Title for Trackback content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getUrl(), topazBody.getUrl(),
          "Topaz and Mysql didn't return the same URL for Trackback content; id: " + mysqlBody.getId());
      assertEquals(topazBody.getBlog_name(), mysqlBody.getBlog_name(),
          "Topaz and Mysql didn't return the same blog_name for Trackback content; id: " + topazBody.getId());
      assertEquals(mysqlBody.getExcerpt(), topazBody.getExcerpt(),
          "Topaz and Mysql didn't return the same excerpt for Trackback content; id: " + mysqlBody.getId());
    } else if (RatingSummaryContent.class == Hibernate.getClass(mysqlAnnotation.getBody())) {
      RatingSummaryContent topazBody = (RatingSummaryContent) topazAnnotation.getBody();
      RatingSummaryContent mysqlBody = (RatingSummaryContent) mysqlAnnotation.getBody();
      assertEquals(mysqlBody.getId(), topazBody.getId(),
          "Topaz and Mysql didn't return the same Id for Rating Summary Content");
      assertEquals(mysqlBody.getInsightNumRatings(), topazBody.getInsightNumRatings(),
          "Topaz and Mysql didn't return the same insightNumRatings for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getInsightTotal(), topazBody.getInsightTotal(),
          "Topaz and Mysql didn't return the same insightTotal for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getReliabilityNumRatings(), topazBody.getReliabilityNumRatings(),
          "Topaz and Mysql didn't return the same reliabilityNumRatings for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getReliabilityTotal(), topazBody.getReliabilityTotal(),
          "Topaz and Mysql didn't return the same reliabilityTotal for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getStyleNumRatings(), topazBody.getStyleNumRatings(),
          "Topaz and Mysql didn't return the same styleNumRatings for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getStyleTotal(), topazBody.getStyleTotal(),
          "Topaz and Mysql didn't return the same styleTotal for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getSingleRatingNumRatings(), topazBody.getSingleRatingNumRatings(),
          "Topaz and Mysql didn't return the same singleRatingNumRatings for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getSingleRatingTotal(), topazBody.getSingleRatingTotal(),
          "Topaz and Mysql didn't return the same singleRatingTotal for Rating Summary Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getNumUsersThatRated(), topazBody.getNumUsersThatRated(),
          "Topaz and Mysql didn't return the same numUsersThatRated for Rating Summary Content; id: " + mysqlBody.getId());
    } else if (RatingContent.class == Hibernate.getClass(mysqlAnnotation.getBody())) {
      RatingContent topazBody = (RatingContent) topazAnnotation.getBody();
      RatingContent mysqlBody = (RatingContent) mysqlAnnotation.getBody();
      assertEquals(mysqlBody.getId(), topazBody.getId(),
          "Topaz and Mysql didn't return the same Id for Rating Content");
      assertEquals(mysqlBody.getInsightValue(), topazBody.getInsightValue(),
          "Topaz and Mysql didn't return the same insightValue for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getReliabilityValue(), topazBody.getReliabilityValue(),
          "Topaz and Mysql didn't return the same reliabilityValue for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getStyleValue(), topazBody.getStyleValue(),
          "Topaz and Mysql didn't return the same styleValue for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getSingleRatingValue(), topazBody.getSingleRatingValue(),
          "Topaz and Mysql didn't return the same singleRatingValue for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getCommentTitle(), topazBody.getCommentTitle(),
          "Topaz and Mysql didn't return the same Comment title for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getCommentValue(), topazBody.getCommentValue(),
          "Topaz and Mysql didn't return the same Comment value for Rating Content; id: " + mysqlBody.getId());
      assertEquals(mysqlBody.getCIStatement(), topazBody.getCIStatement(),
          "Topaz and Mysql didn't return the same ciStatement for Rating Content; id: " + mysqlBody.getId());
    } else if (ReplyBlob.class == Hibernate.getClass(mysqlAnnotation.getBody())) {
      ReplyBlob topazBody = (ReplyBlob) topazAnnotation.getBody();
      ReplyBlob mysqlBody = (ReplyBlob) mysqlAnnotation.getBody();
      assertEquals(mysqlBody.getId(), topazBody.getId(),
          "Topaz and Mysql didn't return the same Id for the Reply Blobs");
      assertEquals(mysqlBody.getCIStatement(), topazBody.getCIStatement(),
          "Topaz and Mysql didn't return the same ciStatement for the Reply Blobs; id: " + mysqlBody.getId());
      assertTrue(Arrays.equals(topazBody.getBody(), mysqlBody.getBody()),
          "Topaz and Mysql didn't return the same bytes for the Reply Blobs; id: " + mysqlBody.getId());
    } else {
      fail("didn't recognize annotation body class: " + Hibernate.getClass(mysqlAnnotation.getBody()));
    }

  }

  @DataProvider(name = "comments")
  public Iterator<Object[]> getComments() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, Comment.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "comments")
  public void diffComments(Comment mysqlComment, Comment topazComment) {
    assertNotNull(topazComment,"Topaz failed to return a(n) comment; check the migration logs for more information");
    topazComment = loadTopazObject(topazComment.getId(), Comment.class);
    assertNotNull(mysqlComment,"Mysql returned null comment for id: " + topazComment.getId());

    compareAnnoteaProperties(mysqlComment, topazComment);
  }

  @DataProvider(name = "minorCorrections")
  public Iterator<Object[]> getMinorCorrections() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, MinorCorrection.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "minorCorrections")
  public void diffMinorCorrections(MinorCorrection mysqlCorrection, MinorCorrection topazCorrection) {
    assertNotNull(topazCorrection,"Topaz failed to return a(n) minor correction; check the migration logs for more information");
    topazCorrection = loadTopazObject(topazCorrection.getId(), MinorCorrection.class);
    assertNotNull(mysqlCorrection,"Mysql returned null minor correction for id: " + topazCorrection.getId());

    compareAnnoteaProperties(mysqlCorrection, topazCorrection);
  }

  @DataProvider(name = "formalCorrections")
  public Iterator<Object[]> getFormalCorrections() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, FormalCorrection.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "formalCorrections")
  public void diffFormalCorrections(FormalCorrection mysqlCorrection, FormalCorrection topazCorrection) {
    assertNotNull(topazCorrection,"Topaz failed to return a(n) formal correction; check the migration logs for more information");
    topazCorrection = loadTopazObject(topazCorrection.getId(), FormalCorrection.class);
    assertNotNull(mysqlCorrection,"Mysql returned null formal correction for id: " + topazCorrection.getId());

    compareAnnoteaProperties(mysqlCorrection, topazCorrection);
    CitationMigrationTest.compareCitations(mysqlCorrection.getBibliographicCitation(), topazCorrection.getBibliographicCitation());
  }

  @DataProvider(name = "retractions")
  public Iterator<Object[]> getRetractions() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, Retraction.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "retractions")
  public void diffRetractions(Retraction mysqlRetraction, Retraction topazRetraction) {
    assertNotNull(topazRetraction,"Topaz failed to return a(n) formal correction; check the migration logs for more information");
    topazRetraction = loadTopazObject(topazRetraction.getId(), Retraction.class);
    assertNotNull(mysqlRetraction,"Mysql returned null formal correction for id: " + topazRetraction.getId());

    compareAnnoteaProperties(mysqlRetraction, topazRetraction);
    CitationMigrationTest.compareCitations(mysqlRetraction.getBibliographicCitation(), topazRetraction.getBibliographicCitation());
  }

  @DataProvider(name = "ratings")
  public Iterator<Object[]> getRatings() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, Rating.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "ratings")
  public void diffRatings(Rating mysqlRating, Rating topazRating) {
    assertNotNull(topazRating,"Topaz failed to return a(n) formal correction; check the migration logs for more information");
    topazRating = loadTopazObject(topazRating.getId(), Rating.class);
    assertNotNull(mysqlRating,"Mysql returned null formal correction for id: " + topazRating.getId());

    compareAnnoteaProperties(mysqlRating, topazRating);
  }

  @DataProvider(name = "ratingSummaries")
  public Iterator<Object[]> getRatingSummaries() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, RatingSummary.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "ratingSummaries")
  public void diffRatingSummaries(RatingSummary mysqlSummary, RatingSummary topazSummary) {
    assertNotNull(topazSummary,"Topaz failed to return a(n) formal correction; check the migration logs for more information");
    topazSummary = loadTopazObject(topazSummary.getId(), RatingSummary.class);
    assertNotNull(mysqlSummary,"Mysql returned null formal correction for id: " + topazSummary.getId());

    compareAnnoteaProperties(mysqlSummary, topazSummary);
  }

  @DataProvider(name = "trackbacks")
  public Iterator<Object[]> getTrackbacks() {

    assertTrue(restartSessions());

    return new MigrationDataIterator(this, Trackback.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "trackbacks")
  public void diffTrackbacks(Trackback mysqlTrackback, Trackback topazTrackback) {
    assertNotNull(topazTrackback,"Topaz failed to return a(n) formal correction; check the migration logs for more information");
    topazTrackback = loadTopazObject(topazTrackback.getId(), Trackback.class);
    assertNotNull(mysqlTrackback,"Mysql returned null formal correction for id: " + topazTrackback.getId());

    compareAnnoteaProperties(mysqlTrackback, topazTrackback);
    assertEquals(mysqlTrackback.getUrl(), topazTrackback.getUrl(),
        "Mysql and topaz didn't return the same Urls; trackback: " + mysqlTrackback.getId());
    assertEquals(topazTrackback.getBlog_name(), mysqlTrackback.getBlog_name(),
        "Mysql and topaz didn't return the same blog_names; trackback: " + topazTrackback.getId());
    assertEquals(mysqlTrackback.getExcerpt(), topazTrackback.getExcerpt(),
        "Mysql and topaz didn't return the same Excerpts; trackback: " + mysqlTrackback.getId());
  }
}
