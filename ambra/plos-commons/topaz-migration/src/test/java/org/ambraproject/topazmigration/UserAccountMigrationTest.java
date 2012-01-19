/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.topazmigration;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.*;

import java.net.URI;
import java.util.*;

import static org.testng.Assert.*;

/**
 * @author Alex Kudlick Date: Mar 28, 2011
 *         <p/>
 *         org.ambraproject.topazmigration
 */
public class UserAccountMigrationTest extends BaseMigrationTest {
  private static final int INCREMENT_SIZE = 100;

  @DataProvider(name = "userAccounts")
  public Iterator<Object[]> getCitations() {
    return new MigrationDataIterator(this, UserAccount.class,INCREMENT_SIZE);
  }

  @Test(dataProvider = "userAccounts")
  public void diffUserAccounts(UserAccount mysqlUserAccount, UserAccount topazUserAccount) {
    assertNotNull(topazUserAccount, "topaz returned a null user account");
    assertNotNull(mysqlUserAccount, "Mysql returned didn't return a user account for id: " + topazUserAccount.getId());
    assertEquals(mysqlUserAccount.getId(), topazUserAccount.getId(),
        "Mysql and Topaz UserAccounts didn't have matching ids");
    assertEquals(mysqlUserAccount.getState(), topazUserAccount.getState(),
        "Mysql and Topaz UserAccounts didn't have matching states; id: " + mysqlUserAccount.getId());

    compareAuthIds(mysqlUserAccount.getAuthIds(), topazUserAccount.getAuthIds());
    compareUserRoles(mysqlUserAccount.getRoles(), topazUserAccount.getRoles());
    compareUserPreferences(mysqlUserAccount.getPreferences(), topazUserAccount.getPreferences(), mysqlUserAccount.getId());
    //Just compare ids for the user profile
    if (topazUserAccount.getProfile() == null) {
      assertNull(mysqlUserAccount.getProfile(),"Topaz User Account had null Profile but Mysql didn't;" +
          " id: " + mysqlUserAccount.getId());
    } else if (mysqlUserAccount.getProfile() == null) {
      assertNull(topazUserAccount.getProfile(),"MySQL User Account had null Profile but Topaz didn't;" +
          " id: " + topazUserAccount.getId());
    } else {
      assertEquals(mysqlUserAccount.getProfile().getId(), topazUserAccount.getProfile().getId(),
          "Mysql and Topaz UserAccounts didn't have matching User Profiles;" +
              " id: " +  mysqlUserAccount.getId());
    }


  }

  private void compareUserPreferences(Set<UserPreferences> mysqlPrefs, Set<UserPreferences> topazPrefs, URI userAccountId) {
    if (topazPrefs == null) {
      assertEquals(mysqlPrefs, 0, "Topaz had empty user preferences set but mysql didn't;" +
          " user account: " + userAccountId);
    } else {
      assertEquals(mysqlPrefs.size(), topazPrefs.size(),
          "Mysql and Topaz didn't return the same number of User Preferences;" +
              " user account: " + userAccountId);
      for (UserPreferences mysqlPref : mysqlPrefs) {
        boolean foundMatch = false;
        for (UserPreferences topazPref : topazPrefs) {
          if (mysqlPref.getId().equals(topazPref.getId())) {
            foundMatch = mysqlPref.getAppId().equals(topazPref.getAppId());
            assertTrue(foundMatch,
                "Mysql and topaz UserPreferences objects with the same id didn't have matching 'appId' values;" +
                    " user account: " + userAccountId);
            compareUserPreference(mysqlPref.getPrefs(), topazPref.getPrefs(), mysqlPref.getId());

          }
        }
        assertTrue(foundMatch, "Didn't find a match for the Mysql UserPreferences: " + mysqlPref);
      }
    }
  }

