/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.alerts;

import java.io.IOException;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

import org.topazproject.common.NoSuchIdException;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;
import org.topazproject.ws.article.DuplicateArticleIdException;
import org.topazproject.ws.article.IngestException;
import org.topazproject.ws.article.NoSuchArticleIdException;

import org.topazproject.ws.pap.Preferences;
import org.topazproject.ws.pap.PreferencesClientFactory;
import org.topazproject.ws.pap.UserPreference;
import org.topazproject.ws.users.DuplicateAuthIdException;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.ws.users.UserAccounts;
import org.topazproject.ws.users.UserAccountsClientFactory;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

/*
 * TODO: Additional tests:
 * - Multiple users (should all get alerts)
 * - Auto-creation of user timestamp (don't call alerts.startUser)
 * - Are dates/date-ranges working properly (loose or get extra alerts
 *   for day(s) around which alerts are sent
 */

/**
 *
 */
public class AlertsServiceTest extends TestCase {
  private Alerts service;
  private Article articleService;
  
  private Preferences prefService;
  private UserAccounts userService;
  private String userId;

  private static final Log log = LogFactory.getLog(AlertsServiceTest.class);
  
  protected static final String ALERTS_SVC_URL =
    "http://localhost:9997/ws-alerts/services/AlertsServicePort";
  protected static final String ARTICLES_SVC_URL =
    "http://localhost:9997/ws-articles/services/ArticleServicePort";
  protected static final String PREF_SVC_URL =
    "http://localhost:9997/ws-pap/services/PreferencesServicePort";
  protected static final String USER_SVC_URL =
    "http://localhost:9997/ws-users/services/UserAccountsServicePort";
  protected static final String[][] TEST_ARTICLES = {
    { "info:doi/10.1371/journal.pbio.0020294", "/pbio.0020294.zip" },
    { "info:doi/10.1371/journal.pbio.0020042", "/pbio.0020042.zip" },
    { "info:doi/10.1371/journal.pbio.0020317", "/pbio.0020317.zip" },
    { "info:doi/10.1371/journal.pbio.0020382", "/pbio.0020382.zip" },
  };

  public AlertsServiceTest(String testName) {
    super(testName);
  }

  protected void setUp()
      throws MalformedURLException, ServiceException, RemoteException, NoSuchUserIdException,
             DuplicateAuthIdException, DuplicateArticleIdException, IngestException,
             NoSuchArticleIdException {
    log.info("setUp");
    
    // Create alerts service
    this.service = AlertsClientFactory.create(ALERTS_SVC_URL);
    log.info("created alerts service");

    // Create articles service
    this.articleService = ArticleClientFactory.create(ARTICLES_SVC_URL);
    log.info("ingesting article(s)");
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      ingestArticle(TEST_ARTICLES[i][0], TEST_ARTICLES[i][1]);
    
    // Create userid
    this.userService = UserAccountsClientFactory.create(USER_SVC_URL);
    this.userId = userService.createUser("alertTest");
    log.info("Created userid: " + this.userId);

    // Create preferences
    this.prefService = PreferencesClientFactory.create(PREF_SVC_URL);
    log.info("Created preferences service");
    
    UserPreference[] prefs = new UserPreference[2];
    prefs[0] = new UserPreference();
    prefs[0].setName("alertsCategories");
    prefs[0].setValues(new String[] { "Biotechnology", "Development" });
    prefs[1] = new UserPreference();
    prefs[1].setName("alertsEmailAddress");
    prefs[1].setValues(new String[] { "ebrown@topazproject.org" });
    this.prefService.setPreferences("alerts", this.userId, prefs);
    log.info("Created alerts preferences");
  }

  protected void tearDown() throws RemoteException, NoSuchArticleIdException {
    log.info("tearDown");
    
    // Delete the articles we created
    for (int i = 0; i < TEST_ARTICLES.length; i++)
      deleteArticle(TEST_ARTICLES[i][0]);
    
    // Delete our user
    try {
      this.userService.deleteUser(userId);
    } catch (NoSuchIdException nsie) {
      // already removed?
      log.warn(nsie);
    }

    // TODO: Do I need to delete preferences?
  }

  protected void ingestArticle(String uri, String resource)
      throws RemoteException, DuplicateArticleIdException, IngestException,
             NoSuchArticleIdException {
    log.info("ingesting article " + uri + " : " + resource);
    deleteArticle(uri);
    URL article = getClass().getResource(resource);
    assertEquals("Wrong uri returned,", uri, articleService.ingest(new DataHandler(article)));
    log.info("ingested article " + uri);
  }

