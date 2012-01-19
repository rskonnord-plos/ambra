/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.admin.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 *
 */
public class JournalServiceTest extends TestCase {
  private Journal service;

  public JournalServiceTest(String testName) {
    super(testName);
  }

  protected void setUp() throws MalformedURLException, ServiceException, RemoteException {
    URL url = new URL("http://localhost:9997/ws-journal-admin/services/JournalServicePort");
    JournalServiceLocator locator = new JournalServiceLocator();
    service = locator.getJournalServicePort(url);
  }

  public void testAll() throws RemoteException {
    basicJournalTest();
    basicIssueTest();
  }

  private void basicJournalTest() throws RemoteException {
    String[] journals = service.listJournals();
    assertTrue("Expected empty list of journals, got " + journals.length, journals.length == 0);

    boolean gotExc = false;
    try {
      String info = service.getJournalInfo("blah");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    gotExc = false;
    try {
      service.setJournalInfo("blah", "");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    gotExc = false;
    try {
      service.deleteJournal("blah");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    service.createJournal("j1");

    journals = service.listJournals();
    assertTrue("Expected one journal, got " + journals.length, journals.length == 1);
    assertEquals("Expected journal-id 'j1', got '" + journals[0] + "'", journals[0], "j1");

    String info = service.getJournalInfo("j1");
    assertNull("Expected no info, got '" + info + "'", info);

    service.setJournalInfo("j1", "hello");
    info = service.getJournalInfo("j1");
    assertEquals("Info mismatch, got '" + info + "'", info, "hello");

    service.deleteJournal("j1");
    gotExc = false;
    try {
      service.deleteJournal("j1");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);
  }

  private void basicIssueTest() throws RemoteException {
    String jid = "j1";
    service.createJournal(jid);

    String[] issues = service.listIssues(jid);
    assertTrue("Expected empty list of issues, got " + issues.length, issues.length == 0);

    boolean gotExc = false;
    try {
      String info = service.getIssueInfo(jid, "blah");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    gotExc = false;
    try {
      service.setIssueInfo(jid, "blah", "");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    gotExc = false;
    try {
      service.deleteIssue(jid, "blah");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    service.createIssue(jid, "i1");

    issues = service.listIssues(jid);
    assertTrue("Expected one issue, got " + issues.length, issues.length == 1);
    assertEquals("Expected issue-id 'i1', got '" + issues[0] + "'", issues[0], "i1");

    String info = service.getIssueInfo(jid, "i1");
    assertNull("Expected no info, got '" + info + "'", info);

    service.setIssueInfo(jid, "i1", "byebye");
    info = service.getIssueInfo(jid, "i1");
    assertEquals("Info mismatch, got '" + info + "'", info, "byebye");

    service.deleteIssue(jid, "i1");
    gotExc = false;
    try {
      service.deleteIssue(jid, "i1");
    } catch (NoSuchJournalIdException nsjie) {
      gotExc = true;
    }
    assertTrue("Failed to get expected NoSuchJournalIdException", gotExc);

    service.deleteJournal(jid);
  }
}
