package org.ambraproject.action.rating;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.AmbraWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.models.Article;
import org.ambraproject.models.Rating;
import org.ambraproject.models.RatingSummary;
import org.ambraproject.models.UserProfile;
import org.ambraproject.views.RatingSummaryView;
import org.ambraproject.views.RatingView;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * @author Alex Kudlick
 *         Date: 6/6/12
 */
public class GetArticleRatingsActionTest extends AmbraWebTest {

  @Autowired
  protected GetArticleRatingsAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider
  public Object[][] ratings() {
    UserProfile creator = new UserProfile(
        "email@RatingAction.org",
        "displayName@RatingAction", "pass");
    dummyDataStore.store(creator);

    //Article with ratings that have insight, reliability and style
    Article multiRatingArticle = new Article("id:test-article-for-GetArticleRatingsAction-multi");
    dummyDataStore.store(multiRatingArticle);

    Rating multiRating1 = new Rating(creator, multiRatingArticle.getID());
    multiRating1.setInsight(2);
    multiRating1.setReliability(4);
    multiRating1.setStyle(3);
    dummyDataStore.store(multiRating1);
    Rating multiRating2 = new Rating(creator, multiRatingArticle.getID());
    multiRating2.setInsight(1);
    multiRating2.setReliability(0);
    multiRating2.setStyle(2);
    dummyDataStore.store(multiRating2);
    Rating multiRating3 = new Rating(creator, multiRatingArticle.getID());
    multiRating3.setInsight(5);
    multiRating3.setReliability(5);
    multiRating3.setStyle(5);
    dummyDataStore.store(multiRating3);
    RatingView[] multiRatings = {new RatingView(multiRating1), new RatingView(multiRating2), new RatingView(multiRating3)};

    RatingSummary multiSummary = new RatingSummary(multiRatingArticle.getID());
    multiSummary.setInsightNumRatings(3);
    multiSummary.setInsightTotal(8);
    multiSummary.setReliabilityNumRatings(3);
    multiSummary.setReliabilityTotal(9);
    multiSummary.setStyleNumRatings(3);
    multiSummary.setStyleTotal(10);
    dummyDataStore.store(multiSummary);
    RatingSummaryView multiSummaryView = new RatingSummaryView(multiSummary);

    //Article with ratings that have a single rating
    Article singleRatingArticle = new Article("id:test-article-for-GetArticleRatingsAction-single");
    dummyDataStore.store(singleRatingArticle);

    Rating singleRating1 = new Rating(creator, singleRatingArticle.getID());
    singleRating1.setSingleRating(1);
    dummyDataStore.store(singleRating1);
    Rating singleRating2 = new Rating(creator, singleRatingArticle.getID());
    singleRating2.setSingleRating(5);
    dummyDataStore.store(singleRating2);
    Rating singleRating3 = new Rating(creator, singleRatingArticle.getID());
    singleRating3.setSingleRating(2);
    dummyDataStore.store(singleRating3);
    Rating singleRating4 = new Rating(creator, singleRatingArticle.getID());
    singleRating4.setSingleRating(4);
    dummyDataStore.store(singleRating4);

    RatingView[] singleRatings =  new RatingView[]{
        new RatingView(singleRating1), new RatingView(singleRating2),
        new RatingView(singleRating3), new RatingView(singleRating4)};


    RatingSummary singleSummary = new RatingSummary(singleRatingArticle.getID());
    singleSummary.setSingleRatingNumRatings(4);
    singleSummary.setSingleRatingTotal(12);
    dummyDataStore.store(singleSummary);
    RatingSummaryView singleSummaryView = new RatingSummaryView(singleSummary);

    return new Object[][]{
        {multiRatingArticle, multiSummaryView, multiRatings},
        {singleRatingArticle, singleSummaryView, singleRatings}
    };
  }

  @Test(dataProvider = "ratings")
  public void testExecute(Article article, RatingSummaryView ratingAverage, RatingView[] ratings) throws Exception {
    action.setArticleURI(article.getDoi());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action returned field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    assertEquals(action.getArticleDescription(), article.getDescription(),
        "Action had incorrect article description");
    assertEquals(action.getArticleTitle(), article.getTitle(), "Action had incorrect title");

    assertEquals(action.getAverageRatings(), ratingAverage, "Action had incorrect average rating");
    assertEqualsNoOrder(action.getArticleRatings().toArray(), ratings,
        "Action had incorrect article ratings");

  }


}
