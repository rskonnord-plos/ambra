/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.article;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipInputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.rpc.ServiceException;

import org.apache.commons.io.IOUtils;
import junit.framework.TestCase;

import org.topazproject.common.NoSuchIdException;

/**
 *
 */
public class ArticleServiceTest extends TestCase {
  private Article service;

  public ArticleServiceTest(String testName) throws MalformedURLException, ServiceException {
    super(testName);

    String uri = "http://localhost:9997/ws-articles/services/ArticleServicePort";
    service = ArticleClientFactory.create(uri);
  }

  protected void setUp() {
  }

  public void testBasicArticle() throws Exception {
    // topaz format
    basicArticleTest("/pbio.0020294.zip", "info:doi/10.1371/journal.pbio.0020294", "pmc.xml");
    // AP format
    basicArticleTest("/pone.0000010.zip", "info:doi/10.1371/journal.pone.0000010",
                     "pone.0000010.xml");
  }

  private void basicArticleTest(String zip, String uri, String pmc) throws Exception {
    try {
      service.delete(uri);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", uri, nsaie.getId());
      // ignore - this just means there wasn't any stale stuff left
    }

    URL article = getClass().getResource(zip);
    String retUri = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", uri, retUri);

    Throwable gotE = null;
    try {
      retUri = service.ingest(new DataHandler(article));
    } catch (DuplicateArticleIdException daie) {
      assertEquals("Mismatched id in exception, ", uri, daie.getId());
      gotE = daie;
    }
    assertNotNull("Failed to get expected duplicate-id exception", gotE);

    /* TODO: this isn't true anymore, as ingest modifies the article.
     * Is there a way we can get the modified article?
    ZipInputStream zis = new ZipInputStream(article.openStream());
    while (!zis.getNextEntry().getName().equals(pmc))
      ;
    byte[] orig  = IOUtils.toByteArray(zis);
    byte[] saved = IOUtils.toByteArray(new URL(service.getObjectURL(retUri, "XML")).openStream());
    assertTrue("Content mismatch: got '" + new String(saved, "UTF-8") + "'",
               Arrays.equals(orig, saved));
    */

    service.delete(retUri);

    gotE = null;
    try {
      service.delete(retUri);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", retUri, nsaie.getId());
      gotE = nsaie;
    }
    assertNotNull("Failed to get expected no-such-id exception", gotE);
    assertTrue("exception is not a subclass of NoSuchIdException: " + gotE.getClass(),
               gotE instanceof NoSuchIdException);
  }

  public void testIngestErrors() throws Exception {
    ingestErrorTest("/test.tpz.missing.file.zip",    "No entry found in zip file for doi");
    ingestErrorTest("/test.ap.missing.file.zip",     "No entry found in zip file for doi");
    ingestErrorTest("/test.tpz.unrefd.file.zip",     "Found unreferenced entry in zip file");
    ingestErrorTest("/test.ap.unrefd.file.zip",      "Found unreferenced entry in zip file");
    ingestErrorTest("/test.tpz.missing.article.zip", "Couldn't find article entry in zip file");
    ingestErrorTest("/test.ap.missing.article.zip",  "Couldn't find article entry in zip file");
    ingestErrorTest("/test.ap.invalid.file.zip",     "does not have same prefix as article");
    ingestErrorTest("/test.ap.invalid.id.zip",       "does not reference an existing id");
    ingestErrorTest("/test.ap.invalid.id2.zip",      "does not reference an existing id");
    ingestErrorTest("/test.ap.invalid.obj_id.zip",   "Found mismatched DOI in object-id");
  }

  private void ingestErrorTest(String zip, String expMsg) throws Exception {
    URL article = getClass().getResource(zip);

    Throwable gotE = null;
    String retUri = null;
    try {
      retUri = service.ingest(new DataHandler(article));
      service.delete(retUri);     // clean up in case of accidental success
    } catch (IngestException ie) {
      gotE = ie;
    }
    assertNotNull("Failed to get expected ingest exception", gotE);
    assertTrue("Failed to get expected exception message - got '" + gotE.getMessage() + "'",
               gotE.getMessage().indexOf(expMsg) >= 0);
  }

