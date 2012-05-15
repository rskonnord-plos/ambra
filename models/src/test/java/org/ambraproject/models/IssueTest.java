/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.models;

import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class IssueTest extends BaseHibernateTest {

  @Test
  public void testSaveIssue() {
    Issue issue = new Issue();
    issue.setIssueUri("id:testIssueUri");
    issue.setTitle("test issue title");
    issue.setRespectOrder(false);
    issue.setDescription("test issue description");
    issue.setImageUri("id:testIssueImageUri");
    issue.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));

    Serializable id = hibernateTemplate.save(issue);

    Issue storedIssue = (Issue) hibernateTemplate.get(Issue.class, id);
    assertNotNull(storedIssue, "didn't save issue");
    assertEquals(storedIssue, issue, "didn't store correct issue properties");
    assertNotNull(storedIssue.getCreated(), "issue didn't get created date set");
  }

  @Test
  public void testUpdateIssue() {
    long testStart = Calendar.getInstance().getTimeInMillis();
    Issue issue = new Issue("id:issueUriToUpdate");
    issue.setTitle("old title");
    issue.setDescription("old description");
    issue.setDisplayName("old display name");
    List<String> articleDois = new ArrayList<String>(3);
    articleDois.add("old doi 1");
    articleDois.add("old doi 2");
    articleDois.add("old doi 3");

    issue.setArticleDois(articleDois);


    Serializable id = hibernateTemplate.save(issue);

    issue.getArticleDois().remove(1);
    issue.getArticleDois().add("new doi");

    issue.setTitle("new title");
    issue.setDescription("new description");
    issue.setDisplayName("new display name");

    hibernateTemplate.update(issue);

    Issue storedIssue = (Issue) hibernateTemplate.get(Issue.class, id);
    assertEquals(storedIssue, issue, "didn't update issue properties");
    assertNotNull(storedIssue.getLastModified(), "issue didn't get last modified date set");
    assertTrue(storedIssue.getLastModified().getTime() > testStart, "last modified wasn't after test start");
    assertTrue(storedIssue.getLastModified().getTime() > storedIssue.getCreated().getTime(),
        "last modified wasn't after created");
  }
}
