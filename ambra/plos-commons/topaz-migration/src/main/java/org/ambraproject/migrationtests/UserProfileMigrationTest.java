/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
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
 * limitations under the License. |
 */
package org.ambraproject.migrationtests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.UserProfile;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;


/**
 * @author Alex Kudlick Date: Mar 21, 2011
 *         <p/>
 *         org.plos.topazMigration
 */
public class UserProfileMigrationTest extends BaseMigrationTest {

  //The number of user profiles to load up at a time
  private static final int INCREMENT_SIZE = 500;

  @DataProvider(name = "userProfiles")
  public Iterator<Object[]> userProfiles() {
    return new MigrationDataIterator(this, UserProfile.class, INCREMENT_SIZE);
  }

  @Test(dataProvider = "userProfiles")
  public void diffUserProfiles(UserProfile mysqlUserProfile, UserProfile topazUserProfile) {
    assertNotNull(topazUserProfile, "Topaz returned a null user profile");
    if (mysqlUserProfile == null) {
      //Could be one of the author/editor user profiles we deleted
      if (topazUserProfile.getEmail() != null && !topazUserProfile.getEmail().toString().isEmpty()) {
        //But it's definitely a real user profile if it has an email
        fail("MySQL was missing a valid user profile: " + topazUserProfile.getId());
      } else {
        return;
      }
    }
    compareUserProfiles(mysqlUserProfile, topazUserProfile);
  }

  public static void compareUserProfiles(UserProfile mysqlUserProfile, UserProfile topazUserProfile) {
    //FoaFPerson properties
    assertEquals(mysqlUserProfile.getRealName(), topazUserProfile.getRealName(),
        "MySQL and Topaz didn't have matching real names; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getGivenNames(), topazUserProfile.getGivenNames(),
        "MySQL and Topaz didn't have matching given names; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getSurnames(), topazUserProfile.getSurnames(),
        "MySQL and Topaz didn't have matching surnames; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getTitle(), topazUserProfile.getTitle(),
        "MySQL and Topaz didn't have matching titles; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getGender(), topazUserProfile.getGender(),
        "MySQL and Topaz didn't have matching genders; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getEmail(), topazUserProfile.getEmail(),
        "MySQL and Topaz didn't have matching emails; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getHomePage(), topazUserProfile.getHomePage(),
        "MySQL and Topaz didn't have matching home pages; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getWeblog(), topazUserProfile.getWeblog(),
        "MySQL and Topaz didn't have matching weblogs; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getPublications(), topazUserProfile.getPublications(),
        "MySQL and Topaz didn't have matching publications; user profile: " + mysqlUserProfile.getId());

    assertMatchingSets(mysqlUserProfile.getInterests(), topazUserProfile.getInterests(),
        "MySQL and Topaz didn't have matching interests; user profile: " + mysqlUserProfile.getId());

    //UserProfile properties
    assertEquals(mysqlUserProfile.getDisplayName(), topazUserProfile.getDisplayName(),
        "MySQL and Topaz didn't have matching displayNames; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getSuffix(), topazUserProfile.getSuffix(),
        "MySQL and Topaz didn't have matching suffixes; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getPositionType(), topazUserProfile.getPositionType(),
        "MySQL and Topaz didn't have matching position types; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getOrganizationName(), topazUserProfile.getOrganizationName(),
        "MySQL and Topaz didn't have matching orginization names; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getOrganizationType(), topazUserProfile.getOrganizationType(),
        "MySQL and Topaz didn't have matching organization types; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getPostalAddress(), topazUserProfile.getPostalAddress(),
        "MySQL and Topaz didn't have matching postal addresses; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getCity(), topazUserProfile.getCity(),
        "MySQL and Topaz didn't have matching cities; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getCountry(), topazUserProfile.getCountry(),
        "MySQL and Topaz didn't have matching countries; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getBiography(), topazUserProfile.getBiography(),
        "MySQL and Topaz didn't have matching biographies; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getBiographyText(), topazUserProfile.getBiographyText(),
        "MySQL and Topaz didn't have matching biography texts; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getInterestsText(), topazUserProfile.getInterestsText(),
        "MySQL and Topaz didn't have matching interests texts; user profile: " + mysqlUserProfile.getId());
    assertEquals(mysqlUserProfile.getResearchAreasText(), topazUserProfile.getResearchAreasText(),
        "MySQL and Topaz didn't have matching research areas texts; user profile: " + mysqlUserProfile.getId());
  }
}
