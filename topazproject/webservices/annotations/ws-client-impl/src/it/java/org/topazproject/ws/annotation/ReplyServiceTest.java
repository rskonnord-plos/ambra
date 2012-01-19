/* $HeadURL::                                                                        $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Comparator;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

/**
 * Tests the Reply web service.
 *
 * @author Pradeep Krishnan
 */
public class ReplyServiceTest extends TestCase {
  private Replies service;
  private String  root     = "foo:bar";
  private String  body     = "foo:body";
  private String  title    = "a title";
  private String  type     = null;
  private String  content  = "This foo:bar stuff is a classic";
  private String  mediator = "integration-test";

  /**
   * Creates a new ReplyServiceTest object.
   *
   * @param testName name of this test
   */
  public ReplyServiceTest(String testName) {
    super(testName);
  }

  /**
   * Sets up the test. Gets the client stub for making calls to the service.
   *
   * @throws ServiceException indicates an error in setting up the client stub
   * @throws RemoteException indicates an error in setting up the client stub
   */
  protected void setUp() throws ServiceException, RemoteException {
    try {
      service =
        RepliesClientFactory.create("http://localhost:9997/ws-annotation/services/ReplyServicePort");
    } catch (MalformedURLException e) {
      throw new Error(e);
    }
  }

  /**
   * Tears downthe test.
   *
   * @throws RemoteException indicates an error
   * @throws NoSuchAnnotationIdException should not happen
   */
  protected void tearDown() throws RemoteException, NoSuchAnnotationIdException {
    service.deleteReplies(root, root);
  }

  /*
   *
   */
  public void testDeleteAll() throws RemoteException {
    boolean noSuchId = false;

    try {
      service.deleteReplies(root, root);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);
  }

  /*
   *
   */
  public void testCreateBody() throws RemoteException {
    boolean   noSuchId = false;
    String    reply = null;
    ReplyInfo info  = null;

    try {
      reply = service.createReply(mediator, type, root, root, false, title, body);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      info = service.getReplyInfo(reply);
      assertEquals(info.getId(), reply);
      assertEquals(info.getRoot(), root);
      assertEquals(info.getInReplyTo(), root);
      assertEquals(info.getBody(), body);
      assertEquals(info.getTitle(), title);
      assertEquals(info.getMediator(), mediator);
      assertTrue(info.getCreator() != null);
      assertEquals(info.getState(), 0);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      service.deleteReplies(reply);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);
  }

