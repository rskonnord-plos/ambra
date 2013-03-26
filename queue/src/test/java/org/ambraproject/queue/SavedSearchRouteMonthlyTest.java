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

import org.ambraproject.models.SavedSearchQuery;
import org.ambraproject.models.UserProfile;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.jvnet.mock_javamail.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import javax.mail.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for the savedSearch camel route
 *
 * @author stumu
 * @author Joe Osowski
 */
@ContextConfiguration
public class SavedSearchRouteMonthlyTest extends SavedSearchRouteBaseTest {
  private static final Logger log = LoggerFactory.getLogger(SavedSearchRouteMonthlyTest.class);

  @Produce(uri = "direct:getsearches")
  protected ProducerTemplate start;

  @DataProvider(name="expectedMonthlyEmails")
  public Object[][] expectedMonthlyEmails() throws Exception {
    setupUsers();
    setupSolr();

    start.sendBody("MONTHLY");

    //WAIT 2.5 seconds for queue jobs to complete
    Thread.sleep(2500);

    //Build up expected emails.  Using email title for predicting contents
    return new Object[][]{
      { "savedSearch0@unittestexample.org", 2, new HashMap() {{
        put("PLOS Search Alert - monthly-0", new String[] { DOI_2, DOI_3, DOI_4 });
        put("PLOS Search Alert - both-0", new String[] { DOI_2, DOI_3, DOI_4 });
      }}
      },
      { "savedSearch1@unittestexample.org", 2, new HashMap() {{
        put("PLOS Search Alert - monthly-1", new String[] { DOI_2, DOI_3, DOI_4 });
        put("PLOS Search Alert - both-1", new String[] { DOI_2, DOI_3, DOI_4 });
      }}
      },
      { "savedSearch2@unittestexample.org", 2, new HashMap() {{
        put("PLOS Search Alert - monthly-2", new String[] { DOI_2, DOI_3, DOI_4 });
        put("PLOS Search Alert - both-2", new String[] { DOI_2, DOI_3, DOI_4 });
      }}
      }
    };
  }

  @Test(dataProvider = "expectedMonthlyEmails")
  @DirtiesContext
  public void expectedMonthlyEmails(String email, int expectedEmails, Map emailContents) throws Exception {
    checkEmail(email, expectedEmails, emailContents);
  }

  @AfterClass
  public void cleanup() {
    //Reset the mailboxes
    log.debug("Clearing mailboxes");

    Mailbox.clearAll();
  }
}
