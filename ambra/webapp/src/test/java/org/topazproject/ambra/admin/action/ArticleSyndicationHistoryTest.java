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

import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.admin.service.AdminService;
import org.topazproject.ambra.admin.service.SyndicationService;
import org.topazproject.ambra.ApplicationException;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.easymock.classextension.IMocksControl;
import org.topazproject.ambra.web.VirtualJournalContext;

import static org.easymock.classextension.EasyMock.createStrictControl;
import static org.easymock.classextension.EasyMock.*;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Scott Sterling
 */
public class ArticleSyndicationHistoryTest {

  /**
   * Get new object every time to ensure that tests do not overwrite the contents
   * @return a <b>new</b> test object
   */
  private static Syndication getTestSyndicationOne() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", Syndication.STATUS_PENDING,
        "testTargetOne", 0, new Date(1L), new Date(11L), null);
  }
  private static Syndication getTestSyndicationTwo() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", Syndication.STATUS_SUCCESS,
        "testTargetTwo", 3, new Date(2L), new Date(22L), null);
  }
  private static Syndication getTestSyndicationThree() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", Syndication.STATUS_FAILURE,
        "testTargetThree", 5, new Date(3L), new Date(33L), "testErrorMessageThree");
  }
  private static Syndication getTestSyndicationFour() throws URISyntaxException {
    return new Syndication("info:doi/00.0000/fake.article.id", Syndication.STATUS_IN_PROGRESS,
        "testTargetFour", 7, new Date(4L), new Date(4L), "testErrorMessageFour");
  }

  /**
   * The execute() method does nothing but call setCommonFields().
   */
  @Test
  public void testExecute() throws Exception {
    IMocksControl ctrl = createStrictControl();
    AdminService adminService = ctrl.createMock(AdminService.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);


    testSetCommonFieldsBeforeReplay(adminService, synService, virtualJournalContext);

    ctrl.replay();

    ArticleSyndicationHistory articleSyndicationHistory = new ArticleSyndicationHistory();
    articleSyndicationHistory.setRequest(requestAttributes);
    articleSyndicationHistory.setArticle("info:doi/00.0000/fake.article.id");
    articleSyndicationHistory.setAdminService(adminService);
    articleSyndicationHistory.setSyndicationService(synService);
    articleSyndicationHistory.execute();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(articleSyndicationHistory);

    assertEquals(articleSyndicationHistory.getArticle(), "info:doi/00.0000/fake.article.id");
  }

  @Test
  public void testMarkSyndicationAsFailed() throws Exception {
    IMocksControl ctrl = createStrictControl();
    AdminService adminService = ctrl.createMock(AdminService.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);

    String[] syndicationTargets = {"testTargetOne", "testTargetThree"};

    expect(synService.updateSyndication("info:doi/00.0000/fake.article.id", "testTargetOne",
        Syndication.STATUS_FAILURE, "Status manually changed to " + Syndication.STATUS_FAILURE))
        .andReturn(getTestSyndicationOne());
    expect(synService.updateSyndication("info:doi/00.0000/fake.article.id", "testTargetThree",
        Syndication.STATUS_FAILURE, "Status manually changed to " + Syndication.STATUS_FAILURE))
        .andReturn(getTestSyndicationThree());
    expect(synService.querySyndication("info:doi/00.0000/fake.article.id"))
        .andReturn(new LinkedList<SyndicationService.SyndicationDTO>());

    testSetCommonFieldsBeforeReplay(adminService, synService, virtualJournalContext);

    ctrl.replay();

    ArticleSyndicationHistory articleSyndicationHistory = new ArticleSyndicationHistory();
    articleSyndicationHistory.setRequest(requestAttributes);
    articleSyndicationHistory.setArticle("info:doi/00.0000/fake.article.id");
    articleSyndicationHistory.setTarget(syndicationTargets);
    articleSyndicationHistory.setAdminService(adminService);
    articleSyndicationHistory.setSyndicationService(synService);
    articleSyndicationHistory.markSyndicationAsFailed();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(articleSyndicationHistory);

    assertEquals(articleSyndicationHistory.getArticle(), "info:doi/00.0000/fake.article.id");
  }

  @Test
  public void testResyndicate() throws Exception {
    IMocksControl ctrl = createStrictControl();
    AdminService adminService = ctrl.createMock(AdminService.class);
    SyndicationService synService = ctrl.createMock(SyndicationService.class);

    VirtualJournalContext virtualJournalContext = ctrl.createMock(VirtualJournalContext.class);

    // Attributes from the Request Scope that are made available to through BaseActionSupport.
    Map requestAttributes = new HashMap<String, Object>();
    requestAttributes.put("ambra.virtualjournal.context", virtualJournalContext);


    String[] resyndicationTargets = {"testTargetOne", "testTargetThree"};

    expect(synService.syndicate("info:doi/00.0000/fake.article.id", "testTargetOne"))
        .andReturn(getTestSyndicationOne());
    expect(synService.syndicate("info:doi/00.0000/fake.article.id", "testTargetThree"))
        .andReturn(getTestSyndicationThree());
    expect(synService.querySyndication("info:doi/00.0000/fake.article.id"))
        .andReturn(new LinkedList<SyndicationService.SyndicationDTO>());

    testSetCommonFieldsBeforeReplay(adminService, synService, virtualJournalContext);

    ctrl.replay();

    ArticleSyndicationHistory articleSyndicationHistory = new ArticleSyndicationHistory();
    articleSyndicationHistory.setRequest(requestAttributes);
    articleSyndicationHistory.setArticle("info:doi/00.0000/fake.article.id");
    articleSyndicationHistory.setTarget(resyndicationTargets);
    articleSyndicationHistory.setAdminService(adminService);
    articleSyndicationHistory.setSyndicationService(synService);
    articleSyndicationHistory.resyndicate();

    ctrl.verify();

    testSetCommonFieldsAfterVerify(articleSyndicationHistory);

    assertEquals(articleSyndicationHistory.getArticle(), "info:doi/00.0000/fake.article.id");
  }

  /**
   * The setCommonFields() method is called by every action.  This test method tells the
   *   testing framework what to expect from the setCommonFields() method.
   */
  private void testSetCommonFieldsBeforeReplay(AdminService adminService,
               SyndicationService synService, VirtualJournalContext virtualJournalContext)
      throws URISyntaxException, ApplicationException {

    // Syndications queried from SyndicationService
    List<SyndicationService.SyndicationDTO> syndicationList =
        new LinkedList<SyndicationService.SyndicationDTO>();
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationOne()));
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationTwo()));
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationThree()));
    syndicationList.add(new SyndicationService.SyndicationDTO(getTestSyndicationFour()));

    expect(virtualJournalContext.getJournal())
        .andReturn("fakeJournalKey");
    expect(adminService.createJournalInfo("fakeJournalKey")).andReturn(new AdminService.JournalInfo());
    expect(synService.querySyndication("info:doi/00.0000/fake.article.id"))
        .andReturn(syndicationList);
  }

  /**
   * The setCommonFields() method is called by every action.
   * This output is usually interpreted by the FTL, so this method just checks that the data
   *   is available through the appropriate "get" methods used by the FTL.
   */
  private void testSetCommonFieldsAfterVerify(ArticleSyndicationHistory articleSyndicationHistory) {
    assertNotNull(articleSyndicationHistory.getSyndicationHistory());
    assertEquals(articleSyndicationHistory.getSyndicationHistory().size(), 4);

    assertNotNull(articleSyndicationHistory.getFinishedSyndications());
    assertEquals(articleSyndicationHistory.getFinishedSyndications().size(), 3);

    //TODO: Also "assert" for content for all of the values read by the FTL (i.e.., non-boolean "get" methods)
  }
}
