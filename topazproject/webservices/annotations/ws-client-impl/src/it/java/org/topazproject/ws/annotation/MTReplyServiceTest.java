/* $HeadURL::                                                                            $
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Tests the Reply web service.
 *
 * @author Pradeep Krishnan
 */
public class MTReplyServiceTest extends TestCase {
  private static final Log log      = LogFactory.getLog(MTReplyServiceTest.class);
  private String           mediator = "mt-integration-test";
  private String           type     = null;
  private int              mt       = 5;
  private Replies[]        services = new Replies[mt];
  private Random           random   = new Random();

  /**
   * Creates a new AnnotationServiceTest object.
   *
   * @param testName name of this test
   */
  public MTReplyServiceTest(String testName) {
    super(testName);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void setUp() throws Exception {
    for (int i = 0; i < services.length; i++)
      services[i] =
        RepliesClientFactory.create("http://localhost:9997/ws-annotation/services/ReplyServicePort");
  }

  /*
   *
   */
  public void testMT() throws Exception {
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
    private Exception error;
    private Replies   service;
    private String    root;
    private String    content;
    private String    title;

    public TestTask(int id) throws Exception {
      this.id    = id;
      service    = services[id];
      root       = "foo:bar/thread=" + id + "/";
      title      = "Title by thread[" + id + "]";
      type       = null;
      content    = "Content from Thread[" + id + "]";
      mediator   = "integration-test";
    }

    public void run() {
      try {
        testThreads();
      } catch (Exception e) {
        e.printStackTrace();
        error = e;
      }
    }

    public void assertPass() throws Exception {
      if (error != null)
        throw error;
    }

    /*
     *
     */
    private void testThreads() throws Exception {
      service.deleteReplies(root, root);
      threadTest(1, 1);

      //threadTest(random.nextInt(4) + 1, random.nextInt(4) + 1);
      //threadTest(2, 2);
      //threadTest(1, 2);
      //threadTest(2, 1);
      //threadTest(4, 4);
    }

    private void threadTest(int levels, int children) throws Exception {
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
                       throws Exception {
      if (levels <= 0)
        return;

      for (int i = 0; i < children; i++) {
        String child =
          service.createReply(mediator, type, root, inReplyTo, false,
                              title + ":" + levels + "." + i, "text/plain;charset=utf-8",
                              toBytes(content + ":" + levels + "." + i, "UTF-8"));
        createThread(child, levels - 1, children);
      }
    }

    private void assertThread(ReplyThread inReplyTo, int levels, int children)
                       throws Exception {
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
                        throws Exception {
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

    private void assertEquals(ReplyInfo[] l1, ReplyInfo[] l2)
                       throws Exception {
      assertEquals(l1.length, l2.length);

      //sort(l1);
      //sort(l2);
      for (int i = 0; i < l1.length; i++)
        assertEquals(l1[i], l2[i]);
    }

    private void assertEquals(ReplyInfo i1, ReplyInfo i2)
                       throws Exception {
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

    private void assertEquals(String v1, String v2) throws Exception {
      if ((v1 != null) && !v1.equals(v2))
        throw new Exception("assert failure: '" + v1 + "' != '" + v2 + "'");

      if ((v2 != null) && !v2.equals(v1))
        throw new Exception("assert failure: '" + v1 + "' != '" + v2 + "'");
    }

    private void assertEquals(int v1, int v2) throws Exception {
      if (v1 != v2)
        throw new Exception("assert failure: '" + v1 + "' != '" + v2 + "'");
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
}