  /*
   *
   */
  public void testAnonymize() throws RemoteException {
    boolean   noSuchId = false;
    String    reply = null;
    ReplyInfo info  = null;

    try {
      reply = service.createReply(mediator, type, root, root, true, title, body);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      info = service.getReplyInfo(reply);
      assertEquals(info.getId(), reply);
      assertEquals(info.getRoot(), root);
      assertEquals(info.getInReplyTo(), root);
      assertEquals(info.getBody(), body);
      assertEquals(info.getTitle(), title);
      assertEquals(info.getCreator(), null);
      assertEquals(info.getState(), 0);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      service.deleteReplies(reply);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);
  }
  /*
   *
   */
  public void testNonExistentReplyToCreate() throws RemoteException {
    boolean noSuchId = false;

    try {
      String id = service.createReply(mediator, type, root, "foo:nonExistent", false, title, body);
      service.deleteReplies(id);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertTrue(noSuchId);
  }

  /*
   *
   */
  public void testCreateWithContent() throws RemoteException {
    String    reply    = null;
    ReplyInfo info     = null;
    boolean   noSuchId = false;

    try {
      reply =
        service.createReply(mediator, type, root, root, false, title, "text/plain;charset=utf-8",
                            toBytes(content, "UTF-8"));
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      info = service.getReplyInfo(reply);
      assertEquals(info.getId(), reply);
      assertEquals(info.getRoot(), root);
      assertEquals(info.getInReplyTo(), root);
      assertEquals(info.getTitle(), title);
      assertContent(info.getBody(), content);
      assertEquals(info.getMediator(), mediator);
      assertTrue(info.getCreator() != null);
      assertEquals(info.getState(), 0);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);

    try {
      service.deleteReplies(reply);
    } catch (NoSuchAnnotationIdException e) {
      noSuchId = true;
    }

    assertFalse(noSuchId);
  }

  /*
   *
   */
  public void testThreads() throws RemoteException, NoSuchAnnotationIdException {
    //threadTest(1, 1);
    //threadTest(1, 2);
    //threadTest(2, 1);
    threadTest(2, 2);

    //threadTest(4, 4);
  }

  private void threadTest(int levels, int children)
      throws RemoteException, NoSuchAnnotationIdException {
    createThread(root, levels, children);

    ReplyThread thread = service.getReplyThread(root, root);
    assertThread(thread, levels, children);

    service.deleteReplies(root, root);

    ReplyInfo[] info = service.listReplies(root, root);
    assertEquals(info.length, 0);

    info = service.listAllReplies(root, root);
    assertEquals(info.length, 0);
  }

  private void createThread(String inReplyTo, int levels, int children)
                     throws NoSuchAnnotationIdException, RemoteException {
    if (levels <= 0)
      return;

    for (int i = 0; i < children; i++) {
      String child =
        service.createReply(mediator, type, root, inReplyTo, false, title + ":" + levels + "." + i,
                            "text/plain;charset=utf-8",
                            toBytes(content + ":" + levels + "." + i, "UTF-8"));
      createThread(child, levels - 1, children);
    }
  }

  private void assertThread(ReplyThread inReplyTo, int levels, int children)
                     throws RemoteException, NoSuchAnnotationIdException {
    if (levels <= 0)
      return;

    int total = countNodes(levels, children);
    assertEquals(total, service.listAllReplies(inReplyTo.getRoot(), inReplyTo.getId()).length);

    ReplyThread[] replies = inReplyTo.getReplies();
    assertEquals(replies.length, children);
    assertEquals(replies, service.listReplies(inReplyTo.getRoot(), inReplyTo.getId()));

    for (int i = 0; i < children; i++) {
      assertEquals(replies[i].getTitle(), title + ":" + levels + "." + i);
      assertContent(replies[i].getBody(), content + ":" + levels + "." + i);
      assertEquals(replies[i], service.getReplyInfo(replies[i].getId()));
      assertThread(replies[i], levels - 1, children);
    }
  }

  private void assertContent(String body, String content)
                      throws RemoteException {
    String s;

    try {
      s = (new BufferedReader(new InputStreamReader((new URL(body)).openStream()))).readLine();
    } catch (IOException e) {
      throw new RemoteException("failed to read reply body", e);
    }

    assertEquals(s, content);
  }

  private int countNodes(int levels, int children) {
    int total = children;
    int prev = children;

    for (int i = 1; i < levels; i++) {
      prev *= children;
      total += prev;
    }

    return total;
  }

  private byte[] toBytes(String string, String encoding) {
    try {
      return string.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw new Error("what the?");
    }
  }

  private void assertEquals(ReplyInfo[] l1, ReplyInfo[] l2) {
    assertEquals(l1.length, l2.length);

    sort(l1);
    sort(l2);

    for (int i = 0; i < l1.length; i++)
      assertEquals(l1[i], l2[i]);
  }

  private void assertEquals(ReplyInfo i1, ReplyInfo i2) {
    assertEquals(i1.getId(), i2.getId());
    assertEquals(i1.getType(), i2.getType());
    assertEquals(i1.getRoot(), i2.getRoot());
    assertEquals(i1.getInReplyTo(), i2.getInReplyTo());
    assertEquals(i1.getTitle(), i2.getTitle());
    assertEquals(i1.getBody(), i2.getBody());
    assertEquals(i1.getCreator(), i2.getCreator());
    assertEquals(i1.getCreated(), i2.getCreated());
    assertEquals(i1.getMediator(), i2.getMediator());
    assertEquals(i1.getState(), i2.getState());
  }

  private void sort(ReplyInfo[] l) {
    Arrays.sort(l,
                new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((ReplyInfo) o1).getId().compareTo(((ReplyInfo) o2).getId());
        }
      });
  }
}
