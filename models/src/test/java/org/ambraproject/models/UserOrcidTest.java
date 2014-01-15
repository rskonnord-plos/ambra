/*
 * Copyright (c) 2007-2014 by Public Library of Science
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
package org.ambraproject.models;

import org.testng.annotations.Test;
import java.util.Calendar;
import static org.testng.Assert.assertEquals;

/**
 * Test the ORCiD data model
 */
public class UserOrcidTest extends BaseHibernateTest {
  @Test
  public void testOrcid() {
    UserOrcid userOrcid = new UserOrcid();

    Calendar now = Calendar.getInstance();
    Long userProfileID = 1000L;

    userOrcid.setID(userProfileID);
    userOrcid.setOrcid("bleh");
    userOrcid.setAccessToken("token");
    userOrcid.setRefreshToken("refreshtoken");
    userOrcid.setTokenExpires(now);
    userOrcid.setTokenScope("scope");

    hibernateTemplate.save(userOrcid);

    userOrcid = (UserOrcid) hibernateTemplate.get(UserOrcid.class, userProfileID);

    assertEquals(userOrcid.getOrcid(), "bleh");
    assertEquals(userOrcid.getAccessToken(), "token");
    assertEquals(userOrcid.getRefreshToken(), "refreshtoken");
    assertEquals(userOrcid.getTokenExpires(), now);
    assertEquals(userOrcid.getTokenScope(), "scope");
 }
}

