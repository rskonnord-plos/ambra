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

import junit.framework.TestCase;

/**
 * Tests the Annotation web service.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationServiceTest extends TestCase {
  private Annotations service;

  /**
   * Creates a new AnnotationServiceTest object.
   *
   * @param testName name of this test
   */
  public AnnotationServiceTest(String testName) {
    super(testName);
  }

  /**
   * Sets up the test. Gets the client stub for making calls to the service.
   *
   * @throws ServiceException indicates an error in setting up the client stub
   * @throws RemoteException indicates an error in setting up the client stub
   * @throws Error DOCUMENT ME!
   */
  protected void setUp() throws ServiceException, RemoteException {
    try {
      service =
        AnnotationClientFactory.create("http://localhost:9997/ws-annotation/services/AnnotationServicePort");
    } catch (MalformedURLException e) {
      throw new Error(e);
    }
  }

  /*
   *
   */
  public void testBasic() throws RemoteException, NoSuchAnnotationIdException {
    String           subject     = "foo:bar";
    String           context     = "foo:bar##xpointer(id(\"Main\")/p[2])";
    String           hackContext = "$user/$annotates/$s/$created/\\'\"\'";
    String           annotation  = "annotation:id#42";
    String           bodyUrl     = "http://gandalf.topazproject.org";
    String           bodyContent = "This is a comment on foo:bar";
    String           title       = "Title";
    String           mediator    = "integration-test";
    AnnotationInfo[] annotations = service.listAnnotations(mediator, subject, null);

    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);

      annotations = service.listAnnotations(mediator, 0);

      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);
    } catch (NoSuchAnnotationIdException nsaie) {
      fail("Unexpected NoSuchAnnotationIdException");
    }

    annotations = service.listAnnotations(mediator, subject, null);
    assertTrue("Expected empty list of annotations, got " + annotations.length,
               annotations.length == 0);

    boolean gotExc = false;

    try {
      AnnotationInfo info = service.getAnnotationInfo(annotation);
    } catch (NoSuchAnnotationIdException nsaie) {
      assertEquals(nsaie.getId(), annotation);
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchAnnotationIdException", gotExc);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation, false);
    } catch (NoSuchAnnotationIdException nsaie) {
      assertEquals(nsaie.getId(), annotation);
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchAnnotationIdException", gotExc);

    gotExc = false;

    try {
      annotation =
        service.createAnnotation(mediator, null, subject, null, null, false, title,
                                 "bad:url/{context}");
    } catch (Exception e) {
      gotExc = true;
    }

    assertTrue("Failed to get expected IllegalArgumentException", gotExc);

    annotation =
      service.createAnnotation(mediator, null, subject, hackContext, null, false, title, bodyUrl);

    annotations = service.listAnnotations(mediator, subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    AnnotationInfo info = service.getAnnotationInfo(annotation);
    assertEquals(info.getId(), annotations[0].getId());
    assertEquals(info.getType(), annotations[0].getType());
    assertEquals(info.getAnnotates(), annotations[0].getAnnotates());
    assertEquals(info.getContext(), annotations[0].getContext());
    assertEquals(info.getBody(), annotations[0].getBody());
    assertEquals(info.getSupersedes(), annotations[0].getSupersedes());
    assertEquals(info.getSupersededBy(), annotations[0].getSupersededBy());
    assertEquals(info.getCreator(), annotations[0].getCreator());
    assertEquals(info.getCreated(), annotations[0].getCreated());
    assertEquals(info.getTitle(), annotations[0].getTitle());
    assertEquals(info.getMediator(), annotations[0].getMediator());
    assertEquals(info.getState(), annotations[0].getState());

    assertEquals(info.getBody(), bodyUrl);
    assertEquals(info.getAnnotates(), subject);
    assertEquals(info.getContext(), hackContext);
    assertEquals(info.getTitle(), title);
    assertEquals(info.getMediator(), mediator);
    assertEquals(info.getState(), 0);

    String superseded = annotation;

    try {
      annotation =
        service.createAnnotation(mediator, null, subject, context, annotation, true, title,
                                 "text/plain;charset=utf-8", bodyContent.getBytes("utf-8"));
    } catch (java.io.UnsupportedEncodingException e) {
      throw new Error(e);
    }

    info = service.getAnnotationInfo(superseded);
    assertEquals(info.getSupersededBy(), annotation);

    assertTrue("expected a creator", info.getCreator() != null);

    info = service.getAnnotationInfo(annotation);
    assertEquals(info.getSupersedes(), superseded);

    assertTrue("expected anonymous creator", info.getCreator() == null);

    String s;

    try {
      s = (new BufferedReader(new InputStreamReader((new URL(info.getBody())).openStream())))
           .readLine();
    } catch (IOException e) {
      throw new RemoteException("failed to read annotation body", e);
    }

    assertEquals("<a:body> mismatch, got '" + s + "'", s, bodyContent);

    assertEquals("<a:context> mismatch, got '" + s + "'", info.getContext(), context);

    annotations = service.listAnnotations(mediator, subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    annotations = service.getPrecedingAnnotations(annotation);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), superseded);

    annotations = service.getPrecedingAnnotations(superseded);
    assertTrue("Expected zero annotation, got " + annotations.length, annotations.length == 0);

    annotations = service.getLatestAnnotations(annotation);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    annotations = service.getLatestAnnotations(superseded);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    service.setAnnotationState(annotation, 42);

    annotations = service.listAnnotations(mediator, 42);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    info = annotations[0];
    assertEquals(info.getState(), 42);

    annotations = service.listAnnotations(mediator, 0);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + annotation + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), annotation);

    info = annotations[0];
    assertEquals(info.getState(), 42);

    service.deleteAnnotation(annotation, false);

    annotations = service.listAnnotations(mediator, subject, null);
    assertTrue("Expected one annotation, got " + annotations.length, annotations.length == 1);
    assertEquals("Expected annotation-id '" + superseded + "', got '" + annotations[0].getId()
                 + "'", annotations[0].getId(), superseded);

    service.deleteAnnotation(superseded, true);

    gotExc = false;

    try {
      service.deleteAnnotation(annotation, true);
    } catch (NoSuchAnnotationIdException nsaie) {
      assertEquals(nsaie.getId(), annotation);
      gotExc = true;
    }

    assertTrue("Failed to get expected NoSuchAnnotationIdException", gotExc);

    annotations = service.listAnnotations(mediator, subject, null);
    assertTrue("Expected zero annotations, got " + annotations.length, annotations.length == 0);
  }

  /*
   *
   */
  public void testAnnotatedContent() throws RemoteException, NoSuchAnnotationIdException {
    final String testXml =
      "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
      + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
      + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter></doc>";

    final String subject     = "foo:bar";
    String       context1    = "foo:bar#xpointer(string-range(/,'Hello+world'))";
    String       context2    = "foo:bar#xpointer(string-range(/,'indeed,+wonderful'))";
    String       context3    = "foo:bar#xpointer(string-range(/,'world,+indeed'))";
    String       bodyUrl     = "http://gandalf.topazproject.org";
    String       title       = "Title";
    String       mediator    = "integration-test";
    AnnotationInfo[] annotations = service.listAnnotations(mediator, subject, null);

    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);

      annotations = service.listAnnotations(mediator, 0);

      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);
    } catch (NoSuchAnnotationIdException nsaie) {
      fail("Unexpected NoSuchAnnotationIdException");
    }

    service.createAnnotation(mediator, null, subject, context1, null, false, title, bodyUrl);
    service.createAnnotation(mediator, null, subject, context2, null, false, title, bodyUrl);
    service.createAnnotation(mediator, null, subject, context3, null, false, title, bodyUrl);

    annotations = service.listAnnotations(mediator, subject, null);

    DataHandler content =
      new DataHandler(new DataSource() {
          public String getContentType() {
            return "text/xml";
          }

          public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(testXml.getBytes("UTF-8"));
          }

          public String getName() {
            return subject;
          }

          public OutputStream getOutputStream() throws IOException {
            return null;
          }
        });

    content = service.getAnnotatedContent(subject, null, content, mediator, null);

    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(content.getInputStream()));
      String         s;

      while ((s = r.readLine()) != null)
        System.out.println(s);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // xxx: may be do a parse and eval an xpath to verify
    try {
      for (int i = 0; i < annotations.length; i++)
        service.deleteAnnotation(annotations[i].getId(), true);
    } catch (NoSuchAnnotationIdException nsaie) {
      fail("Unexpected NoSuchAnnotationIdException");
    }
  }
}
