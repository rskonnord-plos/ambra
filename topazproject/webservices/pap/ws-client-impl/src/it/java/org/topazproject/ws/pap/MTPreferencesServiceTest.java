/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

/**
 * Simple tests for the preferences service.
 *
 * @author Pradeep Krishnan
 */
public class MTPreferencesServiceTest extends TestCase {
 private int mt = 5;
  
  public MTPreferencesServiceTest(String testName) {
    super(testName);
  }

  /*
   *
   */
  public void testMT() throws Throwable {
    TestTask[] tasks = new TestTask[mt];

    for (int i = 0; i < mt; i++)
      tasks[i] = new TestTask(i);

    for (int i = 0; i < mt; i++)
      tasks[i].start();

    for (int i = 0; i < mt; i++)
      tasks[i].join();

    for (int i = 0; i < mt; i++)
      tasks[i].assertPass();
  }

  public class TestTask extends Thread {
    private int       id;
    private Throwable error;
    PreferencesServiceTest test;

    public TestTask(int id) throws Exception {
      this.id = id;
      test = new PreferencesServiceTest("PreferencesServiceTest", id);
    }

    public void run() {
      boolean tearDown = false;
      try {
        test.setUp();
        tearDown = true;
        test.testBasicPreferences();
      } catch (Throwable e) {
        e.printStackTrace();
        error = e;
      } finally {
        try {
           if (tearDown)
             test.tearDown();
        } catch (Throwable t) {
        }
      }
    }

    public void assertPass() throws Throwable {
      if (error != null)
        throw error;
    }
  }
}
