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

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Tests the Annotation web service.
 *
 * @author Pradeep Krishnan
 */
public class MTAnnotationServiceTest extends TestCase {
  private static final Log log       = LogFactory.getLog(MTAnnotationServiceTest.class);
  private String           mediator  = "mt-integration-test";
  private String           annotates = "foo:bar";
  private String           context   = "foo:bar##xpointer(id(\"Main\")/p[2])";
  private String           title     = "Title";
  private String           comment   = "Comment";
  private int              mt        = 5;
  private Annotations[]    services  = new Annotations[mt];

  /**
   * Creates a new AnnotationServiceTest object.
   *
   * @param testName name of this test
   */
  public MTAnnotationServiceTest(String testName) {
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
        AnnotationClientFactory.create("http://localhost:9997/ws-annotation/services/AnnotationServicePort");
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

    public TestTask(int id) throws Exception {
      this.id = id;
    }

    public void run() {
      try {
        String aid =
          services[id].createAnnotation(mediator, null, annotates + id, context + id, null, false,
                                        title + id, "text/plain;charset=utf-8",
                                        (comment + id).getBytes("utf-8"));
        AnnotationInfo info = services[id].getAnnotationInfo(aid);

        AnnotationInfo[] annotations = services[id].listAnnotations(mediator, annotates + id, null);

        for (int i = 0; i < annotations.length; i++)
          services[id].deleteAnnotation(annotations[i].getId(), true);
      } catch (Exception e) {
        e.printStackTrace();
        error = e;
      }
    }

    public void assertPass() throws Exception {
      if (error != null)
        throw error;
    }
  }
}
