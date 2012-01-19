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
package org.ambraproject.migrationtests;

import org.ambraproject.service.MySQLService;
import org.ambraproject.service.TopazService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.URI;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Base test class for unit tests in the Topaz Migration.  Used just for holding common properties / beans. The main
 * goal for these tests is to run after the topazmigration, make requests for objects from both mysql and topaz, and
 * compare the two.  This makes available methods for loading objects from mysql and topaz based on ids, as well as
 * provides a consistent topaz session that can be used to create criteria for loading objects
 *
 * @author Alex Kudlick Date: Mar 18, 2011
 */
public abstract class BaseMigrationTest {
  private static final Logger log = LoggerFactory.getLogger(BaseMigrationTest.class);

  private MySQLService mysqlService;

  //The topazService is private so that we can be sure to maintain just one session throughout the life of the test.
  // Subclasses should reference topazSession to list id's and/or load objects. loadTopazObject() methods
  // have also been provided
  private TopazService topazService;

  //A session provided for subclasses to use to interact with topaz.  This forces the test to use only one session
  protected org.topazproject.otm.Session topazSession;

  //We have Transaction as a field just so we can rollback after all tests
  private org.topazproject.otm.Transaction topazTransaction;

  protected org.hibernate.Session mySqlSession;
  private org.hibernate.Transaction mysqlTransaction;

  @BeforeClass
  public void initServices() throws Exception {
    this.topazService = new TopazService();
    this.mysqlService = new MySQLService();

    topazSession = topazService.openSession();
    topazTransaction = topazSession.beginTransaction();
    mySqlSession = mysqlService.openSession();
    mysqlTransaction = mySqlSession.beginTransaction();
  }

  @AfterClass
  public void closeServices() {
    topazTransaction.rollback();
    topazSession.close();
    topazService.close();

    mysqlTransaction.rollback();
    mySqlSession.close();
    mysqlService.close();
  }

  /**
   * Sessions seem to time out, or get into a strange state after a time.
   * This just resets them
   *
   * @return
   */
  protected boolean restartSessions()
  {
    try {
      closeServices();
      initServices();

      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  protected <T> T loadTopazObject(URI id, Class<T> clazz) {
    return loadTopazObject(id.toString(), clazz);
  }

  protected <T> T loadTopazObject(String id, Class<T> clazz) {
    try {
      return topazSession.get(clazz, id);
    } catch (Exception e) {
      int retry = 5;
      while (retry > 0) {
        try {
          restartSessions();
          return this.topazSession.get(clazz, id);
        } catch (Exception oe) {
          retry = retry - 1;

          log.warn("topaz failed to load a " + clazz.getSimpleName() + " for id: " + id);
        }
      }
      log.error("topaz failed to load a " + clazz.getSimpleName() + " for id: " + id,e);

      return null;
    }
  }

  protected <T> T loadMySQLObject(URI id, Class<T> clazz) {
    return mysqlService.loadObject(id, clazz, mySqlSession, mysqlTransaction);
  }

  protected <T> T loadMySQLObject(String id, Class<T> clazz) {
    return mysqlService.loadObject(id, clazz, mySqlSession, mysqlTransaction);
  }

  /**
   * Helper method to compare dates.  This ONLY compares down to the seconds; since Mysql doesn't store milliseconds
   *
   * @param mysqlDate - the date from mysql to compare
   * @param topazDate - the date from topaz to compare
   * @param msg       - the message to display if an assertion fails
   */
  protected static void assertMatchingDates(Date mysqlDate, Date topazDate, String msg) {
    if (mysqlDate == null || topazDate == null) {
      assertTrue(mysqlDate == null && topazDate == null,msg + ";\nOne was null and the other wasn't");
    } else {
      Calendar mysqlCal = new GregorianCalendar();
      mysqlCal.setTime(mysqlDate);
      Calendar topazCal = new GregorianCalendar();
      topazCal.setTime(topazDate);
      assertEquals(mysqlCal.get(Calendar.YEAR), topazCal.get(Calendar.YEAR),
          msg + ";\nThe years differed");
      assertEquals(mysqlCal.get(Calendar.MONTH), topazCal.get(Calendar.MONTH),
          msg + ";\nThe months differed");
      assertEquals(topazCal.get(Calendar.DAY_OF_MONTH), mysqlCal.get(Calendar.DAY_OF_MONTH),
          msg + ";\nThe days differed");
      assertEquals(topazCal.get(Calendar.DAY_OF_WEEK), mysqlCal.get(Calendar.DAY_OF_WEEK),
          msg + ";\nThe days of week differed");
      assertEquals(mysqlCal.get(Calendar.HOUR), topazCal.get(Calendar.HOUR),
          msg + ";\nThe hours differed");
      assertEquals(mysqlCal.get(Calendar.MINUTE), topazCal.get(Calendar.MINUTE),
          msg + ";\nThe minutes differed");
      assertEquals(mysqlCal.get(Calendar.SECOND), topazCal.get(Calendar.SECOND),
          msg + ";\nThe seconds differed");
    }
  }

  /**
   * Helper method for comparing sets.
   *
   * NOTE that this should ONLY be called on sets of objects that have a decent equals() method
   *
   * @param mysqlSet - the set from mysql to compare
   * @param topazSet - the set from topaz to compare
   * @param msg      - the message to display if an assertion fails
   */
  protected static void assertMatchingSets(Set mysqlSet, Set topazSet, String msg) {
    //NOTE: If there is an empty collection property, topaz returns null and mysql returns empty collection
    if (topazSet == null) {
      assertEquals(mysqlSet.size(), 0,
          msg + ";\nMysql had null set but topaz had non-Empty set");
    } else {
      assertEqualsNoOrder(topazSet.toArray(),
          mysqlSet.toArray(),
          msg);
    }
  }

  /**
   * Helper methods for comparing Lists. This method does a strict ordered comparison; to compare Lists without order,
   * call assertEqualsNoOrder(list.toArray(),list.toArray()). NOTE that this should ONLY be called on sets of objects
   * that have a decent equals() method
   *
   * @param mysqlList  - the list from mysql to compare
   * @param topazList - the list from topaz to compare
   * @param msg - the message to display if an assertion fails
   */
  protected static void assertMatchingLists(List mysqlList, List topazList, String msg) {
    //NOTE: If there is an empty collection property, topaz returns null and mysql returns empty collection
    if (topazList == null) {
      Assert.assertEquals(0, mysqlList.size(),
          msg);
    } else {
      assertEquals(mysqlList.size(), topazList.size(),msg + ";\nThe lists didn't have the same number of elements");
      for (int i = 0; i < topazList.size(); i++) {
        assertEquals(mysqlList.get(i), topazList.get(i),msg + ";\nThe lists differed at index " + i);
      }
    }
  }
}