  public void testObjectInfo() throws Exception {
    // some NoSuchObjectIdException tests
    boolean gotE = false;
    try {
      service.getObjectInfo("info:doi/blah/foo");
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test getObjectInfo()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String uri = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", uri);

    ObjectInfo oi = service.getObjectInfo(uri);
    assertEquals("wrong uri", uri, oi.getUri());
    assertEquals("wrong doi", "10.1371/journal.pbio.0020294", oi.getDoi());
    assertEquals("wrong title",
                 "Regulation of Muscle Fiber Type and Running Endurance by PPAR\u00A0",
                 oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected superseded-by", oi.getSupersededBy());
    assertNull("unexpected context-element", oi.getContextElement());
    assertEquals("wrong state", 1, oi.getState());
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    RepresentationInfo[] ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    // test getObjectInfo on g001
    String secUri = uri + ".g001";
    oi = service.getObjectInfo(secUri);
    assertEquals("wrong uri", secUri, oi.getUri());
    assertEquals("wrong doi", "10.1371/journal.pbio.0020294.g001", oi.getDoi());
    assertEquals("wrong title", "Figure 1", oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected superseded-by", oi.getSupersededBy());
    assertEquals("wrong context-element", "fig", oi.getContextElement());
    assertEquals("wrong state", 1, oi.getState());
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 2, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "PNG", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "image/png", ri[0].getContentType());
    assertEquals("ri-size mismatch", 52422L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "TIF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri[1].getContentType());
    assertEquals("ri-size mismatch", 120432L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    // test getObjectInfo on sv001
    secUri = uri + ".sv001";
    oi = service.getObjectInfo(secUri);
    assertEquals("wrong uri", secUri, oi.getUri());
    assertEquals("wrong doi", "10.1371/journal.pbio.0020294.sv001", oi.getDoi());
    assertEquals("wrong title", "Video S1", oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected superseded-by", oi.getSupersededBy());
    assertEquals("wrong context-element", "supplementary-material", oi.getContextElement());
    assertEquals("wrong state", 1, oi.getState());
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 1, ri.length);

    assertEquals("ri-name mismatch", "MOV", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri[0].getContentType());
    assertEquals("ri-size mismatch", 0L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    // clean up
    service.delete(uri);
  }

  public void testRepresentations() throws Exception {
    // some NoSuchObjectIdException tests
    boolean gotE = false;
    try {
      service.setRepresentation("info:doi/blah/foo", "bar",
                                new DataHandler(new StringDataSource("Some random text")));
    } catch (NoSuchObjectIdException nsoie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsoie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test getObjectInfo()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String art = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", art);

    ObjectInfo oi = service.getObjectInfo(art);
    assertEquals("wrong uri", art, oi.getUri());
    assertEquals("wrong doi", "10.1371/journal.pbio.0020294", oi.getDoi());
    assertEquals("wrong title",
                 "Regulation of Muscle Fiber Type and Running Endurance by PPAR\u00A0",
                 oi.getTitle());
    assertNotNull("missing description", oi.getDescription());
    assertNull("unexpected superseded-by", oi.getSupersededBy());
    assertNull("unexpected context-element", oi.getContextElement());
    assertEquals("wrong state", 1, oi.getState());
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    RepresentationInfo[] ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    // create a new Representation
    service.setRepresentation(art, "TXT",
                              new DataHandler(new StringDataSource("The plain text")));

    oi = service.getObjectInfo(art);
    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 4, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "TXT", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/plain", ri[2].getContentType());
    assertEquals("ri-size mismatch", 14L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    assertEquals("ri-name mismatch", "XML", ri[3].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[3].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[3].getSize());
    assertNotNull("null ri-url", ri[3].getURL());

    // update a Representation
    service.setRepresentation(art, "TXT",
                          new DataHandler(new StringDataSource("The corrected text", "text/foo")));

    oi = service.getObjectInfo(art);
    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 4, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "TXT", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/foo", ri[2].getContentType());
    assertEquals("ri-size mismatch", 18L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    assertEquals("ri-name mismatch", "XML", ri[3].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[3].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[3].getSize());
    assertNotNull("null ri-url", ri[3].getURL());

    // remove a Representation
    service.setRepresentation(art, "TXT", null);

    oi = service.getObjectInfo(art);
    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    // remove a non-existent Representation
    service.setRepresentation(art, "TXT", null);

    oi = service.getObjectInfo(art);
    ri = oi.getRepresentations();
    assertEquals("wrong number of rep-infos", 3, ri.length);

    sort(ri);

    assertEquals("ri-name mismatch", "HTML", ri[0].getName());
    assertEquals("ri-cont-type mismatch", "text/html", ri[0].getContentType());
    assertEquals("ri-size mismatch", 51284L, ri[0].getSize());
    assertNotNull("null ri-url", ri[0].getURL());

    assertEquals("ri-name mismatch", "PDF", ri[1].getName());
    assertEquals("ri-cont-type mismatch", "application/pdf", ri[1].getContentType());
    assertEquals("ri-size mismatch", 81173L, ri[1].getSize());
    assertNotNull("null ri-url", ri[1].getURL());

    assertEquals("ri-name mismatch", "XML", ri[2].getName());
    assertEquals("ri-cont-type mismatch", "text/xml", ri[2].getContentType());
    assertEquals("ri-size mismatch", 65680L, ri[2].getSize());
    assertNotNull("null ri-url", ri[2].getURL());

    // clean up
    service.delete(art);
  }

  public void testSecondaryObjects() throws Exception {
    // some NoSuchArticleIdException tests
    boolean gotE = false;
    try {
      service.listSecondaryObjects("info:doi/blah/foo");
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test listRepresentations()
    URL article = getClass().getResource("/pbio.0020294.zip");
    String art = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", art);

    ObjectInfo[] oi = service.listSecondaryObjects(art);
    assertEquals("wrong number of object-infos", 8, oi.length);

    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g001",  oi[0].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g002",  oi[1].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g003",  oi[2].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g004",  oi[3].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g005",  oi[4].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.g006",  oi[5].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.sv001", oi[6].getUri());
    assertEquals("uri mismatch", "info:doi/10.1371/journal.pbio.0020294.sv002", oi[7].getUri());

    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g001",  oi[0].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g002",  oi[1].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g003",  oi[2].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g004",  oi[3].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g005",  oi[4].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.g006",  oi[5].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv001", oi[6].getDoi());
    assertEquals("doi mismatch", "10.1371/journal.pbio.0020294.sv002", oi[7].getDoi());

    assertEquals("label mismatch", "Figure 1", oi[0].getTitle());
    assertEquals("label mismatch", "Figure 2", oi[1].getTitle());
    assertEquals("label mismatch", "Figure 3", oi[2].getTitle());
    assertEquals("label mismatch", "Figure 4", oi[3].getTitle());
    assertEquals("label mismatch", "Figure 5", oi[4].getTitle());
    assertEquals("label mismatch", "Figure 6", oi[5].getTitle());
    assertEquals("label mismatch", "Video S1", oi[6].getTitle());
    assertEquals("label mismatch", "Video S2", oi[7].getTitle());

    assertNotNull("missing description", oi[0].getDescription());
    assertNotNull("missing description", oi[1].getDescription());
    assertNotNull("missing description", oi[2].getDescription());
    assertNotNull("missing description", oi[3].getDescription());
    assertNotNull("missing description", oi[4].getDescription());
    assertNotNull("missing description", oi[5].getDescription());
    assertNotNull("missing description", oi[6].getDescription());
    assertNotNull("missing description", oi[7].getDescription());

    assertNull("unexpected superseded-by", oi[0].getSupersededBy());
    assertNull("unexpected superseded-by", oi[1].getSupersededBy());
    assertNull("unexpected superseded-by", oi[2].getSupersededBy());
    assertNull("unexpected superseded-by", oi[3].getSupersededBy());
    assertNull("unexpected superseded-by", oi[4].getSupersededBy());
    assertNull("unexpected superseded-by", oi[5].getSupersededBy());
    assertNull("unexpected superseded-by", oi[6].getSupersededBy());
    assertNull("unexpected superseded-by", oi[7].getSupersededBy());

    assertEquals("wrong context-element", "fig", oi[0].getContextElement());
    assertEquals("wrong context-element", "fig", oi[1].getContextElement());
    assertEquals("wrong context-element", "fig", oi[2].getContextElement());
    assertEquals("wrong context-element", "fig", oi[3].getContextElement());
    assertEquals("wrong context-element", "fig", oi[4].getContextElement());
    assertEquals("wrong context-element", "fig", oi[5].getContextElement());
    assertEquals("wrong context-element", "supplementary-material", oi[6].getContextElement());
    assertEquals("wrong context-element", "supplementary-material", oi[7].getContextElement());

    assertEquals("wrong number of rep-infos", 2, oi[0].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[1].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[2].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[3].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[4].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[5].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[6].getRepresentations().length);
    assertEquals("wrong number of rep-infos", 1, oi[7].getRepresentations().length);

    assertEquals("wrong state", 1, oi[0].getState());
    assertEquals("wrong state", 1, oi[1].getState());
    assertEquals("wrong state", 1, oi[2].getState());
    assertEquals("wrong state", 1, oi[3].getState());
    assertEquals("wrong state", 1, oi[4].getState());
    assertEquals("wrong state", 1, oi[5].getState());
    assertEquals("wrong state", 1, oi[6].getState());
    assertEquals("wrong state", 1, oi[7].getState());

    assertNull("unexpected author-user-ids", oi[0].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[1].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[2].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[3].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[4].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[5].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[6].getAuthorUserIds());
    assertNull("unexpected author-user-ids", oi[7].getAuthorUserIds());

    sort(oi[0].getRepresentations());

    RepresentationInfo ri = oi[0].getRepresentations()[0];
    assertEquals("ri-name mismatch", "PNG", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/png", ri.getContentType());
    assertEquals("ri-size mismatch", 52422L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[0].getRepresentations()[1];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 120432L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[1].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 375480L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[2].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 170324L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[3].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 458812L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[4].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 164130L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[5].getRepresentations()[0];
    assertEquals("ri-name mismatch", "TIF", ri.getName());
    assertEquals("ri-cont-type mismatch", "image/tiff", ri.getContentType());
    assertEquals("ri-size mismatch", 101566L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[6].getRepresentations()[0];
    assertEquals("ri-name mismatch", "MOV", ri.getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
    assertEquals("ri-size mismatch", 0L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    ri = oi[7].getRepresentations()[0];
    assertEquals("ri-name mismatch", "MOV", ri.getName());
    assertEquals("ri-cont-type mismatch", "video/quicktime", ri.getContentType());
    assertEquals("ri-size mismatch", 0L, ri.getSize());
    assertNotNull("null ri-url", ri.getURL());

    // clean up
    service.delete(art);
  }

  public void testState() throws Exception {
    // some NoSuchArticleIdException tests
    boolean gotE = false;
    try {
      service.setState("info:doi/blah/foo", 0);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test
    URL article = getClass().getResource("/pbio.0020294.zip");
    String art = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", art);

    ObjectInfo oi = service.getObjectInfo(art);
    assertEquals("wrong state", 1, oi.getState());

    ObjectInfo[] ois = service.listSecondaryObjects(art);
    assertEquals("wrong number of object-infos", 8, ois.length);

    assertEquals("wrong state", 1, ois[0].getState());
    assertEquals("wrong state", 1, ois[1].getState());
    assertEquals("wrong state", 1, ois[2].getState());
    assertEquals("wrong state", 1, ois[3].getState());
    assertEquals("wrong state", 1, ois[4].getState());
    assertEquals("wrong state", 1, ois[5].getState());
    assertEquals("wrong state", 1, ois[6].getState());
    assertEquals("wrong state", 1, ois[7].getState());

    String res = service.getArticles(null, null, null, null, new int[] { 0 }, false);
    assertFalse("Unexpectedly found article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { 1 }, false);
    assertTrue("Failed to find article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { 0, 1 }, false);
    assertTrue("Failed to find article", hasArticle(res));

    service.setState(art, 42);

    oi = service.getObjectInfo(art);
    assertEquals("wrong state", 42, oi.getState());

    ois = service.listSecondaryObjects(art);
    assertEquals("wrong number of object-infos", 8, ois.length);

    assertEquals("wrong state", 42, ois[0].getState());
    assertEquals("wrong state", 42, ois[1].getState());
    assertEquals("wrong state", 42, ois[2].getState());
    assertEquals("wrong state", 42, ois[3].getState());
    assertEquals("wrong state", 42, ois[4].getState());
    assertEquals("wrong state", 42, ois[5].getState());
    assertEquals("wrong state", 42, ois[6].getState());
    assertEquals("wrong state", 42, ois[7].getState());

    res = service.getArticles(null, null, null, null, new int[] { 0, 1 }, false);
    assertFalse("Unexpectedly found article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { 42 }, false);
    assertTrue("Failed to find article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { 42, 1 }, false);
    assertTrue("Failed to find article", hasArticle(res));

    service.setState(art, Article.ST_ACTIVE);

    oi = service.getObjectInfo(art);
    assertEquals("wrong state", Article.ST_ACTIVE, oi.getState());

    ois = service.listSecondaryObjects(art);
    assertEquals("wrong number of object-infos", 8, ois.length);

    assertEquals("wrong state", Article.ST_ACTIVE, ois[0].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[1].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[2].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[3].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[4].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[5].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[6].getState());
    assertEquals("wrong state", Article.ST_ACTIVE, ois[7].getState());

    res = service.getArticles(null, null, null, null, new int[] { 2, 1 }, false);
    assertFalse("Unexpectedly found article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { Article.ST_ACTIVE }, false);
    assertTrue("Failed to find article", hasArticle(res));
    res = service.getArticles(null, null, null, null, new int[] { 42, Article.ST_ACTIVE }, false);
    assertTrue("Failed to find article", hasArticle(res));

    // clean up
    service.delete(art);
  }

  public void testAuthorUserIds() throws Exception {
    // some NoSuchArticleIdException tests
    boolean gotE = false;
    try {
      service.setAuthorUserIds("info:doi/blah/foo", null);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-object-id exception", gotE);

    // ingest article and test
    URL article = getClass().getResource("/pbio.0020294.zip");
    String art = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", art);

    ObjectInfo oi = service.getObjectInfo(art);
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    service.setAuthorUserIds(art, null);

    oi = service.getObjectInfo(art);
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    service.setAuthorUserIds(art, new String[0]);

    oi = service.getObjectInfo(art);
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    String[] ids = new String[] { "foo:bar" };
    service.setAuthorUserIds(art, ids);

    oi = service.getObjectInfo(art);
    Arrays.sort(ids);
    Arrays.sort(oi.getAuthorUserIds());
    assertTrue("mismatched author-user-ids - got " + Arrays.asList(oi.getAuthorUserIds()),
               Arrays.equals(ids, oi.getAuthorUserIds()));

    ids = new String[] { "foo:bar", "bar:baz" };
    service.setAuthorUserIds(art, ids);

    oi = service.getObjectInfo(art);
    Arrays.sort(ids);
    Arrays.sort(oi.getAuthorUserIds());
    assertTrue("mismatched author-user-ids - got " + Arrays.asList(oi.getAuthorUserIds()),
               Arrays.equals(ids, oi.getAuthorUserIds()));

    service.setAuthorUserIds(art, null);

    oi = service.getObjectInfo(art);
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    ids = new String[] { "foo:bar", "bar:baz", "baz:blah" };
    service.setAuthorUserIds(art, ids);

    oi = service.getObjectInfo(art);
    Arrays.sort(ids);
    Arrays.sort(oi.getAuthorUserIds());
    assertTrue("mismatched author-user-ids - got " + Arrays.asList(oi.getAuthorUserIds()),
               Arrays.equals(ids, oi.getAuthorUserIds()));

    service.setAuthorUserIds(art, new String[0]);

    oi = service.getObjectInfo(art);
    assertNull("unexpected author-user-ids", oi.getAuthorUserIds());

    // clean up
    service.delete(art);
  }

  public void testSuperseded() throws Exception {
    // some NoSuchArticleIdException tests
    boolean gotE = false;
    try {
      service.markSuperseded("info:doi/blah/foo", "info:doi/blah/bar");
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/foo", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-article-id exception", gotE);

    URL article = getClass().getResource("/pbio.0020294.zip");
    String art1 = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020294", art1);

    gotE = false;
    try {
      service.markSuperseded(art1, "info:doi/blah/bar");
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/bar", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-article-id exception", gotE);

    gotE = false;
    try {
      service.markSuperseded("info:doi/blah/bar", art1);
    } catch (NoSuchArticleIdException nsaie) {
      assertEquals("Mismatched id in exception, ", "info:doi/blah/bar", nsaie.getId());
      gotE = true;
    }
    assertTrue("Failed to get expected no-such-article-id exception", gotE);

    // test that it works
    article = getClass().getResource("/pbio.0020042.zip");
    String art2 = service.ingest(new DataHandler(article));
    assertEquals("Wrong uri returned,", "info:doi/10.1371/journal.pbio.0020042", art2);

    service.markSuperseded(art1, art2);

    ObjectInfo oi = service.getObjectInfo(art1);
    assertEquals("wrong superseded-by", art2, oi.getSupersededBy());

    oi = service.getObjectInfo(art2);
    assertNull("unexpected superseded-by", oi.getSupersededBy());

    // clean up
    service.delete(art1);
    service.delete(art2);
  }

  private static final boolean hasArticle(String searchResult) {
    return !searchResult.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<articles>\n</articles>\n");
  }

  private static byte[] loadURL(URL url) throws IOException {
    URLConnection con = url.openConnection();
    con.connect();
    byte[] res = new byte[con.getContentLength()];

    InputStream is = con.getInputStream();
    is.read(res);
    is.close();

    return res;
  }

  private static void sort(RepresentationInfo[] ri) {
    Arrays.sort(ri, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((RepresentationInfo) o1).getName().compareTo(((RepresentationInfo) o2).getName());
      }
    });
  }

  private static class StringDataSource implements DataSource {
    private final String src;
    private final String ct;

    public StringDataSource(String content) {
      this(content, "text/plain");
    }

    public StringDataSource(String content, String contType) {
      src = content;
      ct  = contType;
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(src.getBytes("UTF-8"));
    }

    public OutputStream getOutputStream() throws IOException {
      throw new IOException("Not supported");
    }

    public String getContentType() {
      return ct;
    }

    public String getName() {
      return "string";
    }
  }
}
