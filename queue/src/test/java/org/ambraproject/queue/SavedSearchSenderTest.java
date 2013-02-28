package org.ambraproject.queue;

import org.ambraproject.ApplicationException;
import org.ambraproject.search.SavedSearchJob;
import org.ambraproject.search.SavedSearchSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Calendar;

/**
 * Unit test for testing the send mail functionality
 */
public class SavedSearchSenderTest {
  //TODO: Implement

  @Autowired
  protected SavedSearchSender savedSearchSender;

  @DataProvider(name="jobs")
  public Object[][] savedSearchViewData() {
    SavedSearchJob savedSearchJob = new SavedSearchJob(
      new Long(1),
      null,
      null,
      null,
      null,
      null,
      null);

    //savedSearchJob.setSearchHitList();

    return new Object[][] { { savedSearchJob } };
  }

  @Test
  void testSearchSender(SavedSearchJob savedSearchJob) throws ApplicationException
  {
    savedSearchSender.sendSavedSearch(savedSearchJob);

    //I don't test here to check for emails the SavedSearchEmailRoutesTest does that

    //Check here that the database was updated to reflect the messages were sent


  }

}
