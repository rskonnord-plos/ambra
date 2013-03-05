/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.queue;

import org.ambraproject.models.SavedSearch;
import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.UserProfile;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.jvnet.mock_javamail.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import javax.mail.Message;
import java.util.List;
import static org.testng.Assert.assertEquals;

/**
 * Unit test for the savedSearch camel route
 *
 * @author stumu
 * @author Joe Osowski
 */
@ContextConfiguration
public class SavedSearchRouteWeeklyTest extends SavedSearchRouteBaseTest {
  private static final Logger log = LoggerFactory.getLogger(SavedSearchRouteMonthlyTest.class);

  @Produce(uri = "direct:getsearches")
  protected ProducerTemplate start;

  @DataProvider(name="expectedWeeklyEmails")
  public Object[][] expectedWeeklyEmails() throws Exception {
    setupUsers();
    setupSolr();

    start.sendBody("WEEKLY");

    //WAIT 10 seconds for queue jobs to complete
    Thread.sleep(30000);

    return new Object[][]{
      { "savedSearch0@unittestexample.org", 1 },
      { "savedSearch1@unittestexample.org", 1 },
      { "savedSearch2@unittestexample.org" , 2 }
    };
  }

  @Test(dataProvider = "expectedWeeklyEmails")
  public void expectedMonthlyEmails(String email, int expectedEmails) throws Exception {
    List<Message> inboxMessages = Mailbox.get(email);

    List<UserProfile> up = dummyDataStore.getAll(UserProfile.class);
    List<SavedSearchQuery> sq = dummyDataStore.getAll(SavedSearchQuery.class);
    List<SavedSearch> ss = dummyDataStore.getAll(SavedSearch.class);

    log.debug("Inbox Size ({}): {} Expected: {}", new Object[] { email, inboxMessages.size(), expectedEmails });

    //assertEquals(inboxMessages.size(), expectedEmails, "Inbox sizes off");

    //TODO: Check message contents

    //Reset the mailboxes
    Mailbox.clearAll();
  }
}
