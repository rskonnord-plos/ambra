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

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 2/9/12
 */
public class UserProfileTest extends BaseHibernateTest {

  @Test
  public void testSetAlertsFromCollection() {
    UserProfile user = new UserProfile();
    user.setAlertsList(Arrays.asList("foo", "bar"));
    assertEquals(user.getAlertsJournals(), "foo,bar", "didn't get alerts set correctly");
    user.setAlertsJournals(null);
    assertEquals(user.getAlertsList().size(), 0, "didn't handle null alerts string");
  }
  
  @Test
  public void testGetWeeklyAlerts() {
    UserProfile user = new UserProfile();
    user.setAlertsJournals("foo" + UserProfile.WEEKLY_ALERT_SUFFIX + UserProfile.ALERTS_SEPARATOR + "bar");
    List<String> expectedAlerts = new ArrayList<String>(1);
    expectedAlerts.add("foo");
    assertEquals(user.getWeeklyAlerts(), expectedAlerts, "User didn't return correct alerts");

    user.setAlertsJournals(null);
    assertEquals(user.getWeeklyAlerts().size(), 0, "didn't handle null alerts string");
  }

  @Test
  public void testGetMonthlyAlerts() {
    UserProfile user = new UserProfile();
    user.setAlertsJournals("foo" + UserProfile.MONTHLY_ALERT_SUFFIX + UserProfile.ALERTS_SEPARATOR + "bar");
    List<String> expectedAlerts = new ArrayList<String>(1);
    expectedAlerts.add("foo");
    assertEquals(user.getMonthlyAlerts(), expectedAlerts, "User didn't return correct alerts");

    user.setAlertsJournals(null);
    assertEquals(user.getMonthlyAlerts().size(), 0, "didn't handle null alerts string");
  }


  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testUniqueEmailConstraint() {
    UserProfile profile1 = new UserProfile();
    profile1.setEmail("foo@bar.org");
    profile1.setDisplayName("FooBar");
    profile1.setPassword("pass");

    UserProfile profile2 = new UserProfile();
    profile2.setEmail("foo@bar.org");
    profile2.setDisplayName("FooBare");
    profile2.setPassword("pass");

    hibernateTemplate.save(profile1);
    hibernateTemplate.save(profile2);
  }

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testUniqueDisplayNameConstraint() {
    UserProfile profile1 = new UserProfile();
    profile1.setEmail("foo@ambraproject.org");
    profile1.setDisplayName("ambra");
    profile1.setPassword("pass");

    UserProfile profile2 = new UserProfile();
    profile2.setEmail("foo2@ambraproject.org");
    profile2.setDisplayName("ambra");
    profile2.setPassword("pass");

    hibernateTemplate.save(profile1);
    hibernateTemplate.save(profile2);
  }

  @Test(expectedExceptions = {DataIntegrityViolationException.class})
  public void testUniqueAuthIdConstraint() {
    UserProfile profile1 = new UserProfile();
    profile1.setAuthId("test-authId1");
    profile1.setPassword("pass");
    profile1.setDisplayName("displayNameForTestUniqueAuthId1");
    profile1.setEmail("emailNameForTestUniqueAuthId1@example.com");

    UserProfile profile2 = new UserProfile();
    profile2.setAuthId("test-authId1");
    profile1.setPassword("pass");
    profile1.setDisplayName("displayNameForTestUniqueAuthId2");
    profile1.setEmail("emailNameForTestUniqueAuthId2@example.com");

    hibernateTemplate.save(profile1);
    hibernateTemplate.save(profile2);
  }

  @Test
  public void testSaveProfile() {
    UserRole role = new UserRole();
    role.setRoleName("test-role-for-profile");
    hibernateTemplate.save(role);

    final UserProfile profile = new UserProfile();
    profile.setEmail("deltron@3030.com");
    profile.setDisplayName("Deltron3030");
    profile.setPassword("pass");
    profile.setBiography("Foucault is best known for his critical studies of social institutions, most notably " +
        "psychiatry, medicine, the human sciences and the prison system, as well as for his work on the history " +
        "of human sexuality. His writings on power, knowledge, and discourse have been widely influential in " +
        "academic circles. In the 1960s Foucault was associated with structuralism, a movement from which he " +
        "distanced himself. Foucault also rejected the poststructuralist and postmodernist labels later attributed " +
        "to him, preferring to classify his thought as a critical history of modernity rooted in Immanuel Kant. " +
        "Foucault's project was particularly influenced by Nietzsche, his \"genealogy of knowledge\" being a direct " +
        "allusion to Nietzsche's \"genealogy of morality\". In a late interview he definitively stated: " +
        "\"I am a Nietzschean.\"[1]");
    profile.setAlertsJournals("foo1,foo2");
    profile.setRoles(new HashSet<UserRole>(1));
    profile.getRoles().add(role);

    final Serializable id = hibernateTemplate.save(profile);
    final List<String> expectedAlerts = new ArrayList<String>(2);
    expectedAlerts.add("foo1");
    expectedAlerts.add("foo2");
    UserProfile storedProfile = (UserProfile) hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(UserProfile.class)
            .setFetchMode("roles", FetchMode.JOIN)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .add(Restrictions.eq("ID", id))
    ).get(0);

    assertEquals(storedProfile.getEmail(), profile.getEmail(), "storedProfile had incorrect email");
    assertEquals(storedProfile.getDisplayName(), profile.getDisplayName(), "storedProfile had incorrect display name");
    assertEquals(storedProfile.getBiography(), profile.getBiography(), "storedProfile had incorrect biography");
    assertEquals(storedProfile.getRoles().size(), 1, "Didn't save user role");
    assertEquals(storedProfile.getAlertsJournals(), profile.getAlertsJournals(), "saved user didn't have correct alerts text");
    assertEquals(storedProfile.getAlertsList(), expectedAlerts, "saved user didn't return correct alerts list");

    assertNotNull(storedProfile.getAuthId(), "didn't generate auth id");
    assertNotNull(storedProfile.getVerificationToken(), "didn't generate verification token");
    assertNotNull(storedProfile.getProfileUri(), "didn't generate a profile uri");
    assertFalse(storedProfile.getVerified(), "didn't set verified to false");
  }

  @Test(expectedExceptions = {InvalidDataAccessApiUsageException.class})
  public void testDoesNotCascadeToRoles() {
    UserProfile profile = new UserProfile();
    profile.setEmail("emailForRoleTest");
    profile.setDisplayName("nameForRoleTest");
    profile.setPassword("pass");
    profile.setRoles(new HashSet<UserRole>(1));
    UserRole role = new UserRole();
    role.setRoleName("role that should not be added to db");
    profile.getRoles().add(role);
    hibernateTemplate.save(profile);
  }
}