  private void compareUserPreference(Set<UserPreference> mysqlPrefs, Set<UserPreference> topazPrefs, URI preferencesId) {
    if (topazPrefs == null) {
      assertEquals(mysqlPrefs.size(), 0, "Topaz UserPrefences object " +
          preferencesId + " had empty user preference set but mysql didn't");
    } else {
      assertEquals(mysqlPrefs.size(), topazPrefs.size(),
          "Mysql and Topaz didn't return the same number of User Preferences for UserPreferences object "
              + preferencesId);
      for (UserPreference mysqlPref : mysqlPrefs) {
        boolean foundMatch = false;
        for (UserPreference topazPref : topazPrefs) {
          if (mysqlPref.getId().equals(topazPref.getId())) {
            foundMatch = mysqlPref.getName().equals(topazPref.getName());
            assertTrue(foundMatch,
                "Mysql and topaz UserPreference objects with the same id didn't have matching 'name' values");
            //if the array is empty, topaz returns null and hibernate returns empty array
            foundMatch = (topazPref.getValues() == null && mysqlPref.getValues().length == 0) ||
                Arrays.equals(mysqlPref.getValues(), topazPref.getValues());
            assertTrue(foundMatch,
                "Mysql and topaz UserPreference objects with the same id didn't have matching 'values' arrays");
            break;
          }
        }
        assertTrue(foundMatch, "Didn't find a match for the Mysql UserPreference: " + mysqlPref);
      }


    }
  }

  private void compareAuthIds(Set<AuthenticationId> mysqlAuthids, Set<AuthenticationId> topazAuthIds) {
    if (topazAuthIds == null) {
      assertEquals(mysqlAuthids.size(), 0, "Topaz had empty auth ids set but mysql didn't");
    } else {
      assertEquals(mysqlAuthids.size(), topazAuthIds.size(), "Mysql and Topaz didn't return the same number of auth ids");

      for (AuthenticationId mysqlAuthId : mysqlAuthids) {
        boolean foundMatch = false;
        for (AuthenticationId topazAuthId : topazAuthIds) {
          if (mysqlAuthId.getId().equals(topazAuthId.getId())) {
            if (mysqlAuthId.getRealm() == null) {
              foundMatch = topazAuthId.getRealm() == null;
            } else {
              foundMatch = mysqlAuthId.getRealm().equals(topazAuthId.getRealm());
            }
            if (mysqlAuthId.getValue() == null) {
              foundMatch = foundMatch && topazAuthId.getValue() == null;
            } else {
              foundMatch = foundMatch
                  && mysqlAuthId.getValue().equals(topazAuthId.getValue());
            }

            assertTrue(foundMatch,
                "Mysql and Topaz AuthIds with the same id didn't have the same value for 'realm' and 'value';" +
                    " id: " + mysqlAuthId.getId());
            break;
          }
        }
        assertTrue(foundMatch, "Didn't find a match for the Mysql AuthenticationId: " + mysqlAuthId.getId());
      }
    }

  }

  private void compareUserRoles(Set<UserRole> mysqlRoles, Set<UserRole> topazRoles) {
    if (topazRoles == null) {
      assertEquals(mysqlRoles.size(), 0, "Topaz had empty user role set but mysql didn't");
    } else {
      assertEquals(mysqlRoles.size(), topazRoles.size(), "Mysql and Topaz didn't return the same number of user Roles");

      for (UserRole mysqlrole : mysqlRoles) {
        boolean foundMatch = false;
        for (UserRole topazrole : topazRoles) {
          if (mysqlrole.getId().equals(topazrole.getId())) {
            foundMatch = mysqlrole.getRole().equals(topazrole.getRole());
            assertTrue(foundMatch,
                "Mysql and Topaz User roles with the same id didn't have the same value for 'role';" +
                    " id: " + mysqlrole.getId());
            break;
          }
        }
        assertTrue(foundMatch, "Didn't find a match for the Mysql User Role: " + mysqlrole.getId());
      }
    }
  }

}
