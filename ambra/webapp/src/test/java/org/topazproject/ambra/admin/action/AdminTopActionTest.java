/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.topazproject.ambra.admin.action;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.TestException;
import org.topazproject.ambra.admin.service.DocumentManagementService;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.SyndicationService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.Ingester;
import org.topazproject.otm.Session;
import org.easymock.classextension.IMocksControl;
import static org.easymock.classextension.EasyMock.*;
import static org.testng.Assert.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;

/**
 * Test all of the public "actions" in the AdminTopAction class.
 *
 * TODO: Add Syndications of various types to ALL tests so setCommonFields() is thoroughly tested.
 *
 * @author Scott Sterling
 */
public class AdminTopActionTest {

  private Configuration configuration;

  /**
   * Get new object every time to ensure that tests do not overwrite the contents
   * @return a <b>new</b> test object
   */
  private static Syndication getTestSyndicationOne() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", "testStatusOne", "testTargetOne", 1,
        new Date(1L), new Date(11L), "testErrorMessageOne");
  }
  private static Syndication getTestSyndicationTwo() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", "testStatusTwo", "testTargetTwo", 1,
        new Date(2L), new Date(22L), "testErrorMessageTwo");
  }
  private static Article getTestArticle() {
    Article article = new Article();
    try {
      article.setId(new URI("info:doi/00.0000/fake.article.id"));
    } catch (URISyntaxException e) {
      throw new TestException("Could not set Article.id = info:doi/00.0000/fake.article.id" +
          " for test Article returned by method getNewArticle()");
    }
    return article;
  }

  /**
   * Access a fake config file so that the complex behaviors of Configuration do not have to
   * be mocked.
   *
   * @throws org.apache.commons.configuration.ConfigurationException
   */
  @BeforeClass
  public void readConfiguration() throws ConfigurationException {
    String fileName = getClass().getClassLoader().getResource("queue/configuration.xml").getFile();
    configuration = new XMLConfiguration(fileName);
  }

  /**
   * The setCommonFields() method is called by every action.  This test method tells the
   *   testing framework what to expect from the setCommonFields() method.
   */
  private void testSetCommonFieldsBeforeReplay(
      AdminService adminService, DocumentManagementService documentManagementService,
      VirtualJournalContext virtualJournalContext, SyndicationService synService)
      throws URISyntaxException, ApplicationException {

    // Names of files that are ready to be ingested as Articles
    List<String> uploadableFiles = new LinkedList<String>();
    uploadableFiles.add("fileToBeUploadedOne");
    uploadableFiles.add("fileToBeUploadedTwo");
    uploadableFiles.add("fileToBeUploadedThree");

    // Articles that have been ingested but have not yet been published
    Map<String, Article> publishableArticles = new HashMap<String, Article>();
    publishableArticles.put(getTestArticle().getId().toString(), getTestArticle());

    // Syndications queried from SyndicationService
    List<SyndicationService.SyndicationDTO> syndicationList =
        new LinkedList<SyndicationService.SyndicationDTO>();
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationOne()));
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationTwo()));

    expect(virtualJournalContext.getJournal())
        .andReturn("fakeJournalKey");
    expect(adminService.createJournalInfo("fakeJournalKey")).andReturn(new AdminService.JournalInfo());
    expect(documentManagementService.getUploadableFiles())
        .andReturn(uploadableFiles);
    expect(documentManagementService.getAutoIngestFiles())
        .andReturn(uploadableFiles);
    expect(documentManagementService.getPublishableArticles())
        .andReturn(publishableArticles);
    expect(virtualJournalContext.getJournal())
        .andReturn("fakeJournalKey");
    expect(synService.getFailedAndInProgressSyndications("fakeJournalKey"))
        .andReturn(syndicationList);
    expect(synService.querySyndication(getTestArticle().getId().toString()))
        .andReturn(syndicationList);
    //  TODO: This is the wrong object.  ??? Do I need a new one ???
  }

  /**
   * The setCommonFields() method is called by every action.
   * This output is usually interpreted by the FTL, so this method just checks that the data
   *   is available through the appropriate "get" methods used by the FTL.
   */
  private void testSetCommonFieldsAfterVerify(AdminTopAction adminTopAction) {
    assertNotNull(adminTopAction.getIsFailedSyndications());
    assertEquals(adminTopAction.getIsFailedSyndications(), new Boolean(false));

    assertNotNull(adminTopAction.getPublishableArticles());
    assertEquals(adminTopAction.getPublishableArticles().size(), 1);

    assertNotNull(adminTopAction.getPublishableSyndications());
    assertEquals(adminTopAction.getPublishableSyndications().size(), 1);//publishableArticles.size()

    assertNotNull(adminTopAction.getSyndications());
    assertEquals(adminTopAction.getSyndications().size(), 2);

    assertNotNull(adminTopAction.getUploadableFiles());
    assertEquals(adminTopAction.getUploadableFiles().size(), 3);

    assertNotNull(adminTopAction.getAutoIngestFiles());
    assertEquals(adminTopAction.getAutoIngestFiles().size(), 3);

    //TODO: Also "assert" for content for all of the values read by the FTL (i.e.., non-boolean "get" methods)
    
  }

  /**
   * Test the deletion of ONE article.
   */
  @Test
  public void testDelete() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    // From action: delete
    documentManagementService.delete("info:doi/00.0000/fake.article.id.to.delete");
    documentManagementService.revertIngestedQueue("info:doi/00.0000/fake.article.id.to.delete");

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    // ID for the Article which will be deleted.
    adminTopAction.setArticle("info:doi/00.0000/fake.article.id.to.delete");
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.delete();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);
  }

  /**
   * The execute() method does nothing but call setCommonFields().
   */
  @Test
  public void testExecute() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.execute();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);
  }

  @Test
  public void testIngest() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);
    Ingester ingester = ctrl.createMock(Ingester.class);
    Session session = ctrl.createMock(Session.class);

    File file = new File("fakeDirectoryName/ingestibleFileOne");
    Article article = getTestArticle(); // Must be a local object or "session.evict()" fails.

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    // Names of files that will be ingested
    String[] filesToIngest = {"ingestibleFileOne"};

    // From action: ingest
    expect(documentManagementService.getDocumentDirectory()).andReturn("fakeDirectoryName");
    expect(documentManagementService.createIngester(file)).andReturn(ingester);
    ingester.prepare(configuration);
    expect(documentManagementService.ingest(ingester, new Boolean(false)))
        .andReturn(article);
    expect(session.evict(article)).andReturn("");
    documentManagementService.generateIngestedData(file, article);

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.setOtmSession(session);
    adminTopAction.setAmbraConfiguration(configuration);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setFilesToIngest(filesToIngest);
    adminTopAction.ingest();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);
  }

  /**
   * Test the deletion of multiple articles.
   *
   * TODO: add a Syndication that FAILs and then check to see that messageError gets populated.
   *
   * @throws Exception
   */
  @Test
  public void testProcessArticlesActionDelete() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    // IDs of Articles that will be deleted
    String[] articlesToBeProcessed = {"info:doi/00.0001/fake.article.id",
        "info:doi/00.0002/fake.article.id", "info:doi/00.0003/fake.article.id"};

    // Error and success messages returned from method DocumentManagementService.delete()
    List<String> detetionMessages = new LinkedList<String>();
    detetionMessages.add("This is test success message for deleting fake article One.");
    detetionMessages.add("This is test success message for deleting fake article Two.");
    detetionMessages.add("This is test success message for deleting fake article Three.");

    // From action: processArticles
    expect(documentManagementService.delete(articlesToBeProcessed)).andReturn(detetionMessages);
    documentManagementService.revertIngestedQueue("info:doi/00.0001/fake.article.id");
    documentManagementService.revertIngestedQueue("info:doi/00.0002/fake.article.id");
    documentManagementService.revertIngestedQueue("info:doi/00.0003/fake.article.id");

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.setArticles(articlesToBeProcessed);
    adminTopAction.setAction("Delete");
    adminTopAction.processArticles();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);
    assertEquals(adminTopAction.getActionMessages(), detetionMessages);
  }

  @Test
  public void testProcessArticlesPublishAndSyndicate() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    // IDs of Articles that will be published
    String[] articlesToBePublished = {"info:doi/00.0001/fake.article.id",
        "info:doi/00.0002/fake.article.id", "info:doi/00.0003/fake.article.id"};

    // IDs of Articles that will be syndicated
    String[] articlesToBeSyndicated = {"info:doi/00.0001/fake.article.id::testTargetOne",
        "info:doi/00.0003/fake.article.id::testTargetThree"};

    // Error and success messages returned from method DocumentManagementService.publish()
    List<String> publishMessages = new LinkedList<String>();
    publishMessages.add("This is test message for publishing fake article One.");
    publishMessages.add("This is test message for publishing fake article Two.");
    publishMessages.add("This is test message for publishing fake article Three.");

    // Error and success messages returned from method DocumentManagementService.publish()
    List<String> syndicateMessages = new LinkedList<String>();
    syndicateMessages.add("Syndicated: info:doi/00.0001/fake.article.id to testTargetOne");
    syndicateMessages.add("Syndicated: info:doi/00.0003/fake.article.id to testTargetThree");

    // From action: publishArticles
    expect(documentManagementService.publish(articlesToBePublished))
        .andReturn(publishMessages);
    // From action: syndicateArticles
    expect(synService.syndicate("info:doi/00.0001/fake.article.id", "testTargetOne"))
        .andReturn(getTestSyndicationOne());
    expect(synService.syndicate("info:doi/00.0003/fake.article.id", "testTargetThree"))
        .andReturn(getTestSyndicationTwo());

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.setArticles(articlesToBePublished);
    adminTopAction.setSyndicates(articlesToBeSyndicated);
    adminTopAction.setAction("Publish and Syndicate");
    adminTopAction.processArticles();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);

    publishMessages.addAll(syndicateMessages); // So that publishMessages contains ALL messages.
    assertEquals(adminTopAction.getActionMessages(), publishMessages);
  }

  @Test
  public void testResyndicateFailedArticles() throws Exception {
    IMocksControl ctrl = createStrictControl();

    DocumentManagementService documentManagementService =
        ctrl.createMock(DocumentManagementService.class);
    AdminService adminService = ctrl.createMock(AdminService.class);
    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    // IDs of Articles that will be syndicated
    String[] articlesToBeSyndicated = {"info:doi/00.0001/fake.article.id::testTargetOne",
        "info:doi/00.0003/fake.article.id::testTargetThree"};

    // From action: syndicateArticles
    expect(synService.syndicate("info:doi/00.0001/fake.article.id", "testTargetOne"))
        .andReturn(getTestSyndicationOne());
    expect(synService.syndicate("info:doi/00.0003/fake.article.id", "testTargetThree"))
        .andReturn(getTestSyndicationTwo());

    testSetCommonFieldsBeforeReplay(adminService, documentManagementService,
                                    virtualJournalContext, synService);

    ctrl.replay();

    AdminTopAction adminTopAction = new AdminTopAction();
    adminTopAction.setDocumentManagementService(documentManagementService);
    adminTopAction.setAdminService(adminService);
    adminTopAction.setRequest(requestAttributes);
    adminTopAction.setSyndicationService(synService);
    adminTopAction.setResyndicates(articlesToBeSyndicated);
    adminTopAction.resyndicateFailedArticles();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(adminTopAction);
  }
}