  protected void deleteArticle(String uri) throws RemoteException, NoSuchArticleIdException {
    try {
      articleService.delete(uri);
      log.info("deleted article " + uri);
    } catch (Exception nsie) {
      // so what
      //log.debug(nsie);
    }
  }


  /**
   * Do all/most tests in one place to avoid running setUp tearDown multiple times.
   * They just take a long time to run.
   */
  public void testAlerts() throws Exception {
    boolean success = true;
    
    success &= this.tstEmail0();
    success &= this.tstEmail1();
    success &= this.tstEmail2();

    // We do it this way because if something underneath changes, it is possible that
    // there is a good reason all these tests fail. Thus, we'd like them still all to
    // run so that their test files can be copied over the expected files.
    assertTrue(success);
  }
  
  private boolean tstEmail0() throws RemoteException, IOException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c.getTime().toString());

    c.set(2004, 3, 17);
    return this.testEmail(c, "tst0", 0, 0);
  }

  private boolean tstEmail1() throws RemoteException, IOException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c.getTime().toString());

    c.set(2004, 8, 26);
    return this.testEmail(c, "tst1", 1, 1);
  }

  private boolean tstEmail2() throws RemoteException {
    Calendar c = Calendar.getInstance();
    
    this.service.clearUser(userId);
    c.set(2004, 2, 10);
    this.service.startUser(userId, c.getTime().toString());

    c.set(2004, 10, 13);
    return this.testEmail(c, "tst2", 1, 2);
  }
  
  private boolean testEmail(Calendar alertCal, String tstName,
                            int expectedMsgs, int expectedArticles) throws RemoteException {
    SimpleSmtpServer server = startSmtpServer(2525);

    boolean success = this.service.sendAlerts(alertCal.getTime().toString(), 10);
    assertTrue(success);

    server.stop();
    
    int msgCnt = server.getReceivedEmailSize();
    log.info("Email '" + tstName + "' at " + alertCal.getTime() + " got " +
             msgCnt + " message(s)");
    
    int cnt = 0;
    for (Iterator emailIt = server.getReceivedEmail(); emailIt.hasNext(); ) {
      SmtpMessage email = (SmtpMessage)emailIt.next();
      String articles = email.getHeaderValue("X-Topaz-Articles");
      int articleCnt = new StringTokenizer(articles, ",").countTokens(); // hack, but works
      log.info(tstName + "_" + cnt + ": " + articleCnt + " article(s): " + articles);
      writeResult(email.getBody(), "/tmp/" + tstName + "_" + cnt + ".msg");
      cnt++;

      // See if we got the number of articles we expected
      // (Should never have 0 articles or will not have an email)
      if (expectedArticles >= 0) {
        if (expectedArticles != articleCnt)
        {
          log.warn("Expected " + expectedArticles + ", God " + articleCnt);
          success = false;
        }
      }
    }
    
    assertTrue(success);
    if (msgCnt != expectedMsgs) {
      log.warn("Expected " + expectedMsgs + ", Got " + msgCnt);
      success = false;
    }

    return success;
  }

  /**
   * Fix SimpleSmtpServer dead-lock.
   *
   * Basically apply patch 1310992 for bug 1179454 to version 1.6 of dumbster.
   */
  private static SimpleSmtpServer startSmtpServer(int port) {
    SimpleSmtpServer server = new SimpleSmtpServer(port);
    Thread t = new Thread(server);

    synchronized (server) {
      t.start();

      // Block until the server socket is created
      try {
        server.wait();
      } catch (InterruptedException e) {
        log.info("SimpleSmptServer interrupted", e);
      }
      return server;
    }
  }
  
  /**
   * Write expected results to a file because if articles contain any UTF-8 characters,
   * copying the expected results to resources directory is easier than trying to type
   * in expected results.
   *
   * @param result is the xml string of the results we want
   * @param fileName is the name of the file to write (usually /tmp/something...)
   */
  private static void writeResult(String result, String fileName) {
    try {
      FileWriter fw = new FileWriter(fileName);
      IOUtils.write(result.getBytes("UTF-8"), fw);
      fw.close();
    } catch (IOException ie) {
      log.warn("Unable to write " + fileName, ie);
    }
  }
}
